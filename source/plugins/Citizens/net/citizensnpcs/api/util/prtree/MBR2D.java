package net.citizensnpcs.api.util.prtree;

public interface MBR2D {
   double getMaxX();

   double getMaxY();

   double getMinX();

   double getMinY();

   boolean intersects(MBR2D var1);

   MBR2D union(MBR2D var1);
}
