package org.hibernate.type;

/** @deprecated */
public abstract class AbstractLongBinaryType extends AbstractBynaryType {
   public AbstractLongBinaryType() {
      super();
   }

   public Class getReturnedClass() {
      return byte[].class;
   }

   protected Object toExternalFormat(byte[] bytes) {
      return bytes;
   }

   protected byte[] toInternalFormat(Object bytes) {
      return (byte[])bytes;
   }
}
