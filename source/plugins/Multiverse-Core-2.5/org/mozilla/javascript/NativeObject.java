package org.mozilla.javascript;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class NativeObject extends IdScriptableObject implements Map {
   static final long serialVersionUID = -6345305608474346996L;
   private static final Object OBJECT_TAG = "Object";
   private static final int ConstructorId_getPrototypeOf = -1;
   private static final int ConstructorId_keys = -2;
   private static final int ConstructorId_getOwnPropertyNames = -3;
   private static final int ConstructorId_getOwnPropertyDescriptor = -4;
   private static final int ConstructorId_defineProperty = -5;
   private static final int ConstructorId_isExtensible = -6;
   private static final int ConstructorId_preventExtensions = -7;
   private static final int ConstructorId_defineProperties = -8;
   private static final int ConstructorId_create = -9;
   private static final int ConstructorId_isSealed = -10;
   private static final int ConstructorId_isFrozen = -11;
   private static final int ConstructorId_seal = -12;
   private static final int ConstructorId_freeze = -13;
   private static final int Id_constructor = 1;
   private static final int Id_toString = 2;
   private static final int Id_toLocaleString = 3;
   private static final int Id_valueOf = 4;
   private static final int Id_hasOwnProperty = 5;
   private static final int Id_propertyIsEnumerable = 6;
   private static final int Id_isPrototypeOf = 7;
   private static final int Id_toSource = 8;
   private static final int Id___defineGetter__ = 9;
   private static final int Id___defineSetter__ = 10;
   private static final int Id___lookupGetter__ = 11;
   private static final int Id___lookupSetter__ = 12;
   private static final int MAX_PROTOTYPE_ID = 12;

   public NativeObject() {
      super();
   }

   static void init(Scriptable scope, boolean sealed) {
      NativeObject obj = new NativeObject();
      obj.exportAsJSClass(12, scope, sealed);
   }

   public String getClassName() {
      return "Object";
   }

   public String toString() {
      return ScriptRuntime.defaultObjectToString(this);
   }

   protected void fillConstructorProperties(IdFunctionObject ctor) {
      this.addIdFunctionProperty(ctor, OBJECT_TAG, -1, "getPrototypeOf", 1);
      this.addIdFunctionProperty(ctor, OBJECT_TAG, -2, "keys", 1);
      this.addIdFunctionProperty(ctor, OBJECT_TAG, -3, "getOwnPropertyNames", 1);
      this.addIdFunctionProperty(ctor, OBJECT_TAG, -4, "getOwnPropertyDescriptor", 2);
      this.addIdFunctionProperty(ctor, OBJECT_TAG, -5, "defineProperty", 3);
      this.addIdFunctionProperty(ctor, OBJECT_TAG, -6, "isExtensible", 1);
      this.addIdFunctionProperty(ctor, OBJECT_TAG, -7, "preventExtensions", 1);
      this.addIdFunctionProperty(ctor, OBJECT_TAG, -8, "defineProperties", 2);
      this.addIdFunctionProperty(ctor, OBJECT_TAG, -9, "create", 2);
      this.addIdFunctionProperty(ctor, OBJECT_TAG, -10, "isSealed", 1);
      this.addIdFunctionProperty(ctor, OBJECT_TAG, -11, "isFrozen", 1);
      this.addIdFunctionProperty(ctor, OBJECT_TAG, -12, "seal", 1);
      this.addIdFunctionProperty(ctor, OBJECT_TAG, -13, "freeze", 1);
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
            s = "valueOf";
            break;
         case 5:
            arity = 1;
            s = "hasOwnProperty";
            break;
         case 6:
            arity = 1;
            s = "propertyIsEnumerable";
            break;
         case 7:
            arity = 1;
            s = "isPrototypeOf";
            break;
         case 8:
            arity = 0;
            s = "toSource";
            break;
         case 9:
            arity = 2;
            s = "__defineGetter__";
            break;
         case 10:
            arity = 2;
            s = "__defineSetter__";
            break;
         case 11:
            arity = 1;
            s = "__lookupGetter__";
            break;
         case 12:
            arity = 1;
            s = "__lookupSetter__";
            break;
         default:
            throw new IllegalArgumentException(String.valueOf(id));
      }

      this.initPrototypeMethod(OBJECT_TAG, id, s, arity);
   }

   public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      if (!f.hasTag(OBJECT_TAG)) {
         return super.execIdCall(f, cx, scope, thisObj, args);
      } else {
         int id = f.methodId();
         switch (id) {
            case -13:
               Object arg = args.length < 1 ? Undefined.instance : args[0];
               ScriptableObject obj = ensureScriptableObject(arg);

               for(Object name : obj.getAllIds()) {
                  ScriptableObject desc = obj.getOwnPropertyDescriptor(cx, name);
                  if (this.isDataDescriptor(desc) && Boolean.TRUE.equals(desc.get("writable"))) {
                     desc.put("writable", desc, Boolean.FALSE);
                  }

                  if (Boolean.TRUE.equals(desc.get("configurable"))) {
                     desc.put("configurable", desc, Boolean.FALSE);
                  }

                  obj.defineOwnProperty(cx, name, desc, false);
               }

               obj.preventExtensions();
               return obj;
            case -12:
               Object arg = args.length < 1 ? Undefined.instance : args[0];
               ScriptableObject obj = ensureScriptableObject(arg);

               for(Object name : obj.getAllIds()) {
                  ScriptableObject desc = obj.getOwnPropertyDescriptor(cx, name);
                  if (Boolean.TRUE.equals(desc.get("configurable"))) {
                     desc.put("configurable", desc, Boolean.FALSE);
                     obj.defineOwnProperty(cx, name, desc, false);
                  }
               }

               obj.preventExtensions();
               return obj;
            case -11:
               Object arg = args.length < 1 ? Undefined.instance : args[0];
               ScriptableObject obj = ensureScriptableObject(arg);
               if (obj.isExtensible()) {
                  return Boolean.FALSE;
               } else {
                  for(Object name : obj.getAllIds()) {
                     ScriptableObject desc = obj.getOwnPropertyDescriptor(cx, name);
                     if (Boolean.TRUE.equals(desc.get("configurable"))) {
                        return Boolean.FALSE;
                     }

                     if (this.isDataDescriptor(desc) && Boolean.TRUE.equals(desc.get("writable"))) {
                        return Boolean.FALSE;
                     }
                  }

                  return Boolean.TRUE;
               }
            case -10:
               Object arg = args.length < 1 ? Undefined.instance : args[0];
               ScriptableObject obj = ensureScriptableObject(arg);
               if (obj.isExtensible()) {
                  return Boolean.FALSE;
               } else {
                  for(Object name : obj.getAllIds()) {
                     Object configurable = obj.getOwnPropertyDescriptor(cx, name).get("configurable");
                     if (Boolean.TRUE.equals(configurable)) {
                        return Boolean.FALSE;
                     }
                  }

                  return Boolean.TRUE;
               }
            case -9:
               Object arg = args.length < 1 ? Undefined.instance : args[0];
               Scriptable obj = arg == null ? null : ensureScriptable(arg);
               ScriptableObject newObject = new NativeObject();
               newObject.setParentScope(this.getParentScope());
               newObject.setPrototype(obj);
               if (args.length > 1 && args[1] != Undefined.instance) {
                  Scriptable props = Context.toObject(args[1], this.getParentScope());
                  newObject.defineOwnProperties(cx, ensureScriptableObject(props));
               }

               return newObject;
            case -8:
               Object arg = args.length < 1 ? Undefined.instance : args[0];
               ScriptableObject obj = ensureScriptableObject(arg);
               Object propsObj = args.length < 2 ? Undefined.instance : args[1];
               Scriptable props = Context.toObject(propsObj, this.getParentScope());
               obj.defineOwnProperties(cx, ensureScriptableObject(props));
               return obj;
            case -7:
               Object arg = args.length < 1 ? Undefined.instance : args[0];
               ScriptableObject obj = ensureScriptableObject(arg);
               obj.preventExtensions();
               return obj;
            case -6:
               Object arg = args.length < 1 ? Undefined.instance : args[0];
               ScriptableObject obj = ensureScriptableObject(arg);
               return obj.isExtensible();
            case -5:
               Object arg = args.length < 1 ? Undefined.instance : args[0];
               ScriptableObject obj = ensureScriptableObject(arg);
               Object name = args.length < 2 ? Undefined.instance : args[1];
               Object descArg = args.length < 3 ? Undefined.instance : args[2];
               ScriptableObject desc = ensureScriptableObject(descArg);
               obj.defineOwnProperty(cx, name, desc);
               return obj;
            case -4:
               Object arg = args.length < 1 ? Undefined.instance : args[0];
               ScriptableObject obj = ensureScriptableObject(arg);
               Object nameArg = args.length < 2 ? Undefined.instance : args[1];
               String name = ScriptRuntime.toString(nameArg);
               Scriptable desc = obj.getOwnPropertyDescriptor(cx, name);
               return desc == null ? Undefined.instance : desc;
            case -3:
               Object arg = args.length < 1 ? Undefined.instance : args[0];
               ScriptableObject obj = ensureScriptableObject(arg);
               Object[] ids = obj.getAllIds();

               for(int i = 0; i < ids.length; ++i) {
                  ids[i] = ScriptRuntime.toString(ids[i]);
               }

               return cx.newArray(scope, ids);
            case -2:
               Object arg = args.length < 1 ? Undefined.instance : args[0];
               Scriptable obj = ensureScriptable(arg);
               Object[] ids = obj.getIds();

               for(int i = 0; i < ids.length; ++i) {
                  ids[i] = ScriptRuntime.toString(ids[i]);
               }

               return cx.newArray(scope, ids);
            case -1:
               Object arg = args.length < 1 ? Undefined.instance : args[0];
               Scriptable obj = ensureScriptable(arg);
               return obj.getPrototype();
            case 0:
            default:
               throw new IllegalArgumentException(String.valueOf(id));
            case 1:
               if (thisObj != null) {
                  return f.construct(cx, scope, args);
               } else {
                  if (args.length != 0 && args[0] != null && args[0] != Undefined.instance) {
                     return ScriptRuntime.toObject(cx, scope, args[0]);
                  }

                  return new NativeObject();
               }
            case 2:
            case 3:
               if (cx.hasFeature(4)) {
                  String s = ScriptRuntime.defaultObjectToSource(cx, scope, thisObj, args);
                  int L = s.length();
                  if (L != 0 && s.charAt(0) == '(' && s.charAt(L - 1) == ')') {
                     s = s.substring(1, L - 1);
                  }

                  return s;
               }

               return ScriptRuntime.defaultObjectToString(thisObj);
            case 4:
               return thisObj;
            case 5:
               boolean result;
               if (args.length == 0) {
                  result = false;
               } else {
                  String s = ScriptRuntime.toStringIdOrIndex(cx, args[0]);
                  if (s == null) {
                     int index = ScriptRuntime.lastIndexResult(cx);
                     result = thisObj.has(index, thisObj);
                  } else {
                     result = thisObj.has(s, thisObj);
                  }
               }

               return ScriptRuntime.wrapBoolean(result);
            case 6:
               boolean result;
               if (args.length == 0) {
                  result = false;
               } else {
                  String s = ScriptRuntime.toStringIdOrIndex(cx, args[0]);
                  if (s == null) {
                     int index = ScriptRuntime.lastIndexResult(cx);
                     result = thisObj.has(index, thisObj);
                     if (result && thisObj instanceof ScriptableObject) {
                        ScriptableObject so = (ScriptableObject)thisObj;
                        int attrs = so.getAttributes(index);
                        result = (attrs & 2) == 0;
                     }
                  } else {
                     result = thisObj.has(s, thisObj);
                     if (result && thisObj instanceof ScriptableObject) {
                        ScriptableObject so = (ScriptableObject)thisObj;
                        int attrs = so.getAttributes(s);
                        result = (attrs & 2) == 0;
                     }
                  }
               }

               return ScriptRuntime.wrapBoolean(result);
            case 7:
               boolean result = false;
               if (args.length != 0 && args[0] instanceof Scriptable) {
                  Scriptable v = (Scriptable)args[0];

                  do {
                     v = v.getPrototype();
                     if (v == thisObj) {
                        result = true;
                        break;
                     }
                  } while(v != null);
               }

               return ScriptRuntime.wrapBoolean(result);
            case 8:
               return ScriptRuntime.defaultObjectToSource(cx, scope, thisObj, args);
            case 9:
            case 10:
               if (args.length >= 2 && args[1] instanceof Callable) {
                  if (!(thisObj instanceof ScriptableObject)) {
                     throw Context.reportRuntimeError2("msg.extend.scriptable", thisObj.getClass().getName(), String.valueOf(args[0]));
                  }

                  ScriptableObject so = (ScriptableObject)thisObj;
                  String name = ScriptRuntime.toStringIdOrIndex(cx, args[0]);
                  int index = name != null ? 0 : ScriptRuntime.lastIndexResult(cx);
                  Callable getterOrSetter = (Callable)args[1];
                  boolean isSetter = id == 10;
                  so.setGetterOrSetter(name, index, getterOrSetter, isSetter);
                  if (so instanceof NativeArray) {
                     ((NativeArray)so).setDenseOnly(false);
                  }

                  return Undefined.instance;
               }

               Object badArg = args.length >= 2 ? args[1] : Undefined.instance;
               throw ScriptRuntime.notFunctionError(badArg);
            case 11:
            case 12:
               if (args.length >= 1 && thisObj instanceof ScriptableObject) {
                  ScriptableObject so = (ScriptableObject)thisObj;
                  String name = ScriptRuntime.toStringIdOrIndex(cx, args[0]);
                  int index = name != null ? 0 : ScriptRuntime.lastIndexResult(cx);
                  boolean isSetter = id == 12;

                  Object gs;
                  while(true) {
                     gs = so.getGetterOrSetter(name, index, isSetter);
                     if (gs != null) {
                        break;
                     }

                     Scriptable v = so.getPrototype();
                     if (v == null || !(v instanceof ScriptableObject)) {
                        break;
                     }

                     so = (ScriptableObject)v;
                  }

                  if (gs != null) {
                     return gs;
                  } else {
                     return Undefined.instance;
                  }
               } else {
                  return Undefined.instance;
               }
         }
      }
   }

   public boolean containsKey(Object key) {
      if (key instanceof String) {
         return this.has((String)key, this);
      } else {
         return key instanceof Number ? this.has(((Number)key).intValue(), this) : false;
      }
   }

   public boolean containsValue(Object value) {
      for(Object obj : this.values()) {
         if (value == obj || value != null && value.equals(obj)) {
            return true;
         }
      }

      return false;
   }

   public Object remove(Object key) {
      Object value = this.get(key);
      if (key instanceof String) {
         this.delete((String)key);
      } else if (key instanceof Number) {
         this.delete(((Number)key).intValue());
      }

      return value;
   }

   public Set keySet() {
      return new KeySet();
   }

   public Collection values() {
      return new ValueCollection();
   }

   public Set entrySet() {
      return new EntrySet();
   }

   public Object put(Object key, Object value) {
      throw new UnsupportedOperationException();
   }

   public void putAll(Map m) {
      throw new UnsupportedOperationException();
   }

   public void clear() {
      throw new UnsupportedOperationException();
   }

   protected int findPrototypeId(String s) {
      int id = 0;
      String X = null;
      switch (s.length()) {
         case 7:
            X = "valueOf";
            id = 4;
            break;
         case 8:
            int c = s.charAt(3);
            if (c == 111) {
               X = "toSource";
               id = 8;
            } else if (c == 116) {
               X = "toString";
               id = 2;
            }
         case 9:
         case 10:
         case 12:
         case 15:
         case 17:
         case 18:
         case 19:
         default:
            break;
         case 11:
            X = "constructor";
            id = 1;
            break;
         case 13:
            X = "isPrototypeOf";
            id = 7;
            break;
         case 14:
            int var7 = s.charAt(0);
            if (var7 == 'h') {
               X = "hasOwnProperty";
               id = 5;
            } else if (var7 == 't') {
               X = "toLocaleString";
               id = 3;
            }
            break;
         case 16:
            int c = s.charAt(2);
            if (c == 'd') {
               c = s.charAt(8);
               if (c == 'G') {
                  X = "__defineGetter__";
                  id = 9;
               } else if (c == 'S') {
                  X = "__defineSetter__";
                  id = 10;
               }
            } else if (c == 'l') {
               c = s.charAt(8);
               if (c == 'G') {
                  X = "__lookupGetter__";
                  id = 11;
               } else if (c == 'S') {
                  X = "__lookupSetter__";
                  id = 12;
               }
            }
            break;
         case 20:
            X = "propertyIsEnumerable";
            id = 6;
      }

      if (X != null && X != s && !X.equals(s)) {
         id = 0;
      }

      return id;
   }

   class EntrySet extends AbstractSet {
      EntrySet() {
         super();
      }

      public Iterator iterator() {
         return new Iterator() {
            Object[] ids = NativeObject.this.getIds();
            Object key = null;
            int index = 0;

            public boolean hasNext() {
               return this.index < this.ids.length;
            }

            public Map.Entry next() {
               final Object ekey = this.key = this.ids[this.index++];
               final Object value = NativeObject.this.get(this.key);
               return new Map.Entry() {
                  public Object getKey() {
                     return ekey;
                  }

                  public Object getValue() {
                     return value;
                  }

                  public Object setValue(Object valuex) {
                     throw new UnsupportedOperationException();
                  }

                  public boolean equals(Object other) {
                     if (!(other instanceof Map.Entry)) {
                        return false;
                     } else {
                        boolean var10000;
                        label38: {
                           label27: {
                              Map.Entry e = (Map.Entry)other;
                              if (ekey == null) {
                                 if (e.getKey() != null) {
                                    break label27;
                                 }
                              } else if (!ekey.equals(e.getKey())) {
                                 break label27;
                              }

                              if (value == null) {
                                 if (e.getValue() == null) {
                                    break label38;
                                 }
                              } else if (value.equals(e.getValue())) {
                                 break label38;
                              }
                           }

                           var10000 = false;
                           return var10000;
                        }

                        var10000 = true;
                        return var10000;
                     }
                  }

                  public int hashCode() {
                     return (ekey == null ? 0 : ekey.hashCode()) ^ (value == null ? 0 : value.hashCode());
                  }

                  public String toString() {
                     return ekey + "=" + value;
                  }
               };
            }

            public void remove() {
               if (this.key == null) {
                  throw new IllegalStateException();
               } else {
                  NativeObject.this.remove(this.key);
                  this.key = null;
               }
            }
         };
      }

      public int size() {
         return NativeObject.this.size();
      }
   }

   class KeySet extends AbstractSet {
      KeySet() {
         super();
      }

      public boolean contains(Object key) {
         return NativeObject.this.containsKey(key);
      }

      public Iterator iterator() {
         return new Iterator() {
            Object[] ids = NativeObject.this.getIds();
            Object key;
            int index = 0;

            public boolean hasNext() {
               return this.index < this.ids.length;
            }

            public Object next() {
               try {
                  return this.key = this.ids[this.index++];
               } catch (ArrayIndexOutOfBoundsException var2) {
                  this.key = null;
                  throw new NoSuchElementException();
               }
            }

            public void remove() {
               if (this.key == null) {
                  throw new IllegalStateException();
               } else {
                  NativeObject.this.remove(this.key);
                  this.key = null;
               }
            }
         };
      }

      public int size() {
         return NativeObject.this.size();
      }
   }

   class ValueCollection extends AbstractCollection {
      ValueCollection() {
         super();
      }

      public Iterator iterator() {
         return new Iterator() {
            Object[] ids = NativeObject.this.getIds();
            Object key;
            int index = 0;

            public boolean hasNext() {
               return this.index < this.ids.length;
            }

            public Object next() {
               return NativeObject.this.get(this.key = this.ids[this.index++]);
            }

            public void remove() {
               if (this.key == null) {
                  throw new IllegalStateException();
               } else {
                  NativeObject.this.remove(this.key);
                  this.key = null;
               }
            }
         };
      }

      public int size() {
         return NativeObject.this.size();
      }
   }
}
