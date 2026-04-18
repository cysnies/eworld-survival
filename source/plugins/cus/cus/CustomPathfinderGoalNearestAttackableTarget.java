package cus;

import java.util.Collections;
import java.util.List;
import net.minecraft.server.v1_6_R2.DistanceComparator;
import net.minecraft.server.v1_6_R2.Entity;
import net.minecraft.server.v1_6_R2.EntityCreature;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.IEntitySelector;
import net.minecraft.server.v1_6_R2.PathfinderGoalTarget;

public class CustomPathfinderGoalNearestAttackableTarget extends PathfinderGoalTarget {
   private final Class a;
   private final int b;
   private final DistanceComparator e;
   private final IEntitySelector f;
   private EntityLiving g;
   private EntityCreature ec;

   public CustomPathfinderGoalNearestAttackableTarget(EntityCreature paramEntityCreature, Class paramClass, int paramInt, boolean paramBoolean) {
      this(paramEntityCreature, paramClass, paramInt, paramBoolean, false);
   }

   public CustomPathfinderGoalNearestAttackableTarget(EntityCreature paramEntityCreature, Class paramClass, int paramInt, boolean paramBoolean1, boolean paramBoolean2) {
      this(paramEntityCreature, paramClass, paramInt, paramBoolean1, paramBoolean2, (IEntitySelector)null);
   }

   public CustomPathfinderGoalNearestAttackableTarget(EntityCreature paramEntityCreature, Class paramClass, int paramInt, boolean paramBoolean1, boolean paramBoolean2, IEntitySelector paramIEntitySelector) {
      super(paramEntityCreature, paramBoolean1, paramBoolean2);
      this.a = paramClass;
      this.b = paramInt;
      this.e = new DistanceComparator(paramEntityCreature);
      this.a(1);
      this.f = new EntitySelectorNearestAttackableTarget(this, paramIEntitySelector);
      this.ec = paramEntityCreature;
   }

   public boolean a() {
      if (this.b > 0 && this.c.aC().nextInt(this.b) != 0) {
         return false;
      } else {
         double d = this.f();
         List localList = this.c.world.a(this.a, this.c.boundingBox.grow(d, (double)4.0F, d), this.f);
         Collections.sort(localList, this.e);
         if (localList.isEmpty()) {
            return false;
         } else {
            for(Object obj : localList) {
               this.g = (EntityLiving)obj;
               if (CustomEntityUtil.getOpposite(this.ec, this.g).equals(CustomEntityUtil.Opposite.yes)) {
                  return true;
               }
            }

            this.g = null;
            return false;
         }
      }
   }

   public void c() {
      this.c.setGoalTarget(this.g);
      super.c();
   }

   class EntitySelectorNearestAttackableTarget implements IEntitySelector {
      final IEntitySelector c;
      final CustomPathfinderGoalNearestAttackableTarget d;

      EntitySelectorNearestAttackableTarget(CustomPathfinderGoalNearestAttackableTarget pathfindergoalnearestattackabletarget, IEntitySelector ientityselector) {
         super();
         this.d = pathfindergoalnearestattackabletarget;
         this.c = ientityselector;
      }

      public boolean a(Entity entity) {
         return !(entity instanceof EntityLiving) ? false : (this.c != null && !this.c.a(entity) ? false : this.d.a((EntityLiving)entity, false));
      }
   }
}
