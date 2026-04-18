package net.citizensnpcs.api.astar.pathfinder;

public interface BlockExaminer {
   float getCost(BlockSource var1, PathPoint var2);

   boolean isPassable(BlockSource var1, PathPoint var2);
}
