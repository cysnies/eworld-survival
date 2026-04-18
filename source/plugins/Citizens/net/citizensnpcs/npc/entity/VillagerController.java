package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.MobEntityController;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_6_R2.Entity;
import net.minecraft.server.v1_6_R2.EntityHuman;
import net.minecraft.server.v1_6_R2.EntityVillager;
import net.minecraft.server.v1_6_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_6_R2.CraftServer;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftVillager;
import org.bukkit.entity.Villager;
import org.bukkit.util.Vector;

public class VillagerController extends MobEntityController {
   public VillagerController() {
      super(EntityVillagerNPC.class);
   }

   public Villager getBukkitEntity() {
      return (Villager)super.getBukkitEntity();
   }

   public static class EntityVillagerNPC extends EntityVillager implements NPCHolder {
      private boolean blockTrades;
      private final CitizensNPC npc;

      public EntityVillagerNPC(World world) {
         this(world, (NPC)null);
      }

      public EntityVillagerNPC(World world, NPC npc) {
         super(world);
         this.blockTrades = true;
         this.npc = (CitizensNPC)npc;
         if (npc != null) {
            NMS.clearGoals(this.goalSelector, this.targetSelector);
         }

      }

      public boolean a(EntityHuman entityhuman) {
         return this.npc != null && this.blockTrades ? false : super.a(entityhuman);
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
            this.bukkitEntity = new VillagerNPC(this);
         }

         return super.getBukkitEntity();
      }

      public NPC getNPC() {
         return this.npc;
      }

      public boolean isBlockingTrades() {
         return this.blockTrades;
      }

      public void setBlockTrades(boolean blocked) {
         this.blockTrades = blocked;
      }
   }

   public static class VillagerNPC extends CraftVillager implements NPCHolder {
      private final CitizensNPC npc;

      public VillagerNPC(EntityVillagerNPC entity) {
         super((CraftServer)Bukkit.getServer(), entity);
         this.npc = entity.npc;
      }

      public NPC getNPC() {
         return this.npc;
      }
   }
}
