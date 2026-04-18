package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class CommandSenderCreateNPCEvent extends NPCCreateEvent implements Cancellable {
   private boolean cancelled;
   private final CommandSender creator;
   private String reason;
   private static final HandlerList handlers = new HandlerList();

   public CommandSenderCreateNPCEvent(CommandSender sender, NPC npc) {
      super(npc);
      this.creator = sender;
   }

   public String getCancelReason() {
      return this.reason;
   }

   public CommandSender getCreator() {
      return this.creator;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancel) {
      this.cancelled = cancel;
   }

   public void setCancelReason(String reason) {
      this.reason = reason;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
