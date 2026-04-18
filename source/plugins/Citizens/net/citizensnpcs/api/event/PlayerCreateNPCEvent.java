package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class PlayerCreateNPCEvent extends CommandSenderCreateNPCEvent implements Cancellable {
   private static final HandlerList handlers = new HandlerList();

   public PlayerCreateNPCEvent(Player player, NPC npc) {
      super(player, npc);
   }

   public Player getCreator() {
      return (Player)super.getCreator();
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
