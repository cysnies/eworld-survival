package net.citizensnpcs.npc.ai;

import java.util.Iterator;
import net.citizensnpcs.Settings;
import net.citizensnpcs.api.ai.EntityTarget;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.StuckAction;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.TeleportStuckAction;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.ai.event.NavigationReplaceEvent;
import net.citizensnpcs.api.ai.event.NavigatorCallback;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.NMS;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class CitizensNavigator implements Navigator, Runnable {
   private final NavigatorParameters defaultParams;
   private PathStrategy executing;
   private int lastX;
   private int lastY;
   private int lastZ;
   private NavigatorParameters localParams;
   private final NPC npc;
   private int stationaryTicks;
   private static final Location STATIONARY_LOCATION = new Location((World)null, (double)0.0F, (double)0.0F, (double)0.0F);
   private static int UNINITIALISED_SPEED = Integer.MIN_VALUE;

   public CitizensNavigator(NPC npc) {
      super();
      this.defaultParams = (new NavigatorParameters()).baseSpeed((float)UNINITIALISED_SPEED).range(Settings.Setting.DEFAULT_PATHFINDING_RANGE.asFloat()).defaultAttackStrategy(MCTargetStrategy.DEFAULT_ATTACK_STRATEGY).stationaryTicks(Settings.Setting.DEFAULT_STATIONARY_TICKS.asInt()).stuckAction(TeleportStuckAction.INSTANCE).examiner(new MinecraftBlockExaminer()).useNewPathfinder(Settings.Setting.USE_NEW_PATHFINDER.asBoolean());
      this.localParams = this.defaultParams;
      this.npc = npc;
   }

   public void cancelNavigation() {
      this.stopNavigating(CancelReason.PLUGIN);
   }

   public NavigatorParameters getDefaultParameters() {
      return this.defaultParams;
   }

   public EntityTarget getEntityTarget() {
      return this.executing instanceof EntityTarget ? (EntityTarget)this.executing : null;
   }

   public NavigatorParameters getLocalParameters() {
      return !this.isNavigating() ? this.defaultParams : this.localParams;
   }

   public NPC getNPC() {
      return this.npc;
   }

   public Location getTargetAsLocation() {
      return this.isNavigating() ? this.executing.getTargetAsLocation() : null;
   }

   public TargetType getTargetType() {
      return this.isNavigating() ? this.executing.getTargetType() : null;
   }

   public boolean isNavigating() {
      return this.executing != null;
   }

   public void load(DataKey root) {
      this.defaultParams.range((float)root.getDouble("pathfindingrange", (double)Settings.Setting.DEFAULT_PATHFINDING_RANGE.asFloat()));
      this.defaultParams.stationaryTicks(root.getInt("stationaryticks", Settings.Setting.DEFAULT_STATIONARY_TICKS.asInt()));
      this.defaultParams.speedModifier((float)root.getDouble("speedmodifier", (double)1.0F));
      if (root.keyExists("avoidwater")) {
         this.defaultParams.avoidWater(root.getBoolean("avoidwater"));
      }

      if (!root.getBoolean("usedefaultstuckaction") && this.defaultParams.stuckAction() == TeleportStuckAction.INSTANCE) {
         this.defaultParams.stuckAction((StuckAction)null);
      }

   }

   public void onDespawn() {
      this.stopNavigating(CancelReason.NPC_DESPAWNED);
   }

   public void onSpawn() {
      if (this.defaultParams.baseSpeed() == (float)UNINITIALISED_SPEED) {
         this.defaultParams.baseSpeed(NMS.getSpeedFor(this.npc));
      }

      this.updatePathfindingRange();
   }

   public void run() {
      if (this.isNavigating() && this.npc.isSpawned()) {
         if (!this.updateStationaryStatus()) {
            this.updatePathfindingRange();
            boolean finished = this.executing.update();
            if (finished) {
               if (this.executing.getCancelReason() != null) {
                  this.stopNavigating(this.executing.getCancelReason());
               } else {
                  NavigationCompleteEvent event = new NavigationCompleteEvent(this);
                  PathStrategy old = this.executing;
                  Bukkit.getPluginManager().callEvent(event);
                  if (old == this.executing) {
                     this.stopNavigating((CancelReason)null);
                  }
               }

            }
         }
      }
   }

   public void save(DataKey root) {
      root.setDouble("pathfindingrange", (double)this.defaultParams.range());
      root.setInt("stationaryticks", this.defaultParams.stationaryTicks());
      root.setDouble("speedmodifier", (double)this.defaultParams.speedModifier());
      root.setBoolean("avoidwater", this.defaultParams.avoidWater());
      root.setBoolean("usedefaultstuckaction", this.defaultParams.stuckAction() == TeleportStuckAction.INSTANCE);
   }

   public void setTarget(Entity target, boolean aggressive) {
      if (!this.npc.isSpawned()) {
         throw new IllegalStateException("npc is not spawned");
      } else if (target == null) {
         this.cancelNavigation();
      } else {
         this.localParams = this.defaultParams.clone();
         PathStrategy newStrategy = new MCTargetStrategy(this.npc, target, aggressive, this.localParams);
         this.switchStrategyTo(newStrategy);
      }
   }

   public void setTarget(LivingEntity target, boolean aggressive) {
      this.setTarget((Entity)target, aggressive);
   }

   public void setTarget(Location target) {
      if (!this.npc.isSpawned()) {
         throw new IllegalStateException("npc is not spawned");
      } else if (target == null) {
         this.cancelNavigation();
      } else {
         this.localParams = this.defaultParams.clone();
         PathStrategy newStrategy;
         if (this.localParams.useNewPathfinder()) {
            newStrategy = new AStarNavigationStrategy(this.npc, target, this.localParams);
         } else {
            newStrategy = new MCNavigationStrategy(this.npc, target, this.localParams);
         }

         this.switchStrategyTo(newStrategy);
      }
   }

   private void stopNavigating() {
      if (this.executing != null) {
         this.executing.stop();
      }

      this.executing = null;
      this.localParams = this.defaultParams;
      this.stationaryTicks = 0;
      if (this.npc.isSpawned()) {
         Vector velocity = this.npc.getBukkitEntity().getVelocity();
         velocity.setX(0).setY(0).setZ(0);
         this.npc.getBukkitEntity().setVelocity(velocity);
      }

   }

   private void stopNavigating(CancelReason reason) {
      if (this.isNavigating()) {
         Iterator<NavigatorCallback> itr = this.localParams.callbacks().iterator();

         while(itr.hasNext()) {
            ((NavigatorCallback)itr.next()).onCompletion(reason);
            itr.remove();
         }

         if (Messaging.isDebugging()) {
            Messaging.debug(this.npc.getId(), "cancelling with reason", reason);
         }

         if (reason == null) {
            this.stopNavigating();
         } else {
            if (reason == CancelReason.STUCK && this.localParams.stuckAction() != null) {
               StuckAction action = this.localParams.stuckAction();
               boolean shouldContinue = action.run(this.npc, this);
               if (shouldContinue) {
                  this.stationaryTicks = 0;
                  this.executing.clearCancelReason();
                  return;
               }
            }

            NavigationCancelEvent event = new NavigationCancelEvent(this, reason);
            PathStrategy old = this.executing;
            Bukkit.getPluginManager().callEvent(event);
            if (old == this.executing) {
               this.stopNavigating();
            }

         }
      }
   }

   private void switchStrategyTo(PathStrategy newStrategy) {
      Messaging.debug(this.npc.getId(), "changing to new PathStrategy", newStrategy);
      if (this.executing != null) {
         Bukkit.getPluginManager().callEvent(new NavigationReplaceEvent(this));
      }

      this.executing = newStrategy;
      this.stationaryTicks = 0;
      if (this.npc.isSpawned()) {
         NMS.updateNavigationWorld(this.npc.getBukkitEntity(), this.npc.getBukkitEntity().getWorld());
      }

      Bukkit.getPluginManager().callEvent(new NavigationBeginEvent(this));
   }

   private void updatePathfindingRange() {
      NMS.updatePathfindingRange(this.npc, this.localParams.range());
   }

   private boolean updateStationaryStatus() {
      if (this.localParams.stationaryTicks() < 0) {
         return false;
      } else {
         Location current = this.npc.getBukkitEntity().getLocation(STATIONARY_LOCATION);
         if (this.lastX == current.getBlockX() && this.lastY == current.getBlockY() && this.lastZ == current.getBlockZ()) {
            if (++this.stationaryTicks >= this.localParams.stationaryTicks()) {
               this.stopNavigating(CancelReason.STUCK);
               return true;
            }
         } else {
            this.stationaryTicks = 0;
         }

         this.lastX = current.getBlockX();
         this.lastY = current.getBlockY();
         this.lastZ = current.getBlockZ();
         return false;
      }
   }
}
