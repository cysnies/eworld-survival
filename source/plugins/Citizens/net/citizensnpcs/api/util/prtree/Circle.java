package net.citizensnpcs.api.util.prtree;

import java.util.ArrayList;
import java.util.List;

class Circle {
   private int currentPos;
   private final List data;

   public Circle(int size) {
      super();
      this.data = new ArrayList(size);
   }

   public void add(Object t) {
      this.data.add(t);
   }

   public Object get(int pos) {
      pos %= this.data.size();
      return this.data.get(pos);
   }

   public Object getNext() {
      T ret = (T)this.data.get(this.currentPos++);
      this.currentPos %= this.data.size();
      return ret;
   }

   public int getNumElements() {
      return this.data.size();
   }

   public void reset() {
      this.currentPos = 0;
   }
}
