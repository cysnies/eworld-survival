package com.onarandombox.MultiverseCore.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class DebugFileLogger {
   static final int ORIGINAL_DEBUG_LEVEL = 0;
   private static String loggerName = null;
   private static String fileName = null;
   static volatile int debugLevel = 0;
   private static DebugFileLogger instance = null;
   protected final FileHandler fileHandler;
   protected final Logger log;

   public static synchronized void init(String loggerName, String fileName) {
      if (DebugFileLogger.loggerName == null) {
         DebugFileLogger.loggerName = loggerName;
         DebugFileLogger.fileName = fileName;
      }

   }

   public static synchronized void shutdown() {
      loggerName = null;
      fileName = null;
      debugLevel = 0;
   }

   public static synchronized String getLoggerName() {
      return loggerName;
   }

   public static synchronized String getFileName() {
      return fileName;
   }

   public static void setDebugLevel(int debugLevel) {
      DebugFileLogger.debugLevel = debugLevel;
   }

   public static int getDebugLevel() {
      return debugLevel;
   }

   public static synchronized DebugFileLogger getDebugLogger() {
      if (instance == null) {
         if (loggerName == null) {
            throw new IllegalStateException("DebugLog has not been initialized!");
         }

         instance = new DebugFileLogger(loggerName, fileName);
      }

      return instance;
   }

   public static synchronized boolean isClosed() {
      return instance == null;
   }

   protected DebugFileLogger(String logger, String file) {
      super();
      this.log = Logger.getLogger(logger);
      FileHandler fh = null;

      try {
         fh = new FileHandler(file, true);
         this.log.setUseParentHandlers(false);
         Set<Handler> toRemove = new HashSet(this.log.getHandlers().length);

         for(Handler handler : this.log.getHandlers()) {
            toRemove.add(handler);
         }

         for(Handler handler : toRemove) {
            this.log.removeHandler(handler);
         }

         this.log.addHandler(fh);
         this.log.setLevel(Level.ALL);
         fh.setFormatter(new LogFormatter());
      } catch (SecurityException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

      if (fh != null) {
         this.fileHandler = fh;
      } else {
         this.fileHandler = fh;
      }

   }

   public void log(LogRecord record) {
      this.log.log(record);
   }

   public void log(Level level, String msg) {
      this.log(new LogRecord(level, msg));
   }

   public synchronized void close() {
      this.log.removeHandler(this.fileHandler);
      this.fileHandler.close();
      instance = null;
   }

   private static class LogFormatter extends Formatter {
      private final SimpleDateFormat date;

      private LogFormatter() {
         super();
         this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      }

      public String format(LogRecord record) {
         StringBuilder builder = new StringBuilder();
         Throwable ex = record.getThrown();
         builder.append(this.date.format(record.getMillis()));
         builder.append(" [");
         builder.append(record.getLevel().getLocalizedName().toUpperCase());
         builder.append("] ");
         builder.append(record.getMessage());
         builder.append('\n');
         if (ex != null) {
            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            builder.append(writer);
         }

         return builder.toString();
      }
   }
}
