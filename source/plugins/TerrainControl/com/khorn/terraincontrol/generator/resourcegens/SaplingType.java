package com.khorn.terraincontrol.generator.resourcegens;

public enum SaplingType {
   All(-1, true),
   Oak(0, true),
   Redwood(1, true),
   Birch(2, true),
   SmallJungle(3, true),
   BigJungle(4, true),
   RedMushroom(5, false),
   BrownMushroom(6, false);

   private final int id;
   private final boolean isTreeSapling;
   private static SaplingType[] lookupList = new SaplingType[20];

   private SaplingType(int id, boolean isTreeSapling) {
      this.id = id;
      this.isTreeSapling = isTreeSapling;
   }

   public int getSaplingId() {
      return this.id;
   }

   public boolean growsTree() {
      return this.isTreeSapling;
   }

   public static SaplingType get(String name) {
      try {
         return get(Integer.parseInt(name));
      } catch (NumberFormatException var4) {
         try {
            return valueOf(name);
         } catch (IllegalArgumentException var3) {
            return null;
         }
      }
   }

   public static SaplingType get(int id) {
      if (id == -1) {
         return All;
      } else {
         return id >= 0 && id < lookupList.length ? lookupList[id] : null;
      }
   }

   static {
      for(SaplingType type : values()) {
         if (type.id >= 0) {
            lookupList[type.id] = type;
         }
      }

   }
}
