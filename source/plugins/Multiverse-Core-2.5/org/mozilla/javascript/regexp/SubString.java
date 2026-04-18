package org.mozilla.javascript.regexp;

public class SubString {
   public static final SubString emptySubString = new SubString();
   String str;
   int index;
   int length;

   public SubString() {
      super();
   }

   public SubString(String str) {
      super();
      this.str = str;
      this.index = 0;
      this.length = str.length();
   }

   public SubString(String source, int start, int len) {
      super();
      this.str = source;
      this.index = start;
      this.length = len;
   }

   public String toString() {
      return this.str == null ? "" : this.str.substring(this.index, this.index + this.length);
   }
}
