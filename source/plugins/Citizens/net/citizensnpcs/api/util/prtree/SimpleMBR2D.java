package net.citizensnpcs.api.util.prtree;

public class SimpleMBR2D implements MBR2D {
   private final double xmax;
   private final double xmin;
   private final double ymax;
   private final double ymin;

   public SimpleMBR2D(double xmin, double ymin, double xmax, double ymax) {
      super();
      this.xmin = xmin;
      this.ymin = ymin;
      this.xmax = xmax;
      this.ymax = ymax;
   }

   public double getMaxX() {
      return this.xmax;
   }

   public double getMaxY() {
      return this.ymax;
   }

   public double getMinX() {
      return this.xmin;
   }

   public double getMinY() {
      return this.ymin;
   }

   public boolean intersects(MBR2D other) {
      return !(other.getMaxX() < this.xmin) && !(other.getMinX() > this.xmax) && !(other.getMaxY() < this.ymin) && !(other.getMinY() > this.ymax);
   }

   public String toString() {
      return this.getClass().getSimpleName() + "{xmin: " + this.xmin + ", ymin: " + this.ymin + ", xmax: " + this.xmax + ", ymax: " + this.ymax + "}";
   }

   public MBR2D union(MBR2D other) {
      double uxmin = Math.min(this.xmin, other.getMinX());
      double uymin = Math.min(this.ymin, other.getMinY());
      double uxmax = Math.max(this.xmax, other.getMaxX());
      double uymax = Math.max(this.ymax, other.getMaxY());
      return new SimpleMBR2D(uxmin, uymin, uxmax, uymax);
   }
}
