package cus;

import net.minecraft.server.v1_6_R2.EntityAgeable;
import net.minecraft.server.v1_6_R2.EntityHuman;
import net.minecraft.server.v1_6_R2.EntityPig;
import net.minecraft.server.v1_6_R2.Item;
import net.minecraft.server.v1_6_R2.PathfinderGoalBreed;
import net.minecraft.server.v1_6_R2.PathfinderGoalFloat;
import net.minecraft.server.v1_6_R2.PathfinderGoalFollowParent;
import net.minecraft.server.v1_6_R2.PathfinderGoalPanic;
import net.minecraft.server.v1_6_R2.PathfinderGoalPassengerCarrotStick;
import net.minecraft.server.v1_6_R2.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_6_R2.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_6_R2.PathfinderGoalTempt;
import net.minecraft.server.v1_6_R2.World;

public class CustomEntityPig extends EntityPig implements CustomAnimal {
   private final PathfinderGoalPassengerCarrotStick bp = new PathfinderGoalPassengerCarrotStick(this, 0.3F);

   public CustomEntityPig(World world) {
      super(world);
   }

   public EntityPig b(EntityAgeable entityageable) {
      return new CustomEntityPig(this.world);
   }

   public PathfinderGoalPassengerCarrotStick bU() {
      return this.bp;
   }

   public void setAi(int ai) {
      CustomEntityUtil.clearTarget(this.goalSelector, this.targetSelector);
      switch (ai) {
         case 0:
         default:
            break;
         case 1:
            this.goalSelector.a(0, new PathfinderGoalFloat(this));
            this.goalSelector.a(2, this.bp);
            this.goalSelector.a(3, new PathfinderGoalBreed(this, (double)1.0F));
            this.goalSelector.a(4, new PathfinderGoalTempt(this, 1.2, Item.CARROT_STICK.id, false));
            this.goalSelector.a(4, new PathfinderGoalTempt(this, 1.2, Item.CARROT.id, false));
            break;
         case 2:
            this.goalSelector.a(0, new PathfinderGoalFloat(this));
            this.goalSelector.a(1, new PathfinderGoalPanic(this, (double)1.25F));
            this.goalSelector.a(2, this.bp);
            this.goalSelector.a(3, new PathfinderGoalBreed(this, (double)1.0F));
            this.goalSelector.a(4, new PathfinderGoalTempt(this, 1.2, Item.CARROT_STICK.id, false));
            this.goalSelector.a(4, new PathfinderGoalTempt(this, 1.2, Item.CARROT.id, false));
            this.goalSelector.a(5, new PathfinderGoalFollowParent(this, 1.1));
            this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, (double)1.0F));
            this.goalSelector.a(7, new CustomPathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
            this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
      }

   }
}
