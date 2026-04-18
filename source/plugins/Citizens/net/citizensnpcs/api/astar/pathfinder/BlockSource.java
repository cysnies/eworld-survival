package net.citizensnpcs.api.astar.pathfinder;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;

public abstract class BlockSource {
   public BlockSource() {
      super();
   }

   public abstract int getBlockTypeIdAt(int var1, int var2, int var3);

   public Material getMaterialAt(int x, int y, int z) {
      return Material.getMaterial(this.getBlockTypeIdAt(x, y, z));
   }

   public Material getMaterialAt(Vector pos) {
      return this.getMaterialAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
   }

   public abstract World getWorld();
}
