package strength;

import cus.CustomMonster;
import cus.Point;
import java.util.HashMap;
import java.util.List;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class DataInfo {
   private HashMap data = new HashMap();

   public DataInfo(boolean show, int ai, CreatureSpawnEvent.SpawnReason spawnReason, CustomMonster.Camp camp, double range, double speed, int nowPath, HashMap path, String name, boolean showLevel, List levels, float recover, int potionsSelfChance, String potionSelf, int potionsEnemyChance, String potionEnemy, int damageChance, String damageSkill, int attackChance, String attackSkill, boolean shoot, int dropPowerMin, int dropPowerMax, int dropGoldMin, int dropGoldMax, int dropExpMin, int dropExpMax, String dropItems, boolean visible, boolean removeFaraway, int initHealth, int initMaxHealth, int equipWeaponChance, String equipWeapon, int equipHelmetChance, String equipHelmet, int equipChestplateChance, String equipChestplate, int equipLeggingsChance, String equipLeggings, int equipBootsChance, String equipBoots) {
      super();
      this.data.put("show", show);
      this.data.put("ai", ai);
      this.data.put("spawnReason", spawnReason);
      this.data.put("camp", camp);
      this.data.put("range", range);
      this.data.put("speed", speed);
      this.data.put("nowPath", nowPath);
      this.data.put("path", path);
      this.data.put("name", name);
      this.data.put("showLevel", showLevel);
      this.data.put("levels", levels);
      this.data.put("recover", recover);
      this.data.put("potionsSelfChance", potionsSelfChance);
      this.data.put("potionSelf", potionSelf);
      this.data.put("potionsEnemyChance", potionsEnemyChance);
      this.data.put("potionEnemy", potionEnemy);
      this.data.put("damageChance", damageChance);
      this.data.put("damageSkill", damageSkill);
      this.data.put("attackChance", attackChance);
      this.data.put("attackSkill", attackSkill);
      this.data.put("shoot", shoot);
      this.data.put("dropPowerMin", dropPowerMin);
      this.data.put("dropPowerMax", dropPowerMax);
      this.data.put("dropGoldMin", dropGoldMin);
      this.data.put("dropGoldMax", dropGoldMax);
      this.data.put("dropExpMin", dropExpMin);
      this.data.put("dropExpMax", dropExpMax);
      this.data.put("dropItems", dropItems);
      this.data.put("visible", visible);
      this.data.put("removeFaraway", removeFaraway);
      this.data.put("initHealth", initHealth);
      this.data.put("initMaxHealth", initMaxHealth);
      this.data.put("equipWeaponChance", equipWeaponChance);
      this.data.put("equipWeapon", equipWeapon);
      this.data.put("equipHelmetChance", equipHelmetChance);
      this.data.put("equipHelmet", equipHelmet);
      this.data.put("equipChestplateChance", equipChestplateChance);
      this.data.put("equipChestplate", equipChestplate);
      this.data.put("equipLeggingsChance", equipLeggingsChance);
      this.data.put("equipLeggings", equipLeggings);
      this.data.put("equipBootsChance", equipBootsChance);
      this.data.put("equipBoots", equipBoots);
   }

   public Object getData(String name) {
      return this.data.get(name);
   }

   public void setData(String name, Object obj) {
      this.data.put(name, obj);
   }

   public static class LevelInfo {
      int id;
      int min;
      int max;
      int chance;

      public LevelInfo(int id, int min, int max, int chance) {
         super();
         this.id = id;
         this.min = min;
         this.max = max;
         this.chance = chance;
      }

      public int getId() {
         return this.id;
      }

      public int getMin() {
         return this.min;
      }

      public int getMax() {
         return this.max;
      }

      public int getChance() {
         return this.chance;
      }
   }
}
