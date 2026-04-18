package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.MobAlternativeNames;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import net.minecraft.server.v1_6_R2.BiomeBase;
import net.minecraft.server.v1_6_R2.BiomeMeta;
import net.minecraft.server.v1_6_R2.Entity;
import net.minecraft.server.v1_6_R2.EntityTypes;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_6_R2.block.CraftBlock;

public class CustomBiome extends BiomeBase {
   public CustomBiome(int id, String name) {
      super(id);
      this.a(name);

      try {
         Field biomeMapping = CraftBlock.class.getDeclaredField("BIOME_MAPPING");
         biomeMapping.setAccessible(true);
         Biome[] mappingArray = (Biome[])biomeMapping.get((Object)null);
         mappingArray[id] = Biome.OCEAN;
      } catch (Exception e) {
         TerrainControl.log(Level.SEVERE, "Couldn't update Bukkit's biome mappings!");
         e.printStackTrace();
      }

   }

   public void setEffects(BiomeConfig config) {
      this.D = config.BiomeHeight;
      this.E = config.BiomeVolatility;
      this.A = config.SurfaceBlock;
      this.B = config.GroundBlock;
      this.temperature = config.BiomeTemperature;
      this.humidity = config.BiomeWetness;
      if (this.humidity == 0.0F) {
         this.b();
      }

      this.addMobs(this.J, config.spawnMonstersAddDefaults, config.spawnMonsters);
      this.addMobs(this.K, config.spawnCreaturesAddDefaults, config.spawnCreatures);
      this.addMobs(this.L, config.spawnWaterCreaturesAddDefaults, config.spawnWaterCreatures);
      this.addMobs(this.M, config.spawnAmbientCreaturesAddDefaults, config.spawnAmbientCreatures);
   }

   protected void addMobs(List internalList, boolean addDefaults, List configList) {
      if (!addDefaults) {
         internalList.clear();
      }

      for(WeightedMobSpawnGroup mobGroup : configList) {
         Class<? extends Entity> entityClass = this.getEntityClass(mobGroup);
         if (entityClass != null) {
            internalList.add(new BiomeMeta(entityClass, mobGroup.getWeight(), mobGroup.getMin(), mobGroup.getMax()));
         } else {
            TerrainControl.log(Level.WARNING, "Mob type " + mobGroup.getMobName() + " not found in " + this.y.toLowerCase());
         }
      }

   }

   protected Class getEntityClass(WeightedMobSpawnGroup mobGroup) {
      String mobName = MobAlternativeNames.getInternalMinecraftName(mobGroup.getMobName());

      try {
         Field entitiesField = EntityTypes.class.getDeclaredField("b");
         entitiesField.setAccessible(true);
         Map<String, Class<? extends Entity>> entitiesList = (Map)entitiesField.get((Object)null);
         return (Class)entitiesList.get(mobName);
      } catch (Exception e) {
         TerrainControl.log(Level.SEVERE, "Someone forgot to update the mob spawning code! Please report!");
         e.printStackTrace();
         return null;
      }
   }
}
