package net.citizensnpcs.api.ai.goals;

import java.util.Random;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;

public class WanderGoal extends BehaviorGoalAdapter {
   private boolean forceFinish;
   private final NPC npc;
   private final Random random = new Random();
   private final int xrange;
   private final int yrange;

   private WanderGoal(NPC npc, int xrange, int yrange) {
      super();
      this.npc = npc;
      this.xrange = xrange;
      this.yrange = yrange;
   }

   private Location findRandomPosition() {
      Location base = this.npc.getBukkitEntity().getLocation();
      Location found = null;

      for(int i = 0; i < 10; ++i) {
         int x = base.getBlockX() + this.random.nextInt(2 * this.xrange) - this.xrange;
         int y = base.getBlockY() + this.random.nextInt(2 * this.yrange) - this.yrange;
         int z = base.getBlockZ() + this.random.nextInt(2 * this.xrange) - this.xrange;
         Block block = base.getWorld().getBlockAt(x, y - 2, z);
         if (MinecraftBlockExaminer.canStandOn(block)) {
            found = block.getLocation().add((double)0.0F, (double)1.0F, (double)0.0F);
            break;
         }
      }

      return found;
   }

   @EventHandler
   public void onFinish(NavigationCompleteEvent event) {
      this.forceFinish = true;
   }

   public void reset() {
      this.forceFinish = false;
   }

   public BehaviorStatus run() {
      return this.npc.getNavigator().isNavigating() && !this.forceFinish ? BehaviorStatus.RUNNING : BehaviorStatus.SUCCESS;
   }

   public boolean shouldExecute() {
      if (this.npc.isSpawned() && !this.npc.getNavigator().isNavigating()) {
         Location dest = this.findRandomPosition();
         if (dest == null) {
            return false;
         } else {
            this.npc.getNavigator().setTarget(dest);
            return true;
         }
      } else {
         return false;
      }
   }

   public static WanderGoal createWithNPC(NPC npc) {
      return createWithNPCAndRange(npc, 10, 2);
   }

   public static WanderGoal createWithNPCAndRange(NPC npc, int xrange, int yrange) {
      return new WanderGoal(npc, xrange, yrange);
   }
}
