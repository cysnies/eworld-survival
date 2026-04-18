package chat;

public class ChannelUser {
   long id;
   String name;
   int channel;

   public ChannelUser() {
      super();
   }

   public ChannelUser(String name, int channel) {
      super();
      this.name = name;
      this.channel = channel;
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

   public int getChannel() {
      return this.channel;
   }

   public void setChannel(int channel) {
      this.channel = channel;
   }
}
