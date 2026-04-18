package flag;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.IViolationInfo;
import fr.neatmonster.nocheatplus.hooks.NCPHook;
import fr.neatmonster.nocheatplus.hooks.NCPHookManager;
import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BanCheckMoveHandler implements Listener, NCPHook {
   private static final String FLAG_BAN_CHECK_MOVE = "banCheckMove";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public BanCheckMoveHandler(LandManager landManager) {
      super();
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(landManager.getLandMain().getPn()));
      landManager.registerEvents(this);
      landManager.register("banCheckMove", this.tip, this.use, false, false, this.per);
      NCPHookManager.addHook(CheckType.MOVING, this);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public String getHookName() {
      return "banCheckMove";
   }

   public String getHookVersion() {
      return "1.0";
   }

   public boolean onCheckFailure(CheckType arg0, Player p, IViolationInfo arg2) {
      Land land = this.landManager.getHighestPriorityLand(p.getLocation());
      return land != null && land.hasFlag("banCheckMove");
   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banCheckMove.use");
      this.per = config.getString("banCheckMove.per");
      this.tip = config.getString("banCheckMove.tip");
   }
}
