package cus;

import java.util.HashMap;
import java.util.List;
import net.minecraft.server.v1_6_R2.EntityLiving;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.entity.CreatureSpawnEvent;

public interface CustomMonster {
   EntityLiving getEl();

   YamlConfiguration getConfig();

   CreatureSpawnEvent.SpawnReason getSpawnReason();

   void setSpawnReason(CreatureSpawnEvent.SpawnReason var1);

   double getRange();

   void setRange(double var1);

   double getSpeed();

   void setSpeed(double var1);

   Camp getCamp();

   void setCamp(Camp var1);

   int getAi();

   void setAi(int var1);

   int getDropPower();

   void setDropPower(int var1);

   int getDropGold();

   void setDropGold(int var1);

   String getName();

   void setName(String var1);

   boolean isShowLevel();

   void setShowLevel(boolean var1);

   HashMap getLevelHash();

   void setLevelHash(HashMap var1, boolean var2);

   List getDropItems();

   void setDropItems(List var1);

   boolean isShoot();

   void setShoot(boolean var1);

   float getRecover();

   void setRecover(float var1);

   HashMap getPath();

   void setPath(HashMap var1);

   int getNowPath();

   void setNowPath(int var1);

   int getDropExp();

   void setDropExp(int var1);

   int getPotionsSelfChance();

   void setPotionsSelfChance(int var1);

   int getPotionsEnemyChance();

   void setPotionsEnemyChance(int var1);

   String getPotionSelf();

   void setPotionSelf(String var1);

   String getPotionEnemy();

   void setPotionEnemy(String var1);

   int getDamageChance();

   void setDamageChance(int var1);

   String getDamageSkill();

   void setDamageSkill(String var1);

   int getAttackChance();

   void setAttackChance(int var1);

   String getAttackSkill();

   void setAttackSkill(String var1);

   public static enum Camp {
      good,
      bad,
      none;

      private Camp() {
      }
   }
}
