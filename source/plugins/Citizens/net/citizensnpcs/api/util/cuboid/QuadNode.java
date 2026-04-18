package net.citizensnpcs.api.util.cuboid;

import java.util.ArrayList;
import java.util.List;

public class QuadNode {
   final List cuboids = new ArrayList();
   QuadNode nextListHolder;
   QuadNode parent = null;
   final QuadNode[] quads = new QuadNode[4];
   final int size;
   int x;
   int z;

   QuadNode(int x, int z, int size, QuadNode parent) {
      super();
      this.x = x;
      this.z = z;
      this.size = size;
      this.parent = parent;
      if (parent != null) {
         if (parent.cuboids.size() != 0) {
            this.nextListHolder = parent;
         } else {
            this.nextListHolder = parent.nextListHolder;
         }

      }
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof QuadNode)) {
         return false;
      } else {
         QuadNode n = (QuadNode)o;
         return this.x == n.x && this.z == n.z && this.size == n.size;
      }
   }

   public int hashCode() {
      return this.x ^ this.z ^ ~this.size;
   }

   public String toString() {
      return "(" + this.x + "," + this.z + "; " + this.size + ")";
   }
}
