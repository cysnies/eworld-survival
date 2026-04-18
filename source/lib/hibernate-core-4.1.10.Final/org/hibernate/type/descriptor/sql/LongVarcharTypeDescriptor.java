package org.hibernate.type.descriptor.sql;

public class LongVarcharTypeDescriptor extends VarcharTypeDescriptor {
   public static final LongVarcharTypeDescriptor INSTANCE = new LongVarcharTypeDescriptor();

   public LongVarcharTypeDescriptor() {
      super();
   }

   public int getSqlType() {
      return -1;
   }
}
