package pack;

import java.io.Serializable;
import java.util.HashMap;

public class PackUser implements Serializable {
   private static final long serialVersionUID = 1L;
   private long id;
   private String name;
   private HashMap itemsHash;

   public PackUser() {
      super();
   }

   public PackUser(String name, HashMap itemsHash) {
      super();
      this.name = name;
      this.itemsHash = itemsHash;
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

   public HashMap getItemsHash() {
      return this.itemsHash;
   }

   public void setItemsHash(HashMap itemsHash) {
      this.itemsHash = itemsHash;
   }
}
