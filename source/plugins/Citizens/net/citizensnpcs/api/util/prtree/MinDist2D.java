package net.citizensnpcs.api.util.prtree;

public class MinDist2D {
   private MinDist2D() {
      super();
   }

   public static double get(double minx, double miny, double maxx, double maxy, double x, double y) {
      double rx = r(x, minx, maxx);
      double ry = r(y, miny, maxy);
      double xd = x - rx;
      double yd = y - ry;
      return xd * xd + yd * yd;
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
