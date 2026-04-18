package infos;

public class PlayerDayInfo {
   private long id;
   private String name;
   private long time;
   private int onlineTime;

   public PlayerDayInfo() {
      super();
   }

   public PlayerDayInfo(String name, long time) {
      super();
      this.name = name;
      this.time = time;
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

   public long getTime() {
      return this.time;
   }

   public void setTime(long time) {
      this.time = time;
   }

   public int getOnlineTime() {
      return this.onlineTime;
   }

   public void setOnlineTime(int onlineTime) {
      this.onlineTime = onlineTime;
   }
}
