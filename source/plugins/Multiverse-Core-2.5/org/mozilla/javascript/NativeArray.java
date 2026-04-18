package org.mozilla.javascript;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class NativeArray extends IdScriptableObject implements List {
   static final long serialVersionUID = 7331366857676127338L;
   private static final Object ARRAY_TAG = "Array";
   private static final Integer NEGATIVE_ONE = -1;
   private static final int Id_length = 1;
   private static final int MAX_INSTANCE_ID = 1;
   private static final int Id_constructor = 1;
   private static final int Id_toString = 2;
   private static final int Id_toLocaleString = 3;
   private static final int Id_toSource = 4;
   private static final int Id_join = 5;
   private static final int Id_reverse = 6;
   private static final int Id_sort = 7;
   private static final int Id_push = 8;
   private static final int Id_pop = 9;
   private static final int Id_shift = 10;
   private static final int Id_unshift = 11;
   private static final int Id_splice = 12;
   private static final int Id_concat = 13;
   private static final int Id_slice = 14;
   private static final int Id_indexOf = 15;
   private static final int Id_lastIndexOf = 16;
   private static final int Id_every = 17;
   private static final int Id_filter = 18;
   private static final int Id_forEach = 19;
   private static final int Id_map = 20;
   private static final int Id_some = 21;
   private static final int Id_reduce = 22;
   private static final int Id_reduceRight = 23;
   private static final int MAX_PROTOTYPE_ID = 23;
   private static final int ConstructorId_join = -5;
   private static final int ConstructorId_reverse = -6;
   private static final int ConstructorId_sort = -7;
   private static final int ConstructorId_push = -8;
   private static final int ConstructorId_pop = -9;
   private static final int ConstructorId_shift = -10;
   private static final int ConstructorId_unshift = -11;
   private static final int ConstructorId_splice = -12;
   private static final int ConstructorId_concat = -13;
   private static final int ConstructorId_slice = -14;
   private static final int ConstructorId_indexOf = -15;
   private static final int ConstructorId_lastIndexOf = -16;
   private static final int ConstructorId_every = -17;
   private static final int ConstructorId_filter = -18;
   private static final int ConstructorId_forEach = -19;
   private static final int ConstructorId_map = -20;
   private static final int ConstructorId_some = -21;
   private static final int ConstructorId_reduce = -22;
   private static final int ConstructorId_reduceRight = -23;
   private static final int ConstructorId_isArray = -24;
   private long length;
   private int lengthAttr = 6;
   private Object[] dense;
   private boolean denseOnly;
   private static int maximumInitialCapacity = 10000;
   private static final int DEFAULT_INITIAL_CAPACITY = 10;
   private static final double GROW_FACTOR = (double)1.5F;
   private static final int MAX_PRE_GROW_SIZE = 1431655764;

   static void init(Scriptable scope, boolean sealed) {
      NativeArray obj = new NativeArray(0L);
      obj.exportAsJSClass(23, scope, sealed);
   }

   static int getMaximumInitialCapacity() {
      return maximumInitialCapacity;
   }

   static void setMaximumInitialCapacity(int maximumInitialCapacity) {
      NativeArray.maximumInitialCapacity = maximumInitialCapacity;
   }

   public NativeArray(long lengthArg) {
      super();
      this.denseOnly = lengthArg <= (long)maximumInitialCapacity;
      if (this.denseOnly) {
         int intLength = (int)lengthArg;
         if (intLength < 10) {
            intLength = 10;
         }

         this.dense = new Object[intLength];
         Arrays.fill(this.dense, Scriptable.NOT_FOUND);
      }

      this.length = lengthArg;
   }

   public NativeArray(Object[] array) {
      super();
      this.denseOnly = true;
      this.dense = array;
      this.length = (long)array.length;
   }

   public String getClassName() {
      return "Array";
   }

   protected int getMaxInstanceId() {
      return 1;
   }

   protected void setInstanceIdAttributes(int id, int attr) {
      if (id == 1) {
         this.lengthAttr = attr;
      }

   }

   protected int findInstanceIdInfo(String s) {
      return s.equals("length") ? instanceIdInfo(this.lengthAttr, 1) : super.findInstanceIdInfo(s);
   }

   protected String getInstanceIdName(int id) {
      return id == 1 ? "length" : super.getInstanceIdName(id);
   }

   protected Object getInstanceIdValue(int id) {
      return id == 1 ? ScriptRuntime.wrapNumber((double)this.length) : super.getInstanceIdValue(id);
   }

   protected void setInstanceIdValue(int id, Object value) {
      if (id == 1) {
         this.setLength(value);
      } else {
         super.setInstanceIdValue(id, value);
      }
   }

   protected void fillConstructorProperties(IdFunctionObject ctor) {
      this.addIdFunctionProperty(ctor, ARRAY_TAG, -5, "join", 1);
      this.addIdFunctionProperty(ctor, ARRAY_TAG, -6, "reverse", 0);
      this.addIdFunctionProperty(ctor, ARRAY_TAG, -7, "sort", 1);
      this.addIdFunctionProperty(ctor, ARRAY_TAG, -8, "push", 1);
      this.addIdFunctionProperty(ctor, ARRAY_TAG, -9, "pop", 0);
      this.addIdFunctionProperty(ctor, ARRAY_TAG, -10, "shift", 0);
      this.addIdFunctionProperty(ctor, ARRAY_TAG, -11, "unshift", 1);
      this.addIdFunctionProperty(ctor, ARRAY_TAG, -12, "splice", 2);
      this.addIdFunctionProperty(ctor, ARRAY_TAG, -13, "concat", 1);
      this.addIdFunctionProperty(ctor, ARRAY_TAG, -14, "slice", 2);
      this.addIdFunctionProperty(ctor, ARRAY_TAG, -15, "indexOf", 1);
      this.addIdFunctionProperty(ctor, ARRAY_TAG, -16, "lastIndexOf", 1);
      this.addIdFunctionProperty(ctor, ARRAY_TAG, -17, "every", 1);
      this.addIdFunctionProperty(ctor, ARRAY_TAG, -18, "filter", 1);
      this.addIdFunctionProperty(ctor, ARRAY_TAG, -19, "forEach", 1);
      this.addIdFunctionProperty(ctor, ARRAY_TAG, -20, "map", 1);
      this.addIdFunctionProperty(ctor, ARRAY_TAG, -21, "some", 1);
      this.addIdFunctionProperty(ctor, ARRAY_TAG, -22, "reduce", 1);
      this.addIdFunctionProperty(ctor, ARRAY_TAG, -23, "reduceRight", 1);
      this.addIdFunctionProperty(ctor, ARRAY_TAG, -24, "isArray", 1);
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
            s = "toLocaleString";
            break;
         case 4:
            arity = 0;
            s = "toSource";
            break;
         case 5:
            arity = 1;
            s = "join";
            break;
         case 6:
            arity = 0;
            s = "reverse";
            break;
         case 7:
            arity = 1;
            s = "sort";
            break;
         case 8:
            arity = 1;
            s = "push";
            break;
         case 9:
            arity = 0;
            s = "pop";
            break;
         case 10:
            arity = 0;
            s = "shift";
            break;
         case 11:
            arity = 1;
            s = "unshift";
            break;
         case 12:
            arity = 2;
            s = "splice";
            break;
         case 13:
            arity = 1;
            s = "concat";
            break;
         case 14:
            arity = 2;
            s = "slice";
            break;
         case 15:
            arity = 1;
            s = "indexOf";
            break;
         case 16:
            arity = 1;
            s = "lastIndexOf";
            break;
         case 17:
            arity = 1;
            s = "every";
            break;
         case 18:
            arity = 1;
            s = "filter";
            break;
         case 19:
            arity = 1;
            s = "forEach";
            break;
         case 20:
            arity = 1;
            s = "map";
            break;
         case 21:
            arity = 1;
            s = "some";
            break;
         case 22:
            arity = 1;
            s = "reduce";
            break;
         case 23:
            arity = 1;
            s = "reduceRight";
            break;
         default:
            throw new IllegalArgumentException(String.valueOf(id));
      }

      this.initPrototypeMethod(ARRAY_TAG, id, s, arity);
   }

   public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      if (!f.hasTag(ARRAY_TAG)) {
         return super.execIdCall(f, cx, scope, thisObj, args);
      } else {
         int id = f.methodId();

         while(true) {
            switch (id) {
               case -24:
                  return args.length > 0 && args[0] instanceof NativeArray;
               case -23:
               case -22:
               case -21:
               case -20:
               case -19:
               case -18:
               case -17:
               case -16:
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
                  if (args.length > 0) {
                     thisObj = ScriptRuntime.toObject(scope, args[0]);
                     Object[] newArgs = new Object[args.length - 1];

                     for(int i = 0; i < newArgs.length; ++i) {
                        newArgs[i] = args[i + 1];
                     }

                     args = newArgs;
                  }

                  id = -id;
                  break;
               case -4:
               case -3:
               case -2:
               case -1:
               case 0:
               default:
                  throw new IllegalArgumentException(String.valueOf(id));
               case 1:
                  boolean inNewExpr = thisObj == null;
                  if (!inNewExpr) {
                     return f.construct(cx, scope, args);
                  }

                  return jsConstructor(cx, scope, args);
               case 2:
                  return toStringHelper(cx, scope, thisObj, cx.hasFeature(4), false);
               case 3:
                  return toStringHelper(cx, scope, thisObj, false, true);
               case 4:
                  return toStringHelper(cx, scope, thisObj, true, false);
               case 5:
                  return js_join(cx, thisObj, args);
               case 6:
                  return js_reverse(cx, thisObj, args);
               case 7:
                  return js_sort(cx, scope, thisObj, args);
               case 8:
                  return js_push(cx, thisObj, args);
               case 9:
                  return js_pop(cx, thisObj, args);
               case 10:
                  return js_shift(cx, thisObj, args);
               case 11:
                  return js_unshift(cx, thisObj, args);
               case 12:
                  return js_splice(cx, scope, thisObj, args);
               case 13:
                  return js_concat(cx, scope, thisObj, args);
               case 14:
                  return this.js_slice(cx, thisObj, args);
               case 15:
                  return this.indexOfHelper(cx, thisObj, args, false);
               case 16:
                  return this.indexOfHelper(cx, thisObj, args, true);
               case 17:
               case 18:
               case 19:
               case 20:
               case 21:
                  return this.iterativeMethod(cx, id, scope, thisObj, args);
               case 22:
               case 23:
                  return this.reduceMethod(cx, id, scope, thisObj, args);
            }
         }
      }
   }

   public Object get(int index, Scriptable start) {
      if (!this.denseOnly && this.isGetterOrSetter((String)null, index, false)) {
         return super.get(index, start);
      } else {
         return this.dense != null && 0 <= index && index < this.dense.length ? this.dense[index] : super.get(index, start);
      }
   }

   public boolean has(int index, Scriptable start) {
      if (!this.denseOnly && this.isGetterOrSetter((String)null, index, false)) {
         return super.has(index, start);
      } else if (this.dense != null && 0 <= index && index < this.dense.length) {
         return this.dense[index] != NOT_FOUND;
      } else {
         return super.has(index, start);
      }
   }

   private static long toArrayIndex(Object id) {
      if (id instanceof String) {
         return toArrayIndex((String)id);
      } else {
         return id instanceof Number ? toArrayIndex(((Number)id).doubleValue()) : -1L;
      }
   }

   private static long toArrayIndex(String id) {
      long index = toArrayIndex(ScriptRuntime.toNumber(id));
      return Long.toString(index).equals(id) ? index : -1L;
   }

   private static long toArrayIndex(double d) {
      if (d == d) {
         long index = ScriptRuntime.toUint32(d);
         if ((double)index == d && index != 4294967295L) {
            return index;
         }
      }

      return -1L;
   }

   private static int toDenseIndex(Object id) {
      long index = toArrayIndex(id);
      return 0L <= index && index < 2147483647L ? (int)index : -1;
   }

   public void put(String id, Scriptable start, Object value) {
      super.put(id, start, value);
      if (start == this) {
         long index = toArrayIndex(id);
         if (index >= this.length) {
            this.length = index + 1L;
            this.denseOnly = false;
         }
      }

   }

   private boolean ensureCapacity(int capacity) {
      if (capacity > this.dense.length) {
         if (capacity > 1431655764) {
            this.denseOnly = false;
            return false;
         }

         capacity = Math.max(capacity, (int)((double)this.dense.length * (double)1.5F));
         Object[] newDense = new Object[capacity];
         System.arraycopy(this.dense, 0, newDense, 0, this.dense.length);
         Arrays.fill(newDense, this.dense.length, newDense.length, Scriptable.NOT_FOUND);
         this.dense = newDense;
      }

      return true;
   }

   public void put(int index, Scriptable start, Object value) {
      if (start == this && !this.isSealed() && this.dense != null && 0 <= index && (this.denseOnly || !this.isGetterOrSetter((String)null, index, true))) {
         if (index < this.dense.length) {
            this.dense[index] = value;
            if (this.length <= (long)index) {
               this.length = (long)index + 1L;
            }

            return;
         }

         if (this.denseOnly && (double)index < (double)this.dense.length * (double)1.5F && this.ensureCapacity(index + 1)) {
            this.dense[index] = value;
            this.length = (long)index + 1L;
            return;
         }

         this.denseOnly = false;
      }

      super.put(index, start, value);
      if (start == this && (this.lengthAttr & 1) == 0 && this.length <= (long)index) {
         this.length = (long)index + 1L;
      }

   }

   public void delete(int index) {
      if (this.dense == null || 0 > index || index >= this.dense.length || this.isSealed() || !this.denseOnly && this.isGetterOrSetter((String)null, index, true)) {
         super.delete(index);
      } else {
         this.dense[index] = NOT_FOUND;
      }

   }

   public Object[] getIds() {
      Object[] superIds = super.getIds();
      if (this.dense == null) {
         return superIds;
      } else {
         int N = this.dense.length;
         long currentLength = this.length;
         if ((long)N > currentLength) {
            N = (int)currentLength;
         }

         if (N == 0) {
            return superIds;
         } else {
            int superLength = superIds.length;
            Object[] ids = new Object[N + superLength];
            int presentCount = 0;

            for(int i = 0; i != N; ++i) {
               if (this.dense[i] != NOT_FOUND) {
                  ids[presentCount] = i;
                  ++presentCount;
               }
            }

            if (presentCount != N) {
               Object[] tmp = new Object[presentCount + superLength];
               System.arraycopy(ids, 0, tmp, 0, presentCount);
               ids = tmp;
            }

            System.arraycopy(superIds, 0, ids, presentCount, superLength);
            return ids;
         }
      }
   }

   public Object[] getAllIds() {
      Set<Object> allIds = new LinkedHashSet(Arrays.asList(this.getIds()));
      allIds.addAll(Arrays.asList(super.getAllIds()));
      return allIds.toArray();
   }

   public Integer[] getIndexIds() {
      Object[] ids = this.getIds();
      List<Integer> indices = new ArrayList(ids.length);

      for(Object id : ids) {
         int int32Id = ScriptRuntime.toInt32(id);
         if (int32Id >= 0 && ScriptRuntime.toString((double)int32Id).equals(ScriptRuntime.toString(id))) {
            indices.add(int32Id);
         }
      }

      return (Integer[])indices.toArray(new Integer[indices.size()]);
   }

   public Object getDefaultValue(Class hint) {
      if (hint == ScriptRuntime.NumberClass) {
         Context cx = Context.getContext();
         if (cx.getLanguageVersion() == 120) {
            return this.length;
         }
      }

      return super.getDefaultValue(hint);
   }

   private ScriptableObject defaultIndexPropertyDescriptor(Object value) {
      Scriptable scope = this.getParentScope();
      if (scope == null) {
         scope = this;
      }

      ScriptableObject desc = new NativeObject();
      ScriptRuntime.setBuiltinProtoAndParent(desc, scope, TopLevel.Builtins.Object);
      desc.defineProperty("value", (Object)value, 0);
      desc.defineProperty("writable", (Object)true, 0);
      desc.defineProperty("enumerable", (Object)true, 0);
      desc.defineProperty("configurable", (Object)true, 0);
      return desc;
   }

   public int getAttributes(int index) {
      return this.dense != null && index >= 0 && index < this.dense.length && this.dense[index] != NOT_FOUND ? 0 : super.getAttributes(index);
   }

   protected ScriptableObject getOwnPropertyDescriptor(Context cx, Object id) {
      if (this.dense != null) {
         int index = toDenseIndex(id);
         if (0 <= index && index < this.dense.length && this.dense[index] != NOT_FOUND) {
            Object value = this.dense[index];
            return this.defaultIndexPropertyDescriptor(value);
         }
      }

      return super.getOwnPropertyDescriptor(cx, id);
   }

   protected void defineOwnProperty(Context cx, Object id, ScriptableObject desc, boolean checkValid) {
      if (this.dense != null) {
         Object[] values = this.dense;
         this.dense = null;
         this.denseOnly = false;

         for(int i = 0; i < values.length; ++i) {
            if (values[i] != NOT_FOUND) {
               this.put(i, this, values[i]);
            }
         }
      }

      long index = toArrayIndex(id);
      if (index >= this.length) {
         this.length = index + 1L;
      }

      super.defineOwnProperty(cx, id, desc, checkValid);
   }

   private static Object jsConstructor(Context cx, Scriptable scope, Object[] args) {
      if (args.length == 0) {
         return new NativeArray(0L);
      } else if (cx.getLanguageVersion() == 120) {
         return new NativeArray(args);
      } else {
         Object arg0 = args[0];
         if (args.length <= 1 && arg0 instanceof Number) {
            long len = ScriptRuntime.toUint32(arg0);
            if ((double)len != ((Number)arg0).doubleValue()) {
               String msg = ScriptRuntime.getMessage0("msg.arraylength.bad");
               throw ScriptRuntime.constructError("RangeError", msg);
            } else {
               return new NativeArray(len);
            }
         } else {
            return new NativeArray(args);
         }
      }
   }

   public long getLength() {
      return this.length;
   }

   /** @deprecated */
   public long jsGet_length() {
      return this.getLength();
   }

   void setDenseOnly(boolean denseOnly) {
      if (denseOnly && !this.denseOnly) {
         throw new IllegalArgumentException();
      } else {
         this.denseOnly = denseOnly;
      }
   }

   private void setLength(Object val) {
      if ((this.lengthAttr & 1) == 0) {
         double d = ScriptRuntime.toNumber(val);
         long longVal = ScriptRuntime.toUint32(d);
         if ((double)longVal != d) {
            String msg = ScriptRuntime.getMessage0("msg.arraylength.bad");
            throw ScriptRuntime.constructError("RangeError", msg);
         } else {
            if (this.denseOnly) {
               if (longVal < this.length) {
                  Arrays.fill(this.dense, (int)longVal, this.dense.length, NOT_FOUND);
                  this.length = longVal;
                  return;
               }

               if (longVal < 1431655764L && (double)longVal < (double)this.length * (double)1.5F && this.ensureCapacity((int)longVal)) {
                  this.length = longVal;
                  return;
               }

               this.denseOnly = false;
            }

            if (longVal < this.length) {
               if (this.length - longVal > 4096L) {
                  Object[] e = this.getIds();

                  for(int i = 0; i < e.length; ++i) {
                     Object id = e[i];
                     if (id instanceof String) {
                        String strId = (String)id;
                        long index = toArrayIndex(strId);
                        if (index >= longVal) {
                           this.delete(strId);
                        }
                     } else {
                        int index = (Integer)id;
                        if ((long)index >= longVal) {
                           this.delete(index);
                        }
                     }
                  }
               } else {
                  for(long i = longVal; i < this.length; ++i) {
                     deleteElem(this, i);
                  }
               }
            }

            this.length = longVal;
         }
      }
   }

   static long getLengthProperty(Context cx, Scriptable obj) {
      if (obj instanceof NativeString) {
         return (long)((NativeString)obj).getLength();
      } else {
         return obj instanceof NativeArray ? ((NativeArray)obj).getLength() : ScriptRuntime.toUint32(ScriptRuntime.getObjectProp(obj, "length", cx));
      }
   }

   private static Object setLengthProperty(Context cx, Scriptable target, long length) {
      return ScriptRuntime.setObjectProp((Scriptable)target, "length", ScriptRuntime.wrapNumber((double)length), cx);
   }

   private static void deleteElem(Scriptable target, long index) {
      int i = (int)index;
      if ((long)i == index) {
         target.delete(i);
      } else {
         target.delete(Long.toString(index));
      }

   }

   private static Object getElem(Context cx, Scriptable target, long index) {
      if (index > 2147483647L) {
         String id = Long.toString(index);
         return ScriptRuntime.getObjectProp(target, id, cx);
      } else {
         return ScriptRuntime.getObjectIndex(target, (int)index, cx);
      }
   }

   private static Object getRawElem(Scriptable target, long index) {
      return index > 2147483647L ? ScriptableObject.getProperty(target, Long.toString(index)) : ScriptableObject.getProperty(target, (int)index);
   }

   private static void setElem(Context cx, Scriptable target, long index, Object value) {
      if (index > 2147483647L) {
         String id = Long.toString(index);
         ScriptRuntime.setObjectProp(target, id, value, cx);
      } else {
         ScriptRuntime.setObjectIndex(target, (int)index, value, cx);
      }

   }

   private static void setRawElem(Context cx, Scriptable target, long index, Object value) {
      if (value == NOT_FOUND) {
         deleteElem(target, index);
      } else {
         setElem(cx, target, index, value);
      }

   }

   private static String toStringHelper(Context cx, Scriptable scope, Scriptable thisObj, boolean toSource, boolean toLocale) {
      long length = getLengthProperty(cx, thisObj);
      StringBuilder result = new StringBuilder(256);
      String separator;
      if (toSource) {
         result.append('[');
         separator = ", ";
      } else {
         separator = ",";
      }

      boolean haslast = false;
      long i = 0L;
      boolean toplevel;
      boolean iterating;
      if (cx.iterating == null) {
         toplevel = true;
         iterating = false;
         cx.iterating = new ObjToIntMap(31);
      } else {
         toplevel = false;
         iterating = cx.iterating.has(thisObj);
      }

      try {
         if (!iterating) {
            cx.iterating.put(thisObj, 0);
            boolean skipUndefinedAndNull = !toSource || cx.getLanguageVersion() < 150;

            for(i = 0L; i < length; ++i) {
               if (i > 0L) {
                  result.append(separator);
               }

               Object elem = getRawElem(thisObj, i);
               if (elem != NOT_FOUND && (!skipUndefinedAndNull || elem != null && elem != Undefined.instance)) {
                  haslast = true;
                  if (toSource) {
                     result.append(ScriptRuntime.uneval(cx, scope, elem));
                  } else if (elem instanceof String) {
                     String s = (String)elem;
                     if (toSource) {
                        result.append('"');
                        result.append(ScriptRuntime.escapeString(s));
                        result.append('"');
                     } else {
                        result.append(s);
                     }
                  } else {
                     if (toLocale) {
                        Callable fun = ScriptRuntime.getPropFunctionAndThis(elem, "toLocaleString", cx);
                        Scriptable funThis = ScriptRuntime.lastStoredScriptable(cx);
                        elem = fun.call(cx, scope, funThis, ScriptRuntime.emptyArgs);
                     }

                     result.append(ScriptRuntime.toString(elem));
                  }
               } else {
                  haslast = false;
               }
            }
         }
      } finally {
         if (toplevel) {
            cx.iterating = null;
         }

      }

      if (toSource) {
         if (!haslast && i > 0L) {
            result.append(", ]");
         } else {
            result.append(']');
         }
      }

      return result.toString();
   }

   private static String js_join(Context cx, Scriptable thisObj, Object[] args) {
      long llength = getLengthProperty(cx, thisObj);
      int length = (int)llength;
      if (llength != (long)length) {
         throw Context.reportRuntimeError1("msg.arraylength.too.big", String.valueOf(llength));
      } else {
         String separator = args.length >= 1 && args[0] != Undefined.instance ? ScriptRuntime.toString(args[0]) : ",";
         if (thisObj instanceof NativeArray) {
            NativeArray na = (NativeArray)thisObj;
            if (na.denseOnly) {
               StringBuilder sb = new StringBuilder();

               for(int i = 0; i < length; ++i) {
                  if (i != 0) {
                     sb.append(separator);
                  }

                  if (i < na.dense.length) {
                     Object temp = na.dense[i];
                     if (temp != null && temp != Undefined.instance && temp != Scriptable.NOT_FOUND) {
                        sb.append(ScriptRuntime.toString(temp));
                     }
                  }
               }

               return sb.toString();
            }
         }

         if (length == 0) {
            return "";
         } else {
            String[] buf = new String[length];
            int total_size = 0;

            for(int i = 0; i != length; ++i) {
               Object temp = getElem(cx, thisObj, (long)i);
               if (temp != null && temp != Undefined.instance) {
                  String str = ScriptRuntime.toString(temp);
                  total_size += str.length();
                  buf[i] = str;
               }
            }

            total_size += (length - 1) * separator.length();
            StringBuilder sb = new StringBuilder(total_size);

            for(int i = 0; i != length; ++i) {
               if (i != 0) {
                  sb.append(separator);
               }

               String str = buf[i];
               if (str != null) {
                  sb.append(str);
               }
            }

            return sb.toString();
         }
      }
   }

   private static Scriptable js_reverse(Context cx, Scriptable thisObj, Object[] args) {
      if (thisObj instanceof NativeArray) {
         NativeArray na = (NativeArray)thisObj;
         if (na.denseOnly) {
            int i = 0;

            for(int j = (int)na.length - 1; i < j; --j) {
               Object temp = na.dense[i];
               na.dense[i] = na.dense[j];
               na.dense[j] = temp;
               ++i;
            }

            return thisObj;
         }
      }

      long len = getLengthProperty(cx, thisObj);
      long half = len / 2L;

      for(long i = 0L; i < half; ++i) {
         long j = len - i - 1L;
         Object temp1 = getRawElem(thisObj, i);
         Object temp2 = getRawElem(thisObj, j);
         setRawElem(cx, thisObj, i, temp2);
         setRawElem(cx, thisObj, j, temp1);
      }

      return thisObj;
   }

   private static Scriptable js_sort(final Context cx, final Scriptable scope, Scriptable thisObj, Object[] args) {
      Comparator<Object> comparator;
      if (args.length > 0 && Undefined.instance != args[0]) {
         final Callable jsCompareFunction = ScriptRuntime.getValueFunctionAndThis(args[0], cx);
         final Scriptable funThis = ScriptRuntime.lastStoredScriptable(cx);
         final Object[] cmpBuf = new Object[2];
         comparator = new Comparator() {
            public int compare(Object x, Object y) {
               if (x == y) {
                  return 0;
               } else if (y != Undefined.instance && y != Scriptable.NOT_FOUND) {
                  if (x != Undefined.instance && x != Scriptable.NOT_FOUND) {
                     cmpBuf[0] = x;
                     cmpBuf[1] = y;
                     Object ret = jsCompareFunction.call(cx, scope, funThis, cmpBuf);
                     double d = ScriptRuntime.toNumber(ret);
                     if (d < (double)0.0F) {
                        return -1;
                     } else {
                        return d > (double)0.0F ? 1 : 0;
                     }
                  } else {
                     return 1;
                  }
               } else {
                  return -1;
               }
            }
         };
      } else {
         comparator = new Comparator() {
            public int compare(Object x, Object y) {
               if (x == y) {
                  return 0;
               } else if (y != Undefined.instance && y != Scriptable.NOT_FOUND) {
                  if (x != Undefined.instance && x != Scriptable.NOT_FOUND) {
                     String a = ScriptRuntime.toString(x);
                     String b = ScriptRuntime.toString(y);
                     return a.compareTo(b);
                  } else {
                     return 1;
                  }
               } else {
                  return -1;
               }
            }
         };
      }

      int length = (int)getLengthProperty(cx, thisObj);
      Object[] working = new Object[length];

      for(int i = 0; i != length; ++i) {
         working[i] = getElem(cx, thisObj, (long)i);
      }

      Arrays.sort(working, comparator);

      for(int i = 0; i < length; ++i) {
         setElem(cx, thisObj, (long)i, working[i]);
      }

      return thisObj;
   }

   private static Object js_push(Context cx, Scriptable thisObj, Object[] args) {
      if (thisObj instanceof NativeArray) {
         NativeArray na = (NativeArray)thisObj;
         if (na.denseOnly && na.ensureCapacity((int)na.length + args.length)) {
            for(int i = 0; i < args.length; ++i) {
               na.dense[(int)(na.length++)] = args[i];
            }

            return ScriptRuntime.wrapNumber((double)na.length);
         }
      }

      long length = getLengthProperty(cx, thisObj);

      for(int i = 0; i < args.length; ++i) {
         setElem(cx, thisObj, length + (long)i, args[i]);
      }

      length += (long)args.length;
      Object lengthObj = setLengthProperty(cx, thisObj, length);
      if (cx.getLanguageVersion() == 120) {
         return args.length == 0 ? Undefined.instance : args[args.length - 1];
      } else {
         return lengthObj;
      }
   }

   private static Object js_pop(Context cx, Scriptable thisObj, Object[] args) {
      if (thisObj instanceof NativeArray) {
         NativeArray na = (NativeArray)thisObj;
         if (na.denseOnly && na.length > 0L) {
            --na.length;
            Object result = na.dense[(int)na.length];
            na.dense[(int)na.length] = NOT_FOUND;
            return result;
         }
      }

      long length = getLengthProperty(cx, thisObj);
      Object result;
      if (length > 0L) {
         --length;
         result = getElem(cx, thisObj, length);
      } else {
         result = Undefined.instance;
      }

      setLengthProperty(cx, thisObj, length);
      return result;
   }

   private static Object js_shift(Context cx, Scriptable thisObj, Object[] args) {
      if (thisObj instanceof NativeArray) {
         NativeArray na = (NativeArray)thisObj;
         if (na.denseOnly && na.length > 0L) {
            --na.length;
            Object result = na.dense[0];
            System.arraycopy(na.dense, 1, na.dense, 0, (int)na.length);
            na.dense[(int)na.length] = NOT_FOUND;
            return result == NOT_FOUND ? Undefined.instance : result;
         }
      }

      long length = getLengthProperty(cx, thisObj);
      Object result;
      if (length > 0L) {
         long i = 0L;
         --length;
         result = getElem(cx, thisObj, i);
         if (length > 0L) {
            for(long var11 = 1L; var11 <= length; ++var11) {
               Object temp = getRawElem(thisObj, var11);
               setRawElem(cx, thisObj, var11 - 1L, temp);
            }
         }
      } else {
         result = Undefined.instance;
      }

      setLengthProperty(cx, thisObj, length);
      return result;
   }

   private static Object js_unshift(Context cx, Scriptable thisObj, Object[] args) {
      if (thisObj instanceof NativeArray) {
         NativeArray na = (NativeArray)thisObj;
         if (na.denseOnly && na.ensureCapacity((int)na.length + args.length)) {
            System.arraycopy(na.dense, 0, na.dense, args.length, (int)na.length);

            for(int i = 0; i < args.length; ++i) {
               na.dense[i] = args[i];
            }

            na.length += (long)args.length;
            return ScriptRuntime.wrapNumber((double)na.length);
         }
      }

      long length = getLengthProperty(cx, thisObj);
      int argc = args.length;
      if (args.length <= 0) {
         return ScriptRuntime.wrapNumber((double)length);
      } else {
         if (length > 0L) {
            for(long last = length - 1L; last >= 0L; --last) {
               Object temp = getRawElem(thisObj, last);
               setRawElem(cx, thisObj, last + (long)argc, temp);
            }
         }

         for(int i = 0; i < args.length; ++i) {
            setElem(cx, thisObj, (long)i, args[i]);
         }

         length += (long)args.length;
         return setLengthProperty(cx, thisObj, length);
      }
   }

   private static Object js_splice(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      NativeArray na = null;
      boolean denseMode = false;
      if (thisObj instanceof NativeArray) {
         na = (NativeArray)thisObj;
         denseMode = na.denseOnly;
      }

      scope = getTopLevelScope(scope);
      int argc = args.length;
      if (argc == 0) {
         return cx.newArray(scope, 0);
      } else {
         long length = getLengthProperty(cx, thisObj);
         long begin = toSliceIndex(ScriptRuntime.toInteger(args[0]), length);
         --argc;
         long count;
         if (args.length == 1) {
            count = length - begin;
         } else {
            double dcount = ScriptRuntime.toInteger(args[1]);
            if (dcount < (double)0.0F) {
               count = 0L;
            } else if (dcount > (double)(length - begin)) {
               count = length - begin;
            } else {
               count = (long)dcount;
            }

            --argc;
         }

         long end = begin + count;
         Object result;
         if (count != 0L) {
            if (count == 1L && cx.getLanguageVersion() == 120) {
               result = getElem(cx, thisObj, begin);
            } else if (denseMode) {
               int intLen = (int)(end - begin);
               Object[] copy = new Object[intLen];
               System.arraycopy(na.dense, (int)begin, copy, 0, intLen);
               result = cx.newArray(scope, copy);
            } else {
               Scriptable resultArray = cx.newArray(scope, 0);

               for(long last = begin; last != end; ++last) {
                  Object temp = getRawElem(thisObj, last);
                  if (temp != NOT_FOUND) {
                     setElem(cx, resultArray, last - begin, temp);
                  }
               }

               setLengthProperty(cx, resultArray, end - begin);
               result = resultArray;
            }
         } else if (cx.getLanguageVersion() == 120) {
            result = Undefined.instance;
         } else {
            result = cx.newArray(scope, 0);
         }

         long delta = (long)argc - count;
         if (denseMode && length + delta < 2147483647L && na.ensureCapacity((int)(length + delta))) {
            System.arraycopy(na.dense, (int)end, na.dense, (int)(begin + (long)argc), (int)(length - end));
            if (argc > 0) {
               System.arraycopy(args, 2, na.dense, (int)begin, argc);
            }

            if (delta < 0L) {
               Arrays.fill(na.dense, (int)(length + delta), (int)length, NOT_FOUND);
            }

            na.length = length + delta;
            return result;
         } else {
            if (delta > 0L) {
               for(long last = length - 1L; last >= end; --last) {
                  Object temp = getRawElem(thisObj, last);
                  setRawElem(cx, thisObj, last + delta, temp);
               }
            } else if (delta < 0L) {
               for(long last = end; last < length; ++last) {
                  Object temp = getRawElem(thisObj, last);
                  setRawElem(cx, thisObj, last + delta, temp);
               }
            }

            int argoffset = args.length - argc;

            for(int i = 0; i < argc; ++i) {
               setElem(cx, thisObj, begin + (long)i, args[i + argoffset]);
            }

            setLengthProperty(cx, thisObj, length + delta);
            return result;
         }
      }
   }

   private static Scriptable js_concat(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      scope = getTopLevelScope(scope);
      Function ctor = ScriptRuntime.getExistingCtor(cx, scope, "Array");
      Scriptable result = ctor.construct(cx, scope, ScriptRuntime.emptyArgs);
      if (thisObj instanceof NativeArray && result instanceof NativeArray) {
         NativeArray denseThis = (NativeArray)thisObj;
         NativeArray denseResult = (NativeArray)result;
         if (denseThis.denseOnly && denseResult.denseOnly) {
            boolean canUseDense = true;
            int length = (int)denseThis.length;

            for(int i = 0; i < args.length && canUseDense; ++i) {
               if (args[i] instanceof NativeArray) {
                  NativeArray arg = (NativeArray)args[i];
                  canUseDense = arg.denseOnly;
                  length = (int)((long)length + arg.length);
               } else {
                  ++length;
               }
            }

            if (canUseDense && denseResult.ensureCapacity(length)) {
               System.arraycopy(denseThis.dense, 0, denseResult.dense, 0, (int)denseThis.length);
               int cursor = (int)denseThis.length;

               for(int i = 0; i < args.length && canUseDense; ++i) {
                  if (args[i] instanceof NativeArray) {
                     NativeArray arg = (NativeArray)args[i];
                     System.arraycopy(arg.dense, 0, denseResult.dense, cursor, (int)arg.length);
                     cursor += (int)arg.length;
                  } else {
                     denseResult.dense[cursor++] = args[i];
                  }
               }

               denseResult.length = (long)length;
               return result;
            }
         }
      }

      long slot = 0L;
      if (ScriptRuntime.instanceOf(thisObj, ctor, cx)) {
         long length = getLengthProperty(cx, thisObj);

         for(slot = 0L; slot < length; ++slot) {
            Object temp = getRawElem(thisObj, slot);
            if (temp != NOT_FOUND) {
               setElem(cx, result, slot, temp);
            }
         }
      } else {
         setElem(cx, result, slot++, thisObj);
      }

      for(int i = 0; i < args.length; ++i) {
         if (ScriptRuntime.instanceOf(args[i], ctor, cx)) {
            Scriptable arg = (Scriptable)args[i];
            long length = getLengthProperty(cx, arg);

            for(long j = 0L; j < length; ++slot) {
               Object temp = getRawElem(arg, j);
               if (temp != NOT_FOUND) {
                  setElem(cx, result, slot, temp);
               }

               ++j;
            }
         } else {
            setElem(cx, result, slot++, args[i]);
         }
      }

      setLengthProperty(cx, result, slot);
      return result;
   }

   private Scriptable js_slice(Context cx, Scriptable thisObj, Object[] args) {
      Scriptable scope = getTopLevelScope(this);
      Scriptable result = cx.newArray(scope, 0);
      long length = getLengthProperty(cx, thisObj);
      long begin;
      long end;
      if (args.length == 0) {
         begin = 0L;
         end = length;
      } else {
         begin = toSliceIndex(ScriptRuntime.toInteger(args[0]), length);
         if (args.length == 1) {
            end = length;
         } else {
            end = toSliceIndex(ScriptRuntime.toInteger(args[1]), length);
         }
      }

      for(long slot = begin; slot < end; ++slot) {
         Object temp = getRawElem(thisObj, slot);
         if (temp != NOT_FOUND) {
            setElem(cx, result, slot - begin, temp);
         }
      }

      setLengthProperty(cx, result, Math.max(0L, end - begin));
      return result;
   }

   private static long toSliceIndex(double value, long length) {
      long result;
      if (value < (double)0.0F) {
         if (value + (double)length < (double)0.0F) {
            result = 0L;
         } else {
            result = (long)(value + (double)length);
         }
      } else if (value > (double)length) {
         result = length;
      } else {
         result = (long)value;
      }

      return result;
   }

   private Object indexOfHelper(Context cx, Scriptable thisObj, Object[] args, boolean isLast) {
      Object compareTo = args.length > 0 ? args[0] : Undefined.instance;
      long length = getLengthProperty(cx, thisObj);
      long start;
      if (isLast) {
         if (args.length < 2) {
            start = length - 1L;
         } else {
            start = (long)ScriptRuntime.toInteger(args[1]);
            if (start >= length) {
               start = length - 1L;
            } else if (start < 0L) {
               start += length;
            }

            if (start < 0L) {
               return NEGATIVE_ONE;
            }
         }
      } else if (args.length < 2) {
         start = 0L;
      } else {
         start = (long)ScriptRuntime.toInteger(args[1]);
         if (start < 0L) {
            start += length;
            if (start < 0L) {
               start = 0L;
            }
         }

         if (start > length - 1L) {
            return NEGATIVE_ONE;
         }
      }

      if (thisObj instanceof NativeArray) {
         NativeArray na = (NativeArray)thisObj;
         if (na.denseOnly) {
            if (isLast) {
               for(int i = (int)start; i >= 0; --i) {
                  if (na.dense[i] != Scriptable.NOT_FOUND && ScriptRuntime.shallowEq(na.dense[i], compareTo)) {
                     return (long)i;
                  }
               }
            } else {
               for(int i = (int)start; (long)i < length; ++i) {
                  if (na.dense[i] != Scriptable.NOT_FOUND && ScriptRuntime.shallowEq(na.dense[i], compareTo)) {
                     return (long)i;
                  }
               }
            }

            return NEGATIVE_ONE;
         }
      }

      if (isLast) {
         for(long i = start; i >= 0L; --i) {
            Object val = getRawElem(thisObj, i);
            if (val != NOT_FOUND && ScriptRuntime.shallowEq(val, compareTo)) {
               return i;
            }
         }
      } else {
         for(long i = start; i < length; ++i) {
            Object val = getRawElem(thisObj, i);
            if (val != NOT_FOUND && ScriptRuntime.shallowEq(val, compareTo)) {
               return i;
            }
         }
      }

      return NEGATIVE_ONE;
   }

   private Object iterativeMethod(Context cx, int id, Scriptable scope, Scriptable thisObj, Object[] args) {
      Object callbackArg = args.length > 0 ? args[0] : Undefined.instance;
      if (callbackArg != null && callbackArg instanceof Function) {
         Function f = (Function)callbackArg;
         Scriptable parent = ScriptableObject.getTopLevelScope(f);
         Scriptable thisArg;
         if (args.length >= 2 && args[1] != null && args[1] != Undefined.instance) {
            thisArg = ScriptRuntime.toObject(cx, scope, args[1]);
         } else {
            thisArg = parent;
         }

         long length = getLengthProperty(cx, thisObj);
         int resultLength = id == 20 ? (int)length : 0;
         Scriptable array = cx.newArray(scope, resultLength);
         long j = 0L;

         for(long i = 0L; i < length; ++i) {
            Object[] innerArgs = new Object[3];
            Object elem = getRawElem(thisObj, i);
            if (elem != Scriptable.NOT_FOUND) {
               innerArgs[0] = elem;
               innerArgs[1] = i;
               innerArgs[2] = thisObj;
               Object result = f.call(cx, parent, thisArg, innerArgs);
               switch (id) {
                  case 17:
                     if (!ScriptRuntime.toBoolean(result)) {
                        return Boolean.FALSE;
                     }
                     break;
                  case 18:
                     if (ScriptRuntime.toBoolean(result)) {
                        setElem(cx, array, j++, innerArgs[0]);
                     }
                  case 19:
                  default:
                     break;
                  case 20:
                     setElem(cx, array, i, result);
                     break;
                  case 21:
                     if (ScriptRuntime.toBoolean(result)) {
                        return Boolean.TRUE;
                     }
               }
            }
         }

         switch (id) {
            case 17:
               return Boolean.TRUE;
            case 18:
            case 20:
               return array;
            case 19:
            default:
               return Undefined.instance;
            case 21:
               return Boolean.FALSE;
         }
      } else {
         throw ScriptRuntime.notFunctionError(callbackArg);
      }
   }

   private Object reduceMethod(Context cx, int id, Scriptable scope, Scriptable thisObj, Object[] args) {
      Object callbackArg = args.length > 0 ? args[0] : Undefined.instance;
      if (callbackArg != null && callbackArg instanceof Function) {
         Function f = (Function)callbackArg;
         Scriptable parent = ScriptableObject.getTopLevelScope(f);
         long length = getLengthProperty(cx, thisObj);
         boolean movingLeft = id == 22;
         Object value = args.length > 1 ? args[1] : Scriptable.NOT_FOUND;

         for(long i = 0L; i < length; ++i) {
            long index = movingLeft ? i : length - 1L - i;
            Object elem = getRawElem(thisObj, index);
            if (elem != Scriptable.NOT_FOUND) {
               if (value == Scriptable.NOT_FOUND) {
                  value = elem;
               } else {
                  Object[] innerArgs = new Object[]{value, elem, index, thisObj};
                  value = f.call(cx, parent, parent, innerArgs);
               }
            }
         }

         if (value == Scriptable.NOT_FOUND) {
            throw ScriptRuntime.typeError0("msg.empty.array.reduce");
         } else {
            return value;
         }
      } else {
         throw ScriptRuntime.notFunctionError(callbackArg);
      }
   }

   public boolean contains(Object o) {
      return this.indexOf(o) > -1;
   }

   public Object[] toArray() {
      return this.toArray(ScriptRuntime.emptyArgs);
   }

   public Object[] toArray(Object[] a) {
      long longLen = this.length;
      if (longLen > 2147483647L) {
         throw new IllegalStateException();
      } else {
         int len = (int)longLen;
         Object[] array = a.length >= len ? a : (Object[])((Object[])Array.newInstance(a.getClass().getComponentType(), len));

         for(int i = 0; i < len; ++i) {
            array[i] = this.get(i);
         }

         return array;
      }
   }

   public boolean containsAll(Collection c) {
      for(Object aC : c) {
         if (!this.contains(aC)) {
            return false;
         }
      }

      return true;
   }

   public int size() {
      long longLen = this.length;
      if (longLen > 2147483647L) {
         throw new IllegalStateException();
      } else {
         return (int)longLen;
      }
   }

   public Object get(long index) {
      if (index >= 0L && index < this.length) {
         Object value = getRawElem(this, index);
         if (value != Scriptable.NOT_FOUND && value != Undefined.instance) {
            return value instanceof Wrapper ? ((Wrapper)value).unwrap() : value;
         } else {
            return null;
         }
      } else {
         throw new IndexOutOfBoundsException();
      }
   }

   public Object get(int index) {
      return this.get((long)index);
   }

   public int indexOf(Object o) {
      long longLen = this.length;
      if (longLen > 2147483647L) {
         throw new IllegalStateException();
      } else {
         int len = (int)longLen;
         if (o == null) {
            for(int i = 0; i < len; ++i) {
               if (this.get(i) == null) {
                  return i;
               }
            }
         } else {
            for(int i = 0; i < len; ++i) {
               if (o.equals(this.get(i))) {
                  return i;
               }
            }
         }

         return -1;
      }
   }

   public int lastIndexOf(Object o) {
      long longLen = this.length;
      if (longLen > 2147483647L) {
         throw new IllegalStateException();
      } else {
         int len = (int)longLen;
         if (o == null) {
            for(int i = len - 1; i >= 0; --i) {
               if (this.get(i) == null) {
                  return i;
               }
            }
         } else {
            for(int i = len - 1; i >= 0; --i) {
               if (o.equals(this.get(i))) {
                  return i;
               }
            }
         }

         return -1;
      }
   }

   public Iterator iterator() {
      return this.listIterator(0);
   }

   public ListIterator listIterator() {
      return this.listIterator(0);
   }

   public ListIterator listIterator(final int start) {
      long longLen = this.length;
      if (longLen > 2147483647L) {
         throw new IllegalStateException();
      } else {
         final int len = (int)longLen;
         if (start >= 0 && start <= len) {
            return new ListIterator() {
               int cursor = start;

               public boolean hasNext() {
                  return this.cursor < len;
               }

               public Object next() {
                  if (this.cursor == len) {
                     throw new NoSuchElementException();
                  } else {
                     return NativeArray.this.get(this.cursor++);
                  }
               }

               public boolean hasPrevious() {
                  return this.cursor > 0;
               }

               public Object previous() {
                  if (this.cursor == 0) {
                     throw new NoSuchElementException();
                  } else {
                     return NativeArray.this.get(--this.cursor);
                  }
               }

               public int nextIndex() {
                  return this.cursor;
               }

               public int previousIndex() {
                  return this.cursor - 1;
               }

               public void remove() {
                  throw new UnsupportedOperationException();
               }

               public void add(Object o) {
                  throw new UnsupportedOperationException();
               }

               public void set(Object o) {
                  throw new UnsupportedOperationException();
               }
            };
         } else {
            throw new IndexOutOfBoundsException("Index: " + start);
         }
      }
   }

   public boolean add(Object o) {
      throw new UnsupportedOperationException();
   }

   public boolean remove(Object o) {
      throw new UnsupportedOperationException();
   }

   public boolean addAll(Collection c) {
      throw new UnsupportedOperationException();
   }

   public boolean removeAll(Collection c) {
      throw new UnsupportedOperationException();
   }

   public boolean retainAll(Collection c) {
      throw new UnsupportedOperationException();
   }

   public void clear() {
      throw new UnsupportedOperationException();
   }

   public void add(int index, Object element) {
      throw new UnsupportedOperationException();
   }

   public boolean addAll(int index, Collection c) {
      throw new UnsupportedOperationException();
   }

   public Object set(int index, Object element) {
      throw new UnsupportedOperationException();
   }

   public Object remove(int index) {
      throw new UnsupportedOperationException();
   }

   public List subList(int fromIndex, int toIndex) {
      throw new UnsupportedOperationException();
   }

   protected int findPrototypeId(String s) {
      int id;
      String X;
      id = 0;
      X = null;
      label83:
      switch (s.length()) {
         case 3:
            int c = s.charAt(0);
            if (c == 109) {
               if (s.charAt(2) == 'p' && s.charAt(1) == 'a') {
                  id = 20;
                  return id;
               }
            } else if (c == 112 && s.charAt(2) == 'p' && s.charAt(1) == 'o') {
               id = 9;
               return id;
            }
            break;
         case 4:
            switch (s.charAt(2)) {
               case 'i':
                  X = "join";
                  id = 5;
                  break label83;
               case 'm':
                  X = "some";
                  id = 21;
                  break label83;
               case 'r':
                  X = "sort";
                  id = 7;
                  break label83;
               case 's':
                  X = "push";
                  id = 8;
               default:
                  break label83;
            }
         case 5:
            int var7 = s.charAt(1);
            if (var7 == 'h') {
               X = "shift";
               id = 10;
            } else if (var7 == 'l') {
               X = "slice";
               id = 14;
            } else if (var7 == 'v') {
               X = "every";
               id = 17;
            }
            break;
         case 6:
            int var6 = s.charAt(0);
            if (var6 == 'c') {
               X = "concat";
               id = 13;
            } else if (var6 == 'f') {
               X = "filter";
               id = 18;
            } else if (var6 == 's') {
               X = "splice";
               id = 12;
            } else if (var6 == 'r') {
               X = "reduce";
               id = 22;
            }
            break;
         case 7:
            switch (s.charAt(0)) {
               case 'f':
                  X = "forEach";
                  id = 19;
                  break label83;
               case 'i':
                  X = "indexOf";
                  id = 15;
                  break label83;
               case 'r':
                  X = "reverse";
                  id = 6;
                  break label83;
               case 'u':
                  X = "unshift";
                  id = 11;
               default:
                  break label83;
            }
         case 8:
            int var5 = s.charAt(3);
            if (var5 == 'o') {
               X = "toSource";
               id = 4;
            } else if (var5 == 't') {
               X = "toString";
               id = 2;
            }
         case 9:
         case 10:
         case 12:
         case 13:
         default:
            break;
         case 11:
            int c = s.charAt(0);
            if (c == 'c') {
               X = "constructor";
               id = 1;
            } else if (c == 'l') {
               X = "lastIndexOf";
               id = 16;
            } else if (c == 'r') {
               X = "reduceRight";
               id = 23;
            }
            break;
         case 14:
            X = "toLocaleString";
            id = 3;
      }

      if (X != null && X != s && !X.equals(s)) {
         id = 0;
      }

      return id;
   }
}
