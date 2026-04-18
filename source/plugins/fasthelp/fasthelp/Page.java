package fasthelp;

import java.util.HashMap;

public class Page {
   private String name;
   private HashMap contentHash;
   private HashMap toHash;
   private HashMap tipHash;

   public Page(String name, HashMap contentHash, HashMap toHash, HashMap tipHash) {
      super();
      this.name = name;
      this.contentHash = contentHash;
      this.toHash = toHash;
      this.tipHash = tipHash;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public HashMap getContentHash() {
      return this.contentHash;
   }

   public void setContentHash(HashMap contentHash) {
      this.contentHash = contentHash;
   }

   public HashMap getToHash() {
      return this.toHash;
   }

   public void setToHash(HashMap toHash) {
      this.toHash = toHash;
   }

   public HashMap getTipHash() {
      return this.tipHash;
   }

   public void setTipHash(HashMap tipHash) {
      this.tipHash = tipHash;
   }
}
