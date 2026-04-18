package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalWorld;
import java.util.Random;

public class CustomObjectCoordinate {
   private final CustomObject object;
   private final Rotation rotation;
   private final int x;
   private final int y;
   private final int z;

   public CustomObjectCoordinate(CustomObject object, Rotation rotation, int x, int y, int z) {
      super();
      this.object = object;
      this.rotation = rotation;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getZ() {
      return this.z;
   }

   public CustomObject getObject() {
      return this.object;
   }

   public StructuredCustomObject getStructuredObject() {
      return (StructuredCustomObject)this.object;
   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public boolean spawnWithChecks(LocalWorld world, SpawnHeight height, Random random) {
      int y = this.getCorrectY(world, height);
      return !this.object.canSpawnAt(world, this.rotation, this.x, y, this.z) ? false : this.object.spawnForced(world, random, this.rotation, this.x, y, this.z);
   }

   public boolean equals(Object otherObject) {
      if (otherObject == null) {
         return false;
      } else if (!(otherObject instanceof CustomObjectCoordinate)) {
         return false;
      } else {
         CustomObjectCoordinate otherCoord = (CustomObjectCoordinate)otherObject;
         if (otherCoord.x != this.x) {
            return false;
         } else if (otherCoord.y != this.y) {
            return false;
         } else if (otherCoord.z != this.z) {
            return false;
         } else if (!otherCoord.rotation.equals(this.rotation)) {
            return false;
         } else {
            return otherCoord.object.getName().equals(this.object.getName());
         }
      }
   }

   public int hashCode() {
      return Integer.valueOf(this.x).hashCode() >> 13 ^ Integer.valueOf(this.y).hashCode() >> 7 ^ Integer.valueOf(this.z).hashCode() ^ this.object.getName().hashCode() ^ this.rotation.toString().hashCode();
   }

   private int getCorrectY(LocalWorld world, SpawnHeight height) {
      if (height.equals(CustomObjectCoordinate.SpawnHeight.HIGHEST_BLOCK)) {
         return world.getHighestBlockYAt(this.x, this.z);
      } else {
         return height.equals(CustomObjectCoordinate.SpawnHeight.HIGHEST_SOLID_BLOCK) ? world.getSolidHeight(this.x, this.z) : this.y;
      }
   }

   public static enum SpawnHeight {
      PROVIDED,
      HIGHEST_BLOCK,
      HIGHEST_SOLID_BLOCK;

      private SpawnHeight() {
      }
   }
}
