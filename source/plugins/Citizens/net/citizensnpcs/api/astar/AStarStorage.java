package net.citizensnpcs.api.astar;

public interface AStarStorage {
   void close(AStarNode var1);

   AStarNode getBestNode();

   void open(AStarNode var1);

   AStarNode removeBestNode();

   boolean shouldExamine(AStarNode var1);
}
