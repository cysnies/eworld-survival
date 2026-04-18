package ad;

import java.util.ArrayList;
import java.util.List;

public class AdChatUser {
   private long id;
   private String name;
   private int cost;
   private List msg;
   private int count;

   public AdChatUser() {
      super();
   }

   public AdChatUser(String name) {
      super();
      this.name = name;
      this.cost = 100;
      this.msg = new ArrayList();
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

   public int getCost() {
      return this.cost;
   }

   public void setCost(int cost) {
      this.cost = cost;
   }

   public List getMsg() {
      return this.msg;
   }

   public void setMsg(List msg) {
      this.msg = msg;
   }

   public int getCount() {
      return this.count;
   }

   public void setCount(int count) {
      this.count = count;
   }

   public boolean isEmpty() {
      for(String s : this.msg) {
         if (!s.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public int hashCode() {
      return (int)this.id;
   }

   public boolean equals(Object obj) {
      return ((AdChatUser)obj).getId() == this.id;
   }
}
