package org.mozilla.javascript;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.mozilla.javascript.annotations.JSConstructor;
import org.mozilla.javascript.annotations.JSFunction;
import org.mozilla.javascript.annotations.JSGetter;
import org.mozilla.javascript.annotations.JSSetter;
import org.mozilla.javascript.annotations.JSStaticFunction;
import org.mozilla.javascript.debug.DebuggableObject;

public abstract class ScriptableObject implements Scriptable, Serializable, DebuggableObject, ConstProperties {
   static final long serialVersionUID = 2829861078851942586L;
   public static final int EMPTY = 0;
   public static final int READONLY = 1;
   public static final int DONTENUM = 2;
   public static final int PERMANENT = 4;
   public static final int UNINITIALIZED_CONST = 8;
   public static final int CONST = 13;
   private Scriptable prototypeObject;
   private Scriptable parentScopeObject;
   private transient Slot[] slots;
   private int count;
   private transient Slot firstAdded;
   private transient Slot lastAdded;
   private volatile Map associatedValues;
   private static final int SLOT_QUERY = 1;
   private static final int SLOT_MODIFY = 2;
   private static final int SLOT_MODIFY_CONST = 3;
   private static final int SLOT_MODIFY_GETTER_SETTER = 4;
   private static final int SLOT_CONVERT_ACCESSOR_TO_DATA = 5;
   private static final int INITIAL_SLOT_SIZE = 4;
   private boolean isExtensible = true;

   protected static ScriptableObject buildDataDescriptor(Scriptable scope, Object value, int attributes) {
      ScriptableObject desc = new NativeObject();
      ScriptRuntime.setBuiltinProtoAndParent(desc, scope, TopLevel.Builtins.Object);
      desc.defineProperty("value", (Object)value, 0);
      desc.defineProperty("writable", (Object)((attributes & 1) == 0), 0);
      desc.defineProperty("enumerable", (Object)((attributes & 2) == 0), 0);
      desc.defineProperty("configurable", (Object)((attributes & 4) == 0), 0);
      return desc;
   }

   static void checkValidAttributes(int attributes) {
      int mask = 15;
      if ((attributes & -16) != 0) {
         throw new IllegalArgumentException(String.valueOf(attributes));
      }
   }

   public ScriptableObject() {
      super();
   }

   public ScriptableObject(Scriptable scope, Scriptable prototype) {
      super();
      if (scope == null) {
         throw new IllegalArgumentException();
      } else {
         this.parentScopeObject = scope;
         this.prototypeObject = prototype;
      }
   }

   public String getTypeOf() {
      return this.avoidObjectDetection() ? "undefined" : "object";
   }

   public abstract String getClassName();

   public boolean has(String name, Scriptable start) {
      return null != this.getSlot(name, 0, 1);
   }

   public boolean has(int index, Scriptable start) {
      return null != this.getSlot((String)null, index, 1);
   }

   public Object get(String name, Scriptable start) {
      Slot slot = this.getSlot(name, 0, 1);
      return slot == null ? Scriptable.NOT_FOUND : slot.getValue(start);
   }

   public Object get(int index, Scriptable start) {
      Slot slot = this.getSlot((String)null, index, 1);
      return slot == null ? Scriptable.NOT_FOUND : slot.getValue(start);
   }

   public void put(String name, Scriptable start, Object value) {
      if (!this.putImpl(name, 0, start, value)) {
         if (start == this) {
            throw Kit.codeBug();
         } else {
            start.put(name, start, value);
         }
      }
   }

   public void put(int index, Scriptable start, Object value) {
      if (!this.putImpl((String)null, index, start, value)) {
         if (start == this) {
            throw Kit.codeBug();
         } else {
            start.put(index, start, value);
         }
      }
   }

   public void delete(String name) {
      this.checkNotSealed(name, 0);
      this.removeSlot(name, 0);
   }

   public void delete(int index) {
      this.checkNotSealed((String)null, index);
      this.removeSlot((String)null, index);
   }

   public void putConst(String name, Scriptable start, Object value) {
      if (!this.putConstImpl(name, 0, start, value, 1)) {
         if (start == this) {
            throw Kit.codeBug();
         } else {
            if (start instanceof ConstProperties) {
               ((ConstProperties)start).putConst(name, start, value);
            } else {
               start.put(name, start, value);
            }

         }
      }
   }

   public void defineConst(String name, Scriptable start) {
      if (!this.putConstImpl(name, 0, start, Undefined.instance, 8)) {
         if (start == this) {
            throw Kit.codeBug();
         } else {
            if (start instanceof ConstProperties) {
               ((ConstProperties)start).defineConst(name, start);
            }

         }
      }
   }

   public boolean isConst(String name) {
      Slot slot = this.getSlot(name, 0, 1);
      if (slot == null) {
         return false;
      } else {
         return (slot.getAttributes() & 5) == 5;
      }
   }

   /** @deprecated */
   public final int getAttributes(String name, Scriptable start) {
      return this.getAttributes(name);
   }

   /** @deprecated */
   public final int getAttributes(int index, Scriptable start) {
      return this.getAttributes(index);
   }

   /** @deprecated */
   public final void setAttributes(String name, Scriptable start, int attributes) {
      this.setAttributes(name, attributes);
   }

   /** @deprecated */
   public void setAttributes(int index, Scriptable start, int attributes) {
      this.setAttributes(index, attributes);
   }

   public int getAttributes(String name) {
      return this.findAttributeSlot(name, 0, 1).getAttributes();
   }

   public int getAttributes(int index) {
      return this.findAttributeSlot((String)null, index, 1).getAttributes();
   }

   public void setAttributes(String name, int attributes) {
      this.checkNotSealed(name, 0);
      this.findAttributeSlot(name, 0, 2).setAttributes(attributes);
   }

   public void setAttributes(int index, int attributes) {
      this.checkNotSealed((String)null, index);
      this.findAttributeSlot((String)null, index, 2).setAttributes(attributes);
   }

   public void setGetterOrSetter(String name, int index, Callable getterOrSetter, boolean isSetter) {
      this.setGetterOrSetter(name, index, getterOrSetter, isSetter, false);
   }

   private void setGetterOrSetter(String name, int index, Callable getterOrSetter, boolean isSetter, boolean force) {
      if (name != null && index != 0) {
         throw new IllegalArgumentException(name);
      } else {
         if (!force) {
            this.checkNotSealed(name, index);
         }

         GetterSlot gslot;
         if (this.isExtensible()) {
            gslot = (GetterSlot)this.getSlot(name, index, 4);
         } else {
            Slot slot = unwrapSlot(this.getSlot(name, index, 1));
            if (!(slot instanceof GetterSlot)) {
               return;
            }

            gslot = (GetterSlot)slot;
         }

         if (!force) {
            int attributes = gslot.getAttributes();
            if ((attributes & 1) != 0) {
               throw Context.reportRuntimeError1("msg.modify.readonly", name);
            }
         }

         if (isSetter) {
            gslot.setter = getterOrSetter;
         } else {
            gslot.getter = getterOrSetter;
         }

         gslot.value = Undefined.instance;
      }
   }

   public Object getGetterOrSetter(String name, int index, boolean isSetter) {
      if (name != null && index != 0) {
         throw new IllegalArgumentException(name);
      } else {
         Slot slot = unwrapSlot(this.getSlot(name, index, 1));
         if (slot == null) {
            return null;
         } else if (slot instanceof GetterSlot) {
            GetterSlot gslot = (GetterSlot)slot;
            Object result = isSetter ? gslot.setter : gslot.getter;
            return result != null ? result : Undefined.instance;
         } else {
            return Undefined.instance;
         }
      }
   }

   protected boolean isGetterOrSetter(String name, int index, boolean setter) {
      Slot slot = unwrapSlot(this.getSlot(name, index, 1));
      if (slot instanceof GetterSlot) {
         if (setter && ((GetterSlot)slot).setter != null) {
            return true;
         }

         if (!setter && ((GetterSlot)slot).getter != null) {
            return true;
         }
      }

      return false;
   }

   void addLazilyInitializedValue(String name, int index, LazilyLoadedCtor init, int attributes) {
      if (name != null && index != 0) {
         throw new IllegalArgumentException(name);
      } else {
         this.checkNotSealed(name, index);
         GetterSlot gslot = (GetterSlot)this.getSlot(name, index, 4);
         gslot.setAttributes(attributes);
         gslot.getter = null;
         gslot.setter = null;
         gslot.value = init;
      }
   }

   public Scriptable getPrototype() {
      return this.prototypeObject;
   }

   public void setPrototype(Scriptable m) {
      this.prototypeObject = m;
   }

   public Scriptable getParentScope() {
      return this.parentScopeObject;
   }

   public void setParentScope(Scriptable m) {
      this.parentScopeObject = m;
   }

   public Object[] getIds() {
      return this.getIds(false);
   }

   public Object[] getAllIds() {
      return this.getIds(true);
   }

   public Object getDefaultValue(Class typeHint) {
      return getDefaultValue(this, typeHint);
   }

   public static Object getDefaultValue(Scriptable object, Class typeHint) {
      Context cx = null;

      for(int i = 0; i < 2; ++i) {
         boolean tryToString;
         if (typeHint == ScriptRuntime.StringClass) {
            tryToString = i == 0;
         } else {
            tryToString = i == 1;
         }

         String methodName;
         Object[] args;
         if (tryToString) {
            methodName = "toString";
            args = ScriptRuntime.emptyArgs;
         } else {
            methodName = "valueOf";
            args = new Object[1];
            String hint;
            if (typeHint == null) {
               hint = "undefined";
            } else if (typeHint == ScriptRuntime.StringClass) {
               hint = "string";
            } else if (typeHint == ScriptRuntime.ScriptableClass) {
               hint = "object";
            } else if (typeHint == ScriptRuntime.FunctionClass) {
               hint = "function";
            } else if (typeHint != ScriptRuntime.BooleanClass && typeHint != Boolean.TYPE) {
               if (typeHint != ScriptRuntime.NumberClass && typeHint != ScriptRuntime.ByteClass && typeHint != Byte.TYPE && typeHint != ScriptRuntime.ShortClass && typeHint != Short.TYPE && typeHint != ScriptRuntime.IntegerClass && typeHint != Integer.TYPE && typeHint != ScriptRuntime.FloatClass && typeHint != Float.TYPE && typeHint != ScriptRuntime.DoubleClass && typeHint != Double.TYPE) {
                  throw Context.reportRuntimeError1("msg.invalid.type", typeHint.toString());
               }

               hint = "number";
            } else {
               hint = "boolean";
            }

            args[0] = hint;
         }

         Object v = getProperty(object, methodName);
         if (v instanceof Function) {
            Function fun = (Function)v;
            if (cx == null) {
               cx = Context.getContext();
            }

            v = fun.call(cx, fun.getParentScope(), object, args);
            if (v != null) {
               if (!(v instanceof Scriptable)) {
                  return v;
               }

               if (typeHint == ScriptRuntime.ScriptableClass || typeHint == ScriptRuntime.FunctionClass) {
                  return v;
               }

               if (tryToString && v instanceof Wrapper) {
                  Object u = ((Wrapper)v).unwrap();
                  if (u instanceof String) {
                     return u;
                  }
               }
            }
         }
      }

      String arg = typeHint == null ? "undefined" : typeHint.getName();
      throw ScriptRuntime.typeError1("msg.default.value", arg);
   }

   public boolean hasInstance(Scriptable instance) {
      return ScriptRuntime.jsDelegatesTo(instance, this);
   }

   public boolean avoidObjectDetection() {
      return false;
   }

   protected Object equivalentValues(Object value) {
      return this == value ? Boolean.TRUE : Scriptable.NOT_FOUND;
   }

   public static void defineClass(Scriptable scope, Class clazz) throws IllegalAccessException, InstantiationException, InvocationTargetException {
      defineClass(scope, clazz, false, false);
   }

   public static void defineClass(Scriptable scope, Class clazz, boolean sealed) throws IllegalAccessException, InstantiationException, InvocationTargetException {
      defineClass(scope, clazz, sealed, false);
   }

   public static String defineClass(Scriptable scope, Class clazz, boolean sealed, boolean mapInheritance) throws IllegalAccessException, InstantiationException, InvocationTargetException {
      BaseFunction ctor = buildClassCtor(scope, clazz, sealed, mapInheritance);
      if (ctor == null) {
         return null;
      } else {
         String name = ctor.getClassPrototype().getClassName();
         defineProperty(scope, name, ctor, 2);
         return name;
      }
   }

   static BaseFunction buildClassCtor(Scriptable scope, Class clazz, boolean sealed, boolean mapInheritance) throws IllegalAccessException, InstantiationException, InvocationTargetException {
      Method[] methods = FunctionObject.getMethodList(clazz);

      for(int i = 0; i < methods.length; ++i) {
         Method method = methods[i];
         if (method.getName().equals("init")) {
            Class<?>[] parmTypes = method.getParameterTypes();
            if (parmTypes.length == 3 && parmTypes[0] == ScriptRuntime.ContextClass && parmTypes[1] == ScriptRuntime.ScriptableClass && parmTypes[2] == Boolean.TYPE && Modifier.isStatic(method.getModifiers())) {
               Object[] args = new Object[]{Context.getContext(), scope, sealed ? Boolean.TRUE : Boolean.FALSE};
               method.invoke((Object)null, args);
               return null;
            }

            if (parmTypes.length == 1 && parmTypes[0] == ScriptRuntime.ScriptableClass && Modifier.isStatic(method.getModifiers())) {
               Object[] args = new Object[]{scope};
               method.invoke((Object)null, args);
               return null;
            }
         }
      }

      Constructor<?>[] ctors = clazz.getConstructors();
      Constructor<?> protoCtor = null;

      for(int i = 0; i < ctors.length; ++i) {
         if (ctors[i].getParameterTypes().length == 0) {
            protoCtor = ctors[i];
            break;
         }
      }

      if (protoCtor == null) {
         throw Context.reportRuntimeError1("msg.zero.arg.ctor", clazz.getName());
      } else {
         Scriptable proto = (Scriptable)protoCtor.newInstance(ScriptRuntime.emptyArgs);
         String className = proto.getClassName();
         Object existing = getProperty(getTopLevelScope(scope), className);
         if (existing instanceof BaseFunction) {
            Object existingProto = ((BaseFunction)existing).getPrototypeProperty();
            if (existingProto != null && clazz.equals(existingProto.getClass())) {
               return (BaseFunction)existing;
            }
         }

         Scriptable superProto = null;
         if (mapInheritance) {
            Class<? super T> superClass = clazz.getSuperclass();
            if (ScriptRuntime.ScriptableClass.isAssignableFrom(superClass) && !Modifier.isAbstract(superClass.getModifiers())) {
               Class<? extends Scriptable> superScriptable = extendsScriptable(superClass);
               String name = defineClass(scope, superScriptable, sealed, mapInheritance);
               if (name != null) {
                  superProto = getClassPrototype(scope, name);
               }
            }
         }

         if (superProto == null) {
            superProto = getObjectPrototype(scope);
         }

         proto.setPrototype(superProto);
         String functionPrefix = "jsFunction_";
         String staticFunctionPrefix = "jsStaticFunction_";
         String getterPrefix = "jsGet_";
         String setterPrefix = "jsSet_";
         String ctorName = "jsConstructor";
         Member ctorMember = findAnnotatedMember(methods, JSConstructor.class);
         if (ctorMember == null) {
            ctorMember = findAnnotatedMember(ctors, JSConstructor.class);
         }

         if (ctorMember == null) {
            ctorMember = FunctionObject.findSingleMethod(methods, "jsConstructor");
         }

         if (ctorMember == null) {
            if (ctors.length == 1) {
               ctorMember = ctors[0];
            } else if (ctors.length == 2) {
               if (ctors[0].getParameterTypes().length == 0) {
                  ctorMember = ctors[1];
               } else if (ctors[1].getParameterTypes().length == 0) {
                  ctorMember = ctors[0];
               }
            }

            if (ctorMember == null) {
               throw Context.reportRuntimeError1("msg.ctor.multiple.parms", clazz.getName());
            }
         }

         FunctionObject ctor = new FunctionObject(className, ctorMember, scope);
         if (ctor.isVarArgsMethod()) {
            throw Context.reportRuntimeError1("msg.varargs.ctor", ctorMember.getName());
         } else {
            ctor.initAsConstructor(scope, proto);
            Method finishInit = null;
            HashSet<String> staticNames = new HashSet();
            HashSet<String> instanceNames = new HashSet();

            for(Method method : methods) {
               if (method != ctorMember) {
                  String name = method.getName();
                  if (name.equals("finishInit")) {
                     Class<?>[] parmTypes = method.getParameterTypes();
                     if (parmTypes.length == 3 && parmTypes[0] == ScriptRuntime.ScriptableClass && parmTypes[1] == FunctionObject.class && parmTypes[2] == ScriptRuntime.ScriptableClass && Modifier.isStatic(method.getModifiers())) {
                        finishInit = method;
                        continue;
                     }
                  }

                  if (name.indexOf(36) == -1 && !name.equals("jsConstructor")) {
                     Annotation annotation = null;
                     String prefix = null;
                     if (method.isAnnotationPresent(JSFunction.class)) {
                        annotation = method.getAnnotation(JSFunction.class);
                     } else if (method.isAnnotationPresent(JSStaticFunction.class)) {
                        annotation = method.getAnnotation(JSStaticFunction.class);
                     } else if (method.isAnnotationPresent(JSGetter.class)) {
                        annotation = method.getAnnotation(JSGetter.class);
                     } else if (method.isAnnotationPresent(JSSetter.class)) {
                        continue;
                     }

                     if (annotation == null) {
                        if (name.startsWith("jsFunction_")) {
                           prefix = "jsFunction_";
                        } else if (name.startsWith("jsStaticFunction_")) {
                           prefix = "jsStaticFunction_";
                        } else if (name.startsWith("jsGet_")) {
                           prefix = "jsGet_";
                        } else if (annotation == null) {
                           continue;
                        }
                     }

                     boolean isStatic = annotation instanceof JSStaticFunction || prefix == "jsStaticFunction_";
                     HashSet<String> names = isStatic ? staticNames : instanceNames;
                     String propName = getPropertyName(name, prefix, annotation);
                     if (names.contains(propName)) {
                        throw Context.reportRuntimeError2("duplicate.defineClass.name", name, propName);
                     }

                     names.add(propName);
                     if (!(annotation instanceof JSGetter) && prefix != "jsGet_") {
                        if (isStatic && !Modifier.isStatic(method.getModifiers())) {
                           throw Context.reportRuntimeError("jsStaticFunction must be used with static method.");
                        }

                        FunctionObject f = new FunctionObject(propName, method, proto);
                        if (f.isVarArgsConstructor()) {
                           throw Context.reportRuntimeError1("msg.varargs.fun", ctorMember.getName());
                        }

                        defineProperty((Scriptable)(isStatic ? ctor : proto), propName, f, 2);
                        if (sealed) {
                           f.sealObject();
                        }
                     } else {
                        if (!(proto instanceof ScriptableObject)) {
                           throw Context.reportRuntimeError2("msg.extend.scriptable", proto.getClass().toString(), propName);
                        }

                        Method setter = findSetterMethod(methods, propName, "jsSet_");
                        int attr = 6 | (setter != null ? 0 : 1);
                        ((ScriptableObject)proto).defineProperty(propName, (Object)null, method, setter, attr);
                     }
                  }
               }
            }

            if (finishInit != null) {
               Object[] finishArgs = new Object[]{scope, ctor, proto};
               finishInit.invoke((Object)null, finishArgs);
            }

            if (sealed) {
               ctor.sealObject();
               if (proto instanceof ScriptableObject) {
                  ((ScriptableObject)proto).sealObject();
               }
            }

            return ctor;
         }
      }
   }

   private static Member findAnnotatedMember(AccessibleObject[] members, Class annotation) {
      for(AccessibleObject member : members) {
         if (member.isAnnotationPresent(annotation)) {
            return (Member)member;
         }
      }

      return null;
   }

   private static Method findSetterMethod(Method[] methods, String name, String prefix) {
      String newStyleName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);

      for(Method method : methods) {
         JSSetter annotation = (JSSetter)method.getAnnotation(JSSetter.class);
         if (annotation != null && (name.equals(annotation.value()) || "".equals(annotation.value()) && newStyleName.equals(method.getName()))) {
            return method;
         }
      }

      String oldStyleName = prefix + name;

      for(Method method : methods) {
         if (oldStyleName.equals(method.getName())) {
            return method;
         }
      }

      return null;
   }

   private static String getPropertyName(String methodName, String prefix, Annotation annotation) {
      if (prefix != null) {
         return methodName.substring(prefix.length());
      } else {
         String propName = null;
         if (annotation instanceof JSGetter) {
            propName = ((JSGetter)annotation).value();
            if ((propName == null || propName.length() == 0) && methodName.length() > 3 && methodName.startsWith("get")) {
               propName = methodName.substring(3);
               if (Character.isUpperCase(propName.charAt(0))) {
                  if (propName.length() == 1) {
                     propName = propName.toLowerCase();
                  } else if (!Character.isUpperCase(propName.charAt(1))) {
                     propName = Character.toLowerCase(propName.charAt(0)) + propName.substring(1);
                  }
               }
            }
         } else if (annotation instanceof JSFunction) {
            propName = ((JSFunction)annotation).value();
         } else if (annotation instanceof JSStaticFunction) {
            propName = ((JSStaticFunction)annotation).value();
         }

         if (propName == null || propName.length() == 0) {
            propName = methodName;
         }

         return propName;
      }
   }

   private static Class extendsScriptable(Class c) {
      return ScriptRuntime.ScriptableClass.isAssignableFrom(c) ? c : null;
   }

   public void defineProperty(String propertyName, Object value, int attributes) {
      this.checkNotSealed(propertyName, 0);
      this.put(propertyName, this, value);
      this.setAttributes(propertyName, attributes);
   }

   public static void defineProperty(Scriptable destination, String propertyName, Object value, int attributes) {
      if (!(destination instanceof ScriptableObject)) {
         destination.put(propertyName, destination, value);
      } else {
         ScriptableObject so = (ScriptableObject)destination;
         so.defineProperty(propertyName, value, attributes);
      }
   }

   public static void defineConstProperty(Scriptable destination, String propertyName) {
      if (destination instanceof ConstProperties) {
         ConstProperties cp = (ConstProperties)destination;
         cp.defineConst(propertyName, destination);
      } else {
         defineProperty(destination, propertyName, Undefined.instance, 13);
      }

   }

   public void defineProperty(String propertyName, Class clazz, int attributes) {
      int length = propertyName.length();
      if (length == 0) {
         throw new IllegalArgumentException();
      } else {
         char[] buf = new char[3 + length];
         propertyName.getChars(0, length, buf, 3);
         buf[3] = Character.toUpperCase(buf[3]);
         buf[0] = 'g';
         buf[1] = 'e';
         buf[2] = 't';
         String getterName = new String(buf);
         buf[0] = 's';
         String setterName = new String(buf);
         Method[] methods = FunctionObject.getMethodList(clazz);
         Method getter = FunctionObject.findSingleMethod(methods, getterName);
         Method setter = FunctionObject.findSingleMethod(methods, setterName);
         if (setter == null) {
            attributes |= 1;
         }

         this.defineProperty(propertyName, (Object)null, getter, setter == null ? null : setter, attributes);
      }
   }

   public void defineProperty(String propertyName, Object delegateTo, Method getter, Method setter, int attributes) {
      MemberBox getterBox = null;
      if (getter != null) {
         getterBox = new MemberBox(getter);
         boolean delegatedForm;
         if (!Modifier.isStatic(getter.getModifiers())) {
            delegatedForm = delegateTo != null;
            getterBox.delegateTo = delegateTo;
         } else {
            delegatedForm = true;
            getterBox.delegateTo = Void.TYPE;
         }

         String errorId = null;
         Class<?>[] parmTypes = getter.getParameterTypes();
         if (parmTypes.length == 0) {
            if (delegatedForm) {
               errorId = "msg.obj.getter.parms";
            }
         } else if (parmTypes.length == 1) {
            Object argType = parmTypes[0];
            if (argType != ScriptRuntime.ScriptableClass && argType != ScriptRuntime.ScriptableObjectClass) {
               errorId = "msg.bad.getter.parms";
            } else if (!delegatedForm) {
               errorId = "msg.bad.getter.parms";
            }
         } else {
            errorId = "msg.bad.getter.parms";
         }

         if (errorId != null) {
            throw Context.reportRuntimeError1(errorId, getter.toString());
         }
      }

      MemberBox setterBox = null;
      if (setter != null) {
         if (setter.getReturnType() != Void.TYPE) {
            throw Context.reportRuntimeError1("msg.setter.return", setter.toString());
         }

         setterBox = new MemberBox(setter);
         boolean delegatedForm;
         if (!Modifier.isStatic(setter.getModifiers())) {
            delegatedForm = delegateTo != null;
            setterBox.delegateTo = delegateTo;
         } else {
            delegatedForm = true;
            setterBox.delegateTo = Void.TYPE;
         }

         String errorId = null;
         Class<?>[] parmTypes = setter.getParameterTypes();
         if (parmTypes.length == 1) {
            if (delegatedForm) {
               errorId = "msg.setter2.expected";
            }
         } else if (parmTypes.length == 2) {
            Object argType = parmTypes[0];
            if (argType != ScriptRuntime.ScriptableClass && argType != ScriptRuntime.ScriptableObjectClass) {
               errorId = "msg.setter2.parms";
            } else if (!delegatedForm) {
               errorId = "msg.setter1.parms";
            }
         } else {
            errorId = "msg.setter.parms";
         }

         if (errorId != null) {
            throw Context.reportRuntimeError1(errorId, setter.toString());
         }
      }

      GetterSlot gslot = (GetterSlot)this.getSlot(propertyName, 0, 4);
      gslot.setAttributes(attributes);
      gslot.getter = getterBox;
      gslot.setter = setterBox;
   }

   public void defineOwnProperties(Context cx, ScriptableObject props) {
      Object[] ids = props.getIds();

      for(Object id : ids) {
         Object descObj = props.get(id);
         ScriptableObject desc = ensureScriptableObject(descObj);
         this.checkPropertyDefinition(desc);
      }

      for(Object id : ids) {
         ScriptableObject desc = (ScriptableObject)props.get(id);
         this.defineOwnProperty(cx, id, desc);
      }

   }

   public void defineOwnProperty(Context cx, Object id, ScriptableObject desc) {
      this.checkPropertyDefinition(desc);
      this.defineOwnProperty(cx, id, desc, true);
   }

   protected void defineOwnProperty(Context cx, Object id, ScriptableObject desc, boolean checkValid) {
      Slot slot = this.getSlot(cx, id, 1);
      boolean isNew = slot == null;
      if (checkValid) {
         ScriptableObject current = slot == null ? null : slot.getPropertyDescriptor(cx, this);
         String name = ScriptRuntime.toString(id);
         this.checkPropertyChange(name, current, desc);
      }

      boolean isAccessor = this.isAccessorDescriptor(desc);
      int attributes;
      if (slot == null) {
         slot = this.getSlot(cx, id, isAccessor ? 4 : 2);
         attributes = this.applyDescriptorToAttributeBitset(7, desc);
      } else {
         attributes = this.applyDescriptorToAttributeBitset(slot.getAttributes(), desc);
      }

      slot = unwrapSlot(slot);
      if (isAccessor) {
         if (!(slot instanceof GetterSlot)) {
            slot = this.getSlot(cx, id, 4);
         }

         GetterSlot gslot = (GetterSlot)slot;
         Object getter = getProperty(desc, "get");
         if (getter != NOT_FOUND) {
            gslot.getter = getter;
         }

         Object setter = getProperty(desc, "set");
         if (setter != NOT_FOUND) {
            gslot.setter = setter;
         }

         gslot.value = Undefined.instance;
         gslot.setAttributes(attributes);
      } else {
         if (slot instanceof GetterSlot && this.isDataDescriptor(desc)) {
            slot = this.getSlot(cx, id, 5);
         }

         Object value = getProperty(desc, "value");
         if (value != NOT_FOUND) {
            slot.value = value;
         } else if (isNew) {
            slot.value = Undefined.instance;
         }

         slot.setAttributes(attributes);
      }

   }

   protected void checkPropertyDefinition(ScriptableObject desc) {
      Object getter = getProperty(desc, "get");
      if (getter != NOT_FOUND && getter != Undefined.instance && !(getter instanceof Callable)) {
         throw ScriptRuntime.notFunctionError(getter);
      } else {
         Object setter = getProperty(desc, "set");
         if (setter != NOT_FOUND && setter != Undefined.instance && !(setter instanceof Callable)) {
            throw ScriptRuntime.notFunctionError(setter);
         } else if (this.isDataDescriptor(desc) && this.isAccessorDescriptor(desc)) {
            throw ScriptRuntime.typeError0("msg.both.data.and.accessor.desc");
         }
      }
   }

   protected void checkPropertyChange(String id, ScriptableObject current, ScriptableObject desc) {
      if (current == null) {
         if (!this.isExtensible()) {
            throw ScriptRuntime.typeError0("msg.not.extensible");
         }
      } else if (isFalse(current.get("configurable", current))) {
         if (isTrue(getProperty(desc, "configurable"))) {
            throw ScriptRuntime.typeError1("msg.change.configurable.false.to.true", id);
         }

         if (isTrue(current.get("enumerable", current)) != isTrue(getProperty(desc, "enumerable"))) {
            throw ScriptRuntime.typeError1("msg.change.enumerable.with.configurable.false", id);
         }

         boolean isData = this.isDataDescriptor(desc);
         boolean isAccessor = this.isAccessorDescriptor(desc);
         if (isData || isAccessor) {
            if (isData && this.isDataDescriptor(current)) {
               if (isFalse(current.get("writable", current))) {
                  if (isTrue(getProperty(desc, "writable"))) {
                     throw ScriptRuntime.typeError1("msg.change.writable.false.to.true.with.configurable.false", id);
                  }

                  if (!this.sameValue(getProperty(desc, "value"), current.get("value", current))) {
                     throw ScriptRuntime.typeError1("msg.change.value.with.writable.false", id);
                  }
               }
            } else {
               if (!isAccessor || !this.isAccessorDescriptor(current)) {
                  if (this.isDataDescriptor(current)) {
                     throw ScriptRuntime.typeError1("msg.change.property.data.to.accessor.with.configurable.false", id);
                  }

                  throw ScriptRuntime.typeError1("msg.change.property.accessor.to.data.with.configurable.false", id);
               }

               if (!this.sameValue(getProperty(desc, "set"), current.get("set", current))) {
                  throw ScriptRuntime.typeError1("msg.change.setter.with.configurable.false", id);
               }

               if (!this.sameValue(getProperty(desc, "get"), current.get("get", current))) {
                  throw ScriptRuntime.typeError1("msg.change.getter.with.configurable.false", id);
               }
            }
         }
      }

   }

   protected static boolean isTrue(Object value) {
      return value != NOT_FOUND && ScriptRuntime.toBoolean(value);
   }

   protected static boolean isFalse(Object value) {
      return !isTrue(value);
   }

   protected boolean sameValue(Object newValue, Object currentValue) {
      if (newValue == NOT_FOUND) {
         return true;
      } else {
         if (currentValue == NOT_FOUND) {
            currentValue = Undefined.instance;
         }

         if (currentValue instanceof Number && newValue instanceof Number) {
            double d1 = ((Number)currentValue).doubleValue();
            double d2 = ((Number)newValue).doubleValue();
            if (Double.isNaN(d1) && Double.isNaN(d2)) {
               return true;
            }

            if (d1 == (double)0.0F && Double.doubleToLongBits(d1) != Double.doubleToLongBits(d2)) {
               return false;
            }
         }

         return ScriptRuntime.shallowEq(currentValue, newValue);
      }
   }

   protected int applyDescriptorToAttributeBitset(int attributes, ScriptableObject desc) {
      Object enumerable = getProperty(desc, "enumerable");
      if (enumerable != NOT_FOUND) {
         attributes = ScriptRuntime.toBoolean(enumerable) ? attributes & -3 : attributes | 2;
      }

      Object writable = getProperty(desc, "writable");
      if (writable != NOT_FOUND) {
         attributes = ScriptRuntime.toBoolean(writable) ? attributes & -2 : attributes | 1;
      }

      Object configurable = getProperty(desc, "configurable");
      if (configurable != NOT_FOUND) {
         attributes = ScriptRuntime.toBoolean(configurable) ? attributes & -5 : attributes | 4;
      }

      return attributes;
   }

   protected boolean isDataDescriptor(ScriptableObject desc) {
      return hasProperty(desc, "value") || hasProperty(desc, "writable");
   }

   protected boolean isAccessorDescriptor(ScriptableObject desc) {
      return hasProperty(desc, "get") || hasProperty(desc, "set");
   }

   protected boolean isGenericDescriptor(ScriptableObject desc) {
      return !this.isDataDescriptor(desc) && !this.isAccessorDescriptor(desc);
   }

   protected static Scriptable ensureScriptable(Object arg) {
      if (!(arg instanceof Scriptable)) {
         throw ScriptRuntime.typeError1("msg.arg.not.object", ScriptRuntime.typeof(arg));
      } else {
         return (Scriptable)arg;
      }
   }

   protected static ScriptableObject ensureScriptableObject(Object arg) {
      if (!(arg instanceof ScriptableObject)) {
         throw ScriptRuntime.typeError1("msg.arg.not.object", ScriptRuntime.typeof(arg));
      } else {
         return (ScriptableObject)arg;
      }
   }

   public void defineFunctionProperties(String[] names, Class clazz, int attributes) {
      Method[] methods = FunctionObject.getMethodList(clazz);

      for(int i = 0; i < names.length; ++i) {
         String name = names[i];
         Method m = FunctionObject.findSingleMethod(methods, name);
         if (m == null) {
            throw Context.reportRuntimeError2("msg.method.not.found", name, clazz.getName());
         }

         FunctionObject f = new FunctionObject(name, m, this);
         this.defineProperty(name, (Object)f, attributes);
      }

   }

   public static Scriptable getObjectPrototype(Scriptable scope) {
      return TopLevel.getBuiltinPrototype(getTopLevelScope(scope), TopLevel.Builtins.Object);
   }

   public static Scriptable getFunctionPrototype(Scriptable scope) {
      return TopLevel.getBuiltinPrototype(getTopLevelScope(scope), TopLevel.Builtins.Function);
   }

   public static Scriptable getArrayPrototype(Scriptable scope) {
      return TopLevel.getBuiltinPrototype(getTopLevelScope(scope), TopLevel.Builtins.Array);
   }

   public static Scriptable getClassPrototype(Scriptable scope, String className) {
      scope = getTopLevelScope(scope);
      Object ctor = getProperty(scope, className);
      Object proto;
      if (ctor instanceof BaseFunction) {
         proto = ((BaseFunction)ctor).getPrototypeProperty();
      } else {
         if (!(ctor instanceof Scriptable)) {
            return null;
         }

         Scriptable ctorObj = (Scriptable)ctor;
         proto = ctorObj.get("prototype", ctorObj);
      }

      return proto instanceof Scriptable ? (Scriptable)proto : null;
   }

   public static Scriptable getTopLevelScope(Scriptable obj) {
      while(true) {
         Scriptable parent = obj.getParentScope();
         if (parent == null) {
            return obj;
         }

         obj = parent;
      }
   }

   public boolean isExtensible() {
      return this.isExtensible;
   }

   public void preventExtensions() {
      this.isExtensible = false;
   }

   public synchronized void sealObject() {
      if (this.count >= 0) {
         for(Slot slot = this.firstAdded; slot != null; slot = slot.orderedNext) {
            Object value = slot.value;
            if (value instanceof LazilyLoadedCtor) {
               LazilyLoadedCtor initializer = (LazilyLoadedCtor)value;

               try {
                  initializer.init();
               } finally {
                  slot.value = initializer.getValue();
               }
            }
         }

         this.count = ~this.count;
      }

   }

   public final boolean isSealed() {
      return this.count < 0;
   }

   private void checkNotSealed(String name, int index) {
      if (this.isSealed()) {
         String str = name != null ? name : Integer.toString(index);
         throw Context.reportRuntimeError1("msg.modify.sealed", str);
      }
   }

   public static Object getProperty(Scriptable obj, String name) {
      Scriptable start = obj;

      Object result;
      do {
         result = obj.get(name, start);
         if (result != Scriptable.NOT_FOUND) {
            break;
         }

         obj = obj.getPrototype();
      } while(obj != null);

      return result;
   }

   public static Object getTypedProperty(Scriptable s, int index, Class type) {
      Object val = getProperty(s, index);
      if (val == Scriptable.NOT_FOUND) {
         val = null;
      }

      return type.cast(Context.jsToJava(val, type));
   }

   public static Object getProperty(Scriptable obj, int index) {
      Scriptable start = obj;

      Object result;
      do {
         result = obj.get(index, start);
         if (result != Scriptable.NOT_FOUND) {
            break;
         }

         obj = obj.getPrototype();
      } while(obj != null);

      return result;
   }

   public static Object getTypedProperty(Scriptable s, String name, Class type) {
      Object val = getProperty(s, name);
      if (val == Scriptable.NOT_FOUND) {
         val = null;
      }

      return type.cast(Context.jsToJava(val, type));
   }

   public static boolean hasProperty(Scriptable obj, String name) {
      return null != getBase(obj, name);
   }

   public static void redefineProperty(Scriptable obj, String name, boolean isConst) {
      Scriptable base = getBase(obj, name);
      if (base != null) {
         if (base instanceof ConstProperties) {
            ConstProperties cp = (ConstProperties)base;
            if (cp.isConst(name)) {
               throw Context.reportRuntimeError1("msg.const.redecl", name);
            }
         }

         if (isConst) {
            throw Context.reportRuntimeError1("msg.var.redecl", name);
         }
      }
   }

   public static boolean hasProperty(Scriptable obj, int index) {
      return null != getBase(obj, index);
   }

   public static void putProperty(Scriptable obj, String name, Object value) {
      Scriptable base = getBase(obj, name);
      if (base == null) {
         base = obj;
      }

      base.put(name, obj, value);
   }

   public static void putConstProperty(Scriptable obj, String name, Object value) {
      Scriptable base = getBase(obj, name);
      if (base == null) {
         base = obj;
      }

      if (base instanceof ConstProperties) {
         ((ConstProperties)base).putConst(name, obj, value);
      }

   }

   public static void putProperty(Scriptable obj, int index, Object value) {
      Scriptable base = getBase(obj, index);
      if (base == null) {
         base = obj;
      }

      base.put(index, obj, value);
   }

   public static boolean deleteProperty(Scriptable obj, String name) {
      Scriptable base = getBase(obj, name);
      if (base == null) {
         return true;
      } else {
         base.delete(name);
         return !base.has(name, obj);
      }
   }

   public static boolean deleteProperty(Scriptable obj, int index) {
      Scriptable base = getBase(obj, index);
      if (base == null) {
         return true;
      } else {
         base.delete(index);
         return !base.has(index, obj);
      }
   }

   public static Object[] getPropertyIds(Scriptable obj) {
      if (obj == null) {
         return ScriptRuntime.emptyArgs;
      } else {
         Object[] result = obj.getIds();
         ObjToIntMap map = null;

         while(true) {
            obj = obj.getPrototype();
            if (obj == null) {
               if (map != null) {
                  result = map.getKeys();
               }

               return result;
            }

            Object[] ids = obj.getIds();
            if (ids.length != 0) {
               if (map == null) {
                  if (result.length == 0) {
                     result = ids;
                     continue;
                  }

                  map = new ObjToIntMap(result.length + ids.length);

                  for(int i = 0; i != result.length; ++i) {
                     map.intern(result[i]);
                  }

                  result = null;
               }

               for(int i = 0; i != ids.length; ++i) {
                  map.intern(ids[i]);
               }
            }
         }
      }
   }

   public static Object callMethod(Scriptable obj, String methodName, Object[] args) {
      return callMethod((Context)null, obj, methodName, args);
   }

   public static Object callMethod(Context cx, Scriptable obj, String methodName, Object[] args) {
      Object funObj = getProperty(obj, methodName);
      if (!(funObj instanceof Function)) {
         throw ScriptRuntime.notFunctionError(obj, methodName);
      } else {
         Function fun = (Function)funObj;
         Scriptable scope = getTopLevelScope(obj);
         return cx != null ? fun.call(cx, scope, obj, args) : Context.call((ContextFactory)null, fun, scope, obj, args);
      }
   }

   private static Scriptable getBase(Scriptable obj, String name) {
      while(true) {
         if (!obj.has(name, obj)) {
            obj = obj.getPrototype();
            if (obj != null) {
               continue;
            }
         }

         return obj;
      }
   }

   private static Scriptable getBase(Scriptable obj, int index) {
      while(true) {
         if (!obj.has(index, obj)) {
            obj = obj.getPrototype();
            if (obj != null) {
               continue;
            }
         }

         return obj;
      }
   }

   public final Object getAssociatedValue(Object key) {
      Map<Object, Object> h = this.associatedValues;
      return h == null ? null : h.get(key);
   }

   public static Object getTopScopeValue(Scriptable scope, Object key) {
      scope = getTopLevelScope(scope);

      do {
         if (scope instanceof ScriptableObject) {
            ScriptableObject so = (ScriptableObject)scope;
            Object value = so.getAssociatedValue(key);
            if (value != null) {
               return value;
            }
         }

         scope = scope.getPrototype();
      } while(scope != null);

      return null;
   }

   public final synchronized Object associateValue(Object key, Object value) {
      if (value == null) {
         throw new IllegalArgumentException();
      } else {
         Map<Object, Object> h = this.associatedValues;
         if (h == null) {
            h = new HashMap();
            this.associatedValues = h;
         }

         return Kit.initHash(h, key, value);
      }
   }

   private boolean putImpl(String name, int index, Scriptable start, Object value) {
      Slot slot;
      if (this != start) {
         slot = this.getSlot(name, index, 1);
         if (slot == null) {
            return false;
         }
      } else if (!this.isExtensible) {
         slot = this.getSlot(name, index, 1);
         if (slot == null) {
            return true;
         }
      } else {
         if (this.count < 0) {
            this.checkNotSealed(name, index);
         }

         slot = this.getSlot(name, index, 2);
      }

      return slot.setValue(value, this, start);
   }

   private boolean putConstImpl(String name, int index, Scriptable start, Object value, int constFlag) {
      assert constFlag != 0;

      Slot slot;
      if (this != start) {
         slot = this.getSlot(name, index, 1);
         if (slot == null) {
            return false;
         }
      } else {
         if (this.isExtensible()) {
            this.checkNotSealed(name, index);
            slot = unwrapSlot(this.getSlot(name, index, 3));
            int attr = slot.getAttributes();
            if ((attr & 1) == 0) {
               throw Context.reportRuntimeError1("msg.var.redecl", name);
            }

            if ((attr & 8) != 0) {
               slot.value = value;
               if (constFlag != 8) {
                  slot.setAttributes(attr & -9);
               }
            }

            return true;
         }

         slot = this.getSlot(name, index, 1);
         if (slot == null) {
            return true;
         }
      }

      return slot.setValue(value, this, start);
   }

   private Slot findAttributeSlot(String name, int index, int accessType) {
      Slot slot = this.getSlot(name, index, accessType);
      if (slot == null) {
         String str = name != null ? name : Integer.toString(index);
         throw Context.reportRuntimeError1("msg.prop.not.found", str);
      } else {
         return slot;
      }
   }

   private static Slot unwrapSlot(Slot slot) {
      return slot instanceof RelinkedSlot ? ((RelinkedSlot)slot).slot : slot;
   }

   private Slot getSlot(String name, int index, int accessType) {
      Slot[] slotsLocalRef = this.slots;
      if (slotsLocalRef == null && accessType == 1) {
         return null;
      } else {
         int indexOrHash = name != null ? name.hashCode() : index;
         if (slotsLocalRef != null) {
            int slotIndex = getSlotIndex(slotsLocalRef.length, indexOrHash);

            Slot slot;
            for(slot = slotsLocalRef[slotIndex]; slot != null; slot = slot.next) {
               Object sname = slot.name;
               if (indexOrHash == slot.indexOrHash && (sname == name || name != null && name.equals(sname))) {
                  break;
               }
            }

            switch (accessType) {
               case 1:
                  return slot;
               case 2:
               case 3:
                  if (slot != null) {
                     return slot;
                  }
                  break;
               case 4:
                  slot = unwrapSlot(slot);
                  if (slot instanceof GetterSlot) {
                     return slot;
                  }
                  break;
               case 5:
                  slot = unwrapSlot(slot);
                  if (!(slot instanceof GetterSlot)) {
                     return slot;
                  }
            }
         }

         return this.createSlot(name, indexOrHash, accessType);
      }
   }

   private synchronized Slot createSlot(String name, int indexOrHash, int accessType) {
      Slot[] slotsLocalRef = this.slots;
      int insertPos;
      if (this.count == 0) {
         slotsLocalRef = new Slot[4];
         this.slots = slotsLocalRef;
         insertPos = getSlotIndex(slotsLocalRef.length, indexOrHash);
      } else {
         int tableSize = slotsLocalRef.length;
         insertPos = getSlotIndex(tableSize, indexOrHash);
         Slot prev = slotsLocalRef[insertPos];

         Slot slot;
         for(slot = prev; slot != null && (slot.indexOrHash != indexOrHash || slot.name != name && (name == null || !name.equals(slot.name))); slot = slot.next) {
            prev = slot;
         }

         if (slot != null) {
            Slot inner = unwrapSlot(slot);
            Slot newSlot;
            if (accessType == 4 && !(inner instanceof GetterSlot)) {
               newSlot = new GetterSlot(name, indexOrHash, inner.getAttributes());
            } else {
               if (accessType != 5 || !(inner instanceof GetterSlot)) {
                  if (accessType == 3) {
                     return null;
                  }

                  return inner;
               }

               newSlot = new Slot(name, indexOrHash, inner.getAttributes());
            }

            newSlot.value = inner.value;
            newSlot.next = slot.next;
            if (this.lastAdded != null) {
               this.lastAdded.orderedNext = newSlot;
            }

            if (this.firstAdded == null) {
               this.firstAdded = newSlot;
            }

            this.lastAdded = newSlot;
            if (prev == slot) {
               slotsLocalRef[insertPos] = newSlot;
            } else {
               prev.next = newSlot;
            }

            slot.markDeleted();
            return newSlot;
         }

         if (4 * (this.count + 1) > 3 * slotsLocalRef.length) {
            slotsLocalRef = new Slot[slotsLocalRef.length * 2];
            copyTable(this.slots, slotsLocalRef, this.count);
            this.slots = slotsLocalRef;
            insertPos = getSlotIndex(slotsLocalRef.length, indexOrHash);
         }
      }

      Slot newSlot = (Slot)(accessType == 4 ? new GetterSlot(name, indexOrHash, 0) : new Slot(name, indexOrHash, 0));
      if (accessType == 3) {
         newSlot.setAttributes(13);
      }

      ++this.count;
      if (this.lastAdded != null) {
         this.lastAdded.orderedNext = newSlot;
      }

      if (this.firstAdded == null) {
         this.firstAdded = newSlot;
      }

      this.lastAdded = newSlot;
      addKnownAbsentSlot(slotsLocalRef, newSlot, insertPos);
      return newSlot;
   }

   private synchronized void removeSlot(String name, int index) {
      int indexOrHash = name != null ? name.hashCode() : index;
      Slot[] slotsLocalRef = this.slots;
      if (this.count != 0) {
         int tableSize = slotsLocalRef.length;
         int slotIndex = getSlotIndex(tableSize, indexOrHash);
         Slot prev = slotsLocalRef[slotIndex];

         Slot slot;
         for(slot = prev; slot != null && (slot.indexOrHash != indexOrHash || slot.name != name && (name == null || !name.equals(slot.name))); slot = slot.next) {
            prev = slot;
         }

         if (slot != null && (slot.getAttributes() & 4) == 0) {
            --this.count;
            if (prev == slot) {
               slotsLocalRef[slotIndex] = slot.next;
            } else {
               prev.next = slot.next;
            }

            Slot deleted = unwrapSlot(slot);
            if (deleted == this.firstAdded) {
               prev = null;
               this.firstAdded = deleted.orderedNext;
            } else {
               for(prev = this.firstAdded; prev.orderedNext != deleted; prev = prev.orderedNext) {
               }

               prev.orderedNext = deleted.orderedNext;
            }

            if (deleted == this.lastAdded) {
               this.lastAdded = prev;
            }

            slot.markDeleted();
         }
      }

   }

   private static int getSlotIndex(int tableSize, int indexOrHash) {
      return indexOrHash & tableSize - 1;
   }

   private static void copyTable(Slot[] oldSlots, Slot[] newSlots, int count) {
      if (count == 0) {
         throw Kit.codeBug();
      } else {
         int tableSize = newSlots.length;
         int i = oldSlots.length;

         while(true) {
            --i;
            Slot slot = oldSlots[i];

            while(slot != null) {
               int insertPos = getSlotIndex(tableSize, slot.indexOrHash);
               Slot insSlot = (Slot)(slot.next == null ? slot : new RelinkedSlot(slot));
               addKnownAbsentSlot(newSlots, insSlot, insertPos);
               slot = slot.next;
               --count;
               if (count == 0) {
                  return;
               }
            }
         }
      }
   }

   private static void addKnownAbsentSlot(Slot[] slots, Slot slot, int insertPos) {
      if (slots[insertPos] == null) {
         slots[insertPos] = slot;
      } else {
         Slot prev = slots[insertPos];

         for(Slot next = prev.next; next != null; next = next.next) {
            prev = next;
         }

         prev.next = slot;
      }

   }

   Object[] getIds(boolean getAll) {
      Slot[] s = this.slots;
      Object[] a = ScriptRuntime.emptyArgs;
      if (s == null) {
         return a;
      } else {
         int c = 0;

         Slot slot;
         for(slot = this.firstAdded; slot != null && slot.wasDeleted; slot = slot.orderedNext) {
         }

         while(slot != null) {
            if (getAll || (slot.getAttributes() & 2) == 0) {
               if (c == 0) {
                  a = new Object[s.length];
               }

               a[c++] = slot.name != null ? slot.name : slot.indexOrHash;
            }

            for(slot = slot.orderedNext; slot != null && slot.wasDeleted; slot = slot.orderedNext) {
            }
         }

         if (c == a.length) {
            return a;
         } else {
            Object[] result = new Object[c];
            System.arraycopy(a, 0, result, 0, c);
            return result;
         }
      }
   }

   private synchronized void writeObject(ObjectOutputStream out) throws IOException {
      out.defaultWriteObject();
      int objectsCount = this.count;
      if (objectsCount < 0) {
         objectsCount = ~objectsCount;
      }

      if (objectsCount == 0) {
         out.writeInt(0);
      } else {
         out.writeInt(this.slots.length);

         Slot slot;
         for(slot = this.firstAdded; slot != null && slot.wasDeleted; slot = slot.orderedNext) {
         }

         Slot next;
         for(this.firstAdded = slot; slot != null; slot = next) {
            out.writeObject(slot);

            for(next = slot.orderedNext; next != null && next.wasDeleted; next = next.orderedNext) {
            }

            slot.orderedNext = next;
         }
      }

   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      in.defaultReadObject();
      int tableSize = in.readInt();
      if (tableSize != 0) {
         if ((tableSize & tableSize - 1) != 0) {
            if (tableSize > 1073741824) {
               throw new RuntimeException("Property table overflow");
            }

            int newSize;
            for(newSize = 4; newSize < tableSize; newSize <<= 1) {
            }

            tableSize = newSize;
         }

         this.slots = new Slot[tableSize];
         int objectsCount = this.count;
         if (objectsCount < 0) {
            objectsCount = ~objectsCount;
         }

         Slot prev = null;

         for(int i = 0; i != objectsCount; ++i) {
            this.lastAdded = (Slot)in.readObject();
            if (i == 0) {
               this.firstAdded = this.lastAdded;
            } else {
               prev.orderedNext = this.lastAdded;
            }

            int slotIndex = getSlotIndex(tableSize, this.lastAdded.indexOrHash);
            addKnownAbsentSlot(this.slots, this.lastAdded, slotIndex);
            prev = this.lastAdded;
         }
      }

   }

   protected ScriptableObject getOwnPropertyDescriptor(Context cx, Object id) {
      Slot slot = this.getSlot(cx, id, 1);
      if (slot == null) {
         return null;
      } else {
         Scriptable scope = this.getParentScope();
         return slot.getPropertyDescriptor(cx, (Scriptable)(scope == null ? this : scope));
      }
   }

   protected Slot getSlot(Context cx, Object id, int accessType) {
      String name = ScriptRuntime.toStringIdOrIndex(cx, id);
      return name == null ? this.getSlot((String)null, ScriptRuntime.lastIndexResult(cx), accessType) : this.getSlot(name, 0, accessType);
   }

   public int size() {
      return this.count < 0 ? ~this.count : this.count;
   }

   public boolean isEmpty() {
      return this.count == 0 || this.count == -1;
   }

   public Object get(Object key) {
      Object value = null;
      if (key instanceof String) {
         value = this.get((String)key, this);
      } else if (key instanceof Number) {
         value = this.get(((Number)key).intValue(), this);
      }

      if (value != Scriptable.NOT_FOUND && value != Undefined.instance) {
         return value instanceof Wrapper ? ((Wrapper)value).unwrap() : value;
      } else {
         return null;
      }
   }

   private static class Slot implements Serializable {
      private static final long serialVersionUID = -6090581677123995491L;
      String name;
      int indexOrHash;
      private volatile short attributes;
      transient volatile boolean wasDeleted;
      volatile Object value;
      transient Slot next;
      transient volatile Slot orderedNext;

      Slot(String name, int indexOrHash, int attributes) {
         super();
         this.name = name;
         this.indexOrHash = indexOrHash;
         this.attributes = (short)attributes;
      }

      private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
         in.defaultReadObject();
         if (this.name != null) {
            this.indexOrHash = this.name.hashCode();
         }

      }

      boolean setValue(Object value, Scriptable owner, Scriptable start) {
         if ((this.attributes & 1) != 0) {
            return true;
         } else if (owner == start) {
            this.value = value;
            return true;
         } else {
            return false;
         }
      }

      Object getValue(Scriptable start) {
         return this.value;
      }

      int getAttributes() {
         return this.attributes;
      }

      synchronized void setAttributes(int value) {
         ScriptableObject.checkValidAttributes(value);
         this.attributes = (short)value;
      }

      void markDeleted() {
         this.wasDeleted = true;
         this.value = null;
         this.name = null;
      }

      ScriptableObject getPropertyDescriptor(Context cx, Scriptable scope) {
         return ScriptableObject.buildDataDescriptor(scope, this.value, this.attributes);
      }
   }

   private static final class GetterSlot extends Slot {
      static final long serialVersionUID = -4900574849788797588L;
      Object getter;
      Object setter;

      GetterSlot(String name, int indexOrHash, int attributes) {
         super(name, indexOrHash, attributes);
      }

      ScriptableObject getPropertyDescriptor(Context cx, Scriptable scope) {
         int attr = this.getAttributes();
         ScriptableObject desc = new NativeObject();
         ScriptRuntime.setBuiltinProtoAndParent(desc, scope, TopLevel.Builtins.Object);
         desc.defineProperty("enumerable", (Object)((attr & 2) == 0), 0);
         desc.defineProperty("configurable", (Object)((attr & 4) == 0), 0);
         if (this.getter != null) {
            desc.defineProperty("get", (Object)this.getter, 0);
         }

         if (this.setter != null) {
            desc.defineProperty("set", (Object)this.setter, 0);
         }

         return desc;
      }

      boolean setValue(Object value, Scriptable owner, Scriptable start) {
         if (this.setter == null) {
            if (this.getter != null) {
               if (Context.getContext().hasFeature(11)) {
                  throw ScriptRuntime.typeError1("msg.set.prop.no.setter", this.name);
               } else {
                  return true;
               }
            } else {
               return super.setValue(value, owner, start);
            }
         } else {
            Context cx = Context.getContext();
            if (this.setter instanceof MemberBox) {
               MemberBox nativeSetter = (MemberBox)this.setter;
               Class<?>[] pTypes = nativeSetter.argTypes;
               Class<?> valueType = pTypes[pTypes.length - 1];
               int tag = FunctionObject.getTypeTag(valueType);
               Object actualArg = FunctionObject.convertArg(cx, start, value, tag);
               Object setterThis;
               Object[] args;
               if (nativeSetter.delegateTo == null) {
                  setterThis = start;
                  args = new Object[]{actualArg};
               } else {
                  setterThis = nativeSetter.delegateTo;
                  args = new Object[]{start, actualArg};
               }

               nativeSetter.invoke(setterThis, args);
            } else if (this.setter instanceof Function) {
               Function f = (Function)this.setter;
               f.call(cx, f.getParentScope(), start, new Object[]{value});
            }

            return true;
         }
      }

      Object getValue(Scriptable start) {
         if (this.getter != null) {
            if (this.getter instanceof MemberBox) {
               MemberBox nativeGetter = (MemberBox)this.getter;
               Object[] args;
               Object getterThis;
               if (nativeGetter.delegateTo == null) {
                  getterThis = start;
                  args = ScriptRuntime.emptyArgs;
               } else {
                  getterThis = nativeGetter.delegateTo;
                  args = new Object[]{start};
               }

               return nativeGetter.invoke(getterThis, args);
            }

            if (this.getter instanceof Function) {
               Function f = (Function)this.getter;
               Context cx = Context.getContext();
               return f.call(cx, f.getParentScope(), start, ScriptRuntime.emptyArgs);
            }
         }

         Object val = this.value;
         if (val instanceof LazilyLoadedCtor) {
            LazilyLoadedCtor initializer = (LazilyLoadedCtor)val;

            try {
               initializer.init();
            } finally {
               this.value = val = initializer.getValue();
            }
         }

         return val;
      }

      void markDeleted() {
         super.markDeleted();
         this.getter = null;
         this.setter = null;
      }
   }

   private static class RelinkedSlot extends Slot {
      final Slot slot;

      RelinkedSlot(Slot slot) {
         super(slot.name, slot.indexOrHash, slot.attributes);
         this.slot = ScriptableObject.unwrapSlot(slot);
      }

      boolean setValue(Object value, Scriptable owner, Scriptable start) {
         return this.slot.setValue(value, owner, start);
      }

      Object getValue(Scriptable start) {
         return this.slot.getValue(start);
      }

      ScriptableObject getPropertyDescriptor(Context cx, Scriptable scope) {
         return this.slot.getPropertyDescriptor(cx, scope);
      }

      int getAttributes() {
         return this.slot.getAttributes();
      }

      void setAttributes(int value) {
         this.slot.setAttributes(value);
      }

      void markDeleted() {
         super.markDeleted();
         this.slot.markDeleted();
      }

      private void writeObject(ObjectOutputStream out) throws IOException {
         out.writeObject(this.slot);
      }
   }
}
