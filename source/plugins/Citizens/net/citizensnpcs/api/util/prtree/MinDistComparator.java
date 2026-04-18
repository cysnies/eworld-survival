package net.citizensnpcs.api.util.prtree;

import java.util.Comparator;

class MinDistComparator implements Comparator {
   public final MBRConverter converter;
   public final PointND p;

   public MinDistComparator(MBRConverter converter, PointND p) {
      super();
      this.converter = converter;
      this.p = p;
   }

   public int compare(Node t1, Node t2) {
      MBR mbr1 = t1.getMBR(this.converter);
      MBR mbr2 = t2.getMBR(this.converter);
      return Double.compare(MinDist.get(mbr1, this.p), MinDist.get(mbr2, this.p));
   }
}
