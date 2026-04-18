package level;

import java.util.HashMap;

public class LevelUser {
   private long id;
   private String name;
   private HashMap levelHash;
   private int showLevelId;

   public LevelUser() {
      super();
   }

   public LevelUser(String name) {
      super();
      this.name = name;
      this.levelHash = new HashMap();
   }

   public long getId() {
      return this.id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public HashMap getLevelHash() {
      return this.levelHash;
   }

   public void setLevelHash(HashMap levelHash) {
      this.levelHash = levelHash;
   }

   public int getShowLevelId() {
      return this.showLevelId;
   }

   public void setShowLevelId(int showLevelId) {
      this.showLevelId = showLevelId;
   }
}
