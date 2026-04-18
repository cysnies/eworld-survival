package net.citizensnpcs.api.astar.pathfinder;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public interface PathPoint {
   void addCallback(PathCallback var1);

   Vector getVector();

   public interface PathCallback {
      void run(NPC var1, Block var2);
   }
}
