package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;

public abstract class NPCEvent extends CitizensEvent {
   final NPC npc;

   protected NPCEvent(NPC npc) {
      super();
      this.npc = npc;
   }

   public NPC getNPC() {
      return this.npc;
   }
}
