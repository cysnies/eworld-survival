package com.earth2me.essentials;

import com.earth2me.essentials.utils.LocationUtil;
import com.earth2me.essentials.utils.StringUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class SpawnMob {
   public SpawnMob() {
      super();
   }

   public static String mobList(User user) {
      Set<String> mobList = Mob.getMobList();
      Set<String> availableList = new HashSet();

      for(String mob : mobList) {
         if (user.isAuthorized("essentials.spawnmob." + mob.toLowerCase(Locale.ENGLISH))) {
            availableList.add(mob);
         }
      }

      if (availableList.isEmpty()) {
         availableList.add(I18n._("none"));
      }

      return StringUtil.joinList(availableList);
   }

   public static List mobParts(String mobString) {
      String[] mobParts = mobString.split(",");
      List<String> mobs = new ArrayList();

      for(String mobPart : mobParts) {
         String[] mobDatas = mobPart.split(":");
         mobs.add(mobDatas[0]);
      }

      return mobs;
   }

   public static List mobData(String mobString) {
      String[] mobParts = mobString.split(",");
      List<String> mobData = new ArrayList();

      for(String mobPart : mobParts) {
         String[] mobDatas = mobPart.split(":");
         if (mobDatas.length == 1) {
            if (mobPart.contains(":")) {
               mobData.add("");
            } else {
               mobData.add((Object)null);
            }
         } else {
            mobData.add(mobDatas[1]);
         }
      }

      return mobData;
   }

   public static void spawnmob(net.ess3.api.IEssentials ess, Server server, User user, List parts, List data, int mobCount) throws Exception {
      Block block = LocationUtil.getTarget(user.getBase()).getBlock();
      if (block == null) {
         throw new Exception(I18n._("unableToSpawnMob"));
      } else {
         spawnmob(ess, server, user.getBase(), user, block.getLocation(), parts, data, mobCount);
      }
   }

   public static void spawnmob(net.ess3.api.IEssentials ess, Server server, CommandSender sender, User target, List parts, List data, int mobCount) throws Exception {
      spawnmob(ess, server, sender, target, target.getLocation(), parts, data, mobCount);
   }

   public static void spawnmob(net.ess3.api.IEssentials ess, Server server, CommandSender sender, User target, Location loc, List parts, List data, int mobCount) throws Exception {
      Location sloc = LocationUtil.getSafeDestination(loc);

      for(int i = 0; i < parts.size(); ++i) {
         Mob mob = Mob.fromName((String)parts.get(i));
         checkSpawnable(ess, sender, mob);
      }

      int serverLimit = ess.getSettings().getSpawnMobLimit();
      int effectiveLimit = serverLimit / parts.size();
      if (effectiveLimit < 1) {
         effectiveLimit = 1;

         while(parts.size() > serverLimit) {
            parts.remove(serverLimit);
         }
      }

      if (mobCount > effectiveLimit) {
         mobCount = effectiveLimit;
         sender.sendMessage(I18n._("mobSpawnLimit"));
      }

      Mob mob = Mob.fromName((String)parts.get(0));

      try {
         for(int i = 0; i < mobCount; ++i) {
            spawnMob(ess, server, sender, target, sloc, parts, data);
         }

         sender.sendMessage(mobCount * parts.size() + " " + mob.name.toLowerCase(Locale.ENGLISH) + mob.suffix + " " + I18n._("spawned"));
      } catch (Mob.MobException e1) {
         throw new Exception(I18n._("unableToSpawnMob"), e1);
      } catch (NumberFormatException e2) {
         throw new Exception(I18n._("numberRequired"), e2);
      } catch (NullPointerException np) {
         throw new Exception(I18n._("soloMob"), np);
      }
   }

   private static void spawnMob(net.ess3.api.IEssentials ess, Server server, CommandSender sender, User target, Location sloc, List parts, List data) throws Exception {
      Entity spawnedMob = null;

      for(int i = 0; i < parts.size(); ++i) {
         if (i == 0) {
            Mob mob = Mob.fromName((String)parts.get(i));
            spawnedMob = mob.spawn(sloc.getWorld(), server, sloc);
            defaultMobData(mob.getType(), spawnedMob);
            if (data.get(i) != null) {
               changeMobData(sender, mob.getType(), spawnedMob, ((String)data.get(i)).toLowerCase(Locale.ENGLISH), target);
            }
         }

         int next = i + 1;
         if (next < parts.size()) {
            Mob mMob = Mob.fromName((String)parts.get(next));
            Entity spawnedMount = mMob.spawn(sloc.getWorld(), server, sloc);
            defaultMobData(mMob.getType(), spawnedMount);
            if (data.get(next) != null) {
               changeMobData(sender, mMob.getType(), spawnedMount, ((String)data.get(next)).toLowerCase(Locale.ENGLISH), target);
            }

            spawnedMob.setPassenger(spawnedMount);
            spawnedMob = spawnedMount;
         }
      }

   }

   private static void checkSpawnable(net.ess3.api.IEssentials ess, CommandSender sender, Mob mob) throws Exception {
      if (mob == null) {
         throw new Exception(I18n._("invalidMob"));
      } else if (ess.getSettings().getProtectPreventSpawn(mob.getType().toString().toLowerCase(Locale.ENGLISH))) {
         throw new Exception(I18n._("disabledToSpawnMob"));
      } else if (sender instanceof Player && !ess.getUser(sender).isAuthorized("essentials.spawnmob." + mob.name.toLowerCase(Locale.ENGLISH))) {
         throw new Exception(I18n._("noPermToSpawnMob"));
      }
   }

   private static void changeMobData(CommandSender sender, EntityType type, Entity spawned, String inputData, User target) throws Exception {
      String data = inputData;
      if (inputData.equals("")) {
         sender.sendMessage(I18n._("mobDataList", StringUtil.joinList(MobData.getValidHelp(spawned))));
      }

      for(MobData newData = MobData.fromData(spawned, inputData); newData != null; newData = MobData.fromData(spawned, data)) {
         newData.setData(spawned, target.getBase(), data);
         data = data.replace(newData.getMatched(), "");
      }

      if ((spawned instanceof Zombie || type == EntityType.SKELETON) && (inputData.contains("armor") || inputData.contains("armour"))) {
         EntityEquipment invent = ((LivingEntity)spawned).getEquipment();
         if (inputData.contains("diamond")) {
            invent.setBoots(new ItemStack(Material.DIAMOND_BOOTS, 1));
            invent.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS, 1));
            invent.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE, 1));
            invent.setHelmet(new ItemStack(Material.DIAMOND_HELMET, 1));
         } else if (inputData.contains("gold")) {
            invent.setBoots(new ItemStack(Material.GOLD_BOOTS, 1));
            invent.setLeggings(new ItemStack(Material.GOLD_LEGGINGS, 1));
            invent.setChestplate(new ItemStack(Material.GOLD_CHESTPLATE, 1));
            invent.setHelmet(new ItemStack(Material.GOLD_HELMET, 1));
         } else if (inputData.contains("leather")) {
            invent.setBoots(new ItemStack(Material.LEATHER_BOOTS, 1));
            invent.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS, 1));
            invent.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE, 1));
            invent.setHelmet(new ItemStack(Material.LEATHER_HELMET, 1));
         } else if (inputData.contains("no")) {
            invent.clear();
         } else {
            invent.setBoots(new ItemStack(Material.IRON_BOOTS, 1));
            invent.setLeggings(new ItemStack(Material.IRON_LEGGINGS, 1));
            invent.setChestplate(new ItemStack(Material.IRON_CHESTPLATE, 1));
            invent.setHelmet(new ItemStack(Material.IRON_HELMET, 1));
         }

         invent.setBootsDropChance(0.0F);
         invent.setLeggingsDropChance(0.0F);
         invent.setChestplateDropChance(0.0F);
         invent.setHelmetDropChance(0.0F);
      }

   }

   private static void defaultMobData(EntityType type, Entity spawned) {
      if (type == EntityType.SKELETON) {
         EntityEquipment invent = ((LivingEntity)spawned).getEquipment();
         invent.setItemInHand(new ItemStack(Material.BOW, 1));
         invent.setItemInHandDropChance(0.1F);
         invent.setBoots(new ItemStack(Material.GOLD_BOOTS, 1));
         invent.setBootsDropChance(0.0F);
      }

      if (type == EntityType.PIG_ZOMBIE) {
         EntityEquipment invent = ((LivingEntity)spawned).getEquipment();
         invent.setItemInHand(new ItemStack(Material.GOLD_SWORD, 1));
         invent.setItemInHandDropChance(0.1F);
         invent.setBoots(new ItemStack(Material.GOLD_BOOTS, 1));
         invent.setBootsDropChance(0.0F);
      }

      if (type == EntityType.ZOMBIE) {
         EntityEquipment invent = ((LivingEntity)spawned).getEquipment();
         invent.setBoots(new ItemStack(Material.GOLD_BOOTS, 1));
         invent.setBootsDropChance(0.0F);
      }

      if (type == EntityType.HORSE) {
         ((Horse)spawned).setJumpStrength(1.2);
      }

   }
}
