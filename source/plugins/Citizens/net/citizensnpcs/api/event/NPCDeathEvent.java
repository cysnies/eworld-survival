package net.citizensnpcs.api.event;

import java.util.List;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDeathEvent;

public class NPCDeathEvent extends NPCEvent {
   private final EntityDeathEvent event;
   private static final HandlerList handlers = new HandlerList();

   public NPCDeathEvent(NPC npc, EntityDeathEvent event) {
      super(npc);
      this.event = event;
   }

   public int getDroppedExp() {
      return this.event.getDroppedExp();
   }

   public List getDrops() {
      return this.event.getDrops();
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public void setDroppedExp(int exp) {
      this.event.setDroppedExp(exp);
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
