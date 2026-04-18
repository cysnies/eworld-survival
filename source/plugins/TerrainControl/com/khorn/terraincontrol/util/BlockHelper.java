package com.khorn.terraincontrol.util;

import com.khorn.terraincontrol.DefaultMaterial;

public class BlockHelper {
   public BlockHelper() {
      super();
   }

   public static int rotateData(int type, int data) {
      DefaultMaterial mat = DefaultMaterial.getMaterial(type);
      if (mat == DefaultMaterial.UNKNOWN_BLOCK) {
         return data;
      } else {
         switch (mat) {
            case TORCH:
            case REDSTONE_TORCH_OFF:
            case REDSTONE_TORCH_ON:
               switch (data) {
                  case 1:
                     return 4;
                  case 2:
                     return 3;
                  case 3:
                     return 1;
                  case 4:
                     return 2;
                  default:
                     return data;
               }
            case RAILS:
               switch (data) {
                  case 6:
                     return 9;
                  case 7:
                     return 6;
                  case 8:
                     return 7;
                  case 9:
                     return 8;
               }
            case POWERED_RAIL:
            case DETECTOR_RAIL:
            case ACTIVATOR_RAIL:
               int power = data & -8;
               switch (data & 7) {
                  case 0:
                     return 1 | power;
                  case 1:
                     return 0 | power;
                  case 2:
                     return 4 | power;
                  case 3:
                     return 5 | power;
                  case 4:
                     return 3 | power;
                  case 5:
                     return 2 | power;
                  default:
                     return data;
               }
            case LOG:
            case HAY_BLOCK:
               switch (data / 4) {
                  case 1:
                     return data + 4;
                  case 2:
                     return data - 4;
                  default:
                     return data;
               }
            case WOOD_STAIRS:
            case BIRCH_WOOD_STAIRS:
            case SPRUCE_WOOD_STAIRS:
            case JUNGLE_WOOD_STAIRS:
            case COBBLESTONE_STAIRS:
            case BRICK_STAIRS:
            case SMOOTH_STAIRS:
            case NETHER_BRICK_STAIRS:
            case SANDSTONE_STAIRS:
            case QUARTZ_STAIRS:
               switch (data) {
                  case 0:
                     return 3;
                  case 1:
                     return 2;
                  case 2:
                     return 0;
                  case 3:
                     return 1;
                  case 4:
                     return 7;
                  case 5:
                     return 6;
                  case 6:
                     return 4;
                  case 7:
                     return 5;
                  default:
                     return data;
               }
            case LEVER:
            case STONE_BUTTON:
            case WOOD_BUTTON:
               int thrown = data & 8;
               int withoutThrown = data & -9;
               switch (withoutThrown) {
                  case 1:
                     return 4 | thrown;
                  case 2:
                     return 3 | thrown;
                  case 3:
                     return 1 | thrown;
                  case 4:
                     return 2 | thrown;
                  default:
                     return data;
               }
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
               int topHalf = data & 8;
               int swung = data & 4;
               int withoutFlags = data & -13;
               switch (withoutFlags) {
                  case 0:
                     return 3 | topHalf | swung;
                  case 1:
                     return 0 | topHalf | swung;
                  case 2:
                     return 1 | topHalf | swung;
                  case 3:
                     return 2 | topHalf | swung;
                  default:
                     return data;
               }
            case SIGN_POST:
               return (data + 12) % 16;
            case LADDER:
            case WALL_SIGN:
            case CHEST:
            case ENDER_CHEST:
            case TRAPPED_CHEST:
            case FURNACE:
            case BURNING_FURNACE:
               switch (data) {
                  case 2:
                     return 4;
                  case 3:
                     return 5;
                  case 4:
                     return 3;
                  case 5:
                     return 2;
                  default:
                     return data;
               }
            case DISPENSER:
            case DROPPER:
            case HOPPER:
               int dispPower = data & 8;
               switch (data & -9) {
                  case 2:
                     return 4 | dispPower;
                  case 3:
                     return 5 | dispPower;
                  case 4:
                     return 3 | dispPower;
                  case 5:
                     return 2 | dispPower;
                  default:
                     return data;
               }
            case PUMPKIN:
            case JACK_O_LANTERN:
               switch (data) {
                  case 0:
                     return 3;
                  case 1:
                     return 0;
                  case 2:
                     return 1;
                  case 3:
                     return 2;
                  default:
                     return data;
               }
            case DIODE_BLOCK_OFF:
            case DIODE_BLOCK_ON:
            case REDSTONE_COMPARATOR_OFF:
            case REDSTONE_COMPARATOR_ON:
            case BED_BLOCK:
               int dir = data & 3;
               int withoutDir = data - dir;
               switch (dir) {
                  case 0:
                     return 3 | withoutDir;
                  case 1:
                     return 0 | withoutDir;
                  case 2:
                     return 1 | withoutDir;
                  case 3:
                     return 2 | withoutDir;
                  default:
                     return data;
               }
            case TRAP_DOOR:
               int withoutOrientation = data & -4;
               int orientation = data & 3;
               switch (orientation) {
                  case 0:
                     return 2 | withoutOrientation;
                  case 1:
                     return 3 | withoutOrientation;
                  case 2:
                     return 1 | withoutOrientation;
                  case 3:
                     return 0 | withoutOrientation;
               }
            case PISTON_BASE:
            case PISTON_STICKY_BASE:
            case PISTON_EXTENSION:
               int rest = data & -8;
               switch (data & 7) {
                  case 2:
                     return 4 | rest;
                  case 3:
                     return 5 | rest;
                  case 4:
                     return 3 | rest;
                  case 5:
                     return 2 | rest;
                  default:
                     return data;
               }
            case HUGE_MUSHROOM_1:
            case HUGE_MUSHROOM_2:
               if (data >= 10) {
                  return data;
               }

               return data * 7 % 10;
            case VINE:
               return (data >> 1 | data << 3) & 15;
            case FENCE_GATE:
               return data + 3 & 3 | data & -4;
            case COCOA:
            case TRIPWIRE_HOOK:
               int rotationData = data % 4;
               if (rotationData == 0) {
                  return data + 3;
               }

               return data - 1;
            case ANVIL:
               if (data % 2 == 0) {
                  return data + 1;
               }

               return data - 1;
            case QUARTZ_BLOCK:
               if (data == 3) {
                  return 4;
               }

               if (data == 4) {
                  return 3;
               }
         }

         return data;
      }
   }
}
