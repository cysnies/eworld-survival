package net.citizensnpcs.api.util.prtree;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

class NearestNeighbour {
   private final MBRConverter converter;
   private final DistanceCalculator dc;
   private final NodeFilter filter;
   private final int maxHits;
   private final PointND p;
   private final Node root;

   public NearestNeighbour(MBRConverter converter, NodeFilter filter, int maxHits, Node root, DistanceCalculator dc, PointND p) {
      super();
      this.converter = converter;
      this.filter = filter;
      this.maxHits = maxHits;
      this.root = root;
      this.dc = dc;
      this.p = p;
   }

   public List find() {
      List<DistanceResult<T>> ret = new ArrayList(this.maxHits);
      MinDistComparator<T, Node<T>> nc = new MinDistComparator(this.converter, this.p);
      PriorityQueue<Node<T>> queue = new PriorityQueue(20, nc);
      queue.add(this.root);

      while(!queue.isEmpty()) {
         Node<T> n = (Node)queue.remove();
         n.nnExpand(this.dc, this.filter, ret, this.maxHits, queue, nc);
      }

      return ret;
   }
}
