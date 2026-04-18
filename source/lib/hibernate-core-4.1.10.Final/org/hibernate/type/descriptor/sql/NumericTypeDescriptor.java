package org.hibernate.type.descriptor.sql;

public class NumericTypeDescriptor extends DecimalTypeDescriptor {
   public static final NumericTypeDescriptor INSTANCE = new NumericTypeDescriptor();

   public NumericTypeDescriptor() {
      super();
   }

   public int getSqlType() {
      return 2;
   }
}
