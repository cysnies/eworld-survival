package com.onarandombox.MultiverseCore.destination;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.Core;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class WorldDestination implements MVDestination {
   private boolean isValid;
   private MultiverseWorld world;
   private float yaw = -1.0F;
   private String direction = "";

   public WorldDestination() {
      super();
   }

   public String getIdentifier() {
      return "w";
   }

   public boolean isThisType(JavaPlugin plugin, String destination) {
      String[] items = destination.split(":");
      if (items.length > 3) {
         return false;
      } else if (items.length == 1 && ((MultiverseCore)plugin).getMVWorldManager().isMVWorld(items[0])) {
         return true;
      } else if (items.length == 2 && ((MultiverseCore)plugin).getMVWorldManager().isMVWorld(items[0])) {
         return true;
      } else {
         return items[0].equalsIgnoreCase("w") && ((MultiverseCore)plugin).getMVWorldManager().isMVWorld(items[1]);
      }
   }

   public Location getLocation(Entity e) {
      Location spawnLoc = getAcurateSpawnLocation(e, this.world);
      if (this.yaw >= 0.0F) {
         spawnLoc.setYaw(this.yaw);
      }

      return spawnLoc;
   }

   private static Location getAcurateSpawnLocation(Entity e, MultiverseWorld world) {
      return world != null ? world.getSpawnLocation() : e.getWorld().getSpawnLocation().add((double)0.5F, (double)0.0F, (double)0.5F);
   }

   public boolean isValid() {
      return this.isValid;
   }

   public void setDestination(JavaPlugin plugin, String destination) {
      Core core = (Core)plugin;
      String[] items = destination.split(":");
      if (items.length > 3) {
         this.isValid = false;
      } else if (items.length == 1 && ((MultiverseCore)plugin).getMVWorldManager().isMVWorld(items[0])) {
         this.isValid = true;
         this.world = core.getMVWorldManager().getMVWorld(items[0]);
      } else if (items.length == 2 && ((MultiverseCore)plugin).getMVWorldManager().isMVWorld(items[0])) {
         this.world = core.getMVWorldManager().getMVWorld(items[0]);
         this.yaw = core.getLocationManipulation().getYaw(items[1]);
      } else {
         if (items[0].equalsIgnoreCase("w") && ((MultiverseCore)plugin).getMVWorldManager().isMVWorld(items[1])) {
            this.world = ((MultiverseCore)plugin).getMVWorldManager().getMVWorld(items[1]);
            this.isValid = true;
            if (items.length == 3) {
               this.yaw = core.getLocationManipulation().getYaw(items[2]);
            }
         }

      }
   }

   public String getType() {
      return "World";
   }

   public String getName() {
      return this.world.getColoredWorldString();
   }

   public String toString() {
      return this.direction.length() > 0 && this.yaw >= 0.0F ? this.world.getCBWorld().getName() + ":" + this.direction : this.world.getCBWorld().getName();
   }

   public String getRequiredPermission() {
      return "multiverse.access." + this.world.getName();
   }

   public Vector getVelocity() {
      return new Vector(0, 0, 0);
   }

   public boolean useSafeTeleporter() {
      return true;
   }
}
