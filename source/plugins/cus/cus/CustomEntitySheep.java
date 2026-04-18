package cus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.server.v1_6_R2.EntityAgeable;
import net.minecraft.server.v1_6_R2.EntityAnimal;
import net.minecraft.server.v1_6_R2.EntityHuman;
import net.minecraft.server.v1_6_R2.EntitySheep;
import net.minecraft.server.v1_6_R2.Item;
import net.minecraft.server.v1_6_R2.PathfinderGoalBreed;
import net.minecraft.server.v1_6_R2.PathfinderGoalEatTile;
import net.minecraft.server.v1_6_R2.PathfinderGoalFloat;
import net.minecraft.server.v1_6_R2.PathfinderGoalFollowParent;
import net.minecraft.server.v1_6_R2.PathfinderGoalPanic;
import net.minecraft.server.v1_6_R2.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_6_R2.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_6_R2.PathfinderGoalTempt;
import net.minecraft.server.v1_6_R2.World;

public class CustomEntitySheep extends EntitySheep implements CustomAnimal {
   private PathfinderGoalEatTile bs = new PathfinderGoalEatTile(this);

   public CustomEntitySheep(World world) {
      super(world);
   }

   public EntitySheep b(EntityAgeable entityageable) {
      EntitySheep entitysheep = (EntitySheep)entityageable;
      EntitySheep entitysheep1 = new CustomEntitySheep(this.world);
      int i = -1;

      try {
         Method m = EntitySheep.class.getDeclaredMethod("a", EntityAnimal.class, EntityAnimal.class);
         m.setAccessible(true);
         i = (Integer)m.invoke(this, this, entitysheep);
      } catch (NoSuchMethodException e) {
         e.printStackTrace();
      } catch (SecurityException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
      } catch (InvocationTargetException e) {
         e.printStackTrace();
      }

      if (i != -1) {
         entitysheep1.setColor(15 - i);
      }

      return entitysheep1;
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
            this.goalSelector.a(3, new PathfinderGoalTempt(this, 1.1, Item.WHEAT.id, false));
            this.goalSelector.a(5, this.bs);
            break;
         case 2:
            this.goalSelector.a(0, new PathfinderGoalFloat(this));
            this.goalSelector.a(1, new PathfinderGoalPanic(this, (double)1.25F));
            this.goalSelector.a(2, new PathfinderGoalBreed(this, (double)1.0F));
            this.goalSelector.a(3, new PathfinderGoalTempt(this, 1.1, Item.WHEAT.id, false));
            this.goalSelector.a(4, new PathfinderGoalFollowParent(this, 1.1));
            this.goalSelector.a(5, this.bs);
            this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, (double)1.0F));
            this.goalSelector.a(7, new CustomPathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
            this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
      }

   }
}
