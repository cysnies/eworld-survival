package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.Tag;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.BlockHelper;
import java.util.List;
import java.util.Random;

public class BlockFunction extends BO3Function {
   public int blockId;
   public int blockData;
   public int x;
   public int y;
   public int z;
   public boolean hasMetaData;
   public Tag metaDataTag;
   public String metaDataName;

   public BlockFunction() {
      super();
   }

   public void load(List args) throws InvalidConfigException {
      this.assureSize(4, args);
      this.x = this.readInt((String)args.get(0), -100, 100);
      this.y = this.readInt((String)args.get(1), -100, 100);
      this.z = this.readInt((String)args.get(2), -100, 100);
      this.blockId = this.readBlockId((String)args.get(3));
      this.blockData = this.readBlockData((String)args.get(3));
      if (args.size() == 5) {
         this.metaDataTag = BO3Loader.loadMetadata((String)args.get(4), ((BO3Config)this.getHolder()).file);
         if (this.metaDataTag != null) {
            this.hasMetaData = true;
            this.metaDataName = (String)args.get(4);
         }
      }

   }

   protected String makeString() {
      String start = "Block(" + this.x + "," + this.y + "," + this.z + "," + this.makeMaterial(this.blockId, this.blockData);
      if (this.hasMetaData) {
         start = start + "," + this.metaDataName;
      }

      return start + ")";
   }

   public BlockFunction rotate() {
      BlockFunction rotatedBlock = new BlockFunction();
      rotatedBlock.x = this.z;
      rotatedBlock.y = this.y;
      rotatedBlock.z = -this.x;
      rotatedBlock.blockId = this.blockId;
      rotatedBlock.blockData = BlockHelper.rotateData(this.blockId, this.blockData);
      rotatedBlock.hasMetaData = this.hasMetaData;
      rotatedBlock.metaDataTag = this.metaDataTag;
      rotatedBlock.metaDataName = this.metaDataName;
      return rotatedBlock;
   }

   public void spawn(LocalWorld world, Random random, int x, int y, int z) {
      world.setBlock(x, y, z, this.blockId, this.blockData);
      if (this.hasMetaData) {
         world.attachMetadata(x, y, z, this.metaDataTag);
      }

   }
}
