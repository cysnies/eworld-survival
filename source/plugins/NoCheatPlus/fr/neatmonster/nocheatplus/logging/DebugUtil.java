package fr.neatmonster.nocheatplus.logging;

import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.build.BuildParameters;
import java.util.Arrays;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class DebugUtil {
   public DebugUtil() {
      super();
   }

   public static boolean isSamePos(double x1, double y1, double z1, double x2, double y2, double z2) {
      return x1 == x2 && y1 == y2 && z1 == z2;
   }

   public static boolean isSamePos(Location loc1, Location loc2) {
      return isSamePos(loc1.getX(), loc1.getY(), loc1.getZ(), loc2.getX(), loc2.getY(), loc2.getZ());
   }

   public static void addLocation(double x, double y, double z, StringBuilder builder) {
      builder.append(x + ", " + y + ", " + z);
   }

   public static void addLocation(Location loc, StringBuilder builder) {
      addLocation(loc.getX(), loc.getY(), loc.getZ(), builder);
   }

   public static void addLocation(PlayerLocation loc, StringBuilder builder) {
      addLocation(loc.getX(), loc.getY(), loc.getZ(), builder);
   }

   public static void addFormattedLocation(double x, double y, double z, StringBuilder builder) {
      builder.append(StringUtil.fdec3.format(x) + ", " + StringUtil.fdec3.format(y) + ", " + StringUtil.fdec3.format(z));
   }

   public static void addFormattedLocation(Location loc, StringBuilder builder) {
      addFormattedLocation(loc.getX(), loc.getY(), loc.getZ(), builder);
   }

   public static void addFormattedLocation(PlayerLocation loc, StringBuilder builder) {
      addFormattedLocation(loc.getX(), loc.getY(), loc.getZ(), builder);
   }

   public static void addMove(double fromX, double fromY, double fromZ, double toX, double toY, double toZ, StringBuilder builder) {
      builder.append("from: ");
      addLocation(fromX, fromY, fromZ, builder);
      builder.append("\nto: ");
      addLocation(toX, toY, toZ, builder);
   }

   public static void addFormattedMove(double fromX, double fromY, double fromZ, double toX, double toY, double toZ, StringBuilder builder) {
      addFormattedLocation(fromX, fromY, fromZ, builder);
      builder.append(" -> ");
      addFormattedLocation(toX, toY, toZ, builder);
   }

   public static void addFormattedMove(PlayerLocation from, PlayerLocation to, Location loc, StringBuilder builder) {
      if (loc != null && !from.isSamePos(loc)) {
         builder.append("(");
         addFormattedLocation(loc, builder);
         builder.append(") ");
      }

      addFormattedMove(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ(), builder);
   }

   public static void addMove(PlayerLocation from, PlayerLocation to, Location loc, StringBuilder builder) {
      if (loc != null && !from.isSamePos(loc)) {
         builder.append("Location: ");
         addLocation(loc, builder);
         builder.append("\n");
      }

      addMove(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ(), builder);
   }

   public static void addFormattedMove(Location from, Location to, Location loc, StringBuilder builder) {
      if (loc != null && !isSamePos(from, loc)) {
         builder.append("(");
         addFormattedLocation(loc, builder);
         builder.append(") ");
      }

      addFormattedMove(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ(), builder);
   }

   public static void addMove(Location from, Location to, Location loc, StringBuilder builder) {
      if (loc != null && !isSamePos(from, loc)) {
         builder.append("Location: ");
         addLocation(loc, builder);
         builder.append("\n");
      }

      addMove(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ(), builder);
   }

   public static void outputMoveDebug(Player player, PlayerLocation from, PlayerLocation to, double maxYOnGround, MCAccess mcAccess) {
      StringBuilder builder = new StringBuilder(250);
      Location loc = player.getLocation();
      if (BuildParameters.debugLevel > 0) {
         builder.append("\n-------------- MOVE --------------\n");
         builder.append(player.getName() + " " + from.getWorld().getName() + ":\n");
         addMove(from, to, loc, builder);
      } else {
         builder.append(player.getName() + " " + from.getWorld().getName() + " ");
         addFormattedMove(from, to, loc, builder);
      }

      double jump = mcAccess.getJumpAmplifier(player);
      double speed = mcAccess.getFasterMovementAmplifier(player);
      if (BuildParameters.debugLevel > 0) {
         try {
            builder.append("\n(walkspeed=" + player.getWalkSpeed() + " flyspeed=" + player.getFlySpeed() + ")");
         } catch (Throwable var13) {
         }

         if (player.isSprinting()) {
            builder.append("(sprinting)");
         }

         if (player.isSneaking()) {
            builder.append("(sneaking)");
         }
      }

      if (speed != Double.NEGATIVE_INFINITY || jump != Double.NEGATIVE_INFINITY) {
         builder.append(" (" + (speed != Double.NEGATIVE_INFINITY ? "e_speed=" + (speed + (double)1.0F) : "") + (jump != Double.NEGATIVE_INFINITY ? "e_jump=" + (jump + (double)1.0F) : "") + ")");
      }

      System.out.print(builder.toString());
      if (BuildParameters.debugLevel > 0) {
         builder.setLength(0);
         from.collectBlockFlags(maxYOnGround);
         if (from.getBlockFlags() != 0L) {
            builder.append("\nfrom flags: " + StringUtil.join(BlockProperties.getFlagNames(from.getBlockFlags()), "+"));
         }

         if (from.getTypeId() != 0) {
            addBlockInfo(builder, from, "\nfrom");
         }

         if (from.getTypeIdBelow() != 0) {
            addBlockBelowInfo(builder, from, "\nfrom");
         }

         if (!from.isOnGround() && from.isOnGround((double)0.5F)) {
            builder.append(" (ground within 0.5)");
         }

         to.collectBlockFlags(maxYOnGround);
         if (to.getBlockFlags() != 0L) {
            builder.append("\nto flags: " + StringUtil.join(BlockProperties.getFlagNames(to.getBlockFlags()), "+"));
         }

         if (to.getTypeId() != 0) {
            addBlockInfo(builder, to, "\nto");
         }

         if (to.getTypeIdBelow() != 0) {
            addBlockBelowInfo(builder, to, "\nto");
         }

         if (!to.isOnGround() && to.isOnGround((double)0.5F)) {
            builder.append(" (ground within 0.5)");
         }

         System.out.print(builder.toString());
      }

   }

   public static void addBlockBelowInfo(StringBuilder builder, PlayerLocation loc, String tag) {
      builder.append(tag + " below id=" + loc.getTypeIdBelow() + " data=" + loc.getData(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ()) + " shape=" + Arrays.toString(loc.getBlockCache().getBounds(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ())));
   }

   public static void addBlockInfo(StringBuilder builder, PlayerLocation loc, String tag) {
      builder.append(tag + " id=" + loc.getTypeId() + " data=" + loc.getData() + " shape=" + Arrays.toString(loc.getBlockCache().getBounds(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())));
   }

   public static void outputDebugVehicleMove(Player player, Entity vehicle, Location from, Location to, boolean fake) {
      StringBuilder builder = new StringBuilder(250);
      Location vLoc = vehicle.getLocation();
      Location loc = player.getLocation();
      Entity actualVehicle = player.getVehicle();
      boolean wrongVehicle = actualVehicle == null || actualVehicle.getEntityId() != vehicle.getEntityId();
      if (BuildParameters.debugLevel > 0) {
         builder.append("\n-------------- VEHICLE MOVE " + (fake ? "(fake)" : "") + "--------------\n");
         builder.append(player.getName() + " " + from.getWorld().getName() + ":\n");
         addMove((Location)from, (Location)to, (Location)null, builder);
         builder.append("\n Vehicle: ");
         addLocation(vLoc, builder);
         builder.append("\n Player: ");
         addLocation(loc, builder);
      } else {
         builder.append(player.getName() + " " + from.getWorld().getName() + "veh." + (fake ? "(fake)" : "") + " ");
         addFormattedMove((Location)from, (Location)to, (Location)null, builder);
         builder.append("\n Vehicle: ");
         addFormattedLocation(vLoc, builder);
         builder.append(" Player: ");
         addFormattedLocation(loc, builder);
      }

      builder.append("\n Vehicle type: " + vehicle.getType() + (wrongVehicle ? (actualVehicle == null ? " (exited?)" : " actual: " + actualVehicle.getType()) : ""));
      System.out.print(builder.toString());
   }
}
