package net.citizensnpcs.npc;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public abstract class AbstractEntityController implements EntityController {
   private LivingEntity bukkitEntity;

   public AbstractEntityController() {
      super();
   }

   protected abstract LivingEntity createEntity(Location var1, NPC var2);

   public LivingEntity getBukkitEntity() {
      return this.bukkitEntity;
   }

   public void remove() {
      if (this.bukkitEntity != null) {
         this.bukkitEntity.remove();
         this.bukkitEntity = null;
      }
   }

   public void spawn(Location at, NPC npc) {
      this.bukkitEntity = this.createEntity(at, npc);
   }
}
