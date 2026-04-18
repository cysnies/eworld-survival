package infos;

public class PlayerInfo {
   private long id;
   private String name;
   private long joinTime;
   private long lastTime;
   private boolean alive;
   private boolean active;
   private int onlineTime;
   private int mineNum;
   private int breakNum;
   private int placeNum;
   private int killMonsterNum;
   private int killAnimalNum;
   private int killPlayerNum;
   private int death;
   private int power;
   private int level;
   private String qq;
   private String xq;

   public PlayerInfo() {
      super();
   }

   public PlayerInfo(String name, long joinTime) {
      super();
      this.name = name;
      this.joinTime = joinTime;
      this.lastTime = joinTime;
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

   public long getJoinTime() {
      return this.joinTime;
   }

   public void setJoinTime(long joinTime) {
      this.joinTime = joinTime;
   }

   public long getLastTime() {
      return this.lastTime;
   }

   public void setLastTime(long lastTime) {
      this.lastTime = lastTime;
   }

   public boolean isAlive() {
      return this.alive;
   }

   public void setAlive(boolean alive) {
      this.alive = alive;
   }

   public boolean isActive() {
      return this.active;
   }

   public void setActive(boolean active) {
      this.active = active;
   }

   public int getOnlineTime() {
      return this.onlineTime;
   }

   public void setOnlineTime(int onlineTime) {
      this.onlineTime = onlineTime;
   }

   public int getMineNum() {
      return this.mineNum;
   }

   public void setMineNum(int mineNum) {
      this.mineNum = mineNum;
   }

   public int getBreakNum() {
      return this.breakNum;
   }

   public void setBreakNum(int breakNum) {
      this.breakNum = breakNum;
   }

   public int getPlaceNum() {
      return this.placeNum;
   }

   public void setPlaceNum(int placeNum) {
      this.placeNum = placeNum;
   }

   public int getKillMonsterNum() {
      return this.killMonsterNum;
   }

   public void setKillMonsterNum(int killMonsterNum) {
      this.killMonsterNum = killMonsterNum;
   }

   public int getKillAnimalNum() {
      return this.killAnimalNum;
   }

   public void setKillAnimalNum(int killAnimalNum) {
      this.killAnimalNum = killAnimalNum;
   }

   public int getKillPlayerNum() {
      return this.killPlayerNum;
   }

   public void setKillPlayerNum(int killPlayerNum) {
      this.killPlayerNum = killPlayerNum;
   }

   public int getDeath() {
      return this.death;
   }

   public void setDeath(int death) {
      this.death = death;
   }

   public int getPower() {
      return this.power;
   }

   public void setPower(int power) {
      this.power = power;
   }

   public int getLevel() {
      return this.level;
   }

   public void setLevel(int level) {
      this.level = level;
   }

   public String getXq() {
      return this.xq;
   }

   public void setXq(String xq) {
      this.xq = xq;
   }

   public String getQq() {
      return this.qq;
   }

   public void setQq(String qq) {
      this.qq = qq;
   }
}
