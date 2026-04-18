package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import java.util.List;
import java.util.Random;

public class DungeonGen extends Resource {
   private int minAltitude;
   private int maxAltitude;

   public DungeonGen() {
      super();
   }

   public void load(List args) throws InvalidConfigException {
      this.assureSize(4, args);
      this.frequency = this.readInt((String)args.get(0), 1, 100);
      this.rarity = this.readRarity((String)args.get(1));
      this.minAltitude = this.readInt((String)args.get(2), TerrainControl.worldDepth, TerrainControl.worldHeight);
      this.maxAltitude = this.readInt((String)args.get(3), this.minAltitude + 1, TerrainControl.worldHeight);
   }

   public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z) {
      int y = random.nextInt(this.maxAltitude - this.minAltitude) + this.minAltitude;
      world.PlaceDungeons(random, x, y, z);
   }

   public String makeString() {
      return "Dungeon(" + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + ")";
   }
}
