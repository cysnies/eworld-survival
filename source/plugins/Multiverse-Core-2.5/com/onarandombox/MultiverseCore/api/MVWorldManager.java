package com.onarandombox.MultiverseCore.api;

import com.onarandombox.MultiverseCore.utils.PurgeWorlds;
import java.io.File;
import java.util.Collection;
import java.util.List;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;

public interface MVWorldManager {
   boolean addWorld(String var1, World.Environment var2, String var3, WorldType var4, Boolean var5, String var6);

   boolean addWorld(String var1, World.Environment var2, String var3, WorldType var4, Boolean var5, String var6, boolean var7);

   boolean cloneWorld(String var1, String var2, String var3);

   boolean deleteWorld(String var1);

   boolean deleteWorld(String var1, boolean var2);

   boolean deleteWorld(String var1, boolean var2, boolean var3);

   boolean unloadWorld(String var1);

   boolean loadWorld(String var1);

   void removePlayersFromWorld(String var1);

   ChunkGenerator getChunkGenerator(String var1, String var2, String var3);

   Collection getMVWorlds();

   MultiverseWorld getMVWorld(String var1);

   MultiverseWorld getMVWorld(World var1);

   boolean isMVWorld(String var1);

   boolean isMVWorld(World var1);

   void loadWorlds(boolean var1);

   void loadDefaultWorlds();

   /** @deprecated */
   @Deprecated
   PurgeWorlds getWorldPurger();

   WorldPurger getTheWorldPurger();

   MultiverseWorld getSpawnWorld();

   List getUnloadedWorlds();

   void getDefaultWorldGenerators();

   FileConfiguration loadWorldConfig(File var1);

   boolean saveWorldsConfig();

   boolean removeWorldFromConfig(String var1);

   void setFirstSpawnWorld(String var1);

   MultiverseWorld getFirstSpawnWorld();

   boolean regenWorld(String var1, boolean var2, boolean var3, String var4);
}
