package uk.org.whoami.authme.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class AuthMeTeleportEvent extends CustomEvent {
   private Player player;
   private Location to;
   private Location from;

   public AuthMeTeleportEvent(Player player, Location to) {
      super();
      this.player = player;
      this.from = player.getLocation();
      this.to = to;
   }

   public Player getPlayer() {
      return this.player;
   }

   public void setTo(Location to) {
      this.to = to;
   }

   public Location getTo() {
      return this.to;
   }

   public Location getFrom() {
      return this.from;
   }
}
