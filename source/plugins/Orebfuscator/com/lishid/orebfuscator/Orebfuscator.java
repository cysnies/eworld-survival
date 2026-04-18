package com.lishid.orebfuscator;

import com.lishid.orebfuscator.cache.ObfuscatedDataCache;
import com.lishid.orebfuscator.commands.OrebfuscatorCommandExecutor;
import com.lishid.orebfuscator.hithack.BlockHitManager;
import com.lishid.orebfuscator.hook.ChunkProcessingThread;
import com.lishid.orebfuscator.hook.OrebfuscatorPlayerHook;
import com.lishid.orebfuscator.hook.ProtocolLibHook;
import com.lishid.orebfuscator.internal.InternalAccessor;
import com.lishid.orebfuscator.listeners.OrebfuscatorBlockListener;
import com.lishid.orebfuscator.listeners.OrebfuscatorEntityListener;
import com.lishid.orebfuscator.listeners.OrebfuscatorPlayerListener;
import com.lishid.orebfuscator.utils.Metrics;
import com.lishid.orebfuscator.utils.ReflectionHelper;
import com.lishid.orebfuscator.utils.UpdateManager;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Orebfuscator extends JavaPlugin {
   private static Metrics metrics;
   public static final Logger logger = Logger.getLogger("Minecraft.OFC");
   public static Orebfuscator instance;
   public static boolean usePL = false;
   public static boolean useSpigot = false;
   private UpdateManager updater = new UpdateManager();

   public Orebfuscator() {
      super();
   }

   public void onEnable() {
      PluginManager pm = this.getServer().getPluginManager();
      boolean success = InternalAccessor.Initialize(this.getServer());
      if (!success) {
         log("Your version of CraftBukkit is not supported.");
         log("Please look for an updated version of Orebfuscator.");
         pm.disablePlugin(this);
      } else {
         instance = this;
         OrebfuscatorConfig.load();
         this.updater.Initialize(this, this.getFile());
         pm.registerEvents(new OrebfuscatorPlayerListener(), this);
         pm.registerEvents(new OrebfuscatorEntityListener(), this);
         pm.registerEvents(new OrebfuscatorBlockListener(), this);
         pm.registerEvents(new OrebfuscatorPlayerHook(), this);
         if (pm.getPlugin("ProtocolLib") != null) {
            log("ProtocolLib found! Hooking into ProtocolLib.");
            (new ProtocolLibHook()).register(this);
            usePL = true;
         }

         if (pm.getPlugin("NoLagg") != null && !usePL) {
            this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
               public void run() {
                  Orebfuscator.log("WARNING! NoLagg Absolutely NEED ProtocolLib to work with Orebfuscator!");
               }
            }, 0L, 60000L);
         }

         try {
            this.getServer().getClass().getDeclaredField("orebfuscatorEnabled");
            if (!usePL) {
               log("Spigot detected, you need to install ProtocolLib to function properlly!");
            }

            ReflectionHelper.setPrivateField(this.getServer(), "orebfuscatorEnabled", false);
            log("Disabling Spigot internal Orebfuscator!");
            useSpigot = true;
         } catch (Exception var5) {
         }

         try {
            metrics = new Metrics(this);
            metrics.start();
         } catch (Exception e) {
            log((Throwable)e);
         }

      }
   }

   public void onDisable() {
      ObfuscatedDataCache.clearCache();
      BlockHitManager.clearAll();
      ChunkProcessingThread.KillAll();
      this.getServer().getScheduler().cancelAllTasks();
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      return OrebfuscatorCommandExecutor.onCommand(sender, command, label, args);
   }

   public static void log(String text) {
      logger.info("[OFC] " + text);
   }

   public static void log(Throwable e) {
      logger.severe("[OFC] " + e.toString());
      e.printStackTrace();
   }

   public static void message(CommandSender target, String message) {
      target.sendMessage(ChatColor.AQUA + "[OFC] " + message);
   }
}
