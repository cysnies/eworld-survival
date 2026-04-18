package net.citizensnpcs.trait;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.citizensnpcs.Settings;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.CommandConfigurable;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.util.Util;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class LookClose extends Trait implements Toggleable, CommandConfigurable {
   private boolean enabled;
   private Player lookingAt;
   private double range;
   private boolean realisticLooking;
   private static final Location NPC_LOCATION = new Location((World)null, (double)0.0F, (double)0.0F, (double)0.0F);

   public LookClose() {
      super("lookclose");
      this.enabled = Settings.Setting.DEFAULT_LOOK_CLOSE.asBoolean();
      this.range = Settings.Setting.DEFAULT_LOOK_CLOSE_RANGE.asDouble();
      this.realisticLooking = Settings.Setting.DEFAULT_REALISTIC_LOOKING.asBoolean();
   }

   private boolean canSeeTarget() {
      return this.realisticLooking ? this.npc.getBukkitEntity().hasLineOfSight(this.lookingAt) : true;
   }

   public void configure(CommandContext args) {
      this.range = args.getFlagDouble("range", this.range);
      this.range = args.getFlagDouble("r", this.range);
      this.realisticLooking = args.hasFlag('r');
   }

   private void findNewTarget() {
      List<Entity> nearby = this.npc.getBukkitEntity().getNearbyEntities(this.range, this.range, this.range);
      final Location npcLocation = this.npc.getBukkitEntity().getLocation(NPC_LOCATION);
      Collections.sort(nearby, new Comparator() {
         public int compare(Entity o1, Entity o2) {
            double d1 = o1.getLocation().distanceSquared(npcLocation);
            double d2 = o2.getLocation().distanceSquared(npcLocation);
            return Double.compare(d1, d2);
         }
      });

      for(Entity entity : nearby) {
         if (entity.getType() == EntityType.PLAYER && CitizensAPI.getNPCRegistry().getNPC(entity) == null) {
            this.lookingAt = (Player)entity;
            return;
         }
      }

      this.lookingAt = null;
   }

   private boolean hasInvalidTarget() {
      if (this.lookingAt == null) {
         return true;
      } else {
         if (!this.lookingAt.isOnline() || this.lookingAt.getWorld() != this.npc.getBukkitEntity().getWorld() || this.lookingAt.getLocation().distanceSquared(this.npc.getBukkitEntity().getLocation()) > this.range) {
            this.lookingAt = null;
         }

         return this.lookingAt == null;
      }
   }

   public void load(DataKey key) throws NPCLoadException {
      this.enabled = key.getBoolean("enabled", key.getBoolean(""));
      this.range = key.getDouble("range", this.range);
      this.realisticLooking = key.getBoolean("realisticlooking", key.getBoolean("realistic-looking"));
   }

   public void lookClose(boolean lookClose) {
      this.enabled = lookClose;
   }

   public void onDespawn() {
      this.lookingAt = null;
   }

   public void run() {
      if (this.enabled && this.npc.isSpawned() && !this.npc.getNavigator().isNavigating()) {
         if (this.hasInvalidTarget()) {
            this.findNewTarget();
         }

         if (this.lookingAt != null && this.canSeeTarget()) {
            Util.faceEntity(this.npc.getBukkitEntity(), this.lookingAt);
         }

      }
   }

   public void save(DataKey key) {
      if (key.keyExists("")) {
         key.removeKey("");
      }

      if (key.keyExists("realistic-looking")) {
         key.removeKey("realistic-looking");
      }

      key.setBoolean("enabled", this.enabled);
      key.setDouble("range", this.range);
      key.setBoolean("realisticlooking", this.realisticLooking);
   }

   public void setRange(int range) {
      this.range = (double)range;
   }

   public void setRealisticLooking(boolean realistic) {
      this.realisticLooking = realistic;
   }

   public boolean toggle() {
      this.enabled = !this.enabled;
      return this.enabled;
   }

   public String toString() {
      return "LookClose{" + this.enabled + "}";
   }
}
