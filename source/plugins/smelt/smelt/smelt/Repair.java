package smelt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import lib.config.ReloadConfigEvent;
import lib.types.InvalidTypeException;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilNames;
import lib.util.UtilPer;
import lib.util.UtilSpeed;
import lib.util.UtilTypes;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Repair implements Listener {
   private ItemMeta emptyItemMeta = (new ItemStack(1)).getItemMeta();
   private Random r = new Random();
   private String SPEED = "repair";
   private Server server;
   private String pn;
   private String per_smelt_vip;
   private String ignore;
   private int interval;
   private int vipAdd;
   private int vipChanceAdd;
   private String check;
   private String check2;
   private String check3;
   private int chance1;
   private int chance2;
   private int defaultUse;
   private String checkUse;
   private String checkBad;
   private HashMap repairHash;
   private HashMap up1Hash;
   private HashMap up2Hash;

   public Repair(Main main) {
      super();
      this.server = main.getServer();
      this.pn = main.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, main);
      UtilSpeed.register(this.pn, this.SPEED);
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
   public void onInventoryClick(InventoryClickEvent e) {
      if (e.getInventory().getType().equals(InventoryType.ANVIL) && e.getSlot() == 2) {
         ItemStack is = e.getInventory().getItem(0);
         if (is != null) {
            ItemStack check = e.getInventory().getItem(1);
            if (check == null || check.getTypeId() == 0) {
               return;
            }

            int bad = this.getBad(is);
            if (bad == -1) {
               return;
            }

            if (bad == 0) {
               e.setCancelled(true);

               for(HumanEntity he : e.getViewers()) {
                  if (he instanceof Player) {
                     Player p = (Player)he;
                     p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(340)}));
                  }
               }
            } else {
               boolean result = true;
               ItemStack iss = e.getInventory().getItem(2);

               try {
                  if (UtilTypes.checkItem(this.pn, this.ignore, iss.getType().name())) {
                     result = false;
                  }
               } catch (InvalidTypeException e1) {
                  e1.printStackTrace();
               } catch (Exception var11) {
               }

               if (result) {
                  this.setBad(iss, bad - 1);

                  for(HumanEntity he : e.getViewers()) {
                     if (he instanceof Player) {
                        Player p = (Player)he;
                        p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(345)}));
                     }
                  }
               }
            }
         }
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

            int type = this.getRepairType(e.getPlayer().getItemInHand());
            if (type == 0) {
               return;
            }

            if (!UtilSpeed.check(e.getPlayer(), this.pn, this.SPEED, this.interval)) {
               return;
            }

            e.setCancelled(true);
            ItemStack is = e.getPlayer().getInventory().getItem(0);
            if (is == null || is.getTypeId() == 0) {
               e.getPlayer().sendMessage(this.get(350));
               return;
            }

            if (UtilItems.isSame(e.getPlayer().getItemInHand(), is)) {
               this.checkCombine(type, e.getPlayer(), e.getPlayer().getItemInHand(), is);
            } else {
               this.checkRepair(type, e.getPlayer(), e.getPlayer().getItemInHand(), is);
            }
         }

      }
   }

   private void checkCombine(int type, Player p, ItemStack itemInHand, ItemStack is) {
      if (type == 3) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(355)}));
      } else if (UtilItems.getEmptySlots(p.getInventory()) < 1) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(360)}));
      } else {
         if (is.getAmount() <= 1) {
            p.getInventory().setItem(0, (ItemStack)null);
         } else {
            is.setAmount(is.getAmount() - 1);
         }

         int chance;
         if (type == 1) {
            chance = this.chance1;
         } else {
            chance = this.chance2;
         }

         if (UtilPer.hasPer(p, this.per_smelt_vip)) {
            chance += this.vipChanceAdd;
         }

         String vip = "§m";
         if (UtilPer.hasPer(p, this.per_smelt_vip)) {
            vip = "";
         }

         if (this.r.nextInt(100) < chance) {
            if (itemInHand.getAmount() <= 1) {
               p.setItemInHand((ItemStack)null);
            } else {
               itemInHand.setAmount(itemInHand.getAmount() - 1);
            }

            int id = itemInHand.getTypeId();
            String itemType;
            if (type == 1) {
               itemType = (String)this.up1Hash.get(id);
            } else {
               itemType = (String)this.up2Hash.get(id);
            }

            if (itemType == null) {
               return;
            }

            ItemStack result = UtilItems.getItem(this.pn, itemType);
            if (result == null) {
               return;
            }

            p.getInventory().addItem(new ItemStack[]{result});
            p.sendMessage(UtilFormat.format(this.pn, "up1", new Object[]{vip}));
         } else {
            p.sendMessage(UtilFormat.format(this.pn, "up2", new Object[]{vip}));
         }

         p.updateInventory();
      }
   }

   private void checkRepair(int type, Player p, ItemStack itemInHand, ItemStack is) {
      RepairItem repairItem = (RepairItem)this.repairHash.get(itemInHand.getTypeId());
      if (repairItem != null) {
         int effect;
         try {
            if (UtilTypes.checkItem(this.pn, (String)repairItem.getType().get(1), String.valueOf(is.getTypeId()))) {
               if (type == 1) {
                  effect = (Integer)repairItem.getEffect().get(4);
               } else if (type == 2) {
                  effect = (Integer)repairItem.getEffect().get(1);
               } else {
                  effect = (Integer)repairItem.getEffect().get(7);
               }
            } else if (UtilTypes.checkItem(this.pn, (String)repairItem.getType().get(2), String.valueOf(is.getTypeId()))) {
               if (type == 1) {
                  effect = (Integer)repairItem.getEffect().get(5);
               } else if (type == 2) {
                  effect = (Integer)repairItem.getEffect().get(2);
               } else {
                  effect = (Integer)repairItem.getEffect().get(8);
               }
            } else {
               if (!UtilTypes.checkItem(this.pn, (String)repairItem.getType().get(3), String.valueOf(is.getTypeId()))) {
                  p.sendMessage(UtilFormat.format(this.pn, "repairErr", new Object[]{itemInHand.getItemMeta().getDisplayName(), UtilNames.getItemName(is.getTypeId(), is.getDurability())}));
                  return;
               }

               if (type == 1) {
                  effect = (Integer)repairItem.getEffect().get(6);
               } else if (type == 2) {
                  effect = (Integer)repairItem.getEffect().get(3);
               } else {
                  effect = (Integer)repairItem.getEffect().get(9);
               }
            }
         } catch (InvalidTypeException e1) {
            e1.printStackTrace();
            return;
         }

         int bad = this.getBad(is);
         if (bad != -1) {
            if (bad == 0) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(340)}));
            } else if (is.getDurability() <= 0) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(55)}));
            } else {
               if (itemInHand.getAmount() <= 1) {
                  p.setItemInHand((ItemStack)null);
               } else {
                  itemInHand.setAmount(itemInHand.getAmount() - 1);
               }

               int add = 0;
               if (UtilPer.hasPer(p, this.per_smelt_vip)) {
                  add = this.vipAdd;
               }

               int repair = effect * (100 + add) / 100;
               repair = Math.min(is.getDurability(), repair);
               is.setDurability((short)(is.getDurability() - repair));
               if (type != 3) {
                  this.setBad(is, bad - 1);
                  p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(345)}));
               }

               p.updateInventory();
               String vip = this.get(45);
               if (UtilPer.hasPer(p, this.per_smelt_vip)) {
                  vip = "";
               }

               p.sendMessage(UtilFormat.format(this.pn, "repair", new Object[]{UtilNames.getItemName(is.getTypeId(), is.getDurability()), repair, vip, this.vipAdd}));
               p.getWorld().playSound(p.getLocation(), Sound.ANVIL_USE, 2.0F, 0.9F);
            }
         }
      }
   }

   private int getBad(ItemStack is) {
      ItemMeta im = this.checkAddItemMeta(is);
      if (im == null) {
         return -1;
      } else {
         List<String> lore = im.getLore();
         if (lore == null) {
            return this.setBad(is, this.defaultUse) ? this.defaultUse : -1;
         } else {
            for(String s : lore) {
               int result = this.getBad(s);
               if (result >= 0) {
                  return result;
               }
            }

            return this.setBad(is, this.defaultUse) ? this.defaultUse : -1;
         }
      }
   }

   private boolean setBad(ItemStack is, int bad) {
      ItemMeta im = this.checkAddItemMeta(is);
      if (im == null) {
         return false;
      } else {
         List<String> lore = im.getLore();
         if (lore == null) {
            lore = new ArrayList();
         }

         int index = -1;

         for(int i = 0; i < lore.size(); ++i) {
            String s = (String)lore.get(i);
            if (this.getBad(s) >= 0) {
               index = i;
               break;
            }
         }

         String result;
         if (bad == 0) {
            result = this.checkBad;
         } else {
            result = this.checkUse + bad;
         }

         if (index == -1) {
            lore.add(result);
         } else {
            lore.set(index, result);
         }

         im.setLore(lore);
         is.setItemMeta(im);
         return true;
      }
   }

   private int getBad(String s) {
      try {
         if (s == null || s.isEmpty()) {
            return -1;
         }

         if (s.equals(this.checkBad)) {
            return 0;
         }

         if (s.startsWith(this.checkUse)) {
            return Integer.parseInt(s.substring(this.checkUse.length(), s.length()));
         }
      } catch (Exception var3) {
      }

      return -1;
   }

   private ItemMeta checkAddItemMeta(ItemStack is) {
      if (is == null) {
         return null;
      } else {
         ItemMeta result = is.getItemMeta();
         if (result != null) {
            return result;
         } else if (!this.server.getItemFactory().isApplicable(this.emptyItemMeta, is)) {
            return null;
         } else {
            result = this.emptyItemMeta.clone();
            is.setItemMeta(result);
            return result;
         }
      }
   }

   private int getRepairType(ItemStack is) {
      if (is != null && is.getTypeId() != 0) {
         ItemMeta im = is.getItemMeta();
         if (im != null) {
            List<String> lore = im.getLore();
            if (lore != null && lore.size() > 0) {
               if (((String)lore.get(0)).equalsIgnoreCase(this.check)) {
                  return 2;
               }

               if (((String)lore.get(0)).equalsIgnoreCase(this.check2)) {
                  return 1;
               }

               if (((String)lore.get(0)).equalsIgnoreCase(this.check3)) {
                  return 3;
               }
            }
         }

         return 0;
      } else {
         return 0;
      }
   }

   private void loadConfig(FileConfiguration config) {
      this.per_smelt_vip = config.getString("per_smelt_vip");
      this.ignore = config.getString("repair.ignore");
      this.interval = config.getInt("repair.interval");
      this.vipAdd = config.getInt("repair.vipAdd");
      this.vipChanceAdd = config.getInt("repair.vipChanceAdd");
      this.check = Util.convert(config.getString("repair.check"));
      this.check2 = Util.convert(config.getString("repair.check2"));
      this.check3 = Util.convert(config.getString("repair.check3"));
      this.chance1 = config.getInt("repair.chance1");
      this.chance2 = config.getInt("repair.chance2");
      this.defaultUse = config.getInt("repair.defaultUse");
      this.checkUse = Util.convert(config.getString("repair.checkUse"));
      this.checkBad = Util.convert(config.getString("repair.checkBad"));
      this.repairHash = new HashMap();
      this.up1Hash = new HashMap();
      this.up2Hash = new HashMap();

      for(int index = 1; config.contains("repair.item" + index); ++index) {
         int id = config.getInt("repair.item" + index + ".id");
         HashMap<Integer, Integer> effect = new HashMap();

         for(int i = 1; i <= 9; ++i) {
            effect.put(i, config.getInt("repair.item" + index + ".effect" + i));
         }

         HashMap<Integer, String> type = new HashMap();

         for(int i = 1; i <= 3; ++i) {
            type.put(i, config.getString("repair.item" + index + ".type" + i));
         }

         this.up1Hash.put(id, config.getString("repair.item" + index + ".up1"));
         this.up2Hash.put(id, config.getString("repair.item" + index + ".up2"));
         RepairItem smeltItem = new RepairItem(id, effect, type);
         this.repairHash.put(id, smeltItem);
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   class RepairItem {
      private int id;
      private HashMap effect;
      private HashMap type;

      public RepairItem(int id, HashMap effect, HashMap type) {
         super();
         this.id = id;
         this.effect = effect;
         this.type = type;
      }

      public int getId() {
         return this.id;
      }

      public HashMap getEffect() {
         return this.effect;
      }

      public HashMap getType() {
         return this.type;
      }
   }
}
