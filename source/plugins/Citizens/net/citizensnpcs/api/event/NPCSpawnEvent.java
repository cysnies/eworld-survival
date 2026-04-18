package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class NPCSpawnEvent extends NPCEvent implements Cancellable {
   private boolean cancelled = false;
   private final Location location;
   private static final HandlerList handlers = new HandlerList();

   public NPCSpawnEvent(NPC npc, Location location) {
      super(npc);
      this.location = location;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public Location getLocation() {
      return this.location;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
