package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.MobEntityController;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_6_R2.Entity;
import net.minecraft.server.v1_6_R2.EntityWither;
import net.minecraft.server.v1_6_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_6_R2.CraftServer;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftWither;
import org.bukkit.entity.Wither;
import org.bukkit.util.Vector;

public class WitherController extends MobEntityController {
   public WitherController() {
      super(EntityWitherNPC.class);
   }

   public Wither getBukkitEntity() {
      return (Wither)super.getBukkitEntity();
   }

   public static class EntityWitherNPC extends EntityWither implements NPCHolder {
      private int jumpTicks;
      private final CitizensNPC npc;

      public EntityWitherNPC(World world) {
         this(world, (NPC)null);
      }

      public EntityWitherNPC(World world, NPC npc) {
         super(world);
         this.npc = (CitizensNPC)npc;
         if (npc != null) {
            NMS.clearGoals(this.goalSelector, this.targetSelector);
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

      public void c() {
         if (this.npc == null) {
            super.c();
         } else {
            this.updateAIWithMovement();
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
            this.bukkitEntity = new WitherNPC(this);
         }

         return super.getBukkitEntity();
      }

      public NPC getNPC() {
         return this.npc;
      }

      private void updateAIWithMovement() {
         NMS.updateAI(this);
         if (this.bd) {
            if (this.onGround && this.jumpTicks == 0) {
               this.bd();
               this.jumpTicks = 10;
            }
         } else {
            this.jumpTicks = 0;
         }

         this.be *= 0.98F;
         this.bf *= 0.98F;
         this.bg *= 0.9F;
         this.e(this.be, this.bf);
         NMS.setHeadYaw(this, this.yaw);
         if (this.jumpTicks > 0) {
            --this.jumpTicks;
         }

      }
   }

   public static class WitherNPC extends CraftWither implements NPCHolder {
      private final CitizensNPC npc;

      public WitherNPC(EntityWitherNPC entity) {
         super((CraftServer)Bukkit.getServer(), entity);
         this.npc = entity.npc;
      }

      public NPC getNPC() {
         return this.npc;
      }
   }
}
