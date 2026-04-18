package cus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lib.util.Util;
import lib.util.UtilItems;
import net.minecraft.server.v1_6_R2.AttributeInstance;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.EntitySpider;
import net.minecraft.server.v1_6_R2.GenericAttributes;
import net.minecraft.server.v1_6_R2.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

public class CustomEntitySpider extends EntitySpider implements CustomMonster {
   private EntityLiving el = this;
   private String name;
   private boolean showLevel;
   private HashMap levelHash;
   private float recover;
   private int nowPath;
   private HashMap path;
   private int potionsSelfChance;
   private int potionsEnemyChance;
   private String potionSelf;
   private String potionEnemy;
   private int damageChance;
   private String damageSkill;
   private int attackChance;
   private String attackSkill;
   private int ai = 2;
   private CreatureSpawnEvent.SpawnReason spawnReason;
   private CustomMonster.Camp camp;
   private double range = (double)-1.0F;
   private double speed = (double)-1.0F;
   private int dropPower;
   private int dropGold;
   private int dropExp;
   private List dropItems;
   private boolean shoot;
   private YamlConfiguration config = new YamlConfiguration();

   public CustomEntitySpider(World world) {
      super(world);
      CustomEntityUtil.clearTarget(this.goalSelector, this.targetSelector);
      if (Cus.getPlugin() == null) {
         Util.sendConsoleMessage("未准备好~");
      } else {
         CustomEntityUtil.loadDelay(this);
      }

   }

   public CreatureSpawnEvent.SpawnReason getSpawnReason() {
      return this.spawnReason;
   }

   public void setSpawnReason(CreatureSpawnEvent.SpawnReason spawnReason) {
      this.spawnReason = spawnReason;
      this.config.set("spawnReason", spawnReason.name());
   }

   public double getRange() {
      return this.range;
   }

   public void setRange(double range) {
      this.range = range;
      AttributeInstance ai = this.getAttributeInstance(GenericAttributes.b);
      if (ai != null) {
         ai.setValue(range);
      }

      this.config.set("range", range);
   }

   public double getSpeed() {
      return this.speed;
   }

   public void setSpeed(double speed) {
      this.speed = speed;
      AttributeInstance ai = this.getAttributeInstance(GenericAttributes.d);
      if (ai != null) {
         ai.setValue(speed);
      }

      this.config.set("speed", speed);
   }

   public CustomMonster.Camp getCamp() {
      return this.camp;
   }

   public void setCamp(CustomMonster.Camp camp) {
      this.camp = camp;
      this.config.set("camp", camp.name());
   }

   public int getAi() {
      return this.ai;
   }

   public void setAi(int ai) {
      this.ai = ai;
      this.config.set("ai", ai);
      CustomEntityUtil.clearTarget(this.goalSelector, this.targetSelector);
      switch (ai) {
         case 0:
         case 1:
         case 2:
         default:
      }
   }

   public YamlConfiguration getConfig() {
      return this.config;
   }

   public int getDropPower() {
      return this.dropPower;
   }

   public void setDropPower(int dropPower) {
      this.dropPower = dropPower;
      this.config.set("drop.power", dropPower);
   }

   public EntityLiving getEl() {
      return this.el;
   }

   public int getDropGold() {
      return this.dropGold;
   }

   public void setDropGold(int dropGold) {
      this.dropGold = dropGold;
      this.config.set("drop.gold", dropGold);
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
      this.config.set("name", name);
      CustomEntityUtil.updateName(this);
   }

   public boolean isShowLevel() {
      return this.showLevel;
   }

   public void setShowLevel(boolean showLevel) {
      this.showLevel = showLevel;
      this.config.set("showLevel", showLevel);
      CustomEntityUtil.updateName(this);
   }

   public HashMap getLevelHash() {
      return this.levelHash;
   }

   public void setLevelHash(HashMap levelHash, boolean updateLevel) {
      this.levelHash = levelHash;
      List<String> levels = new ArrayList();

      for(int id : levelHash.keySet()) {
         int level = (Integer)levelHash.get(id);
         levels.add(id + " " + level);
      }

      this.config.set("levels", levels);
      CustomEntityUtil.updateName(this);
      if (updateLevel) {
         Cus.getLevelManager().updateLevels(this);
      }

   }

   public List getDropItems() {
      return this.dropItems;
   }

   public void setDropItems(List dropItems) {
      this.dropItems = dropItems;
      if (dropItems == null) {
         this.config.set("drop.items", (Object)null);
      } else {
         this.config.set("drop.items", (Object)null);
         int index = 1;

         for(ItemStack is : dropItems) {
            this.config.set("drop.items.item" + index, UtilItems.saveItem(is));
            ++index;
         }
      }

   }

   public boolean isShoot() {
      return this.shoot;
   }

   public void setShoot(boolean shoot) {
      this.shoot = shoot;
      this.config.set("shoot", shoot);
   }

   public float getRecover() {
      return this.recover;
   }

   public void setRecover(float recover) {
      this.recover = recover;
      this.config.set("recover", recover);
   }

   public HashMap getPath() {
      return this.path;
   }

   public void setPath(HashMap path) {
      this.path = path;
      if (path == null) {
         this.config.set("path", path);
      } else {
         List<String> list = new ArrayList();

         for(int index = 1; index <= path.size(); ++index) {
            Point p = (Point)path.get(index);
            double x = Util.getDouble(p.x, 2);
            double y = Util.getDouble(p.y, 2);
            double z = Util.getDouble(p.z, 2);
            float yaw = (float)Util.getDouble((double)p.yaw, 2);
            float pitch = (float)Util.getDouble((double)p.pitch, 2);
            list.add(x + " " + y + " " + z + " " + yaw + " " + pitch);
         }

         this.config.set("path", list);
      }

   }

   public int getNowPath() {
      return this.nowPath;
   }

   public void setNowPath(int nowPath) {
      this.nowPath = nowPath;
      this.config.set("nowPath", nowPath);
   }

   public int getDropExp() {
      return this.dropExp;
   }

   public void setDropExp(int dropExp) {
      this.dropExp = dropExp;
      this.config.set("drop.exp", dropExp);
   }

   public int getPotionsSelfChance() {
      return this.potionsSelfChance;
   }

   public void setPotionsSelfChance(int potionsSelfChance) {
      this.potionsSelfChance = potionsSelfChance;
      this.config.set("potions.selfChance", potionsSelfChance);
   }

   public int getPotionsEnemyChance() {
      return this.potionsEnemyChance;
   }

   public void setPotionsEnemyChance(int potionsEnemyChance) {
      this.potionsEnemyChance = potionsEnemyChance;
      this.config.set("potions.enemyChance", potionsEnemyChance);
   }

   public String getPotionSelf() {
      return this.potionSelf;
   }

   public void setPotionSelf(String potionSelf) {
      this.potionSelf = potionSelf;
      this.config.set("potions.self", potionSelf);
   }

   public String getPotionEnemy() {
      return this.potionEnemy;
   }

   public void setPotionEnemy(String potionEnemy) {
      this.potionEnemy = potionEnemy;
      this.config.set("potions.enemy", potionEnemy);
   }

   public int getDamageChance() {
      return this.damageChance;
   }

   public void setDamageChance(int damageChance) {
      this.damageChance = damageChance;
      this.config.set("skills.damageChance", damageChance);
   }

   public String getDamageSkill() {
      return this.damageSkill;
   }

   public void setDamageSkill(String damageSkill) {
      this.damageSkill = damageSkill;
      this.config.set("skills.damageSkill", damageSkill);
   }

   public int getAttackChance() {
      return this.attackChance;
   }

   public void setAttackChance(int attackChance) {
      this.attackChance = attackChance;
      this.config.set("skills.attackChance", attackChance);
   }

   public String getAttackSkill() {
      return this.attackSkill;
   }

   public void setAttackSkill(String attackSkill) {
      this.attackSkill = attackSkill;
      this.config.set("skills.attackSkill", attackSkill);
   }
}
