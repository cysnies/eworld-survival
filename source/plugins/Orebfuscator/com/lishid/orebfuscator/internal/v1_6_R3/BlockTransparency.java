package com.lishid.orebfuscator.internal.v1_6_R3;

import com.lishid.orebfuscator.internal.IBlockTransparency;
import net.minecraft.server.v1_6_R3.Block;

public class BlockTransparency implements IBlockTransparency {
   public BlockTransparency() {
      super();
   }

   public boolean isBlockTransparent(int id) {
      return !Block.l(id);
   }
}
