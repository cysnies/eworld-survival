package flag;

import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import org.bukkit.Bukkit;
import org.bukkit.block.Dispenser;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;

public class DisHandler implements Listener {
   private static final String FLAG_DIS = "dis";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public DisHandler(LandManager landManager) {
      super();
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("dis", this.tip, this.use, false, false, this.per);
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
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockDispense(BlockDispenseEvent e) {
      Land land = this.landManager.getHighestPriorityLand(e.getBlock().getLocation());
      if (land != null && land.hasFlag("dis")) {
         try {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.landManager.getLandMain(), new Recover((Dispenser)e.getBlock().getState(), e.getItem()));
         } catch (Exception var4) {
         }
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("dis.use");
      this.per = config.getString("dis.per");
      this.tip = Util.convert(config.getString("dis.tip"));
   }

   private class Recover implements Runnable {
      private Dispenser dis;
      private ItemStack is;

      public Recover(Dispenser dis, ItemStack is) {
         super();
         this.dis = dis;
         this.is = is;
      }

      public void run() {
         try {
            this.dis.getInventory().addItem(new ItemStack[]{this.is});
         } catch (Exception var2) {
         }

      }
   }
}
