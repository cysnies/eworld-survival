package net.citizensnpcs;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.ai.speech.SpeechFactory;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.CommandManager;
import net.citizensnpcs.api.command.Injector;
import net.citizensnpcs.api.event.CitizensDisableEvent;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.citizensnpcs.api.event.CitizensReloadEvent;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.npc.SimpleNPCDataStore;
import net.citizensnpcs.api.scripting.EventRegistrar;
import net.citizensnpcs.api.scripting.ObjectProvider;
import net.citizensnpcs.api.scripting.ScriptCompiler;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitFactory;
import net.citizensnpcs.api.util.DatabaseStorage;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.NBTStorage;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.api.util.Translator;
import net.citizensnpcs.api.util.YamlStorage;
import net.citizensnpcs.commands.AdminCommands;
import net.citizensnpcs.commands.EditorCommands;
import net.citizensnpcs.commands.NPCCommands;
import net.citizensnpcs.commands.TemplateCommands;
import net.citizensnpcs.commands.TraitCommands;
import net.citizensnpcs.commands.WaypointCommands;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.npc.CitizensNPCRegistry;
import net.citizensnpcs.npc.CitizensTraitFactory;
import net.citizensnpcs.npc.NPCSelector;
import net.citizensnpcs.npc.ai.speech.Chat;
import net.citizensnpcs.npc.ai.speech.CitizensSpeechFactory;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.StringHelper;
import net.citizensnpcs.util.Util;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Citizens extends JavaPlugin implements CitizensPlugin {
   private final CommandManager commands = new CommandManager();
   private boolean compatible;
   private Settings config;
   private CitizensNPCRegistry npcRegistry;
   private NPCDataStore saves;
   private NPCSelector selector;
   private CitizensSpeechFactory speechFactory;
   private final Map storedRegistries = Maps.newHashMap();
   private CitizensTraitFactory traitFactory;
   private static final String COMPATIBLE_MC_VERSION = "1.6.2";

   public Citizens() {
      super();
   }

   public NPCRegistry createAnonymousNPCRegistry(NPCDataStore store) {
      return new CitizensNPCRegistry(store);
   }

   public NPCRegistry createNamedNPCRegistry(String name, NPCDataStore store) {
      NPCRegistry created = new CitizensNPCRegistry(store);
      this.storedRegistries.put(name, created);
      return created;
   }

   private NPCDataStore createStorage(File folder) {
      Storage saves = null;
      String type = Settings.Setting.STORAGE_TYPE.asString();
      if (!type.equalsIgnoreCase("db") && !type.equalsIgnoreCase("database")) {
         if (type.equalsIgnoreCase("nbt")) {
            saves = new NBTStorage(new File(folder + File.separator + Settings.Setting.STORAGE_FILE.asString()), "Citizens NPC Storage");
         }
      } else {
         try {
            saves = new DatabaseStorage(Settings.Setting.DATABASE_DRIVER.asString(), Settings.Setting.DATABASE_URL.asString(), Settings.Setting.DATABASE_USERNAME.asString(), Settings.Setting.DATABASE_PASSWORD.asString());
         } catch (SQLException e) {
            e.printStackTrace();
            Messaging.logTr("citizens.notifications.database-connection-failed");
         }
      }

      if (saves == null) {
         saves = new YamlStorage(new File(folder, Settings.Setting.STORAGE_FILE.asString()), "Citizens NPC Storage");
      }

      return !saves.load() ? null : SimpleNPCDataStore.create(saves);
   }

   private void despawnNPCs() {
      for(Iterator<NPC> itr = this.npcRegistry.iterator(); itr.hasNext(); itr.remove()) {
         NPC npc = (NPC)itr.next();

         try {
            npc.despawn(DespawnReason.REMOVAL);

            for(Trait trait : npc.getTraits()) {
               trait.onRemove();
            }
         } catch (Throwable e) {
            e.printStackTrace();
         }
      }

   }

   private void enableSubPlugins() {
      File root = new File(this.getDataFolder(), Settings.Setting.SUBPLUGIN_FOLDER.asString());
      if (root.exists() && root.isDirectory()) {
         File[] files = root.listFiles();

         for(File file : files) {
            Plugin plugin;
            try {
               plugin = Bukkit.getPluginManager().loadPlugin(file);
            } catch (Exception var10) {
               continue;
            }

            if (plugin != null) {
               try {
                  Messaging.logTr("citizens.sub-plugins.load", plugin.getDescription().getFullName());
                  plugin.onLoad();
               } catch (Throwable ex) {
                  Messaging.severeTr("citizens.sub-plugins.error-on-load", ex.getMessage(), plugin.getDescription().getFullName());
                  ex.printStackTrace();
               }
            }
         }

         NMS.loadPlugins();
      }
   }

   public CommandManager.CommandInfo getCommandInfo(String rootCommand, String modifier) {
      return this.commands.getCommand(rootCommand, modifier);
   }

   public Iterable getCommands(String base) {
      return this.commands.getCommands(base);
   }

   public net.citizensnpcs.api.npc.NPCSelector getDefaultNPCSelector() {
      return this.selector;
   }

   public NPCRegistry getNamedNPCRegistry(String name) {
      return (NPCRegistry)this.storedRegistries.get(name);
   }

   public NPCRegistry getNPCRegistry() {
      return this.npcRegistry;
   }

   public NPCSelector getNPCSelector() {
      return this.selector;
   }

   public ClassLoader getOwningClassLoader() {
      return this.getClassLoader();
   }

   public File getScriptFolder() {
      return new File(this.getDataFolder(), "scripts");
   }

   public SpeechFactory getSpeechFactory() {
      return this.speechFactory;
   }

   public TraitFactory getTraitFactory() {
      return this.traitFactory;
   }

   public boolean onCommand(CommandSender sender, Command command, String cmdName, String[] args) {
      String modifier = args.length > 0 ? args[0] : "";
      if (!this.commands.hasCommand(command, modifier) && !modifier.isEmpty()) {
         return this.suggestClosestModifier(sender, command.getName(), modifier);
      } else {
         NPC npc = this.selector == null ? null : this.selector.getSelected(sender);
         Object[] methodArgs = new Object[]{sender, npc};
         return this.commands.executeSafe(command, args, sender, methodArgs);
      }
   }

   public void onDisable() {
      Bukkit.getPluginManager().callEvent(new CitizensDisableEvent());
      Editor.leaveAll();
      CitizensAPI.shutdown();
      if (this.compatible) {
         this.saves.storeAll(this.npcRegistry);
         this.saves.saveToDiskImmediate();
         this.despawnNPCs();
         this.npcRegistry = null;
      }

   }

   public void onEnable() {
      this.setupTranslator();
      CitizensAPI.setImplementation(this);
      this.config = new Settings(this.getDataFolder());
      String mcVersion = Util.getMinecraftVersion();
      this.compatible = mcVersion.startsWith("1.6.2");
      if (Settings.Setting.CHECK_MINECRAFT_VERSION.asBoolean() && !this.compatible) {
         Messaging.severeTr("citizens.notifications.incompatible-version", this.getDescription().getVersion(), mcVersion);
         this.getServer().getPluginManager().disablePlugin(this);
      } else {
         this.registerScriptHelpers();
         this.saves = this.createStorage(this.getDataFolder());
         if (this.saves == null) {
            Messaging.severeTr("citizens.saves.load-failed");
            this.getServer().getPluginManager().disablePlugin(this);
         } else {
            this.npcRegistry = new CitizensNPCRegistry(this.saves);
            this.traitFactory = new CitizensTraitFactory();
            this.selector = new NPCSelector(this);
            this.speechFactory = new CitizensSpeechFactory();
            this.speechFactory.register(Chat.class, "chat");
            this.getServer().getPluginManager().registerEvents(new EventListen(this.storedRegistries), this);
            if (Settings.Setting.NPC_COST.asDouble() > (double)0.0F) {
               this.setupEconomy();
            }

            this.registerCommands();
            this.enableSubPlugins();
            if (this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
               public void run() {
                  Citizens.this.saves.loadInto(Citizens.this.npcRegistry);
                  Messaging.logTr("citizens.notifications.npcs-loaded", Iterables.size(Citizens.this.npcRegistry), "?");
                  Citizens.this.startMetrics();
                  Citizens.this.scheduleSaveTask(Settings.Setting.SAVE_TASK_DELAY.asInt());
                  Bukkit.getPluginManager().callEvent(new CitizensEnableEvent());
               }
            }, 1L) == -1) {
               Messaging.severeTr("citizens.load-task-error");
               this.getServer().getPluginManager().disablePlugin(this);
            }

         }
      }
   }

   public void onImplementationChanged() {
      Messaging.severeTr("citizens.changed-implementation");
      Bukkit.getPluginManager().disablePlugin(this);
   }

   public void registerCommandClass(Class clazz) {
      try {
         this.commands.register(clazz);
      } catch (Throwable ex) {
         Messaging.logTr("citizens.commands.invalid.class");
         ex.printStackTrace();
      }

   }

   private void registerCommands() {
      this.commands.setInjector(new Injector(new Object[]{this}));
      this.commands.register(AdminCommands.class);
      this.commands.register(EditorCommands.class);
      this.commands.register(NPCCommands.class);
      this.commands.register(TemplateCommands.class);
      this.commands.register(TraitCommands.class);
      this.commands.register(WaypointCommands.class);
   }

   private void registerScriptHelpers() {
      ScriptCompiler compiler = CitizensAPI.getScriptCompiler();
      compiler.registerGlobalContextProvider(new EventRegistrar(this));
      compiler.registerGlobalContextProvider(new ObjectProvider("plugin", this));
   }

   public void reload() throws NPCLoadException {
      Editor.leaveAll();
      this.config.reload();
      this.despawnNPCs();
      this.saves.loadInto(this.npcRegistry);
      this.getServer().getPluginManager().callEvent(new CitizensReloadEvent());
   }

   public void removeNamedNPCRegistry(String name) {
      this.storedRegistries.remove(name);
   }

   private void scheduleSaveTask(int delay) {
      Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
         public void run() {
            Citizens.this.storeNPCs();
            Citizens.this.saves.saveToDisk();
         }
      });
   }

   private void setupEconomy() {
      try {
         RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
         if (provider != null && provider.getProvider() != null) {
            Economy economy = (Economy)provider.getProvider();
            Bukkit.getPluginManager().registerEvents(new PaymentListener(economy), this);
         }
      } catch (NoClassDefFoundError var3) {
         Messaging.logTr("citizens.economy.error-loading");
      }

   }

   private void setupTranslator() {
      Locale locale = Locale.getDefault();
      String setting = Settings.Setting.LOCALE.asString();
      if (!setting.isEmpty()) {
         String[] parts = setting.split("[\\._]");
         switch (parts.length) {
            case 1:
               locale = new Locale(parts[0]);
               break;
            case 2:
               locale = new Locale(parts[0], parts[1]);
               break;
            case 3:
               locale = new Locale(parts[0], parts[1], parts[2]);
         }
      }

      Translator.setInstance(new File(this.getDataFolder(), "lang"), locale);
   }

   private void startMetrics() {
      try {
         Metrics metrics = new Metrics(this);
         if (metrics.isOptOut()) {
            return;
         }

         metrics.addCustomData(new Metrics.Plotter("Total NPCs") {
            public int getValue() {
               return Citizens.this.npcRegistry == null ? 0 : Iterables.size(Citizens.this.npcRegistry);
            }
         });
         metrics.addCustomData(new Metrics.Plotter("Total goals") {
            public int getValue() {
               if (Citizens.this.npcRegistry == null) {
                  return 0;
               } else {
                  int goalCount = 0;

                  for(NPC npc : Citizens.this.npcRegistry) {
                     goalCount += Iterables.size(npc.getDefaultGoalController());
                  }

                  return goalCount;
               }
            }
         });
         this.traitFactory.addPlotters(metrics.createGraph("traits"));
         metrics.start();
      } catch (IOException e) {
         Messaging.logTr("citizens.notifications.metrics-load-error", e.getMessage());
      }

   }

   public void storeNPCs() {
      if (this.saves != null) {
         for(NPC npc : this.npcRegistry) {
            this.saves.store(npc);
         }

      }
   }

   public void storeNPCs(CommandContext args) {
      this.storeNPCs();
      boolean async = args.hasFlag('a');
      if (async) {
         this.saves.saveToDisk();
      } else {
         this.saves.saveToDiskImmediate();
      }

   }

   private boolean suggestClosestModifier(CommandSender sender, String command, String modifier) {
      String closest = this.commands.getClosestCommandModifier(command, modifier);
      if (!closest.isEmpty()) {
         sender.sendMessage(ChatColor.GRAY + Messaging.tr("citizens.commands.unknown-command"));
         sender.sendMessage(StringHelper.wrap(" /") + command + " " + StringHelper.wrap(closest));
         return true;
      } else {
         return false;
      }
   }
}
