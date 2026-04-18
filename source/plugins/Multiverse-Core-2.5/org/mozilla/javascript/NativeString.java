package org.mozilla.javascript;

import java.text.Collator;

final class NativeString extends IdScriptableObject {
   static final long serialVersionUID = 920268368584188687L;
   private static final Object STRING_TAG = "String";
   private static final int Id_length = 1;
   private static final int MAX_INSTANCE_ID = 1;
   private static final int ConstructorId_fromCharCode = -1;
   private static final int Id_constructor = 1;
   private static final int Id_toString = 2;
   private static final int Id_toSource = 3;
   private static final int Id_valueOf = 4;
   private static final int Id_charAt = 5;
   private static final int Id_charCodeAt = 6;
   private static final int Id_indexOf = 7;
   private static final int Id_lastIndexOf = 8;
   private static final int Id_split = 9;
   private static final int Id_substring = 10;
   private static final int Id_toLowerCase = 11;
   private static final int Id_toUpperCase = 12;
   private static final int Id_substr = 13;
   private static final int Id_concat = 14;
   private static final int Id_slice = 15;
   private static final int Id_bold = 16;
   private static final int Id_italics = 17;
   private static final int Id_fixed = 18;
   private static final int Id_strike = 19;
   private static final int Id_small = 20;
   private static final int Id_big = 21;
   private static final int Id_blink = 22;
   private static final int Id_sup = 23;
   private static final int Id_sub = 24;
   private static final int Id_fontsize = 25;
   private static final int Id_fontcolor = 26;
   private static final int Id_link = 27;
   private static final int Id_anchor = 28;
   private static final int Id_equals = 29;
   private static final int Id_equalsIgnoreCase = 30;
   private static final int Id_match = 31;
   private static final int Id_search = 32;
   private static final int Id_replace = 33;
   private static final int Id_localeCompare = 34;
   private static final int Id_toLocaleLowerCase = 35;
   private static final int Id_toLocaleUpperCase = 36;
   private static final int Id_trim = 37;
   private static final int MAX_PROTOTYPE_ID = 37;
   private static final int ConstructorId_charAt = -5;
   private static final int ConstructorId_charCodeAt = -6;
   private static final int ConstructorId_indexOf = -7;
   private static final int ConstructorId_lastIndexOf = -8;
   private static final int ConstructorId_split = -9;
   private static final int ConstructorId_substring = -10;
   private static final int ConstructorId_toLowerCase = -11;
   private static final int ConstructorId_toUpperCase = -12;
   private static final int ConstructorId_substr = -13;
   private static final int ConstructorId_concat = -14;
   private static final int ConstructorId_slice = -15;
   private static final int ConstructorId_equalsIgnoreCase = -30;
   private static final int ConstructorId_match = -31;
   private static final int ConstructorId_search = -32;
   private static final int ConstructorId_replace = -33;
   private static final int ConstructorId_localeCompare = -34;
   private static final int ConstructorId_toLocaleLowerCase = -35;
   private CharSequence string;

   static void init(Scriptable scope, boolean sealed) {
      NativeString obj = new NativeString("");
      obj.exportAsJSClass(37, scope, sealed);
   }

   NativeString(CharSequence s) {
      super();
      this.string = s;
   }

   public String getClassName() {
      return "String";
   }

   protected int getMaxInstanceId() {
      return 1;
   }

   protected int findInstanceIdInfo(String s) {
      return s.equals("length") ? instanceIdInfo(7, 1) : super.findInstanceIdInfo(s);
   }

   protected String getInstanceIdName(int id) {
      return id == 1 ? "length" : super.getInstanceIdName(id);
   }

   protected Object getInstanceIdValue(int id) {
      return id == 1 ? ScriptRuntime.wrapInt(this.string.length()) : super.getInstanceIdValue(id);
   }

   protected void fillConstructorProperties(IdFunctionObject ctor) {
      this.addIdFunctionProperty(ctor, STRING_TAG, -1, "fromCharCode", 1);
      this.addIdFunctionProperty(ctor, STRING_TAG, -5, "charAt", 2);
      this.addIdFunctionProperty(ctor, STRING_TAG, -6, "charCodeAt", 2);
      this.addIdFunctionProperty(ctor, STRING_TAG, -7, "indexOf", 2);
      this.addIdFunctionProperty(ctor, STRING_TAG, -8, "lastIndexOf", 2);
      this.addIdFunctionProperty(ctor, STRING_TAG, -9, "split", 3);
      this.addIdFunctionProperty(ctor, STRING_TAG, -10, "substring", 3);
      this.addIdFunctionProperty(ctor, STRING_TAG, -11, "toLowerCase", 1);
      this.addIdFunctionProperty(ctor, STRING_TAG, -12, "toUpperCase", 1);
      this.addIdFunctionProperty(ctor, STRING_TAG, -13, "substr", 3);
      this.addIdFunctionProperty(ctor, STRING_TAG, -14, "concat", 2);
      this.addIdFunctionProperty(ctor, STRING_TAG, -15, "slice", 3);
      this.addIdFunctionProperty(ctor, STRING_TAG, -30, "equalsIgnoreCase", 2);
      this.addIdFunctionProperty(ctor, STRING_TAG, -31, "match", 2);
      this.addIdFunctionProperty(ctor, STRING_TAG, -32, "search", 2);
      this.addIdFunctionProperty(ctor, STRING_TAG, -33, "replace", 2);
      this.addIdFunctionProperty(ctor, STRING_TAG, -34, "localeCompare", 2);
      this.addIdFunctionProperty(ctor, STRING_TAG, -35, "toLocaleLowerCase", 1);
      super.fillConstructorProperties(ctor);
   }

   protected void initPrototypeId(int id) {
      int arity;
      String s;
      switch (id) {
         case 1:
            arity = 1;
            s = "constructor";
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
            arity = 0;
            s = "valueOf";
            break;
         case 5:
            arity = 1;
            s = "charAt";
            break;
         case 6:
            arity = 1;
            s = "charCodeAt";
            break;
         case 7:
            arity = 1;
            s = "indexOf";
            break;
         case 8:
            arity = 1;
            s = "lastIndexOf";
            break;
         case 9:
            arity = 2;
            s = "split";
            break;
         case 10:
            arity = 2;
            s = "substring";
            break;
         case 11:
            arity = 0;
            s = "toLowerCase";
            break;
         case 12:
            arity = 0;
            s = "toUpperCase";
            break;
         case 13:
            arity = 2;
            s = "substr";
            break;
         case 14:
            arity = 1;
            s = "concat";
            break;
         case 15:
            arity = 2;
            s = "slice";
            break;
         case 16:
            arity = 0;
            s = "bold";
            break;
         case 17:
            arity = 0;
            s = "italics";
            break;
         case 18:
            arity = 0;
            s = "fixed";
            break;
         case 19:
            arity = 0;
            s = "strike";
            break;
         case 20:
            arity = 0;
            s = "small";
            break;
         case 21:
            arity = 0;
            s = "big";
            break;
         case 22:
            arity = 0;
            s = "blink";
            break;
         case 23:
            arity = 0;
            s = "sup";
            break;
         case 24:
            arity = 0;
            s = "sub";
            break;
         case 25:
            arity = 0;
            s = "fontsize";
            break;
         case 26:
            arity = 0;
            s = "fontcolor";
            break;
         case 27:
            arity = 0;
            s = "link";
            break;
         case 28:
            arity = 0;
            s = "anchor";
            break;
         case 29:
            arity = 1;
            s = "equals";
            break;
         case 30:
            arity = 1;
            s = "equalsIgnoreCase";
            break;
         case 31:
            arity = 1;
            s = "match";
            break;
         case 32:
            arity = 1;
            s = "search";
            break;
         case 33:
            arity = 1;
            s = "replace";
            break;
         case 34:
            arity = 1;
            s = "localeCompare";
            break;
         case 35:
            arity = 0;
            s = "toLocaleLowerCase";
            break;
         case 36:
            arity = 0;
            s = "toLocaleUpperCase";
            break;
         case 37:
            arity = 0;
            s = "trim";
            break;
         default:
            throw new IllegalArgumentException(String.valueOf(id));
      }

      this.initPrototypeMethod(STRING_TAG, id, s, arity);
   }

   public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      if (!f.hasTag(STRING_TAG)) {
         return super.execIdCall(f, cx, scope, thisObj, args);
      } else {
         int id = f.methodId();

         while(true) {
            switch (id) {
               case -35:
               case -34:
               case -33:
               case -32:
               case -31:
               case -30:
               case -15:
               case -14:
               case -13:
               case -12:
               case -11:
               case -10:
               case -9:
               case -8:
               case -7:
               case -6:
               case -5:
                  if (args.length <= 0) {
                     thisObj = ScriptRuntime.toObject(scope, ScriptRuntime.toCharSequence(thisObj));
                  } else {
                     thisObj = ScriptRuntime.toObject(scope, ScriptRuntime.toCharSequence(args[0]));
                     Object[] newArgs = new Object[args.length - 1];

                     for(int i = 0; i < newArgs.length; ++i) {
                        newArgs[i] = args[i + 1];
                     }

                     args = newArgs;
                  }

                  id = -id;
                  break;
               case -29:
               case -28:
               case -27:
               case -26:
               case -25:
               case -24:
               case -23:
               case -22:
               case -21:
               case -20:
               case -19:
               case -18:
               case -17:
               case -16:
               case -4:
               case -3:
               case -2:
               case 0:
               default:
                  throw new IllegalArgumentException(String.valueOf(id));
               case -1:
                  int N = args.length;
                  if (N < 1) {
                     return "";
                  }

                  StringBuffer sb = new StringBuffer(N);

                  for(int i = 0; i != N; ++i) {
                     sb.append(ScriptRuntime.toUint16(args[i]));
                  }

                  return sb.toString();
               case 1:
                  CharSequence s = (CharSequence)(args.length >= 1 ? ScriptRuntime.toCharSequence(args[0]) : "");
                  if (thisObj == null) {
                     return new NativeString(s);
                  }

                  return s instanceof String ? s : s.toString();
               case 2:
               case 4:
                  CharSequence cs = realThis(thisObj, f).string;
                  return cs instanceof String ? cs : cs.toString();
               case 3:
                  CharSequence s = realThis(thisObj, f).string;
                  return "(new String(\"" + ScriptRuntime.escapeString(s.toString()) + "\"))";
               case 5:
               case 6:
                  CharSequence target = ScriptRuntime.toCharSequence(thisObj);
                  double pos = ScriptRuntime.toInteger(args, 0);
                  if (!(pos < (double)0.0F) && !(pos >= (double)target.length())) {
                     char c = target.charAt((int)pos);
                     if (id == 5) {
                        return String.valueOf(c);
                     }

                     return ScriptRuntime.wrapInt(c);
                  }

                  if (id == 5) {
                     return "";
                  }

                  return ScriptRuntime.NaNobj;
               case 7:
                  return ScriptRuntime.wrapInt(js_indexOf(ScriptRuntime.toString(thisObj), args));
               case 8:
                  return ScriptRuntime.wrapInt(js_lastIndexOf(ScriptRuntime.toString(thisObj), args));
               case 9:
                  return ScriptRuntime.checkRegExpProxy(cx).js_split(cx, scope, ScriptRuntime.toString(thisObj), args);
               case 10:
                  return js_substring(cx, ScriptRuntime.toCharSequence(thisObj), args);
               case 11:
                  return ScriptRuntime.toString(thisObj).toLowerCase(ScriptRuntime.ROOT_LOCALE);
               case 12:
                  return ScriptRuntime.toString(thisObj).toUpperCase(ScriptRuntime.ROOT_LOCALE);
               case 13:
                  return js_substr(ScriptRuntime.toCharSequence(thisObj), args);
               case 14:
                  return js_concat(ScriptRuntime.toString(thisObj), args);
               case 15:
                  return js_slice(ScriptRuntime.toCharSequence(thisObj), args);
               case 16:
                  return tagify(thisObj, "b", (String)null, (Object[])null);
               case 17:
                  return tagify(thisObj, "i", (String)null, (Object[])null);
               case 18:
                  return tagify(thisObj, "tt", (String)null, (Object[])null);
               case 19:
                  return tagify(thisObj, "strike", (String)null, (Object[])null);
               case 20:
                  return tagify(thisObj, "small", (String)null, (Object[])null);
               case 21:
                  return tagify(thisObj, "big", (String)null, (Object[])null);
               case 22:
                  return tagify(thisObj, "blink", (String)null, (Object[])null);
               case 23:
                  return tagify(thisObj, "sup", (String)null, (Object[])null);
               case 24:
                  return tagify(thisObj, "sub", (String)null, (Object[])null);
               case 25:
                  return tagify(thisObj, "font", "size", args);
               case 26:
                  return tagify(thisObj, "font", "color", args);
               case 27:
                  return tagify(thisObj, "a", "href", args);
               case 28:
                  return tagify(thisObj, "a", "name", args);
               case 29:
               case 30:
                  String s1 = ScriptRuntime.toString(thisObj);
                  String s2 = ScriptRuntime.toString(args, 0);
                  return ScriptRuntime.wrapBoolean(id == 29 ? s1.equals(s2) : s1.equalsIgnoreCase(s2));
               case 31:
               case 32:
               case 33:
                  int actionType;
                  if (id == 31) {
                     actionType = 1;
                  } else if (id == 32) {
                     actionType = 3;
                  } else {
                     actionType = 2;
                  }

                  return ScriptRuntime.checkRegExpProxy(cx).action(cx, scope, thisObj, args, actionType);
               case 34:
                  Collator collator = Collator.getInstance(cx.getLocale());
                  collator.setStrength(3);
                  collator.setDecomposition(1);
                  return ScriptRuntime.wrapNumber((double)collator.compare(ScriptRuntime.toString(thisObj), ScriptRuntime.toString(args, 0)));
               case 35:
                  return ScriptRuntime.toString(thisObj).toLowerCase(cx.getLocale());
               case 36:
                  return ScriptRuntime.toString(thisObj).toUpperCase(cx.getLocale());
               case 37:
                  String str = ScriptRuntime.toString(thisObj);
                  char[] chars = str.toCharArray();

                  int start;
                  for(start = 0; start < chars.length && ScriptRuntime.isJSWhitespaceOrLineTerminator(chars[start]); ++start) {
                  }

                  int end;
                  for(end = chars.length; end > start && ScriptRuntime.isJSWhitespaceOrLineTerminator(chars[end - 1]); --end) {
                  }

                  return str.substring(start, end);
            }
         }
      }
   }

   private static NativeString realThis(Scriptable thisObj, IdFunctionObject f) {
      if (!(thisObj instanceof NativeString)) {
         throw incompatibleCallError(f);
      } else {
         return (NativeString)thisObj;
      }
   }

   private static String tagify(Object thisObj, String tag, String attribute, Object[] args) {
      String str = ScriptRuntime.toString(thisObj);
      StringBuffer result = new StringBuffer();
      result.append('<');
      result.append(tag);
      if (attribute != null) {
         result.append(' ');
         result.append(attribute);
         result.append("=\"");
         result.append(ScriptRuntime.toString(args, 0));
         result.append('"');
      }

      result.append('>');
      result.append(str);
      result.append("</");
      result.append(tag);
      result.append('>');
      return result.toString();
   }

   public CharSequence toCharSequence() {
      return this.string;
   }

   public String toString() {
      return this.string instanceof String ? (String)this.string : this.string.toString();
   }

   public Object get(int index, Scriptable start) {
      return 0 <= index && index < this.string.length() ? String.valueOf(this.string.charAt(index)) : super.get(index, start);
   }

   public void put(int index, Scriptable start, Object value) {
      if (0 > index || index >= this.string.length()) {
         super.put(index, start, value);
      }
   }

   private static int js_indexOf(String target, Object[] args) {
      String search = ScriptRuntime.toString(args, 0);
      double begin = ScriptRuntime.toInteger(args, 1);
      if (begin > (double)target.length()) {
         return -1;
      } else {
         if (begin < (double)0.0F) {
            begin = (double)0.0F;
         }

         return target.indexOf(search, (int)begin);
      }
   }

   private static int js_lastIndexOf(String target, Object[] args) {
      String search = ScriptRuntime.toString(args, 0);
      double end = ScriptRuntime.toNumber(args, 1);
      if (end == end && !(end > (double)target.length())) {
         if (end < (double)0.0F) {
            end = (double)0.0F;
         }
      } else {
         end = (double)target.length();
      }

      return target.lastIndexOf(search, (int)end);
   }

   private static CharSequence js_substring(Context cx, CharSequence target, Object[] args) {
      int length = target.length();
      double start = ScriptRuntime.toInteger(args, 0);
      if (start < (double)0.0F) {
         start = (double)0.0F;
      } else if (start > (double)length) {
         start = (double)length;
      }

      double end;
      if (args.length > 1 && args[1] != Undefined.instance) {
         end = ScriptRuntime.toInteger(args[1]);
         if (end < (double)0.0F) {
            end = (double)0.0F;
         } else if (end > (double)length) {
            end = (double)length;
         }

         if (end < start) {
            if (cx.getLanguageVersion() != 120) {
               double temp = start;
               start = end;
               end = temp;
            } else {
               end = start;
            }
         }
      } else {
         end = (double)length;
      }

      return target.subSequence((int)start, (int)end);
   }

   int getLength() {
      return this.string.length();
   }

   private static CharSequence js_substr(CharSequence target, Object[] args) {
      if (args.length < 1) {
         return target;
      } else {
         double begin = ScriptRuntime.toInteger(args[0]);
         int length = target.length();
         if (begin < (double)0.0F) {
            begin += (double)length;
            if (begin < (double)0.0F) {
               begin = (double)0.0F;
            }
         } else if (begin > (double)length) {
            begin = (double)length;
         }

         double end;
         if (args.length == 1) {
            end = (double)length;
         } else {
            end = ScriptRuntime.toInteger(args[1]);
            if (end < (double)0.0F) {
               end = (double)0.0F;
            }

            end += begin;
            if (end > (double)length) {
               end = (double)length;
            }
         }

         return target.subSequence((int)begin, (int)end);
      }
   }

   private static String js_concat(String target, Object[] args) {
      int N = args.length;
      if (N == 0) {
         return target;
      } else if (N == 1) {
         String arg = ScriptRuntime.toString(args[0]);
         return target.concat(arg);
      } else {
         int size = target.length();
         String[] argsAsStrings = new String[N];

         for(int i = 0; i != N; ++i) {
            String s = ScriptRuntime.toString(args[i]);
            argsAsStrings[i] = s;
            size += s.length();
         }

         StringBuffer result = new StringBuffer(size);
         result.append(target);

         for(int i = 0; i != N; ++i) {
            result.append(argsAsStrings[i]);
         }

         return result.toString();
      }
   }

   private static CharSequence js_slice(CharSequence target, Object[] args) {
      if (args.length != 0) {
         double begin = ScriptRuntime.toInteger(args[0]);
         int length = target.length();
         if (begin < (double)0.0F) {
            begin += (double)length;
            if (begin < (double)0.0F) {
               begin = (double)0.0F;
            }
         } else if (begin > (double)length) {
            begin = (double)length;
         }

         double end;
         if (args.length == 1) {
            end = (double)length;
         } else {
            end = ScriptRuntime.toInteger(args[1]);
            if (end < (double)0.0F) {
               end += (double)length;
               if (end < (double)0.0F) {
                  end = (double)0.0F;
               }
            } else if (end > (double)length) {
               end = (double)length;
            }

            if (end < begin) {
               end = begin;
            }
         }

         return target.subSequence((int)begin, (int)end);
      } else {
         return target;
      }
   }

   protected int findPrototypeId(String s) {
      int id;
      String X;
      id = 0;
      X = null;
      label109:
      switch (s.length()) {
         case 3:
            int c = s.charAt(2);
            if (c == 98) {
               if (s.charAt(0) == 's' && s.charAt(1) == 'u') {
                  id = 24;
                  return id;
               }
            } else if (c == 103) {
               if (s.charAt(0) == 'b' && s.charAt(1) == 'i') {
                  id = 21;
                  return id;
               }
            } else if (c == 112 && s.charAt(0) == 's' && s.charAt(1) == 'u') {
               id = 23;
               return id;
            }
            break;
         case 4:
            int var7 = s.charAt(0);
            if (var7 == 'b') {
               X = "bold";
               id = 16;
            } else if (var7 == 'l') {
               X = "link";
               id = 27;
            } else if (var7 == 't') {
               X = "trim";
               id = 37;
            }
            break;
         case 5:
            switch (s.charAt(4)) {
               case 'd':
                  X = "fixed";
                  id = 18;
                  break label109;
               case 'e':
                  X = "slice";
                  id = 15;
               case 'f':
               case 'g':
               case 'i':
               case 'j':
               case 'm':
               case 'n':
               case 'o':
               case 'p':
               case 'q':
               case 'r':
               case 's':
               default:
                  break label109;
               case 'h':
                  X = "match";
                  id = 31;
                  break label109;
               case 'k':
                  X = "blink";
                  id = 22;
                  break label109;
               case 'l':
                  X = "small";
                  id = 20;
                  break label109;
               case 't':
                  X = "split";
                  id = 9;
                  break label109;
            }
         case 6:
            switch (s.charAt(1)) {
               case 'e':
                  X = "search";
                  id = 32;
               case 'f':
               case 'g':
               case 'i':
               case 'j':
               case 'k':
               case 'l':
               case 'm':
               case 'p':
               case 'r':
               case 's':
               default:
                  break label109;
               case 'h':
                  X = "charAt";
                  id = 5;
                  break label109;
               case 'n':
                  X = "anchor";
                  id = 28;
                  break label109;
               case 'o':
                  X = "concat";
                  id = 14;
                  break label109;
               case 'q':
                  X = "equals";
                  id = 29;
                  break label109;
               case 't':
                  X = "strike";
                  id = 19;
                  break label109;
               case 'u':
                  X = "substr";
                  id = 13;
                  break label109;
            }
         case 7:
            switch (s.charAt(1)) {
               case 'a':
                  X = "valueOf";
                  id = 4;
                  break label109;
               case 'e':
                  X = "replace";
                  id = 33;
                  break label109;
               case 'n':
                  X = "indexOf";
                  id = 7;
                  break label109;
               case 't':
                  X = "italics";
                  id = 17;
               default:
                  break label109;
            }
         case 8:
            int var6 = s.charAt(4);
            if (var6 == 'r') {
               X = "toString";
               id = 2;
            } else if (var6 == 's') {
               X = "fontsize";
               id = 25;
            } else if (var6 == 'u') {
               X = "toSource";
               id = 3;
            }
            break;
         case 9:
            int var5 = s.charAt(0);
            if (var5 == 'f') {
               X = "fontcolor";
               id = 26;
            } else if (var5 == 's') {
               X = "substring";
               id = 10;
            }
            break;
         case 10:
            X = "charCodeAt";
            id = 6;
            break;
         case 11:
            switch (s.charAt(2)) {
               case 'L':
                  X = "toLowerCase";
                  id = 11;
                  break;
               case 'U':
                  X = "toUpperCase";
                  id = 12;
                  break;
               case 'n':
                  X = "constructor";
                  id = 1;
                  break;
               case 's':
                  X = "lastIndexOf";
                  id = 8;
            }
         case 12:
         case 14:
         case 15:
         default:
            break;
         case 13:
            X = "localeCompare";
            id = 34;
            break;
         case 16:
            X = "equalsIgnoreCase";
            id = 30;
            break;
         case 17:
            int c = s.charAt(8);
            if (c == 'L') {
               X = "toLocaleLowerCase";
               id = 35;
            } else if (c == 'U') {
               X = "toLocaleUpperCase";
               id = 36;
            }
      }

      if (X != null && X != s && !X.equals(s)) {
         id = 0;
      }

      return id;
   }
}
