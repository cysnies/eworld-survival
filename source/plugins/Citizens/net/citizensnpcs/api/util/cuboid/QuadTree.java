package net.citizensnpcs.api.util.cuboid;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuadTree {
   QuadNode root;

   public QuadTree() {
      super();
   }

   private void addAndFixListHolders(QuadNode node, QuadCuboid cuboid) {
      node.cuboids.add(cuboid);
      if (node.cuboids.size() <= 1) {
         Deque<QuadNode> todo = new ArrayDeque();
         todo.push(node);

         do {
            QuadNode current = (QuadNode)todo.pop();

            for(QuadNode child : current.quads) {
               if (child != null) {
                  if (child.cuboids.size() == 0) {
                     todo.push(child);
                  }

                  child.nextListHolder = node;
               }
            }
         } while(!todo.isEmpty());

      }
   }

   private QuadNode ascendFirstSearch(QuadNode node, int x, int z) {
      while(node != null && (node.x > x || node.z > z || node.x + node.size < x || node.z + node.size < z)) {
         node = node.parent;
      }

      if (node == null) {
         return null;
      } else {
         return this.descendAndSearch(node, x, z);
      }
   }

   private void beginTree(QuadCuboid cuboid) {
      int size = 128;
      int minSize = Math.abs(cuboid.lowCoords[0] - cuboid.highCoords[0]);
      int minSizeB = Math.abs(cuboid.lowCoords[2] - cuboid.highCoords[2]);
      if (minSize < minSizeB) {
         minSize = minSizeB;
      }

      while(size < minSize) {
         size <<= 1;
      }

      this.root = new QuadNode(cuboid.lowCoords[0], cuboid.lowCoords[2], size - 1, (QuadNode)null);
   }

   private int containerFit(QuadNode node, QuadCuboid cuboid) {
      int minSizeA = Math.abs(cuboid.lowCoords[0] - cuboid.highCoords[0]);
      int minSizeB = Math.abs(cuboid.lowCoords[2] - cuboid.highCoords[2]);
      int fitSize;
      if (minSizeA < minSizeB) {
         fitSize = minSizeB;
      } else {
         fitSize = minSizeA;
      }

      if (node.size < fitSize) {
         return -1;
      } else {
         return node.size != 1 && node.size >> 1 >= fitSize ? 1 : 0;
      }
   }

   private QuadNode descendAndCreate(QuadNode start, QuadCuboid cuboid) {
      QuadNode next;
      int i;
      for(next = start; this.containerFit(next, cuboid) > 0; next = next.quads[i]) {
         i = 0;
         int nX = 0;
         int nZ = 0;
         int half = next.size >> 1;
         if (cuboid.lowCoords[0] > next.x + half) {
            ++i;
            nX = half + 1;
         }

         if (cuboid.lowCoords[2] > next.z + half) {
            i += 2;
            nZ = half + 1;
         }

         if (next.quads[i] == null) {
            next.quads[i] = new QuadNode(next.x + nX, next.z + nZ, half, next);
         }
      }

      return next;
   }

   private QuadNode descendAndSearch(QuadNode node, int x, int z) {
      int i;
      for(QuadNode next = node; next != null; next = next.quads[i]) {
         node = next;
         int half = next.size >> 1;
         i = 0;
         if (x > next.x + half) {
            ++i;
         }

         if (z > next.z + half) {
            i += 2;
         }
      }

      return node;
   }

   private QuadNode descendNoCreate(QuadNode start, QuadCuboid cuboid) {
      QuadNode next = start;

      while(this.containerFit(next, cuboid) > 0) {
         int i = 0;
         int nX = 0;
         int nZ = 0;
         int half = next.size >> 1;
         if (cuboid.lowCoords[0] > next.x + half) {
            ++i;
            nX = half + 1;
         }

         if (cuboid.lowCoords[2] > next.z + half) {
            i += 2;
            nZ = half + 1;
         }

         if (next.quads[i] == null) {
            next = new QuadNode(next.x + nX, next.z + nZ, half, next);
         } else {
            next = next.quads[i];
         }
      }

      return next;
   }

   public BookmarkedResult findOverlappingCuboids(int x, int y, int z) {
      return this.relatedSearch((QuadNode)null, x, y, z);
   }

   public BookmarkedResult findOverlappingCuboidsFromBookmark(BookmarkedResult bookmark, int x, int y, int z) {
      return this.relatedSearch(bookmark.bookmark, x, y, z);
   }

   private List generateShards(QuadNode node, QuadCuboid cuboid) {
      List<QuadCuboid> shards = new ArrayList(4);
      int top = node.z + node.size;
      int right = node.x + node.size;
      if (top < cuboid.highCoords[2]) {
         int tmp;
         if (right < cuboid.highCoords[0]) {
            tmp = right;
         } else {
            tmp = cuboid.highCoords[0];
         }

         shards.add(new QuadCuboid(cuboid.lowCoords[0], 0, top + 1, tmp, 0, cuboid.highCoords[2]));
      }

      if (right < cuboid.highCoords[0]) {
         int tmp;
         if (top < cuboid.highCoords[2]) {
            tmp = top;
         } else {
            tmp = cuboid.highCoords[2];
         }

         shards.add(new QuadCuboid(right + 1, 0, cuboid.lowCoords[2], cuboid.highCoords[0], 0, tmp));
      }

      if (right < cuboid.highCoords[0] && top < cuboid.highCoords[2]) {
         shards.add(new QuadCuboid(right + 1, 0, top + 1, cuboid.highCoords[0], 0, cuboid.highCoords[2]));
      }

      if (shards.size() > 0) {
         shards.add(new QuadCuboid(cuboid.lowCoords[0], 0, cuboid.lowCoords[2], right, 0, top));
      }

      return shards;
   }

   public List getAllOverlapsWith(QuadCuboid cuboid) {
      if (this.root == null) {
         return Collections.emptyList();
      } else {
         if (!this.nodeFullyContainsCuboid(this.root, cuboid)) {
            this.repotTree(cuboid);
         }

         QuadNode node = this.root;
         node = this.descendNoCreate(node, cuboid);
         List<QuadNode> targets = this.getAllTargetsNoCreate(node, cuboid);
         Deque<QuadNode> children = new ArrayDeque();
         Set<QuadCuboid> cuboids = new HashSet(256);

         for(QuadNode target : targets) {
            children.add(target);

            do {
               QuadNode childTarget = (QuadNode)children.pop();

               for(QuadNode child : childTarget.quads) {
                  if (child != null) {
                     children.push(child);
                     cuboids.addAll(child.cuboids);
                  }
               }
            } while(!children.isEmpty());

            while(target != null) {
               cuboids.addAll(target.cuboids);
               target = target.nextListHolder;
            }
         }

         List<QuadCuboid> overlaps = new ArrayList();

         for(QuadCuboid pc : cuboids) {
            if (cuboid.overlaps(pc)) {
               overlaps.add(pc);
            }
         }

         return overlaps;
      }
   }

   private List getAllTargets(QuadNode initialNode, QuadCuboid cuboid) {
      List<QuadNode> targets = new ArrayList();
      Deque<QuadCuboid> shards = new ArrayDeque();
      shards.addAll(this.generateShards(initialNode, cuboid));

      while(!shards.isEmpty()) {
         QuadCuboid shard = (QuadCuboid)shards.pop();
         QuadNode node = this.descendAndCreate(this.root, shard);
         List<QuadCuboid> newShards = this.generateShards(node, shard);
         if (newShards.size() == 0) {
            targets.add(node);
         } else {
            shards.addAll(newShards);
         }
      }

      if (targets.size() == 0) {
         targets.add(initialNode);
      }

      return targets;
   }

   private List getAllTargetsNoCreate(QuadNode initialNode, QuadCuboid cuboid) {
      List<QuadNode> targets = new ArrayList();
      Deque<QuadCuboid> shards = new ArrayDeque();
      shards.addAll(this.generateShards(initialNode, cuboid));

      while(!shards.isEmpty()) {
         QuadCuboid shard = (QuadCuboid)shards.pop();
         QuadNode node = this.descendNoCreate(this.root, shard);
         List<QuadCuboid> newShards = this.generateShards(node, shard);
         if (newShards.size() == 0) {
            targets.add(node);
         } else {
            shards.addAll(newShards);
         }
      }

      if (targets.size() == 0) {
         targets.add(initialNode);
      }

      return targets;
   }

   private List getMatchingCuboids(QuadNode target, int x, int y, int z) {
      List<QuadCuboid> matches;
      for(matches = new ArrayList(); target != null; target = target.nextListHolder) {
         for(QuadCuboid potential : target.cuboids) {
            if (potential.includesPoint(x, y, z)) {
               matches.add(potential);
            }
         }
      }

      return matches;
   }

   public void insert(QuadCuboid cuboid) {
      if (this.root == null) {
         this.beginTree(cuboid);
      }

      if (!this.nodeFullyContainsCuboid(this.root, cuboid)) {
         this.repotTree(cuboid);
      }

      QuadNode node = this.root;
      node = this.descendAndCreate(node, cuboid);

      for(QuadNode target : this.getAllTargets(node, cuboid)) {
         this.addAndFixListHolders(target, cuboid);
      }

   }

   public boolean insertIfNoOverlaps(QuadCuboid cuboid) {
      if (this.root == null) {
         this.insert(cuboid);
         return true;
      } else {
         if (!this.nodeFullyContainsCuboid(this.root, cuboid)) {
            this.repotTree(cuboid);
         }

         QuadNode node = this.root;
         node = this.descendAndCreate(node, cuboid);
         List<QuadNode> targets = this.getAllTargets(node, cuboid);
         Deque<QuadNode> children = new ArrayDeque();
         Set<QuadCuboid> cuboids = new HashSet(256);

         for(QuadNode target : targets) {
            children.add(target);

            do {
               QuadNode childTarget = (QuadNode)children.pop();

               for(QuadNode child : childTarget.quads) {
                  if (child != null) {
                     children.push(child);
                     cuboids.addAll(child.cuboids);
                  }
               }
            } while(!children.isEmpty());

            while(target != null) {
               cuboids.addAll(target.cuboids);
               target = target.nextListHolder;
            }
         }

         for(QuadCuboid pc : cuboids) {
            if (cuboid.overlaps(pc)) {
               for(QuadNode target : targets) {
                  if (target.cuboids.size() == 0) {
                     this.pruneTree(node);
                  }
               }

               return false;
            }
         }

         for(QuadNode target : targets) {
            this.addAndFixListHolders(target, cuboid);
         }

         return true;
      }
   }

   private boolean nodeFullyContainsCuboid(QuadNode node, QuadCuboid cuboid) {
      return node.x <= cuboid.lowCoords[0] && node.z <= cuboid.lowCoords[2] && node.x + node.size >= cuboid.highCoords[0] && node.z + node.size >= cuboid.highCoords[2];
   }

   public boolean overlapsExisting(QuadCuboid cuboid) {
      if (this.root == null) {
         return false;
      } else {
         if (!this.nodeFullyContainsCuboid(this.root, cuboid)) {
            this.repotTree(cuboid);
         }

         QuadNode node = this.root;
         node = this.descendNoCreate(node, cuboid);
         List<QuadNode> targets = this.getAllTargetsNoCreate(node, cuboid);
         Deque<QuadNode> children = new ArrayDeque();
         Set<QuadCuboid> cuboids = new HashSet(256);

         for(QuadNode target : targets) {
            children.add(target);

            do {
               QuadNode childTarget = (QuadNode)children.pop();

               for(QuadNode child : childTarget.quads) {
                  if (child != null) {
                     children.push(child);
                     cuboids.addAll(child.cuboids);
                  }
               }
            } while(!children.isEmpty());

            while(target != null) {
               cuboids.addAll(target.cuboids);
               target = target.nextListHolder;
            }
         }

         for(QuadCuboid pc : cuboids) {
            if (cuboid.overlaps(pc)) {
               return true;
            }
         }

         return false;
      }
   }

   private void pruneTree(QuadNode node) {
      while(node.parent != null && node.quads[0] == null && node.quads[1] == null && node.quads[2] == null && node.quads[3] == null) {
         int i = 0;
         if (node.x != node.parent.x) {
            ++i;
         }

         if (node.z != node.parent.z) {
            i += 2;
         }

         node = node.parent;
         node.quads[i] = null;
      }

   }

   private BookmarkedResult relatedSearch(QuadNode bookmark, int x, int y, int z) {
      if (bookmark == null) {
         bookmark = this.root;
      }

      QuadNode node = this.ascendFirstSearch(bookmark, x, z);
      return new BookmarkedResult(node, this.getMatchingCuboids(node, x, y, z));
   }

   public void remove(QuadCuboid cuboid) {
      if (this.root != null) {
         QuadNode node = this.descendAndCreate(this.root, cuboid);

         for(QuadNode target : this.getAllTargets(node, cuboid)) {
            this.removeAndFixListHolders(target, cuboid);
         }

      }
   }

   private void removeAndFixListHolders(QuadNode node, QuadCuboid cuboid) {
      node.cuboids.remove(cuboid);
      if (node.cuboids.size() <= 0) {
         Deque<QuadNode> todo = new ArrayDeque();
         todo.push(node);

         do {
            QuadNode current = (QuadNode)todo.pop();

            for(QuadNode child : current.quads) {
               if (child != null) {
                  if (child.cuboids.size() == 0) {
                     todo.push(child);
                  }

                  child.nextListHolder = node.nextListHolder;
               }
            }
         } while(!todo.isEmpty());

         this.pruneTree(node);
      }
   }

   private void repotTree(QuadCuboid cuboid) {
      do {
         QuadNode oldRoot = this.root;
         this.root = new QuadNode(oldRoot.x, oldRoot.z, (oldRoot.size << 1) + 1, (QuadNode)null);
         oldRoot.parent = this.root;
         int i = 0;
         if (cuboid.lowCoords[0] < this.root.x) {
            ++i;
            QuadNode var10000 = this.root;
            var10000.x -= oldRoot.size + 1;
         }

         if (cuboid.lowCoords[2] < this.root.z) {
            i += 2;
            QuadNode var4 = this.root;
            var4.z -= oldRoot.size + 1;
         }

         this.root.quads[i] = oldRoot;
      } while(!this.nodeFullyContainsCuboid(this.root, cuboid));

   }

   public List search(int x, int y, int z) {
      QuadNode node = this.descendAndSearch(this.root, x, z);
      return this.getMatchingCuboids(node, x, y, z);
   }
}
