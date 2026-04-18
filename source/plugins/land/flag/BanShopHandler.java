package flag;

import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.maxgamer.QuickShop.Shop.ShopCreateEvent;

public class BanShopHandler implements Listener {
   private static final String FLAG_BAN_SHOP = "banShop";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public BanShopHandler(LandManager landManager) {
      super();
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(landManager.getLandMain().getPn()));
      landManager.registerEvents(this);
      landManager.register("banShop", this.tip, this.use, true, false, this.per);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onShopCreate(ShopCreateEvent e) {
      Land land = this.landManager.getHighestPriorityLand(e.getShop().getLocation());
      if (land != null && land.hasFlag("banShop")) {
         if (land.hasPer("banShop", e.getPlayer().getName())) {
            return;
         }

         e.setCancelled(true);
         e.getPlayer().sendMessage(UtilFormat.format(this.pn, "tip16", new Object[]{"banShop"}));
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banShop.use");
      this.per = config.getString("banShop.per");
      this.tip = config.getString("banShop.tip");
   }
}
