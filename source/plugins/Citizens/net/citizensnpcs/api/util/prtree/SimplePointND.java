package net.citizensnpcs.api.util.prtree;

public class SimplePointND implements PointND {
   private final double[] ords;

   public SimplePointND(double... ords) {
      super();
      this.ords = ords;
   }

   public int getDimensions() {
      return this.ords.length;
   }

   public double getOrd(int axis) {
      return this.ords[axis];
   }
}
