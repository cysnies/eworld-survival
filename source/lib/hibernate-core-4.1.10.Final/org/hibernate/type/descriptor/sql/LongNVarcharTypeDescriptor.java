package org.hibernate.type.descriptor.sql;

public class LongNVarcharTypeDescriptor extends NVarcharTypeDescriptor {
   public static final LongVarcharTypeDescriptor INSTANCE = new LongVarcharTypeDescriptor();

   public LongNVarcharTypeDescriptor() {
      super();
   }

   public int getSqlType() {
      return -16;
   }
}
