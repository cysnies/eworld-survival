package town;

public class TownUser {
   private long id;
   private String name;
   private long townId;
   private int pos;

   public TownUser() {
      super();
   }

   public TownUser(String name, long townId) {
      super();
      this.name = name;
      this.townId = townId;
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

   public long getTownId() {
      return this.townId;
   }

   public void setTownId(long townId) {
      this.townId = townId;
   }

   public int getPos() {
      return this.pos;
   }

   public void setPos(int pos) {
      this.pos = pos;
   }
}
