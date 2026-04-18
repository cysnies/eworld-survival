package net.citizensnpcs.api.util.prtree;

import java.util.List;
import java.util.PriorityQueue;

class InternalNode extends NodeBase {
   public InternalNode(Object[] data) {
      super(data);
   }

   public MBR computeMBR(MBRConverter converter) {
      MBR ret = null;
      int i = 0;

      for(int s = this.size(); i < s; ++i) {
         ret = this.getUnion(ret, ((Node)this.get(i)).getMBR(converter));
      }

      return ret;
   }

   public void expand(MBR mbr, MBRConverter converter, List found, List nodesToExpand) {
      int i = 0;

      for(int s = this.size(); i < s; ++i) {
         Node<T> n = (Node)this.get(i);
         if (mbr.intersects(n.getMBR(converter))) {
            nodesToExpand.add(n);
         }
      }

   }

   public void find(MBR mbr, MBRConverter converter, List result) {
      int i = 0;

      for(int s = this.size(); i < s; ++i) {
         Node<T> n = (Node)this.get(i);
         if (mbr.intersects(n.getMBR(converter))) {
            n.find(mbr, converter, result);
         }
      }

   }

   public void nnExpand(DistanceCalculator dc, NodeFilter filter, List drs, int maxHits, PriorityQueue queue, MinDistComparator mdc) {
      int s = this.size();

      for(int i = 0; i < s; ++i) {
         Node<T> n = (Node)this.get(i);
         MBR mbr = n.getMBR(mdc.converter);
         double minDist = MinDist.get(mbr, mdc.p);
         int t = drs.size();
         if (t < maxHits || minDist <= ((DistanceResult)drs.get(t - 1)).getDistance()) {
            queue.add(n);
         }
      }

   }
}
