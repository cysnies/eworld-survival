package net.citizensnpcs.npc.ai;

import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.Navigation;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;

public class MCNavigationStrategy extends AbstractPathStrategy {
   private final Navigation navigation;
   private final NavigatorParameters parameters;
   private final Location target;

   MCNavigationStrategy(NPC npc, Location dest, NavigatorParameters params) {
      super(TargetType.LOCATION);
      this.target = dest;
      this.parameters = params;
      EntityLiving handle = ((CraftLivingEntity)npc.getBukkitEntity()).getHandle();
      handle.onGround = true;
      this.navigation = NMS.getNavigation(handle);
      this.navigation.a(this.parameters.avoidWater());
      this.navigation.a(dest.getX(), dest.getY(), dest.getZ(), (double)this.parameters.speed());
      if (NMS.isNavigationFinished(this.navigation)) {
         this.setCancelReason(CancelReason.STUCK);
      }

   }

   public Location getTargetAsLocation() {
      return this.target;
   }

   public TargetType getTargetType() {
      return TargetType.LOCATION;
   }

   public void stop() {
      NMS.stopNavigation(this.navigation);
   }

   public String toString() {
      return "MCNavigationStrategy [target=" + this.target + "]";
   }

   public boolean update() {
      if (this.getCancelReason() != null) {
         return true;
      } else {
         this.navigation.a(this.parameters.avoidWater());
         this.navigation.a((double)this.parameters.speed());
         return NMS.isNavigationFinished(this.navigation);
      }
   }
}
