package cus;

import net.minecraft.server.v1_6_R2.EntityAgeable;
import net.minecraft.server.v1_6_R2.EntityCow;
import net.minecraft.server.v1_6_R2.EntityHuman;
import net.minecraft.server.v1_6_R2.Item;
import net.minecraft.server.v1_6_R2.PathfinderGoalBreed;
import net.minecraft.server.v1_6_R2.PathfinderGoalFloat;
import net.minecraft.server.v1_6_R2.PathfinderGoalFollowParent;
import net.minecraft.server.v1_6_R2.PathfinderGoalPanic;
import net.minecraft.server.v1_6_R2.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_6_R2.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_6_R2.PathfinderGoalTempt;
import net.minecraft.server.v1_6_R2.World;

public class CustomEntityCow extends EntityCow implements CustomAnimal {
   public CustomEntityCow(World world) {
      super(world);
   }

   public EntityCow b(EntityAgeable entityageable) {
      return new CustomEntityCow(this.world);
   }

   public void setAi(int ai) {
      CustomEntityUtil.clearTarget(this.goalSelector, this.targetSelector);
      switch (ai) {
         case 0:
         default:
            break;
         case 1:
            this.goalSelector.a(0, new PathfinderGoalFloat(this));
            this.goalSelector.a(2, new PathfinderGoalBreed(this, (double)1.0F));
            this.goalSelector.a(3, new PathfinderGoalTempt(this, (double)1.25F, Item.WHEAT.id, false));
            break;
         case 2:
            this.goalSelector.a(0, new PathfinderGoalFloat(this));
            this.goalSelector.a(1, new PathfinderGoalPanic(this, (double)2.0F));
            this.goalSelector.a(2, new PathfinderGoalBreed(this, (double)1.0F));
            this.goalSelector.a(3, new PathfinderGoalTempt(this, (double)1.25F, Item.WHEAT.id, false));
            this.goalSelector.a(4, new PathfinderGoalFollowParent(this, (double)1.25F));
            this.goalSelector.a(5, new PathfinderGoalRandomStroll(this, (double)1.0F));
            this.goalSelector.a(6, new CustomPathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
            this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
      }

   }
}
