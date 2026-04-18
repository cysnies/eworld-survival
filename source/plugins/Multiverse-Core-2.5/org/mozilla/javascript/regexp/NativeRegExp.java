package org.mozilla.javascript.regexp;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.IdFunctionObject;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.Kit;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.TopLevel;
import org.mozilla.javascript.Undefined;

public class NativeRegExp extends IdScriptableObject implements Function {
   static final long serialVersionUID = 4965263491464903264L;
   private static final Object REGEXP_TAG = new Object();
   public static final int JSREG_GLOB = 1;
   public static final int JSREG_FOLD = 2;
   public static final int JSREG_MULTILINE = 4;
   public static final int TEST = 0;
   public static final int MATCH = 1;
   public static final int PREFIX = 2;
   private static final boolean debug = false;
   private static final byte REOP_SIMPLE_START = 1;
   private static final byte REOP_EMPTY = 1;
   private static final byte REOP_BOL = 2;
   private static final byte REOP_EOL = 3;
   private static final byte REOP_WBDRY = 4;
   private static final byte REOP_WNONBDRY = 5;
   private static final byte REOP_DOT = 6;
   private static final byte REOP_DIGIT = 7;
   private static final byte REOP_NONDIGIT = 8;
   private static final byte REOP_ALNUM = 9;
   private static final byte REOP_NONALNUM = 10;
   private static final byte REOP_SPACE = 11;
   private static final byte REOP_NONSPACE = 12;
   private static final byte REOP_BACKREF = 13;
   private static final byte REOP_FLAT = 14;
   private static final byte REOP_FLAT1 = 15;
   private static final byte REOP_FLATi = 16;
   private static final byte REOP_FLAT1i = 17;
   private static final byte REOP_UCFLAT1 = 18;
   private static final byte REOP_UCFLAT1i = 19;
   private static final byte REOP_CLASS = 22;
   private static final byte REOP_NCLASS = 23;
   private static final byte REOP_SIMPLE_END = 23;
   private static final byte REOP_QUANT = 25;
   private static final byte REOP_STAR = 26;
   private static final byte REOP_PLUS = 27;
   private static final byte REOP_OPT = 28;
   private static final byte REOP_LPAREN = 29;
   private static final byte REOP_RPAREN = 30;
   private static final byte REOP_ALT = 31;
   private static final byte REOP_JUMP = 32;
   private static final byte REOP_ASSERT = 41;
   private static final byte REOP_ASSERT_NOT = 42;
   private static final byte REOP_ASSERTTEST = 43;
   private static final byte REOP_ASSERTNOTTEST = 44;
   private static final byte REOP_MINIMALSTAR = 45;
   private static final byte REOP_MINIMALPLUS = 46;
   private static final byte REOP_MINIMALOPT = 47;
   private static final byte REOP_MINIMALQUANT = 48;
   private static final byte REOP_ENDCHILD = 49;
   private static final byte REOP_REPEAT = 51;
   private static final byte REOP_MINIMALREPEAT = 52;
   private static final byte REOP_ALTPREREQ = 53;
   private static final byte REOP_ALTPREREQi = 54;
   private static final byte REOP_ALTPREREQ2 = 55;
   private static final byte REOP_END = 57;
   private static final int ANCHOR_BOL = -2;
   private static final int INDEX_LEN = 2;
   private static final int Id_lastIndex = 1;
   private static final int Id_source = 2;
   private static final int Id_global = 3;
   private static final int Id_ignoreCase = 4;
   private static final int Id_multiline = 5;
   private static final int MAX_INSTANCE_ID = 5;
   private static final int Id_compile = 1;
   private static final int Id_toString = 2;
   private static final int Id_toSource = 3;
   private static final int Id_exec = 4;
   private static final int Id_test = 5;
   private static final int Id_prefix = 6;
   private static final int MAX_PROTOTYPE_ID = 6;
   private RECompiled re;
   double lastIndex;

   public static void init(Context cx, Scriptable scope, boolean sealed) {
      NativeRegExp proto = new NativeRegExp();
      proto.re = compileRE(cx, "", (String)null, false);
      proto.activatePrototypeMap(6);
      proto.setParentScope(scope);
      proto.setPrototype(getObjectPrototype(scope));
      NativeRegExpCtor ctor = new NativeRegExpCtor();
      proto.defineProperty("constructor", ctor, 2);
      ScriptRuntime.setFunctionProtoAndParent(ctor, scope);
      ctor.setImmunePrototypeProperty(proto);
      if (sealed) {
         proto.sealObject();
         ctor.sealObject();
      }

      defineProperty(scope, "RegExp", ctor, 2);
   }

   NativeRegExp(Scriptable scope, RECompiled regexpCompiled) {
      super();
      this.re = regexpCompiled;
      this.lastIndex = (double)0.0F;
      ScriptRuntime.setBuiltinProtoAndParent(this, scope, TopLevel.Builtins.RegExp);
   }

   public String getClassName() {
      return "RegExp";
   }

   public String getTypeOf() {
      return "object";
   }

   public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      return this.execSub(cx, scope, args, 1);
   }

   public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
      return (Scriptable)this.execSub(cx, scope, args, 1);
   }

   Scriptable compile(Context cx, Scriptable scope, Object[] args) {
      if (args.length > 0 && args[0] instanceof NativeRegExp) {
         if (args.length > 1 && args[1] != Undefined.instance) {
            throw ScriptRuntime.typeError0("msg.bad.regexp.compile");
         } else {
            NativeRegExp thatObj = (NativeRegExp)args[0];
            this.re = thatObj.re;
            this.lastIndex = thatObj.lastIndex;
            return this;
         }
      } else {
         String s = args.length == 0 ? "" : escapeRegExp(args[0]);
         String global = args.length > 1 && args[1] != Undefined.instance ? ScriptRuntime.toString(args[1]) : null;
         this.re = compileRE(cx, s, global, false);
         this.lastIndex = (double)0.0F;
         return this;
      }
   }

   public String toString() {
      StringBuilder buf = new StringBuilder();
      buf.append('/');
      if (this.re.source.length != 0) {
         buf.append(this.re.source);
      } else {
         buf.append("(?:)");
      }

      buf.append('/');
      if ((this.re.flags & 1) != 0) {
         buf.append('g');
      }

      if ((this.re.flags & 2) != 0) {
         buf.append('i');
      }

      if ((this.re.flags & 4) != 0) {
         buf.append('m');
      }

      return buf.toString();
   }

   NativeRegExp() {
      super();
   }

   private static RegExpImpl getImpl(Context cx) {
      return (RegExpImpl)ScriptRuntime.getRegExpProxy(cx);
   }

   private static String escapeRegExp(Object src) {
      String s = ScriptRuntime.toString(src);
      StringBuilder sb = null;
      int start = 0;

      for(int slash = s.indexOf(47); slash > -1; slash = s.indexOf(47, slash + 1)) {
         if (slash == start || s.charAt(slash - 1) != '\\') {
            if (sb == null) {
               sb = new StringBuilder();
            }

            sb.append(s, start, slash);
            sb.append("\\/");
            start = slash + 1;
         }
      }

      if (sb != null) {
         sb.append(s, start, s.length());
         s = sb.toString();
      }

      return s;
   }

   private Object execSub(Context cx, Scriptable scopeObj, Object[] args, int matchType) {
      RegExpImpl reImpl = getImpl(cx);
      String str;
      if (args.length == 0) {
         str = reImpl.input;
         if (str == null) {
            reportError("msg.no.re.input.for", this.toString());
         }
      } else {
         str = ScriptRuntime.toString(args[0]);
      }

      double d = (this.re.flags & 1) != 0 ? this.lastIndex : (double)0.0F;
      Object rval;
      if (!(d < (double)0.0F) && !((double)str.length() < d)) {
         int[] indexp = new int[]{(int)d};
         rval = this.executeRegExp(cx, scopeObj, reImpl, str, indexp, matchType);
         if ((this.re.flags & 1) != 0) {
            this.lastIndex = rval != null && rval != Undefined.instance ? (double)indexp[0] : (double)0.0F;
         }
      } else {
         this.lastIndex = (double)0.0F;
         rval = null;
      }

      return rval;
   }

   static RECompiled compileRE(Context cx, String str, String global, boolean flat) {
      RECompiled regexp = new RECompiled(str);
      int length = str.length();
      int flags = 0;
      if (global != null) {
         for(int i = 0; i < global.length(); ++i) {
            char c = global.charAt(i);
            if (c == 'g') {
               flags |= 1;
            } else if (c == 'i') {
               flags |= 2;
            } else if (c == 'm') {
               flags |= 4;
            } else {
               reportError("msg.invalid.re.flag", String.valueOf(c));
            }
         }
      }

      regexp.flags = flags;
      CompilerState state = new CompilerState(cx, regexp.source, length, flags);
      if (flat && length > 0) {
         state.result = new RENode((byte)14);
         state.result.chr = state.cpbegin[0];
         state.result.length = length;
         state.result.flatIndex = 0;
         state.progLength += 5;
      } else if (!parseDisjunction(state)) {
         return null;
      }

      regexp.program = new byte[state.progLength + 1];
      if (state.classCount != 0) {
         regexp.classList = new RECharSet[state.classCount];
         regexp.classCount = state.classCount;
      }

      int endPC = emitREBytecode(state, regexp, 0, state.result);
      regexp.program[endPC++] = 57;
      regexp.parenCount = state.parenCount;
      switch (regexp.program[0]) {
         case 2:
            regexp.anchorCh = -2;
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 30:
         default:
            break;
         case 14:
         case 16:
            int k = getIndex(regexp.program, 1);
            regexp.anchorCh = regexp.source[k];
            break;
         case 15:
         case 17:
            regexp.anchorCh = (char)(regexp.program[1] & 255);
            break;
         case 18:
         case 19:
            regexp.anchorCh = (char)getIndex(regexp.program, 1);
            break;
         case 31:
            RENode n = state.result;
            if (n.kid.op == 2 && n.kid2.op == 2) {
               regexp.anchorCh = -2;
            }
      }

      return regexp;
   }

   static boolean isDigit(char c) {
      return '0' <= c && c <= '9';
   }

   private static boolean isWord(char c) {
      return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z' || isDigit(c) || c == '_';
   }

   private static boolean isControlLetter(char c) {
      return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z';
   }

   private static boolean isLineTerm(char c) {
      return ScriptRuntime.isJSLineTerminator(c);
   }

   private static boolean isREWhiteSpace(int c) {
      return ScriptRuntime.isJSWhitespaceOrLineTerminator(c);
   }

   private static char upcase(char ch) {
      if (ch < 128) {
         return 'a' <= ch && ch <= 'z' ? (char)(ch + -32) : ch;
      } else {
         char cu = Character.toUpperCase(ch);
         return cu < 128 ? ch : cu;
      }
   }

   private static char downcase(char ch) {
      if (ch < 128) {
         return 'A' <= ch && ch <= 'Z' ? (char)(ch + 32) : ch;
      } else {
         char cl = Character.toLowerCase(ch);
         return cl < 128 ? ch : cl;
      }
   }

   private static int toASCIIHexDigit(int c) {
      if (c < 48) {
         return -1;
      } else if (c <= 57) {
         return c - 48;
      } else {
         c |= 32;
         return 97 <= c && c <= 102 ? c - 97 + 10 : -1;
      }
   }

   private static boolean parseDisjunction(CompilerState state) {
      if (!parseAlternative(state)) {
         return false;
      } else {
         char[] source = state.cpbegin;
         int index = state.cp;
         if (index != source.length && source[index] == '|') {
            ++state.cp;
            RENode result = new RENode((byte)31);
            result.kid = state.result;
            if (!parseDisjunction(state)) {
               return false;
            }

            result.kid2 = state.result;
            state.result = result;
            if (result.kid.op == 14 && result.kid2.op == 14) {
               result.op = (byte)((state.flags & 2) == 0 ? 53 : 54);
               result.chr = result.kid.chr;
               result.index = result.kid2.chr;
               state.progLength += 13;
            } else if (result.kid.op == 22 && result.kid.index < 256 && result.kid2.op == 14 && (state.flags & 2) == 0) {
               result.op = 55;
               result.chr = result.kid2.chr;
               result.index = result.kid.index;
               state.progLength += 13;
            } else if (result.kid.op == 14 && result.kid2.op == 22 && result.kid2.index < 256 && (state.flags & 2) == 0) {
               result.op = 55;
               result.chr = result.kid.chr;
               result.index = result.kid2.index;
               state.progLength += 13;
            } else {
               state.progLength += 9;
            }
         }

         return true;
      }
   }

   private static boolean parseAlternative(CompilerState state) {
      RENode headTerm = null;
      RENode tailTerm = null;
      char[] source = state.cpbegin;

      while(state.cp != state.cpend && source[state.cp] != '|' && (state.parenNesting == 0 || source[state.cp] != ')')) {
         if (!parseTerm(state)) {
            return false;
         }

         if (headTerm == null) {
            headTerm = state.result;
            tailTerm = headTerm;
         } else {
            tailTerm.next = state.result;
         }

         while(tailTerm.next != null) {
            tailTerm = tailTerm.next;
         }
      }

      if (headTerm == null) {
         state.result = new RENode((byte)1);
      } else {
         state.result = headTerm;
      }

      return true;
   }

   private static boolean calculateBitmapSize(CompilerState state, RENode target, char[] src, int index, int end) {
      char rangeStart = 0;
      int max = 0;
      boolean inRange = false;
      target.bmsize = 0;
      target.sense = true;
      if (index == end) {
         return true;
      } else {
         if (src[index] == '^') {
            ++index;
            target.sense = false;
         }

         while(index != end) {
            int localMax;
            localMax = 0;
            int nDigits = 2;
            label106:
            switch (src[index]) {
               case '\\':
                  ++index;
                  char c = src[index++];
                  switch (c) {
                     case '0':
                     case '1':
                     case '2':
                     case '3':
                     case '4':
                     case '5':
                     case '6':
                     case '7':
                        int var21 = c - 48;
                        c = src[index];
                        if ('0' <= c && c <= '7') {
                           ++index;
                           var21 = 8 * var21 + (c - 48);
                           c = src[index];
                           if ('0' <= c && c <= '7') {
                              ++index;
                              int var22 = 8 * var21 + (c - 48);
                              if (var22 <= 255) {
                                 var21 = var22;
                              } else {
                                 --index;
                              }
                           }
                        }

                        localMax = var21;
                        break label106;
                     case '8':
                     case '9':
                     case ':':
                     case ';':
                     case '<':
                     case '=':
                     case '>':
                     case '?':
                     case '@':
                     case 'A':
                     case 'B':
                     case 'C':
                     case 'E':
                     case 'F':
                     case 'G':
                     case 'H':
                     case 'I':
                     case 'J':
                     case 'K':
                     case 'L':
                     case 'M':
                     case 'N':
                     case 'O':
                     case 'P':
                     case 'Q':
                     case 'R':
                     case 'T':
                     case 'U':
                     case 'V':
                     case 'X':
                     case 'Y':
                     case 'Z':
                     case '[':
                     case '\\':
                     case ']':
                     case '^':
                     case '_':
                     case '`':
                     case 'a':
                     case 'e':
                     case 'g':
                     case 'h':
                     case 'i':
                     case 'j':
                     case 'k':
                     case 'l':
                     case 'm':
                     case 'o':
                     case 'p':
                     case 'q':
                     default:
                        localMax = c;
                        break label106;
                     case 'D':
                     case 'S':
                     case 'W':
                     case 's':
                     case 'w':
                        if (inRange) {
                           reportError("msg.bad.range", "");
                           return false;
                        } else {
                           target.bmsize = 65536;
                           return true;
                        }
                     case 'b':
                        localMax = 8;
                        break label106;
                     case 'c':
                        if (index < end && isControlLetter(src[index])) {
                           localMax = (char)(src[index++] & 31);
                        } else {
                           --index;
                        }

                        localMax = 92;
                        break label106;
                     case 'd':
                        if (inRange) {
                           reportError("msg.bad.range", "");
                           return false;
                        }

                        localMax = 57;
                        break label106;
                     case 'f':
                        localMax = 12;
                        break label106;
                     case 'n':
                        localMax = 10;
                        break label106;
                     case 'r':
                        localMax = 13;
                        break label106;
                     case 't':
                        localMax = 9;
                        break label106;
                     case 'u':
                        nDigits += 2;
                     case 'x':
                        int n = 0;

                        for(int i = 0; i < nDigits && index < end; ++i) {
                           c = src[index++];
                           n = Kit.xDigitToInt(c, n);
                           if (n < 0) {
                              index -= i + 1;
                              n = 92;
                              break;
                           }
                        }

                        localMax = n;
                        break label106;
                     case 'v':
                        localMax = 11;
                        break label106;
                  }
               default:
                  localMax = src[index++];
            }

            if (inRange) {
               if (rangeStart > localMax) {
                  reportError("msg.bad.range", "");
                  return false;
               }

               inRange = false;
            } else if (index < end - 1 && src[index] == '-') {
               ++index;
               inRange = true;
               rangeStart = (char)localMax;
               continue;
            }

            if ((state.flags & 2) != 0) {
               char cu = upcase((char)localMax);
               char cd = downcase((char)localMax);
               localMax = cu >= cd ? cu : cd;
            }

            if (localMax > max) {
               max = localMax;
            }
         }

         target.bmsize = max + 1;
         return true;
      }
   }

   private static void doFlat(CompilerState state, char c) {
      state.result = new RENode((byte)14);
      state.result.chr = c;
      state.result.length = 1;
      state.result.flatIndex = -1;
      state.progLength += 3;
   }

   private static int getDecimalValue(char c, CompilerState state, int maxValue, String overflowMessageId) {
      boolean overflow = false;
      int start = state.cp;
      char[] src = state.cpbegin;

      int value;
      for(value = c - 48; state.cp != state.cpend; ++state.cp) {
         c = src[state.cp];
         if (!isDigit(c)) {
            break;
         }

         if (!overflow) {
            int digit = c - 48;
            if (value < (maxValue - digit) / 10) {
               value = value * 10 + digit;
            } else {
               overflow = true;
               value = maxValue;
            }
         }
      }

      if (overflow) {
         reportError(overflowMessageId, String.valueOf(src, start, state.cp - start));
      }

      return value;
   }

   private static boolean parseTerm(CompilerState state) {
      char[] src;
      int parenBaseCount;
      src = state.cpbegin;
      char c = src[state.cp++];
      int nDigits = 2;
      parenBaseCount = state.parenCount;
      label210:
      switch (c) {
         case '$':
            state.result = new RENode((byte)3);
            ++state.progLength;
            return true;
         case '(':
            RENode result = null;
            int var33 = state.cp;
            if (state.cp + 1 < state.cpend && src[state.cp] == '?' && ((c = src[state.cp + 1]) == '=' || c == '!' || c == ':')) {
               state.cp += 2;
               if (c == '=') {
                  result = new RENode((byte)41);
                  state.progLength += 4;
               } else if (c == '!') {
                  result = new RENode((byte)42);
                  state.progLength += 4;
               }
            } else {
               result = new RENode((byte)29);
               state.progLength += 6;
               result.parenIndex = state.parenCount++;
            }

            ++state.parenNesting;
            if (!parseDisjunction(state)) {
               return false;
            }

            if (state.cp == state.cpend || src[state.cp] != ')') {
               reportError("msg.unterm.paren", "");
               return false;
            }

            ++state.cp;
            --state.parenNesting;
            if (result != null) {
               result.kid = state.result;
               state.result = result;
            }
            break;
         case ')':
            reportError("msg.re.unmatched.right.paren", "");
            return false;
         case '*':
         case '+':
         case '?':
            reportError("msg.bad.quant", String.valueOf(src[state.cp - 1]));
            return false;
         case '.':
            state.result = new RENode((byte)6);
            ++state.progLength;
            break;
         case '[':
            state.result = new RENode((byte)22);
            int var32 = state.cp;

            for(state.result.startIndex = var32; state.cp != state.cpend; ++state.cp) {
               if (src[state.cp] == '\\') {
                  ++state.cp;
               } else if (src[state.cp] == ']') {
                  state.result.kidlen = state.cp - var32;
                  state.result.index = state.classCount++;
                  if (!calculateBitmapSize(state, state.result, src, var32, state.cp++)) {
                     return false;
                  }

                  state.progLength += 3;
                  break label210;
               }
            }

            reportError("msg.unterm.class", "");
            return false;
         case '\\':
            if (state.cp >= state.cpend) {
               reportError("msg.trail.backslash", "");
               return false;
            }

            c = src[state.cp++];
            switch (c) {
               case '0':
                  reportWarning(state.cx, "msg.bad.backref", "");

                  int num;
                  int tmp;
                  for(num = 0; state.cp < state.cpend; num = tmp) {
                     c = src[state.cp];
                     if (c < '0' || c > '7') {
                        break;
                     }

                     ++state.cp;
                     tmp = 8 * num + (c - 48);
                     if (tmp > 255) {
                        break;
                     }
                  }

                  c = (char)num;
                  doFlat(state, c);
                  break label210;
               case '1':
               case '2':
               case '3':
               case '4':
               case '5':
               case '6':
               case '7':
               case '8':
               case '9':
                  int termStart = state.cp - 1;
                  int num = getDecimalValue(c, state, 65535, "msg.overlarge.backref");
                  if (num > state.parenCount) {
                     reportWarning(state.cx, "msg.bad.backref", "");
                  }

                  if (num > 9 && num > state.parenCount) {
                     state.cp = termStart;

                     int tmp;
                     for(num = 0; state.cp < state.cpend; num = tmp) {
                        c = src[state.cp];
                        if (c < '0' || c > '7') {
                           break;
                        }

                        ++state.cp;
                        tmp = 8 * num + (c - 48);
                        if (tmp > 255) {
                           break;
                        }
                     }

                     c = (char)num;
                     doFlat(state, c);
                  } else {
                     state.result = new RENode((byte)13);
                     state.result.parenIndex = num - 1;
                     state.progLength += 3;
                  }
                  break label210;
               case ':':
               case ';':
               case '<':
               case '=':
               case '>':
               case '?':
               case '@':
               case 'A':
               case 'C':
               case 'E':
               case 'F':
               case 'G':
               case 'H':
               case 'I':
               case 'J':
               case 'K':
               case 'L':
               case 'M':
               case 'N':
               case 'O':
               case 'P':
               case 'Q':
               case 'R':
               case 'T':
               case 'U':
               case 'V':
               case 'X':
               case 'Y':
               case 'Z':
               case '[':
               case '\\':
               case ']':
               case '^':
               case '_':
               case '`':
               case 'a':
               case 'e':
               case 'g':
               case 'h':
               case 'i':
               case 'j':
               case 'k':
               case 'l':
               case 'm':
               case 'o':
               case 'p':
               case 'q':
               default:
                  state.result = new RENode((byte)14);
                  state.result.chr = c;
                  state.result.length = 1;
                  state.result.flatIndex = state.cp - 1;
                  state.progLength += 3;
                  break label210;
               case 'B':
                  state.result = new RENode((byte)5);
                  ++state.progLength;
                  return true;
               case 'D':
                  state.result = new RENode((byte)8);
                  ++state.progLength;
                  break label210;
               case 'S':
                  state.result = new RENode((byte)12);
                  ++state.progLength;
                  break label210;
               case 'W':
                  state.result = new RENode((byte)10);
                  ++state.progLength;
                  break label210;
               case 'b':
                  state.result = new RENode((byte)4);
                  ++state.progLength;
                  return true;
               case 'c':
                  if (state.cp < state.cpend && isControlLetter(src[state.cp])) {
                     c = (char)(src[state.cp++] & 31);
                  } else {
                     --state.cp;
                     c = '\\';
                  }

                  doFlat(state, c);
                  break label210;
               case 'd':
                  state.result = new RENode((byte)7);
                  ++state.progLength;
                  break label210;
               case 'f':
                  c = '\f';
                  doFlat(state, c);
                  break label210;
               case 'n':
                  c = '\n';
                  doFlat(state, c);
                  break label210;
               case 'r':
                  c = '\r';
                  doFlat(state, c);
                  break label210;
               case 's':
                  state.result = new RENode((byte)11);
                  ++state.progLength;
                  break label210;
               case 't':
                  c = '\t';
                  doFlat(state, c);
                  break label210;
               case 'u':
                  nDigits += 2;
               case 'x':
                  int n = 0;

                  for(int i = 0; i < nDigits && state.cp < state.cpend; ++i) {
                     c = src[state.cp++];
                     n = Kit.xDigitToInt(c, n);
                     if (n < 0) {
                        state.cp -= i + 2;
                        n = src[state.cp++];
                        break;
                     }
                  }

                  c = (char)n;
                  doFlat(state, c);
                  break label210;
               case 'v':
                  c = '\u000b';
                  doFlat(state, c);
                  break label210;
               case 'w':
                  state.result = new RENode((byte)9);
                  ++state.progLength;
                  break label210;
            }
         case '^':
            state.result = new RENode((byte)2);
            ++state.progLength;
            return true;
         default:
            state.result = new RENode((byte)14);
            state.result.chr = c;
            state.result.length = 1;
            state.result.flatIndex = state.cp - 1;
            state.progLength += 3;
      }

      RENode term = state.result;
      if (state.cp == state.cpend) {
         return true;
      } else {
         boolean hasQ = false;
         switch (src[state.cp]) {
            case '*':
               state.result = new RENode((byte)25);
               state.result.min = 0;
               state.result.max = -1;
               state.progLength += 8;
               hasQ = true;
               break;
            case '+':
               state.result = new RENode((byte)25);
               state.result.min = 1;
               state.result.max = -1;
               state.progLength += 8;
               hasQ = true;
               break;
            case '?':
               state.result = new RENode((byte)25);
               state.result.min = 0;
               state.result.max = 1;
               state.progLength += 8;
               hasQ = true;
               break;
            case '{':
               int min = 0;
               int max = -1;
               int leftCurl = state.cp;
               if (++state.cp < src.length && isDigit(c = src[state.cp])) {
                  ++state.cp;
                  min = getDecimalValue(c, state, 65535, "msg.overlarge.min");
                  c = src[state.cp];
                  if (c == ',') {
                     c = src[++state.cp];
                     if (isDigit(c)) {
                        ++state.cp;
                        max = getDecimalValue(c, state, 65535, "msg.overlarge.max");
                        c = src[state.cp];
                        if (min > max) {
                           reportError("msg.max.lt.min", String.valueOf(src[state.cp]));
                           return false;
                        }
                     }
                  } else {
                     max = min;
                  }

                  if (c == '}') {
                     state.result = new RENode((byte)25);
                     state.result.min = min;
                     state.result.max = max;
                     state.progLength += 12;
                     hasQ = true;
                  }
               }

               if (!hasQ) {
                  state.cp = leftCurl;
               }
         }

         if (!hasQ) {
            return true;
         } else {
            ++state.cp;
            state.result.kid = term;
            state.result.parenIndex = parenBaseCount;
            state.result.parenCount = state.parenCount - parenBaseCount;
            if (state.cp < state.cpend && src[state.cp] == '?') {
               ++state.cp;
               state.result.greedy = false;
            } else {
               state.result.greedy = true;
            }

            return true;
         }
      }
   }

   private static void resolveForwardJump(byte[] array, int from, int pc) {
      if (from > pc) {
         throw Kit.codeBug();
      } else {
         addIndex(array, from, pc - from);
      }
   }

   private static int getOffset(byte[] array, int pc) {
      return getIndex(array, pc);
   }

   private static int addIndex(byte[] array, int pc, int index) {
      if (index < 0) {
         throw Kit.codeBug();
      } else if (index > 65535) {
         throw Context.reportRuntimeError("Too complex regexp");
      } else {
         array[pc] = (byte)(index >> 8);
         array[pc + 1] = (byte)index;
         return pc + 2;
      }
   }

   private static int getIndex(byte[] array, int pc) {
      return (array[pc] & 255) << 8 | array[pc + 1] & 255;
   }

   private static int emitREBytecode(CompilerState param0, RECompiled param1, int param2, RENode param3) {
      // $FF: Couldn't be decompiled
   }

   private static void pushProgState(REGlobalData gData, int min, int max, int cp, REBackTrackData backTrackLastToSave, int continuationOp, int continuationPc) {
      gData.stateStackTop = new REProgState(gData.stateStackTop, min, max, cp, backTrackLastToSave, continuationOp, continuationPc);
   }

   private static REProgState popProgState(REGlobalData gData) {
      REProgState state = gData.stateStackTop;
      gData.stateStackTop = state.previous;
      return state;
   }

   private static void pushBackTrackState(REGlobalData gData, byte op, int pc) {
      REProgState state = gData.stateStackTop;
      gData.backTrackStackTop = new REBackTrackData(gData, op, pc, gData.cp, state.continuationOp, state.continuationPc);
   }

   private static void pushBackTrackState(REGlobalData gData, byte op, int pc, int cp, int continuationOp, int continuationPc) {
      gData.backTrackStackTop = new REBackTrackData(gData, op, pc, cp, continuationOp, continuationPc);
   }

   private static boolean flatNMatcher(REGlobalData gData, int matchChars, int length, String input, int end) {
      if (gData.cp + length > end) {
         return false;
      } else {
         for(int i = 0; i < length; ++i) {
            if (gData.regexp.source[matchChars + i] != input.charAt(gData.cp + i)) {
               return false;
            }
         }

         gData.cp += length;
         return true;
      }
   }

   private static boolean flatNIMatcher(REGlobalData gData, int matchChars, int length, String input, int end) {
      if (gData.cp + length > end) {
         return false;
      } else {
         char[] source = gData.regexp.source;

         for(int i = 0; i < length; ++i) {
            char c1 = source[matchChars + i];
            char c2 = input.charAt(gData.cp + i);
            if (c1 != c2 && upcase(c1) != upcase(c2)) {
               return false;
            }
         }

         gData.cp += length;
         return true;
      }
   }

   private static boolean backrefMatcher(REGlobalData gData, int parenIndex, String input, int end) {
      if (gData.parens != null && parenIndex < gData.parens.length) {
         int parenContent = gData.parensIndex(parenIndex);
         if (parenContent == -1) {
            return true;
         } else {
            int len = gData.parensLength(parenIndex);
            if (gData.cp + len > end) {
               return false;
            } else {
               if ((gData.regexp.flags & 2) != 0) {
                  for(int i = 0; i < len; ++i) {
                     char c1 = input.charAt(parenContent + i);
                     char c2 = input.charAt(gData.cp + i);
                     if (c1 != c2 && upcase(c1) != upcase(c2)) {
                        return false;
                     }
                  }
               } else if (!input.regionMatches(parenContent, input, gData.cp, len)) {
                  return false;
               }

               gData.cp += len;
               return true;
            }
         }
      } else {
         return false;
      }
   }

   private static void addCharacterToCharSet(RECharSet cs, char c) {
      int byteIndex = c / 8;
      if (c >= cs.length) {
         throw ScriptRuntime.constructError("SyntaxError", "invalid range in character class");
      } else {
         byte[] var10000 = cs.bits;
         var10000[byteIndex] = (byte)(var10000[byteIndex] | 1 << (c & 7));
      }
   }

   private static void addCharacterRangeToCharSet(RECharSet cs, char c1, char c2) {
      int byteIndex1 = c1 / 8;
      int byteIndex2 = c2 / 8;
      if (c2 < cs.length && c1 <= c2) {
         c1 = (char)(c1 & 7);
         c2 = (char)(c2 & 7);
         if (byteIndex1 == byteIndex2) {
            byte[] var10000 = cs.bits;
            var10000[byteIndex1] = (byte)(var10000[byteIndex1] | 255 >> 7 - (c2 - c1) << c1);
         } else {
            byte[] var8 = cs.bits;
            var8[byteIndex1] = (byte)(var8[byteIndex1] | 255 << c1);

            for(int i = byteIndex1 + 1; i < byteIndex2; ++i) {
               cs.bits[i] = -1;
            }

            var8 = cs.bits;
            var8[byteIndex2] = (byte)(var8[byteIndex2] | 255 >> 7 - c2);
         }

      } else {
         throw ScriptRuntime.constructError("SyntaxError", "invalid range in character class");
      }
   }

   private static void processCharSet(REGlobalData gData, RECharSet charSet) {
      synchronized(charSet) {
         if (!charSet.converted) {
            processCharSetImpl(gData, charSet);
            charSet.converted = true;
         }

      }
   }

   private static void processCharSetImpl(REGlobalData gData, RECharSet charSet) {
      int src = charSet.startIndex;
      int end = src + charSet.strlength;
      char rangeStart = 0;
      boolean inRange = false;
      int byteLength = (charSet.length + 7) / 8;
      charSet.bits = new byte[byteLength];
      if (src != end) {
         if (gData.regexp.source[src] == '^') {
            assert !charSet.sense;

            ++src;
         } else {
            assert charSet.sense;
         }

         label171:
         while(src != end) {
            char thisCh;
            int nDigits = 2;
            label161:
            switch (gData.regexp.source[src]) {
               case '\\':
                  ++src;
                  char c = gData.regexp.source[src++];
                  switch (c) {
                     case '0':
                     case '1':
                     case '2':
                     case '3':
                     case '4':
                     case '5':
                     case '6':
                     case '7':
                        int var19 = c - 48;
                        c = gData.regexp.source[src];
                        if ('0' <= c && c <= '7') {
                           ++src;
                           var19 = 8 * var19 + (c - 48);
                           c = gData.regexp.source[src];
                           if ('0' <= c && c <= '7') {
                              ++src;
                              int var24 = 8 * var19 + (c - 48);
                              if (var24 <= 255) {
                                 var19 = var24;
                              } else {
                                 --src;
                              }
                           }
                        }

                        thisCh = (char)var19;
                        break label161;
                     case '8':
                     case '9':
                     case ':':
                     case ';':
                     case '<':
                     case '=':
                     case '>':
                     case '?':
                     case '@':
                     case 'A':
                     case 'B':
                     case 'C':
                     case 'E':
                     case 'F':
                     case 'G':
                     case 'H':
                     case 'I':
                     case 'J':
                     case 'K':
                     case 'L':
                     case 'M':
                     case 'N':
                     case 'O':
                     case 'P':
                     case 'Q':
                     case 'R':
                     case 'T':
                     case 'U':
                     case 'V':
                     case 'X':
                     case 'Y':
                     case 'Z':
                     case '[':
                     case '\\':
                     case ']':
                     case '^':
                     case '_':
                     case '`':
                     case 'a':
                     case 'e':
                     case 'g':
                     case 'h':
                     case 'i':
                     case 'j':
                     case 'k':
                     case 'l':
                     case 'm':
                     case 'o':
                     case 'p':
                     case 'q':
                     default:
                        thisCh = c;
                        break label161;
                     case 'D':
                        addCharacterRangeToCharSet(charSet, '\u0000', '/');
                        addCharacterRangeToCharSet(charSet, ':', (char)(charSet.length - 1));
                        continue;
                     case 'S':
                        int var23 = charSet.length - 1;

                        while(true) {
                           if (var23 < 0) {
                              continue label171;
                           }

                           if (!isREWhiteSpace(var23)) {
                              addCharacterToCharSet(charSet, (char)var23);
                           }

                           --var23;
                        }
                     case 'W':
                        int var22 = charSet.length - 1;

                        while(true) {
                           if (var22 < 0) {
                              continue label171;
                           }

                           if (!isWord((char)var22)) {
                              addCharacterToCharSet(charSet, (char)var22);
                           }

                           --var22;
                        }
                     case 'b':
                        thisCh = '\b';
                        break label161;
                     case 'c':
                        if (src < end && isControlLetter(gData.regexp.source[src])) {
                           thisCh = (char)(gData.regexp.source[src++] & 31);
                        } else {
                           --src;
                           thisCh = '\\';
                        }
                        break label161;
                     case 'd':
                        addCharacterRangeToCharSet(charSet, '0', '9');
                        continue;
                     case 'f':
                        thisCh = '\f';
                        break label161;
                     case 'n':
                        thisCh = '\n';
                        break label161;
                     case 'r':
                        thisCh = '\r';
                        break label161;
                     case 's':
                        int i = charSet.length - 1;

                        while(true) {
                           if (i < 0) {
                              continue label171;
                           }

                           if (isREWhiteSpace(i)) {
                              addCharacterToCharSet(charSet, (char)i);
                           }

                           --i;
                        }
                     case 't':
                        thisCh = '\t';
                        break label161;
                     case 'u':
                        nDigits += 2;
                     case 'x':
                        int n = 0;

                        for(int i = 0; i < nDigits && src < end; ++i) {
                           c = gData.regexp.source[src++];
                           int digit = toASCIIHexDigit(c);
                           if (digit < 0) {
                              src -= i + 1;
                              n = 92;
                              break;
                           }

                           n = n << 4 | digit;
                        }

                        thisCh = (char)n;
                        break label161;
                     case 'v':
                        thisCh = 11;
                        break label161;
                     case 'w':
                        for(int i = charSet.length - 1; i >= 0; --i) {
                           if (isWord((char)i)) {
                              addCharacterToCharSet(charSet, (char)i);
                           }
                        }
                        continue;
                  }
               default:
                  thisCh = gData.regexp.source[src++];
            }

            if (!inRange) {
               if ((gData.regexp.flags & 2) != 0) {
                  addCharacterToCharSet(charSet, upcase(thisCh));
                  addCharacterToCharSet(charSet, downcase(thisCh));
               } else {
                  addCharacterToCharSet(charSet, thisCh);
               }

               if (src < end - 1 && gData.regexp.source[src] == '-') {
                  ++src;
                  inRange = true;
                  rangeStart = thisCh;
               }
            } else {
               if ((gData.regexp.flags & 2) != 0) {
                  assert rangeStart <= thisCh;

                  for(char var18 = rangeStart; var18 <= thisCh; ++var18) {
                     addCharacterToCharSet(charSet, var18);
                     char uch = upcase(var18);
                     char dch = downcase(var18);
                     if (var18 != uch) {
                        addCharacterToCharSet(charSet, uch);
                     }

                     if (var18 != dch) {
                        addCharacterToCharSet(charSet, dch);
                     }
                  }
               } else {
                  addCharacterRangeToCharSet(charSet, rangeStart, thisCh);
               }

               inRange = false;
            }
         }

      }
   }

   private static boolean classMatcher(REGlobalData gData, RECharSet charSet, char ch) {
      if (!charSet.converted) {
         processCharSet(gData, charSet);
      }

      int byteIndex = ch >> 3;
      return (charSet.length == 0 || ch >= charSet.length || (charSet.bits[byteIndex] & 1 << (ch & 7)) == 0) ^ charSet.sense;
   }

   private static boolean reopIsSimple(int op) {
      return op >= 1 && op <= 23;
   }

   private static int simpleMatch(REGlobalData gData, String input, int op, byte[] program, int pc, int end, boolean updatecp) {
      boolean result = false;
      int startcp = gData.cp;
      switch (op) {
         case 1:
            result = true;
            break;
         case 2:
            if (gData.cp == 0 || gData.multiline && isLineTerm(input.charAt(gData.cp - 1))) {
               result = true;
            }
            break;
         case 3:
            if (gData.cp == end || gData.multiline && isLineTerm(input.charAt(gData.cp))) {
               result = true;
            }
            break;
         case 4:
            result = (gData.cp == 0 || !isWord(input.charAt(gData.cp - 1))) ^ (gData.cp >= end || !isWord(input.charAt(gData.cp)));
            break;
         case 5:
            result = (gData.cp == 0 || !isWord(input.charAt(gData.cp - 1))) ^ (gData.cp < end && isWord(input.charAt(gData.cp)));
            break;
         case 6:
            if (gData.cp != end && !isLineTerm(input.charAt(gData.cp))) {
               result = true;
               ++gData.cp;
            }
            break;
         case 7:
            if (gData.cp != end && isDigit(input.charAt(gData.cp))) {
               result = true;
               ++gData.cp;
            }
            break;
         case 8:
            if (gData.cp != end && !isDigit(input.charAt(gData.cp))) {
               result = true;
               ++gData.cp;
            }
            break;
         case 9:
            if (gData.cp != end && isWord(input.charAt(gData.cp))) {
               result = true;
               ++gData.cp;
            }
            break;
         case 10:
            if (gData.cp != end && !isWord(input.charAt(gData.cp))) {
               result = true;
               ++gData.cp;
            }
            break;
         case 11:
            if (gData.cp != end && isREWhiteSpace(input.charAt(gData.cp))) {
               result = true;
               ++gData.cp;
            }
            break;
         case 12:
            if (gData.cp != end && !isREWhiteSpace(input.charAt(gData.cp))) {
               result = true;
               ++gData.cp;
            }
            break;
         case 13:
            int parenIndex = getIndex(program, pc);
            pc += 2;
            result = backrefMatcher(gData, parenIndex, input, end);
            break;
         case 14:
            int offset = getIndex(program, pc);
            pc += 2;
            int length = getIndex(program, pc);
            pc += 2;
            result = flatNMatcher(gData, offset, length, input, end);
            break;
         case 15:
            char matchCh = (char)(program[pc++] & 255);
            if (gData.cp != end && input.charAt(gData.cp) == matchCh) {
               result = true;
               ++gData.cp;
            }
            break;
         case 16:
            int offset = getIndex(program, pc);
            pc += 2;
            int length = getIndex(program, pc);
            pc += 2;
            result = flatNIMatcher(gData, offset, length, input, end);
            break;
         case 17:
            char var20 = (char)(program[pc++] & 255);
            if (gData.cp != end) {
               char c = input.charAt(gData.cp);
               if (var20 == c || upcase(var20) == upcase(c)) {
                  result = true;
                  ++gData.cp;
               }
            }
            break;
         case 18:
            char var19 = (char)getIndex(program, pc);
            pc += 2;
            if (gData.cp != end && input.charAt(gData.cp) == var19) {
               result = true;
               ++gData.cp;
            }
            break;
         case 19:
            char matchCh = (char)getIndex(program, pc);
            pc += 2;
            if (gData.cp != end) {
               char c = input.charAt(gData.cp);
               if (matchCh == c || upcase(matchCh) == upcase(c)) {
                  result = true;
                  ++gData.cp;
               }
            }
            break;
         case 20:
         case 21:
         default:
            throw Kit.codeBug();
         case 22:
         case 23:
            int index = getIndex(program, pc);
            pc += 2;
            if (gData.cp != end && classMatcher(gData, gData.regexp.classList[index], input.charAt(gData.cp))) {
               ++gData.cp;
               result = true;
            }
      }

      if (result) {
         if (!updatecp) {
            gData.cp = startcp;
         }

         return pc;
      } else {
         gData.cp = startcp;
         return -1;
      }
   }

   private static boolean executeREBytecode(REGlobalData gData, String input, int end) {
      int pc = 0;
      byte[] program = gData.regexp.program;
      int continuationOp = 57;
      int continuationPc = 0;
      boolean result = false;
      int op = program[pc++];
      if (gData.regexp.anchorCh < 0 && reopIsSimple(op)) {
         boolean anchor;
         for(anchor = false; gData.cp <= end; ++gData.cp) {
            int match = simpleMatch(gData, input, op, program, pc, end, true);
            if (match >= 0) {
               anchor = true;
               pc = match + 1;
               op = program[match];
               break;
            }

            ++gData.skipped;
         }

         if (!anchor) {
            return false;
         }
      }

      while(true) {
         int nextpc;
         label252:
         while(true) {
            if (reopIsSimple(op)) {
               nextpc = simpleMatch(gData, input, op, program, pc, end, true);
               result = nextpc >= 0;
               if (result) {
                  pc = nextpc;
               }
            } else {
               label215:
               switch (op) {
                  case 25:
                  case 26:
                  case 27:
                  case 28:
                  case 45:
                  case 46:
                  case 47:
                  case 48:
                     boolean greedy = false;
                     int max;
                     switch (op) {
                        case 25:
                           greedy = true;
                        case 48:
                           nextpc = getOffset(program, pc);
                           pc += 2;
                           max = getOffset(program, pc) - 1;
                           pc += 2;
                           break;
                        case 26:
                           greedy = true;
                        case 45:
                           nextpc = 0;
                           max = -1;
                           break;
                        case 27:
                           greedy = true;
                        case 46:
                           nextpc = 1;
                           max = -1;
                           break;
                        case 28:
                           greedy = true;
                        case 47:
                           nextpc = 0;
                           max = 1;
                           break;
                        case 29:
                        case 30:
                        case 31:
                        case 32:
                        case 33:
                        case 34:
                        case 35:
                        case 36:
                        case 37:
                        case 38:
                        case 39:
                        case 40:
                        case 41:
                        case 42:
                        case 43:
                        case 44:
                        default:
                           throw Kit.codeBug();
                     }

                     pushProgState(gData, nextpc, max, gData.cp, (REBackTrackData)null, continuationOp, continuationPc);
                     if (greedy) {
                        pushBackTrackState(gData, (byte)51, pc);
                        continuationOp = 51;
                        continuationPc = pc;
                        pc += 6;
                        op = program[pc++];
                     } else if (nextpc != 0) {
                        continuationOp = 52;
                        continuationPc = pc;
                        pc += 6;
                        op = program[pc++];
                     } else {
                        pushBackTrackState(gData, (byte)52, pc);
                        popProgState(gData);
                        int var39 = pc + 4;
                        pc = var39 + getOffset(program, var39);
                        op = program[pc++];
                     }
                     continue;
                  case 29:
                     nextpc = getIndex(program, pc);
                     pc += 2;
                     gData.setParens(nextpc, gData.cp, 0);
                     op = program[pc++];
                     continue;
                  case 30:
                     nextpc = getIndex(program, pc);
                     pc += 2;
                     int cap_index = gData.parensIndex(nextpc);
                     gData.setParens(nextpc, cap_index, gData.cp - cap_index);
                     op = program[pc++];
                     continue;
                  case 32:
                     nextpc = getOffset(program, pc);
                     pc += nextpc;
                     op = program[pc++];
                     continue;
                  case 33:
                  case 34:
                  case 35:
                  case 36:
                  case 37:
                  case 38:
                  case 39:
                  case 40:
                  case 50:
                  case 56:
                  default:
                     throw Kit.codeBug("invalid bytecode");
                  case 41:
                     nextpc = pc + getIndex(program, pc);
                     pc += 2;
                     op = program[pc++];
                     if (!reopIsSimple(op) || simpleMatch(gData, input, op, program, pc, end, false) >= 0) {
                        pushProgState(gData, 0, 0, gData.cp, gData.backTrackStackTop, continuationOp, continuationPc);
                        pushBackTrackState(gData, (byte)43, nextpc);
                        continue;
                     }

                     result = false;
                     break;
                  case 42:
                     nextpc = pc + getIndex(program, pc);
                     pc += 2;
                     op = program[pc++];
                     if (!reopIsSimple(op)) {
                        break label252;
                     }

                     int match = simpleMatch(gData, input, op, program, pc, end, false);
                     if (match < 0 || program[match] != 44) {
                        break label252;
                     }

                     result = false;
                     break;
                  case 43:
                  case 44:
                     REProgState state = popProgState(gData);
                     gData.cp = state.index;
                     gData.backTrackStackTop = state.backTrack;
                     continuationPc = state.continuationPc;
                     continuationOp = state.continuationOp;
                     if (op == 44) {
                        result = !result;
                     }
                     break;
                  case 49:
                     result = true;
                     pc = continuationPc;
                     op = continuationOp;
                     continue;
                  case 51:
                     do {
                        REProgState state = popProgState(gData);
                        if (!result) {
                           if (state.min == 0) {
                              result = true;
                           }

                           continuationPc = state.continuationPc;
                           continuationOp = state.continuationOp;
                           pc += 4;
                           pc += getOffset(program, pc);
                           break label215;
                        }

                        if (state.min == 0 && gData.cp == state.index) {
                           result = false;
                           continuationPc = state.continuationPc;
                           continuationOp = state.continuationOp;
                           pc += 4;
                           pc += getOffset(program, pc);
                           break label215;
                        }

                        int new_min = state.min;
                        int new_max = state.max;
                        if (new_min != 0) {
                           --new_min;
                        }

                        if (new_max != -1) {
                           --new_max;
                        }

                        if (new_max == 0) {
                           result = true;
                           continuationPc = state.continuationPc;
                           continuationOp = state.continuationOp;
                           pc += 4;
                           pc += getOffset(program, pc);
                           break label215;
                        }

                        nextpc = pc + 6;
                        int nextop = program[nextpc];
                        int startcp = gData.cp;
                        if (reopIsSimple(nextop)) {
                           ++nextpc;
                           int match = simpleMatch(gData, input, nextop, program, nextpc, end, true);
                           if (match < 0) {
                              result = new_min == 0;
                              continuationPc = state.continuationPc;
                              continuationOp = state.continuationOp;
                              pc += 4;
                              pc += getOffset(program, pc);
                              break label215;
                           }

                           result = true;
                           nextpc = match;
                        }

                        continuationOp = 51;
                        continuationPc = pc;
                        pushProgState(gData, new_min, new_max, startcp, (REBackTrackData)null, state.continuationOp, state.continuationPc);
                        if (new_min == 0) {
                           pushBackTrackState(gData, (byte)51, pc, startcp, state.continuationOp, state.continuationPc);
                           int parenCount = getIndex(program, pc);
                           int parenIndex = getIndex(program, pc + 2);

                           for(int k = 0; k < parenCount; ++k) {
                              gData.setParens(parenIndex + k, -1, 0);
                           }
                        }
                     } while(program[nextpc] == 49);

                     pc = nextpc + 1;
                     op = program[nextpc];
                     continue;
                  case 52:
                     REProgState state = popProgState(gData);
                     if (!result) {
                        if (state.max != -1 && state.max <= 0) {
                           continuationPc = state.continuationPc;
                           continuationOp = state.continuationOp;
                           break;
                        }

                        pushProgState(gData, state.min, state.max, gData.cp, (REBackTrackData)null, state.continuationOp, state.continuationPc);
                        continuationOp = 52;
                        continuationPc = pc;
                        int parenCount = getIndex(program, pc);
                        pc += 2;
                        int parenIndex = getIndex(program, pc);
                        pc += 4;

                        for(int k = 0; k < parenCount; ++k) {
                           gData.setParens(parenIndex + k, -1, 0);
                        }

                        op = program[pc++];
                     } else {
                        if (state.min == 0 && gData.cp == state.index) {
                           result = false;
                           continuationPc = state.continuationPc;
                           continuationOp = state.continuationOp;
                           break;
                        }

                        int new_min = state.min;
                        int new_max = state.max;
                        if (new_min != 0) {
                           --new_min;
                        }

                        if (new_max != -1) {
                           --new_max;
                        }

                        pushProgState(gData, new_min, new_max, gData.cp, (REBackTrackData)null, state.continuationOp, state.continuationPc);
                        if (new_min == 0) {
                           continuationPc = state.continuationPc;
                           continuationOp = state.continuationOp;
                           pushBackTrackState(gData, (byte)52, pc);
                           popProgState(gData);
                           int var22 = pc + 4;
                           pc = var22 + getOffset(program, var22);
                           op = program[pc++];
                           continue;
                        }

                        continuationOp = 52;
                        continuationPc = pc;
                        int parenCount = getIndex(program, pc);
                        pc += 2;
                        int parenIndex = getIndex(program, pc);
                        pc += 4;

                        for(int k = 0; k < parenCount; ++k) {
                           gData.setParens(parenIndex + k, -1, 0);
                        }

                        op = program[pc++];
                     }
                     continue;
                  case 53:
                  case 54:
                  case 55:
                     nextpc = (char)getIndex(program, pc);
                     pc += 2;
                     char matchCh2 = (char)getIndex(program, pc);
                     pc += 2;
                     if (gData.cp == end) {
                        result = false;
                        break;
                     } else {
                        char c = input.charAt(gData.cp);
                        if (op == 55) {
                           if (c != nextpc && !classMatcher(gData, gData.regexp.classList[matchCh2], c)) {
                              result = false;
                              break;
                           }
                        } else {
                           if (op == 54) {
                              c = upcase(c);
                           }

                           if (c != nextpc && c != matchCh2) {
                              result = false;
                              break;
                           }
                        }
                     }
                  case 31:
                     nextpc = pc + getOffset(program, pc);
                     pc += 2;
                     op = program[pc++];
                     int startcp = gData.cp;
                     if (reopIsSimple(op)) {
                        int match = simpleMatch(gData, input, op, program, pc, end, true);
                        if (match < 0) {
                           op = program[nextpc++];
                           pc = nextpc;
                           continue;
                        }

                        result = true;
                        pc = match + 1;
                        op = program[match];
                     }

                     byte nextop = program[nextpc++];
                     pushBackTrackState(gData, nextop, nextpc, startcp, continuationOp, continuationPc);
                     continue;
                  case 57:
                     return true;
               }
            }

            if (!result) {
               REBackTrackData backTrackData = gData.backTrackStackTop;
               if (backTrackData == null) {
                  return false;
               }

               gData.backTrackStackTop = backTrackData.previous;
               gData.parens = backTrackData.parens;
               gData.cp = backTrackData.cp;
               gData.stateStackTop = backTrackData.stateStackTop;
               continuationOp = backTrackData.continuationOp;
               continuationPc = backTrackData.continuationPc;
               pc = backTrackData.pc;
               op = backTrackData.op;
            } else {
               op = program[pc++];
            }
         }

         pushProgState(gData, 0, 0, gData.cp, gData.backTrackStackTop, continuationOp, continuationPc);
         pushBackTrackState(gData, (byte)44, nextpc);
      }
   }

   private static boolean matchRegExp(REGlobalData gData, RECompiled re, String input, int start, int end, boolean multiline) {
      if (re.parenCount != 0) {
         gData.parens = new long[re.parenCount];
      } else {
         gData.parens = null;
      }

      gData.backTrackStackTop = null;
      gData.stateStackTop = null;
      gData.multiline = multiline || (re.flags & 4) != 0;
      gData.regexp = re;
      int anchorCh = gData.regexp.anchorCh;

      for(int i = start; i <= end; ++i) {
         if (anchorCh >= 0) {
            while(true) {
               if (i == end) {
                  return false;
               }

               char matchCh = input.charAt(i);
               if (matchCh == anchorCh || (gData.regexp.flags & 2) != 0 && upcase(matchCh) == upcase((char)anchorCh)) {
                  break;
               }

               ++i;
            }
         }

         gData.cp = i;
         gData.skipped = i - start;

         for(int j = 0; j < re.parenCount; ++j) {
            gData.parens[j] = -1L;
         }

         boolean result = executeREBytecode(gData, input, end);
         gData.backTrackStackTop = null;
         gData.stateStackTop = null;
         if (result) {
            return true;
         }

         if (anchorCh == -2 && !gData.multiline) {
            gData.skipped = end;
            return false;
         }

         i = start + gData.skipped;
      }

      return false;
   }

   Object executeRegExp(Context cx, Scriptable scope, RegExpImpl res, String str, int[] indexp, int matchType) {
      REGlobalData gData = new REGlobalData();
      int start = indexp[0];
      int end = str.length();
      if (start > end) {
         start = end;
      }

      boolean matches = matchRegExp(gData, this.re, str, start, end, res.multiline);
      if (!matches) {
         return matchType != 2 ? null : Undefined.instance;
      } else {
         int index = gData.cp;
         int ep = indexp[0] = index;
         int matchlen = ep - (start + gData.skipped);
         index -= matchlen;
         Object result;
         Scriptable obj;
         if (matchType == 0) {
            result = Boolean.TRUE;
            obj = null;
         } else {
            result = cx.newArray(scope, 0);
            obj = (Scriptable)result;
            String matchstr = str.substring(index, index + matchlen);
            obj.put(0, obj, matchstr);
         }

         if (this.re.parenCount == 0) {
            res.parens = null;
            res.lastParen = SubString.emptySubString;
         } else {
            SubString parsub = null;
            res.parens = new SubString[this.re.parenCount];

            for(int num = 0; num < this.re.parenCount; ++num) {
               int cap_index = gData.parensIndex(num);
               if (cap_index != -1) {
                  int cap_length = gData.parensLength(num);
                  parsub = new SubString(str, cap_index, cap_length);
                  res.parens[num] = parsub;
                  if (matchType != 0) {
                     obj.put(num + 1, obj, parsub.toString());
                  }
               } else if (matchType != 0) {
                  obj.put(num + 1, obj, Undefined.instance);
               }
            }

            res.lastParen = parsub;
         }

         if (matchType != 0) {
            obj.put("index", obj, start + gData.skipped);
            obj.put("input", obj, str);
         }

         if (res.lastMatch == null) {
            res.lastMatch = new SubString();
            res.leftContext = new SubString();
            res.rightContext = new SubString();
         }

         res.lastMatch.str = str;
         res.lastMatch.index = index;
         res.lastMatch.length = matchlen;
         res.leftContext.str = str;
         if (cx.getLanguageVersion() == 120) {
            res.leftContext.index = start;
            res.leftContext.length = gData.skipped;
         } else {
            res.leftContext.index = 0;
            res.leftContext.length = start + gData.skipped;
         }

         res.rightContext.str = str;
         res.rightContext.index = ep;
         res.rightContext.length = end - ep;
         return result;
      }
   }

   int getFlags() {
      return this.re.flags;
   }

   private static void reportWarning(Context cx, String messageId, String arg) {
      if (cx.hasFeature(11)) {
         String msg = ScriptRuntime.getMessage1(messageId, arg);
         Context.reportWarning(msg);
      }

   }

   private static void reportError(String messageId, String arg) {
      String msg = ScriptRuntime.getMessage1(messageId, arg);
      throw ScriptRuntime.constructError("SyntaxError", msg);
   }

   protected int getMaxInstanceId() {
      return 5;
   }

   protected int findInstanceIdInfo(String s) {
      int id = 0;
      String X = null;
      int s_length = s.length();
      if (s_length == 6) {
         int c = s.charAt(0);
         if (c == 103) {
            X = "global";
            id = 3;
         } else if (c == 115) {
            X = "source";
            id = 2;
         }
      } else if (s_length == 9) {
         int c = s.charAt(0);
         if (c == 108) {
            X = "lastIndex";
            id = 1;
         } else if (c == 109) {
            X = "multiline";
            id = 5;
         }
      } else if (s_length == 10) {
         X = "ignoreCase";
         id = 4;
      }

      if (X != null && X != s && !X.equals(s)) {
         id = 0;
      }

      if (id == 0) {
         return super.findInstanceIdInfo(s);
      } else {
         int attr;
         switch (id) {
            case 1:
               attr = 6;
               break;
            case 2:
            case 3:
            case 4:
            case 5:
               attr = 7;
               break;
            default:
               throw new IllegalStateException();
         }

         return instanceIdInfo(attr, id);
      }
   }

   protected String getInstanceIdName(int id) {
      switch (id) {
         case 1:
            return "lastIndex";
         case 2:
            return "source";
         case 3:
            return "global";
         case 4:
            return "ignoreCase";
         case 5:
            return "multiline";
         default:
            return super.getInstanceIdName(id);
      }
   }

   protected Object getInstanceIdValue(int id) {
      switch (id) {
         case 1:
            return ScriptRuntime.wrapNumber(this.lastIndex);
         case 2:
            return new String(this.re.source);
         case 3:
            return ScriptRuntime.wrapBoolean((this.re.flags & 1) != 0);
         case 4:
            return ScriptRuntime.wrapBoolean((this.re.flags & 2) != 0);
         case 5:
            return ScriptRuntime.wrapBoolean((this.re.flags & 4) != 0);
         default:
            return super.getInstanceIdValue(id);
      }
   }

   protected void setInstanceIdValue(int id, Object value) {
      switch (id) {
         case 1:
            this.lastIndex = ScriptRuntime.toNumber(value);
            return;
         case 2:
         case 3:
         case 4:
         case 5:
            return;
         default:
            super.setInstanceIdValue(id, value);
      }
   }

   protected void initPrototypeId(int id) {
      int arity;
      String s;
      switch (id) {
         case 1:
            arity = 1;
            s = "compile";
            break;
         case 2:
            arity = 0;
            s = "toString";
            break;
         case 3:
            arity = 0;
            s = "toSource";
            break;
         case 4:
            arity = 1;
            s = "exec";
            break;
         case 5:
            arity = 1;
            s = "test";
            break;
         case 6:
            arity = 1;
            s = "prefix";
            break;
         default:
            throw new IllegalArgumentException(String.valueOf(id));
      }

      this.initPrototypeMethod(REGEXP_TAG, id, s, arity);
   }

   public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      if (!f.hasTag(REGEXP_TAG)) {
         return super.execIdCall(f, cx, scope, thisObj, args);
      } else {
         int id = f.methodId();
         switch (id) {
            case 1:
               return realThis(thisObj, f).compile(cx, scope, args);
            case 2:
            case 3:
               return realThis(thisObj, f).toString();
            case 4:
               return realThis(thisObj, f).execSub(cx, scope, args, 1);
            case 5:
               Object x = realThis(thisObj, f).execSub(cx, scope, args, 0);
               return Boolean.TRUE.equals(x) ? Boolean.TRUE : Boolean.FALSE;
            case 6:
               return realThis(thisObj, f).execSub(cx, scope, args, 2);
            default:
               throw new IllegalArgumentException(String.valueOf(id));
         }
      }
   }

   private static NativeRegExp realThis(Scriptable thisObj, IdFunctionObject f) {
      if (!(thisObj instanceof NativeRegExp)) {
         throw incompatibleCallError(f);
      } else {
         return (NativeRegExp)thisObj;
      }
   }

   protected int findPrototypeId(String s) {
      int id = 0;
      String X = null;
      switch (s.length()) {
         case 4:
            int c = s.charAt(0);
            if (c == 101) {
               X = "exec";
               id = 4;
            } else if (c == 116) {
               X = "test";
               id = 5;
            }
         case 5:
         default:
            break;
         case 6:
            X = "prefix";
            id = 6;
            break;
         case 7:
            X = "compile";
            id = 1;
            break;
         case 8:
            int c = s.charAt(3);
            if (c == 'o') {
               X = "toSource";
               id = 3;
            } else if (c == 't') {
               X = "toString";
               id = 2;
            }
      }

      if (X != null && X != s && !X.equals(s)) {
         id = 0;
      }

      return id;
   }
}
