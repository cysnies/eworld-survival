package com.lishid.orebfuscator.hook;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.internal.IChunkQueue;
import com.lishid.orebfuscator.internal.IPacket56;
import com.lishid.orebfuscator.obfuscation.Calculations;
import com.lishid.orebfuscator.utils.OrebfuscatorAsyncQueue;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Deflater;
import org.bukkit.entity.Player;

public class ChunkProcessingThread extends Thread {
   private static OrebfuscatorAsyncQueue queue = new OrebfuscatorAsyncQueue();
   private static LinkedList threads = new LinkedList();
   static ThreadLocal localDeflater = new ThreadLocal() {
      protected Deflater initialValue() {
         return new Deflater(OrebfuscatorConfig.CompressionLevel);
      }
   };
   AtomicBoolean kill = new AtomicBoolean(false);

   public ChunkProcessingThread() {
      super();
   }

   public static synchronized void KillAll() {
      for(ChunkProcessingThread thread : threads) {
         thread.kill.set(true);
         thread.interrupt();
      }

      threads.clear();
      queue.clear();
   }

   public static synchronized void SyncThreads() {
      if (threads.size() != OrebfuscatorConfig.ProcessingThreads) {
         if (threads.size() > OrebfuscatorConfig.ProcessingThreads) {
            ((ChunkProcessingThread)threads.getLast()).kill.set(true);
            ((ChunkProcessingThread)threads.getLast()).interrupt();
            threads.removeLast();
         } else {
            ChunkProcessingThread thread = new ChunkProcessingThread();
            thread.setName("Orebfuscator Processing Thread");

            try {
               thread.setPriority(OrebfuscatorConfig.OrebfuscatorPriority);
            } catch (Exception var2) {
               thread.setPriority(1);
            }

            thread.start();
            threads.add(thread);
         }
      }
   }

   public static void Queue(IPacket56 packet, Player player, IChunkQueue output) {
      SyncThreads();
      queue.queue(new ChunkProcessingOrder(packet, player, output));
   }

   public void run() {
      while(!Thread.interrupted() && !this.kill.get()) {
         try {
            ChunkProcessingOrder order = (ChunkProcessingOrder)queue.dequeue();
            Calculations.Obfuscate(order.packet, order.player);
            order.packet.compress((Deflater)localDeflater.get());
            order.output.FinishedProcessing(order.packet);
            Thread.sleep(1L);
         } catch (InterruptedException var2) {
         } catch (Exception e) {
            Orebfuscator.log((Throwable)e);
         }
      }

   }

   static class ChunkProcessingOrder {
      IPacket56 packet;
      Player player;
      IChunkQueue output;

      public ChunkProcessingOrder(IPacket56 packet, Player player, IChunkQueue output) {
         super();
         this.packet = packet;
         this.player = player;
         this.output = output;
      }
   }
}
