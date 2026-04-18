package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.MobEntityController;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_6_R2.Entity;
import net.minecraft.server.v1_6_R2.EntityCreeper;
import net.minecraft.server.v1_6_R2.EntityLightning;
import net.minecraft.server.v1_6_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_6_R2.CraftServer;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftCreeper;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEntity;
import org.bukkit.entity.Creeper;
import org.bukkit.util.Vector;

public class CreeperController extends MobEntityController {
   public CreeperController() {
      super(EntityCreeperNPC.class);
   }

   public Creeper getBukkitEntity() {
      return (Creeper)super.getBukkitEntity();
   }

   public static class CreeperNPC extends CraftCreeper implements NPCHolder {
      private final CitizensNPC npc;

      public CreeperNPC(EntityCreeperNPC entity) {
         super((CraftServer)Bukkit.getServer(), entity);
         this.npc = entity.npc;
      }

      public NPC getNPC() {
         return this.npc;
      }
   }

   public static class EntityCreeperNPC extends EntityCreeper implements NPCHolder {
      private boolean allowPowered;
      private final CitizensNPC npc;

      public EntityCreeperNPC(World world) {
         this(world, (NPC)null);
      }

      public EntityCreeperNPC(World world, NPC npc) {
         super(world);
         this.npc = (CitizensNPC)npc;
         if (npc != null) {
            NMS.clearGoals(this.goalSelector, this.targetSelector);
         }

      }

      public void a(EntityLightning entitylightning) {
         if (this.npc == null || this.allowPowered) {
            super.a(entitylightning);
         }

      }

      public void bh() {
         super.bh();
         if (this.npc != null) {
            this.npc.update();
         }

      }

      public boolean bH() {
         if (this.npc == null) {
            return super.bH();
         } else {
            boolean protectedDefault = (Boolean)this.npc.data().get("protected", true);
            if (protectedDefault && (Boolean)this.npc.data().get("protected-leash", protectedDefault)) {
               if (super.bH()) {
                  this.a(true, false);
               }

               return false;
            } else {
               return super.bH();
            }
         }
      }

      public void collide(Entity entity) {
         super.collide(entity);
         if (this.npc != null) {
            Util.callCollisionEvent(this.npc, entity.getBukkitEntity());
         }

      }

      public void g(double x, double y, double z) {
         if (this.npc == null) {
            super.g(x, y, z);
         } else if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0) {
            if (!(Boolean)this.npc.data().get("protected", true)) {
               super.g(x, y, z);
            }

         } else {
            Vector vector = new Vector(x, y, z);
            NPCPushEvent event = Util.callPushEvent(this.npc, vector);
            if (!event.isCancelled()) {
               vector = event.getCollisionVector();
               super.g(vector.getX(), vector.getY(), vector.getZ());
            }

         }
      }

      public CraftEntity getBukkitEntity() {
         if (this.bukkitEntity == null && this.npc != null) {
            this.bukkitEntity = new CreeperNPC(this);
         }

         return super.getBukkitEntity();
      }

      public NPC getNPC() {
         return this.npc;
      }

      public void setAllowPowered(boolean allowPowered) {
         this.allowPowered = allowPowered;
      }
   }
}
