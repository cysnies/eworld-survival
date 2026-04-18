package net.citizensnpcs.api.astar;

public interface AStarGoal {
   float g(AStarNode var1, AStarNode var2);

   float getInitialCost(AStarNode var1);

   float h(AStarNode var1);

   boolean isFinished(AStarNode var1);
}
