package cus;

import java.lang.reflect.Field;
import net.minecraft.server.v1_6_R2.EntityCreature;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.PathfinderGoalMeleeAttack;

public class CustomPathfinderGoalMeleeAttack extends PathfinderGoalMeleeAttack {
   private static Field field;
   private static Field field2;

   static {
      try {
         field = PathfinderGoalMeleeAttack.class.getDeclaredField("b");
      } catch (NoSuchFieldException e) {
         e.printStackTrace();
      } catch (SecurityException e) {
         e.printStackTrace();
      }

      field.setAccessible(true);

      try {
         field2 = PathfinderGoalMeleeAttack.class.getDeclaredField("f");
      } catch (NoSuchFieldException e) {
         e.printStackTrace();
      } catch (SecurityException e) {
         e.printStackTrace();
      }

      field2.setAccessible(true);
   }

   public CustomPathfinderGoalMeleeAttack(EntityCreature entitycreature, Class oclass, double d0, boolean flag) {
      super(entitycreature, oclass, d0, flag);
   }

   public boolean a() {
      try {
         if (super.a()) {
            EntityCreature ec = (EntityCreature)field.get(this);
            EntityLiving el = ec.getGoalTarget();
            if (CustomEntityUtil.getOpposite(ec, el).equals(CustomEntityUtil.Opposite.yes)) {
               return true;
            }

            field2.set(this, (Object)null);
            return false;
         }

         return false;
      } catch (SecurityException e) {
         e.printStackTrace();
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      }

      return false;
   }

   public void e() {
      try {
         EntityCreature ec = (EntityCreature)field.get(this);
         if (ec.getGoalTarget() == null) {
            return;
         }

         super.e();
      } catch (IllegalArgumentException e1) {
         e1.printStackTrace();
      } catch (IllegalAccessException e1) {
         e1.printStackTrace();
      }

   }
}
