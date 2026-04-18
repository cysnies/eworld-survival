package ticket;

public class TicketLog {
   private long id;
   private long time;
   private String log;

   public TicketLog() {
      super();
   }

   public TicketLog(long time, String log) {
      super();
      this.time = time;
      this.log = log;
   }

   public long getId() {
      return this.id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public long getTime() {
      return this.time;
   }

   public void setTime(long time) {
      this.time = time;
   }

   public String getLog() {
      return this.log;
   }

   public void setLog(String log) {
      this.log = log;
   }
}
