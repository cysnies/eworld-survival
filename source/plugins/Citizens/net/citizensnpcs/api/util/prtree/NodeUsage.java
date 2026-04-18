package net.citizensnpcs.api.util.prtree;

class NodeUsage {
   private final Object data;
   private int id;

   public NodeUsage(Object data, int id) {
      super();
      this.data = data;
      this.id = id;
   }

   public void changeOwner(int id) {
      this.id = id;
   }

   public Object getData() {
      return this.data;
   }

   public int getOwner() {
      return this.id;
   }

   public boolean isUsed() {
      return this.id < 0;
   }

   public String toString() {
      return this.getClass().getSimpleName() + "{data: " + this.data + ", id: " + this.id + "}";
   }

   public void use() {
      if (this.id >= 0) {
         this.id = -this.id;
      } else {
         throw new RuntimeException("Trying to use already used node");
      }
   }
}
