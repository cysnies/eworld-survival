package com.lishid.orebfuscator.internal.v1_4_R1;

import com.lishid.orebfuscator.internal.IBlockTransparency;
import net.minecraft.server.v1_4_R1.Block;

public class BlockTransparency implements IBlockTransparency {
   public BlockTransparency() {
      super();
   }

   public boolean isBlockTransparent(int id) {
      return !Block.i(id);
   }
}
