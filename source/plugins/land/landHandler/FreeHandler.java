package landHandler;

import java.util.HashMap;
import java.util.List;
import land.Land;
import land.LandUser;
import land.Pos;
import land.Range;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import lib.util.UtilSpeed;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class FreeHandler implements Listener {
   private static final String PER_FREE = "per.land.freeCheck";
   private static final int DEFAUTL_LEVEL = 100;
   private static final String SPEED_FREE = "free";
   private String pn;
   private LandManager lm;
   private boolean enable;
   private String per;
   private int checkId;
   private String check;
   private int interval;
   private HashList worlds;
   private int radius;
   private String namePrefix;
   private HashMap addFlagsHash;

   public FreeHandler(LandManager lm) {
      super();
      this.pn = lm.getLandMain().getPn();
      this.lm = lm;
      this.loadConfig(UtilConfig.getConfig(this.pn));
      lm.registerEvents(this);
      UtilSpeed.register(this.pn, "free");
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
   public void onPlayerInteract(PlayerInteractEvent e) {
      if (e.getAction().equals(Action.LEFT_CLICK_BLOCK) && this.isFreeBar(e.getPlayer().getItemInHand())) {
         e.setCancelled(true);
         if (!UtilSpeed.check(e.getPlayer(), this.pn, "free", this.interval)) {
            return;
         }

         this.get(e.getPlayer(), e.getClickedBlock());
      }

   }

   public void get(Player p, Block b) {
      if (b != null) {
         if (UtilPer.checkPer(p, this.per)) {
            if (this.hasFree(p)) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(1700)}));
            } else if (!this.worlds.has(b.getWorld().getName())) {
               p.sendMessage(this.get(1705));
            } else {
               HashList<Land> landList = this.lm.getUserLands(p.getName());
               HashMap<String, LandUser> userHash = this.lm.getLandUserHandler().getUserHash();
               if (landList != null && landList.size() >= ((LandUser)userHash.get(p.getName())).getMaxLands()) {
                  p.sendMessage(UtilFormat.format(this.pn, "landMax", new Object[]{((LandUser)userHash.get(p.getName())).getMaxLands()}));
               } else {
                  String world = b.getWorld().getName();
                  Location l = b.getLocation();
                  int x = l.getBlockX();
                  int y = l.getBlockY();
                  int z = l.getBlockZ();
                  Pos p1 = new Pos(world, x - this.radius, y - this.radius, z - this.radius);
                  Pos p2 = new Pos(world, x + this.radius, y + this.radius, z + this.radius);
                  Range range = new Range(p1, p2);

                  for(Land land : this.lm.getLandCheck().getCollisionLands(range)) {
                     if (!land.isOverlap()) {
                        p.sendMessage(UtilFormat.format(this.pn, "landOverlap2", new Object[]{land.getName(), land.getId()}));
                        return;
                     }
                  }

                  UtilPer.add(p, "per.land.freeCheck");
                  String landName = this.namePrefix + p.getName();
                  if (this.lm.getLand(landName) != null) {
                     landName = null;
                  }

                  Land land;
                  if (landName == null) {
                     land = LandManager.createLand(1, false, p.getName(), range, 100);
                  } else {
                     land = LandManager.createLand(1, false, landName, p.getName(), range, 100);
                  }

                  for(String flag : this.addFlagsHash.keySet()) {
                     p.sendMessage(UtilFormat.format(this.pn, "landShow7", new Object[]{flag, this.addFlagsHash.get(flag)}));
                     this.lm.getFlagHandler().addFlag(land, flag, (Integer)this.addFlagsHash.get(flag));
                  }

                  this.lm.getTpHandler().setTp(land, Pos.toLoc(range.getCenter()));
                  p.sendMessage(UtilFormat.format((String)null, "tip", new Object[]{this.get(1215)}));
                  p.sendMessage(UtilFormat.format(this.pn, "createFree", new Object[]{land.getName()}));
               }
            }
         }
      }
   }

   public boolean hasFree(Player p) {
      return UtilPer.hasPer(p, "per.land.freeCheck");
   }

   public boolean isEnable() {
      return this.enable;
   }

   private boolean isFreeBar(ItemStack is) {
      if (is != null && is.getTypeId() == this.checkId && is.hasItemMeta()) {
         List<String> lore = is.getItemMeta().getLore();
         if (lore != null && lore.size() >= 1 && ((String)lore.get(0)).equals(this.check)) {
            return true;
         }
      }

      return false;
   }

   private void loadConfig(YamlConfiguration config) {
      this.enable = config.getBoolean("free.enable");
      this.per = config.getString("free.per");
      this.checkId = UtilItems.getItem(this.pn, "sel_free").getTypeId();
      this.check = Util.convert(config.getString("free.check"));
      this.interval = config.getInt("free.interval");
      this.worlds = new HashListImpl();

      for(String s : config.getStringList("free.worlds")) {
         this.worlds.add(s);
      }

      this.radius = config.getInt("free.radius");
      this.namePrefix = config.getString("free.namePrefix");
      this.addFlagsHash = new HashMap();

      for(String s : config.getStringList("create.addFlags")) {
         String flag = s.split(":")[0];
         int value = Integer.parseInt(s.split(":")[1]);
         this.addFlagsHash.put(flag, value);
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
