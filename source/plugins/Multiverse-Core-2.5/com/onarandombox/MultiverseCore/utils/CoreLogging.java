package com.onarandombox.MultiverseCore.utils;

import java.io.File;
import java.util.IllegalFormatException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;

public class CoreLogging {
   static final String ORIGINAL_NAME = CoreLogging.class.getSimpleName();
   static final String ORIGINAL_VERSION = "v.???";
   static final String ORIGINAL_DEBUG = "-Debug";
   static final boolean SHOW_CONFIG = true;
   static final InterceptedLogger LOG = new InterceptedLogger(Logger.getLogger("Minecraft"));
   static String name;
   static String version;
   static String debug;
   static DebugFileLogger debugLog;
   static Plugin plugin;
   static volatile boolean showConfig;

   protected CoreLogging() {
      super();
      throw new AssertionError();
   }

   public static synchronized void init(Plugin plugin) {
      if (CoreLogging.plugin != null) {
         shutdown();
      }

      name = plugin.getName();
      version = plugin.getDescription().getVersion();
      DebugFileLogger.init(name, getDebugFileName(plugin));
      setDebugLevel(0);
      CoreLogging.plugin = plugin;
   }

   static synchronized String getDebugFileName(Plugin plugin) {
      return plugin.getDataFolder() + File.separator + "debug.log";
   }

   public static synchronized void shutdown() {
      closeDebugLog();
      DebugFileLogger.shutdown();
      plugin = null;
      name = ORIGINAL_NAME;
      version = "v.???";
      debug = "-Debug";
      showConfig = true;
   }

   static synchronized void closeDebugLog() {
      if (debugLog != null) {
         debugLog.close();
         debugLog = null;
      }

   }

   public static synchronized void setDebugLevel(int debugLevel) {
      if (debugLevel <= 3 && debugLevel >= 0) {
         if (debugLevel > 0) {
            debugLog = DebugFileLogger.getDebugLogger();
         } else {
            closeDebugLog();
         }

         DebugFileLogger.setDebugLevel(debugLevel);
      } else {
         throw new IllegalArgumentException("debugLevel must be between 0 and 3!");
      }
   }

   public static synchronized int getDebugLevel() {
      return DebugFileLogger.getDebugLevel();
   }

   public static void setShowingConfig(boolean showConfig) {
      CoreLogging.showConfig = showConfig;
   }

   public static boolean isShowingConfig() {
      return showConfig;
   }

   public static synchronized String getPrefixedMessage(String message, boolean showVersion) {
      StringBuilder builder = (new StringBuilder("[")).append(name);
      if (showVersion) {
         builder.append(" ").append(version);
      }

      builder.append("] ").append(message);
      return builder.toString();
   }

   public static synchronized void setDebugPrefix(String debugPrefix) {
      debug = debugPrefix;
   }

   public static synchronized String getDebugString(String message) {
      return "[" + name + debug + "] " + message;
   }

   public static Logger getLogger() {
      return LOG;
   }

   public static synchronized void log(boolean showVersion, Level level, String message, Object... args) {
      int debugLevel = getDebugLevel();
      if ((level != Level.FINE || debugLevel < 1) && (level != Level.FINER || debugLevel < 2) && (level != Level.FINEST || debugLevel < 3)) {
         if (level != Level.FINE && level != Level.FINER && level != Level.FINEST && (level != Level.CONFIG || showConfig)) {
            if (level == Level.CONFIG) {
               LOG._log(Level.INFO, getPrefixedMessage(format(message, args), showVersion));
            } else {
               LOG._log(level, getPrefixedMessage(format(message, args), showVersion));
            }
         }
      } else {
         debug(Level.INFO, message, args);
      }

   }

   private static String format(String message, Object[] args) {
      try {
         return String.format(message, args);
      } catch (IllegalFormatException var3) {
         getLogger().fine("Illegal format in the following message:");
         return message;
      }
   }

   public static void log(Level level, String message, Object... args) {
      log(false, level, message, args);
   }

   static void debug(Level level, String message, Object... args) {
      LOG._log(level, getDebugString(format(message, args)));
   }

   public static void fine(String message, Object... args) {
      log(false, Level.FINE, message, args);
   }

   public static void finer(String message, Object... args) {
      log(false, Level.FINER, message, args);
   }

   public static void finest(String message, Object... args) {
      log(false, Level.FINEST, message, args);
   }

   public static void config(String message, Object... args) {
      log(false, Level.CONFIG, message, args);
   }

   public static void info(String message, Object... args) {
      log(false, Level.INFO, message, args);
   }

   public static void warning(String message, Object... args) {
      log(false, Level.WARNING, message, args);
   }

   public static void severe(String message, Object... args) {
      log(false, Level.SEVERE, message, args);
   }

   static {
      name = ORIGINAL_NAME;
      version = "v.???";
      debug = "-Debug";
      debugLog = null;
      plugin = null;
      showConfig = true;
   }

   static class InterceptedLogger extends Logger {
      final Logger logger;

      InterceptedLogger(Logger logger) {
         super(logger.getName(), logger.getResourceBundleName());
         this.logger = logger;
      }

      synchronized void _log(Level level, String message) {
         LogRecord record = new LogRecord(level, message);
         record.setLoggerName(this.getName());
         record.setResourceBundle(this.getResourceBundle());
         this._log(record);
      }

      synchronized void _log(LogRecord record) {
         this.logger.log(record);
         if (CoreLogging.debugLog != null) {
            CoreLogging.debugLog.log(record);
         }

      }

      public synchronized void log(LogRecord record) {
         Level level = record.getLevel();
         String message = record.getMessage();
         int debugLevel = CoreLogging.getDebugLevel();
         if ((level != Level.FINE || debugLevel < 1) && (level != Level.FINER || debugLevel < 2) && (level != Level.FINEST || debugLevel < 3)) {
            if (level != Level.FINE && level != Level.FINER && level != Level.FINEST && (level != Level.CONFIG || CoreLogging.showConfig)) {
               if (level == Level.CONFIG) {
                  record.setLevel(Level.INFO);
               }

               record.setMessage(CoreLogging.getPrefixedMessage(message, false));
               CoreLogging.LOG._log(record);
            }
         } else {
            record.setLevel(Level.INFO);
            record.setMessage(CoreLogging.getDebugString(message));
            CoreLogging.LOG._log(record);
         }

      }
   }
}
