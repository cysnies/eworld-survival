package shop;

import lib.util.UtilItems;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Shop {
   private long id;
   private String s;
   private String owner;
   private int price;
   private long start;
   private long last;
   private boolean ticket;
   private ItemStack is;
   private ItemStack showIs;

   public Shop() {
      super();
   }

   public Shop(String s, String owner, int price, long start, long last) {
      super();
      this.s = s;
      this.is = UtilItems.loadItem(s);
      if (this.is != null) {
         this.showIs = this.is.clone();
         ItemMeta im = this.showIs.getItemMeta();
         im.setDisplayName((String)null);
         this.showIs.setItemMeta(im);
      }

      this.owner = owner;
      this.price = price;
      this.start = start;
      this.last = last;
   }

   public long getId() {
      return this.id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public String getS() {
      return this.s;
   }

   public void setS(String s) {
      this.s = s;
      this.is = UtilItems.loadItem(s);
      if (this.is != null) {
         this.showIs = this.is.clone();
         ItemMeta im = this.showIs.getItemMeta();
         im.setDisplayName((String)null);
         this.showIs.setItemMeta(im);
      }

   }

   public String getOwner() {
      return this.owner;
   }

   public void setOwner(String owner) {
      this.owner = owner;
   }

   public int getPrice() {
      return this.price;
   }

   public void setPrice(int price) {
      this.price = price;
   }

   public long getStart() {
      return this.start;
   }

   public void setStart(long start) {
      this.start = start;
   }

   public long getLast() {
      return this.last;
   }

   public void setLast(long last) {
      this.last = last;
   }

   public boolean isTicket() {
      return this.ticket;
   }

   public void setTicket(boolean ticket) {
      this.ticket = ticket;
   }

   public ItemStack getIs() {
      return this.is;
   }

   public ItemStack getShowIs() {
      return this.showIs;
   }
}
