package cus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.Util;
import lib.util.UtilConfig;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import strength.DataInfo;

public class LevelManager implements Listener {
   private Random r = new Random();
   private String pn;
   private String levelConfigPath;
   private HashMap typeHash;
   private HashMap levelHash;

   public LevelManager(Cus cus) {
      super();
      this.pn = cus.getPn();
      this.levelConfigPath = cus.getPluginPath() + File.separator + this.pn + File.separator + "levels.yml";
      this.loadConfig(UtilConfig.getConfig(this.pn));
      cus.getPm().registerEvents(this, cus);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public HashMap getLevelHash() {
      return this.levelHash;
   }

   public void updateLevels(CustomMonster cm) {
      boolean update = false;
      int maxHealth = 0;
      int health = 0;
      HashList<Integer> hasList = new HashListImpl();
      Iterator<Integer> it = cm.getLevelHash().keySet().iterator();

      while(it.hasNext()) {
         int id = (Integer)it.next();
         Level level = (Level)this.levelHash.get(id);
         if (level == null) {
            it.remove();
            update = true;
         } else if (hasList.has(id)) {
            it.remove();
            update = true;
         } else {
            hasList.add(id);
            int value = (Integer)cm.getLevelHash().get(id);
            if (value < level.getMinLevel()) {
               value = level.getMinLevel();
               update = true;
               cm.getLevelHash().put(id, value);
            } else if (value > level.getMaxLevel()) {
               value = level.getMaxLevel();
               update = true;
               cm.getLevelHash().put(id, value);
            }

            switch (id) {
               case 1:
                  maxHealth = 2 * value;
                  break;
               case 2:
                  maxHealth = 10 * value;
                  break;
               case 3:
                  maxHealth = 100 * value;
                  break;
               case 4:
                  maxHealth = 1000 * value;
                  break;
               case 5:
                  health = value;
               case 6:
               case 7:
               case 8:
               case 9:
               case 13:
               case 14:
               case 18:
               case 19:
               case 24:
               default:
                  break;
               case 10:
                  cm.setDropPower(value * 100);
                  break;
               case 11:
                  cm.setDropPower(value * 10);
                  break;
               case 12:
                  cm.setDropPower(value);
                  break;
               case 15:
                  cm.setRecover((float)(10 * value));
                  break;
               case 16:
                  cm.setRecover((float)value);
                  break;
               case 17:
                  cm.setRecover((float)(0.1 * (double)value));
                  break;
               case 20:
                  cm.setDropGold(1000 * value);
                  break;
               case 21:
                  cm.setDropGold(100 * value);
                  break;
               case 22:
                  cm.setDropGold(10 * value);
                  break;
               case 23:
                  cm.setDropGold(value);
                  break;
               case 25:
                  cm.setDropExp(100 * value);
                  break;
               case 26:
                  cm.setDropExp(10 * value);
                  break;
               case 27:
                  cm.setDropExp(value);
            }

            CraftLivingEntity cle = (CraftLivingEntity)cm.getEl().getBukkitEntity();
            if (maxHealth > 0) {
               cle.setMaxHealth((double)maxHealth);
            }

            if (health > 0) {
               cle.setHealth(cle.getMaxHealth() * (double)health / (double)10.0F);
            } else {
               cle.setHealth(cle.getMaxHealth());
            }

            if (update) {
               cm.setLevelHash(cm.getLevelHash(), false);
            }
         }
      }

   }

   public HashMap getLevels(List levels) {
      HashList<Integer> hasType = new HashListImpl();
      HashMap<Integer, Integer> levelHash = new HashMap();

      for(DataInfo.LevelInfo li : levels) {
         Level level = (Level)this.levelHash.get(li.getId());
         if (level != null && !hasType.has(level.getType()) && this.r.nextInt(1000) < li.getChance()) {
            levelHash.put(li.getId(), this.r.nextInt(li.getMax() - li.getMin() + 1) + li.getMin());
            hasType.add(level.getType());
         }
      }

      return levelHash;
   }

   private void loadConfig(YamlConfiguration config) {
      try {
         YamlConfiguration levelConfig = new YamlConfiguration();
         levelConfig.load(this.levelConfigPath);
         this.typeHash = new HashMap();

         for(String s : levelConfig.getStringList("types")) {
            int type = Integer.parseInt(s.split(" ")[0]);
            String name = s.split(" ")[1];
            this.typeHash.put(type, name);
         }

         this.levelHash = new HashMap();

         for(int type : this.typeHash.keySet()) {
            if (levelConfig.contains("levels.type" + type)) {
               int index = 1;

               while(true) {
                  String path = "levels.type" + type + ".level" + index;
                  if (!levelConfig.contains(path)) {
                     break;
                  }

                  int id = levelConfig.getInt(path + ".id");
                  String name = Util.convert(levelConfig.getString(path + ".name"));
                  int minLevel = levelConfig.getInt(path + ".level.min");
                  int maxLevel = levelConfig.getInt(path + ".level.max");
                  Level level = new Level(type, id, name, minLevel, maxLevel);
                  this.levelHash.put(id, level);
                  ++index;
               }
            }
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

   }

   public class Level {
      private int type;
      private int id;
      private String name;
      private int minLevel;
      private int maxLevel;

      public Level(int type, int id, String name, int minLevel, int maxLevel) {
         super();
         this.id = id;
         this.name = name;
         this.minLevel = minLevel;
         this.maxLevel = maxLevel;
      }

      public int getType() {
         return this.type;
      }

      public void setType(int type) {
         this.type = type;
      }

      public int getId() {
         return this.id;
      }

      public void setId(int id) {
         this.id = id;
      }

      public String getName() {
         return this.name;
      }

      public void setName(String name) {
         this.name = name;
      }

      public int getMinLevel() {
         return this.minLevel;
      }

      public void setMinLevel(int minLevel) {
         this.minLevel = minLevel;
      }

      public int getMaxLevel() {
         return this.maxLevel;
      }

      public void setMaxLevel(int maxLevel) {
         this.maxLevel = maxLevel;
      }

      public String getName(int level) {
         String show = (String)Cus.getNumHash().get(level);
         if (show == null) {
            show = String.valueOf(level);
         }

         return this.name.replace("*", show);
      }
   }
}
