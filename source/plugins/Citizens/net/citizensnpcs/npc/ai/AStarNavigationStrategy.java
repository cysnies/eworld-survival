package net.citizensnpcs.npc.ai;

import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.astar.AStarMachine;
import net.citizensnpcs.api.astar.pathfinder.ChunkBlockSource;
import net.citizensnpcs.api.astar.pathfinder.Path;
import net.citizensnpcs.api.astar.pathfinder.VectorGoal;
import net.citizensnpcs.api.astar.pathfinder.VectorNode;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_6_R2.EntityLiving;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class AStarNavigationStrategy extends AbstractPathStrategy {
   private final Location destination;
   private final NPC npc;
   private final NavigatorParameters params;
   private Path plan;
   private Vector vector;
   private static final AStarMachine ASTAR = AStarMachine.createWithDefaultStorage();
   private static final Location NPC_LOCATION = new Location((World)null, (double)0.0F, (double)0.0F, (double)0.0F);

   AStarNavigationStrategy(NPC npc, Location dest, NavigatorParameters params) {
      super(TargetType.LOCATION);
      this.params = params;
      this.destination = dest;
      this.npc = npc;
      Location location = npc.getBukkitEntity().getEyeLocation();
      this.plan = (Path)ASTAR.runFully(new VectorGoal(dest, (float)params.distanceMargin()), new VectorNode(location, new ChunkBlockSource(location, params.range()), params.examiners()), 50000);
      if (this.plan != null && !this.plan.isComplete()) {
         this.vector = this.plan.getCurrentVector();
      } else {
         this.setCancelReason(CancelReason.STUCK);
      }

   }

   public Location getTargetAsLocation() {
      return this.destination;
   }

   public void stop() {
      this.plan = null;
   }

   public boolean update() {
      if (this.getCancelReason() == null && this.plan != null && !this.plan.isComplete()) {
         if (this.npc.getBukkitEntity().getLocation(NPC_LOCATION).toVector().distanceSquared(this.vector) <= this.params.distanceMargin()) {
            this.plan.update(this.npc);
            if (this.plan.isComplete()) {
               return true;
            }

            this.vector = this.plan.getCurrentVector();
         }

         EntityLiving handle = NMS.getHandle(this.npc.getBukkitEntity());
         double dX = (double)this.vector.getBlockX() - handle.locX;
         double dZ = (double)this.vector.getBlockZ() - handle.locZ;
         double dY = this.vector.getY() - handle.locY;
         double xzDistance = dX * dX + dZ * dZ;
         double distance = xzDistance + dY * dY;
         if (distance > (double)0.0F && dY > (double)0.0F && xzDistance <= 4.205) {
            NMS.setShouldJump(this.npc.getBukkitEntity());
         }

         NMS.setDestination(this.npc.getBukkitEntity(), this.vector.getX(), this.vector.getY(), this.vector.getZ(), this.params.speed());
         return false;
      } else {
         return true;
      }
   }
}
