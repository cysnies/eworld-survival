package sub;

public class PackLimit {
   private long id;
   private String name;
   private int limit;

   public PackLimit() {
      super();
   }

   public PackLimit(String name, int limit) {
      super();
      this.name = name;
      this.limit = limit;
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

   public int getLimit() {
      return this.limit;
   }

   public void setLimit(int limit) {
      this.limit = limit;
   }
}
