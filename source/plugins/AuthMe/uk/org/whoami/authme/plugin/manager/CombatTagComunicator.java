package uk.org.whoami.authme.plugin.manager;

import com.trc202.CombatTag.CombatTag;
import com.trc202.CombatTagApi.CombatTagApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public abstract class CombatTagComunicator {
   static CombatTagApi combatApi;

   public CombatTagComunicator() {
      super();
      if (Bukkit.getServer().getPluginManager().getPlugin("CombatTag") != null) {
         combatApi = new CombatTagApi((CombatTag)Bukkit.getServer().getPluginManager().getPlugin("CombatTag"));
      }

   }

   public abstract boolean isInCombat(String var1);

   public abstract boolean isInCombat(Player var1);

   public abstract long getRemainingTagTime(String var1);

   public static boolean isNPC(Entity player) {
      try {
         if (Bukkit.getServer().getPluginManager().getPlugin("CombatTag") != null) {
            combatApi = new CombatTagApi((CombatTag)Bukkit.getServer().getPluginManager().getPlugin("CombatTag"));
            return combatApi.isNPC(player);
         } else {
            return false;
         }
      } catch (ClassCastException var2) {
         return false;
      } catch (NullPointerException var3) {
         return false;
      } catch (NoClassDefFoundError var4) {
         return false;
      }
   }
}
