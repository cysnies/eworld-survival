package com.onarandombox.MultiverseCore.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;

/** @deprecated */
@Deprecated
public class BlockSafety {
   public BlockSafety() {
      super();
   }

   public boolean isBlockAboveAir(Location l) {
      Location downOne = l.clone();
      downOne.setY(downOne.getY() - (double)1.0F);
      return downOne.getBlock().getType() == Material.AIR;
   }

   private boolean blockIsNotSafe(World world, double x, double y, double z) {
      return !this.playerCanSpawnHereSafely(world, x, y, z);
   }

   public boolean playerCanSpawnHereSafely(World world, double x, double y, double z) {
      Location l = new Location(world, x, y, z);
      return this.playerCanSpawnHereSafely(l);
   }

   public boolean playerCanSpawnHereSafely(Location l) {
      if (l == null) {
         return false;
      } else {
         World world = l.getWorld();
         Location actual = l.clone();
         Location upOne = l.clone();
         Location downOne = l.clone();
         upOne.setY(upOne.getY() + (double)1.0F);
         downOne.setY(downOne.getY() - (double)1.0F);
         if (!this.isSolidBlock(world.getBlockAt(actual).getType()) && !this.isSolidBlock(upOne.getBlock().getType())) {
            if (downOne.getBlock().getType() != Material.LAVA && downOne.getBlock().getType() != Material.STATIONARY_LAVA) {
               if (downOne.getBlock().getType() == Material.FIRE) {
                  CoreLogging.finest("There's fire below! (%s)[%s]", actual.getBlock().getType(), this.isSolidBlock(actual.getBlock().getType()));
                  return false;
               } else if (this.isBlockAboveAir(actual)) {
                  CoreLogging.finest("Is block above air [%s]", this.isBlockAboveAir(actual));
                  CoreLogging.finest("Has 2 blocks of water below [%s]", this.hasTwoBlocksofWaterBelow(actual));
                  return this.hasTwoBlocksofWaterBelow(actual);
               } else {
                  return true;
               }
            } else {
               CoreLogging.finest("Error Here (downOne)? (%s)[%s]", downOne.getBlock().getType(), this.isSolidBlock(downOne.getBlock().getType()));
               return false;
            }
         } else {
            CoreLogging.finest("Error Here (Actual)? (%s)[%s]", actual.getBlock().getType(), this.isSolidBlock(actual.getBlock().getType()));
            CoreLogging.finest("Error Here (upOne)? (%s)[%s]", upOne.getBlock().getType(), this.isSolidBlock(upOne.getBlock().getType()));
            return false;
         }
      }
   }

   public Location getTopBlock(Location l) {
      Location check = l.clone();
      check.setY((double)127.0F);

      while(check.getY() > (double)0.0F) {
         if (this.playerCanSpawnHereSafely(check)) {
            return check;
         }

         check.setY(check.getY() - (double)1.0F);
      }

      return null;
   }

   public Location getBottomBlock(Location l) {
      Location check = l.clone();
      check.setY((double)0.0F);

      while(check.getY() < (double)127.0F) {
         if (this.playerCanSpawnHereSafely(check)) {
            return check;
         }

         check.setY(check.getY() + (double)1.0F);
      }

      return null;
   }

   private boolean isSolidBlock(Material type) {
      switch (type) {
         case AIR:
            return false;
         case SNOW:
            return false;
         case TRAP_DOOR:
            return false;
         case TORCH:
            return false;
         case YELLOW_FLOWER:
            return false;
         case RED_ROSE:
            return false;
         case RED_MUSHROOM:
            return false;
         case BROWN_MUSHROOM:
            return false;
         case REDSTONE:
            return false;
         case REDSTONE_WIRE:
            return false;
         case RAILS:
            return false;
         case POWERED_RAIL:
            return false;
         case REDSTONE_TORCH_ON:
            return false;
         case REDSTONE_TORCH_OFF:
            return false;
         case DEAD_BUSH:
            return false;
         case SAPLING:
            return false;
         case STONE_BUTTON:
            return false;
         case LEVER:
            return false;
         case LONG_GRASS:
            return false;
         case PORTAL:
            return false;
         case STONE_PLATE:
            return false;
         case WOOD_PLATE:
            return false;
         case SEEDS:
            return false;
         case SUGAR_CANE_BLOCK:
            return false;
         case WALL_SIGN:
            return false;
         case SIGN_POST:
            return false;
         case WOODEN_DOOR:
            return false;
         case STATIONARY_WATER:
            return false;
         case WATER:
            return false;
         default:
            return true;
      }
   }

   public boolean isEntitiyOnTrack(Location l) {
      Material currentBlock = l.getBlock().getType();
      return currentBlock == Material.POWERED_RAIL || currentBlock == Material.DETECTOR_RAIL || currentBlock == Material.RAILS;
   }

   private void showDangers(Location l) {
      Location actual = new Location(l.getWorld(), l.getX(), l.getY(), l.getZ());
      Location upOne = new Location(l.getWorld(), l.getX(), l.getY(), l.getZ());
      Location downOne = new Location(l.getWorld(), l.getX(), l.getY(), l.getZ());
      upOne.setY(upOne.getY() + (double)1.0F);
      downOne.setY(downOne.getY() - (double)1.0F);
      System.out.print("Location Up:   " + upOne.getBlock().getType());
      System.out.print("               " + upOne);
      System.out.print("Location:      " + actual.getBlock().getType());
      System.out.print("               " + actual);
      System.out.print("Location Down: " + downOne.getBlock().getType());
      System.out.print("               " + downOne);
   }

   private boolean hasTwoBlocksofWaterBelow(Location l) {
      if (l.getBlockY() < 0) {
         return false;
      } else {
         Location oneBelow = l.clone();
         oneBelow.subtract((double)0.0F, (double)1.0F, (double)0.0F);
         if (oneBelow.getBlock().getType() != Material.WATER && oneBelow.getBlock().getType() != Material.STATIONARY_WATER) {
            return oneBelow.getBlock().getType() != Material.AIR ? false : this.hasTwoBlocksofWaterBelow(oneBelow);
         } else {
            Location twoBelow = oneBelow.clone();
            twoBelow.subtract((double)0.0F, (double)1.0F, (double)0.0F);
            return oneBelow.getBlock().getType() == Material.WATER || oneBelow.getBlock().getType() == Material.STATIONARY_WATER;
         }
      }
   }

   public boolean canSpawnCartSafely(Minecart cart) {
      if (this.isBlockAboveAir(cart.getLocation())) {
         return true;
      } else {
         return this.isEntitiyOnTrack(LocationManipulation.getNextBlock(cart));
      }
   }

   public boolean canSpawnVehicleSafely(Vehicle vehicle) {
      return this.isBlockAboveAir(vehicle.getLocation());
   }
}
