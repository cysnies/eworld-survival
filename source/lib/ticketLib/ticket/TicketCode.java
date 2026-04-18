package ticket;

public class TicketCode {
   private long id;
   private String code;
   private int ticket;
   private int status;
   private String user;
   private long createTime;
   private long useTime;

   public TicketCode() {
      super();
   }

   public TicketCode(String code, int ticket, long createTime) {
      super();
      this.code = code;
      this.ticket = ticket;
      this.createTime = createTime;
   }

   public long getId() {
      return this.id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public String getCode() {
      return this.code;
   }

   public void setCode(String code) {
      this.code = code;
   }

   public int getTicket() {
      return this.ticket;
   }

   public void setTicket(int ticket) {
      this.ticket = ticket;
   }

   public int getStatus() {
      return this.status;
   }

   public void setStatus(int status) {
      this.status = status;
   }

   public String getUser() {
      return this.user;
   }

   public void setUser(String user) {
      this.user = user;
   }

   public long getCreateTime() {
      return this.createTime;
   }

   public void setCreateTime(long createTime) {
      this.createTime = createTime;
   }

   public long getUseTime() {
      return this.useTime;
   }

   public void setUseTime(long useTime) {
      this.useTime = useTime;
   }
}
