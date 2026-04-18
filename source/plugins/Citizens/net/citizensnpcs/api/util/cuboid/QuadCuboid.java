package net.citizensnpcs.api.util.cuboid;

public class QuadCuboid {
   private int hashcode = 0;
   int[] highCoords = new int[]{0, 0, 0};
   int[] highIndex = new int[3];
   int[] lowCoords = new int[]{0, 0, 0};
   int[] lowIndex = new int[3];

   public QuadCuboid(int x1, int y1, int z1, int x2, int y2, int z2) {
      super();
      this.lowCoords[0] = x1;
      this.lowCoords[1] = y1;
      this.lowCoords[2] = z1;
      this.highCoords[0] = x2;
      this.highCoords[1] = y2;
      this.highCoords[2] = z2;
      this.normalize();
   }

   public QuadCuboid(int[] low, int[] high) {
      super();
      this.lowCoords = (int[])(([I)low).clone();
      this.highCoords = (int[])(([I)high).clone();
      this.normalize();
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof QuadCuboid)) {
         return false;
      } else {
         QuadCuboid c = (QuadCuboid)o;

         for(int i = 0; i < 3; ++i) {
            if (this.lowCoords[i] != c.lowCoords[i]) {
               return false;
            }

            if (this.highCoords[i] != c.highCoords[i]) {
               return false;
            }
         }

         return true;
      }
   }

   public int hashCode() {
      return this.hashcode;
   }

   public boolean includesPoint(int x, int y, int z) {
      return this.lowCoords[0] <= x && this.lowCoords[1] <= y && this.lowCoords[2] <= z && this.highCoords[0] >= x && this.highCoords[1] >= y && this.highCoords[2] >= z;
   }

   public boolean includesPoint(int[] point) {
      return this.includesPoint(point[0], point[1], point[2]);
   }

   private void normalize() {
      for(int i = 0; i < 3; ++i) {
         if (this.lowCoords[i] > this.highCoords[i]) {
            int temp = this.lowCoords[i];
            this.lowCoords[i] = this.highCoords[i];
            this.highCoords[i] = temp;
         }

         this.hashcode ^= this.highCoords[i] ^ ~this.lowCoords[i];
      }

   }

   public boolean overlaps(QuadCuboid cuboid) {
      for(int i = 0; i < 3; ++i) {
         if (this.lowCoords[i] > cuboid.highCoords[i] || cuboid.lowCoords[i] > this.highCoords[i]) {
            return false;
         }
      }

      return true;
   }

   public String toString() {
      return "x1=" + this.lowCoords[0] + " y1=" + this.lowCoords[1] + " z1=" + this.lowCoords[2] + " x2=" + this.highCoords[0] + " y2=" + this.highCoords[1] + " z2=" + this.highCoords[2];
   }
}
