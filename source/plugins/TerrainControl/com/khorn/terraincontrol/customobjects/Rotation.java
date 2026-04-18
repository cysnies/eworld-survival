package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.exception.InvalidConfigException;
import java.util.Random;

public enum Rotation {
   NORTH(0),
   WEST(1),
   SOUTH(2),
   EAST(3);

   private final int ROTATION_ID;

   private Rotation(int id) {
      this.ROTATION_ID = id;
   }

   public int getRotationId() {
      return this.ROTATION_ID;
   }

   public static Rotation getRotation(int id) {
      for(Rotation rotation : values()) {
         if (rotation.ROTATION_ID == id) {
            return rotation;
         }
      }

      return null;
   }

   public static Rotation getRandomRotation(Random random) {
      return values()[random.nextInt(values().length)];
   }

   public static Rotation next(Rotation rotation) {
      int id = rotation.getRotationId();
      ++id;
      if (id >= values().length) {
         id = 0;
      }

      return getRotation(id);
   }

   public static Rotation getRotation(String string) throws InvalidConfigException {
      Rotation rotation = null;

      try {
         rotation = getRotation(Integer.parseInt(string));
      } catch (NumberFormatException var4) {
      }

      if (rotation != null) {
         return rotation;
      } else {
         try {
            rotation = valueOf(string.toUpperCase());
         } catch (IllegalArgumentException var3) {
         }

         if (rotation != null) {
            return rotation;
         } else {
            throw new InvalidConfigException("Unknown rotation: " + string);
         }
      }
   }
}
