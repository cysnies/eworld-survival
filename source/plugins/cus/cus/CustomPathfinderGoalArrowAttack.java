package cus;

import java.lang.reflect.Field;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.IRangedEntity;
import net.minecraft.server.v1_6_R2.PathfinderGoalArrowAttack;

public class CustomPathfinderGoalArrowAttack extends PathfinderGoalArrowAttack {
   static Field field;

   static {
      try {
         field = PathfinderGoalArrowAttack.class.getDeclaredField("c");
         field.setAccessible(true);
      } catch (NoSuchFieldException e) {
         e.printStackTrace();
      } catch (SecurityException e) {
         e.printStackTrace();
      }

   }

   public CustomPathfinderGoalArrowAttack(IRangedEntity irangedentity, double d0, int i, float f) {
      this(irangedentity, d0, i, i, f);
   }

   public CustomPathfinderGoalArrowAttack(IRangedEntity irangedentity, double d0, int i, int j, float f) {
      super(irangedentity, d0, i, j, f);
   }

   public void d() {
      try {
         EntityLiving el = (EntityLiving)field.get(this);
         if (el == null) {
            return;
         }

         super.d();
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      }

   }

   public void e() {
      try {
         EntityLiving el = (EntityLiving)field.get(this);
         if (el == null) {
            return;
         }

         super.e();
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      }

   }
}
