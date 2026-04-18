package net.citizensnpcs.util;

import java.util.Arrays;
import net.minecraft.server.v1_6_R2.Entity;
import net.minecraft.server.v1_6_R2.EntityPlayer;
import net.minecraft.server.v1_6_R2.Packet;
import net.minecraft.server.v1_6_R2.Packet17EntityLocationAction;
import net.minecraft.server.v1_6_R2.Packet18ArmAnimation;
import net.minecraft.server.v1_6_R2.Packet40EntityMetadata;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

public enum PlayerAnimation {
   ARM_SWING {
      protected void playAnimation(EntityPlayer player, int radius) {
         Packet18ArmAnimation packet = new Packet18ArmAnimation(player, 1);
         this.sendPacketNearby(packet, player, radius);
      }
   },
   CRIT {
      protected void playAnimation(EntityPlayer player, int radius) {
         Packet18ArmAnimation packet = new Packet18ArmAnimation(player, 6);
         this.sendPacketNearby(packet, player, radius);
      }
   },
   HURT {
      protected void playAnimation(EntityPlayer player, int radius) {
         Packet18ArmAnimation packet = new Packet18ArmAnimation(player, 2);
         this.sendPacketNearby(packet, player, radius);
      }
   },
   MAGIC_CRIT {
      protected void playAnimation(EntityPlayer player, int radius) {
         Packet18ArmAnimation packet = new Packet18ArmAnimation(player, 7);
         this.sendPacketNearby(packet, player, radius);
      }
   },
   SIT {
      protected void playAnimation(EntityPlayer player, int radius) {
         player.mount(player);
      }
   },
   SLEEP {
      protected void playAnimation(EntityPlayer player, int radius) {
         Packet17EntityLocationAction packet = new Packet17EntityLocationAction(player, 0, (int)player.locX, (int)player.locY, (int)player.locZ);
         this.sendPacketNearby(packet, player, radius);
      }
   },
   SNEAK {
      protected void playAnimation(EntityPlayer player, int radius) {
         player.getBukkitEntity().setSneaking(true);
         this.sendPacketNearby(new Packet40EntityMetadata(player.id, player.getDataWatcher(), true), player, radius);
      }
   },
   STOP_SITTING {
      protected void playAnimation(EntityPlayer player, int radius) {
         player.mount((Entity)null);
      }
   },
   STOP_SLEEPING {
      protected void playAnimation(EntityPlayer player, int radius) {
         Packet18ArmAnimation packet = new Packet18ArmAnimation(player, 3);
         this.sendPacketNearby(packet, player, radius);
      }
   },
   STOP_SNEAKING {
      protected void playAnimation(EntityPlayer player, int radius) {
         player.getBukkitEntity().setSneaking(false);
         this.sendPacketNearby(new Packet40EntityMetadata(player.id, player.getDataWatcher(), true), player, radius);
      }
   };

   private PlayerAnimation() {
   }

   public void play(Player player) {
      this.play(player, 64);
   }

   public void play(Player player, int radius) {
      this.playAnimation(((CraftPlayer)player).getHandle(), radius);
   }

   protected void playAnimation(EntityPlayer player, int radius) {
      throw new UnsupportedOperationException("unimplemented animation");
   }

   protected void sendPacketNearby(Packet packet, EntityPlayer player, int radius) {
      NMS.sendPacketsNearby(player.getBukkitEntity().getLocation(), Arrays.asList(packet), (double)radius);
   }
}
