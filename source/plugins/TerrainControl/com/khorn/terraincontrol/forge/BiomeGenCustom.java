package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.MobAlternativeNames;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import java.util.List;
import java.util.logging.Level;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.SpawnListEntry;

public class BiomeGenCustom extends BiomeGenBase {
   private int skyColor;
   private int grassColor;
   private boolean grassColorIsMultiplier;
   private int foliageColor;
   private boolean foliageColorIsMultiplier;
   private boolean grassColorSet = false;
   private boolean foliageColorSet = false;

   public BiomeGenCustom(int id, String name) {
      super(id);
      this.func_76735_a(name);
   }

   public void setEffects(BiomeConfig config) {
      this.field_76750_F = config.BiomeTemperature;
      this.field_76751_G = config.BiomeWetness;
      if (this.field_76751_G == 0.0F) {
         this.func_76745_m();
      }

      this.field_76759_H = config.WaterColor;
      this.skyColor = config.SkyColor;
      this.grassColor = config.GrassColor;
      this.grassColorIsMultiplier = config.GrassColorIsMultiplier;
      this.foliageColor = config.FoliageColor;
      this.foliageColorIsMultiplier = config.FoliageColorIsMultiplier;
      if (this.grassColor != 16777215) {
         this.grassColorSet = true;
      }

      if (this.foliageColor != 16777215) {
         this.foliageColorSet = true;
      }

      this.addMobs(this.field_76761_J, config.spawnMonstersAddDefaults, config.spawnMonsters);
      this.addMobs(this.field_76762_K, config.spawnCreaturesAddDefaults, config.spawnCreatures);
      this.addMobs(this.field_76755_L, config.spawnWaterCreaturesAddDefaults, config.spawnWaterCreatures);
      this.addMobs(this.field_82914_M, config.spawnAmbientCreaturesAddDefaults, config.spawnAmbientCreatures);
   }

   protected void addMobs(List internalList, boolean addDefaults, List configList) {
      if (!addDefaults) {
         internalList.clear();
      }

      for(WeightedMobSpawnGroup mobGroup : configList) {
         Class<? extends Entity> entityClass = this.getEntityClass(mobGroup);
         if (entityClass != null) {
            internalList.add(new SpawnListEntry(entityClass, mobGroup.getWeight(), mobGroup.getMin(), mobGroup.getMax()));
         } else {
            TerrainControl.log(Level.WARNING, "Mob type " + mobGroup.getMobName() + " not found in " + this.field_76791_y);
         }
      }

   }

   protected Class getEntityClass(WeightedMobSpawnGroup mobGroup) {
      String mobName = MobAlternativeNames.getInternalMinecraftName(mobGroup.getMobName());
      return (Class)EntityList.field_75625_b.get(mobName);
   }

   public void CopyBiome(BiomeGenBase baseBiome) {
      this.field_76753_B = baseBiome.field_76753_B;
      this.field_76752_A = baseBiome.field_76752_A;
      this.field_76791_y = baseBiome.field_76791_y;
      this.field_76790_z = baseBiome.field_76790_z;
      this.field_76748_D = baseBiome.field_76748_D;
      this.field_76749_E = baseBiome.field_76749_E;
      this.field_76750_F = baseBiome.field_76750_F;
      this.field_76760_I = baseBiome.field_76760_I;
      this.field_76759_H = baseBiome.field_76759_H;
      this.field_76761_J = baseBiome.func_76747_a(EnumCreatureType.monster);
      this.field_76762_K = baseBiome.func_76747_a(EnumCreatureType.creature);
      this.field_76755_L = baseBiome.func_76747_a(EnumCreatureType.waterCreature);
      this.field_82914_M = baseBiome.func_76747_a(EnumCreatureType.ambient);
   }

   public int func_76731_a(float v) {
      return this.skyColor;
   }

   public int func_76737_k() {
      if (!this.grassColorSet) {
         return super.func_76737_k();
      } else if (this.grassColorIsMultiplier) {
         double temperature = (double)this.func_76743_j();
         double rainfall = (double)this.func_76727_i();
         return ((ColorizerFoliage.func_77470_a(temperature, rainfall) & 16711422) + this.grassColor) / 2;
      } else {
         return this.grassColor;
      }
   }

   public int func_76726_l() {
      if (!this.foliageColorSet) {
         return super.func_76726_l();
      } else if (this.foliageColorIsMultiplier) {
         double temperature = (double)this.func_76743_j();
         double rainfall = (double)this.func_76727_i();
         return ((ColorizerFoliage.func_77470_a(temperature, rainfall) & 16711422) + this.foliageColor) / 2;
      } else {
         return this.foliageColor;
      }
   }

   public String toString() {
      return "BiomeGenCustom of " + this.field_76791_y;
   }
}
