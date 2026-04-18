package uk.org.whoami.authme.settings;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import uk.org.whoami.authme.ConsoleLogger;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.security.PasswordSecurity;

public final class Settings extends YamlConfiguration {
   public static final String PLUGIN_FOLDER = "./plugins/AuthMe";
   public static final String CACHE_FOLDER = "./plugins/AuthMe/cache";
   public static final String AUTH_FILE = "./plugins/AuthMe/auths.db";
   public static final String MESSAGE_FILE = "./plugins/AuthMe/messages";
   public static final String SETTINGS_FILE = "./plugins/AuthMe/config.yml";
   public static List allowCommands = null;
   public static List getJoinPermissions = null;
   public static List getUnrestrictedName = null;
   private static List getRestrictedIp;
   public static List getMySQLOtherUsernameColumn = null;
   public static List getForcedWorlds = null;
   public final Plugin plugin;
   private final File file;
   public static DataSource.DataSourceType getDataSource;
   public static PasswordSecurity.HashAlgorithm getPasswordHash;
   public static PasswordSecurity.HashAlgorithm rakamakHash;
   public static Boolean useLogging = false;
   public static Boolean isPermissionCheckEnabled;
   public static Boolean isRegistrationEnabled;
   public static Boolean isForcedRegistrationEnabled;
   public static Boolean isTeleportToSpawnEnabled;
   public static Boolean isSessionsEnabled;
   public static Boolean isChatAllowed;
   public static Boolean isAllowRestrictedIp;
   public static Boolean isMovementAllowed;
   public static Boolean isKickNonRegisteredEnabled;
   public static Boolean isForceSingleSessionEnabled;
   public static Boolean isForceSpawnLocOnJoinEnabled;
   public static Boolean isForceExactSpawnEnabled;
   public static Boolean isSaveQuitLocationEnabled;
   public static Boolean isForceSurvivalModeEnabled;
   public static Boolean isResetInventoryIfCreative;
   public static Boolean isCachingEnabled;
   public static Boolean isKickOnWrongPasswordEnabled;
   public static Boolean getEnablePasswordVerifier;
   public static Boolean protectInventoryBeforeLogInEnabled;
   public static Boolean isBackupActivated;
   public static Boolean isBackupOnStart;
   public static Boolean isBackupOnStop;
   public static Boolean enablePasspartu;
   public static Boolean isStopEnabled;
   public static Boolean reloadSupport;
   public static Boolean rakamakUseIp;
   public static Boolean noConsoleSpam;
   public static Boolean removePassword;
   public static Boolean displayOtherAccounts;
   public static Boolean useCaptcha;
   public static Boolean emailRegistration;
   public static Boolean multiverse;
   public static Boolean notifications;
   public static Boolean chestshop;
   public static Boolean bungee;
   public static Boolean banUnsafeIp;
   public static Boolean doubleEmailCheck;
   public static Boolean sessionExpireOnIpChange;
   public static Boolean disableSocialSpy;
   public static Boolean useMultiThreading;
   public static Boolean forceOnlyAfterLogin;
   public static Boolean useEssentialsMotd;
   public static String getNickRegex;
   public static String getUnloggedinGroup;
   public static String getMySQLHost;
   public static String getMySQLPort;
   public static String getMySQLUsername;
   public static String getMySQLPassword;
   public static String getMySQLDatabase;
   public static String getMySQLTablename;
   public static String getMySQLColumnName;
   public static String getMySQLColumnPassword;
   public static String getMySQLColumnIp;
   public static String getMySQLColumnLastLogin;
   public static String getMySQLColumnSalt;
   public static String getMySQLColumnGroup;
   public static String getMySQLColumnEmail;
   public static String unRegisteredGroup;
   public static String backupWindowsPath;
   public static String getcUnrestrictedName;
   public static String getRegisteredGroup;
   public static String messagesLanguage;
   public static String getMySQLlastlocX;
   public static String getMySQLlastlocY;
   public static String getMySQLlastlocZ;
   public static String rakamakUsers;
   public static String rakamakUsersIp;
   public static String getmailAccount;
   public static String getmailPassword;
   public static String getmailSMTP;
   public static String getMySQLColumnId;
   public static String getmailSenderName;
   public static String getPredefinedSalt;
   public static String getMailSubject;
   public static String getMailText;
   public static String getMySQLlastlocWorld;
   public static int getWarnMessageInterval;
   public static int getSessionTimeout;
   public static int getRegistrationTimeout;
   public static int getMaxNickLength;
   public static int getMinNickLength;
   public static int getPasswordMinLen;
   public static int getMovementRadius;
   public static int getmaxRegPerIp;
   public static int getNonActivatedGroup;
   public static int passwordMaxLength;
   public static int getRecoveryPassLength;
   public static int getMailPort;
   public static int maxLoginTry;
   public static int captchaLength;
   public static int saltLength;
   public static int getmaxRegPerEmail;
   public static int bCryptLog2Rounds;
   protected static YamlConfiguration configFile;

   public Settings(Plugin plugin) {
      super();
      this.file = new File(plugin.getDataFolder(), "config.yml");
      this.plugin = plugin;
      if (this.exists()) {
         this.load();
      } else {
         this.loadDefaults(this.file.getName());
         this.load();
      }

      configFile = (YamlConfiguration)plugin.getConfig();
   }

   public void loadConfigOptions() {
      this.plugin.getLogger().info("Loading Configuration File...");
      this.mergeConfig();
      messagesLanguage = checkLang(configFile.getString("settings.messagesLanguage", "en"));
      isPermissionCheckEnabled = configFile.getBoolean("permission.EnablePermissionCheck", false);
      isForcedRegistrationEnabled = configFile.getBoolean("settings.registration.force", true);
      isRegistrationEnabled = configFile.getBoolean("settings.registration.enabled", true);
      isTeleportToSpawnEnabled = configFile.getBoolean("settings.restrictions.teleportUnAuthedToSpawn", false);
      getWarnMessageInterval = configFile.getInt("settings.registration.messageInterval", 5);
      isSessionsEnabled = configFile.getBoolean("settings.sessions.enabled", false);
      getSessionTimeout = configFile.getInt("settings.sessions.timeout", 10);
      getRegistrationTimeout = configFile.getInt("settings.restrictions.timeout", 30);
      isChatAllowed = configFile.getBoolean("settings.restrictions.allowChat", false);
      getMaxNickLength = configFile.getInt("settings.restrictions.maxNicknameLength", 20);
      getMinNickLength = configFile.getInt("settings.restrictions.minNicknameLength", 3);
      getPasswordMinLen = configFile.getInt("settings.security.minPasswordLength", 4);
      getNickRegex = configFile.getString("settings.restrictions.allowedNicknameCharacters", "[a-zA-Z0-9_?]*");
      isAllowRestrictedIp = configFile.getBoolean("settings.restrictions.AllowRestrictedUser", false);
      getRestrictedIp = configFile.getStringList("settings.restrictions.AllowedRestrictedUser");
      isMovementAllowed = configFile.getBoolean("settings.restrictions.allowMovement", false);
      getMovementRadius = configFile.getInt("settings.restrictions.allowedMovementRadius", 100);
      getJoinPermissions = configFile.getStringList("GroupOptions.Permissions.PermissionsOnJoin");
      isKickOnWrongPasswordEnabled = configFile.getBoolean("settings.restrictions.kickOnWrongPassword", false);
      isKickNonRegisteredEnabled = configFile.getBoolean("settings.restrictions.kickNonRegistered", false);
      isForceSingleSessionEnabled = configFile.getBoolean("settings.restrictions.ForceSingleSession", true);
      isForceSpawnLocOnJoinEnabled = configFile.getBoolean("settings.restrictions.ForceSpawnLocOnJoinEnabled", false);
      isSaveQuitLocationEnabled = configFile.getBoolean("settings.restrictions.SaveQuitLocation", false);
      isForceSurvivalModeEnabled = configFile.getBoolean("settings.GameMode.ForceSurvivalMode", false);
      isResetInventoryIfCreative = configFile.getBoolean("settings.GameMode.ResetInventoryIfCreative", false);
      getmaxRegPerIp = configFile.getInt("settings.restrictions.maxRegPerIp", 1);
      getPasswordHash = getPasswordHash();
      getUnloggedinGroup = configFile.getString("settings.security.unLoggedinGroup", "unLoggedInGroup");
      getDataSource = getDataSource();
      isCachingEnabled = configFile.getBoolean("DataSource.caching", true);
      getMySQLHost = configFile.getString("DataSource.mySQLHost", "127.0.0.1");
      getMySQLPort = configFile.getString("DataSource.mySQLPort", "3306");
      getMySQLUsername = configFile.getString("DataSource.mySQLUsername", "authme");
      getMySQLPassword = configFile.getString("DataSource.mySQLPassword", "12345");
      getMySQLDatabase = configFile.getString("DataSource.mySQLDatabase", "authme");
      getMySQLTablename = configFile.getString("DataSource.mySQLTablename", "authme");
      getMySQLColumnEmail = configFile.getString("DataSource.mySQLColumnEmail", "email");
      getMySQLColumnName = configFile.getString("DataSource.mySQLColumnName", "username");
      getMySQLColumnPassword = configFile.getString("DataSource.mySQLColumnPassword", "password");
      getMySQLColumnIp = configFile.getString("DataSource.mySQLColumnIp", "ip");
      getMySQLColumnLastLogin = configFile.getString("DataSource.mySQLColumnLastLogin", "lastlogin");
      getMySQLColumnSalt = configFile.getString("ExternalBoardOptions.mySQLColumnSalt");
      getMySQLColumnGroup = configFile.getString("ExternalBoardOptions.mySQLColumnGroup", "");
      getMySQLlastlocX = configFile.getString("DataSource.mySQLlastlocX", "x");
      getMySQLlastlocY = configFile.getString("DataSource.mySQLlastlocY", "y");
      getMySQLlastlocZ = configFile.getString("DataSource.mySQLlastlocZ", "z");
      getMySQLlastlocWorld = configFile.getString("DataSource.mySQLlastlocWorld", "world");
      getNonActivatedGroup = configFile.getInt("ExternalBoardOptions.nonActivedUserGroup", -1);
      unRegisteredGroup = configFile.getString("GroupOptions.UnregisteredPlayerGroup", "");
      getUnrestrictedName = configFile.getStringList("settings.unrestrictions.UnrestrictedName");
      getRegisteredGroup = configFile.getString("GroupOptions.RegisteredPlayerGroup", "");
      getEnablePasswordVerifier = configFile.getBoolean("settings.restrictions.enablePasswordVerifier", true);
      protectInventoryBeforeLogInEnabled = configFile.getBoolean("settings.restrictions.ProtectInventoryBeforeLogIn", true);
      passwordMaxLength = configFile.getInt("settings.security.passwordMaxLength", 20);
      isBackupActivated = configFile.getBoolean("BackupSystem.ActivateBackup", false);
      isBackupOnStart = configFile.getBoolean("BackupSystem.OnServerStart", false);
      isBackupOnStop = configFile.getBoolean("BackupSystem.OnServeStop", false);
      backupWindowsPath = configFile.getString("BackupSystem.MysqlWindowsPath", "C:\\Program Files\\MySQL\\MySQL Server 5.1\\");
      enablePasspartu = configFile.getBoolean("Passpartu.enablePasspartu", false);
      isStopEnabled = configFile.getBoolean("Security.SQLProblem.stopServer", true);
      reloadSupport = configFile.getBoolean("Security.ReloadCommand.useReloadCommandSupport", true);
      allowCommands = configFile.getList("settings.restrictions.allowCommands");
      if (configFile.contains("allowCommands")) {
         if (!allowCommands.contains("/login")) {
            allowCommands.add("/login");
         }

         if (!allowCommands.contains("/register")) {
            allowCommands.add("/register");
         }

         if (!allowCommands.contains("/l")) {
            allowCommands.add("/l");
         }

         if (!allowCommands.contains("/reg")) {
            allowCommands.add("/reg");
         }

         if (!allowCommands.contains("/passpartu")) {
            allowCommands.add("/passpartu");
         }

         if (!allowCommands.contains("/email")) {
            allowCommands.add("/email");
         }

         if (!allowCommands.contains("/captcha")) {
            allowCommands.add("/captcha");
         }
      }

      rakamakUsers = configFile.getString("Converter.Rakamak.fileName", "users.rak");
      rakamakUsersIp = configFile.getString("Converter.Rakamak.ipFileName", "UsersIp.rak");
      rakamakUseIp = configFile.getBoolean("Converter.Rakamak.useIp", false);
      rakamakHash = getRakamakHash();
      noConsoleSpam = configFile.getBoolean("Security.console.noConsoleSpam", false);
      removePassword = configFile.getBoolean("Security.console.removePassword", true);
      getmailAccount = configFile.getString("Email.mailAccount", "");
      getmailPassword = configFile.getString("Email.mailPassword", "");
      getmailSMTP = configFile.getString("Email.mailSMTP", "smtp.gmail.com");
      getMailPort = configFile.getInt("Email.mailPort", 465);
      getRecoveryPassLength = configFile.getInt("Email.RecoveryPasswordLength", 8);
      getMySQLOtherUsernameColumn = configFile.getList("ExternalBoardOptions.mySQLOtherUsernameColumns", new ArrayList());
      displayOtherAccounts = configFile.getBoolean("settings.restrictions.displayOtherAccounts", true);
      getMySQLColumnId = configFile.getString("DataSource.mySQLColumnId", "id");
      getmailSenderName = configFile.getString("Email.mailSenderName", "");
      getPredefinedSalt = configFile.getString("Xenoforo.predefinedSalt", "");
      useCaptcha = configFile.getBoolean("Security.captcha.useCaptcha", false);
      maxLoginTry = configFile.getInt("Security.captcha.maxLoginTry", 5);
      captchaLength = configFile.getInt("Security.captcha.captchaLength", 5);
      getMailSubject = configFile.getString("Email.mailSubject", "Your new AuthMe Password");
      getMailText = configFile.getString("Email.mailText", "Dear <playername>, <br /><br /> This is your new AuthMe password for the server <br /><br /> <servername> : <br /><br /> <generatedpass><br /><br />Do not forget to change password after login! <br /> /changepassword <generatedpass> newPassword");
      emailRegistration = configFile.getBoolean("settings.registration.enableEmailRegistrationSystem", false);
      saltLength = configFile.getInt("settings.security.doubleMD5SaltLength", 8);
      getmaxRegPerEmail = configFile.getInt("Email.maxRegPerEmail", 1);
      multiverse = configFile.getBoolean("Hooks.multiverse", true);
      chestshop = configFile.getBoolean("Hooks.chestshop", true);
      notifications = configFile.getBoolean("Hooks.notifications", true);
      bungee = configFile.getBoolean("Hooks.bungeecord", false);
      getForcedWorlds = configFile.getList("settings.restrictions.ForceSpawnOnTheseWorlds");
      banUnsafeIp = configFile.getBoolean("settings.restrictions.banUnsafedIP", false);
      doubleEmailCheck = configFile.getBoolean("settings.registration.doubleEmailCheck", false);
      sessionExpireOnIpChange = configFile.getBoolean("settings.sessions.sessionExpireOnIpChange", false);
      useLogging = configFile.getBoolean("Security.console.logConsole", false);
      disableSocialSpy = configFile.getBoolean("Hooks.disableSocialSpy", true);
      useMultiThreading = configFile.getBoolean("Performances.useMultiThreading", false);
      bCryptLog2Rounds = configFile.getInt("ExternalBoardOptions.bCryptLog2Round", 10);
      forceOnlyAfterLogin = configFile.getBoolean("settings.GameMode.ForceOnlyAfterLogin", false);
      useEssentialsMotd = configFile.getBoolean("Hooks.useEssentialsMotd", false);
      this.saveDefaults();
   }

   public static void reloadConfigOptions(YamlConfiguration newConfig) {
      configFile = newConfig;
      messagesLanguage = checkLang(configFile.getString("settings.messagesLanguage", "en"));
      isPermissionCheckEnabled = configFile.getBoolean("permission.EnablePermissionCheck", false);
      isForcedRegistrationEnabled = configFile.getBoolean("settings.registration.force", true);
      isRegistrationEnabled = configFile.getBoolean("settings.registration.enabled", true);
      isTeleportToSpawnEnabled = configFile.getBoolean("settings.restrictions.teleportUnAuthedToSpawn", false);
      getWarnMessageInterval = configFile.getInt("settings.registration.messageInterval", 5);
      isSessionsEnabled = configFile.getBoolean("settings.sessions.enabled", false);
      getSessionTimeout = configFile.getInt("settings.sessions.timeout", 10);
      getRegistrationTimeout = configFile.getInt("settings.restrictions.timeout", 30);
      isChatAllowed = configFile.getBoolean("settings.restrictions.allowChat", false);
      getMaxNickLength = configFile.getInt("settings.restrictions.maxNicknameLength", 20);
      getMinNickLength = configFile.getInt("settings.restrictions.minNicknameLength", 3);
      getPasswordMinLen = configFile.getInt("settings.security.minPasswordLength", 4);
      getNickRegex = configFile.getString("settings.restrictions.allowedNicknameCharacters", "[a-zA-Z0-9_?]*");
      isAllowRestrictedIp = configFile.getBoolean("settings.restrictions.AllowRestrictedUser", false);
      getRestrictedIp = configFile.getStringList("settings.restrictions.AllowedRestrictedUser");
      isMovementAllowed = configFile.getBoolean("settings.restrictions.allowMovement", false);
      getMovementRadius = configFile.getInt("settings.restrictions.allowedMovementRadius", 100);
      getJoinPermissions = configFile.getStringList("GroupOptions.Permissions.PermissionsOnJoin");
      isKickOnWrongPasswordEnabled = configFile.getBoolean("settings.restrictions.kickOnWrongPassword", false);
      isKickNonRegisteredEnabled = configFile.getBoolean("settings.restrictions.kickNonRegistered", false);
      isForceSingleSessionEnabled = configFile.getBoolean("settings.restrictions.ForceSingleSession", true);
      isForceSpawnLocOnJoinEnabled = configFile.getBoolean("settings.restrictions.ForceSpawnLocOnJoinEnabled", false);
      isSaveQuitLocationEnabled = configFile.getBoolean("settings.restrictions.SaveQuitLocation", false);
      isForceSurvivalModeEnabled = configFile.getBoolean("settings.GameMode.ForceSurvivalMode", false);
      isResetInventoryIfCreative = configFile.getBoolean("settings.GameMode.ResetInventoryIfCreative", false);
      getmaxRegPerIp = configFile.getInt("settings.restrictions.maxRegPerIp", 1);
      getPasswordHash = getPasswordHash();
      getUnloggedinGroup = configFile.getString("settings.security.unLoggedinGroup", "unLoggedInGroup");
      getDataSource = getDataSource();
      isCachingEnabled = configFile.getBoolean("DataSource.caching", true);
      getMySQLHost = configFile.getString("DataSource.mySQLHost", "127.0.0.1");
      getMySQLPort = configFile.getString("DataSource.mySQLPort", "3306");
      getMySQLUsername = configFile.getString("DataSource.mySQLUsername", "authme");
      getMySQLPassword = configFile.getString("DataSource.mySQLPassword", "12345");
      getMySQLDatabase = configFile.getString("DataSource.mySQLDatabase", "authme");
      getMySQLTablename = configFile.getString("DataSource.mySQLTablename", "authme");
      getMySQLColumnEmail = configFile.getString("DataSource.mySQLColumnEmail", "email");
      getMySQLColumnName = configFile.getString("DataSource.mySQLColumnName", "username");
      getMySQLColumnPassword = configFile.getString("DataSource.mySQLColumnPassword", "password");
      getMySQLColumnIp = configFile.getString("DataSource.mySQLColumnIp", "ip");
      getMySQLColumnLastLogin = configFile.getString("DataSource.mySQLColumnLastLogin", "lastlogin");
      getMySQLlastlocX = configFile.getString("DataSource.mySQLlastlocX", "x");
      getMySQLlastlocY = configFile.getString("DataSource.mySQLlastlocY", "y");
      getMySQLlastlocZ = configFile.getString("DataSource.mySQLlastlocZ", "z");
      getMySQLlastlocWorld = configFile.getString("DataSource.mySQLlastlocWorld", "world");
      getMySQLColumnSalt = configFile.getString("ExternalBoardOptions.mySQLColumnSalt", "");
      getMySQLColumnGroup = configFile.getString("ExternalBoardOptions.mySQLColumnGroup", "");
      getNonActivatedGroup = configFile.getInt("ExternalBoardOptions.nonActivedUserGroup", -1);
      unRegisteredGroup = configFile.getString("GroupOptions.UnregisteredPlayerGroup", "");
      getUnrestrictedName = configFile.getStringList("settings.unrestrictions.UnrestrictedName");
      getRegisteredGroup = configFile.getString("GroupOptions.RegisteredPlayerGroup", "");
      getEnablePasswordVerifier = configFile.getBoolean("settings.restrictions.enablePasswordVerifier", true);
      protectInventoryBeforeLogInEnabled = configFile.getBoolean("settings.restrictions.ProtectInventoryBeforeLogIn", true);
      passwordMaxLength = configFile.getInt("settings.security.passwordMaxLength", 20);
      isBackupActivated = configFile.getBoolean("BackupSystem.ActivateBackup", false);
      isBackupOnStart = configFile.getBoolean("BackupSystem.OnServerStart", false);
      isBackupOnStop = configFile.getBoolean("BackupSystem.OnServeStop", false);
      backupWindowsPath = configFile.getString("BackupSystem.MysqlWindowsPath", "C:\\Program Files\\MySQL\\MySQL Server 5.1\\");
      enablePasspartu = configFile.getBoolean("Passpartu.enablePasspartu", false);
      isStopEnabled = configFile.getBoolean("Security.SQLProblem.stopServer", true);
      reloadSupport = configFile.getBoolean("Security.ReloadCommand.useReloadCommandSupport", true);
      allowCommands = configFile.getList("settings.restrictions.allowCommands");
      if (configFile.contains("allowCommands")) {
         if (!allowCommands.contains("/login")) {
            allowCommands.add("/login");
         }

         if (!allowCommands.contains("/register")) {
            allowCommands.add("/register");
         }

         if (!allowCommands.contains("/l")) {
            allowCommands.add("/l");
         }

         if (!allowCommands.contains("/reg")) {
            allowCommands.add("/reg");
         }

         if (!allowCommands.contains("/passpartu")) {
            allowCommands.add("/passpartu");
         }

         if (!allowCommands.contains("/email")) {
            allowCommands.add("/email");
         }

         if (!allowCommands.contains("/captcha")) {
            allowCommands.add("/captcha");
         }
      }

      rakamakUsers = configFile.getString("Converter.Rakamak.fileName", "users.rak");
      rakamakUsersIp = configFile.getString("Converter.Rakamak.ipFileName", "UsersIp.rak");
      rakamakUseIp = configFile.getBoolean("Converter.Rakamak.useIp", false);
      rakamakHash = getRakamakHash();
      noConsoleSpam = configFile.getBoolean("Security.console.noConsoleSpam", false);
      removePassword = configFile.getBoolean("Security.console.removePassword", true);
      getmailAccount = configFile.getString("Email.mailAccount", "");
      getmailPassword = configFile.getString("Email.mailPassword", "");
      getmailSMTP = configFile.getString("Email.mailSMTP", "smtp.gmail.com");
      getMailPort = configFile.getInt("Email.mailPort", 465);
      getRecoveryPassLength = configFile.getInt("Email.RecoveryPasswordLength", 8);
      getMySQLOtherUsernameColumn = configFile.getList("ExternalBoardOptions.mySQLOtherUsernameColumns", new ArrayList());
      displayOtherAccounts = configFile.getBoolean("settings.restrictions.displayOtherAccounts", true);
      getMySQLColumnId = configFile.getString("DataSource.mySQLColumnId", "id");
      getmailSenderName = configFile.getString("Email.mailSenderName", "");
      getPredefinedSalt = configFile.getString("Xenoforo.predefinedSalt", "");
      useCaptcha = configFile.getBoolean("Security.captcha.useCaptcha", false);
      maxLoginTry = configFile.getInt("Security.captcha.maxLoginTry", 5);
      captchaLength = configFile.getInt("Security.captcha.captchaLength", 5);
      getMailSubject = configFile.getString("Email.mailSubject", "Your new AuthMe Password");
      getMailText = configFile.getString("Email.mailText", "Dear <playername>, <br /><br /> This is your new AuthMe password for the server <br /><br /> <servername> : <br /><br /> <generatedpass><br /><br />Do not forget to change password after login! <br /> /changepassword <generatedpass> newPassword");
      emailRegistration = configFile.getBoolean("settings.registration.enableEmailRegistrationSystem", false);
      saltLength = configFile.getInt("settings.security.doubleMD5SaltLength", 8);
      getmaxRegPerEmail = configFile.getInt("Email.maxRegPerEmail", 1);
      multiverse = configFile.getBoolean("Hooks.multiverse", true);
      chestshop = configFile.getBoolean("Hooks.chestshop", true);
      notifications = configFile.getBoolean("Hooks.notifications", true);
      bungee = configFile.getBoolean("Hooks.bungeecord", false);
      getForcedWorlds = configFile.getList("settings.restrictions.ForceSpawnOnTheseWorlds");
      banUnsafeIp = configFile.getBoolean("settings.restrictions.banUnsafedIP", false);
      doubleEmailCheck = configFile.getBoolean("settings.registration.doubleEmailCheck", false);
      sessionExpireOnIpChange = configFile.getBoolean("settings.sessions.sessionExpireOnIpChange", false);
      useLogging = configFile.getBoolean("Security.console.logConsole", false);
      disableSocialSpy = configFile.getBoolean("Hooks.disableSocialSpy", true);
      useMultiThreading = configFile.getBoolean("Performances.useMultiThreading", false);
      bCryptLog2Rounds = configFile.getInt("ExternalBoardOptions.bCryptLog2Round", 10);
      forceOnlyAfterLogin = configFile.getBoolean("settings.GameMode.ForceOnlyAfterLogin", false);
      useEssentialsMotd = configFile.getBoolean("Hooks.useEssentialsMotd", false);
   }

   public void mergeConfig() {
      if (this.contains("settings.restrictions.allowedPluginTeleportHandler")) {
         this.set("settings.restrictions.allowedPluginTeleportHandler", (Object)null);
      }

      if (!this.contains("DataSource.mySQLColumnEmail")) {
         this.set("DataSource.mySQLColumnEmail", "email");
      }

      if (this.contains("Email.GmailAccount")) {
         this.set("Email.mailAccount", this.getString("Email.GmailAccount"));
         this.set("Email.GmailAccount", (Object)null);
      }

      if (this.contains("Email.GmailPassword")) {
         this.set("Email.mailPassword", this.getString("Email.GmailPassword"));
         this.set("Email.GmailPassword", (Object)null);
      }

      if (!this.contains("Email.RecoveryPasswordLength")) {
         this.set("Email.RecoveryPasswordLength", 8);
      }

      if (!this.contains("Email.mailPort")) {
         this.set("Email.mailPort", 465);
      }

      if (!this.contains("Email.mailSMTP")) {
         this.set("Email.mailSMTP", "smtp.gmail.com");
      }

      if (!this.contains("Email.mailAccount")) {
         this.set("Email.mailAccount", "");
      }

      if (!this.contains("Email.mailPassword")) {
         this.set("Email.mailPassword", "");
      }

      if (!this.contains("ExternalBoardOptions.mySQLOtherUsernameColumns")) {
         this.set("ExternalBoardOptions.mySQLOtherUsernameColumns", new ArrayList());
      }

      if (!this.contains("settings.restrictions.displayOtherAccounts")) {
         this.set("settings.restrictions.displayOtherAccounts", true);
      }

      if (!this.contains("DataSource.mySQLColumnId")) {
         this.set("DataSource.mySQLColumnId", "id");
      }

      if (!this.contains("Email.mailSenderName")) {
         this.set("Email.mailSenderName", "");
      }

      if (!this.contains("Xenoforo.predefinedSalt")) {
         this.set("Xenoforo.predefinedSalt", "");
      }

      if (!this.contains("Security.captcha.useCaptcha")) {
         this.set("Security.captcha.useCaptcha", false);
      }

      if (!this.contains("Security.captcha.maxLoginTry")) {
         this.set("Security.captcha.maxLoginTry", 5);
      }

      if (!this.contains("Security.captcha.captchaLength")) {
         this.set("Security.captcha.captchaLength", 5);
      }

      if (!this.contains("Email.mailSubject")) {
         this.set("Email.mailSubject", "");
      }

      if (!this.contains("Email.mailText")) {
         this.set("Email.mailText", "Dear <playername>, <br /><br /> This is your new AuthMe password for the server <br /><br /> <servername> : <br /><br /> <generatedpass><br /><br />Do not forget to change password after login! <br /> /changepassword <generatedpass> newPassword");
      }

      if (this.contains("Email.mailText")) {
         try {
            String s = this.getString("Email.mailText");
            s = s.replaceAll("\n", "<br />");
            this.set("Email.mailText", (Object)null);
            this.set("Email.mailText", s);
         } catch (Exception var2) {
         }
      }

      if (!this.contains("settings.registration.enableEmailRegistrationSystem")) {
         this.set("settings.registration.enableEmailRegistrationSystem", false);
      }

      if (!this.contains("settings.security.doubleMD5SaltLength")) {
         this.set("settings.security.doubleMD5SaltLength", 8);
      }

      if (!this.contains("Email.maxRegPerEmail")) {
         this.set("Email.maxRegPerEmail", 1);
      }

      if (!this.contains("Hooks.multiverse")) {
         this.set("Hooks.multiverse", true);
         this.set("Hooks.chestshop", true);
         this.set("Hooks.notifications", true);
         this.set("Hooks.bungeecord", false);
      }

      if (!this.contains("settings.restrictions.ForceSpawnOnTheseWorlds")) {
         this.set("settings.restrictions.ForceSpawnOnTheseWorlds", new ArrayList());
      }

      if (!this.contains("settings.restrictions.banUnsafedIP")) {
         this.set("settings.restrictions.banUnsafedIP", false);
      }

      if (!this.contains("settings.registration.doubleEmailCheck")) {
         this.set("settings.registration.doubleEmailCheck", false);
      }

      if (!this.contains("settings.sessions.sessionExpireOnIpChange")) {
         this.set("settings.sessions.sessionExpireOnIpChange", false);
      }

      if (!this.contains("Security.console.logConsole")) {
         this.set("Security.console.logConsole", false);
      }

      if (!this.contains("Hooks.disableSocialSpy")) {
         this.set("Hooks.disableSocialSpy", true);
      }

      if (!this.contains("Performances.useMultiThreading")) {
         this.set("Performances.useMultiThreading", false);
      }

      if (!this.contains("ExternalBoardOptions.bCryptLog2Round")) {
         this.set("ExternalBoardOptions.bCryptLog2Round", 10);
      }

      if (!this.contains("DataSource.mySQLlastlocWorld")) {
         this.set("DataSource.mySQLlastlocWorld", "world");
      }

      if (!this.contains("settings.GameMode.ForceOnlyAfterLogin")) {
         this.set("settings.GameMode.ForceOnlyAfterLogin", false);
      }

      if (!this.contains("Hooks.useEssentialsMotd")) {
         this.set("Hooks.useEssentialsMotd", false);
      }

      this.plugin.getLogger().info("Merge new Config Options if needed..");
      this.plugin.saveConfig();
   }

   private static PasswordSecurity.HashAlgorithm getPasswordHash() {
      String key = "settings.security.passwordHash";

      try {
         return PasswordSecurity.HashAlgorithm.valueOf(configFile.getString(key, "SHA256").toUpperCase());
      } catch (IllegalArgumentException var2) {
         ConsoleLogger.showError("Unknown Hash Algorithm; defaulting to SHA256");
         return PasswordSecurity.HashAlgorithm.SHA256;
      }
   }

   private static PasswordSecurity.HashAlgorithm getRakamakHash() {
      String key = "Converter.Rakamak.newPasswordHash";

      try {
         return PasswordSecurity.HashAlgorithm.valueOf(configFile.getString(key, "SHA256").toUpperCase());
      } catch (IllegalArgumentException var2) {
         ConsoleLogger.showError("Unknown Hash Algorithm; defaulting to SHA256");
         return PasswordSecurity.HashAlgorithm.SHA256;
      }
   }

   private static DataSource.DataSourceType getDataSource() {
      String key = "DataSource.backend";

      try {
         return DataSource.DataSourceType.valueOf(configFile.getString(key).toUpperCase());
      } catch (IllegalArgumentException var2) {
         ConsoleLogger.showError("Unknown database backend; defaulting to file database");
         return DataSource.DataSourceType.FILE;
      }
   }

   public static Boolean getRestrictedIp(String name, String ip) {
      Iterator<String> iter = getRestrictedIp.iterator();
      Boolean trueonce = false;
      Boolean namefound = false;

      while(iter.hasNext()) {
         String[] args = ((String)iter.next()).split(";");
         String testname = args[0];
         String testip = args[1];
         if (testname.equalsIgnoreCase(name)) {
            namefound = true;
            if (testip.equalsIgnoreCase(ip)) {
               trueonce = true;
            }
         }
      }

      if (!namefound) {
         return true;
      } else if (trueonce) {
         return true;
      } else {
         return false;
      }
   }

   public final boolean load() {
      try {
         this.load(this.file);
         return true;
      } catch (Exception var2) {
         return false;
      }
   }

   public final void reload() {
      this.load();
      this.loadDefaults(this.file.getName());
   }

   public final boolean save() {
      try {
         this.save(this.file);
         return true;
      } catch (Exception var2) {
         return false;
      }
   }

   public final boolean exists() {
      return this.file.exists();
   }

   public final void loadDefaults(String filename) {
      InputStream stream = this.plugin.getResource(filename);
      if (stream != null) {
         this.setDefaults(YamlConfiguration.loadConfiguration(stream));
      }
   }

   public final boolean saveDefaults() {
      this.options().copyDefaults(true);
      this.options().copyHeader(true);
      boolean success = this.save();
      this.options().copyDefaults(false);
      this.options().copyHeader(false);
      return success;
   }

   public final void clearDefaults() {
      this.setDefaults(new MemoryConfiguration());
   }

   public boolean checkDefaults() {
      return this.getDefaults() == null ? true : this.getKeys(true).containsAll(this.getDefaults().getKeys(true));
   }

   public static String checkLang(String lang) {
      messagesLang[] var4;
      for(messagesLang language : var4 = Settings.messagesLang.values()) {
         if (lang.toLowerCase().contains(language.toString())) {
            ConsoleLogger.info("Set Language: " + lang);
            return lang;
         }
      }

      ConsoleLogger.info("Set Default Language: En ");
      return "en";
   }

   public static enum messagesLang {
      en,
      de,
      br,
      cz,
      pl,
      fr,
      ru,
      hu,
      sk,
      es,
      zhtw,
      fi,
      zhcn,
      lt,
      it,
      ko,
      pt;

      private messagesLang() {
      }
   }
}
