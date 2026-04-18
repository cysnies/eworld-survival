package infos;

import java.util.HashMap;

public class GiftUser {
   private long id;
   private String name;
   private HashMap giftHash;

   public GiftUser() {
      super();
   }

   public GiftUser(String name) {
      super();
      this.name = name;
      this.giftHash = new HashMap();
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

   public HashMap getGiftHash() {
      return this.giftHash;
   }

   public void setGiftHash(HashMap giftHash) {
      this.giftHash = giftHash;
   }
}
