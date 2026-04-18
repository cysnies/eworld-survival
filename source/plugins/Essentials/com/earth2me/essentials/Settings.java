package com.earth2me.essentials;

import com.earth2me.essentials.commands.IEssentialsCommand;
import com.earth2me.essentials.signs.EssentialsSign;
import com.earth2me.essentials.signs.Signs;
import com.earth2me.essentials.textreader.IText;
import com.earth2me.essentials.textreader.SimpleTextInput;
import com.earth2me.essentials.utils.FormatUtil;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

public class Settings implements net.ess3.api.ISettings {
   private final transient EssentialsConf config;
   private static final Logger logger = Logger.getLogger("Minecraft");
   private final transient net.ess3.api.IEssentials ess;
   private boolean metricsEnabled = true;
   private int chatRadius = 0;
   private Set disabledCommands = new HashSet();
   private ConfigurationSection commandCosts;
   private Set socialSpyCommands = new HashSet();
   private String nicknamePrefix = "~";
   private ConfigurationSection kits;
   private ChatColor operatorColor = null;
   private Map chatFormats = Collections.synchronizedMap(new HashMap());
   private List itemSpawnBl = new ArrayList();
   private List enabledSigns = new ArrayList();
   private boolean signsEnabled = false;
   private boolean warnOnBuildDisallow;
   private boolean debug = false;
   private boolean configDebug = false;
   private boolean economyDisabled = false;
   private static final BigDecimal MAXMONEY = new BigDecimal("10000000000000");
   private BigDecimal maxMoney;
   private static final BigDecimal MINMONEY = new BigDecimal("-10000000000000");
   private BigDecimal minMoney;
   private boolean economyLog;
   private boolean economyLogUpdate;
   private boolean changeDisplayName;
   private boolean changePlayerListName;
   private boolean prefixsuffixconfigured;
   private boolean addprefixsuffix;
   private boolean essentialsChatActive;
   private boolean disablePrefix;
   private boolean disableSuffix;
   private boolean getFreezeAfkPlayers;
   private boolean cancelAfkOnMove;
   private boolean cancelAfkOnInteract;
   private Set noGodWorlds;
   private boolean registerBackInListener;
   private boolean disableItemPickupWhileAfk;
   private long teleportInvulnerabilityTime;
   private boolean teleportInvulnerability;
   private long loginAttackDelay;
   private int signUsePerSecond;
   private int mailsPerMinute;
   private long economyLagWarning;

   public Settings(net.ess3.api.IEssentials ess) {
      super();
      this.maxMoney = MAXMONEY;
      this.minMoney = MINMONEY;
      this.economyLog = false;
      this.economyLogUpdate = false;
      this.changeDisplayName = true;
      this.changePlayerListName = false;
      this.prefixsuffixconfigured = false;
      this.addprefixsuffix = false;
      this.essentialsChatActive = false;
      this.disablePrefix = false;
      this.disableSuffix = false;
      this.noGodWorlds = new HashSet();
      this.ess = ess;
      this.config = new EssentialsConf(new File(ess.getDataFolder(), "config.yml"));
      this.config.setTemplateName("/config.yml");
      this.reloadConfig();
   }

   public boolean getRespawnAtHome() {
      return this.config.getBoolean("respawn-at-home", false);
   }

   public boolean getUpdateBedAtDaytime() {
      return this.config.getBoolean("update-bed-at-daytime", true);
   }

   public Set getMultipleHomes() {
      return this.config.getConfigurationSection("sethome-multiple").getKeys(false);
   }

   public int getHomeLimit(User user) {
      int limit = 1;
      if (user.isAuthorized("essentials.sethome.multiple")) {
         limit = this.getHomeLimit("default");
      }

      Set<String> homeList = this.getMultipleHomes();
      if (homeList != null) {
         for(String set : homeList) {
            if (user.isAuthorized("essentials.sethome.multiple." + set) && limit < this.getHomeLimit(set)) {
               limit = this.getHomeLimit(set);
            }
         }
      }

      return limit;
   }

   public int getHomeLimit(String set) {
      return this.config.getInt("sethome-multiple." + set, this.config.getInt("sethome-multiple.default", 3));
   }

   private int _getChatRadius() {
      return this.config.getInt("chat.radius", this.config.getInt("chat-radius", 0));
   }

   public int getChatRadius() {
      return this.chatRadius;
   }

   public double getTeleportDelay() {
      return this.config.getDouble("teleport-delay", (double)0.0F);
   }

   public int getOversizedStackSize() {
      return this.config.getInt("oversized-stacksize", 64);
   }

   public int getDefaultStackSize() {
      return this.config.getInt("default-stack-size", -1);
   }

   public BigDecimal getStartingBalance() {
      return this.config.getBigDecimal("starting-balance", BigDecimal.ZERO);
   }

   public boolean isCommandDisabled(IEssentialsCommand cmd) {
      return this.isCommandDisabled(cmd.getName());
   }

   public boolean isCommandDisabled(String label) {
      return this.disabledCommands.contains(label);
   }

   private Set getDisabledCommands() {
      Set<String> disCommands = new HashSet();

      for(String c : this.config.getStringList("disabled-commands")) {
         disCommands.add(c.toLowerCase(Locale.ENGLISH));
      }

      for(String c : this.config.getKeys(false)) {
         if (c.startsWith("disable-")) {
            disCommands.add(c.substring(8).toLowerCase(Locale.ENGLISH));
         }
      }

      return disCommands;
   }

   public boolean isPlayerCommand(String label) {
      for(String c : this.config.getStringList("player-commands")) {
         if (c.equalsIgnoreCase(label)) {
            return true;
         }
      }

      return false;
   }

   public boolean isCommandOverridden(String name) {
      for(String c : this.config.getStringList("overridden-commands")) {
         if (c.equalsIgnoreCase(name)) {
            return true;
         }
      }

      return this.config.getBoolean("override-" + name.toLowerCase(Locale.ENGLISH), false);
   }

   public BigDecimal getCommandCost(IEssentialsCommand cmd) {
      return this.getCommandCost(cmd.getName());
   }

   private ConfigurationSection _getCommandCosts() {
      if (this.config.isConfigurationSection("command-costs")) {
         ConfigurationSection section = this.config.getConfigurationSection("command-costs");
         ConfigurationSection newSection = new MemoryConfiguration();

         for(String command : section.getKeys(false)) {
            PluginCommand cmd = this.ess.getServer().getPluginCommand(command);
            if (command.charAt(0) == '/') {
               this.ess.getLogger().warning("Invalid command cost. '" + command + "' should not start with '/'.");
            }

            if (section.isDouble(command)) {
               newSection.set(command.toLowerCase(Locale.ENGLISH), section.getDouble(command));
            } else if (section.isInt(command)) {
               newSection.set(command.toLowerCase(Locale.ENGLISH), (double)section.getInt(command));
            } else if (section.isString(command)) {
               String costString = section.getString(command);

               try {
                  double cost = Double.parseDouble(costString.trim().replace(this.getCurrencySymbol(), "").replaceAll("\\W", ""));
                  newSection.set(command.toLowerCase(Locale.ENGLISH), cost);
               } catch (NumberFormatException var9) {
                  this.ess.getLogger().warning("Invalid command cost for: " + command + " (" + costString + ")");
               }
            } else {
               this.ess.getLogger().warning("Invalid command cost for: " + command);
            }
         }

         return newSection;
      } else {
         return null;
      }
   }

   public BigDecimal getCommandCost(String name) {
      name = name.replace('.', '_').replace('/', '_');
      return this.commandCosts != null ? EssentialsConf.toBigDecimal(this.commandCosts.getString(name), BigDecimal.ZERO) : BigDecimal.ZERO;
   }

   private Set _getSocialSpyCommands() {
      Set<String> socialspyCommands = new HashSet();
      if (this.config.isList("socialspy-commands")) {
         for(String c : this.config.getStringList("socialspy-commands")) {
            socialspyCommands.add(c.toLowerCase(Locale.ENGLISH));
         }
      } else {
         socialspyCommands.addAll(Arrays.asList("msg", "r", "mail", "m", "whisper", "emsg", "t", "tell", "er", "reply", "ereply", "email", "action", "describe", "eme", "eaction", "edescribe", "etell", "ewhisper", "pm"));
      }

      return socialspyCommands;
   }

   public Set getSocialSpyCommands() {
      return this.socialSpyCommands;
   }

   private String _getNicknamePrefix() {
      return this.config.getString("nickname-prefix", "~");
   }

   public String getNicknamePrefix() {
      return this.nicknamePrefix;
   }

   public double getTeleportCooldown() {
      return this.config.getDouble("teleport-cooldown", (double)0.0F);
   }

   public double getHealCooldown() {
      return this.config.getDouble("heal-cooldown", (double)0.0F);
   }

   private ConfigurationSection _getKits() {
      if (this.config.isConfigurationSection("kits")) {
         ConfigurationSection section = this.config.getConfigurationSection("kits");
         ConfigurationSection newSection = new MemoryConfiguration();

         for(String kitItem : section.getKeys(false)) {
            if (section.isConfigurationSection(kitItem)) {
               newSection.set(kitItem.toLowerCase(Locale.ENGLISH), section.getConfigurationSection(kitItem));
            }
         }

         return newSection;
      } else {
         return null;
      }
   }

   public ConfigurationSection getKits() {
      return this.kits;
   }

   public Map getKit(String name) {
      name = name.replace('.', '_').replace('/', '_');
      if (this.getKits() != null) {
         ConfigurationSection kits = this.getKits();
         if (kits.isConfigurationSection(name)) {
            return kits.getConfigurationSection(name).getValues(true);
         }
      }

      return null;
   }

   public ChatColor getOperatorColor() {
      return this.operatorColor;
   }

   private ChatColor _getOperatorColor() {
      String colorName = this.config.getString("ops-name-color", (String)null);
      if (colorName == null) {
         return ChatColor.DARK_RED;
      } else if (!"none".equalsIgnoreCase(colorName) && !colorName.isEmpty()) {
         try {
            return ChatColor.valueOf(colorName.toUpperCase(Locale.ENGLISH));
         } catch (IllegalArgumentException var3) {
            return ChatColor.getByChar(colorName);
         }
      } else {
         return null;
      }
   }

   public int getSpawnMobLimit() {
      return this.config.getInt("spawnmob-limit", 10);
   }

   public boolean showNonEssCommandsInHelp() {
      return this.config.getBoolean("non-ess-in-help", true);
   }

   public boolean hidePermissionlessHelp() {
      return this.config.getBoolean("hide-permissionless-help", true);
   }

   public int getProtectCreeperMaxHeight() {
      return this.config.getInt("protect.creeper.max-height", -1);
   }

   public boolean areSignsDisabled() {
      return !this.signsEnabled;
   }

   public long getBackupInterval() {
      return (long)this.config.getInt("backup.interval", 1440);
   }

   public String getBackupCommand() {
      return this.config.getString("backup.command", (String)null);
   }

   public String getChatFormat(String group) {
      String mFormat = (String)this.chatFormats.get(group);
      if (mFormat == null) {
         String var3 = this.config.getString("chat.group-formats." + (group == null ? "Default" : group), this.config.getString("chat.format", "&7[{GROUP}]&r {DISPLAYNAME}&7:&r {MESSAGE}"));
         String var4 = FormatUtil.replaceFormat(var3);
         String var5 = var4.replace("{DISPLAYNAME}", "%1$s");
         String var6 = var5.replace("{MESSAGE}", "%2$s");
         String var7 = var6.replace("{GROUP}", "{0}");
         String var8 = var7.replace("{WORLDNAME}", "{1}");
         String var9 = var8.replace("{SHORTWORLDNAME}", "{2}");
         String var10 = var9.replace("{TEAMPREFIX}", "{3}");
         String var11 = var10.replace("{TEAMSUFFIX}", "{4}");
         String var12 = var11.replace("{TEAMNAME}", "{5}");
         mFormat = "§r".concat(var12);
         this.chatFormats.put(group, mFormat);
      }

      return mFormat;
   }

   public boolean getAnnounceNewPlayers() {
      return !this.config.getString("newbies.announce-format", "-").isEmpty();
   }

   public IText getAnnounceNewPlayerFormat() {
      return new SimpleTextInput(FormatUtil.replaceFormat(this.config.getString("newbies.announce-format", "&dWelcome {DISPLAYNAME} to the server!")));
   }

   public String getNewPlayerKit() {
      return this.config.getString("newbies.kit", "");
   }

   public String getNewbieSpawn() {
      return this.config.getString("newbies.spawnpoint", "default");
   }

   public boolean getPerWarpPermission() {
      return this.config.getBoolean("per-warp-permission", false);
   }

   public Map getListGroupConfig() {
      if (this.config.isConfigurationSection("list")) {
         Map<String, Object> values = this.config.getConfigurationSection("list").getValues(false);
         if (!values.isEmpty()) {
            return values;
         }
      }

      Map<String, Object> defaultMap = new HashMap();
      if (this.config.getBoolean("sort-list-by-groups", false)) {
         defaultMap.put("ListByGroup", "ListByGroup");
      } else {
         defaultMap.put("Players", "*");
      }

      return defaultMap;
   }

   public void reloadConfig() {
      this.config.load();
      this.noGodWorlds = new HashSet(this.config.getStringList("no-god-in-worlds"));
      this.enabledSigns = this._getEnabledSigns();
      this.teleportInvulnerabilityTime = this._getTeleportInvulnerability();
      this.teleportInvulnerability = this._isTeleportInvulnerability();
      this.disableItemPickupWhileAfk = this._getDisableItemPickupWhileAfk();
      this.registerBackInListener = this._registerBackInListener();
      this.cancelAfkOnInteract = this._cancelAfkOnInteract();
      this.cancelAfkOnMove = this._cancelAfkOnMove() && this.cancelAfkOnInteract;
      this.getFreezeAfkPlayers = this._getFreezeAfkPlayers();
      this.itemSpawnBl = this._getItemSpawnBlacklist();
      this.loginAttackDelay = this._getLoginAttackDelay();
      this.signUsePerSecond = this._getSignUsePerSecond();
      this.kits = this._getKits();
      this.chatFormats.clear();
      this.changeDisplayName = this._changeDisplayName();
      this.disabledCommands = this.getDisabledCommands();
      this.nicknamePrefix = this._getNicknamePrefix();
      this.operatorColor = this._getOperatorColor();
      this.changePlayerListName = this._changePlayerListName();
      this.configDebug = this._isDebug();
      this.prefixsuffixconfigured = this._isPrefixSuffixConfigured();
      this.addprefixsuffix = this._addPrefixSuffix();
      this.disablePrefix = this._disablePrefix();
      this.disableSuffix = this._disableSuffix();
      this.chatRadius = this._getChatRadius();
      this.commandCosts = this._getCommandCosts();
      this.socialSpyCommands = this._getSocialSpyCommands();
      this.warnOnBuildDisallow = this._warnOnBuildDisallow();
      this.mailsPerMinute = this._getMailsPerMinute();
      this.maxMoney = this._getMaxMoney();
      this.minMoney = this._getMinMoney();
      this.economyLagWarning = this._getEconomyLagWarning();
      this.economyLog = this._isEcoLogEnabled();
      this.economyLogUpdate = this._isEcoLogUpdateEnabled();
      this.economyDisabled = this._isEcoDisabled();
   }

   public List itemSpawnBlacklist() {
      return this.itemSpawnBl;
   }

   private List _getItemSpawnBlacklist() {
      List<Integer> epItemSpwn = new ArrayList();
      if (this.ess.getItemDb() == null) {
         logger.log(Level.FINE, "Aborting ItemSpawnBL read, itemDB not yet loaded.");
         return epItemSpwn;
      } else {
         for(String itemName : this.config.getString("item-spawn-blacklist", "").split(",")) {
            itemName = itemName.trim();
            if (!itemName.isEmpty()) {
               try {
                  ItemStack iStack = this.ess.getItemDb().get(itemName);
                  epItemSpwn.add(iStack.getTypeId());
               } catch (Exception var7) {
                  logger.log(Level.SEVERE, I18n._("unknownItemInList", itemName, "item-spawn-blacklist"));
               }
            }
         }

         return epItemSpwn;
      }
   }

   public List enabledSigns() {
      return this.enabledSigns;
   }

   private List _getEnabledSigns() {
      List<EssentialsSign> newSigns = new ArrayList();

      for(String signName : this.config.getStringList("enabledSigns")) {
         signName = signName.trim().toUpperCase(Locale.ENGLISH);
         if (!signName.isEmpty()) {
            if (!signName.equals("COLOR") && !signName.equals("COLOUR")) {
               try {
                  newSigns.add(Signs.valueOf(signName).getSign());
               } catch (Exception var5) {
                  logger.log(Level.SEVERE, I18n._("unknownItemInList", signName, "enabledSigns"));
                  continue;
               }

               this.signsEnabled = true;
            } else {
               this.signsEnabled = true;
            }
         }
      }

      return newSigns;
   }

   private boolean _warnOnBuildDisallow() {
      return this.config.getBoolean("protect.disable.warn-on-build-disallow", false);
   }

   public boolean warnOnBuildDisallow() {
      return this.warnOnBuildDisallow;
   }

   private boolean _isDebug() {
      return this.config.getBoolean("debug", false);
   }

   public boolean isDebug() {
      return this.debug || this.configDebug;
   }

   public boolean warnOnSmite() {
      return this.config.getBoolean("warn-on-smite", true);
   }

   public boolean permissionBasedItemSpawn() {
      return this.config.getBoolean("permission-based-item-spawn", false);
   }

   public String getLocale() {
      return this.config.getString("locale", "");
   }

   public String getCurrencySymbol() {
      return this.config.getString("currency-symbol", "$").concat("$").substring(0, 1).replaceAll("[0-9]", "$");
   }

   public boolean isTradeInStacks(int id) {
      return this.config.getBoolean("trade-in-stacks-" + id, false);
   }

   public boolean _isEcoDisabled() {
      return this.config.getBoolean("disable-eco", false);
   }

   public boolean isEcoDisabled() {
      return this.economyDisabled;
   }

   public boolean getProtectPreventSpawn(String creatureName) {
      return this.config.getBoolean("protect.prevent.spawn." + creatureName, false);
   }

   public List getProtectList(String configName) {
      List<Integer> list = new ArrayList();

      for(String itemName : this.config.getString(configName, "").split(",")) {
         itemName = itemName.trim();
         if (!itemName.isEmpty()) {
            try {
               ItemStack itemStack = this.ess.getItemDb().get(itemName);
               list.add(itemStack.getTypeId());
            } catch (Exception var9) {
               logger.log(Level.SEVERE, I18n._("unknownItemInList", itemName, configName));
            }
         }
      }

      return list;
   }

   public String getProtectString(String configName) {
      return this.config.getString(configName, (String)null);
   }

   public boolean getProtectBoolean(String configName, boolean def) {
      return this.config.getBoolean(configName, def);
   }

   private BigDecimal _getMaxMoney() {
      return this.config.getBigDecimal("max-money", MAXMONEY);
   }

   public BigDecimal getMaxMoney() {
      return this.maxMoney;
   }

   private BigDecimal _getMinMoney() {
      BigDecimal min = this.config.getBigDecimal("min-money", MINMONEY);
      if (min.signum() > 0) {
         min = min.negate();
      }

      return min;
   }

   public BigDecimal getMinMoney() {
      return this.minMoney;
   }

   public boolean isEcoLogEnabled() {
      return this.economyLog;
   }

   public boolean _isEcoLogEnabled() {
      return this.config.getBoolean("economy-log-enabled", false);
   }

   public boolean isEcoLogUpdateEnabled() {
      return this.economyLogUpdate;
   }

   public boolean _isEcoLogUpdateEnabled() {
      return this.config.getBoolean("economy-log-update-enabled", false);
   }

   public boolean removeGodOnDisconnect() {
      return this.config.getBoolean("remove-god-on-disconnect", false);
   }

   private boolean _changeDisplayName() {
      return this.config.getBoolean("change-displayname", true);
   }

   public boolean changeDisplayName() {
      return this.changeDisplayName;
   }

   private boolean _changePlayerListName() {
      return this.config.getBoolean("change-playerlist", false);
   }

   public boolean changePlayerListName() {
      return this.changePlayerListName;
   }

   public boolean useBukkitPermissions() {
      return this.config.getBoolean("use-bukkit-permissions", false);
   }

   private boolean _addPrefixSuffix() {
      return this.config.getBoolean("add-prefix-suffix", false);
   }

   private boolean _isPrefixSuffixConfigured() {
      return this.config.hasProperty("add-prefix-suffix");
   }

   public void setEssentialsChatActive(boolean essentialsChatActive) {
      this.essentialsChatActive = essentialsChatActive;
   }

   public boolean addPrefixSuffix() {
      return this.prefixsuffixconfigured ? this.addprefixsuffix : this.essentialsChatActive;
   }

   private boolean _disablePrefix() {
      return this.config.getBoolean("disablePrefix", false);
   }

   public boolean disablePrefix() {
      return this.disablePrefix;
   }

   private boolean _disableSuffix() {
      return this.config.getBoolean("disableSuffix", false);
   }

   public boolean disableSuffix() {
      return this.disableSuffix;
   }

   public long getAutoAfk() {
      return this.config.getLong("auto-afk", 300L);
   }

   public long getAutoAfkKick() {
      return this.config.getLong("auto-afk-kick", -1L);
   }

   public boolean getFreezeAfkPlayers() {
      return this.getFreezeAfkPlayers;
   }

   private boolean _getFreezeAfkPlayers() {
      return this.config.getBoolean("freeze-afk-players", false);
   }

   public boolean cancelAfkOnMove() {
      return this.cancelAfkOnMove;
   }

   private boolean _cancelAfkOnMove() {
      return this.config.getBoolean("cancel-afk-on-move", true);
   }

   public boolean cancelAfkOnInteract() {
      return this.cancelAfkOnInteract;
   }

   private boolean _cancelAfkOnInteract() {
      return this.config.getBoolean("cancel-afk-on-interact", true);
   }

   public boolean areDeathMessagesEnabled() {
      return this.config.getBoolean("death-messages", true);
   }

   public Set getNoGodWorlds() {
      return this.noGodWorlds;
   }

   public void setDebug(boolean debug) {
      this.debug = debug;
   }

   public boolean getRepairEnchanted() {
      return this.config.getBoolean("repair-enchanted", true);
   }

   public boolean allowUnsafeEnchantments() {
      return this.config.getBoolean("unsafe-enchantments", false);
   }

   public boolean isWorldTeleportPermissions() {
      return this.config.getBoolean("world-teleport-permissions", false);
   }

   public boolean isWorldHomePermissions() {
      return this.config.getBoolean("world-home-permissions", false);
   }

   public boolean registerBackInListener() {
      return this.registerBackInListener;
   }

   private boolean _registerBackInListener() {
      return this.config.getBoolean("register-back-in-listener", false);
   }

   public boolean getDisableItemPickupWhileAfk() {
      return this.disableItemPickupWhileAfk;
   }

   private boolean _getDisableItemPickupWhileAfk() {
      return this.config.getBoolean("disable-item-pickup-while-afk", false);
   }

   public EventPriority getRespawnPriority() {
      String priority = this.config.getString("respawn-listener-priority", "normal").toLowerCase(Locale.ENGLISH);
      if ("lowest".equals(priority)) {
         return EventPriority.LOWEST;
      } else if ("low".equals(priority)) {
         return EventPriority.LOW;
      } else if ("normal".equals(priority)) {
         return EventPriority.NORMAL;
      } else if ("high".equals(priority)) {
         return EventPriority.HIGH;
      } else {
         return "highest".equals(priority) ? EventPriority.HIGHEST : EventPriority.NORMAL;
      }
   }

   public long getTpaAcceptCancellation() {
      return this.config.getLong("tpa-accept-cancellation", 120L);
   }

   public boolean isMetricsEnabled() {
      return this.metricsEnabled;
   }

   public void setMetricsEnabled(boolean metricsEnabled) {
      this.metricsEnabled = metricsEnabled;
   }

   private long _getTeleportInvulnerability() {
      return this.config.getLong("teleport-invulnerability", 0L) * 1000L;
   }

   public long getTeleportInvulnerability() {
      return this.teleportInvulnerabilityTime;
   }

   private boolean _isTeleportInvulnerability() {
      return this.config.getLong("teleport-invulnerability", 0L) > 0L;
   }

   public boolean isTeleportInvulnerability() {
      return this.teleportInvulnerability;
   }

   private long _getLoginAttackDelay() {
      return this.config.getLong("login-attack-delay", 0L) * 1000L;
   }

   public long getLoginAttackDelay() {
      return this.loginAttackDelay;
   }

   private int _getSignUsePerSecond() {
      int perSec = this.config.getInt("sign-use-per-second", 4);
      return perSec > 0 ? perSec : 1;
   }

   public int getSignUsePerSecond() {
      return this.signUsePerSecond;
   }

   public double getMaxFlySpeed() {
      double maxSpeed = this.config.getDouble("max-fly-speed", 0.8);
      return maxSpeed > (double)1.0F ? (double)1.0F : Math.abs(maxSpeed);
   }

   public double getMaxWalkSpeed() {
      double maxSpeed = this.config.getDouble("max-walk-speed", 0.8);
      return maxSpeed > (double)1.0F ? (double)1.0F : Math.abs(maxSpeed);
   }

   private int _getMailsPerMinute() {
      return this.config.getInt("mails-per-minute", 1000);
   }

   public int getMailsPerMinute() {
      return this.mailsPerMinute;
   }

   private long _getEconomyLagWarning() {
      long value = (long)(this.config.getDouble("economy-lag-warning", (double)20.0F) * (double)1000000.0F);
      return value;
   }

   public long getEconomyLagWarning() {
      return this.economyLagWarning;
   }

   public long getMaxTempban() {
      return this.config.getLong("max-tempban-time", -1L);
   }

   public int getMaxNickLength() {
      return this.config.getInt("max-nick-length", 30);
   }

   public int getMaxUserCacheCount() {
      long count = Runtime.getRuntime().maxMemory() / 1024L / 96L;
      return this.config.getInt("max-user-cache-count", (int)count);
   }
}
