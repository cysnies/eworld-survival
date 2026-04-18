package sit;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import java.io.File;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;
import land.Land;
import landMain.LandMain;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.time.TimeEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.material.Stairs;
import org.bukkit.plugin.java.JavaPlugin;

public class Sit extends JavaPlugin implements Listener {
   private static final String LIB = "lib";
   private static final long DELAY = 2000L;
   private static final double MOVE_MAX = 0.8;
   private static final String FLAG_BAN_SIT = "banSit";
   private Random r;
   private String pn;
   private Server server;
   private String dataFolder;
   private String pluginVersion;
   private String pluginPath;
   private ProtocolManager protocolManager;
   private String per_sit_admin;
   private String per_sit_use;
   private boolean sneaking;
   private boolean rotate;
   private double distance;
   private double sittingheight;
   private boolean interactCancel;
   private int interval;
   private int chance;
   private int from;
   private int to;
   private int min;
   private int max;
   private int player;
   private int fix;
   private HashList canSitList;
   private HashList notObstacle;
   private HashMap openSitHash;
   private HashMap playerHash = new HashMap();
   private HashMap locHash = new HashMap();
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$org$bukkit$block$BlockFace;

   public Sit() {
      super();
   }

   public void onEnable() {
      this.r = new Random();
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, this);
      this.protocolManager = ProtocolLibrary.getProtocolManager();
      this.openSitHash = new HashMap();
      this.playerHash = new HashMap();
      this.locHash = new HashMap();
      Util.sendConsoleMessage(UtilFormat.format("lib", "pluginEnabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public void onDisable() {
      Player[] var4;
      for(Player p : var4 = this.server.getOnlinePlayers()) {
         this.leave(p, (Location)this.playerHash.get(p), false);
      }

      Util.sendConsoleMessage(UtilFormat.format("lib", "pluginDisabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      int length = args.length;
      if (length == 0) {
         if (p == null) {
            sender.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(35)}));
         } else {
            this.toggleSit(p);
         }

         return true;
      } else if (length == 1 && args[0].equalsIgnoreCase("reload")) {
         this.reloadConfig(sender);
         return true;
      } else {
         sender.sendMessage(UtilFormat.format("lib", "cmdHelpHeader", new Object[]{this.get(10)}));
         if (p == null || UtilPer.hasPer(p, this.per_sit_admin)) {
            sender.sendMessage(UtilFormat.format("lib", "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
         }

         return true;
      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig0(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onTime(TimeEvent e) {
      if (TimeEvent.getTime() % (long)this.interval == 0L) {
         this.addGold();
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      this.join(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.quit(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void playerInteract(PlayerInteractEvent e) {
      if (this.interactCancel && this.isInSit(e.getPlayer())) {
         this.leave(e.getPlayer(), (Location)this.playerHash.get(e.getPlayer()), true);
      } else if (e.getClickedBlock() != null && e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getBlockFace().equals(BlockFace.UP) && this.checkInteract(e.getPlayer(), e.getClickedBlock())) {
         e.setUseInteractedBlock(Result.DENY);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void blockBreak(BlockBreakEvent e) {
      this.checkBlockBreak(e.getBlock().getLocation());
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void playerMove(PlayerMoveEvent e) {
      this.checkPlayerMove(e.getPlayer());
   }

   public boolean isInSit(Player p) {
      return this.playerHash.containsKey(p);
   }

   public boolean isOpenSit(Player p) {
      return !this.openSitHash.containsKey(p) ? false : (Boolean)this.openSitHash.get(p);
   }

   public String getModeShow(Player p) {
      return this.isOpenSit(p) ? this.get(1200) : this.get(1205);
   }

   private void initBasic() {
      this.server = this.getServer();
      this.pn = this.getName();
      this.pluginPath = this.getFile().getParentFile().getAbsolutePath();
      this.dataFolder = this.pluginPath + File.separator + this.pn;
      this.pluginVersion = Util.getPluginVersion(this.getFile());
   }

   private void initConfig() {
      HashList<Pattern> filter = UtilConfig.getDefaultFilter();
      filter.add(Pattern.compile("time.yml"));
      UtilConfig.register(new File(this.pluginPath + File.separator + this.pn + ".jar"), this.dataFolder, filter, this.pn);
      this.loadConfig((CommandSender)null);
   }

   private void reloadConfig(CommandSender sender) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_sit_admin)) {
         this.loadConfig(sender);
      }
   }

   private void loadConfig(CommandSender sender) {
      try {
         if (UtilConfig.loadConfig(this.pn)) {
            if (sender != null) {
               sender.sendMessage(UtilFormat.format("lib", "success", new Object[]{this.get(25)}));
            }
         } else if (sender != null) {
            sender.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(30)}));
         }
      } catch (InvalidConfigurationException e) {
         if (sender == null) {
            Util.sendConsoleMessage(e.getMessage());
         } else {
            sender.sendMessage(e.getMessage());
         }
      }

   }

   private void addGold() {
      if (this.r.nextInt(100) < this.chance) {
         int online = this.server.getOnlinePlayers().length;

         for(Player p : this.playerHash.keySet()) {
            int add = this.r.nextInt(this.to - this.from + 1) + this.from;
            add += online / this.player * this.fix;
            if (add < this.min) {
               add = this.min;
            } else if (add > this.max) {
               add = this.max;
            }

            UtilEco.add(p.getName(), (double)add);
            p.sendMessage(UtilFormat.format(this.pn, "addGold", new Object[]{add}));
         }
      }

   }

   private void join(Player p) {
      this.openSitHash.put(p, true);
      (new Timer()).schedule(new TimerTask() {
         public void run() {
            Sit.this.sendSit();
         }
      }, 2000L);
   }

   private void quit(Player p) {
      this.openSitHash.remove(p);
      if (this.playerHash.containsKey(p)) {
         this.locHash.remove(this.playerHash.get(p));
         this.playerHash.remove(p);
         Location loc = p.getLocation().clone();
         loc.setY(loc.getY() + (double)1.0F);
         p.teleport(loc, TeleportCause.PLUGIN);
      }

   }

   private void checkBlockBreak(Location l) {
      Player p;
      if ((p = (Player)this.locHash.get(l)) != null && p.isOnline()) {
         this.leave(p, l, true);
      }

   }

   private void checkPlayerMove(Player p) {
      if (this.playerHash.containsKey(p)) {
         Location from = p.getLocation();
         Location to = (Location)this.playerHash.get(p);
         if (from.getWorld() == to.getWorld()) {
            if (from.distance(to) > 0.8) {
               this.locHash.remove(this.playerHash.get(p));
               this.playerHash.remove(p);
               p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(1170)}));
               this.sendStand(p);
            } else {
               this.sendSit(p);
            }
         } else {
            this.locHash.remove(this.playerHash.get(p));
            this.playerHash.remove(p);
            p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(1170)}));
            this.sendStand(p);
         }
      }

   }

   private boolean checkInteract(Player p, Block b) {
      if (b != null && this.canSitList.has(b.getTypeId()) && p != null && this.openSitHash.containsKey(p)) {
         if (!UtilPer.checkPer(p, this.per_sit_use)) {
            return false;
         } else if (b.getRelative(BlockFace.UP).getTypeId() != 0 && !this.notObstacle.has(b.getRelative(BlockFace.UP).getTypeId())) {
            p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(1150)}));
            return false;
         } else if (this.sneaking && !p.isSneaking()) {
            return false;
         } else if (!this.sneaking && p.isSneaking()) {
            return false;
         } else if (this.distance > (double)0.0F && p.getLocation().distance(b.getLocation().add((double)0.5F, (double)0.0F, (double)0.5F)) > this.distance) {
            p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(1190)}));
            return false;
         } else if (!(b.getState().getData() instanceof Stairs)) {
            return false;
         } else {
            Stairs stairs = (Stairs)b.getState().getData();
            if (stairs.isInverted()) {
               return false;
            } else {
               Location l = b.getLocation();
               if (this.locHash.containsKey(l) && !((Player)this.locHash.get(l)).equals(p)) {
                  p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(1160)}));
                  return false;
               } else if (p.getVehicle() != null) {
                  p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(1195)}));
                  return false;
               } else if (this.playerHash.containsKey(p)) {
                  this.leave(p, (Location)this.playerHash.get(p), true);
                  p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(1170)}));
                  return true;
               } else {
                  Land land = LandMain.getLandManager().getHighestPriorityLand(l);
                  if (land != null && land.hasFlag("banSit") && !land.hasPer("banSit", p.getName())) {
                     p.sendMessage(UtilFormat.format(this.pn, "tip1", new Object[]{"banSit"}));
                     return true;
                  } else {
                     if (this.rotate) {
                        Location plocation = l.clone();
                        plocation.add((double)0.5F, this.sittingheight - (double)0.5F, (double)0.5F);
                        switch (stairs.getDescendingDirection()) {
                           case NORTH:
                              plocation.setYaw(180.0F);
                              break;
                           case EAST:
                              plocation.setYaw(-90.0F);
                              break;
                           case SOUTH:
                              plocation.setYaw(0.0F);
                              break;
                           case WEST:
                              plocation.setYaw(90.0F);
                        }

                        p.teleport(plocation);
                     } else {
                        Location plocation = l.clone();
                        plocation.setYaw(p.getLocation().getYaw());
                        p.teleport(plocation.add((double)0.5F, this.sittingheight - (double)0.5F, (double)0.5F));
                     }

                     p.setSneaking(true);
                     this.playerHash.put(p, l);
                     this.locHash.put(l, p);
                     p.sendMessage(UtilFormat.format("lib", "success", new Object[]{this.get(1165)}));
                     (new Timer()).schedule(new TimerTask() {
                        public void run() {
                           Sit.this.sendSit();
                        }
                     }, 2000L);
                     return true;
                  }
               }
            }
         }
      } else {
         return false;
      }
   }

   public void toggleSit(Player p) {
      if (UtilPer.checkPer(p, this.per_sit_use)) {
         if (this.openSitHash.containsKey(p)) {
            this.openSitHash.remove(p);
            p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(1175)}));
         } else {
            this.openSitHash.put(p, true);
            p.sendMessage(UtilFormat.format("lib", "success", new Object[]{this.get(1180)}));
         }

      }
   }

   private void loadConfig0(FileConfiguration config) {
      this.per_sit_admin = config.getString("per_sit_admin");
      this.per_sit_use = config.getString("per_sit_use");
      this.sneaking = config.getBoolean("sit.sneaking");
      this.rotate = config.getBoolean("sit.rotate");
      this.distance = config.getDouble("sit.distance");
      this.sittingheight = config.getDouble("sit.sittingheight");
      this.interactCancel = config.getBoolean("sit.interactCancel");
      this.interval = config.getInt("sit.addGold.interval");
      this.chance = config.getInt("sit.addGold.chance");
      this.from = config.getInt("sit.addGold.amount.from");
      this.to = config.getInt("sit.addGold.amount.to");
      this.min = config.getInt("sit.addGold.amount.min");
      this.max = config.getInt("sit.addGold.amount.max");
      this.player = config.getInt("sit.addGold.amount.player");
      this.fix = config.getInt("sit.addGold.amount.fix");
      this.canSitList = new HashListImpl();
      this.notObstacle = new HashListImpl();

      for(int i : config.getIntegerList("sit.canSitList")) {
         this.canSitList.add(i);
      }

      for(int i : config.getIntegerList("sit.notObstacle")) {
         this.notObstacle.add(i);
      }

   }

   private void leave(Player p, Location l, boolean send) {
      if (this.playerHash.containsKey(p)) {
         this.playerHash.remove(p);
         this.locHash.remove(l);
         Location loc = p.getLocation().clone();
         loc.setY(loc.getY() + (double)1.0F);
         p.teleport(loc, TeleportCause.PLUGIN);
         p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(1170)}));
         if (send) {
            this.sendStand(p);
         }

      }
   }

   private void sendSit() {
      try {
         for(Player p : this.playerHash.keySet()) {
            if (p != null && p.isOnline()) {
               this.sendSit(p);
            }
         }
      } catch (Exception var3) {
      }

   }

   private void sendSit(Player p) {
      PacketContainer fakeSit = this.protocolManager.createPacket(40);
      fakeSit.getSpecificModifier(Integer.TYPE).write(0, p.getEntityId());
      WrappedDataWatcher watcher = new WrappedDataWatcher();
      watcher.setObject(0, (byte)4);
      fakeSit.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

      Player[] var7;
      for(Player onlinePlayer : var7 = Bukkit.getOnlinePlayers()) {
         try {
            this.protocolManager.sendServerPacket(onlinePlayer, fakeSit);
         } catch (Exception var9) {
         }
      }

   }

   private void sendStand(Player p) {
      PacketContainer fakeSit = this.protocolManager.createPacket(40);
      fakeSit.getSpecificModifier(Integer.TYPE).write(0, p.getEntityId());
      WrappedDataWatcher watcher = new WrappedDataWatcher();
      watcher.setObject(0, (byte)0);
      fakeSit.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

      Player[] var7;
      for(Player onlinePlayer : var7 = Bukkit.getOnlinePlayers()) {
         try {
            this.protocolManager.sendServerPacket(onlinePlayer, fakeSit);
         } catch (Exception var9) {
         }
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$org$bukkit$block$BlockFace() {
      int[] var10000 = $SWITCH_TABLE$org$bukkit$block$BlockFace;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[BlockFace.values().length];

         try {
            var0[BlockFace.DOWN.ordinal()] = 6;
         } catch (NoSuchFieldError var19) {
         }

         try {
            var0[BlockFace.EAST.ordinal()] = 2;
         } catch (NoSuchFieldError var18) {
         }

         try {
            var0[BlockFace.EAST_NORTH_EAST.ordinal()] = 14;
         } catch (NoSuchFieldError var17) {
         }

         try {
            var0[BlockFace.EAST_SOUTH_EAST.ordinal()] = 15;
         } catch (NoSuchFieldError var16) {
         }

         try {
            var0[BlockFace.NORTH.ordinal()] = 1;
         } catch (NoSuchFieldError var15) {
         }

         try {
            var0[BlockFace.NORTH_EAST.ordinal()] = 7;
         } catch (NoSuchFieldError var14) {
         }

         try {
            var0[BlockFace.NORTH_NORTH_EAST.ordinal()] = 13;
         } catch (NoSuchFieldError var13) {
         }

         try {
            var0[BlockFace.NORTH_NORTH_WEST.ordinal()] = 12;
         } catch (NoSuchFieldError var12) {
         }

         try {
            var0[BlockFace.NORTH_WEST.ordinal()] = 8;
         } catch (NoSuchFieldError var11) {
         }

         try {
            var0[BlockFace.SELF.ordinal()] = 19;
         } catch (NoSuchFieldError var10) {
         }

         try {
            var0[BlockFace.SOUTH.ordinal()] = 3;
         } catch (NoSuchFieldError var9) {
         }

         try {
            var0[BlockFace.SOUTH_EAST.ordinal()] = 9;
         } catch (NoSuchFieldError var8) {
         }

         try {
            var0[BlockFace.SOUTH_SOUTH_EAST.ordinal()] = 16;
         } catch (NoSuchFieldError var7) {
         }

         try {
            var0[BlockFace.SOUTH_SOUTH_WEST.ordinal()] = 17;
         } catch (NoSuchFieldError var6) {
         }

         try {
            var0[BlockFace.SOUTH_WEST.ordinal()] = 10;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[BlockFace.UP.ordinal()] = 5;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[BlockFace.WEST.ordinal()] = 4;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[BlockFace.WEST_NORTH_WEST.ordinal()] = 11;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[BlockFace.WEST_SOUTH_WEST.ordinal()] = 18;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$org$bukkit$block$BlockFace = var0;
         return var0;
      }
   }
}
