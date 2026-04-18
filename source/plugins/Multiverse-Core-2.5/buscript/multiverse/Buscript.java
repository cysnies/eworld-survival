package buscript.multiverse;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;

public class Buscript {
   public static final String NULL = "!!NULL";
   private String target = null;
   private Plugin plugin;
   private Scriptable global;
   private Permission permissions;
   private Economy economy;
   private Chat chat;
   private File scriptFolder;
   private File scriptFile;
   private FileConfiguration scriptConfig;
   private List delayedReplacements = null;
   private final List stringReplacers = new ArrayList();
   private Map metaData = new HashMap();
   boolean runTasks = true;
   Map delayedScripts = new HashMap();

   public Buscript(Plugin plugin) {
      super();
      this.plugin = plugin;
      this.registerStringReplacer(new TargetReplacer(this));
      this.scriptFolder = new File(plugin.getDataFolder(), "scripts");
      if (!this.getScriptFolder().exists()) {
         this.getScriptFolder().mkdirs();
      }

      Context cx = Context.enter();

      try {
         this.global = cx.initStandardObjects();
         this.global.put("server", this.global, plugin.getServer());
         this.global.put("plugin", this.global, plugin);
         this.global.put("metaData", this.global, this.metaData);
         this.global.put("NULL", this.global, "!!NULL");
      } finally {
         Context.exit();
      }

      this.addScriptMethods(new DefaultFunctions(this));
      this.setupVault();
      plugin.getServer().getPluginManager().registerEvents(new VaultListener(this), plugin);
      this.initData();
      ScriptTask scriptTask = new ScriptTask(this);
      scriptTask.start();
      plugin.getServer().getPluginManager().registerEvents(new BuscriptListener(this), plugin);
   }

   private void initData() {
      this.scriptFile = new File(this.getScriptFolder(), "scripts.bin");
      this.scriptConfig = YamlConfiguration.loadConfiguration(this.scriptFile);
      ConfigurationSection scripts = this.scriptConfig.getConfigurationSection("scripts");
      if (scripts != null) {
         for(String player : scripts.getKeys(false)) {
            List<Map<String, Object>> playerScripts = new ArrayList();
            this.delayedScripts.put(player, playerScripts);

            for(Object scriptObj : scripts.getList(player)) {
               if (scriptObj instanceof Map) {
                  Map scriptMap = (Map)scriptObj;
                  Map<String, Object> script = new HashMap(2);

                  for(Object keyObj : scriptMap.keySet()) {
                     if (keyObj.toString().equals("time")) {
                        try {
                           script.put(keyObj.toString(), Long.valueOf(scriptMap.get(keyObj).toString()));
                        } catch (NumberFormatException var12) {
                           this.getPlugin().getLogger().warning("Script data error, time reset");
                           script.put(keyObj.toString(), 0);
                        }
                     } else {
                        script.put(keyObj.toString(), scriptMap.get(keyObj));
                     }
                  }

                  playerScripts.add(script);
               }
            }
         }
      }

   }

   void setupVault() {
      if (this.getPlugin().getServer().getPluginManager().getPlugin("Vault") != null) {
         RegisteredServiceProvider<Permission> permissionProvider = this.getPlugin().getServer().getServicesManager().getRegistration(Permission.class);
         if (permissionProvider != null) {
            this.permissions = (Permission)permissionProvider.getProvider();
         }

         RegisteredServiceProvider<Economy> economyProvider = this.getPlugin().getServer().getServicesManager().getRegistration(Economy.class);
         if (economyProvider != null) {
            this.economy = (Economy)economyProvider.getProvider();
         }

         RegisteredServiceProvider<Chat> chatProvider = this.getPlugin().getServer().getServicesManager().getRegistration(Chat.class);
         if (chatProvider != null) {
            this.chat = (Chat)chatProvider.getProvider();
         }

         this.updateVaultInGlobalScope();
      }
   }

   void disableVault() {
      this.permissions = null;
      this.economy = null;
      this.chat = null;
      this.updateVaultInGlobalScope();
   }

   private void updateVaultInGlobalScope() {
      Context.enter();

      try {
         this.global.put("permissions", this.global, this.permissions);
         this.global.put("chat", this.global, this.chat);
         this.global.put("economy", this.global, this.economy);
      } finally {
         Context.exit();
      }

   }

   void saveData() {
      this.scriptConfig.set("scripts", this.delayedScripts);

      try {
         this.scriptConfig.save(this.scriptFile);
      } catch (IOException e) {
         this.plugin.getLogger().warning("Could not save script data: " + e.getMessage());
      }

   }

   public Plugin getPlugin() {
      return this.plugin;
   }

   public Scriptable getGlobalScope() {
      return this.global;
   }

   public Permission getPermissions() {
      return this.permissions;
   }

   public Economy getEconomy() {
      return this.economy;
   }

   public Chat getChat() {
      return this.chat;
   }

   public String getTarget() {
      return this.target;
   }

   public File getScriptFolder() {
      return this.scriptFolder;
   }

   public void setScriptFolder(File folder) {
      if (!folder.isDirectory()) {
         throw new IllegalArgumentException("folder must be a directory!");
      } else {
         this.scriptFolder = folder;
      }
   }

   public String stringReplace(String string) {
      if (string == null) {
         throw new IllegalArgumentException("string must not be null");
      } else {
         String result = string;
         if (this.delayedReplacements != null) {
            for(Map replacement : this.delayedReplacements) {
               Object regex = replacement.get("regex");
               Object replace = replacement.get("replace");
               if (regex != null) {
                  if (replace == null) {
                     replace = "!!NULL";
                  }

                  result = result.replaceAll(regex.toString(), replace.toString());
               }
            }
         } else {
            for(StringReplacer r : this.stringReplacers) {
               String regex = r.getRegexString();
               if (regex != null) {
                  String replace = r.getReplacement();
                  if (replace == null) {
                     replace = "!!NULL";
                  }

                  result = result.replaceAll(regex, replace);
               }
            }
         }

         result = ChatColor.translateAlternateColorCodes('&', result);
         return result;
      }
   }

   public void registerStringReplacer(StringReplacer replacer) {
      Iterator<StringReplacer> it = this.stringReplacers.iterator();

      while(it.hasNext()) {
         StringReplacer r = (StringReplacer)it.next();
         if (r.getRegexString().equals(replacer.getRegexString())) {
            it.remove();
         }
      }

      this.stringReplacers.add(replacer);
   }

   public void addScriptMethod(String name, Method method, Scriptable obj) {
      FunctionObject scriptMethod = new FunctionObject(name, method, obj);
      this.global.put(name, this.global, scriptMethod);
   }

   public void addScriptMethods(String[] names, Scriptable obj) {
      for(Method method : obj.getClass().getDeclaredMethods()) {
         for(String name : names) {
            if (method.getName().equals(name)) {
               this.addScriptMethod(name, method, obj);
            }
         }
      }

   }

   public void addScriptMethods(Scriptable obj) {
      for(Method method : obj.getClass().getDeclaredMethods()) {
         this.addScriptMethod(method.getName(), method, obj);
      }

   }

   void executeDelayedScript(File scriptFile, List replacements, Map data) {
      if (data != null) {
         this.metaData = data;
      }

      this.delayedReplacements = replacements;
      this.executeScript(scriptFile, (String)null, (Player)null);
      this.delayedReplacements = null;
   }

   public void executeScript(File scriptFile) {
      this.executeScript(scriptFile, (String)null, (Player)null);
   }

   public void executeScript(File scriptFile, Player executor) {
      this.executeScript(scriptFile, (String)null, executor);
   }

   public void executeScript(File scriptFile, String target) {
      this.executeScript(scriptFile, target, (Player)null);
   }

   public void executeScript(File scriptFile, String target, Player executor) {
      this.target = target;
      this.runScript(scriptFile, executor);
      this.target = null;
      this.metaData.clear();
   }

   public void scheduleScript(File scriptFile, long delay) {
      this.scheduleScript(scriptFile, (String)null, delay);
   }

   public void scheduleScript(File scriptFile, String target, long delay) {
      if (target == null) {
         target = "!!NULL";
      }

      List<Map<String, Object>> playerScripts = (List)this.delayedScripts.get(target);
      if (playerScripts == null) {
         playerScripts = new ArrayList();
         this.delayedScripts.put(target, playerScripts);
      }

      Map<String, Object> script = new HashMap(2);
      script.put("time", System.currentTimeMillis() + delay);
      script.put("file", scriptFile.toString());
      List<Map<String, Object>> replacements = new ArrayList(this.stringReplacers.size());

      for(StringReplacer r : this.stringReplacers) {
         Map<String, Object> replacement = new HashMap(2);
         String regex = r.getRegexString();
         if (regex != null) {
            replacement.put("regex", regex);
         }

         String replace = r.getReplacement();
         if (replace != null) {
            replacement.put("replace", replace);
         }

         String var = r.getGlobalVarName();
         if (var != null) {
            replacement.put("var", var);
         }

         replacements.add(replacement);
      }

      script.put("replacements", replacements);
      script.put("metaData", new HashMap(this.metaData));
      playerScripts.add(script);
      this.saveData();
   }

   private void runScript(File script, Player executor) {
      Context cx = Context.enter();

      try {
         if (this.delayedReplacements != null) {
            for(Map replacement : this.delayedReplacements) {
               Object var = replacement.get("var");
               Object replace = replacement.get("replace");
               if (var != null) {
                  if (replace == null) {
                     replace = "!!NULL";
                  }

                  this.global.put(var.toString(), this.global, replace);
               }
            }
         } else {
            for(StringReplacer r : this.stringReplacers) {
               String var = r.getGlobalVarName();
               String replace = r.getReplacement();
               if (var != null) {
                  if (replace == null) {
                     replace = "!!NULL";
                  }

                  this.global.put(var, this.global, replace);
               }
            }
         }

         this.global.put("metaData", this.global, this.metaData);
         Reader reader = null;

         try {
            reader = new FileReader(script);
            cx.evaluateReader(this.global, reader, script.toString(), 1, (Object)null);
         } catch (Exception e) {
            this.getPlugin().getLogger().warning("Error running script: " + e.getMessage());
            if (executor != null) {
               executor.sendMessage("Error running script: " + e.getMessage());
            }
         } finally {
            if (reader != null) {
               try {
                  reader.close();
               } catch (IOException var23) {
               }
            }

         }
      } finally {
         Context.exit();
      }

   }

   public void clearScheduledScripts(String target) {
      this.delayedScripts.remove(target);
      this.saveData();
   }

   private static class TargetReplacer implements StringReplacer {
      private Buscript buscript;

      private TargetReplacer(Buscript buscript) {
         super();
         this.buscript = buscript;
      }

      public String getRegexString() {
         return "%target%";
      }

      public String getReplacement() {
         return this.buscript.getTarget();
      }

      public String getGlobalVarName() {
         return "target";
      }
   }
}
