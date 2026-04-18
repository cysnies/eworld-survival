package chest;

import land.Pos;

public class ChestInfo {
   private long id;
   private Pos pos;
   private boolean generate;
   private boolean refresh;
   private int check;
   private int chance;
   private String itemType;
   private String enchantType;

   public ChestInfo() {
      super();
   }

   public ChestInfo(Pos pos, boolean generate, boolean refresh, int check, int chance, String itemType, String enchantType) {
      super();
      this.pos = pos;
      this.generate = generate;
      this.refresh = refresh;
      this.check = check;
      this.chance = chance;
      this.itemType = itemType;
      this.enchantType = enchantType;
   }

   public long getId() {
      return this.id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public Pos getPos() {
      return this.pos;
   }

   public void setPos(Pos pos) {
      this.pos = pos;
   }

   public String getItemType() {
      return this.itemType;
   }

   public void setItemType(String itemType) {
      this.itemType = itemType;
   }

   public String getEnchantType() {
      return this.enchantType;
   }

   public void setEnchantType(String enchantType) {
      this.enchantType = enchantType;
   }

   public boolean isRefresh() {
      return this.refresh;
   }

   public void setRefresh(boolean refresh) {
      this.refresh = refresh;
   }

   public int getChance() {
      return this.chance;
   }

   public void setChance(int chance) {
      this.chance = chance;
   }

   public boolean isGenerate() {
      return this.generate;
   }

   public void setGenerate(boolean generate) {
      this.generate = generate;
   }

   public int getCheck() {
      return this.check;
   }

   public void setCheck(int check) {
      this.check = check;
   }
}
