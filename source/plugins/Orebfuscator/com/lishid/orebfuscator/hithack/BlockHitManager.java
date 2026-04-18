package com.lishid.orebfuscator.hithack;

import com.lishid.orebfuscator.OrebfuscatorConfig;
import java.util.HashMap;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BlockHitManager {
   private static HashMap playersBlockTrackingStatus = new HashMap();

   public BlockHitManager() {
      super();
   }

   public static boolean hitBlock(Player player, Block block) {
      if (player.getGameMode() == GameMode.CREATIVE) {
         return true;
      } else {
         PlayerBlockTracking playerBlockTracking = getPlayerBlockTracking(player);
         if (playerBlockTracking.isBlock(block)) {
            return true;
         } else {
            long time = playerBlockTracking.getTimeDifference();
            playerBlockTracking.incrementHackingIndicator();
            playerBlockTracking.setBlock(block);
            playerBlockTracking.updateTime();
            int decrement = (int)(time / (long)OrebfuscatorConfig.AntiHitHackDecrementFactor);
            playerBlockTracking.decrementHackingIndicator(decrement);
            if (playerBlockTracking.getHackingIndicator() == OrebfuscatorConfig.AntiHitHackMaxViolation) {
               playerBlockTracking.incrementHackingIndicator(OrebfuscatorConfig.AntiHitHackMaxViolation);
            }

            return playerBlockTracking.getHackingIndicator() <= OrebfuscatorConfig.AntiHitHackMaxViolation;
         }
      }
   }

   public static boolean canFakeHit(Player player) {
      PlayerBlockTracking playerBlockTracking = getPlayerBlockTracking(player);
      return playerBlockTracking.getHackingIndicator() <= OrebfuscatorConfig.AntiHitHackMaxViolation;
   }

   public static boolean fakeHit(Player player) {
      PlayerBlockTracking playerBlockTracking = getPlayerBlockTracking(player);
      playerBlockTracking.incrementHackingIndicator();
      return playerBlockTracking.getHackingIndicator() <= OrebfuscatorConfig.AntiHitHackMaxViolation;
   }

   public static void breakBlock(Player player, Block block) {
      if (player.getGameMode() != GameMode.CREATIVE) {
         PlayerBlockTracking playerBlockTracking = getPlayerBlockTracking(player);
         if (playerBlockTracking.isBlock(block)) {
            playerBlockTracking.decrementHackingIndicator(2);
         }

      }
   }

   private static PlayerBlockTracking getPlayerBlockTracking(Player player) {
      if (!playersBlockTrackingStatus.containsKey(player)) {
         playersBlockTrackingStatus.put(player, new PlayerBlockTracking(player));
      }

      return (PlayerBlockTracking)playersBlockTrackingStatus.get(player);
   }

   public static void clearHistory(Player player) {
      playersBlockTrackingStatus.remove(player);
   }

   public static void clearAll() {
      playersBlockTrackingStatus.clear();
   }
}
