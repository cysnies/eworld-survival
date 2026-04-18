package com.lishid.orebfuscator.obfuscation;

import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.internal.IMinecraftWorldServer;
import com.lishid.orebfuscator.internal.InternalAccessor;
import java.util.HashSet;
import java.util.List;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;

public class BlockUpdate {
   private static IMinecraftWorldServer worldServerAccessor;

   public BlockUpdate() {
      super();
   }

   private static IMinecraftWorldServer getWorldServer() {
      if (worldServerAccessor == null) {
         worldServerAccessor = InternalAccessor.Instance.newMinecraftWorldServer();
      }

      return worldServerAccessor;
   }

   public static boolean needsUpdate(Block block) {
      return !OrebfuscatorConfig.isBlockTransparent((short)block.getTypeId());
   }

   public static void Update(Block block) {
      if (needsUpdate(block)) {
         HashSet<Block> updateBlocks = GetAjacentBlocks(block.getWorld(), new HashSet(), block, OrebfuscatorConfig.UpdateRadius);
         World world = block.getWorld();
         IMinecraftWorldServer worldServer = getWorldServer();

         for(Block nearbyBlock : updateBlocks) {
            worldServer.Notify(world, nearbyBlock.getX(), nearbyBlock.getY(), nearbyBlock.getZ());
         }

      }
   }

   public static void Update(List blocks) {
      if (blocks.size() > 0) {
         HashSet<Block> updateBlocks = new HashSet();

         for(Block block : blocks) {
            if (needsUpdate(block)) {
               updateBlocks.addAll(GetAjacentBlocks(block.getWorld(), new HashSet(), block, OrebfuscatorConfig.UpdateRadius));
            }
         }

         World world = ((Block)blocks.get(0)).getWorld();
         IMinecraftWorldServer worldServer = getWorldServer();

         for(Block nearbyBlock : updateBlocks) {
            worldServer.Notify(world, nearbyBlock.getX(), nearbyBlock.getY(), nearbyBlock.getZ());
         }

      }
   }

   public static HashSet GetAjacentBlocks(World world, HashSet allBlocks, Block block, int countdown) {
      if (block == null) {
         return allBlocks;
      } else {
         AddBlockCheck(allBlocks, block);
         if (countdown == 0) {
            return allBlocks;
         } else {
            GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX() + 1, block.getY(), block.getZ()), countdown - 1);
            GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX() - 1, block.getY(), block.getZ()), countdown - 1);
            GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY() + 1, block.getZ()), countdown - 1);
            GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY() - 1, block.getZ()), countdown - 1);
            GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY(), block.getZ() + 1), countdown - 1);
            GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY(), block.getZ() - 1), countdown - 1);
            return allBlocks;
         }
      }
   }

   public static void AddBlockCheck(HashSet allBlocks, Block block) {
      if (OrebfuscatorConfig.isObfuscated((byte)block.getTypeId(), block.getWorld().getEnvironment() == Environment.NETHER) || OrebfuscatorConfig.isDarknessObfuscated((byte)block.getTypeId())) {
         allBlocks.add(block);
      }

   }
}
