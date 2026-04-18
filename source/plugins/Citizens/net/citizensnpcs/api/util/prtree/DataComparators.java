package net.citizensnpcs.api.util.prtree;

import java.util.Comparator;

class DataComparators implements NodeComparators {
   private final MBRConverter converter;

   public DataComparators(MBRConverter converter) {
      super();
      this.converter = converter;
   }

   public Comparator getMaxComparator(int axis) {
      // $FF: Couldn't be decompiled
   }

   public Comparator getMinComparator(int axis) {
      // $FF: Couldn't be decompiled
   }

   // $FF: synthetic method
   static MBRConverter access$000(DataComparators x0) {
      return x0.converter;
   }
}
