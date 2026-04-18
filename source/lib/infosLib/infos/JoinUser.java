package infos;

public class JoinUser {
   private long id;
   private String name;
   private boolean free;
   private String joinUser;
   private int amount;

   public JoinUser() {
      super();
   }

   public JoinUser(String name) {
      super();
      this.name = name;
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

   public boolean isFree() {
      return this.free;
   }

   public void setFree(boolean free) {
      this.free = free;
   }

   public String getJoinUser() {
      return this.joinUser;
   }

   public void setJoinUser(String joinUser) {
      this.joinUser = joinUser;
   }

   public int getAmount() {
      return this.amount;
   }

   public void setAmount(int amount) {
      this.amount = amount;
   }
}
