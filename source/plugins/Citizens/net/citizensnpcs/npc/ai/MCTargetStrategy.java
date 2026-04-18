package net.citizensnpcs.npc.ai;

import java.lang.reflect.Field;
import net.citizensnpcs.Settings;
import net.citizensnpcs.api.ai.AttackStrategy;
import net.citizensnpcs.api.ai.EntityTarget;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.PlayerAnimation;
import net.citizensnpcs.util.nms.PlayerNavigation;
import net.minecraft.server.v1_6_R2.AttributeInstance;
import net.minecraft.server.v1_6_R2.Entity;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.EntityPlayer;
import net.minecraft.server.v1_6_R2.Navigation;
import net.minecraft.server.v1_6_R2.PathEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

public class MCTargetStrategy implements PathStrategy, EntityTarget {
   private final boolean aggro;
   private int attackTicks;
   private CancelReason cancelReason;
   private final EntityLiving handle;
   private final NPC npc;
   private final NavigatorParameters parameters;
   private final Entity target;
   private final TargetNavigator targetNavigator;
   private static final int ATTACK_DELAY_TICKS = 20;
   static final AttackStrategy DEFAULT_ATTACK_STRATEGY = new AttackStrategy() {
      public boolean handle(LivingEntity attacker, LivingEntity bukkitTarget) {
         EntityLiving handle = NMS.getHandle(attacker);
         EntityLiving target = NMS.getHandle(bukkitTarget);
         if (handle instanceof EntityPlayer) {
            EntityPlayer humanHandle = (EntityPlayer)handle;
            humanHandle.attack(target);
            PlayerAnimation.ARM_SWING.play(humanHandle.getBukkitEntity());
         } else {
            NMS.attack(handle, target);
         }

         return false;
      }
   };
   private static Field E_NAV_E = NMS.getField(Navigation.class, "e");
   private static Field E_NAV_J = NMS.getField(Navigation.class, "j");
   private static Field E_NAV_M = NMS.getField(Navigation.class, "m");
   private static final Location HANDLE_LOCATION = new Location((World)null, (double)0.0F, (double)0.0F, (double)0.0F);
   private static Field P_NAV_E = NMS.getField(PlayerNavigation.class, "e");
   private static Field P_NAV_J = NMS.getField(PlayerNavigation.class, "j");
   private static Field P_NAV_M = NMS.getField(PlayerNavigation.class, "m");
   private static final Location TARGET_LOCATION = new Location((World)null, (double)0.0F, (double)0.0F, (double)0.0F);

   public MCTargetStrategy(NPC npc, org.bukkit.entity.Entity target, boolean aggro, NavigatorParameters params) {
      super();
      this.npc = npc;
      this.parameters = params;
      this.handle = ((CraftLivingEntity)npc.getBukkitEntity()).getHandle();
      this.target = ((CraftEntity)target).getHandle();
      Navigation nav = NMS.getNavigation(this.handle);
      this.targetNavigator = (TargetNavigator)(nav != null && !params.useNewPathfinder() ? new NavigationFieldWrapper(nav) : new AStarTargeter());
      this.aggro = aggro;
   }

   private boolean canAttack() {
      return this.attackTicks == 0 && this.handle.boundingBox.e > this.target.boundingBox.b && this.handle.boundingBox.b < this.target.boundingBox.e && this.distanceSquared() <= Settings.Setting.NPC_ATTACK_DISTANCE.asDouble() && this.hasLineOfSight();
   }

   public void clearCancelReason() {
      this.cancelReason = null;
   }

   private double distanceSquared() {
      return this.handle.getBukkitEntity().getLocation(HANDLE_LOCATION).distanceSquared(this.target.getBukkitEntity().getLocation(TARGET_LOCATION));
   }

   public CancelReason getCancelReason() {
      return this.cancelReason;
   }

   public LivingEntity getTarget() {
      return (LivingEntity)this.target.getBukkitEntity();
   }

   public Location getTargetAsLocation() {
      return this.getTarget().getLocation();
   }

   public TargetType getTargetType() {
      return TargetType.ENTITY;
   }

   private boolean hasLineOfSight() {
      return ((LivingEntity)this.handle.getBukkitEntity()).hasLineOfSight(this.target.getBukkitEntity());
   }

   public boolean isAggressive() {
      return this.aggro;
   }

   private void setPath() {
      this.targetNavigator.setPath();
   }

   public void stop() {
      this.targetNavigator.stop();
   }

   public String toString() {
      return "MCTargetStrategy [target=" + this.target + "]";
   }

   public boolean update() {
      if (this.target != null && this.target.getBukkitEntity().isValid()) {
         if (this.target.world != this.handle.world) {
            this.cancelReason = CancelReason.TARGET_MOVED_WORLD;
            return true;
         } else if (this.cancelReason != null) {
            return true;
         } else {
            this.setPath();
            NMS.look(this.handle, this.target);
            if (this.aggro && this.canAttack()) {
               AttackStrategy strategy = this.parameters.attackStrategy();
               if ((strategy == null || !strategy.handle((LivingEntity)this.handle.getBukkitEntity(), this.getTarget())) && strategy != this.parameters.defaultAttackStrategy()) {
                  this.parameters.defaultAttackStrategy().handle((LivingEntity)this.handle.getBukkitEntity(), this.getTarget());
               }

               this.attackTicks = 20;
            }

            if (this.attackTicks > 0) {
               --this.attackTicks;
            }

            return false;
         }
      } else {
         this.cancelReason = CancelReason.TARGET_DIED;
         return true;
      }
   }

   private class AStarTargeter implements TargetNavigator {
      private int failureTimes;
      private AStarNavigationStrategy strategy;

      private AStarTargeter() {
         super();
         this.failureTimes = 0;
         this.strategy = new AStarNavigationStrategy(MCTargetStrategy.this.npc, MCTargetStrategy.this.target.getBukkitEntity().getLocation(MCTargetStrategy.TARGET_LOCATION), MCTargetStrategy.this.parameters);
      }

      public void setPath() {
         this.strategy = new AStarNavigationStrategy(MCTargetStrategy.this.npc, MCTargetStrategy.this.target.getBukkitEntity().getLocation(MCTargetStrategy.TARGET_LOCATION), MCTargetStrategy.this.parameters);
         this.strategy.update();
         CancelReason subReason = this.strategy.getCancelReason();
         if (subReason == CancelReason.STUCK) {
            if (this.failureTimes++ > 10) {
               MCTargetStrategy.this.cancelReason = this.strategy.getCancelReason();
            }
         } else {
            this.failureTimes = 0;
            MCTargetStrategy.this.cancelReason = this.strategy.getCancelReason();
         }

      }

      public void stop() {
         this.strategy.stop();
      }
   }

   private class NavigationFieldWrapper implements TargetNavigator {
      boolean j;
      boolean k;
      boolean l;
      boolean m;
      private final Navigation navigation;
      float range;

      private NavigationFieldWrapper(Navigation navigation) {
         super();
         this.j = true;
         this.navigation = navigation;
         this.k = navigation.c();
         this.l = navigation.a();

         try {
            if (navigation instanceof PlayerNavigation) {
               if (MCTargetStrategy.P_NAV_E != null) {
                  this.range = (float)((AttributeInstance)MCTargetStrategy.P_NAV_E.get(navigation)).getValue();
               }

               if (MCTargetStrategy.P_NAV_J != null) {
                  this.j = MCTargetStrategy.P_NAV_J.getBoolean(navigation);
               }

               if (MCTargetStrategy.P_NAV_M != null) {
                  this.m = MCTargetStrategy.P_NAV_M.getBoolean(navigation);
               }
            } else {
               if (MCTargetStrategy.E_NAV_E != null) {
                  this.range = (float)((AttributeInstance)MCTargetStrategy.E_NAV_E.get(navigation)).getValue();
               }

               if (MCTargetStrategy.E_NAV_J != null) {
                  this.j = MCTargetStrategy.E_NAV_J.getBoolean(navigation);
               }

               if (MCTargetStrategy.E_NAV_M != null) {
                  this.m = MCTargetStrategy.E_NAV_M.getBoolean(navigation);
               }
            }
         } catch (Exception var4) {
            this.range = MCTargetStrategy.this.parameters.range();
         }

      }

      public PathEntity findPath(Entity from, Entity to) {
         return MCTargetStrategy.this.handle.world.findPath(from, to, this.range, this.j, this.k, this.l, this.m);
      }

      public void setPath() {
         this.navigation.a(MCTargetStrategy.this.parameters.avoidWater());
         this.navigation.a(this.findPath(MCTargetStrategy.this.handle, MCTargetStrategy.this.target), (double)MCTargetStrategy.this.parameters.speed());
      }

      public void stop() {
         NMS.stopNavigation(this.navigation);
      }
   }

   private interface TargetNavigator {
      void setPath();

      void stop();
   }
}
