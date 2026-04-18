package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.generator.resourcegens.SaplingGen;
import com.khorn.terraincontrol.generator.resourcegens.SaplingType;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.TreeType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldInitEvent;

public class TCListener implements Listener {
   private TCPlugin tcPlugin;
   private TCSender tcSender;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$org$bukkit$TreeType;

   public TCListener(TCPlugin plugin) {
      super();
      this.tcPlugin = plugin;
      this.tcSender = new TCSender(plugin);
      Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onWorldInit(WorldInitEvent event) {
      this.tcPlugin.onWorldInit(event.getWorld());
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onStructureGrow(StructureGrowEvent event) {
      BukkitWorld bukkitWorld = (BukkitWorld)this.tcPlugin.worlds.get(event.getWorld().getUID());
      if (bukkitWorld != null) {
         int x = event.getLocation().getBlockX();
         int y = event.getLocation().getBlockY();
         int z = event.getLocation().getBlockZ();
         int biomeId = bukkitWorld.getCalculatedBiomeId(x, z);
         if (bukkitWorld.getSettings().biomeConfigs[biomeId] != null) {
            BiomeConfig biomeConfig = bukkitWorld.getSettings().biomeConfigs[biomeId];
            SaplingGen sapling;
            switch (event.getSpecies()) {
               case TREE:
               case BIG_TREE:
                  sapling = biomeConfig.getSaplingGen(SaplingType.Oak);
                  break;
               case REDWOOD:
               case TALL_REDWOOD:
                  sapling = biomeConfig.getSaplingGen(SaplingType.Redwood);
                  break;
               case BIRCH:
                  sapling = biomeConfig.getSaplingGen(SaplingType.Birch);
                  break;
               case JUNGLE:
                  sapling = biomeConfig.getSaplingGen(SaplingType.BigJungle);
                  break;
               case SMALL_JUNGLE:
                  sapling = biomeConfig.getSaplingGen(SaplingType.SmallJungle);
                  break;
               case JUNGLE_BUSH:
               default:
                  sapling = null;
                  break;
               case RED_MUSHROOM:
                  sapling = biomeConfig.getSaplingGen(SaplingType.RedMushroom);
                  break;
               case BROWN_MUSHROOM:
                  sapling = biomeConfig.getSaplingGen(SaplingType.BrownMushroom);
            }

            if (sapling != null) {
               boolean success = false;

               for(int i = 0; i < 10; ++i) {
                  if (sapling.growSapling(bukkitWorld, new Random(), x, y, z)) {
                     success = true;
                     break;
                  }
               }

               if (success) {
                  event.getBlocks().clear();
               } else {
                  event.setCancelled(true);
               }
            }

         }
      }
   }

   @EventHandler
   public void onPlayerRegisterChannel(PlayerRegisterChannelEvent event) {
      if (event.getChannel().equals(TCDefaultValues.ChannelName.stringValue())) {
         this.tcSender.send(event.getPlayer());
      }

   }

   @EventHandler
   public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
      Player player = event.getPlayer();
      if (player.getListeningPluginChannels().contains(TCDefaultValues.ChannelName.stringValue())) {
         this.tcSender.send(player);
      }

   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$org$bukkit$TreeType() {
      int[] var10000 = $SWITCH_TABLE$org$bukkit$TreeType;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[TreeType.values().length];

         try {
            var0[TreeType.BIG_TREE.ordinal()] = 2;
         } catch (NoSuchFieldError var11) {
         }

         try {
            var0[TreeType.BIRCH.ordinal()] = 5;
         } catch (NoSuchFieldError var10) {
         }

         try {
            var0[TreeType.BROWN_MUSHROOM.ordinal()] = 10;
         } catch (NoSuchFieldError var9) {
         }

         try {
            var0[TreeType.JUNGLE.ordinal()] = 6;
         } catch (NoSuchFieldError var8) {
         }

         try {
            var0[TreeType.JUNGLE_BUSH.ordinal()] = 8;
         } catch (NoSuchFieldError var7) {
         }

         try {
            var0[TreeType.REDWOOD.ordinal()] = 3;
         } catch (NoSuchFieldError var6) {
         }

         try {
            var0[TreeType.RED_MUSHROOM.ordinal()] = 9;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[TreeType.SMALL_JUNGLE.ordinal()] = 7;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[TreeType.SWAMP.ordinal()] = 11;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[TreeType.TALL_REDWOOD.ordinal()] = 4;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[TreeType.TREE.ordinal()] = 1;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$org$bukkit$TreeType = var0;
         return var0;
      }
   }
}
