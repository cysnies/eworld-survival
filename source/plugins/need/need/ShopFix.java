package need;

import java.util.HashSet;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilSpeed;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Util.Util;

public class ShopFix implements Listener {
   private static final String SPEED_SEE = "see";
   private Main main;
   private String pn;
   private QuickShop qs;
   private int interval;
   private String check;
   private static HashSet transSet = new HashSet();

   static {
      transSet.add((byte)0);
      transSet.add((byte)68);
   }

   public ShopFix(Main main) {
      super();
      this.main = main;
      this.pn = main.getPn();
      this.qs = (QuickShop)Bukkit.getPluginManager().getPlugin("QuickShop");
      this.loadConfig(UtilConfig.getConfig(this.pn));
      Bukkit.getServer().getPluginManager().registerEvents(this, main);
      UtilSpeed.register(this.pn, "see");
   }

   public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      String cmdName = cmd.getName();
      if (cmdName.equalsIgnoreCase("see")) {
         if (p == null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(40)}));
            return;
         }

         this.see(p);
      }

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
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onInventoryClick(InventoryClickEvent e) {
      if (e.getInventory().getType().equals(InventoryType.CHEST) && e.getInventory().getTitle() != null && e.getInventory().getTitle().equalsIgnoreCase(this.check) && e.getWhoClicked() instanceof Player) {
         e.setCancelled(true);
         Bukkit.getScheduler().scheduleSyncDelayedTask(this.main, new Close((Player)e.getWhoClicked()));
         ((Player)e.getWhoClicked()).sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(4005)}));
      }

   }

   private void see(Player p) {
      if (UtilSpeed.check(p, this.pn, "see", this.interval)) {
         Block b = p.getTargetBlock(transSet, 16);
         Shop shop = this.getShop(b);
         if (shop == null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(4000)}));
         } else {
            ItemStack is = shop.getItem();
            Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, this.check);
            inv.setItem(4, is);
            p.closeInventory();
            p.openInventory(inv);
         }
      }
   }

   private Shop getShop(Block b) {
      Location loc = b.getLocation();
      Shop shop = this.qs.getShopManager().getShop(loc);
      if (shop == null && b.getType() == Material.WALL_SIGN) {
         Block attached = Util.getAttached(b);
         if (attached != null) {
            shop = this.qs.getShopManager().getShop(attached.getLocation());
         }

         return shop;
      } else {
         return shop;
      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.interval = config.getInt("shopFix.interval");
      this.check = lib.util.Util.convert(config.getString("shopFix.check"));
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   private class Close implements Runnable {
      private Player p;

      public Close(Player p) {
         super();
         this.p = p;
      }

      public void run() {
         if (this.p != null && this.p.isOnline()) {
            this.p.closeInventory();
         }

      }
   }
}
