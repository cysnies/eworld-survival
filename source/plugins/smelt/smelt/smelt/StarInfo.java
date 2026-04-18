package smelt;

public class StarInfo {
   private int total;
   private int fill;

   public StarInfo(int total, int fill) {
      super();
      this.total = total;
      this.fill = fill;
   }

   public int getTotal() {
      return this.total;
   }

   public int getFill() {
      return this.fill;
   }

   public void setTotal(int total) {
      this.total = total;
   }

   public void setFill(int fill) {
      this.fill = fill;
   }
}
