package com.onarandombox.MultiverseCore.api;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.utils.DebugLog;
import com.pneumaticraft.commandhandler.multiverse.CommandHandler;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class MultiversePlugin extends JavaPlugin implements MVPlugin {
   private MultiverseCore core;
   protected String logTag;
   private DebugLog debugLog;

   public MultiversePlugin() {
      super();
   }

   public final void onEnable() {
      MultiverseCore theCore = (MultiverseCore)this.getServer().getPluginManager().getPlugin("Multiverse-Core");
      if (theCore == null) {
         this.getLogger().severe("Core not found! The plugin dev needs to add a dependency!");
         this.getLogger().severe("Disabling!");
         this.getServer().getPluginManager().disablePlugin(this);
      } else if (theCore.getProtocolVersion() < this.getProtocolVersion()) {
         this.getLogger().severe("You need a newer version of Multiverse-Core!");
         this.getLogger().severe("Disabling!");
         this.getServer().getPluginManager().disablePlugin(this);
      } else {
         this.setCore(theCore);
         this.getServer().getLogger().info(String.format("%s - Version %s enabled - By %s", this.getDescription().getName(), this.getDescription().getVersion(), this.getAuthors()));
         this.getDataFolder().mkdirs();
         File debugLogFile = new File(this.getDataFolder(), "debug.log");

         try {
            debugLogFile.createNewFile();
         } catch (IOException e) {
            e.printStackTrace();
         }

         this.debugLog = new DebugLog(this.getDescription().getName(), this.getDataFolder() + File.separator + "debug.log");
         this.debugLog.setTag(String.format("[%s-Debug]", this.getDescription().getName()));
         this.onPluginEnable();
      }
   }

   protected String getAuthors() {
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

   protected abstract void onPluginEnable();

   protected abstract void registerCommands(CommandHandler var1);

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!this.isEnabled()) {
         sender.sendMessage("This plugin is Disabled!");
         return true;
      } else {
         ArrayList<String> allArgs = new ArrayList(args.length + 1);
         allArgs.add(command.getName());
         allArgs.addAll(Arrays.asList(args));
         return this.getCore().getCommandHandler().locateAndRunCommand(sender, allArgs);
      }
   }

   public void log(Level level, String msg) {
      int debugLevel = this.getCore().getMVConfig().getGlobalDebug();
      if ((level != Level.FINE || debugLevel < 1) && (level != Level.FINER || debugLevel < 2) && (level != Level.FINEST || debugLevel < 3)) {
         if (level != Level.FINE && level != Level.FINER && level != Level.FINEST) {
            String message = this.getLogTag() + msg;
            this.getServer().getLogger().log(level, message);
            this.debugLog.log(level, message);
         }
      } else {
         this.debugLog.log(level, msg);
      }

   }

   private String getLogTag() {
      if (this.logTag == null) {
         this.logTag = String.format("[%s]", this.getDescription().getName());
      }

      return this.logTag;
   }

   protected final void setDebugLogTag(String tag) {
      this.debugLog.setTag(tag);
   }

   public final String dumpVersionInfo(String buffer) {
      throw new UnsupportedOperationException("This is gone.");
   }

   public final MultiverseCore getCore() {
      if (this.core == null) {
         throw new IllegalStateException("Core is null!");
      } else {
         return this.core;
      }
   }

   public final void setCore(MultiverseCore core) {
      this.core = core;
   }
}
