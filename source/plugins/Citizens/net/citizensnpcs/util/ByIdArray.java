package net.citizensnpcs.util;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ByIdArray implements Iterable {
   private final int capacity;
   private Object[] elementData;
   private int highest;
   private int lowest;
   private int modCount;
   private int size;

   public ByIdArray() {
      this(100);
   }

   public ByIdArray(int capacity) {
      super();
      this.highest = Integer.MIN_VALUE;
      this.lowest = Integer.MAX_VALUE;
      if (capacity < 0) {
         throw new IllegalArgumentException("Capacity cannot be less than 0.");
      } else {
         this.capacity = capacity;
         this.elementData = new Object[capacity];
      }
   }

   public int add(Object t) {
      int index = 0;

      while(this.elementData[index++] != null) {
         if (index >= this.elementData.length) {
            this.ensureCapacity(this.elementData.length + 1);
            index = this.elementData.length - 1;
         }
      }

      this.put(index, t);
      return index;
   }

   public void clear() {
      this.modCount = this.highest = this.size = this.lowest = 0;
      this.elementData = new Object[this.capacity];
   }

   public boolean contains(int index) {
      return this.elementData.length > index && this.elementData[index] != null;
   }

   public void ensureCapacity(int minCapacity) {
      int oldCapacity = this.elementData.length;
      if (minCapacity > oldCapacity) {
         int newCapacity = oldCapacity * 3 / 2 + 1;
         if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
         }

         this.elementData = Arrays.copyOf(this.elementData, newCapacity);
      }

   }

   private void fastRemove(int index) {
      ++this.modCount;
      if (index == this.highest) {
         this.recalcHighest();
      }

      if (index == this.lowest) {
         this.recalcLowest();
      }

      this.elementData[index] = null;
      --this.size;
   }

   public Object get(int index) {
      return index >= this.elementData.length ? null : this.elementData[index];
   }

   public Iterator iterator() {
      return new Itr();
   }

   public void put(int index, Object t) {
      if (t == null) {
         throw new IllegalArgumentException("can't insert a null object");
      } else {
         ++this.modCount;
         if (index > this.highest) {
            this.highest = index;
         }

         if (index < this.lowest) {
            this.lowest = index;
         }

         this.ensureCapacity(index + 2);
         this.elementData[index] = t;
         ++this.size;
      }
   }

   private void recalcHighest() {
      this.highest = this.elementData.length - 1;

      while(this.highest != 0 && this.elementData[this.highest--] == null) {
      }

   }

   private void recalcLowest() {
      this.lowest = 0;

      while(this.elementData.length > this.lowest && this.elementData[this.lowest++] == null) {
      }

   }

   public Object remove(int index) {
      if (index <= this.elementData.length && this.elementData[index] != null) {
         ++this.modCount;
         if (index == this.highest) {
            this.recalcHighest();
         }

         if (index == this.lowest) {
            this.recalcLowest();
         }

         T prev = (T)this.elementData[index];
         this.elementData[index] = null;
         if (prev != null) {
            --this.size;
         }

         return prev;
      } else {
         return null;
      }
   }

   public int size() {
      return this.size;
   }

   public void trimToSize() {
      if (this.elementData.length > this.highest) {
         this.elementData = Arrays.copyOf(this.elementData, this.highest + 1);
      }

   }

   public static ByIdArray create() {
      return new ByIdArray();
   }

   private class Itr implements Iterator {
      private int expected;
      private int idx;

      private Itr() {
         super();
         this.expected = ByIdArray.this.modCount;
         if (ByIdArray.this.size > 0) {
            if (ByIdArray.this.highest == Integer.MIN_VALUE || ByIdArray.this.highest >= ByIdArray.this.elementData.length || ByIdArray.this.elementData[ByIdArray.this.highest] == null) {
               ByIdArray.this.recalcHighest();
            }

            if (ByIdArray.this.lowest >= ByIdArray.this.elementData.length || ByIdArray.this.elementData[ByIdArray.this.lowest] == null) {
               ByIdArray.this.recalcLowest();
            }

            this.idx = ByIdArray.this.lowest - 1;
         }

      }

      public boolean hasNext() {
         if (ByIdArray.this.modCount != this.expected) {
            throw new ConcurrentModificationException();
         } else {
            return ByIdArray.this.size > 0 && ByIdArray.this.highest > this.idx;
         }
      }

      public Object next() {
         if (ByIdArray.this.modCount != this.expected) {
            throw new ConcurrentModificationException();
         } else if (this.idx > ByIdArray.this.highest) {
            throw new NoSuchElementException();
         } else {
            do {
               ++this.idx;
            } while(this.idx != ByIdArray.this.highest + 1 && ByIdArray.this.elementData[this.idx] == null);

            T next = (T)ByIdArray.this.elementData[this.idx];
            if (next == null) {
               throw new NoSuchElementException();
            } else {
               return next;
            }
         }
      }

      public void remove() {
         if (ByIdArray.this.modCount != this.expected) {
            throw new ConcurrentModificationException();
         } else {
            ByIdArray.this.fastRemove(this.idx);
            this.expected = ByIdArray.this.modCount;
         }
      }
   }
}
