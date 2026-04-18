package com.lishid.orebfuscator.obfuscation;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;

public class ProximityHider extends Thread implements Runnable {
   public static HashMap proximityHiderTracker = new HashMap();
   public static HashMap playersToCheck = new HashMap();
   public static ProximityHider thread = new ProximityHider();
   public long lastExecute = System.currentTimeMillis();
   public AtomicBoolean kill = new AtomicBoolean(false);
   public static boolean running = false;

   public ProximityHider() {
      super();
   }

   public static void Load() {
      running = true;
      if (thread == null || thread.isInterrupted() || !thread.isAlive()) {
         thread = new ProximityHider();
         thread.setName("Orebfuscator ProximityHider Thread");
         thread.setPriority(1);
         thread.start();
      }

   }

   public static void terminate() {
      if (thread != null) {
         thread.kill.set(true);
      }

   }

   public void run() {
      label143:
      while(!this.isInterrupted() && !this.kill.get()) {
         try {
            long timeWait = this.lastExecute + (long)OrebfuscatorConfig.ProximityHiderRate - System.currentTimeMillis();
            this.lastExecute = System.currentTimeMillis();
            if (timeWait > 0L) {
               Thread.sleep(timeWait);
            }

            if (!OrebfuscatorConfig.UseProximityHider) {
               running = false;
               return;
            }

            HashMap<Player, Location> newPlayers = new HashMap();
            synchronized(playersToCheck) {
               newPlayers.putAll(playersToCheck);
               playersToCheck.clear();
            }

            int distanceSquared = OrebfuscatorConfig.ProximityHiderDistance;
            distanceSquared *= distanceSquared;
            Iterator var6 = newPlayers.keySet().iterator();

            while(true) {
               Player p;
               while(true) {
                  if (!var6.hasNext()) {
                     continue label143;
                  }

                  p = (Player)var6.next();
                  synchronized(proximityHiderTracker) {
                     if (p != null && proximityHiderTracker.containsKey(p)) {
                        break;
                     }
                  }
               }

               Location loc1 = p.getLocation();
               Location loc2 = (Location)newPlayers.get(p);
               if (!loc1.getWorld().equals(loc2.getWorld())) {
                  synchronized(proximityHiderTracker) {
                     proximityHiderTracker.remove(p);
                  }
               } else if (loc1.getBlockX() != loc2.getBlockX() || loc1.getBlockY() != loc2.getBlockY() || loc1.getBlockZ() != loc2.getBlockZ()) {
                  HashSet<Block> blocks = new HashSet();
                  HashSet<Block> removedBlocks = new HashSet();
                  synchronized(proximityHiderTracker) {
                     if (proximityHiderTracker.get(p) != null) {
                        blocks.addAll((Collection)proximityHiderTracker.get(p));
                     }
                  }

                  for(Block b : blocks) {
                     if (b != null && p != null && b.getWorld() != null && p.getWorld() != null) {
                        if (!p.getWorld().equals(b.getWorld())) {
                           removedBlocks.add(b);
                        } else if (p.getLocation().distanceSquared(b.getLocation()) < (double)distanceSquared) {
                           removedBlocks.add(b);
                           if (CalculationsUtil.isChunkLoaded(b.getWorld(), b.getChunk().getX(), b.getChunk().getZ())) {
                              p.sendBlockChange(b.getLocation(), b.getTypeId(), b.getData());
                              if (b instanceof CreatureSpawner) {
                                 CreatureSpawner spawner = (CreatureSpawner)b;
                                 spawner.setSpawnedType(spawner.getSpawnedType());
                              }
                           }
                        }
                     } else {
                        removedBlocks.add(b);
                     }
                  }

                  synchronized(proximityHiderTracker) {
                     for(Block b : removedBlocks) {
                        if (proximityHiderTracker.get(p) != null) {
                           ((HashSet)proximityHiderTracker.get(p)).remove(b);
                        }
                     }
                  }
               }
            }
         } catch (Exception e) {
            Orebfuscator.log((Throwable)e);
         }
      }

      running = false;
   }

   public static void restart() {
      synchronized(thread) {
         if (thread.isInterrupted() || !thread.isAlive()) {
            running = false;
         }

         if (!running && OrebfuscatorConfig.UseProximityHider) {
            Load();
         }

      }
   }

   public static void AddProximityBlocks(Player player, ArrayList blocks) {
      if (OrebfuscatorConfig.UseProximityHider) {
         restart();
         synchronized(proximityHiderTracker) {
            if (!proximityHiderTracker.containsKey(player)) {
               proximityHiderTracker.put(player, new HashSet());
            }

            for(Block b : blocks) {
               ((HashSet)proximityHiderTracker.get(player)).add(b);
            }

         }
      }
   }
}
