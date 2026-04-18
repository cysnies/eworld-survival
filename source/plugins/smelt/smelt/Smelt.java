package smelt;

import java.util.HashMap;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilSpeed;
import org.bukkit.Server;
import org.bukkit.block.Furnace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Smelt implements Listener {
   private static final String REPAIR = "repair";
   private Main main;
   private Server server;
   private String pn;
   private int repairCost;
   private int repairInterval;
   private HashMap smeltHash;

   public Smelt(Main main) {
      super();
      this.main = main;
      this.server = main.getServer();
      this.pn = main.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, main);
      UtilSpeed.register(this.pn, "repair");
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onFurnaceBurn(FurnaceBurnEvent e) {
      if (this.isSmelt((Furnace)e.getBlock().getState())) {
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onFurnaceSmelt(FurnaceSmeltEvent e) {
      if (this.isEmpty((Furnace)e.getBlock().getState())) {
         String type = (String)this.smeltHash.get(e.getSource().getTypeId());
         if (type != null) {
            ItemStack result = UtilItems.getItem(this.pn, type);
            if (result != null && result.getTypeId() != 0) {
               e.setResult(result);
            }
         }
      } else if (this.isSmelt((Furnace)e.getBlock().getState())) {
         e.setCancelled(true);
      }

   }

   private boolean isSmelt(Furnace furnace) {
      ItemStack is = furnace.getInventory().getResult();
      if (is != null && is.getTypeId() != 0) {
         ItemMeta im = is.getItemMeta();
         if (im != null && im.getDisplayName() != null && !im.getDisplayName().isEmpty()) {
            return true;
         }
      }

      return false;
   }

   private boolean isEmpty(Furnace furnace) {
      ItemStack is = furnace.getInventory().getResult();
      return is == null || is.getTypeId() == 0;
   }

   private void loadConfig(FileConfiguration config) {
      this.repairCost = config.getInt("repairCost");
      this.repairInterval = config.getInt("repairInterval");
      this.smeltHash = new HashMap();

      for(String s : config.getStringList("smelt")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         String type = s.split(" ")[1];
         this.smeltHash.put(id, type);
      }

   }

   public int getRepairCost() {
      return this.repairCost;
   }

   public void repair(Player p) {
      if (UtilSpeed.check(p, this.pn, "repair", this.repairInterval)) {
         ItemStack is = p.getItemInHand();
         if (is != null && is.getTypeId() != 0) {
            if (is.hasItemMeta() && is.getItemMeta().hasLore()) {
               if (UtilEco.get(p.getName()) < (double)this.repairCost) {
                  p.sendMessage(UtilFormat.format(this.pn, "tip2", new Object[]{this.repairCost}));
               } else {
                  UtilEco.del(p.getName(), (double)this.repairCost);
                  p.sendMessage(UtilFormat.format(this.pn, "tip3", new Object[]{this.repairCost}));
                  is = this.main.getGem().repair(is);
                  p.setItemInHand(is);
                  p.updateInventory();
                  p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(615)}));
               }
            } else {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(610)}));
            }
         } else {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(605)}));
         }
      }
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
