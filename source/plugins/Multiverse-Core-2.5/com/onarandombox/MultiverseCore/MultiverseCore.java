package com.onarandombox.MultiverseCore;

import buscript.multiverse.Buscript;
import com.fernferret.allpay.multiverse.AllPay;
import com.fernferret.allpay.multiverse.GenericBank;
import com.onarandombox.MultiverseCore.api.BlockSafety;
import com.onarandombox.MultiverseCore.api.Core;
import com.onarandombox.MultiverseCore.api.LocationManipulation;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseCoreConfig;
import com.onarandombox.MultiverseCore.api.MultiverseMessaging;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.MultiverseCore.commands.AnchorCommand;
import com.onarandombox.MultiverseCore.commands.CheckCommand;
import com.onarandombox.MultiverseCore.commands.CloneCommand;
import com.onarandombox.MultiverseCore.commands.ConfigCommand;
import com.onarandombox.MultiverseCore.commands.ConfirmCommand;
import com.onarandombox.MultiverseCore.commands.CoordCommand;
import com.onarandombox.MultiverseCore.commands.CreateCommand;
import com.onarandombox.MultiverseCore.commands.DebugCommand;
import com.onarandombox.MultiverseCore.commands.DeleteCommand;
import com.onarandombox.MultiverseCore.commands.EnvironmentCommand;
import com.onarandombox.MultiverseCore.commands.GeneratorCommand;
import com.onarandombox.MultiverseCore.commands.HelpCommand;
import com.onarandombox.MultiverseCore.commands.ImportCommand;
import com.onarandombox.MultiverseCore.commands.InfoCommand;
import com.onarandombox.MultiverseCore.commands.ListCommand;
import com.onarandombox.MultiverseCore.commands.LoadCommand;
import com.onarandombox.MultiverseCore.commands.ModifyAddCommand;
import com.onarandombox.MultiverseCore.commands.ModifyClearCommand;
import com.onarandombox.MultiverseCore.commands.ModifyCommand;
import com.onarandombox.MultiverseCore.commands.ModifyRemoveCommand;
import com.onarandombox.MultiverseCore.commands.ModifySetCommand;
import com.onarandombox.MultiverseCore.commands.PurgeCommand;
import com.onarandombox.MultiverseCore.commands.RegenCommand;
import com.onarandombox.MultiverseCore.commands.ReloadCommand;
import com.onarandombox.MultiverseCore.commands.RemoveCommand;
import com.onarandombox.MultiverseCore.commands.ScriptCommand;
import com.onarandombox.MultiverseCore.commands.SetSpawnCommand;
import com.onarandombox.MultiverseCore.commands.SilentCommand;
import com.onarandombox.MultiverseCore.commands.SpawnCommand;
import com.onarandombox.MultiverseCore.commands.TeleportCommand;
import com.onarandombox.MultiverseCore.commands.UnloadCommand;
import com.onarandombox.MultiverseCore.commands.VersionCommand;
import com.onarandombox.MultiverseCore.commands.WhoCommand;
import com.onarandombox.MultiverseCore.destination.AnchorDestination;
import com.onarandombox.MultiverseCore.destination.BedDestination;
import com.onarandombox.MultiverseCore.destination.CannonDestination;
import com.onarandombox.MultiverseCore.destination.DestinationFactory;
import com.onarandombox.MultiverseCore.destination.ExactDestination;
import com.onarandombox.MultiverseCore.destination.PlayerDestination;
import com.onarandombox.MultiverseCore.destination.WorldDestination;
import com.onarandombox.MultiverseCore.listeners.MVAsyncPlayerChatListener;
import com.onarandombox.MultiverseCore.listeners.MVChatListener;
import com.onarandombox.MultiverseCore.listeners.MVEntityListener;
import com.onarandombox.MultiverseCore.listeners.MVPlayerChatListener;
import com.onarandombox.MultiverseCore.listeners.MVPlayerListener;
import com.onarandombox.MultiverseCore.listeners.MVPluginListener;
import com.onarandombox.MultiverseCore.listeners.MVPortalListener;
import com.onarandombox.MultiverseCore.listeners.MVWeatherListener;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import com.onarandombox.MultiverseCore.utils.CoreLogging;
import com.onarandombox.MultiverseCore.utils.MVMessaging;
import com.onarandombox.MultiverseCore.utils.MVPermissions;
import com.onarandombox.MultiverseCore.utils.MVPlayerSession;
import com.onarandombox.MultiverseCore.utils.SimpleBlockSafety;
import com.onarandombox.MultiverseCore.utils.SimpleLocationManipulation;
import com.onarandombox.MultiverseCore.utils.SimpleSafeTTeleporter;
import com.onarandombox.MultiverseCore.utils.VaultHandler;
import com.onarandombox.MultiverseCore.utils.WorldManager;
import com.pneumaticraft.commandhandler.multiverse.CommandHandler;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import me.main__.util.multiverse.SerializationConfig.NoSuchPropertyException;
import me.main__.util.multiverse.SerializationConfig.SerializationConfig;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.multiverse.Metrics;

public class MultiverseCore extends JavaPlugin implements MVPlugin, Core {
   private static final int PROTOCOL = 18;
   private static Map teleportQueue = new HashMap();
   private AnchorManager anchorManager = new AnchorManager(this);
   private volatile MultiverseCoreConfiguration config;
   private CommandHandler commandHandler;
   private static final String LOG_TAG = "[Multiverse-Core]";
   private MVPermissions ph;
   private FileConfiguration multiverseConfig = null;
   private final MVWorldManager worldManager = new WorldManager(this);
   private final MVPlayerListener playerListener = new MVPlayerListener(this);
   private final MVEntityListener entityListener = new MVEntityListener(this);
   private final MVPluginListener pluginListener = new MVPluginListener(this);
   private final MVWeatherListener weatherListener = new MVWeatherListener(this);
   private final MVPortalListener portalListener = new MVPortalListener(this);
   private MVChatListener chatListener;
   private HashMap playerSessions;
   private VaultHandler vaultHandler;
   private GenericBank bank = null;
   private AllPay banker;
   private Buscript buscript;
   private int pluginCount;
   private DestinationFactory destFactory;
   private MultiverseMessaging messaging;
   private BlockSafety blockSafety;
   private LocationManipulation locationManipulation;
   private SafeTTeleporter safeTTeleporter;
   private File serverFolder = new File(System.getProperty("user.dir"));

   public MultiverseCore() {
      super();
   }

   public static String getPlayerTeleporter(String playerName) {
      if (teleportQueue.containsKey(playerName)) {
         String teleportee = (String)teleportQueue.get(playerName);
         teleportQueue.remove(playerName);
         return teleportee;
      } else {
         return null;
      }
   }

   public static void addPlayerToTeleportQueue(String teleporter, String teleportee) {
      CoreLogging.finest("Adding mapping '%s' => '%s' to teleport queue", teleporter, teleportee);
      teleportQueue.put(teleportee, teleporter);
   }

   public String toString() {
      return "The Multiverse-Core Plugin";
   }

   /** @deprecated */
   @Deprecated
   public String dumpVersionInfo(String buffer) {
      return buffer;
   }

   public MultiverseCore getCore() {
      return this;
   }

   public void setCore(MultiverseCore core) {
   }

   public int getProtocolVersion() {
      return 18;
   }

   public void onLoad() {
      SerializationConfig.registerAll(MultiverseCoreConfiguration.class);
      SerializationConfig.registerAll(WorldProperties.class);
      this.getDataFolder().mkdirs();
      CoreLogging.init(this);
      SerializationConfig.initLogging(CoreLogging.getLogger());
      this.blockSafety = new SimpleBlockSafety(this);
      this.locationManipulation = new SimpleLocationManipulation();
      this.safeTTeleporter = new SimpleSafeTTeleporter(this);
   }

   /** @deprecated */
   @Deprecated
   public FileConfiguration getMVConfiguration() {
      return this.multiverseConfig;
   }

   /** @deprecated */
   @Deprecated
   public GenericBank getBank() {
      return this.bank;
   }

   public VaultHandler getVaultHandler() {
      return this.vaultHandler;
   }

   public void onEnable() {
      this.messaging = new MVMessaging();
      this.banker = new AllPay(this, "[Multiverse-Core] ");
      this.vaultHandler = new VaultHandler(this);
      this.worldManager.getDefaultWorldGenerators();
      this.registerEvents();
      this.ph = new MVPermissions(this);
      this.bank = this.banker.loadEconPlugin();
      this.commandHandler = new CommandHandler(this, this.ph);
      this.registerCommands();
      this.initializeDestinationFactory();
      this.playerSessions = new HashMap();
      this.loadConfigs();
      if (this.multiverseConfig != null) {
         CoreLogging.setDebugLevel(this.getMVConfig().getGlobalDebug());
         CoreLogging.setShowingConfig(!this.getMVConfig().getSilentStart());
         this.worldManager.loadDefaultWorlds();
         this.worldManager.loadWorlds(true);
      } else {
         this.log(Level.SEVERE, "Your configs were not loaded. Very little will function in Multiverse.");
      }

      this.anchorManager.loadAnchors();
      this.worldManager.setFirstSpawnWorld(this.getMVConfig().getFirstSpawnWorld());

      try {
         this.getMVConfig().setFirstSpawnWorld(this.worldManager.getFirstSpawnWorld().getName());
      } catch (NullPointerException var3) {
      }

      this.saveMVConfig();

      try {
         Class.forName("org.bukkit.event.player.AsyncPlayerChatEvent");
      } catch (ClassNotFoundException var2) {
         this.getMVConfig().setUseAsyncChat(false);
      }

      if (this.getMVConfig().getUseAsyncChat()) {
         this.chatListener = new MVAsyncPlayerChatListener(this, this.playerListener);
      } else {
         this.chatListener = new MVPlayerChatListener(this, this.playerListener);
      }

      this.getServer().getPluginManager().registerEvents(this.chatListener, this);
      this.initializeBuscript();
      this.setupMetrics();
      CoreLogging.config("Version %s (API v%s) Enabled - By %s", this.getDescription().getVersion(), 18, this.getAuthors());
   }

   private void initializeBuscript() {
      this.buscript = new Buscript(this);
      this.buscript.getGlobalScope().put("multiverse", this.buscript.getGlobalScope(), this);
   }

   private void setupMetrics() {
      try {
         Metrics m = new Metrics(this);
         Metrics.Graph envGraph = m.createGraph("worlds_by_env");

         for(World.Environment env : Environment.values()) {
            envGraph.addPlotter(new EnvironmentPlotter(this, env));
         }

         m.addCustomData(new Metrics.Plotter("Loaded worlds") {
            public int getValue() {
               return MultiverseCore.this.getMVWorldManager().getMVWorlds().size();
            }
         });
         m.addCustomData(new Metrics.Plotter("Total number of worlds") {
            public int getValue() {
               return MultiverseCore.this.getMVWorldManager().getMVWorlds().size() + MultiverseCore.this.getMVWorldManager().getUnloadedWorlds().size();
            }
         });
         Set<String> gens = new HashSet();

         for(MultiverseWorld w : this.getMVWorldManager().getMVWorlds()) {
            gens.add(w.getGenerator());
         }

         gens.remove((Object)null);
         gens.remove("null");
         Metrics.Graph genGraph = m.createGraph("custom_gens");

         for(String gen : gens) {
            genGraph.addPlotter(new GeneratorPlotter(this, gen));
         }

         m.start();
         this.log(Level.FINE, "Metrics have run!");
      } catch (IOException e) {
         this.log(Level.WARNING, "There was an issue while enabling metrics: " + e.getMessage());
      }

   }

   private void initializeDestinationFactory() {
      this.destFactory = new DestinationFactory(this);
      this.destFactory.registerDestinationType(WorldDestination.class, "");
      this.destFactory.registerDestinationType(WorldDestination.class, "w");
      this.destFactory.registerDestinationType(ExactDestination.class, "e");
      this.destFactory.registerDestinationType(PlayerDestination.class, "pl");
      this.destFactory.registerDestinationType(CannonDestination.class, "ca");
      this.destFactory.registerDestinationType(BedDestination.class, "b");
      this.destFactory.registerDestinationType(AnchorDestination.class, "a");
   }

   private void registerEvents() {
      PluginManager pm = this.getServer().getPluginManager();
      pm.registerEvents(this.playerListener, this);
      pm.registerEvents(this.entityListener, this);
      pm.registerEvents(this.pluginListener, this);
      pm.registerEvents(this.weatherListener, this);
      pm.registerEvents(this.portalListener, this);
   }

   public void loadConfigs() {
      this.multiverseConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "config.yml"));
      Configuration coreDefaults = YamlConfiguration.loadConfiguration(this.getClass().getResourceAsStream("/defaults/config.yml"));
      this.multiverseConfig.setDefaults(coreDefaults);
      this.multiverseConfig.options().copyDefaults(false);
      this.multiverseConfig.options().copyHeader(true);
      MultiverseCoreConfiguration wantedConfig = null;

      try {
         wantedConfig = (MultiverseCoreConfiguration)this.multiverseConfig.get("multiverse-configuration");
      } catch (Exception var7) {
      } finally {
         this.config = wantedConfig == null ? new MultiverseCoreConfiguration() : wantedConfig;
      }

      this.migrateWorldConfig();
      this.worldManager.loadWorldConfig(new File(this.getDataFolder(), "worlds.yml"));
      this.messaging.setCooldown(this.getMVConfig().getMessageCooldown());
      this.multiverseConfig.set("enforcegamemodes", (Object)null);
      this.multiverseConfig.set("bedrespawn", (Object)null);
      this.multiverseConfig.set("opfallback", (Object)null);
      this.migrate22Values();
      this.saveMVConfigs();
   }

   private void migrate22Values() {
      if (this.multiverseConfig.isSet("worldnameprefix")) {
         CoreLogging.config("Migrating 'worldnameprefix'...");
         this.getMVConfig().setPrefixChat(this.multiverseConfig.getBoolean("worldnameprefix"));
         this.multiverseConfig.set("worldnameprefix", (Object)null);
      }

      if (this.multiverseConfig.isSet("firstspawnworld")) {
         CoreLogging.config("Migrating 'firstspawnworld'...");
         this.getMVConfig().setFirstSpawnWorld(this.multiverseConfig.getString("firstspawnworld"));
         this.multiverseConfig.set("firstspawnworld", (Object)null);
      }

      if (this.multiverseConfig.isSet("enforceaccess")) {
         CoreLogging.config("Migrating 'enforceaccess'...");
         this.getMVConfig().setEnforceAccess(this.multiverseConfig.getBoolean("enforceaccess"));
         this.multiverseConfig.set("enforceaccess", (Object)null);
      }

      if (this.multiverseConfig.isSet("displaypermerrors")) {
         CoreLogging.config("Migrating 'displaypermerrors'...");
         this.getMVConfig().setDisplayPermErrors(this.multiverseConfig.getBoolean("displaypermerrors"));
         this.multiverseConfig.set("displaypermerrors", (Object)null);
      }

      if (this.multiverseConfig.isSet("teleportintercept")) {
         CoreLogging.config("Migrating 'teleportintercept'...");
         this.getMVConfig().setTeleportIntercept(this.multiverseConfig.getBoolean("teleportintercept"));
         this.multiverseConfig.set("teleportintercept", (Object)null);
      }

      if (this.multiverseConfig.isSet("firstspawnoverride")) {
         CoreLogging.config("Migrating 'firstspawnoverride'...");
         this.getMVConfig().setFirstSpawnOverride(this.multiverseConfig.getBoolean("firstspawnoverride"));
         this.multiverseConfig.set("firstspawnoverride", (Object)null);
      }

      if (this.multiverseConfig.isSet("messagecooldown")) {
         CoreLogging.config("Migrating 'messagecooldown'...");
         this.getMVConfig().setMessageCooldown(this.multiverseConfig.getInt("messagecooldown"));
         this.multiverseConfig.set("messagecooldown", (Object)null);
      }

      if (this.multiverseConfig.isSet("debug")) {
         CoreLogging.config("Migrating 'debug'...");
         this.getMVConfig().setGlobalDebug(this.multiverseConfig.getInt("debug"));
         this.multiverseConfig.set("debug", (Object)null);
      }

      if (this.multiverseConfig.isSet("version")) {
         CoreLogging.config("Migrating 'version'...");
         this.multiverseConfig.set("version", (Object)null);
      }

   }

   private void migrateWorldConfig() {
      FileConfiguration wconf = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "worlds.yml"));
      if (!wconf.isConfigurationSection("worlds")) {
         this.log(Level.FINE, "No worlds to migrate!");
      } else {
         Map<String, Object> values = wconf.getConfigurationSection("worlds").getValues(false);
         boolean wasChanged = false;
         Map<String, Object> newValues = new LinkedHashMap(values.size());

         for(Map.Entry entry : values.entrySet()) {
            if (entry.getValue() instanceof WorldProperties) {
               newValues.put(entry.getKey(), entry.getValue());
            } else if (entry.getValue() instanceof ConfigurationSection) {
               this.log(Level.FINE, "Migrating: " + (String)entry.getKey());
               WorldProperties world = new WorldProperties(Collections.EMPTY_MAP);
               ConfigurationSection section = (ConfigurationSection)entry.getValue();
               if (section.isConfigurationSection("animals")) {
                  ConfigurationSection animalSection = section.getConfigurationSection("animals");
                  if (animalSection.contains("spawn")) {
                     if (animalSection.isBoolean("spawn")) {
                        world.setAllowAnimalSpawn(animalSection.getBoolean("spawn"));
                     } else {
                        world.setAllowAnimalSpawn(Boolean.parseBoolean(animalSection.getString("spawn")));
                     }
                  }

                  if (animalSection.isList("exceptions")) {
                     world.getAnimalList().clear();
                     world.getAnimalList().addAll(animalSection.getStringList("exceptions"));
                  }
               }

               if (section.isConfigurationSection("monsters")) {
                  ConfigurationSection monsterSection = section.getConfigurationSection("monsters");
                  if (monsterSection.contains("spawn")) {
                     if (monsterSection.isBoolean("spawn")) {
                        world.setAllowMonsterSpawn(monsterSection.getBoolean("spawn"));
                     } else {
                        world.setAllowMonsterSpawn(Boolean.parseBoolean(monsterSection.getString("spawn")));
                     }
                  }

                  if (monsterSection.isList("exceptions")) {
                     world.getMonsterList().clear();
                     world.getMonsterList().addAll(monsterSection.getStringList("exceptions"));
                  }
               }

               if (section.isConfigurationSection("entryfee")) {
                  ConfigurationSection feeSection = section.getConfigurationSection("entryfee");
                  if (feeSection.isInt("currency")) {
                     world.setCurrency(feeSection.getInt("currency"));
                  }

                  if (feeSection.isDouble("amount")) {
                     world.setPrice(feeSection.getDouble("amount"));
                  } else if (feeSection.isInt("amount")) {
                     world.setPrice((double)feeSection.getInt("amount"));
                  }
               }

               if (section.isBoolean("pvp")) {
                  world.setPVPMode(section.getBoolean("pvp"));
               }

               if (section.isConfigurationSection("alias")) {
                  ConfigurationSection aliasSection = section.getConfigurationSection("alias");
                  if (aliasSection.isString("color")) {
                     world.setColor(aliasSection.getString("color"));
                  }

                  if (aliasSection.isString("name")) {
                     world.setAlias(aliasSection.getString("name"));
                  }
               }

               if (section.isList("worldblacklist")) {
                  world.getWorldBlacklist().clear();
                  world.getWorldBlacklist().addAll(section.getStringList("worldblacklist"));
               }

               if (section.isDouble("scale")) {
                  world.setScaling(section.getDouble("scale"));
               }

               if (section.isString("gamemode")) {
                  GameMode gameMode = GameMode.valueOf(section.getString("gamemode").toUpperCase());
                  if (gameMode != null) {
                     world.setGameMode(gameMode);
                  }
               }

               if (section.isBoolean("hunger")) {
                  world.setHunger(section.getBoolean("hunger"));
               }

               if (section.isBoolean("hidden")) {
                  world.setHidden(section.getBoolean("hidden"));
               }

               if (section.isBoolean("autoheal")) {
                  world.setAutoHeal(section.getBoolean("autoheal"));
               }

               if (section.isString("portalform")) {
                  try {
                     world.setProperty("portalform", section.getString("portalform"), true);
                  } catch (NoSuchPropertyException e) {
                     throw new RuntimeException("Who forgot to update the migrator?", e);
                  }
               }

               if (section.isString("environment")) {
                  try {
                     world.setProperty("environment", section.getString("environment"), true);
                  } catch (NoSuchPropertyException e) {
                     throw new RuntimeException("Who forgot to update the migrator?", e);
                  }
               }

               if (section.isString("generator")) {
                  world.setGenerator(section.getString("generator"));
               }

               if (section.isLong("seed")) {
                  world.setSeed(section.getLong("seed"));
               }

               if (section.isBoolean("allowweather")) {
                  world.setEnableWeather(section.getBoolean("allowweather"));
               }

               if (section.isBoolean("adjustspawn")) {
                  world.setAdjustSpawn(section.getBoolean("adjustspawn"));
               }

               if (section.isBoolean("autoload")) {
                  world.setAutoLoad(section.getBoolean("autoload"));
               }

               if (section.isBoolean("bedrespawn")) {
                  world.setBedRespawn(section.getBoolean("bedrespawn"));
               }

               if (section.isConfigurationSection("spawn")) {
                  ConfigurationSection spawnSect = section.getConfigurationSection("spawn");
                  Location spawnLoc = new MVWorld.NullLocation();
                  if (spawnSect.isDouble("yaw")) {
                     spawnLoc.setYaw((float)spawnSect.getDouble("yaw"));
                  }

                  if (spawnSect.isDouble("pitch")) {
                     spawnLoc.setPitch((float)spawnSect.getDouble("pitch"));
                  }

                  if (spawnSect.isDouble("x")) {
                     spawnLoc.setX(spawnSect.getDouble("x"));
                  }

                  if (spawnSect.isDouble("y")) {
                     spawnLoc.setY(spawnSect.getDouble("y"));
                  }

                  if (spawnSect.isDouble("z")) {
                     spawnLoc.setZ(spawnSect.getDouble("z"));
                  }

                  world.setSpawnLocation(spawnLoc);
               }

               if (section.isString("difficulty")) {
                  Difficulty difficulty = Difficulty.valueOf(section.getString("difficulty").toUpperCase());
                  if (difficulty != null) {
                     world.setDifficulty(difficulty);
                  }
               }

               if (section.isBoolean("keepspawninmemory")) {
                  world.setKeepSpawnInMemory(section.getBoolean("keepspawninmemory"));
               }

               newValues.put(entry.getKey(), world);
               wasChanged = true;
            } else {
               this.log(Level.WARNING, "Removing unknown entry in the config: " + entry);
               wasChanged = true;
            }
         }

         if (wasChanged) {
            wconf.set("worlds", (Object)null);
            ConfigurationSection rootSection = wconf.createSection("worlds");

            for(Map.Entry entry : newValues.entrySet()) {
               rootSection.set((String)entry.getKey(), entry.getValue());
            }

            try {
               wconf.save(new File(this.getDataFolder(), "worlds.yml"));
            } catch (IOException e) {
               e.printStackTrace();
            }
         }

      }
   }

   public MultiverseMessaging getMessaging() {
      return this.messaging;
   }

   private void registerCommands() {
      this.commandHandler.registerCommand(new HelpCommand(this));
      this.commandHandler.registerCommand(new VersionCommand(this));
      this.commandHandler.registerCommand(new ListCommand(this));
      this.commandHandler.registerCommand(new InfoCommand(this));
      this.commandHandler.registerCommand(new CreateCommand(this));
      this.commandHandler.registerCommand(new CloneCommand(this));
      this.commandHandler.registerCommand(new ImportCommand(this));
      this.commandHandler.registerCommand(new ReloadCommand(this));
      this.commandHandler.registerCommand(new SetSpawnCommand(this));
      this.commandHandler.registerCommand(new CoordCommand(this));
      this.commandHandler.registerCommand(new TeleportCommand(this));
      this.commandHandler.registerCommand(new WhoCommand(this));
      this.commandHandler.registerCommand(new SpawnCommand(this));
      this.commandHandler.registerCommand(new UnloadCommand(this));
      this.commandHandler.registerCommand(new LoadCommand(this));
      this.commandHandler.registerCommand(new RemoveCommand(this));
      this.commandHandler.registerCommand(new DeleteCommand(this));
      this.commandHandler.registerCommand(new RegenCommand(this));
      this.commandHandler.registerCommand(new ConfirmCommand(this));
      this.commandHandler.registerCommand(new ModifyCommand(this));
      this.commandHandler.registerCommand(new PurgeCommand(this));
      this.commandHandler.registerCommand(new ModifyAddCommand(this));
      this.commandHandler.registerCommand(new ModifySetCommand(this));
      this.commandHandler.registerCommand(new ModifyRemoveCommand(this));
      this.commandHandler.registerCommand(new ModifyClearCommand(this));
      this.commandHandler.registerCommand(new ConfigCommand(this));
      this.commandHandler.registerCommand(new AnchorCommand(this));
      this.commandHandler.registerCommand(new EnvironmentCommand(this));
      this.commandHandler.registerCommand(new DebugCommand(this));
      this.commandHandler.registerCommand(new SilentCommand(this));
      this.commandHandler.registerCommand(new GeneratorCommand(this));
      this.commandHandler.registerCommand(new CheckCommand(this));
      this.commandHandler.registerCommand(new ScriptCommand(this));
   }

   public void onDisable() {
      this.saveMVConfigs();
      this.banker = null;
      this.bank = null;
      CoreLogging.shutdown();
   }

   public MVPlayerSession getPlayerSession(Player player) {
      if (this.playerSessions.containsKey(player.getName())) {
         return (MVPlayerSession)this.playerSessions.get(player.getName());
      } else {
         this.playerSessions.put(player.getName(), new MVPlayerSession(player, this.getMVConfig()));
         return (MVPlayerSession)this.playerSessions.get(player.getName());
      }
   }

   /** @deprecated */
   @Deprecated
   public com.onarandombox.MultiverseCore.utils.SafeTTeleporter getTeleporter() {
      return new com.onarandombox.MultiverseCore.utils.SafeTTeleporter(this);
   }

   public MVPermissions getMVPerms() {
      return this.ph;
   }

   public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
      if (!this.isEnabled()) {
         sender.sendMessage("This plugin is Disabled!");
         return true;
      } else {
         ArrayList<String> allArgs = new ArrayList(Arrays.asList(args));
         allArgs.add(0, command.getName());

         try {
            return this.commandHandler.locateAndRunCommand(sender, allArgs, this.getMVConfig().getDisplayPermErrors());
         } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "An internal error occurred when attempting to perform this command.");
            if (sender.isOp()) {
               sender.sendMessage(ChatColor.RED + "Details were printed to the server console and logs, please add that to your bug report.");
            } else {
               sender.sendMessage(ChatColor.RED + "Try again and contact the server owner or an admin if this problem persists.");
            }

            return true;
         }
      }
   }

   public void log(Level level, String msg) {
      CoreLogging.log(level, msg);
   }

   /** @deprecated */
   @Deprecated
   public static void staticLog(Level level, String msg) {
      CoreLogging.log(level, msg);
   }

   /** @deprecated */
   @Deprecated
   public static void staticDebugLog(Level level, String msg) {
      CoreLogging.log(level, msg);
   }

   public String getAuthors() {
      String authors = "";
      List<String> auths = this.getDescription().getAuthors();
      if (auths.size() == 0) {
         return "";
      } else if (auths.size() == 1) {
         return (String)auths.get(0);
      } else {
         for(int i = 0; i < auths.size(); ++i) {
            if (i == this.getDescription().getAuthors().size() - 1) {
               authors = authors + " and " + (String)this.getDescription().getAuthors().get(i);
            } else {
               authors = authors + ", " + (String)this.getDescription().getAuthors().get(i);
            }
         }

         return authors.substring(2);
      }
   }

   public CommandHandler getCommandHandler() {
      return this.commandHandler;
   }

   public String getTag() {
      return "[Multiverse-Core]";
   }

   public void showNotMVWorldMessage(CommandSender sender, String worldName) {
      sender.sendMessage("Multiverse doesn't know about " + ChatColor.DARK_AQUA + worldName + ChatColor.WHITE + " yet.");
      sender.sendMessage("Type " + ChatColor.DARK_AQUA + "/mv import ?" + ChatColor.WHITE + " for help!");
   }

   public void removePlayerSession(Player player) {
      if (this.playerSessions.containsKey(player.getName())) {
         this.playerSessions.remove(player.getName());
      }

   }

   public int getPluginCount() {
      return this.pluginCount;
   }

   public void incrementPluginCount() {
      ++this.pluginCount;
   }

   public void decrementPluginCount() {
      --this.pluginCount;
   }

   /** @deprecated */
   @Deprecated
   public AllPay getBanker() {
      return this.banker;
   }

   /** @deprecated */
   @Deprecated
   public void setBank(GenericBank bank) {
      this.bank = bank;
   }

   public DestinationFactory getDestFactory() {
      return this.destFactory;
   }

   public void teleportPlayer(CommandSender teleporter, Player p, Location l) {
      this.getSafeTTeleporter().safelyTeleport(teleporter, p, l, false);
   }

   public File getServerFolder() {
      return this.serverFolder;
   }

   public void setServerFolder(File newServerFolder) {
      if (!newServerFolder.isDirectory()) {
         throw new IllegalArgumentException("That's not a folder!");
      } else {
         this.serverFolder = newServerFolder;
      }
   }

   public MVWorldManager getMVWorldManager() {
      return this.worldManager;
   }

   public MVPlayerListener getPlayerListener() {
      return this.playerListener;
   }

   public MVChatListener getChatListener() {
      return this.chatListener;
   }

   public MVEntityListener getEntityListener() {
      return this.entityListener;
   }

   public MVWeatherListener getWeatherListener() {
      return this.weatherListener;
   }

   public boolean saveMVConfig() {
      try {
         this.multiverseConfig.set("multiverse-configuration", this.getMVConfig());
         this.multiverseConfig.save(new File(this.getDataFolder(), "config.yml"));
         return true;
      } catch (IOException var2) {
         this.log(Level.SEVERE, "Could not save Multiverse config.yml config. Please check your file permissions.");
         return false;
      }
   }

   public boolean saveWorldConfig() {
      return this.worldManager.saveWorldsConfig();
   }

   public boolean saveMVConfigs() {
      return this.saveMVConfig() && this.saveWorldConfig();
   }

   public Boolean deleteWorld(String name) {
      return this.worldManager.deleteWorld(name);
   }

   public Boolean cloneWorld(String oldName, String newName, String generator) {
      return this.worldManager.cloneWorld(oldName, newName, generator);
   }

   /** @deprecated */
   @Deprecated
   public Boolean regenWorld(String name, Boolean useNewSeed, Boolean randomSeed, String seed) {
      return this.worldManager.regenWorld(name, useNewSeed, randomSeed, seed);
   }

   public AnchorManager getAnchorManager() {
      return this.anchorManager;
   }

   public BlockSafety getBlockSafety() {
      return this.blockSafety;
   }

   public void setBlockSafety(BlockSafety bs) {
      this.blockSafety = bs;
   }

   public LocationManipulation getLocationManipulation() {
      return this.locationManipulation;
   }

   public void setLocationManipulation(LocationManipulation locationManipulation) {
      this.locationManipulation = locationManipulation;
   }

   public SafeTTeleporter getSafeTTeleporter() {
      return this.safeTTeleporter;
   }

   public void setSafeTTeleporter(SafeTTeleporter safeTTeleporter) {
      this.safeTTeleporter = safeTTeleporter;
   }

   public MultiverseCoreConfig getMVConfig() {
      return this.config;
   }

   /** @deprecated */
   @Deprecated
   public static MultiverseCoreConfiguration getStaticConfig() {
      return MultiverseCoreConfiguration.getInstance();
   }

   public Buscript getScriptAPI() {
      return this.buscript;
   }

   private static final class EnvironmentPlotter extends Metrics.Plotter {
      private MultiverseCore core;
      private final World.Environment env;

      public EnvironmentPlotter(MultiverseCore core, World.Environment env) {
         super(envToString(env));
         this.core = core;
         this.env = env;
      }

      private static String envToString(World.Environment env) {
         return env.name().toUpperCase().charAt(0) + env.name().toLowerCase().substring(1);
      }

      public int getValue() {
         int count = 0;

         for(MultiverseWorld w : this.core.getMVWorldManager().getMVWorlds()) {
            if (w.getEnvironment() == this.env) {
               ++count;
            }
         }

         this.core.log(Level.FINE, String.format("Tracking %d worlds of type %s", count, this.env));
         return count;
      }
   }

   private static final class GeneratorPlotter extends Metrics.Plotter {
      private MultiverseCore core;
      private final String gen;

      public GeneratorPlotter(MultiverseCore core, String gen) {
         super(gen);
         this.core = core;
         this.gen = gen;
      }

      public int getValue() {
         int count = 0;

         for(MultiverseWorld w : this.core.getMVWorldManager().getMVWorlds()) {
            if (this.gen.equals(w.getGenerator())) {
               ++count;
            }
         }

         this.core.log(Level.FINE, String.format("Tracking %d worlds of type %s", count, this.gen));
         return count;
      }
   }
}
