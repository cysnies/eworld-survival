package com.lishid.orebfuscator.internal.craftbukkit;

import com.lishid.orebfuscator.internal.IBlockTransparency;
import net.minecraft.server.Block;

public class BlockTransparency implements IBlockTransparency {
   public BlockTransparency() {
      super();
   }

   public boolean isBlockTransparent(int id) {
      return !Block.i(id);
   }
}
