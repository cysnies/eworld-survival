package org.mozilla.javascript;

public class WrapFactory {
   private boolean javaPrimitiveWrap = true;

   public WrapFactory() {
      super();
   }

   public Object wrap(Context cx, Scriptable scope, Object obj, Class staticType) {
      if (obj != null && obj != Undefined.instance && !(obj instanceof Scriptable)) {
         if (staticType != null && staticType.isPrimitive()) {
            if (staticType == Void.TYPE) {
               return Undefined.instance;
            } else {
               return staticType == Character.TYPE ? Integer.valueOf((Character)obj) : obj;
            }
         } else {
            if (!this.isJavaPrimitiveWrap()) {
               if (obj instanceof String || obj instanceof Number || obj instanceof Boolean) {
                  return obj;
               }

               if (obj instanceof Character) {
                  return String.valueOf((Character)obj);
               }
            }

            Class<?> cls = obj.getClass();
            if (cls.isArray()) {
               return NativeJavaArray.wrap(scope, obj);
            } else {
               return this.wrapAsJavaObject(cx, scope, obj, staticType);
            }
         }
      } else {
         return obj;
      }
   }

   public Scriptable wrapNewObject(Context cx, Scriptable scope, Object obj) {
      if (obj instanceof Scriptable) {
         return (Scriptable)obj;
      } else {
         Class<?> cls = obj.getClass();
         return (Scriptable)(cls.isArray() ? NativeJavaArray.wrap(scope, obj) : this.wrapAsJavaObject(cx, scope, obj, (Class)null));
      }
   }

   public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class staticType) {
      return new NativeJavaObject(scope, javaObject, staticType);
   }

   public Scriptable wrapJavaClass(Context cx, Scriptable scope, Class javaClass) {
      return new NativeJavaClass(scope, javaClass);
   }

   public final boolean isJavaPrimitiveWrap() {
      return this.javaPrimitiveWrap;
   }

   public final void setJavaPrimitiveWrap(boolean value) {
      Context cx = Context.getCurrentContext();
      if (cx != null && cx.isSealed()) {
         Context.onSealedMutation();
      }

      this.javaPrimitiveWrap = value;
   }
}
