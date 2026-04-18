package chat;

import lib.hashList.HashList;
import lib.hashList.HashListImpl;

public class BlackUser {
   long id;
   String name;
   HashList blackList;

   public BlackUser() {
      super();
   }

   public BlackUser(String name) {
      super();
      this.name = name;
      this.blackList = new HashListImpl();
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

   public HashList getBlackList() {
      return this.blackList;
   }

   public void setBlackList(HashList blackList) {
      this.blackList = blackList;
   }
}
