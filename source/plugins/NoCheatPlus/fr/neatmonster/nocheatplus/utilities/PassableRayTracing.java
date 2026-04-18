package fr.neatmonster.nocheatplus.utilities;

public class PassableRayTracing extends RayTracing {
   protected BlockCache blockCache = null;
   protected boolean collides = false;
   protected boolean ignorefirst = false;

   public PassableRayTracing() {
      super();
   }

   public BlockCache getBlockCache() {
      return this.blockCache;
   }

   public void setBlockCache(BlockCache blockCache) {
      this.blockCache = blockCache;
   }

   public void set(PlayerLocation from, PlayerLocation to) {
      this.set(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
      this.setBlockCache(from.getBlockCache());
   }

   public void set(double x0, double y0, double z0, double x1, double y1, double z1) {
      super.set(x0, y0, z0, x1, y1, z1);
      this.collides = false;
      this.ignorefirst = false;
   }

   public boolean collides() {
      return this.collides;
   }

   public void setIgnorefirst() {
      this.ignorefirst = true;
   }

   public boolean getIgnoreFirst() {
      return this.ignorefirst;
   }

   public void cleanup() {
      if (this.blockCache != null) {
         this.blockCache = null;
      }

   }

   protected boolean step(int blockX, int blockY, int blockZ, double oX, double oY, double oZ, double dT) {
      if (this.step == 1 && this.ignorefirst) {
         return true;
      } else if (BlockProperties.isPassableRay(this.blockCache, blockX, blockY, blockZ, oX, oY, oZ, this.dX, this.dY, this.dZ, dT)) {
         return true;
      } else {
         this.collides = true;
         return false;
      }
   }
}
