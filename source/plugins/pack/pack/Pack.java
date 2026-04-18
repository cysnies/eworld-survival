package pack;

import java.util.HashMap;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import lib.util.UtilSpeed;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class Pack implements Listener {
   private static int SIZE = 54;
   private static String per = "per.pack.open.";
   private static String SPEED = "pack";
   private String pn;
   private int max;
   private int speed;
   private HashMap handleHash = new HashMap();

   public Pack(Main main) {
      super();
      this.pn = main.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      Bukkit.getPluginManager().registerEvents(this, main);
      UtilSpeed.register(this.pn, SPEED);
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      e.getPlayer().closeInventory();
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onInventoryClose(InventoryCloseEvent e) {
      if (e.getPlayer() instanceof Player) {
         Player p = (Player)e.getPlayer();
         if (this.handleHash.containsKey(p)) {
            Info info = (Info)this.handleHash.remove(p);
            if (info != null) {
               PackUser pu = info.pu;
               HashMap<Integer, String> itemHash = new HashMap();
               pu.getItemsHash().put(info.id, itemHash);
               Inventory inv = e.getInventory();
               int size = inv.getSize();

               for(int slot = 0; slot < size; ++slot) {
                  ItemStack is = inv.getItem(slot);
                  if (is != null && is.getTypeId() != 0 && is.getTypeId() != 386 && is.getTypeId() != 387) {
                     itemHash.put(slot, UtilItems.saveItem(is));
                  }
               }

               Main.getDao().addOrUpdatePackUser(pu);
               p.sendMessage(UtilFormat.format(this.pn, "save", new Object[]{info.id}));
            }
         }
      }

   }

   public boolean open(Player p, int id) {
      if (!UtilSpeed.check(p, this.pn, SPEED, this.speed)) {
         return false;
      } else if (id < 1) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(55)}));
         return false;
      } else if (id > this.max) {
         p.sendMessage(UtilFormat.format(this.pn, "maxPack", new Object[]{id, this.max}));
         return false;
      } else if (!UtilPer.hasPer(p, per + id)) {
         p.sendMessage(UtilFormat.format(this.pn, "noPackPer", new Object[]{id}));
         return false;
      } else {
         PackUser pu = this.checkInit(p.getName());
         boolean update = false;
         HashMap<Integer, HashMap<Integer, String>> packHash = pu.getItemsHash();
         HashMap<Integer, String> itemHash = (HashMap)packHash.get(id);
         if (itemHash == null) {
            update = true;
            itemHash = new HashMap();
            packHash.put(id, itemHash);
         }

         Info info = new Info(pu, id);
         if (update) {
            Main.getDao().addOrUpdatePackUser(pu);
         }

         Inventory inv = Bukkit.createInventory((InventoryHolder)null, SIZE, UtilFormat.format(this.pn, "packTitle", new Object[]{id}));

         for(int slot : itemHash.keySet()) {
            try {
               String data = (String)itemHash.get(slot);
               ItemStack is = UtilItems.loadItem(data);
               inv.setItem(slot, is);
            } catch (Exception var13) {
            }
         }

         p.closeInventory();
         p.openInventory(inv);
         this.handleHash.put(p, info);
         return true;
      }
   }

   public String getCanUse(Player p) {
      String result = "";

      for(int i = 1; i <= this.max; ++i) {
         if (UtilPer.hasPer(p, per + i)) {
            if (!result.isEmpty()) {
               result = result + ",";
            }

            result = result + i;
         }
      }

      return result;
   }

   private void loadConfig(YamlConfiguration config) {
      this.max = config.getInt("max");
      this.speed = config.getInt("speed");
   }

   private PackUser checkInit(String name) {
      PackUser pu = Main.getDao().getPackUser(name);
      if (pu == null) {
         pu = new PackUser(name, new HashMap());
         if (!Main.getDao().addPackUser(pu)) {
            pu = Main.getDao().getPackUser(name);
         }
      }

      return pu;
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   private class Info {
      private PackUser pu;
      private int id;

      public Info(PackUser pu, int id) {
         super();
         this.pu = pu;
         this.id = id;
      }
   }
}
