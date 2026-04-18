package fr.neatmonster.nocheatplus.checks.moving;

import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.utilities.TeleportUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class VehicleSetBack implements Runnable {
   private final Entity vehicle;
   private final Player player;
   private final Location location;
   private final boolean debug;

   public VehicleSetBack(Entity vehicle, Player player, Location location, boolean debug) {
      super();
      this.vehicle = vehicle;
      this.player = player;
      this.location = location;
      this.debug = debug;
   }

   public void run() {
      MovingData data = MovingData.getData(this.player);
      data.morePacketsVehicleTaskId = -1;

      try {
         data.setTeleported(this.location);
         TeleportUtil.teleport(this.vehicle, this.player, this.location, this.debug);
      } catch (Throwable t) {
         LogUtil.logSevere(t);
      }

   }
}
