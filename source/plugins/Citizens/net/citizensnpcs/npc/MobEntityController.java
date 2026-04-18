package net.citizensnpcs.npc;

import com.google.common.collect.Maps;
import java.lang.reflect.Constructor;
import java.util.Map;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.entity.LivingEntity;

public abstract class MobEntityController extends AbstractEntityController {
   private final Constructor constructor;
   private static final Map CONSTRUCTOR_CACHE = Maps.newHashMap();

   protected MobEntityController(Class clazz) {
      super();
      this.constructor = getConstructor(clazz);
      NMS.registerEntityClass(clazz);
   }

   protected LivingEntity createEntity(Location at, NPC npc) {
      EntityLiving entity = this.createEntityFromClass(((CraftWorld)at.getWorld()).getHandle(), npc);
      entity.setPositionRotation(at.getX(), at.getY(), at.getZ(), at.getYaw(), at.getPitch());
      Material beneath = at.getBlock().getRelative(BlockFace.DOWN).getType();
      if (beneath.isBlock()) {
         entity.onGround = true;
      }

      return (LivingEntity)entity.getBukkitEntity();
   }

   private EntityLiving createEntityFromClass(Object... args) {
      try {
         return (EntityLiving)this.constructor.newInstance(args);
      } catch (Exception ex) {
         ex.printStackTrace();
         return null;
      }
   }

   private static Constructor getConstructor(Class clazz) {
      Constructor<?> constructor = (Constructor)CONSTRUCTOR_CACHE.get(clazz);
      if (constructor != null) {
         return constructor;
      } else {
         try {
            return clazz.getConstructor(World.class, NPC.class);
         } catch (Exception var3) {
            throw new IllegalStateException("unable to find an entity constructor");
         }
      }
   }
}
