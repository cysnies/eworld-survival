package net.citizensnpcs.api.util.prtree;

import java.util.Arrays;

public class SimpleMBR implements MBR {
   private final double[] values;

   public SimpleMBR(double... values) {
      super();
      this.values = (double[])(([D)values).clone();
   }

   private SimpleMBR(int dimensions) {
      super();
      this.values = new double[dimensions * 2];
   }

   public SimpleMBR(Object t, MBRConverter converter) {
      super();
      int dims = converter.getDimensions();
      this.values = new double[dims * 2];
      int p = 0;

      for(int i = 0; i < dims; ++i) {
         this.values[p++] = converter.getMin(i, t);
         this.values[p++] = converter.getMax(i, t);
      }

   }

   public int getDimensions() {
      return this.values.length / 2;
   }

   public double getMax(int axis) {
      return this.values[axis * 2 + 1];
   }

   public double getMin(int axis) {
      return this.values[axis * 2];
   }

   public boolean intersects(MBR other) {
      for(int i = 0; i < this.getDimensions(); ++i) {
         if (other.getMax(i) < this.getMin(i) || other.getMin(i) > this.getMax(i)) {
            return false;
         }
      }

      return true;
   }

   public boolean intersects(Object t, MBRConverter converter) {
      for(int i = 0; i < this.getDimensions(); ++i) {
         if (converter.getMax(i, t) < this.getMin(i) || converter.getMin(i, t) > this.getMax(i)) {
            return false;
         }
      }

      return true;
   }

   public String toString() {
      return this.getClass().getSimpleName() + "{values: " + Arrays.toString(this.values) + "}";
   }

   public MBR union(MBR mbr) {
      int dims = this.getDimensions();
      SimpleMBR n = new SimpleMBR(dims);
      int p = 0;

      for(int i = 0; i < dims; ++i) {
         n.values[p] = Math.min(this.getMin(i), mbr.getMin(i));
         ++p;
         n.values[p] = Math.max(this.getMax(i), mbr.getMax(i));
         ++p;
      }

      return n;
   }
}
