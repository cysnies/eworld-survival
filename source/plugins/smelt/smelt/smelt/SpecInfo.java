package smelt;

public class SpecInfo {
   private int id;
   private String check;
   private String name;
   private float offset;
   private float speed;
   private int count;
   private String type;

   public SpecInfo(int id, String check, String name, float offset, float speed, int count, String type) {
      super();
      this.id = id;
      this.check = check;
      this.name = name;
      this.offset = offset;
      this.speed = speed;
      this.count = count;
      this.type = type;
   }

   public int getId() {
      return this.id;
   }

   public String getCheck() {
      return this.check;
   }

   public String getName() {
      return this.name;
   }

   public float getOffset() {
      return this.offset;
   }

   public float getSpeed() {
      return this.speed;
   }

   public int getCount() {
      return this.count;
   }

   public String getType() {
      return this.type;
   }
}
