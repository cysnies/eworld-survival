package org.mozilla.javascript.json;

import java.util.ArrayList;
import java.util.List;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;

public class JsonParser {
   private Context cx;
   private Scriptable scope;
   private int pos;
   private int length;
   private String src;

   public JsonParser(Context cx, Scriptable scope) {
      super();
      this.cx = cx;
      this.scope = scope;
   }

   public synchronized Object parseValue(String json) throws ParseException {
      if (json == null) {
         throw new ParseException("Input string may not be null");
      } else {
         this.pos = 0;
         this.length = json.length();
         this.src = json;
         Object value = this.readValue();
         this.consumeWhitespace();
         if (this.pos < this.length) {
            throw new ParseException("Expected end of stream at char " + this.pos);
         } else {
            return value;
         }
      }
   }

   private Object readValue() throws ParseException {
      this.consumeWhitespace();
      if (this.pos < this.length) {
         char c = this.src.charAt(this.pos++);
         switch (c) {
            case '"':
               return this.readString();
            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
               return this.readNumber(c);
            case '[':
               return this.readArray();
            case 'f':
               return this.readFalse();
            case 'n':
               return this.readNull();
            case 't':
               return this.readTrue();
            case '{':
               return this.readObject();
            default:
               throw new ParseException("Unexpected token: " + c);
         }
      } else {
         throw new ParseException("Empty JSON string");
      }
   }

   private Object readObject() throws ParseException {
      Scriptable object = this.cx.newObject(this.scope);
      boolean needsComma = false;
      this.consumeWhitespace();

      for(; this.pos < this.length; this.consumeWhitespace()) {
         char c = this.src.charAt(this.pos++);
         switch (c) {
            case '"':
               if (needsComma) {
                  throw new ParseException("Missing comma in object literal");
               }

               String id = this.readString();
               this.consume(':');
               Object value = this.readValue();
               long index = ScriptRuntime.indexFromString(id);
               if (index < 0L) {
                  object.put(id, object, value);
               } else {
                  object.put((int)index, object, value);
               }

               needsComma = true;
               break;
            case ',':
               if (!needsComma) {
                  throw new ParseException("Unexpected comma in object literal");
               }

               needsComma = false;
               break;
            case '}':
               return object;
            default:
               throw new ParseException("Unexpected token in object literal");
         }
      }

      throw new ParseException("Unterminated object literal");
   }

   private Object readArray() throws ParseException {
      List<Object> list = new ArrayList();
      boolean needsComma = false;
      this.consumeWhitespace();

      for(; this.pos < this.length; this.consumeWhitespace()) {
         char c = this.src.charAt(this.pos);
         switch (c) {
            case ',':
               if (!needsComma) {
                  throw new ParseException("Unexpected comma in array literal");
               }

               needsComma = false;
               ++this.pos;
               break;
            case ']':
               ++this.pos;
               return this.cx.newArray(this.scope, list.toArray());
            default:
               if (needsComma) {
                  throw new ParseException("Missing comma in array literal");
               }

               list.add(this.readValue());
               needsComma = true;
         }
      }

      throw new ParseException("Unterminated array literal");
   }

   private String readString() throws ParseException {
      StringBuilder b = new StringBuilder();

      while(this.pos < this.length) {
         char c = this.src.charAt(this.pos++);
         if (c <= 31) {
            throw new ParseException("String contains control character");
         }

         switch (c) {
            case '"':
               return b.toString();
            case '\\':
               if (this.pos >= this.length) {
                  throw new ParseException("Unterminated string");
               }

               c = this.src.charAt(this.pos++);
               switch (c) {
                  case '"':
                     b.append('"');
                     continue;
                  case '/':
                     b.append('/');
                     continue;
                  case '\\':
                     b.append('\\');
                     continue;
                  case 'b':
                     b.append('\b');
                     continue;
                  case 'f':
                     b.append('\f');
                     continue;
                  case 'n':
                     b.append('\n');
                     continue;
                  case 'r':
                     b.append('\r');
                     continue;
                  case 't':
                     b.append('\t');
                     continue;
                  case 'u':
                     if (this.length - this.pos < 5) {
                        throw new ParseException("Invalid character code: \\u" + this.src.substring(this.pos));
                     }

                     try {
                        b.append((char)Integer.parseInt(this.src.substring(this.pos, this.pos + 4), 16));
                        this.pos += 4;
                        continue;
                     } catch (NumberFormatException var4) {
                        throw new ParseException("Invalid character code: " + this.src.substring(this.pos, this.pos + 4));
                     }
                  default:
                     throw new ParseException("Unexcpected character in string: '\\" + c + "'");
               }
            default:
               b.append(c);
         }
      }

      throw new ParseException("Unterminated string literal");
   }

   private Number readNumber(char first) throws ParseException {
      StringBuilder b = new StringBuilder();
      b.append(first);

      while(this.pos < this.length) {
         char c = this.src.charAt(this.pos);
         if (!Character.isDigit(c) && c != '-' && c != '+' && c != '.' && c != 'e' && c != 'E') {
            break;
         }

         ++this.pos;
         b.append(c);
      }

      String num = b.toString();
      int numLength = num.length();

      try {
         for(int i = 0; i < numLength; ++i) {
            char c = num.charAt(i);
            if (Character.isDigit(c)) {
               if (c == '0' && numLength > i + 1 && Character.isDigit(num.charAt(i + 1))) {
                  throw new ParseException("Unsupported number format: " + num);
               }
               break;
            }
         }

         double dval = Double.parseDouble(num);
         int ival = (int)dval;
         return (Number)((double)ival == dval ? ival : dval);
      } catch (NumberFormatException var10) {
         throw new ParseException("Unsupported number format: " + num);
      }
   }

   private Boolean readTrue() throws ParseException {
      if (this.length - this.pos >= 3 && this.src.charAt(this.pos) == 'r' && this.src.charAt(this.pos + 1) == 'u' && this.src.charAt(this.pos + 2) == 'e') {
         this.pos += 3;
         return Boolean.TRUE;
      } else {
         throw new ParseException("Unexpected token: t");
      }
   }

   private Boolean readFalse() throws ParseException {
      if (this.length - this.pos >= 4 && this.src.charAt(this.pos) == 'a' && this.src.charAt(this.pos + 1) == 'l' && this.src.charAt(this.pos + 2) == 's' && this.src.charAt(this.pos + 3) == 'e') {
         this.pos += 4;
         return Boolean.FALSE;
      } else {
         throw new ParseException("Unexpected token: f");
      }
   }

   private Object readNull() throws ParseException {
      if (this.length - this.pos >= 3 && this.src.charAt(this.pos) == 'u' && this.src.charAt(this.pos + 1) == 'l' && this.src.charAt(this.pos + 2) == 'l') {
         this.pos += 3;
         return null;
      } else {
         throw new ParseException("Unexpected token: n");
      }
   }

   private void consumeWhitespace() {
      while(this.pos < this.length) {
         char c = this.src.charAt(this.pos);
         switch (c) {
            case '\t':
            case '\n':
            case '\r':
            case ' ':
               ++this.pos;
               break;
            default:
               return;
         }
      }

   }

   private void consume(char token) throws ParseException {
      this.consumeWhitespace();
      if (this.pos >= this.length) {
         throw new ParseException("Expected " + token + " but reached end of stream");
      } else {
         char c = this.src.charAt(this.pos++);
         if (c != token) {
            throw new ParseException("Expected " + token + " found " + c);
         }
      }
   }

   public static class ParseException extends Exception {
      static final long serialVersionUID = 4804542791749920772L;

      ParseException(String message) {
         super(message);
      }

      ParseException(Exception cause) {
         super(cause);
      }
   }
}
