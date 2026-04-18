package chat;

public class ChatUser {
   long id;
   String name;
   char c;

   public ChatUser() {
      super();
   }

   public ChatUser(String name, char c) {
      super();
      this.name = name;
      this.c = c;
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

   public char getC() {
      return this.c;
   }

   public void setC(char c) {
      this.c = c;
   }
}
