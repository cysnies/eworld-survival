package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

public class NPCTraitCommandAttachEvent extends NPCEvent {
   private final CommandSender sender;
   private final Class traitClass;
   private static final HandlerList handlers = new HandlerList();

   public NPCTraitCommandAttachEvent(NPC npc, Class traitClass, CommandSender sender) {
      super(npc);
      this.traitClass = traitClass;
      this.sender = sender;
   }

   public CommandSender getCommandSender() {
      return this.sender;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public Class getTraitClass() {
      return this.traitClass;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
