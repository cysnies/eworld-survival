package fr.neatmonster.nocheatplus.checks.moving;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.PassableRayTracing;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Passable extends Check {
   private final PassableRayTracing rayTracing = new PassableRayTracing();

   public Passable() {
      super(CheckType.MOVING_PASSABLE);
      this.rayTracing.setMaxSteps(60);
   }

   public Location check(Player player, Location loc, PlayerLocation from, PlayerLocation to, MovingData data, MovingConfig cc) {
      String tags = "";
      int manhattan = from.manhattan(to);
      if (manhattan == 0 && (BlockProperties.getBlockFlags(from.getTypeId()) & 8L) != 0L) {
         return null;
      } else {
         boolean toPassable = to.isPassable();
         if (toPassable && cc.passableRayTracingCheck && (!cc.passableRayTracingVclipOnly || from.getY() != to.getY()) && (!cc.passableRayTracingBlockChangeOnly || manhattan > 0)) {
            this.rayTracing.set(from, to);
            this.rayTracing.loop();
            if (this.rayTracing.collides() || this.rayTracing.getStepsDone() >= this.rayTracing.getMaxSteps()) {
               int maxBlockDist = manhattan <= 1 ? manhattan : from.maxBlockDist(to);
               if (maxBlockDist <= 1 && this.rayTracing.getStepsDone() == 1 && !from.isPassable()) {
                  if (this.collidesIgnoreFirst(from, to)) {
                     toPassable = false;
                     tags = "raytracing_2x_";
                  } else if (cc.debug) {
                     System.out.println(player.getName() + " passable: allow moving out of a block.");
                  }
               } else if (!this.allowsSplitMove(from, to, manhattan)) {
                  toPassable = false;
                  tags = "raytracing_";
               }
            }

            this.rayTracing.cleanup();
         }

         if (toPassable) {
            data.passableVL *= 0.99;
            return null;
         } else {
            return this.potentialViolation(player, loc, from, to, manhattan, tags, data, cc);
         }
      }
   }

   private Location potentialViolation(Player player, Location loc, PlayerLocation from, PlayerLocation to, int manhattan, String tags, MovingData data, MovingConfig cc) {
      int lbX = loc.getBlockX();
      int lbY = loc.getBlockY();
      int lbZ = loc.getBlockZ();
      if (from.isPassable()) {
         if (from.isBlockAbove(to) && (BlockProperties.getBlockFlags(to.getTypeId()) & 64L) != 0L && BlockProperties.collidesBlock(to.getBlockCache(), from.getX(), from.getY(), from.getZ(), from.getX(), from.getY(), from.getZ(), to.getBlockX(), to.getBlockY(), to.getBlockZ(), to.getTypeId())) {
            return null;
         }

         loc = null;
         tags = tags + "into";
      } else if (BlockProperties.isPassable(from.getBlockCache(), loc.getX(), loc.getY(), loc.getZ(), from.getTypeId(lbX, lbY, lbZ))) {
         tags = tags + "into_shift";
      } else if (!from.isSameBlock(lbX, lbY, lbZ)) {
         tags = tags + "cross_shift";
      } else {
         if (manhattan == 1 && to.isBlockAbove(from) && BlockProperties.isPassable(from.getBlockCache(), from.getX(), from.getY() + player.getEyeHeight(), from.getZ(), from.getTypeId(from.getBlockX(), Location.locToBlock(from.getY() + player.getEyeHeight()), from.getBlockZ()))) {
            return null;
         }

         if (manhattan <= 0) {
            return null;
         }

         loc = null;
         tags = tags + "cross";
      }

      if (data.hasSetBack()) {
         Location ref = data.getSetBack(to);
         if (BlockProperties.isPassable(from.getBlockCache(), ref)) {
            loc = ref;
         }
      }

      ++data.passableVL;
      ViolationData vd = new ViolationData(this, player, data.passableVL, (double)1.0F, cc.passableActions);
      if (cc.debug || vd.needsParameters()) {
         vd.setParameter(ParameterName.BLOCK_ID, "" + to.getTypeId());
         if (!tags.isEmpty()) {
            vd.setParameter(ParameterName.TAGS, tags);
         }
      }

      if (this.executeActions(vd)) {
         Location newTo;
         if (loc != null) {
            newTo = loc;
         } else {
            newTo = from.getLocation();
         }

         newTo.setYaw(to.getYaw());
         newTo.setPitch(to.getPitch());
         return newTo;
      } else {
         return null;
      }
   }

   private boolean collidesIgnoreFirst(PlayerLocation from, PlayerLocation to) {
      this.rayTracing.set(from, to);
      this.rayTracing.setIgnorefirst();
      this.rayTracing.loop();
      return this.rayTracing.collides() || this.rayTracing.getStepsDone() >= this.rayTracing.getMaxSteps();
   }

   private boolean allowsSplitMove(PlayerLocation from, PlayerLocation to, int manhattan) {
      double yDiff = to.getY() - from.getY();
      if (manhattan <= 3 && yDiff > (double)0.0F && Math.abs(yDiff) < (double)1.0F) {
         if (yDiff > (double)0.0F) {
            this.rayTracing.set(from.getX(), from.getY(), from.getZ(), from.getX(), to.getY(), from.getZ());
            this.rayTracing.loop();
            if (!this.rayTracing.collides()) {
               this.rayTracing.set(from.getX(), to.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
               this.rayTracing.loop();
               if (!this.rayTracing.collides()) {
                  return true;
               }
            }
         } else {
            this.rayTracing.set(from.getX(), from.getY(), from.getZ(), to.getX(), from.getY(), to.getZ());
            this.rayTracing.loop();
            if (!this.rayTracing.collides()) {
               this.rayTracing.set(to.getX(), from.getY(), to.getZ(), to.getX(), to.getY(), to.getZ());
               this.rayTracing.loop();
               if (!this.rayTracing.collides()) {
                  return true;
               }
            }
         }
      }

      return false;
   }
}
