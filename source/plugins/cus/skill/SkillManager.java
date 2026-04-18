package skill;

import cus.Cus;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import lib.config.ReloadConfigEvent;
import lib.hashList.ChanceHashList;
import lib.hashList.ChanceHashListImpl;
import lib.util.Util;
import lib.util.UtilConfig;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SkillManager implements Listener {
   private String pn;
   private String skillsPath;
   private static HashMap skillTypeHash = new HashMap();
   private HashMap skillNameHash;
   private HashMap skillHash;

   static {
      skillTypeHash.put(1, Tp.class);
      skillTypeHash.put(2, Attack.class);
      skillTypeHash.put(3, AttackAll.class);
      skillTypeHash.put(5, Suck.class);
      skillTypeHash.put(7, Rape.class);
      skillTypeHash.put(9, Kick.class);
   }

   public SkillManager(Cus cus) {
      super();
      this.pn = cus.getPn();
      this.skillsPath = cus.getPluginPath() + File.separator + this.pn + File.separator + "skills.yml";
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

   public String getSkillName(int id) {
      return (String)this.skillNameHash.get(id);
   }

   public Skill getRandomSkill(String type) {
      try {
         return (Skill)((ChanceHashList)this.skillHash.get(type)).getRandom();
      } catch (Exception var3) {
         return null;
      }
   }

   private void loadConfig(YamlConfiguration config) {
      try {
         YamlConfiguration skillConfig = new YamlConfiguration();
         skillConfig.load(this.skillsPath);
         this.skillNameHash = new HashMap();

         for(String s : skillConfig.getStringList("skillName")) {
            int id = Integer.parseInt(s.split(" ")[0]);
            String name = s.split(" ")[1];
            this.skillNameHash.put(id, name);
         }

         this.skillHash = new HashMap();
         MemorySection ms = (MemorySection)skillConfig.get("skills");
         Map<String, Object> map = ms.getValues(false);

         for(String s : map.keySet()) {
            this.loadSkillConfig(skillConfig, s);
         }
      } catch (NumberFormatException e) {
         e.printStackTrace();
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

   }

   private void loadSkillConfig(FileConfiguration config, String type) {
      this.skillHash.put(type, new ChanceHashListImpl());

      for(String s : config.getStringList("skills." + type)) {
         int chance = Integer.parseInt(s.split(" ")[0]);
         int id = Integer.parseInt(s.split(" ")[1]);
         String arg = null;
         if (s.split(" ").length > 2) {
            arg = Util.combine(s.split(" "), " ", 2, s.split(" ").length);
         }

         try {
            Skill skill = (Skill)((Class)skillTypeHash.get(id)).getConstructor(String.class).newInstance(arg);
            ((ChanceHashList)this.skillHash.get(type)).addChance(skill, chance);
         } catch (InstantiationException e) {
            e.printStackTrace();
         } catch (IllegalAccessException e) {
            e.printStackTrace();
         } catch (IllegalArgumentException e) {
            e.printStackTrace();
         } catch (InvocationTargetException e) {
            e.printStackTrace();
         } catch (NoSuchMethodException e) {
            e.printStackTrace();
         } catch (SecurityException e) {
            e.printStackTrace();
         }
      }

   }
}
