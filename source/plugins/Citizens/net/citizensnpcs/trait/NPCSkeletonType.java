package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;

public class NPCSkeletonType extends Trait {
   private Skeleton skeleton;
   @Persist
   private Skeleton.SkeletonType type;

   public NPCSkeletonType() {
      super("skeletontype");
      this.type = SkeletonType.NORMAL;
   }

   public void onSpawn() {
      this.skeleton = this.npc.getBukkitEntity() instanceof Skeleton ? (Skeleton)this.npc.getBukkitEntity() : null;
   }

   public void run() {
      if (this.skeleton != null) {
         this.skeleton.setSkeletonType(this.type);
      }

   }

   public void setType(Skeleton.SkeletonType type) {
      this.type = type;
   }
}
