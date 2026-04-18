package friend;

import lib.hashList.HashList;

public class Friend {
   private long id;
   private String name;
   private boolean tip;
   private int limits;
   private HashList friendList;

   public Friend() {
      super();
   }

   public Friend(String name, boolean tip, int limits, HashList friendList) {
      super();
      this.name = name;
      this.tip = tip;
      this.limits = limits;
      this.friendList = friendList;
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

   public boolean isTip() {
      return this.tip;
   }

   public void setTip(boolean tip) {
      this.tip = tip;
   }

   public int getLimits() {
      return this.limits;
   }

   public void setLimits(int limits) {
      this.limits = limits;
   }

   public HashList getFriendList() {
      return this.friendList;
   }

   public void setFriendList(HashList friendList) {
      this.friendList = friendList;
   }
}
