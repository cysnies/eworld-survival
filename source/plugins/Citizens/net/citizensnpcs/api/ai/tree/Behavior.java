package net.citizensnpcs.api.ai.tree;

import org.bukkit.event.Listener;

public interface Behavior extends Listener {
   void reset();

   BehaviorStatus run();

   boolean shouldExecute();
}
