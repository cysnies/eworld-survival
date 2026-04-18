package smelt;

import java.util.HashMap;
import java.util.List;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Luck implements Listener {
   private Server server;
   private String pn;
   private String check;
   private HashMap luckHash;

   public Luck(Main main) {
      super();
      this.server = main.getServer();
      this.pn = main.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, main);
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
   public void onPlayerInteract(PlayerInteractEvent e) {
      if (!Main.isIgnored(e.getClickedBlock())) {
         if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && this.isLuck(e.getPlayer().getItemInHand())) {
            e.setCancelled(true);
            if (UtilItems.getEmptySlots(e.getPlayer().getInventory()) <= 0) {
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(60)}));
               return;
            }

            int id = e.getPlayer().getItemInHand().getTypeId();
            String type = (String)this.luckHash.get(id);
            if (type == null) {
               return;
            }

            ItemStack result = UtilItems.getItem(this.pn, type);
            if (e.getPlayer().getItemInHand().getAmount() <= 1) {
               e.getPlayer().setItemInHand((ItemStack)null);
            } else {
               e.getPlayer().getItemInHand().setAmount(e.getPlayer().getItemInHand().getAmount() - 1);
            }

            e.getPlayer().getInventory().addItem(new ItemStack[]{result});
            e.getPlayer().updateInventory();
            e.getPlayer().sendMessage(UtilFormat.format(this.pn, "luckGet", new Object[]{result.getItemMeta().getDisplayName()}));
            e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.valueOf("SUCCESSFUL_HIT"), 2.0F, 1.0F);
         }

      }
   }

   private boolean isLuck(ItemStack is) {
      if (is != null && is.getTypeId() != 0) {
         ItemMeta im = is.getItemMeta();
         if (im != null) {
            List<String> lore = im.getLore();
            if (lore != null && lore.size() > 0 && ((String)lore.get(0)).equals(this.check)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private void loadConfig(FileConfiguration config) {
      this.check = Util.convert(config.getString("luck.check"));
      this.luckHash = new HashMap();

      for(String s : config.getStringList("luck.get")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         String type = s.split(" ")[1];
         this.luckHash.put(id, type);
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
