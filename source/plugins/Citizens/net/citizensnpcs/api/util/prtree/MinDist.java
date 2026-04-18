package net.citizensnpcs.api.util.prtree;

public class MinDist {
   private MinDist() {
      super();
   }

   public static double get(MBR mbr, PointND p) {
      double res = (double)0.0F;

      for(int i = 0; i < p.getDimensions(); ++i) {
         double o = p.getOrd(i);
         double rv = r(o, mbr.getMin(i), mbr.getMax(i));
         double dr = o - rv;
         res += dr * dr;
      }

      return res;
   }

   private static double r(double x, double min, double max) {
      double r = x;
      if (x < min) {
         r = min;
      }

      if (x > max) {
         r = max;
      }

      return r;
   }
}
