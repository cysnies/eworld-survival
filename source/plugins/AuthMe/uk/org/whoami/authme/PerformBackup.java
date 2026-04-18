package uk.org.whoami.authme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.settings.Settings;

public class PerformBackup {
   private String dbName;
   private String dbUserName;
   private String dbPassword;
   private String tblname;
   SimpleDateFormat format;
   String dateString;
   private String path;
   private AuthMe instance;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$uk$org$whoami$authme$datasource$DataSource$DataSourceType;

   public PerformBackup(AuthMe instance) {
      super();
      this.dbName = Settings.getMySQLDatabase;
      this.dbUserName = Settings.getMySQLUsername;
      this.dbPassword = Settings.getMySQLPassword;
      this.tblname = Settings.getMySQLTablename;
      this.format = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
      this.dateString = this.format.format(new Date());
      this.path = AuthMe.getInstance().getDataFolder() + "/backups/backup" + this.dateString;
      this.setInstance(instance);
   }

   public boolean DoBackup() {
      switch (Settings.getDataSource) {
         case MYSQL:
            return this.MySqlBackup();
         case FILE:
            return this.FileBackup("auths.db");
         case SQLITE:
            return this.FileBackup(Settings.getMySQLDatabase + ".db");
         default:
            return false;
      }
   }

   private boolean MySqlBackup() {
      File dirBackup = new File(AuthMe.getInstance().getDataFolder() + "/backups");
      if (!dirBackup.exists()) {
         dirBackup.mkdir();
      }

      if (this.checkWindows(Settings.backupWindowsPath)) {
         String executeCmd = Settings.backupWindowsPath + "\\bin\\mysqldump.exe -u " + this.dbUserName + " -p" + this.dbPassword + " " + this.dbName + " --tables " + this.tblname + " -r " + this.path + ".sql";

         try {
            Process runtimeProcess = Runtime.getRuntime().exec(executeCmd);
            int processComplete = runtimeProcess.waitFor();
            if (processComplete == 0) {
               ConsoleLogger.info("Backup created successfully");
               return true;
            }

            ConsoleLogger.info("Could not create the backup");
         } catch (Exception ex) {
            ex.printStackTrace();
         }
      } else {
         String executeCmd = "mysqldump -u " + this.dbUserName + " -p" + this.dbPassword + " " + this.dbName + " --tables " + this.tblname + " -r " + this.path + ".sql";

         try {
            Process runtimeProcess = Runtime.getRuntime().exec(executeCmd);
            int processComplete = runtimeProcess.waitFor();
            if (processComplete == 0) {
               ConsoleLogger.info("Backup created successfully");
               return true;
            }

            ConsoleLogger.info("Could not create the backup");
         } catch (Exception ex) {
            ex.printStackTrace();
         }
      }

      return false;
   }

   private boolean FileBackup(String backend) {
      File dirBackup = new File(AuthMe.getInstance().getDataFolder() + "/backups");
      if (!dirBackup.exists()) {
         dirBackup.mkdir();
      }

      try {
         this.copy(new File("plugins/AuthMe/" + backend), new File(this.path + ".db"));
         return true;
      } catch (Exception ex) {
         ex.printStackTrace();
         return false;
      }
   }

   private boolean checkWindows(String windowsPath) {
      String isWin = System.getProperty("os.name").toLowerCase();
      if (isWin.indexOf("win") >= 0) {
         if ((new File(windowsPath + "\\bin\\mysqldump.exe")).exists()) {
            return true;
         } else {
            ConsoleLogger.showError("Mysql Windows Path is incorrect please check it");
            return true;
         }
      } else {
         return false;
      }
   }

   void copy(File src, File dst) throws IOException {
      InputStream in = new FileInputStream(src);
      OutputStream out = new FileOutputStream(dst);
      byte[] buf = new byte[1024];

      int len;
      while((len = in.read(buf)) > 0) {
         out.write(buf, 0, len);
      }

      in.close();
      out.close();
   }

   public void setInstance(AuthMe instance) {
      this.instance = instance;
   }

   public AuthMe getInstance() {
      return this.instance;
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$uk$org$whoami$authme$datasource$DataSource$DataSourceType() {
      int[] var10000 = $SWITCH_TABLE$uk$org$whoami$authme$datasource$DataSource$DataSourceType;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[DataSource.DataSourceType.values().length];

         try {
            var0[DataSource.DataSourceType.FILE.ordinal()] = 2;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[DataSource.DataSourceType.MYSQL.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[DataSource.DataSourceType.SQLITE.ordinal()] = 3;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$uk$org$whoami$authme$datasource$DataSource$DataSourceType = var0;
         return var0;
      }
   }
}
