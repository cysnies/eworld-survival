package org.mozilla.javascript.regexp;

import java.io.Serializable;

final class RECharSet implements Serializable {
   static final long serialVersionUID = 7931787979395898394L;
   final int length;
   final int startIndex;
   final int strlength;
   final boolean sense;
   transient volatile boolean converted;
   transient volatile byte[] bits;

   RECharSet(int length, int startIndex, int strlength, boolean sense) {
      super();
      this.length = length;
      this.startIndex = startIndex;
      this.strlength = strlength;
      this.sense = sense;
   }
}
