package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.events.EventHandler;
import com.khorn.terraincontrol.generator.resourcegens.CustomObjectGen;
import com.khorn.terraincontrol.generator.resourcegens.DungeonGen;
import com.khorn.terraincontrol.generator.resourcegens.LiquidGen;
import com.khorn.terraincontrol.generator.resourcegens.OreGen;
import com.khorn.terraincontrol.generator.resourcegens.Resource;
import com.khorn.terraincontrol.generator.resourcegens.SmallLakeGen;
import com.khorn.terraincontrol.generator.resourcegens.UndergroundLakeGen;
import cpw.mods.fml.common.registry.GameRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType;

public class EventManager extends EventHandler {
   private Map hasOreGenBegun = new HashMap();
   private Map hasDecorationBegun = new HashMap();

   public EventManager() {
      super();
   }

   public boolean onResourceProcess(Resource resource, LocalWorld localWorld, Random random, boolean villageInChunk, int chunkX, int chunkZ, boolean isCancelled) {
      SingleWorld world = (SingleWorld)localWorld;
      if (!(resource instanceof DungeonGen) && !(resource instanceof SmallLakeGen) && !(resource instanceof UndergroundLakeGen) && !(resource instanceof LiquidGen) && !(resource instanceof CustomObjectGen)) {
         if (resource instanceof OreGen) {
            if (!this.hasOreGenerationBegun(world)) {
               MinecraftForge.ORE_GEN_BUS.post(new OreGenEvent.Pre(world.getWorld(), random, chunkX, chunkZ));
               this.setOreGenerationBegun(world, true);
            }

            OreGenEvent.GenerateMinable.EventType forgeEvent = this.getOreEventType(resource.getBlockId());
            return TerrainGen.generateOre(world.getWorld(), random, (WorldGenerator)null, chunkX, chunkZ, forgeEvent);
         } else {
            if (!this.hasDecorationBegun(world)) {
               MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Pre(world.getWorld(), random, chunkX, chunkZ));
               this.setDecorationBegun(world, true);
            }

            DecorateBiomeEvent.Decorate.EventType forgeEvent = this.getDecorateEventType(resource.getBlockId());
            return TerrainGen.decorate(world.getWorld(), random, chunkX, chunkZ, forgeEvent);
         }
      } else {
         PopulateChunkEvent.Populate.EventType forgeEvent = this.getPopulateEventType(resource.getBlockId());
         return TerrainGen.populate(world.getChunkGenerator(), world.getWorld(), random, chunkX, chunkZ, villageInChunk, forgeEvent);
      }
   }

   public void onPopulateStart(LocalWorld localWorld, Random random, boolean villageInChunk, int chunkX, int chunkZ) {
      SingleWorld world = (SingleWorld)localWorld;
      this.setDecorationBegun(world, false);
      this.setOreGenerationBegun(world, false);
      PopulateChunkEvent forgeEvent = new PopulateChunkEvent.Pre(world.getChunkGenerator(), world.getWorld(), random, chunkX, chunkZ, villageInChunk);
      MinecraftForge.EVENT_BUS.post(forgeEvent);
   }

   public void onPopulateEnd(LocalWorld localWorld, Random random, boolean villageInChunk, int chunkX, int chunkZ) {
      SingleWorld world = (SingleWorld)localWorld;
      if (this.hasDecorationBegun(world)) {
         MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Post(world.getWorld(), random, chunkX, chunkZ));
         this.setDecorationBegun(world, false);
      }

      if (this.hasOreGenerationBegun(world)) {
         MinecraftForge.EVENT_BUS.post(new OreGenEvent.Post(world.getWorld(), random, chunkX, chunkZ));
         this.setOreGenerationBegun(world, false);
      }

      PopulateChunkEvent forgeEvent = new PopulateChunkEvent.Post(world.getChunkGenerator(), world.getWorld(), random, chunkX, chunkZ, villageInChunk);
      MinecraftForge.EVENT_BUS.post(forgeEvent);
      GameRegistry.generateWorld(chunkX, chunkZ, world.getWorld(), world.getChunkGenerator(), world.getChunkGenerator());
   }

   private DecorateBiomeEvent.Decorate.EventType getDecorateEventType(int blockId) {
      if (blockId == DefaultMaterial.WATER_LILY.id) {
         return EventType.LILYPAD;
      } else if (blockId == DefaultMaterial.CACTUS.id) {
         return EventType.CACTUS;
      } else if (blockId == DefaultMaterial.LONG_GRASS.id) {
         return EventType.GRASS;
      } else if (blockId == DefaultMaterial.DEAD_BUSH.id) {
         return EventType.DEAD_BUSH;
      } else if (blockId != DefaultMaterial.RED_ROSE.id && blockId != DefaultMaterial.YELLOW_FLOWER.id) {
         if (blockId == DefaultMaterial.PUMPKIN.id) {
            return EventType.PUMPKIN;
         } else if (blockId != DefaultMaterial.BROWN_MUSHROOM.id && blockId != DefaultMaterial.RED_MUSHROOM.id) {
            if (blockId == DefaultMaterial.SUGAR_CANE_BLOCK.id) {
               return EventType.REED;
            } else if (blockId == DefaultMaterial.SAND.id) {
               return EventType.SAND;
            } else {
               return blockId == DefaultMaterial.CLAY.id ? EventType.CLAY : EventType.CUSTOM;
            }
         } else {
            return EventType.SHROOM;
         }
      } else {
         return EventType.FLOWERS;
      }
   }

   private PopulateChunkEvent.Populate.EventType getPopulateEventType(int blockId) {
      if (blockId == DefaultMaterial.WATER.id) {
         return net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.LAKE;
      } else {
         return blockId == DefaultMaterial.LAVA.id ? net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.LAVA : net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.CUSTOM;
      }
   }

   private OreGenEvent.GenerateMinable.EventType getOreEventType(int blockId) {
      if (blockId == DefaultMaterial.COAL_ORE.id) {
         return net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.COAL;
      } else if (blockId == DefaultMaterial.DIAMOND_ORE.id) {
         return net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.DIAMOND;
      } else if (blockId == DefaultMaterial.DIRT.id) {
         return net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.DIRT;
      } else if (blockId == DefaultMaterial.GOLD_ORE.id) {
         return net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.GOLD;
      } else if (blockId == DefaultMaterial.GRAVEL.id) {
         return net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.GRAVEL;
      } else if (blockId == DefaultMaterial.IRON_ORE.id) {
         return net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.IRON;
      } else if (blockId == DefaultMaterial.LAPIS_ORE.id) {
         return net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.LAPIS;
      } else {
         return blockId == DefaultMaterial.REDSTONE_ORE.id ? net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.REDSTONE : net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.CUSTOM;
      }
   }

   private boolean hasOreGenerationBegun(LocalWorld world) {
      return (Boolean)this.hasOreGenBegun.get(world.getName());
   }

   private boolean hasDecorationBegun(LocalWorld world) {
      return (Boolean)this.hasDecorationBegun.get(world.getName());
   }

   private void setOreGenerationBegun(LocalWorld world, boolean begun) {
      this.hasOreGenBegun.put(world.getName(), begun);
   }

   private void setDecorationBegun(LocalWorld world, boolean begun) {
      this.hasDecorationBegun.put(world.getName(), begun);
   }
}
