package net.citizensnpcs.api.util.prtree;

public class DistanceResult {
   private final double dist;
   private final Object t;

   public DistanceResult(Object t, double dist) {
      super();
      this.t = t;
      this.dist = dist;
   }

   public Object get() {
      return this.t;
   }

   public double getDistance() {
      return this.dist;
   }
}
