package org.mozilla.javascript;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import org.mozilla.javascript.json.JsonParser;

public final class NativeJSON extends IdScriptableObject {
   static final long serialVersionUID = -4567599697595654984L;
   private static final Object JSON_TAG = "JSON";
   private static final int MAX_STRINGIFY_GAP_LENGTH = 10;
   private static final int Id_toSource = 1;
   private static final int Id_parse = 2;
   private static final int Id_stringify = 3;
   private static final int LAST_METHOD_ID = 3;
   private static final int MAX_ID = 3;

   static void init(Scriptable scope, boolean sealed) {
      NativeJSON obj = new NativeJSON();
      obj.activatePrototypeMap(3);
      obj.setPrototype(getObjectPrototype(scope));
      obj.setParentScope(scope);
      if (sealed) {
         obj.sealObject();
      }

      ScriptableObject.defineProperty(scope, "JSON", obj, 2);
   }

   private NativeJSON() {
      super();
   }

   public String getClassName() {
      return "JSON";
   }

   protected void initPrototypeId(int id) {
      if (id <= 3) {
         int arity;
         String name;
         switch (id) {
            case 1:
               arity = 0;
               name = "toSource";
               break;
            case 2:
               arity = 2;
               name = "parse";
               break;
            case 3:
               arity = 3;
               name = "stringify";
               break;
            default:
               throw new IllegalStateException(String.valueOf(id));
         }

         this.initPrototypeMethod(JSON_TAG, id, name, arity);
      } else {
         throw new IllegalStateException(String.valueOf(id));
      }
   }

   public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      if (!f.hasTag(JSON_TAG)) {
         return super.execIdCall(f, cx, scope, thisObj, args);
      } else {
         int methodId = f.methodId();
         switch (methodId) {
            case 1:
               return "JSON";
            case 2:
               String jtext = ScriptRuntime.toString(args, 0);
               Object reviver = null;
               if (args.length > 1) {
                  reviver = args[1];
               }

               if (reviver instanceof Callable) {
                  return parse(cx, scope, jtext, (Callable)reviver);
               }

               return parse(cx, scope, jtext);
            case 3:
               Object value = null;
               Object replacer = null;
               Object space = null;
               switch (args.length) {
                  case 3:
                  default:
                     space = args[2];
                  case 2:
                     replacer = args[1];
                  case 1:
                     value = args[0];
                  case 0:
                     return stringify(cx, scope, value, replacer, space);
               }
            default:
               throw new IllegalStateException(String.valueOf(methodId));
         }
      }
   }

   private static Object parse(Context cx, Scriptable scope, String jtext) {
      try {
         return (new JsonParser(cx, scope)).parseValue(jtext);
      } catch (JsonParser.ParseException ex) {
         throw ScriptRuntime.constructError("SyntaxError", ex.getMessage());
      }
   }

   public static Object parse(Context cx, Scriptable scope, String jtext, Callable reviver) {
      Object unfiltered = parse(cx, scope, jtext);
      Scriptable root = cx.newObject(scope);
      root.put("", root, unfiltered);
      return walk(cx, scope, reviver, root, "");
   }

   private static Object walk(Context cx, Scriptable scope, Callable reviver, Scriptable holder, Object name) {
      Object property;
      if (name instanceof Number) {
         property = holder.get(((Number)name).intValue(), holder);
      } else {
         property = holder.get((String)name, holder);
      }

      if (property instanceof Scriptable) {
         Scriptable val = (Scriptable)property;
         if (val instanceof NativeArray) {
            int len = (int)((NativeArray)val).getLength();

            for(int i = 0; i < len; ++i) {
               Object newElement = walk(cx, scope, reviver, val, i);
               if (newElement == Undefined.instance) {
                  val.delete(i);
               } else {
                  val.put(i, val, newElement);
               }
            }
         } else {
            Object[] keys = val.getIds();

            for(Object p : keys) {
               Object newElement = walk(cx, scope, reviver, val, p);
               if (newElement == Undefined.instance) {
                  if (p instanceof Number) {
                     val.delete(((Number)p).intValue());
                  } else {
                     val.delete((String)p);
                  }
               } else if (p instanceof Number) {
                  val.put(((Number)p).intValue(), val, newElement);
               } else {
                  val.put((String)p, val, newElement);
               }
            }
         }
      }

      return reviver.call(cx, scope, holder, new Object[]{name, property});
   }

   private static String repeat(char c, int count) {
      char[] chars = new char[count];
      Arrays.fill(chars, c);
      return new String(chars);
   }

   public static Object stringify(Context cx, Scriptable scope, Object value, Object replacer, Object space) {
      String indent = "";
      String gap = "";
      List<Object> propertyList = null;
      Callable replacerFunction = null;
      if (replacer instanceof Callable) {
         replacerFunction = (Callable)replacer;
      } else if (replacer instanceof NativeArray) {
         propertyList = new LinkedList();
         NativeArray replacerArray = (NativeArray)replacer;
         Integer[] arr$ = replacerArray.getIndexIds();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            int i = arr$[i$];
            Object v = replacerArray.get(i, replacerArray);
            if (!(v instanceof String) && !(v instanceof Number)) {
               if (v instanceof NativeString || v instanceof NativeNumber) {
                  propertyList.add(ScriptRuntime.toString(v));
               }
            } else {
               propertyList.add(v);
            }
         }
      }

      if (space instanceof NativeNumber) {
         space = ScriptRuntime.toNumber(space);
      } else if (space instanceof NativeString) {
         space = ScriptRuntime.toString(space);
      }

      if (space instanceof Number) {
         int gapLength = (int)ScriptRuntime.toInteger(space);
         gapLength = Math.min(10, gapLength);
         gap = gapLength > 0 ? repeat(' ', gapLength) : "";
         space = gapLength;
      } else if (space instanceof String) {
         gap = (String)space;
         if (gap.length() > 10) {
            gap = gap.substring(0, 10);
         }
      }

      StringifyState state = new StringifyState(cx, scope, indent, gap, replacerFunction, propertyList, space);
      ScriptableObject wrapper = new NativeObject();
      wrapper.setParentScope(scope);
      wrapper.setPrototype(ScriptableObject.getObjectPrototype(scope));
      wrapper.defineProperty("", (Object)value, 0);
      return str("", wrapper, state);
   }

   private static Object str(Object key, Scriptable holder, StringifyState state) {
      Object value = null;
      if (key instanceof String) {
         value = getProperty(holder, (String)key);
      } else {
         value = getProperty(holder, ((Number)key).intValue());
      }

      if (value instanceof Scriptable) {
         Object toJSON = getProperty((Scriptable)value, "toJSON");
         if (toJSON instanceof Callable) {
            value = callMethod(state.cx, (Scriptable)value, "toJSON", new Object[]{key});
         }
      }

      if (state.replacer != null) {
         value = state.replacer.call(state.cx, state.scope, holder, new Object[]{key, value});
      }

      if (value instanceof NativeNumber) {
         value = ScriptRuntime.toNumber(value);
      } else if (value instanceof NativeString) {
         value = ScriptRuntime.toString(value);
      } else if (value instanceof NativeBoolean) {
         value = ((NativeBoolean)value).getDefaultValue(ScriptRuntime.BooleanClass);
      }

      if (value == null) {
         return "null";
      } else if (value.equals(Boolean.TRUE)) {
         return "true";
      } else if (value.equals(Boolean.FALSE)) {
         return "false";
      } else if (value instanceof CharSequence) {
         return quote(value.toString());
      } else if (value instanceof Number) {
         double d = ((Number)value).doubleValue();
         return d == d && d != Double.POSITIVE_INFINITY && d != Double.NEGATIVE_INFINITY ? ScriptRuntime.toString(value) : "null";
      } else if (value instanceof Scriptable && !(value instanceof Callable)) {
         return value instanceof NativeArray ? ja((NativeArray)value, state) : jo((Scriptable)value, state);
      } else {
         return Undefined.instance;
      }
   }

   private static String join(Collection objs, String delimiter) {
      if (objs != null && !objs.isEmpty()) {
         Iterator<Object> iter = objs.iterator();
         if (!iter.hasNext()) {
            return "";
         } else {
            StringBuilder builder = new StringBuilder(iter.next().toString());

            while(iter.hasNext()) {
               builder.append(delimiter).append(iter.next().toString());
            }

            return builder.toString();
         }
      } else {
         return "";
      }
   }

   private static String jo(Scriptable value, StringifyState state) {
      if (state.stack.search(value) != -1) {
         throw ScriptRuntime.typeError0("msg.cyclic.value");
      } else {
         state.stack.push(value);
         String stepback = state.indent;
         state.indent = state.indent + state.gap;
         Object[] k = null;
         if (state.propertyList != null) {
            k = state.propertyList.toArray();
         } else {
            k = value.getIds();
         }

         List<Object> partial = new LinkedList();

         for(Object p : k) {
            Object strP = str(p, value, state);
            if (strP != Undefined.instance) {
               String member = quote(p.toString()) + ":";
               if (state.gap.length() > 0) {
                  member = member + " ";
               }

               member = member + strP;
               partial.add(member);
            }
         }

         String finalValue;
         if (partial.isEmpty()) {
            finalValue = "{}";
         } else if (state.gap.length() == 0) {
            finalValue = '{' + join(partial, ",") + '}';
         } else {
            String separator = ",\n" + state.indent;
            String properties = join(partial, separator);
            finalValue = "{\n" + state.indent + properties + '\n' + stepback + '}';
         }

         state.stack.pop();
         state.indent = stepback;
         return finalValue;
      }
   }

   private static String ja(NativeArray value, StringifyState state) {
      if (state.stack.search(value) != -1) {
         throw ScriptRuntime.typeError0("msg.cyclic.value");
      } else {
         state.stack.push(value);
         String stepback = state.indent;
         state.indent = state.indent + state.gap;
         List<Object> partial = new LinkedList();
         int len = (int)value.getLength();

         for(int index = 0; index < len; ++index) {
            Object strP = str(index, value, state);
            if (strP == Undefined.instance) {
               partial.add("null");
            } else {
               partial.add(strP);
            }
         }

         String finalValue;
         if (partial.isEmpty()) {
            finalValue = "[]";
         } else if (state.gap.length() == 0) {
            finalValue = '[' + join(partial, ",") + ']';
         } else {
            String separator = ",\n" + state.indent;
            String properties = join(partial, separator);
            finalValue = "[\n" + state.indent + properties + '\n' + stepback + ']';
         }

         state.stack.pop();
         state.indent = stepback;
         return finalValue;
      }
   }

   private static String quote(String string) {
      StringBuffer product = new StringBuffer(string.length() + 2);
      product.append('"');
      int length = string.length();

      for(int i = 0; i < length; ++i) {
         char c = string.charAt(i);
         switch (c) {
            case '\b':
               product.append("\\b");
               break;
            case '\t':
               product.append("\\t");
               break;
            case '\n':
               product.append("\\n");
               break;
            case '\f':
               product.append("\\f");
               break;
            case '\r':
               product.append("\\r");
               break;
            case '"':
               product.append("\\\"");
               break;
            case '\\':
               product.append("\\\\");
               break;
            default:
               if (c < ' ') {
                  product.append("\\u");
                  String hex = String.format("%04x", Integer.valueOf(c));
                  product.append(hex);
               } else {
                  product.append(c);
               }
         }
      }

      product.append('"');
      return product.toString();
   }

   protected int findPrototypeId(String s) {
      int id = 0;
      String X = null;
      switch (s.length()) {
         case 5:
            X = "parse";
            id = 2;
         case 6:
         case 7:
         default:
            break;
         case 8:
            X = "toSource";
            id = 1;
            break;
         case 9:
            X = "stringify";
            id = 3;
      }

      if (X != null && X != s && !X.equals(s)) {
         id = 0;
      }

      return id;
   }

   private static class StringifyState {
      Stack stack = new Stack();
      String indent;
      String gap;
      Callable replacer;
      List propertyList;
      Object space;
      Context cx;
      Scriptable scope;

      StringifyState(Context cx, Scriptable scope, String indent, String gap, Callable replacer, List propertyList, Object space) {
         super();
         this.cx = cx;
         this.scope = scope;
         this.indent = indent;
         this.gap = gap;
         this.replacer = replacer;
         this.propertyList = propertyList;
         this.space = space;
      }
   }
}
