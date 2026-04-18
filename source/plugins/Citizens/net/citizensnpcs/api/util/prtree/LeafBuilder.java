package net.citizensnpcs.api.util.prtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class LeafBuilder {
   private final int branchFactor;
   private final int dimensions;

   public LeafBuilder(int dimensions, int branchFactor) {
      super();
      this.dimensions = dimensions;
      this.branchFactor = branchFactor;
   }

   private void addGetterAndSplitter(List nodes, Comparator tcomp, Circle getters) {
      Comparator<NodeUsage<T>> comp = new NodeUsageComparator(tcomp);
      Collections.sort(nodes, comp);
      List<NodeUsage<T>> sortedNodes = new ArrayList(nodes);
      getters.add(new Noder(sortedNodes));
   }

   public void buildLeafs(Collection ls, NodeComparators comparators, NodeFactory nf, List leafNodes) {
      List<NodeUsage<T>> nodes = new ArrayList(ls.size());

      for(Object t : ls) {
         nodes.add(new NodeUsage(t, 1));
      }

      Circle<Noder<T, N>> getters = new Circle(this.dimensions * 2);

      for(int i = 0; i < this.dimensions; ++i) {
         this.addGetterAndSplitter(nodes, comparators.getMinComparator(i), getters);
      }

      for(int i = 0; i < this.dimensions; ++i) {
         this.addGetterAndSplitter(nodes, comparators.getMaxComparator(i), getters);
      }

      this.getLeafs(1, ls.size(), getters, nf, leafNodes);
   }

   private void getLeafs(int id, int totalNumberOfElements, Circle getters, NodeFactory nf, List leafNodes) {
      List<Partition> partitionsToExpand = new ArrayList();
      int[] pos = new int[2 * this.dimensions];
      partitionsToExpand.add(new Partition(id, totalNumberOfElements, pos));

      while(!partitionsToExpand.isEmpty()) {
         Partition p = (Partition)partitionsToExpand.remove(0);
         getters.reset();

         for(int i = 0; i < getters.getNumElements(); ++i) {
            int nodesToGet = Math.min(p.numElementsLeft, this.branchFactor);
            if (nodesToGet == 0) {
               break;
            }

            Noder<T, N> noder = (Noder)getters.getNext();
            leafNodes.add(noder.getNextNode(p, i, nodesToGet, nf));
            p.numElementsLeft = nodesToGet;
         }

         if (p.numElementsLeft > 0) {
            int splitPos = this.getSplitPos(p.id) % getters.getNumElements();
            Noder<T, N> s = (Noder)getters.get(splitPos);
            s.split(p, splitPos, p.numElementsLeft, p.id, 2 * p.id, 2 * p.id + 1, partitionsToExpand);
         }
      }

   }

   private int getSplitPos(int n) {
      int splitPos;
      for(splitPos = 0; n >= 2; ++splitPos) {
         n >>= 1;
      }

      return splitPos;
   }

   private static class Noder {
      private final List data;

      private Noder(List data) {
         super();
         this.data = data;
      }

      private Object getNextNode(Partition p, int gi, int maxObjects, NodeFactory nf) {
         Object[] nodeData = new Object[maxObjects];
         int s = this.data.size();

         for(int i = 0; i < maxObjects; ++i) {
            while(p.currentPositions[gi] < s && this.isUsedNode(p, p.currentPositions[gi])) {
               int var10002 = p.currentPositions[gi]++;
            }

            NodeUsage<T> nu = (NodeUsage)this.data.set(p.currentPositions[gi], (Object)null);
            nodeData[i] = nu.getData();
            nu.use();
         }

         for(int i = 0; i < nodeData.length; ++i) {
            if (nodeData[i] == null) {
               for(int j = 0; j < this.data.size(); ++j) {
                  System.err.println(j + ": " + this.data.get(j));
               }

               throw new NullPointerException("Null data found at: " + i);
            }
         }

         return nf.create(nodeData);
      }

      private boolean isUsedNode(Partition p, int pos) {
         NodeUsage<T> nu = (NodeUsage)this.data.get(pos);
         return nu == null || nu.isUsed() || nu.getOwner() != p.id;
      }

      private int markPart(int numToMark, int fromId, int toId, int startPos) {
         while(numToMark > 0) {
            NodeUsage<T> nu;
            while((nu = (NodeUsage)this.data.get(startPos)) == null || nu.getOwner() != fromId) {
               ++startPos;
            }

            nu.changeOwner(toId);
            --numToMark;
         }

         return startPos;
      }

      private void split(Partition p, int gi, int nodesToMark, int fromId, int toId1, int toId2, List partitionsToExpand) {
         int sizePart2 = nodesToMark / 2;
         int sizePart1 = nodesToMark - sizePart2;
         int startPos = p.currentPositions[gi];
         int startPos2 = this.markPart(sizePart1, fromId, toId1, startPos);
         this.markPart(sizePart2, fromId, toId2, startPos2);
         partitionsToExpand.add(0, new Partition(toId1, sizePart1, p.currentPositions));
         int[] pos = (int[])p.currentPositions.clone();
         pos[gi] = startPos2;
         partitionsToExpand.add(1, new Partition(toId2, sizePart2, pos));
      }
   }

   private static class NodeUsageComparator implements Comparator {
      private Comparator sorter;

      public NodeUsageComparator(Comparator sorter) {
         super();
         this.sorter = sorter;
      }

      public int compare(NodeUsage n1, NodeUsage n2) {
         return this.sorter.compare(n1.getData(), n2.getData());
      }
   }

   private static class Partition {
      private int[] currentPositions;
      private final int id;
      private int numElementsLeft;

      public Partition(int id, int numElementsLeft, int[] currentPositions) {
         super();
         this.id = id;
         this.numElementsLeft = numElementsLeft;
         this.currentPositions = currentPositions;
      }

      public String toString() {
         return this.getClass().getSimpleName() + "{id: " + this.id + ", numElementsLeft: " + this.numElementsLeft + ", currentPositions: " + Arrays.toString(this.currentPositions) + "}";
      }
   }
}
