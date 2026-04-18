package net.citizensnpcs.api.util.prtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PRTree {
   private final int branchFactor;
   private final MBRConverter converter;
   private int height;
   private int numLeafs;
   private Node root;

   public PRTree(MBRConverter converter, int branchFactor) {
      super();
      this.converter = converter;
      this.branchFactor = branchFactor;
   }

   public void clear() {
      this.root = null;
      this.height = 0;
      this.numLeafs = 0;
   }

   private int estimateSize(int dataSize) {
      return (int)((double)1.0F / (double)(this.branchFactor - 1) * (double)dataSize);
   }

   public Iterable find(double xmin, double ymin, double xmax, double ymax) {
      return this.find(new SimpleMBR(new double[]{xmin, xmax, ymin, ymax}));
   }

   public void find(double xmin, double ymin, double xmax, double ymax, List resultNodes) {
      this.find(new SimpleMBR(new double[]{xmin, xmax, ymin, ymax}), resultNodes);
   }

   public Iterable find(MBR query) {
      // $FF: Couldn't be decompiled
   }

   public void find(MBR query, List resultNodes) {
      this.validateRect(query);
      this.root.find(query, this.converter, resultNodes);
   }

   public int getHeight() {
      return this.height;
   }

   public MBR getMBR() {
      return this.root.getMBR(this.converter);
   }

   public MBR2D getMBR2D() {
      MBR mbr = this.getMBR();
      return mbr == null ? null : new SimpleMBR2D(mbr.getMin(0), mbr.getMin(1), mbr.getMax(0), mbr.getMax(1));
   }

   public int getNumberOfLeaves() {
      return this.numLeafs;
   }

   public boolean isEmpty() {
      return this.numLeafs == 0;
   }

   public void load(Collection data) {
      if (this.root != null) {
         throw new IllegalStateException("Tree is already loaded");
      } else {
         this.numLeafs = data.size();
         LeafBuilder lb = new LeafBuilder(this.converter.getDimensions(), this.branchFactor);
         List<LeafNode<T>> leafNodes = new ArrayList(this.estimateSize(this.numLeafs));
         lb.buildLeafs(data, new DataComparators(this.converter), new LeafNodeFactory(), leafNodes);
         this.height = 1;

         List<? extends Node<T>> nodes;
         List<InternalNode<T>> internalNodes;
         for(nodes = leafNodes; nodes.size() > this.branchFactor; nodes = internalNodes) {
            ++this.height;
            internalNodes = new ArrayList(this.estimateSize(nodes.size()));
            lb.buildLeafs(nodes, new InternalNodeComparators(this.converter), new InternalNodeFactory(), internalNodes);
         }

         this.setRoot(nodes);
      }
   }

   public List nearestNeighbour(DistanceCalculator dc, NodeFilter filter, int maxHits, PointND p) {
      if (this.isEmpty()) {
         return Collections.emptyList();
      } else {
         NearestNeighbour<T> nn = new NearestNeighbour(this.converter, filter, maxHits, this.root, dc, p);
         return nn.find();
      }
   }

   private void setRoot(List nodes) {
      if (nodes.size() == 0) {
         this.root = new InternalNode(new Object[0]);
      } else if (nodes.size() == 1) {
         this.root = (Node)nodes.get(0);
      } else {
         ++this.height;
         this.root = new InternalNode(nodes.toArray());
      }

   }

   private void validateRect(MBR query) {
      for(int i = 0; i < this.converter.getDimensions(); ++i) {
         double max = query.getMax(i);
         double min = query.getMin(i);
         if (max < min) {
            throw new IllegalArgumentException("max: " + max + " < min: " + min + ", axis: " + i + ", query: " + query);
         }
      }

   }

   public static PRTree create(MBRConverter converter, int branchFactor) {
      return new PRTree(converter, branchFactor);
   }

   private class Finder implements Iterator {
      private int dataNodesVisited = 0;
      private final MBR mbr;
      private Object next;
      private final List toVisit = new ArrayList();
      private final List ts = new ArrayList();
      private int visitedNodes = 0;

      public Finder(MBR mbr) {
         super();
         this.mbr = mbr;
         this.toVisit.add(PRTree.this.root);
         this.findNext();
      }

      private void findNext() {
         while(this.ts.isEmpty() && !this.toVisit.isEmpty()) {
            Node<T> n = (Node)this.toVisit.remove(this.toVisit.size() - 1);
            ++this.visitedNodes;
            n.expand(this.mbr, PRTree.this.converter, this.ts, this.toVisit);
         }

         if (this.ts.isEmpty()) {
            this.next = null;
         } else {
            this.next = this.ts.remove(this.ts.size() - 1);
            ++this.dataNodesVisited;
         }

      }

      public boolean hasNext() {
         return this.next != null;
      }

      public Object next() {
         T toReturn = (T)this.next;
         this.findNext();
         return toReturn;
      }

      public void remove() {
         throw new UnsupportedOperationException("Not implemented");
      }
   }

   private class InternalNodeFactory implements NodeFactory {
      private InternalNodeFactory() {
         super();
      }

      public InternalNode create(Object[] data) {
         return new InternalNode(data);
      }
   }

   private class LeafNodeFactory implements NodeFactory {
      private LeafNodeFactory() {
         super();
      }

      public LeafNode create(Object[] data) {
         return new LeafNode(data);
      }
   }
}
