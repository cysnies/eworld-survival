package smelt;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilNames;
import lib.util.UtilPer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Seal implements Listener {
   private Random r = new Random();
   private String pn;
   private String per_smelt_vip;
   private int vipAdd;
   private String check;
   private String times;
   private HashMap sealHash;

   public Seal(Main main) {
      super();
      this.pn = main.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      Bukkit.getPluginManager().registerEvents(this, main);
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
   public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
      ItemStack is = e.getPlayer().getItemInHand();
      if (this.isSeal(is)) {
         e.setCancelled(true);
         if (!(e.getRightClicked() instanceof LivingEntity)) {
            return;
         }

         if (e.getRightClicked().getPassenger() != null || e.getRightClicked().getVehicle() != null) {
            e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(370)}));
            return;
         }

         Entity entity = e.getRightClicked();
         if (entity instanceof InventoryHolder) {
            InventoryHolder ih = (InventoryHolder)entity;
            if (ih.getInventory() != null) {
               Inventory inv = ih.getInventory();

               for(int i = 0; i < inv.getSize(); ++i) {
                  ItemStack is2 = inv.getItem(i);
                  if (is2 != null && is2.getTypeId() != 0) {
                     e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(380)}));
                     return;
                  }
               }
            }
         }

         if (entity instanceof Tameable) {
            Tameable tameable = (Tameable)entity;
            if (tameable.getOwner() != null) {
               if (tameable.getOwner() instanceof Player) {
                  Player owner = (Player)tameable.getOwner();
                  if (!owner.getName().equals(e.getPlayer().getName())) {
                     e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(385)}));
                     return;
                  }
               } else if (tameable.getOwner() instanceof OfflinePlayer) {
                  OfflinePlayer owner = (OfflinePlayer)tameable.getOwner();
                  if (!owner.getName().equals(e.getPlayer().getName())) {
                     e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(385)}));
                     return;
                  }
               }
            }
         }

         SealItem sealItem = (SealItem)this.sealHash.get(is.getTypeId());
         if (sealItem == null) {
            return;
         }

         int typeId = entity.getType().getTypeId();
         if (!sealItem.getChance().containsKey(typeId)) {
            e.getPlayer().sendMessage(UtilFormat.format(this.pn, "sealErr", new Object[]{is.getItemMeta().getDisplayName(), UtilNames.getEntityName(entity)}));
            return;
         }

         if (UtilItems.getEmptySlots(e.getPlayer().getInventory()) <= 1) {
            e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(375)}));
            return;
         }

         int leftTimes = 1;

         try {
            String s = (String)is.getItemMeta().getLore().get(1);
            leftTimes = Integer.parseInt(s.substring(this.times.length(), s.length()));
         } catch (Exception var12) {
         }

         if (leftTimes > 1) {
            --leftTimes;
            if (is.getAmount() > 1) {
               is.setAmount(is.getAmount() - 1);
               ItemStack result = is.clone();
               result.setAmount(1);
               ItemMeta im = result.getItemMeta();
               List<String> lore = im.getLore();
               lore.set(1, this.times + leftTimes);
               im.setLore(lore);
               result.setItemMeta(im);
               e.getPlayer().getInventory().addItem(new ItemStack[]{result});
            } else {
               ItemMeta im = is.getItemMeta();
               List<String> lore = im.getLore();
               lore.set(1, this.times + leftTimes);
               im.setLore(lore);
               is.setItemMeta(im);
            }
         } else if (is.getAmount() <= 1) {
            e.getPlayer().setItemInHand((ItemStack)null);
         } else {
            is.setAmount(is.getAmount() - 1);
         }

         int add = 0;
         if (UtilPer.hasPer(e.getPlayer(), this.per_smelt_vip)) {
            add = this.vipAdd;
         }

         int chance;
         try {
            chance = (Integer)sealItem.getChance().get(typeId);
         } catch (Exception var11) {
            return;
         }

         chance = chance * (add + 100) / 100;
         String vip = this.get(45);
         if (UtilPer.hasPer(e.getPlayer(), this.per_smelt_vip)) {
            vip = "";
         }

         if (this.r.nextInt(100) >= chance) {
            e.getPlayer().sendMessage(UtilFormat.format(this.pn, "sealFail", new Object[]{vip}));
            e.getPlayer().updateInventory();
            return;
         }

         ItemStack result = new ItemStack(383);
         result.setDurability(entity.getType().getTypeId());
         e.getPlayer().getInventory().addItem(new ItemStack[]{result});
         e.getPlayer().updateInventory();
         e.getRightClicked().remove();
         e.getPlayer().sendMessage(UtilFormat.format(this.pn, "sealSuccess", new Object[]{vip}));
      }

   }

   private boolean isSeal(ItemStack is) {
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
      this.per_smelt_vip = config.getString("per_smelt_vip");
      this.vipAdd = config.getInt("seal.vipAdd");
      this.check = Util.convert(config.getString("seal.check"));
      this.times = Util.convert(config.getString("seal.times"));
      this.sealHash = new HashMap();

      for(int index = 1; config.contains("seal.item" + index); ++index) {
         int id = config.getInt("seal.item" + index + ".id");
         HashMap<Integer, Integer> chance = new HashMap();

         for(String s : config.getStringList("seal.item" + index + ".chance")) {
            int typeId = Integer.parseInt(s.split(" ")[0]);
            int i = Integer.parseInt(s.split(" ")[1]);
            chance.put(typeId, i);
         }

         SealItem smeltItem = new SealItem(id, chance);
         this.sealHash.put(id, smeltItem);
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   private class SealItem {
      private int id;
      private HashMap chance;

      public SealItem(int id, HashMap chance) {
         super();
         this.id = id;
         this.chance = chance;
      }

      public int getId() {
         return this.id;
      }

      public HashMap getChance() {
         return this.chance;
      }
   }
}
