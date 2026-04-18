package net.citizensnpcs.util.nms;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.server.v1_6_R2.Entity;
import net.minecraft.server.v1_6_R2.EntityLiving;

public class PlayerEntitySenses {
   private final EntityLiving entity;
   private final Set seenEntities = new HashSet();
   private final Set unseenEntities = new HashSet();

   public PlayerEntitySenses(EntityLiving entityinsentient) {
      super();
      this.entity = entityinsentient;
   }

   public void a() {
      this.seenEntities.clear();
      this.unseenEntities.clear();
   }

   public boolean canSee(Entity entity) {
      if (this.seenEntities.contains(entity)) {
         return true;
      } else if (this.unseenEntities.contains(entity)) {
         return false;
      } else {
         boolean flag = this.entity.o(entity);
         if (flag) {
            this.seenEntities.add(entity);
         } else {
            this.unseenEntities.add(entity);
         }

         return flag;
      }
   }
}
