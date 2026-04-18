package level;

import java.io.Serializable;

public class LevelUserInfo implements Serializable {
   private static final long serialVersionUID = 1L;
   private long id;
   private int levelId;
   private long start;
   private int last;
   private boolean effect;

   public LevelUserInfo(int levelId, long start, int last, boolean effect) {
      super();
      this.levelId = levelId;
      this.start = start;
      this.last = last;
      this.effect = effect;
   }

   public long getId() {
      return this.id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public int getLevelId() {
      return this.levelId;
   }

   public void setLevelId(int levelId) {
      this.levelId = levelId;
   }

   public long getStart() {
      return this.start;
   }

   public void setStart(long start) {
      this.start = start;
   }

   public int getLast() {
      return this.last;
   }

   public void setLast(int last) {
      this.last = last;
   }

   public boolean isEffect() {
      return this.effect;
   }

   public void setEffect(boolean effect) {
      this.effect = effect;
   }
}
