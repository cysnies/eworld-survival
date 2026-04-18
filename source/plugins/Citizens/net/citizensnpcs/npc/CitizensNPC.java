package net.citizensnpcs.npc;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import java.util.Collection;
import net.citizensnpcs.NPCNeedsRespawnEvent;
import net.citizensnpcs.Settings;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.AbstractNPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.ai.CitizensNavigator;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_6_R2.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class CitizensNPC extends AbstractNPC {
   private EntityController entityController;
   private final CitizensNavigator navigator = new CitizensNavigator(this);
   private static final String NPC_METADATA_MARKER = "NPC";

   public CitizensNPC(int id, String name, EntityController entityController) {
      super(id, name);
      Preconditions.checkNotNull(entityController);
      this.entityController = entityController;
   }

   public boolean despawn(DespawnReason reason) {
      if (!this.isSpawned()) {
         Messaging.debug("Tried to despawn", this.getId(), "while already despawned.");
         return false;
      } else {
         NPCDespawnEvent event = new NPCDespawnEvent(this, reason);
         if (reason == DespawnReason.CHUNK_UNLOAD) {
            event.setCancelled(Settings.Setting.KEEP_CHUNKS_LOADED.asBoolean());
         }

         Bukkit.getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            this.getBukkitEntity().getLocation().getChunk().load();
            Messaging.debug("Couldn't despawn", this.getId(), "due to despawn event cancellation. Force loaded chunk.");
            return false;
         } else {
            boolean keepSelected = ((Spawned)this.getTrait(Spawned.class)).shouldSpawn();
            if (!keepSelected) {
               this.data().remove("selectors");
            }

            for(Trait trait : this.traits.values()) {
               trait.onDespawn();
            }

            this.navigator.onDespawn();
            this.entityController.remove();
            return true;
         }
      }
   }

   public void faceLocation(Location location) {
      if (this.isSpawned()) {
         Util.faceLocation(this.getBukkitEntity(), location);
      }
   }

   public LivingEntity getBukkitEntity() {
      return this.entityController == null ? null : this.entityController.getBukkitEntity();
   }

   public Navigator getNavigator() {
      return this.navigator;
   }

   public Location getStoredLocation() {
      return this.isSpawned() ? this.getBukkitEntity().getLocation() : ((CurrentLocation)this.getTrait(CurrentLocation.class)).getLocation();
   }

   public boolean isSpawned() {
      return this.getBukkitEntity() != null;
   }

   public void load(DataKey root) {
      super.load(root);
      CurrentLocation spawnLocation = (CurrentLocation)this.getTrait(CurrentLocation.class);
      if (((Spawned)this.getTrait(Spawned.class)).shouldSpawn() && spawnLocation.getLocation() != null) {
         this.spawn(spawnLocation.getLocation());
      }

      this.navigator.load(root.getRelative("navigator"));
   }

   public void save(DataKey root) {
      super.save(root);
      this.navigator.save(root.getRelative("navigator"));
   }

   public void setBukkitEntityType(EntityType type) {
      EntityController controller = EntityControllers.createForType(type);
      if (controller == null) {
         throw new IllegalArgumentException("Unsupported entity type " + type);
      } else {
         this.setEntityController(controller);
      }
   }

   public void setEntityController(EntityController newController) {
      Preconditions.checkNotNull(newController);
      boolean wasSpawned = this.isSpawned();
      Location prev = null;
      if (wasSpawned) {
         prev = this.getBukkitEntity().getLocation();
         this.despawn(DespawnReason.PENDING_RESPAWN);
      }

      this.entityController = newController;
      if (wasSpawned) {
         this.spawn(prev);
      }

   }

   public boolean spawn(Location at) {
      Preconditions.checkNotNull(at, "location cannot be null");
      if (this.isSpawned()) {
         Messaging.debug("Tried to spawn", this.getId(), "while already spawned.");
         return false;
      } else {
         at = at.clone();
         this.entityController.spawn(at, this);
         EntityLiving mcEntity = ((CraftLivingEntity)this.getBukkitEntity()).getHandle();
         boolean couldSpawn = !Util.isLoaded(at) ? false : mcEntity.world.addEntity(mcEntity, SpawnReason.CUSTOM);
         mcEntity.setPositionRotation(at.getX(), at.getY(), at.getZ(), at.getYaw(), at.getPitch());
         if (!couldSpawn) {
            Messaging.debug("Retrying spawn of", this.getId(), "later due to chunk being unloaded.");
            this.entityController.remove();
            Bukkit.getPluginManager().callEvent(new NPCNeedsRespawnEvent(this, at));
            return false;
         } else {
            NMS.setHeadYaw(mcEntity, at.getYaw());
            NPCSpawnEvent spawnEvent = new NPCSpawnEvent(this, at);
            Bukkit.getPluginManager().callEvent(spawnEvent);
            if (spawnEvent.isCancelled()) {
               this.entityController.remove();
               Messaging.debug("Couldn't spawn", this.getId(), "due to event cancellation.");
               return false;
            } else {
               this.getBukkitEntity().setMetadata("NPC", new FixedMetadataValue(CitizensAPI.getPlugin(), true));
               ((CurrentLocation)this.getTrait(CurrentLocation.class)).setLocation(at);
               ((Spawned)this.getTrait(Spawned.class)).setSpawned(true);
               this.navigator.onSpawn();
               Collection<Trait> onSpawn = this.traits.values();

               for(Trait trait : (Trait[])onSpawn.toArray(new Trait[onSpawn.size()])) {
                  try {
                     trait.onSpawn();
                  } catch (Throwable ex) {
                     Messaging.severeTr("citizens.notifications.trait-onspawn-failed", trait.getName(), this.getId());
                     ex.printStackTrace();
                  }
               }

               this.getBukkitEntity().setRemoveWhenFarAway(false);
               this.getBukkitEntity().setCustomName(this.getFullName());
               return true;
            }
         }
      }
   }

   private void teleport(final Entity entity, Location location, boolean loaded, int delay) {
      if (!loaded) {
         location.getBlock().getChunk();
      }

      final Entity passenger = entity.getPassenger();
      entity.eject();
      entity.teleport(location);
      if (passenger != null) {
         this.teleport(passenger, location, true, delay++);
         Runnable task = new Runnable() {
            public void run() {
               NMS.mount(entity, passenger);
            }
         };
         if (!location.getWorld().equals(entity.getWorld())) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), task, (long)delay);
         } else {
            task.run();
         }

      }
   }

   public void teleport(Location location, PlayerTeleportEvent.TeleportCause cause) {
      if (this.isSpawned()) {
         this.teleport(NMS.getRootVehicle(this.getBukkitEntity()), location, false, 5);
      }
   }

   public void update() {
      try {
         super.update();
         if (this.isSpawned()) {
            NMS.trySwim(this.getBukkitEntity());
            this.navigator.run();
         }
      } catch (Exception ex) {
         Throwable error = Throwables.getRootCause(ex);
         Messaging.logTr("citizens.notifications.exception-updating-npc", this.getId(), error.getMessage());
         error.printStackTrace();
      }

   }
}
