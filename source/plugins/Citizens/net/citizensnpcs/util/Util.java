package net.citizensnpcs.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.util.Random;
import net.citizensnpcs.api.event.NPCCollisionEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Util {
   private static final Location AT_LOCATION = new Location((World)null, (double)0.0F, (double)0.0F, (double)0.0F);
   private static final Location FROM_LOCATION = new Location((World)null, (double)0.0F, (double)0.0F, (double)0.0F);

   private Util() {
      super();
   }

   public static void assumePose(LivingEntity entity, float yaw, float pitch) {
      NMS.look(entity, yaw, pitch);
   }

   public static void callCollisionEvent(NPC npc, Entity entity) {
      if (NPCCollisionEvent.getHandlerList().getRegisteredListeners().length > 0) {
         Bukkit.getPluginManager().callEvent(new NPCCollisionEvent(npc, entity));
      }

   }

   public static NPCPushEvent callPushEvent(NPC npc, Vector vector) {
      NPCPushEvent event = new NPCPushEvent(npc, vector);
      event.setCancelled((Boolean)npc.data().get("protected", true));
      Bukkit.getPluginManager().callEvent(event);
      return event;
   }

   public static void faceEntity(LivingEntity from, LivingEntity at) {
      if (from.getWorld() == at.getWorld()) {
         faceLocation(from, at.getLocation(AT_LOCATION));
      }
   }

   public static void faceLocation(LivingEntity from, Location to) {
      if (from.getWorld() == to.getWorld()) {
         Location fromLocation = from.getLocation(FROM_LOCATION);
         double xDiff = to.getX() - fromLocation.getX();
         double yDiff = to.getY() - fromLocation.getY();
         double zDiff = to.getZ() - fromLocation.getZ();
         double distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
         double distanceY = Math.sqrt(distanceXZ * distanceXZ + yDiff * yDiff);
         double yaw = Math.toDegrees(Math.acos(xDiff / distanceXZ));
         double pitch = Math.toDegrees(Math.acos(yDiff / distanceY)) - (double)90.0F;
         if (zDiff < (double)0.0F) {
            yaw += Math.abs((double)180.0F - yaw) * (double)2.0F;
         }

         NMS.look(from, (float)yaw - 90.0F, (float)pitch);
      }
   }

   public static Random getFastRandom() {
      return new XORShiftRNG();
   }

   public static String getMinecraftVersion() {
      String raw = Bukkit.getVersion();
      int start = raw.indexOf("MC:");
      if (start == -1) {
         return raw;
      } else {
         start += 4;
         int end = raw.indexOf(41, start);
         return raw.substring(start, end);
      }
   }

   public static boolean isLoaded(Location location) {
      if (location.getWorld() == null) {
         return false;
      } else {
         int chunkX = location.getBlockX() >> 4;
         int chunkZ = location.getBlockZ() >> 4;
         return location.getWorld().isChunkLoaded(chunkX, chunkZ);
      }
   }

   public static String listValuesPretty(Enum[] values) {
      return Joiner.on(", ").join(values).toLowerCase().replace('_', ' ');
   }

   public static boolean locationWithinRange(Location current, Location target, double range) {
      if (current != null && target != null) {
         if (current.getWorld() != target.getWorld()) {
            return false;
         } else {
            return current.distanceSquared(target) < Math.pow(range, (double)2.0F);
         }
      } else {
         return false;
      }
   }

   public static EntityType matchEntityType(String toMatch) {
      EntityType type = EntityType.fromName(toMatch);
      return type != null ? type : (EntityType)matchEnum(EntityType.values(), toMatch);
   }

   public static Enum matchEnum(Enum[] values, String toMatch) {
      toMatch = toMatch.toLowerCase().replace('-', '_').replace(' ', '_');

      for(Enum check : values) {
         if (toMatch.equals(check.name().toLowerCase())) {
            return check;
         }
      }

      for(Enum check : values) {
         String name = check.name().toLowerCase();
         if (name.replace("_", "").equals(toMatch) || name.matches(toMatch) || name.startsWith(toMatch)) {
            return check;
         }
      }

      return null;
   }

   public static boolean matchesItemInHand(Player player, String setting) {
      if (setting.contains("*")) {
         return true;
      } else {
         for(String part : Splitter.on(',').split(setting)) {
            if (Material.matchMaterial(part) == player.getItemInHand().getType()) {
               return true;
            }
         }

         return false;
      }
   }

   public static String prettyEnum(Enum e) {
      return e.name().toLowerCase().replace('_', ' ');
   }
}
