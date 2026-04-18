package net.citizensnpcs;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import java.util.List;
import java.util.Map;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.CommandSenderCreateNPCEvent;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.EntityTargetNPCEvent;
import net.citizensnpcs.api.event.NPCCombustByBlockEvent;
import net.citizensnpcs.api.event.NPCCombustByEntityEvent;
import net.citizensnpcs.api.event.NPCCombustEvent;
import net.citizensnpcs.api.event.NPCDamageByBlockEvent;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDamageEvent;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.PlayerCreateNPCEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.Controllable;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.util.NMS;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class EventListen implements Listener {
   private final NPCRegistry npcRegistry = CitizensAPI.getNPCRegistry();
   private final Map registries;
   private final ListMultimap toRespawn = ArrayListMultimap.create();

   EventListen(Map registries) {
      super();
      this.registries = registries;
   }

   private void checkCreationEvent(CommandSenderCreateNPCEvent event) {
      if (!event.getCreator().hasPermission("citizens.admin.avoid-limits")) {
         int limit = Settings.Setting.DEFAULT_NPC_LIMIT.asInt();
         int maxChecks = Settings.Setting.MAX_NPC_LIMIT_CHECKS.asInt();

         for(int i = maxChecks; i >= 0; --i) {
            if (event.getCreator().hasPermission("citizens.npc.limit." + i)) {
               limit = i;
               break;
            }
         }

         if (limit >= 0) {
            int owned = 0;

            for(NPC npc : this.npcRegistry) {
               if (!event.getNPC().equals(npc) && npc.hasTrait(Owner.class) && ((Owner)npc.getTrait(Owner.class)).isOwnedBy(event.getCreator())) {
                  ++owned;
               }
            }

            int wouldOwn = owned + 1;
            if (wouldOwn > limit) {
               event.setCancelled(true);
               event.setCancelReason(Messaging.tr("citizens.limits.over-npc-limit", limit));
            }

         }
      }
   }

   private Iterable getAllNPCs() {
      return Iterables.concat(this.npcRegistry, Iterables.concat(this.registries.values()));
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onChunkLoad(ChunkLoadEvent event) {
      this.respawnAllFromCoord(this.toCoord(event.getChunk()));
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onChunkUnload(ChunkUnloadEvent event) {
      ChunkCoord coord = this.toCoord(event.getChunk());
      Location loc = new Location((World)null, (double)0.0F, (double)0.0F, (double)0.0F);

      for(NPC npc : this.getAllNPCs()) {
         if (npc.isSpawned()) {
            loc = npc.getBukkitEntity().getLocation(loc);
            boolean sameChunkCoordinates = coord.z == loc.getBlockZ() >> 4 && coord.x == loc.getBlockX() >> 4;
            if (sameChunkCoordinates && event.getWorld().equals(loc.getWorld())) {
               if (!npc.despawn(DespawnReason.CHUNK_UNLOAD)) {
                  event.setCancelled(true);
                  Messaging.debug("Cancelled chunk unload at [" + coord.x + "," + coord.z + "]");
                  this.respawnAllFromCoord(coord);
                  return;
               }

               this.toRespawn.put(coord, npc);
               Messaging.debug("Despawned id", npc.getId(), "due to chunk unload at [" + coord.x + "," + coord.z + "]");
            }
         }
      }

   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onCommandSenderCreateNPC(CommandSenderCreateNPCEvent event) {
      this.checkCreationEvent(event);
   }

   @EventHandler
   public void onEntityCombust(EntityCombustEvent event) {
      NPC npc = this.npcRegistry.getNPC(event.getEntity());
      if (npc != null) {
         event.setCancelled((Boolean)npc.data().get("protected", true));
         if (event instanceof EntityCombustByEntityEvent) {
            Bukkit.getPluginManager().callEvent(new NPCCombustByEntityEvent((EntityCombustByEntityEvent)event, npc));
         } else if (event instanceof EntityCombustByBlockEvent) {
            Bukkit.getPluginManager().callEvent(new NPCCombustByBlockEvent((EntityCombustByBlockEvent)event, npc));
         } else {
            Bukkit.getPluginManager().callEvent(new NPCCombustEvent(event, npc));
         }

      }
   }

   @EventHandler
   public void onEntityDamage(EntityDamageEvent event) {
      NPC npc = this.npcRegistry.getNPC(event.getEntity());
      if (npc != null) {
         event.setCancelled((Boolean)npc.data().get("protected", true));
         if (event instanceof EntityDamageByEntityEvent) {
            NPCDamageByEntityEvent damageEvent = new NPCDamageByEntityEvent(npc, (EntityDamageByEntityEvent)event);
            Bukkit.getPluginManager().callEvent(damageEvent);
            if (!damageEvent.isCancelled() || !(damageEvent.getDamager() instanceof Player)) {
               return;
            }

            Player damager = (Player)damageEvent.getDamager();
            NPCLeftClickEvent leftClickEvent = new NPCLeftClickEvent(npc, damager);
            Bukkit.getPluginManager().callEvent(leftClickEvent);
         } else if (event instanceof EntityDamageByBlockEvent) {
            Bukkit.getPluginManager().callEvent(new NPCDamageByBlockEvent(npc, (EntityDamageByBlockEvent)event));
         } else {
            Bukkit.getPluginManager().callEvent(new NPCDamageEvent(npc, event));
         }

      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onEntityDeath(EntityDeathEvent event) {
      final NPC npc = this.npcRegistry.getNPC(event.getEntity());
      if (npc != null) {
         Bukkit.getPluginManager().callEvent(new NPCDeathEvent(npc, event));
         final Location location = npc.getBukkitEntity().getLocation();
         npc.despawn(DespawnReason.DEATH);
         if ((Integer)npc.data().get("respawn-delay", -1) >= 0) {
            int delay = (Integer)npc.data().get("respawn-delay", -1);
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
               public void run() {
                  if (!npc.isSpawned()) {
                     npc.spawn(location);
                  }

               }
            }, (long)(delay + 2));
         }

      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onEntitySpawn(CreatureSpawnEvent event) {
      if (event.isCancelled() && this.npcRegistry.isNPC(event.getEntity())) {
         event.setCancelled(false);
      }

   }

   @EventHandler
   public void onEntityTarget(EntityTargetEvent event) {
      NPC npc = this.npcRegistry.getNPC(event.getTarget());
      if (npc != null) {
         event.setCancelled(!(Boolean)npc.data().get("protected-target", !(Boolean)npc.data().get("protected", true)));
         Bukkit.getPluginManager().callEvent(new EntityTargetNPCEvent(event, npc));
      }
   }

   @EventHandler
   public void onNeedsRespawn(NPCNeedsRespawnEvent event) {
      ChunkCoord coord = this.toCoord(event.getSpawnLocation());
      if (!this.toRespawn.containsEntry(coord, event.getNPC())) {
         this.toRespawn.put(coord, event.getNPC());
      }
   }

   @EventHandler
   public void onNPCDespawn(NPCDespawnEvent event) {
      if (event.getReason() == DespawnReason.PLUGIN || event.getReason() == DespawnReason.REMOVAL) {
         this.toRespawn.remove(this.toCoord(event.getNPC().getBukkitEntity().getLocation()), event.getNPC());
      }

   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
      if (event.getPlayer() instanceof NPCHolder) {
         NMS.removeFromServerPlayerList(event.getPlayer());
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onPlayerCreateNPC(PlayerCreateNPCEvent event) {
      this.checkCreationEvent(event);
   }

   @EventHandler
   public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
      NPC npc = this.npcRegistry.getNPC(event.getRightClicked());
      if (npc != null) {
         Player player = event.getPlayer();
         NPCRightClickEvent rightClickEvent = new NPCRightClickEvent(npc, player);
         Bukkit.getPluginManager().callEvent(rightClickEvent);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      Editor.leave(event.getPlayer());
      if (event.getPlayer().isInsideVehicle()) {
         NPC npc = this.npcRegistry.getNPC(event.getPlayer().getVehicle());
         if (npc != null) {
            event.getPlayer().leaveVehicle();
         }
      }

   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onVehicleEnter(VehicleEnterEvent event) {
      if (this.npcRegistry.isNPC(event.getEntered())) {
         NPC npc = this.npcRegistry.getNPC(event.getEntered());
         if (npc.getBukkitEntity().getType() == EntityType.HORSE && !((Controllable)npc.getTrait(Controllable.class)).isEnabled()) {
            event.setCancelled(true);
         }

      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onWorldLoad(WorldLoadEvent event) {
      for(ChunkCoord chunk : this.toRespawn.keySet()) {
         if (chunk.worldName.equals(event.getWorld().getName()) && event.getWorld().isChunkLoaded(chunk.x, chunk.z)) {
            this.respawnAllFromCoord(chunk);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onWorldUnload(WorldUnloadEvent event) {
      for(NPC npc : this.getAllNPCs()) {
         if (npc.isSpawned() && npc.getBukkitEntity().getWorld().equals(event.getWorld())) {
            boolean despawned = npc.despawn(DespawnReason.WORLD_UNLOAD);
            if (event.isCancelled() || !despawned) {
               for(ChunkCoord coord : this.toRespawn.keySet()) {
                  if (event.getWorld().getName().equals(coord.worldName)) {
                     this.respawnAllFromCoord(coord);
                  }
               }

               return;
            }

            this.storeForRespawn(npc);
            Messaging.debug("Despawned", npc.getId() + "due to world unload at", event.getWorld().getName());
         }
      }

   }

   private void respawnAllFromCoord(ChunkCoord coord) {
      List<NPC> ids = this.toRespawn.get(coord);

      for(int i = 0; i < ids.size(); ++i) {
         NPC npc = (NPC)ids.get(i);
         boolean success = this.spawn(npc);
         if (!success) {
            Messaging.debug("Couldn't respawn id", npc.getId(), "during chunk event at [" + coord.x + "," + coord.z + "]");
         } else {
            ids.remove(i--);
            Messaging.debug("Spawned id", npc.getId(), "due to chunk event at [" + coord.x + "," + coord.z + "]");
         }
      }

   }

   private boolean spawn(NPC npc) {
      Location spawn = ((CurrentLocation)npc.getTrait(CurrentLocation.class)).getLocation();
      if (spawn == null) {
         Messaging.debug("Couldn't find a spawn location for despawned NPC id", npc.getId());
         return false;
      } else {
         return npc.spawn(spawn);
      }
   }

   private void storeForRespawn(NPC npc) {
      this.toRespawn.put(this.toCoord(npc.getBukkitEntity().getLocation()), npc);
   }

   private ChunkCoord toCoord(Chunk chunk) {
      return new ChunkCoord(chunk);
   }

   private ChunkCoord toCoord(Location loc) {
      return new ChunkCoord(loc.getWorld().getName(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
   }

   private static class ChunkCoord {
      private final String worldName;
      private final int x;
      private final int z;

      private ChunkCoord(Chunk chunk) {
         this(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
      }

      private ChunkCoord(String worldName, int x, int z) {
         super();
         this.x = x;
         this.z = z;
         this.worldName = worldName;
      }

      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            ChunkCoord other = (ChunkCoord)obj;
            if (this.worldName == null) {
               if (other.worldName != null) {
                  return false;
               }
            } else if (!this.worldName.equals(other.worldName)) {
               return false;
            }

            return this.x == other.x && this.z == other.z;
         } else {
            return false;
         }
      }

      public int hashCode() {
         int prime = 31;
         int result = 31 + (this.worldName == null ? 0 : this.worldName.hashCode());
         result = 31 * result + this.x;
         return 31 * result + this.z;
      }
   }
}
