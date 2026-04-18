package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;
import javax.mail.Flags;

public class FLAGS extends Flags implements Item {
   static final char[] name = new char[]{'F', 'L', 'A', 'G', 'S'};
   public int msgno;
   private static final long serialVersionUID = 439049847053756670L;

   public FLAGS(IMAPResponse r) throws ParsingException {
      super();
      this.msgno = r.getNumber();
      r.skipSpaces();
      String[] flags = r.readSimpleList();
      if (flags != null) {
         for(int i = 0; i < flags.length; ++i) {
            String s = flags[i];
            if (s.length() >= 2 && s.charAt(0) == '\\') {
               switch (Character.toUpperCase(s.charAt(1))) {
                  case '*':
                     this.add(Flags.Flag.USER);
                     break;
                  case 'A':
                     this.add(Flags.Flag.ANSWERED);
                     break;
                  case 'D':
                     if (s.length() >= 3) {
                        char c = s.charAt(2);
                        if (c != 'e' && c != 'E') {
                           if (c == 'r' || c == 'R') {
                              this.add(Flags.Flag.DRAFT);
                           }
                        } else {
                           this.add(Flags.Flag.DELETED);
                        }
                     } else {
                        this.add(s);
                     }
                     break;
                  case 'F':
                     this.add(Flags.Flag.FLAGGED);
                     break;
                  case 'R':
                     this.add(Flags.Flag.RECENT);
                     break;
                  case 'S':
                     this.add(Flags.Flag.SEEN);
                     break;
                  default:
                     this.add(s);
               }
            } else {
               this.add(s);
            }
         }
      }

   }
}
