package org.mozilla.javascript;

import java.util.EnumMap;

public class TopLevel extends IdScriptableObject {
   static final long serialVersionUID = -4648046356662472260L;
   private EnumMap ctors;

   public TopLevel() {
      super();
   }

   public String getClassName() {
      return "global";
   }

   public void cacheBuiltins() {
      this.ctors = new EnumMap(Builtins.class);

      for(Builtins builtin : TopLevel.Builtins.values()) {
         Object value = ScriptableObject.getProperty(this, builtin.name());
         if (value instanceof BaseFunction) {
            this.ctors.put(builtin, (BaseFunction)value);
         }
      }

   }

   public static Function getBuiltinCtor(Context cx, Scriptable scope, Builtins type) {
      assert scope.getParentScope() == null;

      if (scope instanceof TopLevel) {
         Function result = ((TopLevel)scope).getBuiltinCtor(type);
         if (result != null) {
            return result;
         }
      }

      return ScriptRuntime.getExistingCtor(cx, scope, type.name());
   }

   public static Scriptable getBuiltinPrototype(Scriptable scope, Builtins type) {
      assert scope.getParentScope() == null;

      if (scope instanceof TopLevel) {
         Scriptable result = ((TopLevel)scope).getBuiltinPrototype(type);
         if (result != null) {
            return result;
         }
      }

      return ScriptableObject.getClassPrototype(scope, type.name());
   }

   public BaseFunction getBuiltinCtor(Builtins type) {
      return this.ctors != null ? (BaseFunction)this.ctors.get(type) : null;
   }

   public Scriptable getBuiltinPrototype(Builtins type) {
      BaseFunction func = this.getBuiltinCtor(type);
      Object proto = func != null ? func.getPrototypeProperty() : null;
      return proto instanceof Scriptable ? (Scriptable)proto : null;
   }

   public static enum Builtins {
      Object,
      Array,
      Function,
      String,
      Number,
      Boolean,
      RegExp,
      Error;

      private Builtins() {
      }
   }
}
