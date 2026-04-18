package net.citizensnpcs.trait.waypoint;

import java.util.Iterator;
import net.citizensnpcs.api.event.CitizensEvent;
import org.bukkit.event.HandlerList;

public class LinearWaypointsCompleteEvent extends CitizensEvent {
   private Iterator next;
   private final WaypointProvider provider;
   private static final HandlerList handlers = new HandlerList();

   public LinearWaypointsCompleteEvent(WaypointProvider provider, Iterator next) {
      super();
      this.next = next;
      this.provider = provider;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public Iterator getNextWaypoints() {
      return this.next;
   }

   public WaypointProvider getWaypointProvider() {
      return this.provider;
   }

   public void setNextWaypoints(Iterator waypoints) {
      this.next = waypoints;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
