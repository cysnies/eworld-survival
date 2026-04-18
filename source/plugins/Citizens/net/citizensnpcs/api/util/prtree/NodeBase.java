package net.citizensnpcs.api.util.prtree;

abstract class NodeBase implements Node {
   private Object[] data;
   private MBR mbr;

   public NodeBase(Object[] data) {
      super();
      this.data = data;
   }

   public abstract MBR computeMBR(MBRConverter var1);

   public Object get(int i) {
      return this.data[i];
   }

   public MBR getMBR(MBRConverter converter) {
      if (this.mbr == null) {
         this.mbr = this.computeMBR(converter);
      }

      return this.mbr;
   }

   public MBR getUnion(MBR m1, MBR m2) {
      return m1 == null ? m2 : m1.union(m2);
   }

   public int size() {
      return this.data.length;
   }
}
