package clear;

import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.time.TimeEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Liquid implements Listener {
   private static final int CHECK_INTERVAL = 200;
   private static final String LIB = "lib";
   private String pn;
   private ServerManager serverManager;
   private boolean enable;
   private int checkInterval;
   private int goodLimit;
   private int goodCancel;
   private int fineLimit;
   private int fineCancel;
   private int badLimit;
   private int badCancel;
   private int unknownLimit;
   private int unknownCancel;
   private HashList liquidBlockList;
   private HashList itemsList;
   private int limit;
   private int cancel;
   private int count;

   public Liquid(Main main) {
      super();
      this.pn = main.getPn();
      this.serverManager = main.getServerManager();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      main.getServer().getPluginManager().registerEvents(this, main);
      this.check();
      main.getServer().getScheduler().scheduleSyncRepeatingTask(main, new Runnable() {
         public void run() {
            Liquid.this.check();
         }
      }, 200L, 200L);
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
   public void onBlockFromTo(BlockFromToEvent e) {
      if (this.enable && this.liquidBlockList.has(e.getBlock().getTypeId()) && !this.flow()) {
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      if (e.hasItem() && this.itemsList.has(e.getItem().getTypeId()) && this.count >= this.limit) {
         e.setCancelled(true);
         e.getPlayer().sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(2000)}));
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent e) {
      if (TimeEvent.getTime() % (long)this.checkInterval == 0L) {
         this.count = 0;
      }

   }

   private boolean flow() {
      ++this.count;
      return this.count < this.cancel;
   }

   private void check() {
      switch (this.serverManager.getServerStatus()) {
         case 0:
            this.limit = this.goodLimit;
            this.cancel = this.goodCancel;
            break;
         case 1:
            this.limit = this.fineLimit;
            this.cancel = this.fineCancel;
            break;
         case 2:
            this.limit = this.badLimit;
            this.cancel = this.badCancel;
            break;
         case 3:
            this.limit = this.unknownLimit;
            this.cancel = this.unknownCancel;
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.enable = config.getBoolean("liquid.enable");
      this.checkInterval = config.getInt("liquid.checkInterval");
      this.goodLimit = config.getInt("liquid.times.good.limit");
      this.goodCancel = config.getInt("liquid.times.good.cancel");
      this.fineLimit = config.getInt("liquid.times.fine.limit");
      this.fineCancel = config.getInt("liquid.times.fine.cancel");
      this.badLimit = config.getInt("liquid.times.bad.limit");
      this.badCancel = config.getInt("liquid.times.bad.cancel");
      this.unknownLimit = config.getInt("liquid.times.unknown.limit");
      this.unknownCancel = config.getInt("liquid.times.unknown.cancel");
      this.liquidBlockList = new HashListImpl();

      for(int id : config.getIntegerList("liquid.liquidBlock")) {
         this.liquidBlockList.add(id);
      }

      this.itemsList = new HashListImpl();

      for(int id : config.getIntegerList("liquid.items")) {
         this.itemsList.add(id);
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
