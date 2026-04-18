package com.onarandombox.MultiverseCore.configuration;

import java.util.Map;
import me.main__.util.multiverse.SerializationConfig.Property;
import me.main__.util.multiverse.SerializationConfig.SerializationConfig;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("MVSpawnSettings")
public class SpawnSettings extends SerializationConfig {
   @Property
   private SubSpawnSettings animals;
   @Property
   private SubSpawnSettings monsters;

   public SpawnSettings() {
      super();
   }

   public SpawnSettings(Map values) {
      super(values);
   }

   public void setDefaults() {
      this.animals = new SubSpawnSettings();
      this.monsters = new SubSpawnSettings();
   }

   public SubSpawnSettings getAnimalSettings() {
      return this.animals;
   }

   public SubSpawnSettings getMonsterSettings() {
      return this.monsters;
   }
}
