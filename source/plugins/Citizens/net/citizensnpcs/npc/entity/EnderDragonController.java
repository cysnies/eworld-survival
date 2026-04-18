package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.MobEntityController;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_6_R2.Entity;
import net.minecraft.server.v1_6_R2.EntityEnderDragon;
import net.minecraft.server.v1_6_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_6_R2.CraftServer;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEntity;
import org.bukkit.entity.EnderDragon;
import org.bukkit.util.Vector;

public class EnderDragonController extends MobEntityController {
   public EnderDragonController() {
      super(EntityEnderDragonNPC.class);
   }

   public EnderDragon getBukkitEntity() {
      return (EnderDragon)super.getBukkitEntity();
   }

   public static class EnderDragonNPC extends CraftEnderDragon implements NPCHolder {
      private final CitizensNPC npc;

      public EnderDragonNPC(EntityEnderDragonNPC entity) {
         super((CraftServer)Bukkit.getServer(), entity);
         this.npc = entity.npc;
      }

      public NPC getNPC() {
         return this.npc;
      }
   }

   public static class EntityEnderDragonNPC extends EntityEnderDragon implements NPCHolder {
      private final CitizensNPC npc;

      public EntityEnderDragonNPC(World world) {
         this(world, (NPC)null);
      }

      public EntityEnderDragonNPC(World world, NPC npc) {
         super(world);
         this.npc = (CitizensNPC)npc;
         if (npc != null) {
            NMS.clearGoals(this.goalSelector, this.targetSelector);
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

      public void bk() {
         if (this.npc == null) {
            super.bk();
         }

      }

      public void c() {
         if (this.npc != null) {
            this.npc.update();
            if (this.motX != (double)0.0F || this.motY != (double)0.0F || this.motZ != (double)0.0F) {
               this.motX *= 0.98;
               this.motY *= 0.98;
               this.motZ *= 0.98;
               this.yaw = this.getCorrectYaw(this.locX + this.motX, this.locZ + this.motZ);
               this.setPosition(this.locX + this.motX, this.locY + this.motY, this.locZ + this.motZ);
            }
         } else {
            super.c();
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
            this.bukkitEntity = new EnderDragonNPC(this);
         }

         return super.getBukkitEntity();
      }

      private float getCorrectYaw(double tX, double tZ) {
         if (this.locZ > tZ) {
            return (float)(-Math.toDegrees(Math.atan((this.locX - tX) / (this.locZ - tZ))));
         } else {
            return this.locZ < tZ ? (float)(-Math.toDegrees(Math.atan((this.locX - tX) / (this.locZ - tZ)))) + 180.0F : this.yaw;
         }
      }

      public NPC getNPC() {
         return this.npc;
      }
   }
}
