package town;

import java.util.HashMap;

public class TownInfo {
   private long id;
   private long landId;
   private String name;
   private int level;
   private int exp;
   private boolean safeLock;
   private int x;
   private int z;
   private HashMap userHash;
   private HashMap giveHash;
   private HashMap askHash;
   private boolean active;

   public TownInfo() {
      super();
   }

   public TownInfo(long landId, int x, int z) {
      super();
      this.landId = landId;
      this.x = x;
      this.z = z;
      this.userHash = new HashMap();
      this.giveHash = new HashMap();
      this.askHash = new HashMap();
      this.active = true;
   }

   public long getId() {
      return this.id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public long getLandId() {
      return this.landId;
   }

   public void setLandId(long landId) {
      this.landId = landId;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public int getX() {
      return this.x;
   }

   public void setX(int x) {
      this.x = x;
   }

   public int getZ() {
      return this.z;
   }

   public void setZ(int z) {
      this.z = z;
   }

   public HashMap getUserHash() {
      return this.userHash;
   }

   public void setUserHash(HashMap userHash) {
      this.userHash = userHash;
   }

   public HashMap getAskHash() {
      return this.askHash;
   }

   public void setAskHash(HashMap askHash) {
      this.askHash = askHash;
   }

   public boolean isActive() {
      return this.active;
   }

   public void setActive(boolean active) {
      this.active = active;
   }

   public int getLevel() {
      return this.level;
   }

   public void setLevel(int level) {
      this.level = level;
   }

   public int getExp() {
      return this.exp;
   }

   public void setExp(int exp) {
      this.exp = exp;
   }

   public boolean isSafeLock() {
      return this.safeLock;
   }

   public void setSafeLock(boolean safeLock) {
      this.safeLock = safeLock;
   }

   public HashMap getGiveHash() {
      if (this.giveHash == null) {
         this.giveHash = new HashMap();
      }

      return this.giveHash;
   }

   public void setGiveHash(HashMap giveHash) {
      this.giveHash = giveHash;
   }

   public int hashCode() {
      return (int)this.id;
   }

   public boolean equals(Object obj) {
      return this.id == ((TownInfo)obj).id;
   }
}
