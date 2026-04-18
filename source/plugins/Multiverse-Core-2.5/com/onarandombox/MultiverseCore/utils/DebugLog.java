package com.onarandombox.MultiverseCore.utils;

import com.onarandombox.MultiverseCore.MultiverseCoreConfiguration;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/** @deprecated */
@Deprecated
public class DebugLog extends Logger {
   private FileHandler fh;
   private Logger standardLog = null;
   private String prefix = "[MVCore-Debug] ";

   public DebugLog(String logger, String file) {
      super(logger, (String)null);

      try {
         this.fh = new FileHandler(file, true);
         this.setUseParentHandlers(false);

         for(Handler handler : Arrays.asList(this.getHandlers())) {
            this.removeHandler(handler);
         }

         this.addHandler(this.fh);
         this.setLevel(Level.ALL);
         this.fh.setFormatter(new LogFormatter());
      } catch (SecurityException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   public void setTag(String tag) {
      this.prefix = tag + " ";
   }

   public void setStandardLogger(Logger logger) {
      this.standardLog = logger;
   }

   public void log(Level level, String msg) {
      if (MultiverseCoreConfiguration.isSet() && MultiverseCoreConfiguration.getInstance().getGlobalDebug() > 0) {
         if (level.intValue() < Level.INFO.intValue() && this.standardLog != null) {
            this.standardLog.log(Level.INFO, this.prefix + msg);
         }

         super.log(level, this.prefix + msg);
      }

   }

   public void close() {
      this.fh.close();
   }

   private class LogFormatter extends Formatter {
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
