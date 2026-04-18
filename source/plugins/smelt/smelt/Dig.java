package smelt;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import lib.config.ReloadConfigEvent;
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

public class Dig implements Listener {
   private Random r = new Random();
   private Server server;
   private String pn;
   private Star star;
   private Jd jd;
   private String per_smelt_vip;
   private int vipAdd;
   private String check;
   private HashMap typeHash;
   private HashMap chanceHash;
   private int tipLevel;

   public Dig(Main main) {
      super();
      this.server = main.getServer();
      this.pn = main.getPn();
      this.star = main.getStar();
      this.jd = main.getJd();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, main);
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
   public void onPlayerInteract(PlayerInteractEvent e) {
      if (!Main.isIgnored(e.getClickedBlock())) {
         if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && this.isDig(e.getPlayer().getItemInHand())) {
            e.setCancelled(true);
            ItemStack is = e.getPlayer().getInventory().getItem(0);
            if (is == null || is.getTypeId() == 0) {
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(145)}));
               return;
            }

            int id = e.getPlayer().getItemInHand().getTypeId();

            try {
               if (!UtilTypes.checkItem(this.pn, (String)this.typeHash.get(id), String.valueOf(is.getTypeId()))) {
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "digErr", new Object[]{e.getPlayer().getItemInHand().getItemMeta().getDisplayName(), UtilNames.getItemName(is.getTypeId(), is.getDurability())}));
                  return;
               }
            } catch (InvalidTypeException e1) {
               e1.printStackTrace();
               return;
            }

            StarInfo starInfo = this.star.getStarInfo(is);
            if (starInfo == null) {
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(150)}));
               return;
            }

            if (starInfo.getTotal() - starInfo.getFill() <= 0) {
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(155)}));
               return;
            }

            int level = starInfo.getTotal();
            if (!this.chanceHash.containsKey(level)) {
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(160)}));
               return;
            }

            if (this.jd.getMaxStar(id) <= level) {
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(170)}));
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

            int chance = (Integer)this.chanceHash.get(level) + add;
            if (this.r.nextInt(100) < chance) {
               starInfo.setTotal(level + 1);
               this.star.setStar(is, starInfo);
               e.getPlayer().updateInventory();
               String vip = this.get(45);
               if (UtilPer.hasPer(e.getPlayer(), this.per_smelt_vip)) {
                  vip = "";
               }

               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "digSuccess", new Object[]{vip, this.vipAdd}));
               if (level + 1 >= this.tipLevel) {
                  this.server.broadcastMessage(UtilFormat.format(this.pn, "digTip", new Object[]{e.getPlayer().getName(), level + 1, UtilNames.getItemName(is.getTypeId(), 0)}));
               }

               e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.ORB_PICKUP, 2.0F, 1.0F);
               return;
            }

            starInfo.setTotal(level - 1);
            this.star.setStar(is, starInfo);
            e.getPlayer().updateInventory();
            String vip = this.get(45);
            if (UtilPer.hasPer(e.getPlayer(), this.per_smelt_vip)) {
               vip = "";
            }

            e.getPlayer().sendMessage(UtilFormat.format(this.pn, "digFail", new Object[]{vip, this.vipAdd}));
            e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.ZOMBIE_METAL, 1.8F, 1.0F);
         }

      }
   }

   private boolean isDig(ItemStack is) {
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
      this.vipAdd = config.getInt("dig.vipAdd");
      this.check = Util.convert(config.getString("dig.check"));
      this.typeHash = new HashMap();

      for(String s : config.getStringList("dig.type")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         String type = s.split(" ")[1];
         this.typeHash.put(id, type);
      }

      this.chanceHash = new HashMap();

      for(String s : config.getStringList("dig.chance")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         int chance = Integer.parseInt(s.split(" ")[1]);
         this.chanceHash.put(id, chance);
      }

      this.tipLevel = config.getInt("dig.tipLevel");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
