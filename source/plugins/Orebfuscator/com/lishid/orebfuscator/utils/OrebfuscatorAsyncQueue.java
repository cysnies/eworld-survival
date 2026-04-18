package com.lishid.orebfuscator.utils;

import java.util.ArrayDeque;

public class OrebfuscatorAsyncQueue {
   private final Object lockObject = new Object();
   private ArrayDeque list = new ArrayDeque();

   public OrebfuscatorAsyncQueue() {
      super();
   }

   public void clear() {
      synchronized(this.lockObject) {
         this.list.clear();
      }
   }

   public void queue(Object input) {
      synchronized(this.lockObject) {
         this.list.add(input);
         this.lockObject.notify();
      }
   }

   public Object dequeue() throws InterruptedException {
      synchronized(this.lockObject) {
         while(this.list.size() <= 0) {
            this.lockObject.wait();
         }

         return this.list.pop();
      }
   }
}
