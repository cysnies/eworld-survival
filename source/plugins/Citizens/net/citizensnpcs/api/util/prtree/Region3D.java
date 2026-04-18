package net.citizensnpcs.api.util.prtree;

import org.bukkit.util.Vector;

public class Region3D implements MBR {
   private final Object data;
   private final Vector min;
   private final Vector max;

   public Region3D(Vector min, Vector max, Object data) {
      super();
      this.min = min;
      this.max = max;
      this.data = data;
   }

   public Object getData() {
      return this.data;
   }

   public int getDimensions() {
      return 3;
   }

   public double getMax(int axis) {
      switch (axis) {
         case 0:
            return (double)this.max.getBlockX();
         case 1:
            return (double)this.max.getBlockY();
         case 2:
            return (double)this.max.getBlockZ();
         default:
            return (double)0.0F;
      }
   }

   public double getMin(int axis) {
      switch (axis) {
         case 0:
            return (double)this.min.getBlockX();
         case 1:
            return (double)this.min.getBlockY();
         case 2:
            return (double)this.min.getBlockZ();
         default:
            return (double)0.0F;
      }
   }

   public boolean intersects(Object t, MBRConverter converter) {
      converter.getMin(this.getDimensions(), t);
      return false;
   }

   public boolean intersects(MBR other) {
      if (!(other.getMax(0) < (double)this.min.getBlockX()) && !(other.getMax(1) < (double)this.min.getBlockY()) && !(other.getMax(2) < (double)this.min.getBlockZ())) {
         return !(other.getMin(0) > (double)this.max.getBlockX()) && !(other.getMin(1) > (double)this.max.getBlockY()) && !(other.getMin(2) > (double)this.max.getBlockZ()) ? false : false;
      } else {
         return false;
      }
   }

   public MBR union(MBR mbr) {
      Vector umin = new Vector(Math.min(this.min.getX(), mbr.getMin(0)), Math.min(this.min.getY(), mbr.getMin(1)), Math.min(this.min.getZ(), mbr.getMin(2)));
      Vector umax = new Vector(Math.max(this.max.getX(), mbr.getMax(0)), Math.max(this.max.getY(), mbr.getMax(1)), Math.max(this.max.getZ(), mbr.getMax(2)));
      return new Region3D(umin, umax, this.data);
   }

   public static DistanceCalculator distanceCalculator() {
      return new DistanceCalculator() {
         public double distanceTo(Region3D t, PointND p) {
            double x = p.getOrd(0);
            double y = p.getOrd(1);
            double z = p.getOrd(2);
            return Math.sqrt(Math.pow(x - (t.getMin(0) + t.getMax(0)) / (double)2.0F, (double)2.0F) + Math.pow(y - (t.getMin(1) + t.getMax(1)) / (double)2.0F, (double)2.0F) + Math.pow(z - (t.getMin(2) + t.getMax(2)) / (double)2.0F, (double)2.0F));
         }
      };
   }

   public static NodeFilter alwaysAcceptNodeFilter() {
      return new NodeFilter() {
         public boolean accept(Region3D t) {
            return true;
         }
      };
   }

   public static class Converter implements MBRConverter {
      public Converter() {
         super();
      }

      public int getDimensions() {
         return 3;
      }

      public double getMax(int axis, Region3D t) {
         return t.getMax(axis);
      }

      public double getMin(int axis, Region3D t) {
         return t.getMin(axis);
      }
   }
}
