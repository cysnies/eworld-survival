package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import java.util.List;
import java.util.Random;

public class VinesGen extends Resource {
   private int minAltitude;
   private int maxAltitude;
   public static final int[] d = new int[]{-1, -1, 2, 0, 1, 3};
   public static final int[] OPPOSITE_FACING = new int[]{1, 0, 3, 2, 5, 4};

   public VinesGen() {
      super();
   }

   public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z) {
      int _x = x;
      int _z = z;

      for(int y = this.minAltitude; y < this.maxAltitude; ++y) {
         if (world.isEmpty(_x, y, _z)) {
            for(int direction = 2; direction <= 5; ++direction) {
               if (this.canPlace(world, _x, y, _z, direction)) {
                  world.setBlock(_x, y, _z, DefaultMaterial.VINE.id, 1 << d[OPPOSITE_FACING[direction]]);
                  break;
               }
            }
         } else {
            _x = x + rand.nextInt(4) - rand.nextInt(4);
            _z = z + rand.nextInt(4) - rand.nextInt(4);
         }
      }

   }

   public boolean canPlace(LocalWorld world, int x, int y, int z, int paramInt4) {
      int id;
      switch (paramInt4) {
         case 1:
            id = world.getTypeId(x, y + 1, z);
            break;
         case 2:
            id = world.getTypeId(x, y, z + 1);
            break;
         case 3:
            id = world.getTypeId(x, y, z - 1);
            break;
         case 4:
            id = world.getTypeId(x + 1, y, z);
            break;
         case 5:
            id = world.getTypeId(x - 1, y, z);
            break;
         default:
            return false;
      }

      return DefaultMaterial.getMaterial(id).isSolid();
   }

   public void load(List args) throws InvalidConfigException {
      this.blockId = DefaultMaterial.VINE.id;
      this.assureSize(4, args);
      this.frequency = this.readInt((String)args.get(0), 1, 100);
      this.rarity = this.readRarity((String)args.get(1));
      this.minAltitude = this.readInt((String)args.get(2), TerrainControl.worldDepth, TerrainControl.worldHeight);
      this.maxAltitude = this.readInt((String)args.get(3), this.minAltitude + 1, TerrainControl.worldHeight);
   }

   public String makeString() {
      return "Vines(" + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + ")";
   }
}
