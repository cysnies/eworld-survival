package com.khorn.terraincontrol.bukkit;

import net.minecraft.server.v1_6_R2.WorldProvider;
import net.minecraft.server.v1_6_R2.WorldProviderNormal;

public class TCWorldProvider extends WorldProviderNormal {
   protected BukkitWorld localWorld;
   private final WorldProvider oldWorldProvider;

   public TCWorldProvider(BukkitWorld localWorld, WorldProvider oldWorldProvider) {
      super();
      this.localWorld = localWorld;
      this.oldWorldProvider = oldWorldProvider;
      this.a(localWorld.getWorld());
      this.f = oldWorldProvider.f;
      this.g = oldWorldProvider.g;
   }

   public int getSeaLevel() {
      return this.localWorld.getSettings().waterLevelMax;
   }

   public String getName() {
      return "Overworld";
   }

   public WorldProvider getOldWorldProvider() {
      return this.oldWorldProvider;
   }
}
