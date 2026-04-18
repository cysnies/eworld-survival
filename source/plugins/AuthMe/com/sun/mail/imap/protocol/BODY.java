package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ByteArray;
import com.sun.mail.iap.ParsingException;
import java.io.ByteArrayInputStream;

public class BODY implements Item {
   static final char[] name = new char[]{'B', 'O', 'D', 'Y'};
   public int msgno;
   public ByteArray data;
   public String section;
   public int origin = 0;

   public BODY(FetchResponse r) throws ParsingException {
      super();
      this.msgno = r.getNumber();
      r.skipSpaces();

      int b;
      while((b = r.readByte()) != 93) {
         if (b == 0) {
            throw new ParsingException("BODY parse error: missing ``]'' at section end");
         }
      }

      if (r.readByte() == 60) {
         this.origin = r.readNumber();
         r.skip(1);
      }

      this.data = r.readByteArray();
   }

   public ByteArray getByteArray() {
      return this.data;
   }

   public ByteArrayInputStream getByteArrayInputStream() {
      return this.data != null ? this.data.toByteArrayInputStream() : null;
   }
}
