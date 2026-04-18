package ad;

public class AdUser {
   private long id;
   private String name;
   private String msg;
   private int price;
   private long start;

   public AdUser() {
      super();
   }

   public AdUser(String name, String msg, int price, long start) {
      super();
      this.name = name;
      this.msg = msg;
      this.price = price;
      this.start = start;
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

   public String getMsg() {
      return this.msg;
   }

   public int getPrice() {
      return this.price;
   }

   public void setMsg(String msg) {
      this.msg = msg;
   }

   public void setPrice(int price) {
      this.price = price;
   }

   public long getStart() {
      return this.start;
   }

   public void setStart(long start) {
      this.start = start;
   }

   public int hashCode() {
      return (int)this.id;
   }

   public boolean equals(Object obj) {
      return ((AdUser)obj).getId() == this.id;
   }
}
