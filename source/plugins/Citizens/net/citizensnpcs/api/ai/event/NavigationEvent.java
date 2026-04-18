package net.citizensnpcs.api.ai.event;

import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.event.Event;

public abstract class NavigationEvent extends Event {
   private final Navigator navigator;

   protected NavigationEvent(Navigator navigator) {
      super();
      this.navigator = navigator;
   }

   public Navigator getNavigator() {
      return this.navigator;
   }

   public NPC getNPC() {
      return this.navigator.getNPC();
   }
}
