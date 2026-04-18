package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.Tag;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.BlockHelper;
import java.util.List;
import java.util.Random;

public class RandomBlockFunction extends BlockFunction {
   public int[] blockIds;
   public byte[] blockDatas;
   public byte[] blockChances;
   public String[] metaDataNames;
   public Tag[] metaDataTags;
   public int blockCount = 0;

   public RandomBlockFunction() {
      super();
   }

   public void load(List args) throws InvalidConfigException {
      this.assureSize(5, args);
      this.x = this.readInt((String)args.get(0), -100, 100);
      this.y = this.readInt((String)args.get(1), -100, 100);
      this.z = this.readInt((String)args.get(2), -100, 100);
      int i = 3;
      int size = args.size();
      this.blockIds = new int[size / 2 + 1];
      this.blockDatas = new byte[size / 2 + 1];
      this.blockChances = new byte[size / 2 + 1];
      this.metaDataNames = new String[size / 2 + 1];

      for(this.metaDataTags = new Tag[size / 2 + 1]; i < size; ++this.blockCount) {
         this.blockIds[this.blockCount] = this.readBlockId((String)args.get(i));
         this.blockDatas[this.blockCount] = (byte)this.readBlockData((String)args.get(i));
         ++i;

         try {
            this.blockChances[this.blockCount] = (byte)this.readInt((String)args.get(i), 1, 100);
         } catch (InvalidConfigException var6) {
            Tag metaData = BO3Loader.loadMetadata((String)args.get(i), ((BO3Config)this.getHolder()).file);
            if (metaData != null) {
               this.metaDataNames[this.blockCount] = (String)args.get(i);
               this.metaDataTags[this.blockCount] = metaData;
            }

            ++i;
            this.blockChances[this.blockCount] = (byte)this.readInt((String)args.get(i), 1, 100);
         }

         ++i;
      }

   }

   public String makeString() {
      String text = "RandomBlock(" + this.x + "," + this.y + "," + this.z;

      for(int i = 0; i < this.blockCount; ++i) {
         if (this.metaDataTags[i] == null) {
            text = text + "," + this.makeMaterial(this.blockIds[i], this.blockDatas[i]) + "," + this.blockChances[i];
         } else {
            text = text + "," + this.makeMaterial(this.blockIds[i], this.blockDatas[i]) + "," + this.metaDataNames[i] + "," + this.blockChances[i];
         }
      }

      return text + ")";
   }

   public RandomBlockFunction rotate() {
      RandomBlockFunction rotatedBlock = new RandomBlockFunction();
      rotatedBlock.x = this.z;
      rotatedBlock.y = this.y;
      rotatedBlock.z = -this.x;
      rotatedBlock.blockCount = this.blockCount;
      rotatedBlock.blockIds = this.blockIds;
      rotatedBlock.blockDatas = new byte[this.blockCount];

      for(int i = 0; i < this.blockCount; ++i) {
         rotatedBlock.blockDatas[i] = (byte)BlockHelper.rotateData(this.blockIds[i], this.blockDatas[i]);
      }

      rotatedBlock.blockChances = this.blockChances;
      rotatedBlock.metaDataTags = this.metaDataTags;
      rotatedBlock.metaDataNames = this.metaDataNames;
      return rotatedBlock;
   }

   public void spawn(LocalWorld world, Random random, int x, int y, int z) {
      for(int i = 0; i < this.blockCount; ++i) {
         if (random.nextInt(100) < this.blockChances[i]) {
            world.setBlock(x, y, z, this.blockIds[i], this.blockDatas[i]);
            if (this.metaDataTags[i] != null) {
               world.attachMetadata(x, y, z, this.metaDataTags[i]);
            }
            break;
         }
      }

   }
}
