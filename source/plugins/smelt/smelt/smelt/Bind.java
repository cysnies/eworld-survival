package smelt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilNames;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Bind implements Listener {
   private ItemMeta IM = (new ItemStack(1)).getItemMeta();
   private String pn;
   private String checkStone;
   private String check;
   private String times;
   private String owner;
   private int globalTip;
   private int tip;
   private long lastTip;
   private HashMap lastTipHash = new HashMap();

   public Bind(Main main) {
      super();
      this.pn = main.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      Bukkit.getPluginManager().registerEvents(this, main);
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
   public void onPlayerDropItem(PlayerDropItemEvent e) {
      if (this.isBind(e.getItemDrop().getItemStack()) && UtilItems.getEmptySlots(e.getPlayer().getInventory()) > 0) {
         e.setCancelled(true);
         e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(500)}));
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      if (!Main.isIgnored(e.getClickedBlock())) {
         if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (e.getPlayer().getInventory().getHeldItemSlot() == 0) {
               return;
            }

            ItemStack is = e.getPlayer().getItemInHand();
            if (this.isBindStone(is)) {
               ItemStack item0 = e.getPlayer().getInventory().getItem(0);
               if (item0 == null || item0.getTypeId() == 0) {
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(505)}));
                  return;
               }

               if (item0.getAmount() != 1) {
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(510)}));
                  return;
               }

               if (UtilItems.getEmptySlots(e.getPlayer().getInventory()) < 1) {
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(425)}));
                  return;
               }

               int times = this.getTimes(is);
               if (times <= 0) {
                  return;
               }

               boolean bind = true;
               if (this.isBind(item0)) {
                  String owner = this.getOwner(item0);
                  if (owner != null && !owner.equals(e.getPlayer().getName())) {
                     e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(520)}));
                     return;
                  }

                  bind = false;
               }

               if (times > 1) {
                  if (is.getAmount() > 1) {
                     is.setAmount(is.getAmount() - 1);
                     ItemStack result = is.clone();
                     result.setAmount(1);
                     this.setTimes(result, times - 1);
                     e.getPlayer().getInventory().addItem(new ItemStack[]{result});
                  } else {
                     this.setTimes(is, times - 1);
                  }
               } else if (is.getAmount() > 1) {
                  is.setAmount(is.getAmount() - 1);
               } else {
                  e.getPlayer().setItemInHand((ItemStack)null);
               }

               if (bind) {
                  this.setBind(item0);
                  this.setOwner(item0, e.getPlayer().getName());
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(515)}));
               } else {
                  this.cancelBind(item0);
                  this.setOwner(item0, (String)null);
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(525)}));
               }

               e.getPlayer().updateInventory();
            }
         }

      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onInventoryClick(InventoryClickEvent e) {
      if (e.getWhoClicked() instanceof Player) {
         long now = System.currentTimeMillis();
         if (now - this.lastTip < (long)this.globalTip) {
            return;
         }

         this.lastTip = now;
         Player p = (Player)e.getWhoClicked();
         ItemStack is = p.getItemInHand();
         String owner = this.getOwner(is);
         if (owner == null || owner.equals(p.getName())) {
            is = p.getItemOnCursor();
            owner = this.getOwner(is);
         }

         now /= 1000L;
         if (owner != null && !owner.equals(p.getName())) {
            if (!this.lastTipHash.containsKey(owner)) {
               this.lastTipHash.put(owner, 0L);
            }

            if (now - (Long)this.lastTipHash.get(owner) >= (long)this.tip) {
               this.lastTipHash.put(owner, now);
               Util.sendMsg(owner, UtilFormat.format(this.pn, "ownerTip", new Object[]{p.getName(), UtilNames.getItemName(is.getTypeId(), 0)}));
            }
         }
      }

   }

   public String getOwner(ItemStack is) {
      try {
         for(String s : is.getItemMeta().getLore()) {
            if (s.startsWith(this.owner)) {
               return s.substring(this.owner.length());
            }
         }
      } catch (Exception var5) {
      }

      return null;
   }

   private void setOwner(ItemStack is, String owner) {
      try {
         ItemMeta im = is.getItemMeta();
         List<String> lore = im.getLore();

         for(int i = 0; i < lore.size(); ++i) {
            String s = (String)lore.get(i);
            if (s.startsWith(this.owner)) {
               lore.remove(i);
               im.setLore(lore);
               is.setItemMeta(im);
               break;
            }
         }
      } catch (Exception var8) {
      }

      if (owner != null) {
         try {
            ItemMeta im = is.getItemMeta();
            List<String> lore = im.getLore();
            lore.add(this.owner + owner);
            im.setLore(lore);
            is.setItemMeta(im);
         } catch (Exception var7) {
         }
      }

   }

   private int getTimes(ItemStack is) {
      try {
         return Integer.parseInt(((String)is.getItemMeta().getLore().get(1)).split(" ")[1]);
      } catch (Exception var3) {
         return 0;
      }
   }

   private void setTimes(ItemStack is, int times) {
      ItemMeta im = is.getItemMeta();
      List<String> lore = im.getLore();
      lore.set(1, this.times + times);
      im.setLore(lore);
      is.setItemMeta(im);
   }

   private void setBind(ItemStack is) {
      try {
         ItemMeta im = is.getItemMeta();
         if (im == null) {
            im = this.IM.clone();
         }

         List<String> lore = im.getLore();
         if (lore == null) {
            lore = new ArrayList();
         }

         lore.add(this.check);
         im.setLore(lore);
         is.setItemMeta(im);
      } catch (Exception var4) {
      }

   }

   private void cancelBind(ItemStack is) {
      try {
         ItemMeta im = is.getItemMeta();
         List<String> lore = im.getLore();
         lore.remove(this.check);
         im.setLore(lore);
         is.setItemMeta(im);
      } catch (Exception var4) {
      }

   }

   private boolean isBindStone(ItemStack is) {
      try {
         return ((String)is.getItemMeta().getLore().get(0)).equals(this.checkStone);
      } catch (Exception var3) {
         return false;
      }
   }

   private boolean isBind(ItemStack is) {
      try {
         if (is.getItemMeta().getLore().contains(this.check)) {
            return true;
         }
      } catch (Exception var3) {
      }

      return false;
   }

   private void loadConfig(FileConfiguration config) {
      this.checkStone = Util.convert(config.getString("bind.checkStone"));
      this.check = Util.convert(config.getString("bind.check"));
      this.times = Util.convert(config.getString("bind.times"));
      this.owner = Util.convert(config.getString("bind.owner"));
      this.globalTip = config.getInt("bind.globalTip");
      this.tip = config.getInt("bind.tip");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
