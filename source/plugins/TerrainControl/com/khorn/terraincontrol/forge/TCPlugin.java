package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.TerrainControlEngine;
import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.customobjects.BODefaultValues;
import com.khorn.terraincontrol.events.EventPriority;
import com.khorn.terraincontrol.util.StringHelper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import java.io.File;
import java.lang.reflect.Field;
import java.util.logging.Level;
import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;

@Mod(
   modid = "TerrainControl",
   name = "TerrainControl",
   version = "2.4.14"
)
@NetworkMod(
   clientSideRequired = false,
   serverSideRequired = false,
   versionBounds = "*"
)
public class TCPlugin implements TerrainControlEngine {
   @Instance("TerrainControl")
   public static TCPlugin instance;
   public File terrainControlDirectory;
   private TCWorldType worldType;

   public TCPlugin() {
      super();
   }

   @EventHandler
   public void load(FMLInitializationEvent event) {
      try {
         Field minecraftDir = Loader.class.getDeclaredField("minecraftDir");
         minecraftDir.setAccessible(true);
         this.terrainControlDirectory = new File((File)minecraftDir.get((Object)null), "mods" + File.separator + "TerrainControl");
      } catch (Throwable e) {
         this.terrainControlDirectory = new File("mods" + File.separator + "TerrainControl");
         System.out.println("Could not reflect the Minecraft directory, save location may be unpredicatble.");
         e.printStackTrace();
      }

      TerrainControl.supportedBlockIds = 4095;
      TerrainControl.startEngine(this);
      LanguageRegistry.instance().addStringLocalization("generator.TerrainControl", "TerrainControl");
      this.worldType = new TCWorldType(this, "TerrainControl");
      if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
         NetworkRegistry.instance().registerChannel(new PacketHandler(this), TCDefaultValues.ChannelName.stringValue());
      }

      GameRegistry.registerPlayerTracker(new PlayerTracker(this));
      SaplingListener saplingListener = new SaplingListener();
      MinecraftForge.TERRAIN_GEN_BUS.register(saplingListener);
      MinecraftForge.EVENT_BUS.register(saplingListener);
      TerrainControl.registerEventHandler(new EventManager(), EventPriority.CANCELABLE);
   }

   public LocalWorld getWorld(String name) {
      LocalWorld world = this.worldType.worldTC;
      if (world == null) {
         return null;
      } else {
         return world.getName().equals(name) ? world : null;
      }
   }

   public LocalWorld getWorld() {
      return this.worldType.worldTC;
   }

   public void log(Level level, String... messages) {
      FMLCommonHandler.instance().getFMLLogger().log(level, "TerrainControl: " + StringHelper.join((Object[])messages, ","));
   }

   public File getGlobalObjectsDirectory() {
      return new File(this.terrainControlDirectory, BODefaultValues.BO_GlobalDirectoryName.stringValue());
   }

   public boolean isValidBlockId(int id) {
      if (id == 0) {
         return true;
      } else if (id >= 0 && id <= TerrainControl.supportedBlockIds) {
         return Block.field_71973_m[id] != null;
      } else {
         return false;
      }
   }
}
