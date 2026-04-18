package smelt;

import java.util.HashMap;
import java.util.List;
import lib.config.ReloadConfigEvent;
import lib.types.InvalidTypeException;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilNames;
import lib.util.UtilTypes;
import net.minecraft.server.v1_6_R2.Packet63WorldParticles;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;

public class Spec implements Listener {
   private Main main;
   private Server server;
   private String pn;
   private BukkitScheduler scheduler;
   private Star star;
   private Check c;
   private int interval;
   private int range;
   private String check;
   private String checkType;
   private HashMap specInfoHash;
   private HashMap idHash;

   public Spec(Main main) {
      super();
      this.main = main;
      this.server = main.getServer();
      this.pn = main.getPn();
      this.scheduler = this.server.getScheduler();
      this.star = main.getStar();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, main);
      this.c = new Check();
      this.scheduler.scheduleSyncDelayedTask(main, this.c, (long)this.interval);
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
         if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && this.isSpec(e.getPlayer().getItemInHand())) {
            e.setCancelled(true);
            ItemStack is = e.getPlayer().getInventory().getItem(0);
            if (is == null || is.getTypeId() == 0) {
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(320)}));
               return;
            }

            try {
               if (!UtilTypes.checkItem(this.pn, this.checkType, String.valueOf(is.getTypeId()))) {
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "specErr", new Object[]{e.getPlayer().getItemInHand().getItemMeta().getDisplayName(), UtilNames.getItemName(is.getTypeId(), 0)}));
                  return;
               }
            } catch (InvalidTypeException e1) {
               e1.printStackTrace();
               return;
            }

            if (this.star.getStarInfo(is) == null) {
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(330)}));
               return;
            }

            if (this.getSpec(is) != null) {
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(325)}));
               return;
            }

            SpecInfo si = this.getStoneSpec(e.getPlayer().getItemInHand());
            if (si == null) {
               return;
            }

            if (e.getPlayer().getItemInHand().getAmount() <= 1) {
               e.getPlayer().setItemInHand((ItemStack)null);
            } else {
               e.getPlayer().getItemInHand().setAmount(e.getPlayer().getItemInHand().getAmount() - 1);
            }

            this.addSpec(is, si);
            e.getPlayer().updateInventory();
            e.getPlayer().sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(335)}));
         }

      }
   }

   private void addSpec(ItemStack is, SpecInfo si) {
      ItemMeta im = is.getItemMeta();
      List<String> lore = im.getLore();
      lore.add(1, si.getCheck());
      im.setLore(lore);
      is.setItemMeta(im);
   }

   private boolean isSpec(ItemStack is) {
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

   private SpecInfo getSpec(ItemStack is) {
      if (is != null && is.getTypeId() != 0) {
         ItemMeta im = is.getItemMeta();
         if (im != null) {
            List<String> lore = im.getLore();
            if (lore != null && lore.size() > 1) {
               return (SpecInfo)this.specInfoHash.get(lore.get(1));
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private SpecInfo getStoneSpec(ItemStack is) {
      try {
         if (this.isSpec(is)) {
            ItemMeta im = is.getItemMeta();
            List<String> lore = im.getLore();
            if (lore.size() > 1) {
               int id = Integer.parseInt(((String)lore.get(1)).split(" ")[1]);
               SpecInfo specInfo = (SpecInfo)this.idHash.get(id);
               return specInfo;
            }
         }
      } catch (Exception var6) {
      }

      return null;
   }

   private void loadConfig(FileConfiguration config) {
      this.interval = config.getInt("spec.interval");
      this.range = config.getInt("spec.range");
      this.check = Util.convert(config.getString("spec.check"));
      this.checkType = config.getString("spec.checkType");
      this.specInfoHash = new HashMap();
      this.idHash = new HashMap();

      for(int index = 1; config.contains("spec.spec" + index); ++index) {
         int id = config.getInt("spec.spec" + index + ".id");
         String check = Util.convert(config.getString("spec.spec" + index + ".check"));
         String name = config.getString("spec.spec" + index + ".name");
         float offset = (float)config.getDouble("spec.spec" + index + ".offset");
         float speed = (float)config.getDouble("spec.spec" + index + ".speed");
         int count = config.getInt("spec.spec" + index + ".count");
         String type = config.getString("spec.spec" + index + ".type");
         SpecInfo specInfo = new SpecInfo(id, check, name, offset, speed, count, type);
         this.specInfoHash.put(check, specInfo);
         this.idHash.put(id, specInfo);
      }

   }

   private void check() {
      try {
         Player[] var4;
         for(Player p : var4 = this.server.getOnlinePlayers()) {
            try {
               SpecInfo si = this.getSpec(p.getItemInHand());
               if (si != null) {
                  Location l = p.getLocation();
                  Packet63WorldParticles packet = new Packet63WorldParticles(si.getName(), (float)l.getX(), (float)l.getY(), (float)l.getZ(), si.getOffset(), si.getOffset(), si.getOffset(), si.getSpeed(), si.getCount());
                  ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);

                  for(Entity e : p.getNearbyEntities((double)this.range, (double)this.range, (double)this.range)) {
                     if (e instanceof Player) {
                        Player tar = (Player)e;
                        if (!tar.getName().equals(p.getName())) {
                           ((CraftPlayer)tar).getHandle().playerConnection.sendPacket(packet);
                        }
                     }
                  }
               }
            } catch (Exception var11) {
            }
         }
      } catch (Exception var12) {
      }

      this.scheduler.scheduleSyncDelayedTask(this.main, this.c, (long)this.interval);
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   class Check implements Runnable {
      Check() {
         super();
      }

      public void run() {
         Spec.this.check();
      }
   }
}
