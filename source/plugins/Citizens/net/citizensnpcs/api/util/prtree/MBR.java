package net.citizensnpcs.api.util.prtree;

public interface MBR {
   int getDimensions();

   double getMax(int var1);

   double getMin(int var1);

   boolean intersects(MBR var1);

   boolean intersects(Object var1, MBRConverter var2);

   MBR union(MBR var1);
}
