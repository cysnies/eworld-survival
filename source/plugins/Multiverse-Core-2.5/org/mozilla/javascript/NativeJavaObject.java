package org.mozilla.javascript;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

public class NativeJavaObject implements Scriptable, Wrapper, Serializable {
   static final long serialVersionUID = -6948590651130498591L;
   private static final int JSTYPE_UNDEFINED = 0;
   private static final int JSTYPE_NULL = 1;
   private static final int JSTYPE_BOOLEAN = 2;
   private static final int JSTYPE_NUMBER = 3;
   private static final int JSTYPE_STRING = 4;
   private static final int JSTYPE_JAVA_CLASS = 5;
   private static final int JSTYPE_JAVA_OBJECT = 6;
   private static final int JSTYPE_JAVA_ARRAY = 7;
   private static final int JSTYPE_OBJECT = 8;
   static final byte CONVERSION_TRIVIAL = 1;
   static final byte CONVERSION_NONTRIVIAL = 0;
   static final byte CONVERSION_NONE = 99;
   protected Scriptable prototype;
   protected Scriptable parent;
   protected transient Object javaObject;
   protected transient Class staticType;
   protected transient JavaMembers members;
   private transient Map fieldAndMethods;
   protected transient boolean isAdapter;
   private static final Object COERCED_INTERFACE_KEY = "Coerced Interface";
   private static Method adapter_writeAdapterObject;
   private static Method adapter_readAdapterObject;

   public NativeJavaObject() {
      super();
   }

   public NativeJavaObject(Scriptable scope, Object javaObject, Class staticType) {
      this(scope, javaObject, staticType, false);
   }

   public NativeJavaObject(Scriptable scope, Object javaObject, Class staticType, boolean isAdapter) {
      super();
      this.parent = scope;
      this.javaObject = javaObject;
      this.staticType = staticType;
      this.isAdapter = isAdapter;
      this.initMembers();
   }

   protected void initMembers() {
      Class<?> dynamicType;
      if (this.javaObject != null) {
         dynamicType = this.javaObject.getClass();
      } else {
         dynamicType = this.staticType;
      }

      this.members = JavaMembers.lookupClass(this.parent, dynamicType, this.staticType, this.isAdapter);
      this.fieldAndMethods = this.members.getFieldAndMethodsObjects(this, this.javaObject, false);
   }

   public boolean has(String name, Scriptable start) {
      return this.members.has(name, false);
   }

   public boolean has(int index, Scriptable start) {
      return false;
   }

   public Object get(String name, Scriptable start) {
      if (this.fieldAndMethods != null) {
         Object result = this.fieldAndMethods.get(name);
         if (result != null) {
            return result;
         }
      }

      return this.members.get(this, name, this.javaObject, false);
   }

   public Object get(int index, Scriptable start) {
      throw this.members.reportMemberNotFound(Integer.toString(index));
   }

   public void put(String name, Scriptable start, Object value) {
      if (this.prototype != null && !this.members.has(name, false)) {
         this.prototype.put(name, this.prototype, value);
      } else {
         this.members.put(this, name, this.javaObject, value, false);
      }

   }

   public void put(int index, Scriptable start, Object value) {
      throw this.members.reportMemberNotFound(Integer.toString(index));
   }

   public boolean hasInstance(Scriptable value) {
      return false;
   }

   public void delete(String name) {
   }

   public void delete(int index) {
   }

   public Scriptable getPrototype() {
      return this.prototype == null && this.javaObject instanceof String ? TopLevel.getBuiltinPrototype(ScriptableObject.getTopLevelScope(this.parent), TopLevel.Builtins.String) : this.prototype;
   }

   public void setPrototype(Scriptable m) {
      this.prototype = m;
   }

   public Scriptable getParentScope() {
      return this.parent;
   }

   public void setParentScope(Scriptable m) {
      this.parent = m;
   }

   public Object[] getIds() {
      return this.members.getIds(false);
   }

   /** @deprecated */
   public static Object wrap(Scriptable scope, Object obj, Class staticType) {
      Context cx = Context.getContext();
      return cx.getWrapFactory().wrap(cx, scope, obj, staticType);
   }

   public Object unwrap() {
      return this.javaObject;
   }

   public String getClassName() {
      return "JavaObject";
   }

   public Object getDefaultValue(Class hint) {
      if (hint == null && this.javaObject instanceof Boolean) {
         hint = ScriptRuntime.BooleanClass;
      }

      Object value;
      if (hint != null && hint != ScriptRuntime.StringClass) {
         String converterName;
         if (hint == ScriptRuntime.BooleanClass) {
            converterName = "booleanValue";
         } else {
            if (hint != ScriptRuntime.NumberClass) {
               throw Context.reportRuntimeError0("msg.default.value");
            }

            converterName = "doubleValue";
         }

         Object converterObject = this.get(converterName, this);
         if (converterObject instanceof Function) {
            Function f = (Function)converterObject;
            value = f.call(Context.getContext(), f.getParentScope(), this, ScriptRuntime.emptyArgs);
         } else if (hint == ScriptRuntime.NumberClass && this.javaObject instanceof Boolean) {
            boolean b = (Boolean)this.javaObject;
            value = ScriptRuntime.wrapNumber(b ? (double)1.0F : (double)0.0F);
         } else {
            value = this.javaObject.toString();
         }
      } else {
         value = this.javaObject.toString();
      }

      return value;
   }

   public static boolean canConvert(Object fromObj, Class to) {
      int weight = getConversionWeight(fromObj, to);
      return weight < 99;
   }

   static int getConversionWeight(Object fromObj, Class to) {
      int fromCode = getJSTypeCode(fromObj);
      switch (fromCode) {
         case 0:
            if (to == ScriptRuntime.StringClass || to == ScriptRuntime.ObjectClass) {
               return 1;
            }
            break;
         case 1:
            if (!to.isPrimitive()) {
               return 1;
            }
            break;
         case 2:
            if (to == Boolean.TYPE) {
               return 1;
            }

            if (to == ScriptRuntime.BooleanClass) {
               return 2;
            }

            if (to == ScriptRuntime.ObjectClass) {
               return 3;
            }

            if (to == ScriptRuntime.StringClass) {
               return 4;
            }
            break;
         case 3:
            if (to.isPrimitive()) {
               if (to == Double.TYPE) {
                  return 1;
               }

               if (to != Boolean.TYPE) {
                  return 1 + getSizeRank(to);
               }
            } else {
               if (to == ScriptRuntime.StringClass) {
                  return 9;
               }

               if (to == ScriptRuntime.ObjectClass) {
                  return 10;
               }

               if (ScriptRuntime.NumberClass.isAssignableFrom(to)) {
                  return 2;
               }
            }
            break;
         case 4:
            if (to == ScriptRuntime.StringClass) {
               return 1;
            }

            if (to.isInstance(fromObj)) {
               return 2;
            }

            if (to.isPrimitive()) {
               if (to == Character.TYPE) {
                  return 3;
               }

               if (to != Boolean.TYPE) {
                  return 4;
               }
            }
            break;
         case 5:
            if (to == ScriptRuntime.ClassClass) {
               return 1;
            }

            if (to == ScriptRuntime.ObjectClass) {
               return 3;
            }

            if (to == ScriptRuntime.StringClass) {
               return 4;
            }
            break;
         case 6:
         case 7:
            Object javaObj = fromObj;
            if (fromObj instanceof Wrapper) {
               javaObj = ((Wrapper)fromObj).unwrap();
            }

            if (to.isInstance(javaObj)) {
               return 0;
            }

            if (to == ScriptRuntime.StringClass) {
               return 2;
            }

            if (to.isPrimitive() && to != Boolean.TYPE) {
               return fromCode == 7 ? 99 : 2 + getSizeRank(to);
            }
            break;
         case 8:
            if (to != ScriptRuntime.ObjectClass && to.isInstance(fromObj)) {
               return 1;
            }

            if (to.isArray()) {
               if (fromObj instanceof NativeArray) {
                  return 2;
               }
            } else {
               if (to == ScriptRuntime.ObjectClass) {
                  return 3;
               }

               if (to == ScriptRuntime.StringClass) {
                  return 4;
               }

               if (to == ScriptRuntime.DateClass) {
                  if (fromObj instanceof NativeDate) {
                     return 1;
                  }
               } else {
                  if (to.isInterface()) {
                     if (!(fromObj instanceof NativeObject) && !(fromObj instanceof NativeFunction)) {
                        return 12;
                     }

                     return 1;
                  }

                  if (to.isPrimitive() && to != Boolean.TYPE) {
                     return 4 + getSizeRank(to);
                  }
               }
            }
      }

      return 99;
   }

   static int getSizeRank(Class aType) {
      if (aType == Double.TYPE) {
         return 1;
      } else if (aType == Float.TYPE) {
         return 2;
      } else if (aType == Long.TYPE) {
         return 3;
      } else if (aType == Integer.TYPE) {
         return 4;
      } else if (aType == Short.TYPE) {
         return 5;
      } else if (aType == Character.TYPE) {
         return 6;
      } else if (aType == Byte.TYPE) {
         return 7;
      } else {
         return aType == Boolean.TYPE ? 99 : 8;
      }
   }

   private static int getJSTypeCode(Object value) {
      if (value == null) {
         return 1;
      } else if (value == Undefined.instance) {
         return 0;
      } else if (value instanceof CharSequence) {
         return 4;
      } else if (value instanceof Number) {
         return 3;
      } else if (value instanceof Boolean) {
         return 2;
      } else if (value instanceof Scriptable) {
         if (value instanceof NativeJavaClass) {
            return 5;
         } else if (value instanceof NativeJavaArray) {
            return 7;
         } else {
            return value instanceof Wrapper ? 6 : 8;
         }
      } else if (value instanceof Class) {
         return 5;
      } else {
         Class<?> valueClass = value.getClass();
         return valueClass.isArray() ? 7 : 6;
      }
   }

   /** @deprecated */
   public static Object coerceType(Class type, Object value) {
      return coerceTypeImpl(type, value);
   }

   static Object coerceTypeImpl(Class type, Object value) {
      if (value != null && value.getClass() == type) {
         return value;
      } else {
         switch (getJSTypeCode(value)) {
            case 0:
               if (type == ScriptRuntime.StringClass || type == ScriptRuntime.ObjectClass) {
                  return "undefined";
               }

               reportConversionError("undefined", type);
               break;
            case 1:
               if (type.isPrimitive()) {
                  reportConversionError(value, type);
               }

               return null;
            case 2:
               if (type == Boolean.TYPE || type == ScriptRuntime.BooleanClass || type == ScriptRuntime.ObjectClass) {
                  return value;
               }

               if (type == ScriptRuntime.StringClass) {
                  return value.toString();
               }

               reportConversionError(value, type);
               break;
            case 3:
               if (type == ScriptRuntime.StringClass) {
                  return ScriptRuntime.toString(value);
               }

               if (type == ScriptRuntime.ObjectClass) {
                  return coerceToNumber(Double.TYPE, value);
               }

               if (type.isPrimitive() && type != Boolean.TYPE || ScriptRuntime.NumberClass.isAssignableFrom(type)) {
                  return coerceToNumber(type, value);
               }

               reportConversionError(value, type);
               break;
            case 4:
               if (type == ScriptRuntime.StringClass || type.isInstance(value)) {
                  return value.toString();
               }

               if (type != Character.TYPE && type != ScriptRuntime.CharacterClass) {
                  if (type.isPrimitive() && type != Boolean.TYPE || ScriptRuntime.NumberClass.isAssignableFrom(type)) {
                     return coerceToNumber(type, value);
                  }

                  reportConversionError(value, type);
                  break;
               }

               if (((CharSequence)value).length() == 1) {
                  return ((CharSequence)value).charAt(0);
               }

               return coerceToNumber(type, value);
            case 5:
               if (value instanceof Wrapper) {
                  value = ((Wrapper)value).unwrap();
               }

               if (type == ScriptRuntime.ClassClass || type == ScriptRuntime.ObjectClass) {
                  return value;
               }

               if (type == ScriptRuntime.StringClass) {
                  return value.toString();
               }

               reportConversionError(value, type);
               break;
            case 6:
            case 7:
               if (value instanceof Wrapper) {
                  value = ((Wrapper)value).unwrap();
               }

               if (type.isPrimitive()) {
                  if (type == Boolean.TYPE) {
                     reportConversionError(value, type);
                  }

                  return coerceToNumber(type, value);
               }

               if (type == ScriptRuntime.StringClass) {
                  return value.toString();
               }

               if (type.isInstance(value)) {
                  return value;
               }

               reportConversionError(value, type);
               break;
            case 8:
               if (type == ScriptRuntime.StringClass) {
                  return ScriptRuntime.toString(value);
               }

               if (type.isPrimitive()) {
                  if (type == Boolean.TYPE) {
                     reportConversionError(value, type);
                  }

                  return coerceToNumber(type, value);
               }

               if (type.isInstance(value)) {
                  return value;
               }

               if (type == ScriptRuntime.DateClass && value instanceof NativeDate) {
                  double time = ((NativeDate)value).getJSTimeValue();
                  return new Date((long)time);
               }

               if (type.isArray() && value instanceof NativeArray) {
                  NativeArray array = (NativeArray)value;
                  long length = array.getLength();
                  Class<?> arrayType = type.getComponentType();
                  Object Result = Array.newInstance(arrayType, (int)length);

                  for(int i = 0; (long)i < length; ++i) {
                     try {
                        Array.set(Result, i, coerceTypeImpl(arrayType, array.get(i, array)));
                     } catch (EvaluatorException var11) {
                        reportConversionError(value, type);
                     }
                  }

                  return Result;
               }

               if (value instanceof Wrapper) {
                  value = ((Wrapper)value).unwrap();
                  if (type.isInstance(value)) {
                     return value;
                  }

                  reportConversionError(value, type);
               } else {
                  if (type.isInterface() && (value instanceof NativeObject || value instanceof NativeFunction)) {
                     return createInterfaceAdapter(type, (ScriptableObject)value);
                  }

                  reportConversionError(value, type);
               }
         }

         return value;
      }
   }

   protected static Object createInterfaceAdapter(Class type, ScriptableObject so) {
      Object key = Kit.makeHashKeyFromPair(COERCED_INTERFACE_KEY, type);
      Object old = so.getAssociatedValue(key);
      if (old != null) {
         return old;
      } else {
         Context cx = Context.getContext();
         Object glue = InterfaceAdapter.create(cx, type, so);
         glue = so.associateValue(key, glue);
         return glue;
      }
   }

   private static Object coerceToNumber(Class type, Object value) {
      Class<?> valueClass = value.getClass();
      if (type != Character.TYPE && type != ScriptRuntime.CharacterClass) {
         if (type != ScriptRuntime.ObjectClass && type != ScriptRuntime.DoubleClass && type != Double.TYPE) {
            if (type != ScriptRuntime.FloatClass && type != Float.TYPE) {
               if (type != ScriptRuntime.IntegerClass && type != Integer.TYPE) {
                  if (type != ScriptRuntime.LongClass && type != Long.TYPE) {
                     if (type != ScriptRuntime.ShortClass && type != Short.TYPE) {
                        if (type != ScriptRuntime.ByteClass && type != Byte.TYPE) {
                           return new Double(toDouble(value));
                        } else {
                           return valueClass == ScriptRuntime.ByteClass ? value : (byte)((int)toInteger(value, ScriptRuntime.ByteClass, (double)-128.0F, (double)127.0F));
                        }
                     } else {
                        return valueClass == ScriptRuntime.ShortClass ? value : (short)((int)toInteger(value, ScriptRuntime.ShortClass, (double)-32768.0F, (double)32767.0F));
                     }
                  } else if (valueClass == ScriptRuntime.LongClass) {
                     return value;
                  } else {
                     double max = Double.longBitsToDouble(4890909195324358655L);
                     double min = Double.longBitsToDouble(-4332462841530417152L);
                     return toInteger(value, ScriptRuntime.LongClass, min, max);
                  }
               } else {
                  return valueClass == ScriptRuntime.IntegerClass ? value : (int)toInteger(value, ScriptRuntime.IntegerClass, (double)Integer.MIN_VALUE, (double)Integer.MAX_VALUE);
               }
            } else if (valueClass == ScriptRuntime.FloatClass) {
               return value;
            } else {
               double number = toDouble(value);
               if (!Double.isInfinite(number) && !Double.isNaN(number) && number != (double)0.0F) {
                  double absNumber = Math.abs(number);
                  if (absNumber < (double)Float.MIN_VALUE) {
                     return new Float(number > (double)0.0F ? (double)0.0F : (double)-0.0F);
                  } else {
                     return absNumber > (double)Float.MAX_VALUE ? new Float(number > (double)0.0F ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY) : new Float((float)number);
                  }
               } else {
                  return new Float((float)number);
               }
            }
         } else {
            return valueClass == ScriptRuntime.DoubleClass ? value : new Double(toDouble(value));
         }
      } else {
         return valueClass == ScriptRuntime.CharacterClass ? value : (char)((int)toInteger(value, ScriptRuntime.CharacterClass, (double)0.0F, (double)65535.0F));
      }
   }

   private static double toDouble(Object value) {
      if (value instanceof Number) {
         return ((Number)value).doubleValue();
      } else if (value instanceof String) {
         return ScriptRuntime.toNumber((String)value);
      } else if (value instanceof Scriptable) {
         return value instanceof Wrapper ? toDouble(((Wrapper)value).unwrap()) : ScriptRuntime.toNumber(value);
      } else {
         Method meth;
         try {
            meth = value.getClass().getMethod("doubleValue", (Class[])null);
         } catch (NoSuchMethodException var5) {
            meth = null;
         } catch (SecurityException var6) {
            meth = null;
         }

         if (meth != null) {
            try {
               return ((Number)meth.invoke(value, (Object[])null)).doubleValue();
            } catch (IllegalAccessException var3) {
               reportConversionError(value, Double.TYPE);
            } catch (InvocationTargetException var4) {
               reportConversionError(value, Double.TYPE);
            }
         }

         return ScriptRuntime.toNumber(value.toString());
      }
   }

   private static long toInteger(Object value, Class type, double min, double max) {
      double d = toDouble(value);
      if (Double.isInfinite(d) || Double.isNaN(d)) {
         reportConversionError(ScriptRuntime.toString(value), type);
      }

      if (d > (double)0.0F) {
         d = Math.floor(d);
      } else {
         d = Math.ceil(d);
      }

      if (d < min || d > max) {
         reportConversionError(ScriptRuntime.toString(value), type);
      }

      return (long)d;
   }

   static void reportConversionError(Object value, Class type) {
      throw Context.reportRuntimeError2("msg.conversion.not.allowed", String.valueOf(value), JavaMembers.javaSignature(type));
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.defaultWriteObject();
      out.writeBoolean(this.isAdapter);
      if (this.isAdapter) {
         if (adapter_writeAdapterObject == null) {
            throw new IOException();
         }

         Object[] args = new Object[]{this.javaObject, out};

         try {
            adapter_writeAdapterObject.invoke((Object)null, args);
         } catch (Exception var4) {
            throw new IOException();
         }
      } else {
         out.writeObject(this.javaObject);
      }

      if (this.staticType != null) {
         out.writeObject(this.staticType.getClass().getName());
      } else {
         out.writeObject((Object)null);
      }

   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      in.defaultReadObject();
      this.isAdapter = in.readBoolean();
      if (this.isAdapter) {
         if (adapter_readAdapterObject == null) {
            throw new ClassNotFoundException();
         }

         Object[] args = new Object[]{this, in};

         try {
            this.javaObject = adapter_readAdapterObject.invoke((Object)null, args);
         } catch (Exception var4) {
            throw new IOException();
         }
      } else {
         this.javaObject = in.readObject();
      }

      String className = (String)in.readObject();
      if (className != null) {
         this.staticType = Class.forName(className);
      } else {
         this.staticType = null;
      }

      this.initMembers();
   }

   static {
      Class<?>[] sig2 = new Class[2];
      Class<?> cl = Kit.classOrNull("org.mozilla.javascript.JavaAdapter");
      if (cl != null) {
         try {
            sig2[0] = ScriptRuntime.ObjectClass;
            sig2[1] = Kit.classOrNull("java.io.ObjectOutputStream");
            adapter_writeAdapterObject = cl.getMethod("writeAdapterObject", sig2);
            sig2[0] = ScriptRuntime.ScriptableClass;
            sig2[1] = Kit.classOrNull("java.io.ObjectInputStream");
            adapter_readAdapterObject = cl.getMethod("readAdapterObject", sig2);
         } catch (NoSuchMethodException var3) {
            adapter_writeAdapterObject = null;
            adapter_readAdapterObject = null;
         }
      }

   }
}
