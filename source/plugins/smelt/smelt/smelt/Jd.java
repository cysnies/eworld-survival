package smelt;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import lib.config.ReloadConfigEvent;
import lib.hashList.ChanceHashList;
import lib.hashList.ChanceHashListImpl;
import lib.types.InvalidTypeException;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilNames;
import lib.util.UtilPer;
import lib.util.UtilTypes;
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

public class Jd implements Listener {
   private Main main;
   private Random r;
   private Server server;
   private String pn;
   private Star star;
   private String per_smelt_vip;
   private int vipAdd;
   private String check;
   private HashMap itemInfoHash;
   private int tipLevel;
   private ChanceHashList levels;

   public Jd(Main main) {
      super();
      this.main = main;
      this.r = new Random();
      this.server = main.getServer();
      this.pn = main.getPn();
      this.star = main.getStar();
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
         if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && this.isJd(e.getPlayer().getItemInHand())) {
            e.setCancelled(true);
            ItemStack is = e.getPlayer().getInventory().getItem(0);
            if (is == null || is.getTypeId() == 0) {
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(80)}));
               return;
            }

            ItemInfo itemInfo = (ItemInfo)this.itemInfoHash.get(e.getPlayer().getItemInHand().getTypeId());
            if (itemInfo == null) {
               return;
            }

            try {
               if (!UtilTypes.checkItem(this.pn, itemInfo.getType(), String.valueOf(is.getTypeId()))) {
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "jdErr", new Object[]{e.getPlayer().getItemInHand().getItemMeta().getDisplayName(), UtilNames.getItemName(is.getTypeId(), is.getDurability())}));
                  return;
               }
            } catch (InvalidTypeException e1) {
               e1.printStackTrace();
               return;
            }

            StarInfo starInfo = this.star.getStarInfo(is);
            if (starInfo != null) {
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(85)}));
               return;
            }

            if (this.main.getGem().hasEffect(is)) {
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(86)}));
               return;
            }

            if (e.getPlayer().getItemInHand().getAmount() <= 1) {
               e.getPlayer().setItemInHand((ItemStack)null);
            } else {
               e.getPlayer().getItemInHand().setAmount(e.getPlayer().getItemInHand().getAmount() - 1);
            }

            int add = 0;
            if (UtilPer.hasPer(e.getPlayer(), this.per_smelt_vip)) {
               add = this.vipAdd;
            }

            String vip = this.get(45);
            if (UtilPer.hasPer(e.getPlayer(), this.per_smelt_vip)) {
               vip = "";
            }

            if (this.r.nextInt(100) >= itemInfo.getChance() + add) {
               e.getPlayer().updateInventory();
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "jdFail", new Object[]{vip, this.vipAdd}));
               e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.ZOMBIE_METAL, 1.8F, 1.0F);
               return;
            }

            int newStar = (Integer)this.levels.getRandom();
            if (newStar > itemInfo.getMax()) {
               newStar = itemInfo.getMax();
            }

            StarInfo newStarInfo = new StarInfo(newStar, 0);
            this.star.setStar(is, newStarInfo);
            e.getPlayer().updateInventory();
            e.getPlayer().sendMessage(UtilFormat.format(this.pn, "jd", new Object[]{vip, this.vipAdd}));
            if (newStar >= this.tipLevel) {
               this.server.broadcastMessage(UtilFormat.format(this.pn, "jd1", new Object[]{e.getPlayer().getName(), newStar, UtilNames.getItemName(is.getTypeId(), is.getDurability())}));
            }

            e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.valueOf("SUCCESSFUL_HIT"), 2.0F, 1.0F);
         }

      }
   }

   public int getMaxStar(int id) {
      try {
         return ((ItemInfo)this.itemInfoHash.get(id)).getMax();
      } catch (Exception var3) {
         return 0;
      }
   }

   private boolean isJd(ItemStack is) {
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
      this.vipAdd = config.getInt("jd.vipAdd");
      this.check = Util.convert(config.getString("jd.check"));
      this.itemInfoHash = new HashMap();

      for(int index = 1; config.contains("jd.item" + index); ++index) {
         int id = config.getInt("jd.item" + index + ".id");
         int chance = config.getInt("jd.item" + index + ".chance");
         int max = config.getInt("jd.item" + index + ".max");
         String type = config.getString("jd.item" + index + ".type");
         ItemInfo itemInfo = new ItemInfo(id, chance, max, type);
         this.itemInfoHash.put(id, itemInfo);
      }

      this.tipLevel = config.getInt("jd.tipLevel");
      this.levels = new ChanceHashListImpl();

      for(String s : config.getStringList("jd.levels")) {
         int level = Integer.parseInt(s.split(" ")[0]);
         int chance = Integer.parseInt(s.split(" ")[1]);
         this.levels.addChance(level, chance);
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   class ItemInfo {
      private int id;
      private int chance;
      private int max;
      private String type;

      public ItemInfo(int id, int chance, int max, String type) {
         super();
         this.id = id;
         this.chance = chance;
         this.max = max;
         this.type = type;
      }

      public int getId() {
         return this.id;
      }

      public int getChance() {
         return this.chance;
      }

      public int getMax() {
         return this.max;
      }

      public String getType() {
         return this.type;
      }
   }
}
