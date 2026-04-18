package org.hibernate.type;

/** @deprecated */
@Deprecated
public class PrimitiveByteArrayBlobType extends ByteArrayBlobType {
   public PrimitiveByteArrayBlobType() {
      super();
   }

   public Class getReturnedClass() {
      return byte[].class;
   }

   protected Object wrap(byte[] bytes) {
      return bytes;
   }

   protected byte[] unWrap(Object bytes) {
      return (byte[])bytes;
   }
}
