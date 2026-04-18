package net.milkbowl.vault.economy.plugins;

import java.util.List;
import java.util.logging.Logger;
import me.coniin.plugins.minefaconomy.Minefaconomy;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class Economy_Minefaconomy implements Economy {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "Minefaconomy";
   private Plugin plugin = null;
   private Minefaconomy economy = null;

   public Economy_Minefaconomy(Plugin plugin) {
      super();
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);
      Plugin econ = null;
      if (this.economy == null) {
         econ = plugin.getServer().getPluginManager().getPlugin("Minefaconomy");
         log.info("Loading Minefaconomy");
      }

      if (econ != null && econ.isEnabled()) {
         this.economy = (Minefaconomy)econ;
         Logger var10000 = log;
         Object[] var10002 = new Object[]{plugin.getDescription().getName(), null};
         this.getClass();
         var10002[1] = "Minefaconomy";
         var10000.info(String.format("[%s][Economy] %s hooked.", var10002));
      } else {
         log.info("Error Loading Minefaconomy");
      }
   }

   public boolean isEnabled() {
      return this.economy != null && this.economy.isEnabled();
   }

   public String getName() {
      return "Minefaconomy";
   }

   public int fractionalDigits() {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.fractionalDigits();
   }

   public String format(double amount) {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.format(amount);
   }

   public String currencyNamePlural() {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.currencyNamePlural();
   }

   public String currencyNameSingular() {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.currencyNameSingular();
   }

   public boolean hasAccount(String playerName) {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.hasAccount(playerName);
   }

   public boolean hasAccount(String playerName, String worldName) {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.hasAccount(playerName);
   }

   public double getBalance(String playerName) {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.getBalance(playerName);
   }

   public double getBalance(String playerName, String world) {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.getBalance(playerName);
   }

   public boolean has(String playerName, double amount) {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.has(playerName, amount);
   }

   public boolean has(String playerName, String worldName, double amount) {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.has(playerName, amount);
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.withdrawPlayer(playerName, amount);
   }

   public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.withdrawPlayer(playerName, amount);
   }

   public EconomyResponse depositPlayer(String playerName, double amount) {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.depositPlayer(playerName, amount);
   }

   public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.depositPlayer(playerName, amount);
   }

   public boolean createPlayerAccount(String playerName) {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.createPlayerAccount(playerName);
   }

   public boolean createPlayerAccount(String playerName, String worldName) {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.createPlayerAccount(playerName);
   }

   public boolean hasBankSupport() {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.hasBankSupport();
   }

   public EconomyResponse createBank(String name, String player) {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.createBank(name, player);
   }

   public EconomyResponse deleteBank(String name) {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.deleteBank(name);
   }

   public EconomyResponse bankBalance(String name) {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.bankBalance(name);
   }

   public EconomyResponse bankHas(String name, double amount) {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.bankHas(name, amount);
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.bankWithdraw(name, amount);
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.bankDeposit(name, amount);
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.isBankOwner(name, playerName);
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.isBankMember(name, playerName);
   }

   public List getBanks() {
      Minefaconomy var10000 = this.economy;
      return Minefaconomy.vaultLayer.getBanks();
   }

   public class EconomyServerListener implements Listener {
      Economy_Minefaconomy economy_minefaconomy = null;

      public EconomyServerListener(Economy_Minefaconomy economy_minefaconomy) {
         super();
         this.economy_minefaconomy = economy_minefaconomy;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.economy_minefaconomy.economy == null) {
            Plugin mfc = event.getPlugin();
            if (mfc.getDescription().getName().equals("Minefaconomy")) {
               this.economy_minefaconomy.economy = Economy_Minefaconomy.this.economy;
               Economy_Minefaconomy.log.info(String.format("[%s][Economy] %s hooked.", Economy_Minefaconomy.this.plugin.getDescription().getName(), "Minefaconomy"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.economy_minefaconomy.economy != null && event.getPlugin().getDescription().getName().equals("Minefaconomy")) {
            this.economy_minefaconomy.economy = null;
            Economy_Minefaconomy.log.info(String.format("[%s][Economy] %s unhooked.", Economy_Minefaconomy.this.plugin.getDescription().getName(), "Minefaconomy"));
         }

      }
   }
}
