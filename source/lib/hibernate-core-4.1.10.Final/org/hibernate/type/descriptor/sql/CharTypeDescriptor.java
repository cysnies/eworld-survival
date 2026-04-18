package org.hibernate.type.descriptor.sql;

public class CharTypeDescriptor extends VarcharTypeDescriptor {
   public static final CharTypeDescriptor INSTANCE = new CharTypeDescriptor();

   public CharTypeDescriptor() {
      super();
   }

   public int getSqlType() {
      return 1;
   }
}
