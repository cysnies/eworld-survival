package org.hibernate.type.descriptor.sql;

public class FloatTypeDescriptor extends RealTypeDescriptor {
   public static final FloatTypeDescriptor INSTANCE = new FloatTypeDescriptor();

   public FloatTypeDescriptor() {
      super();
   }

   public int getSqlType() {
      return 6;
   }
}
