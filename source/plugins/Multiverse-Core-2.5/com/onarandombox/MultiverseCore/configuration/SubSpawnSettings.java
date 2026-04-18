package com.onarandombox.MultiverseCore.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.main__.util.multiverse.SerializationConfig.Property;
import me.main__.util.multiverse.SerializationConfig.SerializationConfig;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("MVSpawnSubSettings")
public class SubSpawnSettings extends SerializationConfig {
   @Property
   private boolean spawn;
   @Property
   private int spawnrate;
   @Property
   private List exceptions;

   public SubSpawnSettings() {
      super();
   }

   public SubSpawnSettings(Map values) {
      super(values);
   }

   public void setDefaults() {
      this.spawn = true;
      this.exceptions = new ArrayList();
      this.spawnrate = -1;
   }

   public boolean doSpawn() {
      return this.spawn;
   }

   public void setSpawn(boolean spawn) {
      this.spawn = spawn;
   }

   public List getExceptions() {
      return this.exceptions;
   }

   public void setSpawnRate(int rate) {
      this.spawnrate = rate;
   }

   public int getSpawnRate() {
      return this.spawnrate;
   }
}
