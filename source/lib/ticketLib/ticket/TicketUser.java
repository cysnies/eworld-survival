package ticket;

public class TicketUser {
   private long id;
   private String name;
   private int ticket;

   public TicketUser() {
      super();
   }

   public TicketUser(String name, int ticket) {
      super();
      this.name = name;
      this.ticket = ticket;
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

   public int getTicket() {
      return this.ticket;
   }

   public void setTicket(int ticket) {
      this.ticket = ticket;
   }
}
