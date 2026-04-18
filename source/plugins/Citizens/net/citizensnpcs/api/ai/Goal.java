package net.citizensnpcs.api.ai;

import org.bukkit.event.Listener;

public interface Goal extends Listener {
   void reset();

   void run(GoalSelector var1);

   boolean shouldExecute(GoalSelector var1);
}
