package level;

import java.util.List;
import lib.hashList.HashList;
import lib.util.UtilFormat;

public class Level {
   private int id;
   private int typeId;
   private String name;
   private String show;
   private boolean overlap;
   private String condition;
   private String rewards;
   private List rewardsShow;
   private String group;
   private List pers;
   private int timeLimit;
   private int addType;
   private HashList effect;

   public Level(int id, int typeId, String name, String show, boolean overlap, String condition, String rewards, List rewardsShow, String group, List pers, int timeLimit, int addType, HashList effect) {
      super();
      this.id = id;
      this.typeId = typeId;
      this.name = name;
      this.show = show;
      this.overlap = overlap;
      this.condition = condition;
      this.rewards = rewards;
      this.rewardsShow = rewardsShow;
      this.group = group;
      this.pers = pers;
      this.timeLimit = timeLimit;
      this.addType = addType;
      this.effect = effect;
   }

   public int getId() {
      return this.id;
   }

   public int getTypeId() {
      return this.typeId;
   }

   public String getName() {
      return this.name;
   }

   public String getShow() {
      return this.show;
   }

   public boolean isOverlap() {
      return this.overlap;
   }

   public String getOverlapShow() {
      return this.overlap ? get(190) : get(195);
   }

   public String getCondition() {
      return this.condition;
   }

   public String getRewards() {
      return this.rewards;
   }

   public String getRewardsShow2() {
      if (this.rewardsShow.isEmpty()) {
         return "";
      } else {
         String result = "";

         for(String re : this.rewardsShow) {
            result = result + "\n";
            result = result + "- ";
            result = result + re;
         }

         return result;
      }
   }

   public String getGroup() {
      return this.group;
   }

   public List getPers() {
      return this.pers;
   }

   public String getPerShow() {
      if (this.pers.isEmpty()) {
         return "";
      } else {
         String result = "";

         for(String per : this.pers) {
            result = result + "\n";
            result = result + "- ";
            result = result + per;
         }

         return result;
      }
   }

   public int getTimeLimit() {
      return this.timeLimit;
   }

   public String getTimeLimitShow() {
      return getTimeLimitShow(this.timeLimit);
   }

   public static String getTimeLimitShow(int timeLimit) {
      if (timeLimit == -2) {
         return get(220);
      } else {
         return timeLimit == -1 ? get(225) : get(230);
      }
   }

   public int getAddType() {
      return this.addType;
   }

   public String getAddTypeShow() {
      switch (this.addType) {
         case 0:
            return get(200);
         case 1:
            return get(205);
         case 2:
            return get(210);
         case 3:
            return get(215);
         default:
            return get(200);
      }
   }

   public HashList getEffect() {
      return this.effect;
   }

   public String getEffectShow() {
      if (this.effect.isEmpty()) {
         return "";
      } else {
         String result = "";

         for(Effect e : this.effect) {
            result = result + "\n";
            result = result + "- ";
            result = result + e.getShow();
         }

         return result;
      }
   }

   public List getRewardsShow() {
      return this.rewardsShow;
   }

   private static String get(int id) {
      return UtilFormat.format(Main.getPn(), id);
   }
}
