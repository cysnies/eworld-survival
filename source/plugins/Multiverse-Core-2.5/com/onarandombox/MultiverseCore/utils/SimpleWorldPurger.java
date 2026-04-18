package com.onarandombox.MultiverseCore.utils;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.api.WorldPurger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Golem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Squid;

public class SimpleWorldPurger implements WorldPurger {
   private MultiverseCore plugin;

   public SimpleWorldPurger(MultiverseCore plugin) {
      super();
      this.plugin = plugin;
   }

   public void purgeWorlds(List worlds) {
      if (worlds != null && !worlds.isEmpty()) {
         for(MultiverseWorld world : worlds) {
            this.purgeWorld(world);
         }

      }
   }

   public void purgeWorld(MultiverseWorld world) {
      if (world != null) {
         ArrayList<String> allMobs = new ArrayList(world.getAnimalList());
         allMobs.addAll(world.getMonsterList());
         this.purgeWorld(world, allMobs, !world.canAnimalsSpawn(), !world.canMonstersSpawn());
      }
   }

   public boolean shouldWeKillThisCreature(MultiverseWorld world, Entity e) {
      ArrayList<String> allMobs = new ArrayList(world.getAnimalList());
      allMobs.addAll(world.getMonsterList());
      return this.shouldWeKillThisCreature(e, allMobs, !world.canAnimalsSpawn(), !world.canMonstersSpawn());
   }

   public void purgeWorld(MultiverseWorld mvworld, List thingsToKill, boolean negateAnimals, boolean negateMonsters, CommandSender sender) {
      if (mvworld != null) {
         World world = mvworld.getCBWorld();
         if (world != null) {
            int projectilesKilled = 0;
            int entitiesKilled = 0;
            boolean specifiedAll = thingsToKill.contains("ALL");
            boolean specifiedAnimals = thingsToKill.contains("ANIMALS") || specifiedAll;
            boolean specifiedMonsters = thingsToKill.contains("MONSTERS") || specifiedAll;
            List<Entity> worldEntities = world.getEntities();
            List<LivingEntity> livingEntities = new ArrayList(worldEntities.size());
            List<Projectile> projectiles = new ArrayList(worldEntities.size());

            for(Entity e : worldEntities) {
               if (e instanceof Projectile) {
                  Projectile p = (Projectile)e;
                  if (p.getShooter() != null) {
                     projectiles.add((Projectile)e);
                  }
               } else if (e instanceof LivingEntity) {
                  livingEntities.add((LivingEntity)e);
               }
            }

            for(LivingEntity e : livingEntities) {
               if (this.killDecision(e, thingsToKill, negateAnimals, negateMonsters, specifiedAnimals, specifiedMonsters)) {
                  Iterator<Projectile> it = projectiles.iterator();

                  while(it.hasNext()) {
                     Projectile p = (Projectile)it.next();
                     if (p.getShooter().equals(e)) {
                        p.remove();
                        it.remove();
                        ++projectilesKilled;
                     }
                  }

                  e.remove();
                  ++entitiesKilled;
               }
            }

            if (sender != null) {
               sender.sendMessage(entitiesKilled + " entities purged from the world '" + world.getName() + "' along with " + projectilesKilled + " projectiles that belonged to them.");
            }

         }
      }
   }

   private boolean killDecision(Entity e, List thingsToKill, boolean negateAnimals, boolean negateMonsters, boolean specifiedAnimals, boolean specifiedMonsters) {
      boolean negate = false;
      boolean specified = false;
      if (!(e instanceof Golem) && !(e instanceof Squid) && !(e instanceof Animals)) {
         if (e instanceof Monster || e instanceof Ghast || e instanceof Slime) {
            if (specifiedMonsters && !negateMonsters) {
               CoreLogging.finest("Removing an entity because I was told to remove all monsters in world %s: %s", e.getWorld().getName(), e);
               return true;
            }

            if (specifiedMonsters) {
               specified = true;
            }

            negate = negateMonsters;
         }
      } else {
         if (specifiedAnimals && !negateAnimals) {
            CoreLogging.finest("Removing an entity because I was told to remove all animals in world %s: %s", e.getWorld().getName(), e);
            return true;
         }

         if (specifiedAnimals) {
            specified = true;
         }

         negate = negateAnimals;
      }

      for(String s : thingsToKill) {
         EntityType type = EntityType.fromName(s);
         if (type != null && type.equals(e.getType())) {
            specified = true;
            if (!negate) {
               CoreLogging.finest("Removing an entity because it WAS specified and we are NOT negating in world %s: %s", e.getWorld().getName(), e);
               return true;
            }
            break;
         }
      }

      if (!specified && negate) {
         CoreLogging.finest("Removing an entity because it was NOT specified and we ARE negating in world %s: %s", e.getWorld().getName(), e);
         return true;
      } else {
         return false;
      }
   }

   public boolean shouldWeKillThisCreature(Entity e, List thingsToKill, boolean negateAnimals, boolean negateMonsters) {
      boolean specifiedAll = thingsToKill.contains("ALL");
      boolean specifiedAnimals = thingsToKill.contains("ANIMALS") || specifiedAll;
      boolean specifiedMonsters = thingsToKill.contains("MONSTERS") || specifiedAll;
      return this.killDecision(e, thingsToKill, negateAnimals, negateMonsters, specifiedAnimals, specifiedMonsters);
   }

   public void purgeWorld(MultiverseWorld mvworld, List thingsToKill, boolean negateAnimals, boolean negateMonsters) {
      this.purgeWorld(mvworld, thingsToKill, negateAnimals, negateMonsters, (CommandSender)null);
   }
}
