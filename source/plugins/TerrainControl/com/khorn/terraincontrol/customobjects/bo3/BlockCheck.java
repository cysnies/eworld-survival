package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.BlockHelper;
import java.util.ArrayList;
import java.util.List;

public class BlockCheck extends BO3Check {
   public List blockIds;
   public List blockDatas;

   public BlockCheck() {
      super();
   }

   public boolean preventsSpawn(LocalWorld world, int x, int y, int z) {
      int blockId = world.getTypeId(x, y, z);
      int indexOf = this.blockIds.indexOf(blockId);
      if (indexOf == -1) {
         return true;
      } else {
         return (Byte)this.blockDatas.get(indexOf) != -1 && world.getTypeData(x, y, z) != (Byte)this.blockDatas.get(indexOf);
      }
   }

   public void load(List args) throws InvalidConfigException {
      this.assureSize(4, args);
      this.x = this.readInt((String)args.get(0), -100, 100);
      this.y = this.readInt((String)args.get(1), -100, 100);
      this.z = this.readInt((String)args.get(2), -100, 100);
      this.blockIds = new ArrayList();
      this.blockDatas = new ArrayList();

      for(int i = 3; i < args.size(); ++i) {
         String blockIdAndData = (String)args.get(i);
         if (!blockIdAndData.contains(".") && !blockIdAndData.contains(":")) {
            this.blockIds.add(this.readBlockId(blockIdAndData));
            this.blockDatas.add(-1);
         } else {
            this.blockIds.add(this.readBlockId(blockIdAndData));
            this.blockDatas.add((byte)this.readBlockData(blockIdAndData));
         }
      }

   }

   public String makeString() {
      StringBuilder builder = new StringBuilder("BlockCheck(");
      builder.append(this.x);
      builder.append(',');
      builder.append(this.y);
      builder.append(',');
      builder.append(this.z);

      for(int i = 0; i < this.blockIds.size(); ++i) {
         builder.append(',');
         builder.append(this.makeMaterial((Integer)this.blockIds.get(i)));
         if ((Byte)this.blockDatas.get(i) != -1) {
            builder.append(':');
            builder.append(this.blockDatas.get(i));
         }
      }

      builder.append(')');
      return builder.toString();
   }

   public BO3Check rotate() {
      BlockCheck rotatedCheck = new BlockCheck();
      rotatedCheck.x = this.z;
      rotatedCheck.y = this.y;
      rotatedCheck.z = -this.x;
      rotatedCheck.blockIds = this.blockIds;
      rotatedCheck.blockDatas = new ArrayList();

      for(int i = 0; i < this.blockDatas.size(); ++i) {
         if ((Byte)this.blockDatas.get(i) == -1) {
            rotatedCheck.blockDatas.add(-1);
         } else {
            rotatedCheck.blockDatas.add((byte)BlockHelper.rotateData((Integer)this.blockIds.get(i), (Byte)this.blockDatas.get(i)));
         }
      }

      return rotatedCheck;
   }
}
