package cus;

import java.lang.reflect.Field;
import net.minecraft.server.v1_6_R2.EntityInsentient;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.PathfinderGoalLookAtPlayer;

public class CustomPathfinderGoalLookAtPlayer extends PathfinderGoalLookAtPlayer {
   private static Field field;

   static {
      try {
         field = PathfinderGoalLookAtPlayer.class.getDeclaredField("b");
      } catch (NoSuchFieldException e) {
         e.printStackTrace();
      } catch (SecurityException e) {
         e.printStackTrace();
      }

      field.setAccessible(true);
   }

   public CustomPathfinderGoalLookAtPlayer(EntityInsentient arg0, Class arg1, float arg2) {
      super(arg0, arg1, arg2);
   }

   public boolean a() {
      try {
         if (super.a()) {
            if (this.a instanceof EntityLiving) {
               EntityInsentient ei = (EntityInsentient)field.get(this);
               if (CustomEntityUtil.getOpposite(ei, (EntityLiving)this.a).equals(CustomEntityUtil.Opposite.yes)) {
                  return true;
               }

               this.a = null;
               return false;
            }

            return true;
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
}
