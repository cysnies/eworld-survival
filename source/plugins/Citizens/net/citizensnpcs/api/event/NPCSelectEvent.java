package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

public class NPCSelectEvent extends NPCEvent {
   private final CommandSender sender;
   private static final HandlerList handlers = new HandlerList();

   public NPCSelectEvent(NPC npc, CommandSender sender) {
      super(npc);
      this.sender = sender;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public CommandSender getSelector() {
      return this.sender;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
