package fix;

import java.util.HashMap;
import java.util.Random;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;

public class UseQuicker implements Listener {
   private static final Enchantment ENCH = Enchantment.getById(34);
   private static HashMap typeHash = new HashMap();
   private static HashMap maxDurabilityHash;
   private Random r = new Random();
   private Fix basic;
   private int chanceFire;
   private int addFire;
   private int chanceFish;
   private int addFish;
   private int chanceBow;
   private int addBow;
   private HashMap chanceHash;
   private HashMap addHash;
   private HashMap addConsumeHash;

   static {
      typeHash.put(268, 1);
      typeHash.put(269, 1);
      typeHash.put(270, 1);
      typeHash.put(271, 1);
      typeHash.put(290, 1);
      typeHash.put(272, 2);
      typeHash.put(273, 2);
      typeHash.put(274, 2);
      typeHash.put(275, 2);
      typeHash.put(291, 2);
      typeHash.put(256, 3);
      typeHash.put(257, 3);
      typeHash.put(258, 3);
      typeHash.put(267, 3);
      typeHash.put(292, 3);
      typeHash.put(283, 4);
      typeHash.put(284, 4);
      typeHash.put(285, 4);
      typeHash.put(286, 4);
      typeHash.put(294, 4);
      typeHash.put(276, 5);
      typeHash.put(277, 5);
      typeHash.put(278, 5);
      typeHash.put(279, 5);
      typeHash.put(293, 5);
      typeHash.put(359, 6);
      maxDurabilityHash = new HashMap();
      maxDurabilityHash.put(1, 60);
      maxDurabilityHash.put(2, 132);
      maxDurabilityHash.put(3, 251);
      maxDurabilityHash.put(4, 33);
      maxDurabilityHash.put(5, 1562);
      maxDurabilityHash.put(6, 238);
   }

   public UseQuicker(Fix basic) {
      super();
      this.basic = basic;
      this.loadConfig(UtilConfig.getConfig(basic.getPn()));
      basic.getPm().registerEvents(this, basic);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.basic.getPn())) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockBreak(BlockBreakEvent e) {
      this.toolUseMore(e.getPlayer(), e.getBlock().getTypeId());
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.hasItem()) {
         ItemStack is = e.getItem();
         if (is != null && is.getTypeId() != 0) {
            int id = is.getTypeId();
            if (id == 259) {
               int level = is.getEnchantmentLevel(ENCH);
               if (level > 3) {
                  level = 3;
               }

               if (this.r.nextInt(100) < this.chanceFire && this.r.nextInt(1 + level) < 1) {
                  short result = (short)(is.getDurability() + this.addFire);
                  if (result >= 65) {
                     e.getPlayer().setItemInHand((ItemStack)null);
                  } else {
                     is.setDurability(result);
                  }
               }
            } else if (id >= 290 && id <= 294 && e.getBlockFace().equals(BlockFace.UP) && e.getClickedBlock() != null) {
               int blockId = e.getClickedBlock().getTypeId();
               if (blockId == 2 || blockId == 3) {
                  this.toolUseMore(e.getPlayer(), blockId);
               }
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerFish(PlayerFishEvent e) {
      ItemStack is = e.getPlayer().getItemInHand();
      if (is != null && is.getTypeId() != 0) {
         if (e.getState().equals(State.CAUGHT_ENTITY) || e.getState().equals(State.CAUGHT_FISH) || e.getState().equals(State.FAILED_ATTEMPT)) {
            int level = is.getEnchantmentLevel(ENCH);
            if (level > 3) {
               level = 3;
            }

            if (this.r.nextInt(100) < this.chanceFish && this.r.nextInt(1 + level) < 1) {
               short result = (short)(is.getDurability() + this.addFish);
               if (result >= 65) {
                  e.getPlayer().setItemInHand((ItemStack)null);
               } else {
                  is.setDurability(result);
               }
            }
         }

      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onEntityShootBow(EntityShootBowEvent e) {
      if (e.getEntity() instanceof Player) {
         Player p = (Player)e.getEntity();
         ItemStack is = p.getItemInHand();
         if (is != null && is.getTypeId() == 261) {
            int level = is.getEnchantmentLevel(ENCH);
            if (level > 3) {
               level = 3;
            }

            if (this.r.nextInt(100) < this.chanceBow && this.r.nextInt(1 + level) < 1) {
               short result = (short)(is.getDurability() + this.addBow);
               if (result >= 385) {
                  p.setItemInHand((ItemStack)null);
               } else {
                  is.setDurability(result);
               }
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerShearEntity(PlayerShearEntityEvent e) {
      this.toolUseMore(e.getPlayer(), 0);
   }

   public void toolUseMore(Player p, int id) {
      ItemStack is = p.getItemInHand();
      int toolId = is.getTypeId();
      if (typeHash.containsKey(toolId)) {
         int type = (Integer)typeHash.get(toolId);
         int level = is.getEnchantmentLevel(ENCH);
         if (level > 3) {
            level = 3;
         }

         int add = 0;
         if (this.addConsumeHash.containsKey(id) && this.r.nextInt(1 + level) < 1) {
            add += (Integer)this.addConsumeHash.get(id);
         }

         if (this.r.nextInt(100) < (Integer)this.chanceHash.get(type) && this.r.nextInt(1 + level) < 1) {
            add += (Integer)this.addHash.get(type);
         }

         if (add > 0) {
            short result = (short)(is.getDurability() + add);
            if (result >= (Integer)maxDurabilityHash.get(type)) {
               p.setItemInHand((ItemStack)null);
            } else {
               is.setDurability(result);
            }
         }

      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.addConsumeHash = new HashMap();

      for(String s : config.getStringList("addConsume")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         int add = Integer.parseInt(s.split(" ")[1]);
         this.addConsumeHash.put(id, add);
      }

      this.chanceFire = config.getInt("chanceFire");
      this.addFire = config.getInt("addFire");
      this.chanceFish = config.getInt("chanceFish");
      this.addFish = config.getInt("addFish");
      this.chanceBow = config.getInt("chanceBow");
      this.addBow = config.getInt("addBow");
      this.chanceHash = new HashMap();
      this.chanceHash.put(1, config.getInt("chanceWood"));
      this.chanceHash.put(2, config.getInt("chanceStone"));
      this.chanceHash.put(3, config.getInt("chanceIron"));
      this.chanceHash.put(4, config.getInt("chanceGold"));
      this.chanceHash.put(5, config.getInt("chanceDiamond"));
      this.chanceHash.put(6, config.getInt("chanceShears"));
      this.addHash = new HashMap();
      this.addHash.put(1, config.getInt("addWood"));
      this.addHash.put(2, config.getInt("addStone"));
      this.addHash.put(3, config.getInt("addIron"));
      this.addHash.put(4, config.getInt("addGold"));
      this.addHash.put(5, config.getInt("addDiamond"));
      this.addHash.put(6, config.getInt("addShears"));
   }
}
