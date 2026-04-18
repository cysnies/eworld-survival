package net.citizensnpcs.api.trait.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class Spawned extends Trait {
   private boolean shouldSpawn = true;

   public Spawned() {
      super("spawned");
   }

   public void load(DataKey key) throws NPCLoadException {
      this.shouldSpawn = key.getBoolean("");
   }

   public void save(DataKey key) {
      key.setBoolean("", this.shouldSpawn);
   }

   public void setSpawned(boolean shouldSpawn) {
      this.shouldSpawn = shouldSpawn;
   }

   public boolean shouldSpawn() {
      return this.shouldSpawn;
   }

   public String toString() {
      return "Spawned{" + this.shouldSpawn + "}";
   }
}
