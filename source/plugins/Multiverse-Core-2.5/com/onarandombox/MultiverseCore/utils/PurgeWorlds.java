package com.onarandombox.MultiverseCore.utils;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Squid;

/** @deprecated */
@Deprecated
public class PurgeWorlds {
   private MultiverseCore plugin;

   public PurgeWorlds(MultiverseCore plugin) {
      super();
      this.plugin = plugin;
   }

   public void purgeWorlds(CommandSender sender, List worlds) {
      if (worlds != null && !worlds.isEmpty()) {
         for(MultiverseWorld world : worlds) {
            this.purgeWorld(sender, world);
         }

      }
   }

   public void purgeWorld(CommandSender sender, MultiverseWorld world) {
      if (world != null) {
         ArrayList<String> allMobs = new ArrayList(world.getAnimalList());
         allMobs.addAll(world.getMonsterList());
         this.purgeWorld(sender, world, allMobs, !world.canAnimalsSpawn(), !world.canMonstersSpawn());
      }
   }

   public void purgeWorld(CommandSender sender, MultiverseWorld mvworld, List thingsToKill, boolean negateAnimals, boolean negateMonsters) {
      if (mvworld != null) {
         World world = this.plugin.getServer().getWorld(mvworld.getName());
         if (world != null) {
            int entitiesKilled = 0;

            for(Entity e : world.getEntities()) {
               this.plugin.log(Level.FINEST, "Entity list (aval for purge) from WORLD < " + mvworld.getName() + " >: " + e.toString());
               if (this.killMonster(mvworld, e, thingsToKill, negateMonsters)) {
                  ++entitiesKilled;
               } else if (this.killCreature(mvworld, e, thingsToKill, negateAnimals)) {
                  ++entitiesKilled;
               }
            }

            if (sender != null) {
               sender.sendMessage(entitiesKilled + " entities purged from the world '" + world.getName() + "'");
            }

         }
      }
   }

   private boolean killCreature(MultiverseWorld mvworld, Entity e, List creaturesToKill, boolean negate) {
      String entityName = e.toString().replaceAll("Craft", "").toUpperCase();
      if (e instanceof Squid || e instanceof Animals) {
         if (!creaturesToKill.contains(entityName) && !creaturesToKill.contains("ALL") && !creaturesToKill.contains("ANIMALS")) {
            if (negate) {
               e.remove();
               return true;
            }
         } else if (!negate) {
            e.remove();
            return true;
         }
      }

      return false;
   }

   private boolean killMonster(MultiverseWorld mvworld, Entity e, List creaturesToKill, boolean negate) {
      String entityName = "";
      if (e instanceof EnderDragon) {
         entityName = "ENDERDRAGON";
      } else {
         entityName = e.toString().replaceAll("Craft", "").toUpperCase();
      }

      if (e instanceof Slime || e instanceof Monster || e instanceof Ghast || e instanceof EnderDragon) {
         this.plugin.log(Level.FINEST, "Looking at a monster: " + e);
         if (!creaturesToKill.contains(entityName) && !creaturesToKill.contains("ALL") && !creaturesToKill.contains("MONSTERS")) {
            if (negate) {
               this.plugin.log(Level.FINEST, "Removing a monster: " + e);
               e.remove();
               return true;
            }
         } else if (!negate) {
            this.plugin.log(Level.FINEST, "Removing a monster: " + e);
            e.remove();
            return true;
         }
      }

      return false;
   }
}
