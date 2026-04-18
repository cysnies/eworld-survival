package com.lishid.orebfuscator.internal.v1_4_6;

import com.lishid.orebfuscator.hook.ChunkProcessingThread;
import com.lishid.orebfuscator.internal.IChunkQueue;
import com.lishid.orebfuscator.internal.IPacket56;
import com.lishid.orebfuscator.internal.InternalAccessor;
import com.lishid.orebfuscator.utils.ReflectionHelper;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.server.v1_4_6.Chunk;
import net.minecraft.server.v1_4_6.ChunkCoordIntPair;
import net.minecraft.server.v1_4_6.EntityPlayer;
import net.minecraft.server.v1_4_6.NetworkManager;
import net.minecraft.server.v1_4_6.Packet;
import net.minecraft.server.v1_4_6.Packet56MapChunkBulk;
import net.minecraft.server.v1_4_6.TileEntity;
import net.minecraft.server.v1_4_6.WorldServer;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer;

public class ChunkQueue extends LinkedList implements IChunkQueue {
   private static final long serialVersionUID = -1928681564741152336L;
   List internalQueue = Collections.synchronizedList(new LinkedList());
   List outputQueue = Collections.synchronizedList(new LinkedList());
   List processingQueue = Collections.synchronizedList(new LinkedList());
   Packet56MapChunkBulk lastPacket;
   CraftPlayer player;
   Thread thread;
   AtomicBoolean kill = new AtomicBoolean(false);

   public ChunkQueue(CraftPlayer player, List previousEntries) {
      super();
      this.player = player;
      this.internalQueue.addAll(previousEntries);
   }

   public boolean remove(Object arg0) {
      return this.internalQueue.remove(arg0) || this.processingQueue.remove(arg0) || this.outputQueue.remove(arg0);
   }

   public void clear() {
      this.internalQueue.clear();
      super.clear();
   }

   public boolean add(ChunkCoordIntPair e) {
      boolean result = this.internalQueue.add(e);
      return result;
   }

   public Object[] toArray() {
      this.sort();
      return this.internalQueue.toArray();
   }

   public boolean contains(Object o) {
      return this.internalQueue.contains(o) || this.processingQueue.contains(o);
   }

   public boolean isEmpty() {
      if (this.player.getHandle().playerConnection.disconnected) {
         this.internalQueue.clear();
         this.processingQueue.clear();
         this.outputQueue.clear();
         this.lastPacket = null;
      } else {
         this.processOutput();
         this.processInput();
      }

      return true;
   }

   public void sort() {
      synchronized(this.internalQueue) {
         Collections.sort(this.internalQueue, new ChunkCoordComparator(this.player.getHandle()));
      }
   }

   public void FinishedProcessing(IPacket56 packet) {
      if (this.lastPacket != null) {
         this.player.getHandle().playerConnection.sendPacket(this.lastPacket);
         this.lastPacket = null;
      }

      this.outputQueue.addAll(this.processingQueue);
      this.processingQueue.clear();
   }

   private void processOutput() {
      while(!this.outputQueue.isEmpty()) {
         ChunkCoordIntPair chunk = (ChunkCoordIntPair)this.outputQueue.remove(0);
         if (chunk != null && ((WorldServer)this.player.getHandle().world).isLoaded(chunk.x << 4, 0, chunk.z << 4)) {
            for(Object o : ((WorldServer)this.player.getHandle().world).getTileEntities(chunk.x * 16, 0, chunk.z * 16, chunk.x * 16 + 16, 256, chunk.z * 16 + 16)) {
               this.updateTileEntity((TileEntity)o);
            }

            this.player.getHandle().p().getTracker().a(this.player.getHandle(), this.player.getHandle().p().getChunkAt(chunk.x, chunk.z));
         }
      }

   }

   private void processInput() {
      if (this.processingQueue.isEmpty() && !this.internalQueue.isEmpty()) {
         try {
            NetworkManager networkManager = (NetworkManager)this.player.getHandle().playerConnection.networkManager;
            if ((Integer)ReflectionHelper.getPrivateField(NetworkManager.class, networkManager, "y") > 75000) {
               return;
            }
         } catch (Exception var3) {
         }

         List<Chunk> chunks = new LinkedList();

         while(!this.internalQueue.isEmpty() && chunks.size() < 5) {
            ChunkCoordIntPair chunkcoordintpair = (ChunkCoordIntPair)this.internalQueue.remove(0);
            if (chunkcoordintpair != null) {
               int var10001 = chunkcoordintpair.x << 4;
               if (this.player.getHandle().world.isLoaded(var10001, 0, chunkcoordintpair.z << 4)) {
                  this.processingQueue.add(chunkcoordintpair);
                  chunks.add(this.player.getHandle().world.getChunkAt(chunkcoordintpair.x, chunkcoordintpair.z));
               }
            }
         }

         if (!chunks.isEmpty()) {
            IPacket56 packet = InternalAccessor.Instance.newPacket56();
            this.lastPacket = new Packet56MapChunkBulk(chunks);
            packet.setPacket(this.lastPacket);
            ChunkProcessingThread.Queue(packet, this.player, this);
         }
      }

   }

   private void updateTileEntity(TileEntity tileentity) {
      if (tileentity != null) {
         Packet packet = tileentity.getUpdatePacket();
         if (packet != null) {
            this.player.getHandle().playerConnection.sendPacket(packet);
         }
      }

   }

   public ListIterator listIterator() {
      return new FakeIterator((FakeIterator)null);
   }

   private class FakeIterator implements ListIterator {
      private FakeIterator() {
         super();
      }

      public boolean hasNext() {
         return false;
      }

      public ChunkCoordIntPair next() {
         return null;
      }

      public boolean hasPrevious() {
         return false;
      }

      public ChunkCoordIntPair previous() {
         return null;
      }

      public int nextIndex() {
         return 0;
      }

      public int previousIndex() {
         return 0;
      }

      public void remove() {
      }

      public void set(ChunkCoordIntPair e) {
      }

      public void add(ChunkCoordIntPair e) {
      }

      // $FF: synthetic method
      FakeIterator(FakeIterator var2) {
         this();
      }
   }

   private static class ChunkCoordComparator implements Comparator {
      private int x;
      private int z;

      public ChunkCoordComparator(EntityPlayer entityplayer) {
         super();
         this.x = (int)entityplayer.locX >> 4;
         this.z = (int)entityplayer.locZ >> 4;
      }

      public int compare(ChunkCoordIntPair a, ChunkCoordIntPair b) {
         if (a.equals(b)) {
            return 0;
         } else {
            int ax = a.x - this.x;
            int az = a.z - this.z;
            int bx = b.x - this.x;
            int bz = b.z - this.z;
            int result = (ax - bx) * (ax + bx) + (az - bz) * (az + bz);
            if (result != 0) {
               return result;
            } else if (ax < 0) {
               return bx < 0 ? bz - az : -1;
            } else {
               return bx < 0 ? 1 : az - bz;
            }
         }
      }
   }
}
