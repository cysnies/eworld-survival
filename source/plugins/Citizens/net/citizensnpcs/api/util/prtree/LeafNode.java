package net.citizensnpcs.api.util.prtree;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

class LeafNode extends NodeBase {
   private static final Comparator comp = new Comparator() {
      public int compare(DistanceResult d1, DistanceResult d2) {
         return Double.compare(d1.getDistance(), d2.getDistance());
      }
   };

   public LeafNode(Object[] data) {
      super(data);
   }

   private void add(List drs, DistanceResult dr, int maxHits) {
      int n = drs.size();
      if (n == maxHits) {
         drs.remove(n - 1);
      }

      int pos = Collections.binarySearch(drs, dr, comp);
      if (pos < 0) {
         pos = -(pos + 1);
      }

      drs.add(pos, dr);
   }

   public MBR computeMBR(MBRConverter converter) {
      MBR ret = null;
      int i = 0;

      for(int s = this.size(); i < s; ++i) {
         ret = this.getUnion(ret, this.getMBR(this.get(i), converter));
      }

      return ret;
   }

   public void expand(MBR mbr, MBRConverter converter, List found, List nodesToExpand) {
      this.find(mbr, converter, found);
   }

   public void find(MBR mbr, MBRConverter converter, List result) {
      int i = 0;

      for(int s = this.size(); i < s; ++i) {
         T t = (T)this.get(i);
         if (mbr.intersects(t, converter)) {
            result.add(t);
         }
      }

   }

   public MBR getMBR(Object t, MBRConverter converter) {
      return new SimpleMBR(t, converter);
   }

   public void nnExpand(DistanceCalculator dc, NodeFilter filter, List drs, int maxHits, PriorityQueue queue, MinDistComparator mdc) {
      int i = 0;

      for(int s = this.size(); i < s; ++i) {
         T t = (T)this.get(i);
         if (filter.accept(t)) {
            double dist = dc.distanceTo(t, mdc.p);
            int n = drs.size();
            if (n < maxHits || dist < ((DistanceResult)drs.get(n - 1)).getDistance()) {
               this.add(drs, new DistanceResult(t, dist), maxHits);
            }
         }
      }

   }
}
