package com.fernferret.allpay.multiverse;

import ca.agnate.EconXP.EconXP;
import com.iCo6.iConomy;
import cosine.boseconomy.BOSEconomy;
import fr.crafter.tickleman.RealEconomy.RealEconomy;
import fr.crafter.tickleman.RealPlugin.RealPlugin;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;

public class AllPay {
   private static double version;
   private Properties props = new Properties();
   protected String logPrefix = "";
   protected static final Logger LOGGER = Logger.getLogger("Minecraft");
   private String prefix;
   private Plugin plugin;
   private GenericBank bank;
   private static final String[] VALID_ECON_PLUGINS = new String[]{"Essentials", "RealShop", "BOSEconomy", "iConomy", "MultiCurrency", "EconXP"};

   public AllPay(Plugin plugin, String prefix) {
      super();

      try {
         this.props.load(this.getClass().getResourceAsStream("/allpay.properties"));
         version = (double)Integer.parseInt(this.props.getProperty("version", "-1"));
      } catch (NumberFormatException var4) {
         this.logBadAllPay(plugin);
      } catch (FileNotFoundException var5) {
         this.logBadAllPay(plugin);
      } catch (IOException var6) {
         this.logBadAllPay(plugin);
      }

      this.logPrefix = "[AllPay] - Version " + version;
      this.plugin = plugin;
      this.prefix = prefix;
   }

   public static String[] getValidEconPlugins() {
      return VALID_ECON_PLUGINS;
   }

   private void logBadAllPay(Plugin plugin) {
      plugin.getLogger().log(Level.SEVERE, String.format("AllPay looks corrupted, meaning this plugin (%s) is corrupted too!", plugin.getDescription().getName()));
   }

   public GenericBank loadEconPlugin() {
      this.loadiConomy();
      this.loadBOSEconomy();
      this.loadRealShopEconomy();
      this.loadEssentialsEconomoy();
      this.loadEconXPEconomy();
      this.loadDefaultItemEconomy();
      this.bank.setPrefix(this.prefix);
      return this.bank;
   }

   public GenericBank getEconPlugin() {
      return this.bank;
   }

   public double getVersion() {
      return version;
   }

   private void loadEssentialsEconomoy() {
      if (this.bank == null) {
         try {
            Plugin essentialsPlugin = this.plugin.getServer().getPluginManager().getPlugin("Essentials");
            if (essentialsPlugin != null) {
               this.bank = new EssentialsBank();
               LOGGER.info(this.logPrefix + " - hooked into Essentials Economy for " + this.plugin.getDescription().getFullName());
            }
         } catch (Exception var2) {
            LOGGER.warning(this.logPrefix + "You are using a VERY old version of Essentials. Please upgrade it.");
         }
      }

   }

   private void loadRealShopEconomy() {
      if (this.bank == null && !(this.bank instanceof EssentialsBank)) {
         Plugin realShopPlugin = this.plugin.getServer().getPluginManager().getPlugin("RealShop");
         if (realShopPlugin != null) {
            RealEconomy realEconPlugin = new RealEconomy((RealPlugin)realShopPlugin);
            LOGGER.info(this.logPrefix + " - hooked into RealEconomy for " + this.plugin.getDescription().getFullName());
            this.bank = new RealEconomyBank(realEconPlugin);
         }
      }

   }

   private void loadBOSEconomy() {
      if (this.bank == null && !(this.bank instanceof EssentialsBank)) {
         Plugin boseconPlugin = this.plugin.getServer().getPluginManager().getPlugin("BOSEconomy");
         if (boseconPlugin != null) {
            this.bank = new BOSEconomyBank((BOSEconomy)boseconPlugin);
            LOGGER.info(this.logPrefix + " - hooked into BOSEconomy for " + this.plugin.getDescription().getFullName());
         }
      }

   }

   private void loadEconXPEconomy() {
      if (this.bank == null && !(this.bank instanceof EssentialsBank)) {
         Plugin econXPPlugin = this.plugin.getServer().getPluginManager().getPlugin("EconXP");
         if (econXPPlugin != null) {
            this.bank = new EconXPBank((EconXP)econXPPlugin);
            LOGGER.info(this.logPrefix + " - hooked into EconXP for " + this.plugin.getDescription().getFullName());
         }
      }

   }

   private void loadDefaultItemEconomy() {
      if (this.bank == null) {
         this.bank = new ItemBank();
         LOGGER.info(this.logPrefix + " - using only an item based economy for " + this.plugin.getDescription().getFullName());
      }

   }

   private void loadiConomy() {
      if (this.bank == null && !(this.bank instanceof EssentialsBank)) {
         Plugin iConomyTest = this.plugin.getServer().getPluginManager().getPlugin("iConomy");

         try {
            if (iConomyTest != null && iConomyTest instanceof iConomy) {
               this.bank = new iConomyBank6X();
               LOGGER.info(this.logPrefix + " - hooked into iConomy 6 for " + this.plugin.getDescription().getFullName());
            }
         } catch (NoClassDefFoundError var3) {
            this.loadiConomy5X(iConomyTest);
         }
      }

   }

   private void loadiConomy5X(Plugin iConomyTest) {
      try {
         if (iConomyTest != null && iConomyTest instanceof com.iConomy.iConomy) {
            this.bank = new iConomyBank5X();
            LOGGER.info(this.logPrefix + " - hooked into iConomy 5 for " + this.plugin.getDescription().getFullName());
         }
      } catch (NoClassDefFoundError var3) {
         if (iConomyTest != null) {
            this.loadiConomy4X();
         }
      }

   }

   private void loadiConomy4X() {
      com.nijiko.coelho.iConomy.iConomy iConomyPlugin = (com.nijiko.coelho.iConomy.iConomy)this.plugin.getServer().getPluginManager().getPlugin("iConomy");
      if (iConomyPlugin != null) {
         this.bank = new iConomyBank4X();
         LOGGER.info(this.logPrefix + " - hooked into iConomy 4 for " + this.plugin.getDescription().getFullName());
      }

   }
}
