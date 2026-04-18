package com.onarandombox.MultiverseCore.utils;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.destination.InvalidDestination;
import com.onarandombox.MultiverseCore.enums.TeleportResult;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

public class SimpleSafeTTeleporter implements com.onarandombox.MultiverseCore.api.SafeTTeleporter {
   private MultiverseCore plugin;
   private static final int DEFAULT_TOLERANCE = 6;
   private static final int DEFAULT_RADIUS = 9;

   public SimpleSafeTTeleporter(MultiverseCore plugin) {
      super();
      this.plugin = plugin;
   }

   public Location getSafeLocation(Location l) {
      return this.getSafeLocation(l, 6, 9);
   }

   public Location getSafeLocation(Location l, int tolerance, int radius) {
      Location safe = this.checkAboveAndBelowLocation(l, tolerance, radius);
      if (safe != null) {
         safe.setX((double)safe.getBlockX() + (double)0.5F);
         safe.setZ((double)safe.getBlockZ() + (double)0.5F);
         this.plugin.log(Level.FINE, "Hey! I found one: " + this.plugin.getLocationManipulation().strCoordsRaw(safe));
      } else {
         this.plugin.log(Level.FINE, "Uh oh! No safe place found!");
      }

      return safe;
   }

   private Location checkAboveAndBelowLocation(Location l, int tolerance, int radius) {
      if (tolerance % 2 != 0) {
         ++tolerance;
      }

      tolerance /= 2;
      this.plugin.log(Level.FINER, "Given Location of: " + this.plugin.getLocationManipulation().strCoordsRaw(l));
      this.plugin.log(Level.FINER, "Checking +-" + tolerance + " with a radius of " + radius);
      Location locToCheck = l.clone();
      Location safe = this.checkAroundLocation(locToCheck, radius);
      if (safe != null) {
         return safe;
      } else {
         for(int currentLevel = 1; currentLevel <= tolerance; ++currentLevel) {
            locToCheck = l.clone();
            locToCheck.add((double)0.0F, (double)currentLevel, (double)0.0F);
            safe = this.checkAroundLocation(locToCheck, radius);
            if (safe != null) {
               return safe;
            }

            locToCheck = l.clone();
            locToCheck.subtract((double)0.0F, (double)currentLevel, (double)0.0F);
            safe = this.checkAroundLocation(locToCheck, radius);
            if (safe != null) {
               return safe;
            }
         }

         return null;
      }
   }

   private Location checkAroundLocation(Location l, int diameter) {
      if (diameter % 2 == 0) {
         ++diameter;
      }

      Location checkLoc = l.clone();

      for(int loopcounter = 3; loopcounter <= diameter; loopcounter += 2) {
         boolean foundSafeArea = this.checkAroundSpecificDiameter(checkLoc, loopcounter);
         if (foundSafeArea) {
            return checkLoc;
         }

         checkLoc = l.clone();
      }

      return null;
   }

   private boolean checkAroundSpecificDiameter(Location checkLoc, int circle) {
      int adjustedCircle = (circle - 1) / 2;
      checkLoc.add((double)adjustedCircle, (double)0.0F, (double)0.0F);
      if (this.plugin.getBlockSafety().playerCanSpawnHereSafely(checkLoc)) {
         return true;
      } else {
         for(int i = 0; i < adjustedCircle; ++i) {
            checkLoc.add((double)0.0F, (double)0.0F, (double)1.0F);
            if (this.plugin.getBlockSafety().playerCanSpawnHereSafely(checkLoc)) {
               return true;
            }
         }

         for(int i = 0; i < adjustedCircle * 2; ++i) {
            checkLoc.add((double)-1.0F, (double)0.0F, (double)0.0F);
            if (this.plugin.getBlockSafety().playerCanSpawnHereSafely(checkLoc)) {
               return true;
            }
         }

         for(int i = 0; i < adjustedCircle * 2; ++i) {
            checkLoc.add((double)0.0F, (double)0.0F, (double)-1.0F);
            if (this.plugin.getBlockSafety().playerCanSpawnHereSafely(checkLoc)) {
               return true;
            }
         }

         for(int i = 0; i < adjustedCircle * 2; ++i) {
            checkLoc.add((double)1.0F, (double)0.0F, (double)0.0F);
            if (this.plugin.getBlockSafety().playerCanSpawnHereSafely(checkLoc)) {
               return true;
            }
         }

         for(int i = 0; i < adjustedCircle - 1; ++i) {
            checkLoc.add((double)0.0F, (double)0.0F, (double)1.0F);
            if (this.plugin.getBlockSafety().playerCanSpawnHereSafely(checkLoc)) {
               return true;
            }
         }

         return false;
      }
   }

   public TeleportResult safelyTeleport(CommandSender teleporter, Entity teleportee, MVDestination d) {
      if (d instanceof InvalidDestination) {
         this.plugin.log(Level.FINER, "Entity tried to teleport to an invalid destination");
         return TeleportResult.FAIL_INVALID;
      } else {
         Player teleporteePlayer = null;
         if (teleportee instanceof Player) {
            teleporteePlayer = (Player)teleportee;
         } else if (teleportee.getPassenger() instanceof Player) {
            teleporteePlayer = (Player)teleportee.getPassenger();
         }

         if (teleporteePlayer == null) {
            return TeleportResult.FAIL_INVALID;
         } else {
            MultiverseCore.addPlayerToTeleportQueue(teleporter.getName(), teleporteePlayer.getName());
            Location safeLoc = d.getLocation(teleportee);
            if (d.useSafeTeleporter()) {
               safeLoc = this.getSafeLocation(teleportee, d);
            }

            if (safeLoc != null) {
               if (teleportee.teleport(safeLoc)) {
                  if (!d.getVelocity().equals(new Vector(0, 0, 0))) {
                     teleportee.setVelocity(d.getVelocity());
                  }

                  return TeleportResult.SUCCESS;
               } else {
                  return TeleportResult.FAIL_OTHER;
               }
            } else {
               return TeleportResult.FAIL_UNSAFE;
            }
         }
      }
   }

   public TeleportResult safelyTeleport(CommandSender teleporter, Entity teleportee, Location location, boolean safely) {
      if (safely) {
         location = this.getSafeLocation(location);
      }

      if (location != null) {
         return teleportee.teleport(location) ? TeleportResult.SUCCESS : TeleportResult.FAIL_OTHER;
      } else {
         return TeleportResult.FAIL_UNSAFE;
      }
   }

   public Location getSafeLocation(Entity e, MVDestination d) {
      Location l = d.getLocation(e);
      if (this.plugin.getBlockSafety().playerCanSpawnHereSafely(l)) {
         this.plugin.log(Level.FINE, "The first location you gave me was safe.");
         return l;
      } else {
         if (e instanceof Minecart) {
            Minecart m = (Minecart)e;
            if (!this.plugin.getBlockSafety().canSpawnCartSafely(m)) {
               return null;
            }
         } else if (e instanceof Vehicle) {
            Vehicle v = (Vehicle)e;
            if (!this.plugin.getBlockSafety().canSpawnVehicleSafely(v)) {
               return null;
            }
         }

         Location safeLocation = this.getSafeLocation(l);
         if (safeLocation != null) {
            if (e instanceof Minecart && !this.plugin.getBlockSafety().isEntitiyOnTrack(safeLocation)) {
               safeLocation.setY((double)safeLocation.getBlockY() + (double)0.5F);
               this.plugin.log(Level.FINER, "Player was inside a minecart. Offsetting Y location.");
            }

            this.plugin.log(Level.FINE, "Had to look for a bit, but I found a safe place for ya!");
            return safeLocation;
         } else {
            if (e instanceof Player) {
               Player p = (Player)e;
               this.plugin.getMessaging().sendMessage(p, "No safe locations found!", false);
               this.plugin.log(Level.FINER, "No safe location found for " + p.getName());
            } else if (e.getPassenger() instanceof Player) {
               Player p = (Player)e.getPassenger();
               this.plugin.getMessaging().sendMessage(p, "No safe locations found!", false);
               this.plugin.log(Level.FINER, "No safe location found for " + p.getName());
            }

            this.plugin.log(Level.FINE, "Sorry champ, you're basically trying to teleport into a minefield. I should just kill you now.");
            return null;
         }
      }
   }

   public Location findPortalBlockNextTo(Location l) {
      Block b = l.getWorld().getBlockAt(l);
      Location foundLocation = null;
      if (b.getType() == Material.PORTAL) {
         return l;
      } else {
         if (b.getRelative(BlockFace.NORTH).getType() == Material.PORTAL) {
            foundLocation = getCloserBlock(l, b.getRelative(BlockFace.NORTH).getLocation(), foundLocation);
         }

         if (b.getRelative(BlockFace.SOUTH).getType() == Material.PORTAL) {
            foundLocation = getCloserBlock(l, b.getRelative(BlockFace.SOUTH).getLocation(), foundLocation);
         }

         if (b.getRelative(BlockFace.EAST).getType() == Material.PORTAL) {
            foundLocation = getCloserBlock(l, b.getRelative(BlockFace.EAST).getLocation(), foundLocation);
         }

         if (b.getRelative(BlockFace.WEST).getType() == Material.PORTAL) {
            foundLocation = getCloserBlock(l, b.getRelative(BlockFace.WEST).getLocation(), foundLocation);
         }

         return foundLocation;
      }
   }

   private static Location getCloserBlock(Location source, Location blockA, Location blockB) {
      if (blockB == null) {
         return blockA;
      } else {
         blockA.add((double)0.5F, (double)0.0F, (double)0.5F);
         blockB.add((double)0.5F, (double)0.0F, (double)0.5F);
         double testA = source.distance(blockA);
         double testB = source.distance(blockB);
         return testA <= testB ? blockA : blockB;
      }
   }
}
