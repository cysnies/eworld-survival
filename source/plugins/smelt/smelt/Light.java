package smelt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import lib.config.ReloadConfigEvent;
import lib.realDamage.RealDamageEvent;
import lib.types.InvalidTypeException;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import lib.util.UtilSpeed;
import lib.util.UtilTypes;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Light implements Listener {
   private static final String SPEED = "light";
   private static final HashMap HASH = new HashMap();
   private ItemMeta emptyItemMeta = (new ItemStack(1)).getItemMeta();
   private Random r = new Random();
   private Server server;
   private String pn;
   private String per_smelt_vip;
   private int chance;
   private String check;
   private String checkStart;
   private String checkEnd;
   private int vipAdd;
   private int start;
   private int left;
   private String item;
   private int range;
   private int min;
   private int max;
   private int speed;
   private HashMap upgradeMax;
   private HashMap types;
   private String stoneCheck;
   private String stoneName;
   private String stoneLevel;
   private String stoneTip;

   static {
      HASH.put(1, "I");
      HASH.put(2, "II");
      HASH.put(3, "III");
      HASH.put(4, "IV");
      HASH.put(5, "V");
      HASH.put(6, "VI");
      HASH.put(7, "VII");
      HASH.put(8, "VIII");
      HASH.put(9, "IX");
      HASH.put(10, "X");
   }

   public Light(Main main) {
      super();
      this.server = main.getServer();
      this.pn = main.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, main);
      UtilSpeed.register(this.pn, "light");
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
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onRealDamage(RealDamageEvent e) {
      if (e.getVictim().isValid() && e.getDamager() instanceof Player && this.r.nextInt(this.chance) < 1) {
         Player p = (Player)e.getDamager();
         int level = this.getLightLevel(p.getItemInHand());
         if (level > 0) {
            int add = 0;
            String vip = "§m";
            if (UtilPer.hasPer(p, this.per_smelt_vip)) {
               add = this.vipAdd;
               vip = "";
            }

            e.setDamage(e.getDamage() * (double)(level + 1) * (double)(100 + add) / (double)100.0F);
            Util.strikeLightning(e.getVictim().getLocation(), this.range);
            p.sendMessage(UtilFormat.format(this.pn, "lightTip1", new Object[]{level, vip, this.vipAdd}));
            if (e.getVictim() instanceof Player) {
               ((Player)e.getVictim()).sendMessage(UtilFormat.format(this.pn, "lightTip2", new Object[]{level}));
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      try {
         if (Main.isIgnored(e.getClickedBlock())) {
            return;
         }

         if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (e.getPlayer().getInventory().getHeldItemSlot() == 0) {
               return;
            }

            ItemStack is = e.getPlayer().getItemInHand();
            if (!this.isLightStone(is)) {
               return;
            }

            e.setCancelled(true);
            if (!UtilSpeed.check(e.getPlayer(), this.pn, "light", this.speed)) {
               return;
            }

            int level = this.getStoneLevel(is);
            if (level < this.min || level > this.max) {
               return;
            }

            String type = (String)this.types.get(is.getTypeId());
            ItemStack item0 = e.getPlayer().getInventory().getItem(0);

            try {
               if (item0 != null && UtilTypes.checkItem(this.pn, type, "" + item0.getTypeId())) {
                  int tarLevel = this.getStoneLevel(is);
                  if (tarLevel <= 0) {
                     return;
                  }

                  int lightLevel = this.getLightLevel(item0);
                  if (lightLevel > 0) {
                     if (lightLevel != tarLevel) {
                        e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(405)}));
                        return;
                     }

                     ++tarLevel;
                  }

                  int max2 = (Integer)this.upgradeMax.get(is.getTypeId());
                  if (tarLevel > this.max || tarLevel > max2) {
                     e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(400)}));
                     return;
                  }

                  if (!this.setLightLevel(item0, tarLevel)) {
                     e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(410)}));
                     return;
                  }

                  if (is.getAmount() > 1) {
                     is.setAmount(is.getAmount() - 1);
                  } else {
                     e.getPlayer().setItemInHand((ItemStack)null);
                  }

                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(415)}));
                  e.getPlayer().updateInventory();
               } else if (UtilItems.isSame(is, item0)) {
                  int max2 = (Integer)this.upgradeMax.get(is.getTypeId());
                  if (level >= this.max || level >= max2) {
                     e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(400)}));
                     return;
                  }

                  if (UtilItems.getEmptySlots(e.getPlayer().getInventory()) < 1) {
                     e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(425)}));
                     return;
                  }

                  ++level;
                  ItemStack result = is.clone();
                  result.setAmount(1);
                  this.setStoneLevel(result, level);
                  if (item0.getAmount() > 1) {
                     item0.setAmount(item0.getAmount() - 1);
                  } else {
                     e.getPlayer().getInventory().setItem(0, (ItemStack)null);
                  }

                  if (is.getAmount() > 1) {
                     is.setAmount(is.getAmount() - 1);
                  } else {
                     e.getPlayer().setItemInHand((ItemStack)null);
                  }

                  e.getPlayer().getInventory().addItem(new ItemStack[]{result});
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "lightCombine", new Object[]{level}));
                  e.getPlayer().updateInventory();
               } else {
                  e.getPlayer().sendMessage(this.get(420));
               }
            } catch (InvalidTypeException e1) {
               e1.printStackTrace();
            }
         }
      } catch (Exception var10) {
      }

   }

   public boolean setLightLevel(ItemStack is, int level) {
      try {
         if (is == null) {
            return false;
         }

         if (UtilTypes.checkItem(this.pn, this.item, String.valueOf(is.getTypeId()))) {
            ItemMeta im = is.getItemMeta();
            if (im == null) {
               im = this.emptyItemMeta.clone();
            }

            List<String> lore = im.getLore();
            if (lore == null) {
               lore = new ArrayList();
            }

            for(int index = 0; index < lore.size(); ++index) {
               String s = (String)lore.get(index);
               int i = this.getLightLevel(s);
               if (i > 0) {
                  lore.set(index, this.check.replace("*", String.valueOf(level)));
                  im.setLore(lore);
                  is.setItemMeta(im);
                  return true;
               }
            }

            lore.add(this.check.replace("*", String.valueOf(level)));
            im.setLore(lore);
            is.setItemMeta(im);
            return true;
         }
      } catch (InvalidTypeException var8) {
      }

      return false;
   }

   public int getLightLevel(ItemStack is) {
      try {
         for(String s : is.getItemMeta().getLore()) {
            int level = this.getLightLevel(s);
            if (level > 0) {
               return level;
            }
         }
      } catch (Exception var6) {
      }

      return 0;
   }

   private int getStoneLevel(ItemStack is) {
      try {
         if (this.isLightStone(is)) {
            ItemMeta im = is.getItemMeta();
            List<String> lore = im.getLore();
            return Integer.parseInt(((String)lore.get(1)).split(" ")[1]);
         }
      } catch (Exception var4) {
      }

      return 0;
   }

   private void setStoneLevel(ItemStack is, int level) {
      try {
         if (this.isLightStone(is)) {
            ItemMeta im = is.getItemMeta();
            String levelString = (String)HASH.get(level);
            im.setDisplayName(this.stoneName.replace("*", levelString));
            List<String> lore = im.getLore();
            int max = (Integer)this.upgradeMax.get(is.getTypeId());
            lore.set(1, this.stoneLevel.replace("*", String.valueOf(max)) + " " + level);
            lore.set(2, this.stoneTip.replace("*", String.valueOf(level)));
            im.setLore(lore);
            is.setItemMeta(im);
         }
      } catch (Exception var7) {
      }

   }

   private boolean isLightStone(ItemStack is) {
      if (is != null) {
         ItemMeta im = is.getItemMeta();
         if (im != null) {
            List<String> lore = im.getLore();
            if (lore != null && lore.size() >= 1 && ((String)lore.get(0)).equals(this.stoneCheck)) {
               return true;
            }
         }
      }

      return false;
   }

   private int getLightLevel(String s) {
      try {
         if (s.substring(0, this.start).equals(this.checkStart) && s.substring(s.length() - this.left, s.length()).equals(this.checkEnd)) {
            int result = Integer.parseInt(s.substring(this.start, s.length() - this.left));
            if (result < this.min) {
               result = this.min;
            } else if (result > this.max) {
               result = this.max;
            }

            return result;
         }
      } catch (Exception var3) {
      }

      return 0;
   }

   private void loadConfig(FileConfiguration config) {
      this.per_smelt_vip = config.getString("per_smelt_vip");
      this.chance = config.getInt("light.chance");
      this.check = Util.convert(config.getString("light.check"));
      this.vipAdd = config.getInt("light.vipAdd");
      this.start = config.getInt("light.start");
      this.left = config.getInt("light.left");
      this.item = config.getString("light.item");
      this.range = config.getInt("light.range");
      this.min = config.getInt("light.min");
      this.max = config.getInt("light.max");
      this.checkStart = this.check.substring(0, this.start);
      this.checkEnd = this.check.substring(this.check.length() - this.left, this.check.length());
      this.speed = config.getInt("light.speed");
      this.upgradeMax = new HashMap();

      for(String s : config.getStringList("light.upgrade.max")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         int max = Integer.parseInt(s.split(" ")[1]);
         this.upgradeMax.put(id, max);
      }

      this.types = new HashMap();

      for(String s : config.getStringList("light.types")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         String type = s.split(" ")[1];
         this.types.put(id, type);
      }

      this.stoneCheck = Util.convert(config.getString("light.stone.check"));
      this.stoneName = Util.convert(config.getString("light.stone.name"));
      this.stoneLevel = Util.convert(config.getString("light.stone.level"));
      this.stoneTip = Util.convert(config.getString("light.stone.tip"));
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
