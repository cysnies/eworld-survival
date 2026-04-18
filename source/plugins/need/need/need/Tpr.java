package need;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.WorldBorder;
import java.util.Random;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.types.InvalidTypeException;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilCosts;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import lib.util.UtilSpeed;
import lib.util.UtilTypes;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Tpr implements Listener {
   private static final String LIB = "lib";
   private Random r = new Random();
   private Main main;
   private Server server;
   private String pn;
   private WorldBorder worldBorder;
   private String per_need_tpr_vip;
   private String per_need_tpr;
   private String per_need_noDelay;
   private String per_need_noCostTpr;
   private int interval;
   private int vipInterval;
   private int delay;
   private HashList worlds;
   private int times;

   public Tpr(Main main) {
      super();
      this.main = main;
      this.server = main.getServer();
      this.pn = main.getPn();
      this.worldBorder = (WorldBorder)this.server.getPluginManager().getPlugin("WorldBorder");
      UtilSpeed.register(this.pn, "tpr");
      this.loadConfig(UtilConfig.getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, main);
   }

   public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      try {
         Player p = null;
         if (sender instanceof Player) {
            p = (Player)sender;
         }

         String cmdName = cmd.getName();
         int length = args.length;
         if (cmdName.equalsIgnoreCase("tpr")) {
            if (p == null) {
               sender.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(40)}));
               return;
            }

            if (length != 1 || !args[0].equalsIgnoreCase("?")) {
               if (length == 0) {
                  this.tpr(p, p.getWorld().getName());
                  return;
               }

               if (length == 1) {
                  this.tpr(p, args[0]);
                  return;
               }
            }

            sender.sendMessage(UtilFormat.format("lib", "cmdHelpHeader", new Object[]{this.get(447)}));
            if (UtilPer.hasPer(p, this.per_need_tpr)) {
               sender.sendMessage(UtilFormat.format("lib", "cmdHelpItem", new Object[]{this.get(450), this.get(455)}));
            }
         }
      } catch (NumberFormatException var8) {
         sender.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(415)}));
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.per_need_tpr_vip = config.getString("per_need_tpr_vip");
      this.per_need_tpr = config.getString("per_need_tpr");
      this.per_need_noDelay = config.getString("per_need_noDelay");
      this.per_need_noCostTpr = config.getString("per_need_noCostTpr");
      this.interval = config.getInt("interval");
      this.vipInterval = config.getInt("vipInterval");
      this.delay = config.getInt("delay");
      this.worlds = new HashListImpl();

      for(String s : config.getStringList("tprWorlds")) {
         this.worlds.add(s);
      }

      this.times = config.getInt("times");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   private void tpr(Player p, String worldName) {
      if (this.worldBorder == null) {
         p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(465)}));
      } else {
         World w = this.server.getWorld(worldName);
         if (w == null) {
            p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(460)}));
         } else {
            worldName = w.getName();
            if (!this.worlds.has(worldName)) {
               p.sendMessage(UtilFormat.format(this.pn, "tprErr", new Object[]{worldName}));
            } else {
               int interval = this.interval;
               String vip = "§m";
               if (UtilPer.hasPer(p, this.per_need_tpr_vip)) {
                  interval = this.vipInterval;
                  vip = "";
               }

               if (!UtilSpeed.check(p, this.pn, "tpr", interval)) {
                  p.sendMessage(UtilFormat.format(this.pn, "speedErr", new Object[]{vip}));
               } else {
                  BorderData borderData = this.worldBorder.GetWorldBorder(worldName);
                  if (borderData == null) {
                     p.sendMessage(UtilFormat.format(this.pn, "tprErr2", new Object[]{worldName}));
                  } else {
                     try {
                        if (!UtilPer.hasPer(p, this.per_need_noCostTpr) && !UtilCosts.cost(p, this.pn, "tprCost", false)) {
                           return;
                        }
                     } catch (InvalidTypeException e) {
                        e.printStackTrace();
                        return;
                     }

                     int x = 0;
                     int z = 0;

                     for(int i = 0; i < this.times; ++i) {
                        if (borderData.getShape() != null && borderData.getShape()) {
                           x = (int)(Math.sin((double)this.r.nextInt(360) / (double)180.0F * 3.14) * (double)this.r.nextInt(Math.max(1, borderData.getRadiusX())));
                           z = (int)(Math.cos((double)this.r.nextInt(360) / (double)180.0F * 3.14) * (double)this.r.nextInt(Math.max(1, borderData.getRadiusZ())));
                        } else {
                           x = (int)((double)this.r.nextInt(Math.max(1, borderData.getRadiusX())) + borderData.getX());
                           if (this.r.nextInt(2) == 0) {
                              x = -x;
                           }

                           z = (int)((double)this.r.nextInt(Math.max(1, borderData.getRadiusZ())) + borderData.getZ());
                           if (this.r.nextInt(2) == 0) {
                              z = -z;
                           }
                        }

                        if (!this.isSea(w, x, z)) {
                           break;
                        }
                     }

                     int delay = 0;
                     vip = "";
                     if (!UtilPer.hasPer(p, this.per_need_noDelay)) {
                        delay = this.delay;
                        vip = "§m";
                     }

                     TprDelay tprDelay = new TprDelay(p, w, x, z);
                     this.server.getScheduler().scheduleSyncDelayedTask(this.main, tprDelay, (long)(delay / 50));
                     p.sendMessage(UtilFormat.format(this.pn, "tprDelay", new Object[]{delay / 1000, vip}));
                  }
               }
            }
         }
      }
   }

   private boolean isSea(World w, int x, int z) {
      if (!w.getBlockAt(x, 100, z).getChunk().load(true)) {
         return true;
      } else {
         for(int y = 255; y > 0; --y) {
            int id = w.getBlockTypeIdAt(x, y, z);
            if (id != 0) {
               if (id != 8 && id != 9) {
                  return false;
               }

               return true;
            }
         }

         return true;
      }
   }

   class TprDelay implements Runnable {
      private static final String LIB = "lib";
      private Player p;
      private Location l;
      private World w;
      private int x;
      private int z;

      public TprDelay(Player p, World w, int x, int z) {
         super();
         this.p = p;
         this.l = p.getLocation().clone();
         this.w = w;
         this.x = x;
         this.z = z;
      }

      public void run() {
         try {
            if (this.p == null || !this.p.isOnline() || this.p.isDead()) {
               return;
            }

            if (this.l.distance(this.p.getLocation()) > (double)1.0F) {
               this.p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{Tpr.this.get(470)}));
               return;
            }

            this.p.getWorld().getBlockAt(this.x, 100, this.z).getChunk().load(true);

            for(int y = 255; y > 0; --y) {
               Block b = this.w.getBlockAt(this.x, y, this.z);
               int typeId = b.getTypeId();
               int smallId = b.getData();
               if (!UtilTypes.checkItem((String)null, "safeBlocks", typeId + ":" + smallId)) {
                  Util.tp(this.p, this.w.getBlockAt(this.x, y + 1, this.z).getLocation(), true, true);
                  this.p.sendMessage(UtilFormat.format("lib", "success", new Object[]{Tpr.this.get(475)}));
                  return;
               }
            }
         } catch (InvalidTypeException e) {
            e.printStackTrace();
         }

      }
   }
}
