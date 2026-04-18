package org.mozilla.javascript;

public final class NativeCall extends IdScriptableObject {
   static final long serialVersionUID = -7471457301304454454L;
   private static final Object CALL_TAG = "Call";
   private static final int Id_constructor = 1;
   private static final int MAX_PROTOTYPE_ID = 1;
   NativeFunction function;
   Object[] originalArgs;
   transient NativeCall parentActivationCall;

   static void init(Scriptable scope, boolean sealed) {
      NativeCall obj = new NativeCall();
      obj.exportAsJSClass(1, scope, sealed);
   }

   NativeCall() {
      super();
   }

   NativeCall(NativeFunction function, Scriptable scope, Object[] args) {
      super();
      this.function = function;
      this.setParentScope(scope);
      this.originalArgs = args == null ? ScriptRuntime.emptyArgs : args;
      int paramAndVarCount = function.getParamAndVarCount();
      int paramCount = function.getParamCount();
      if (paramAndVarCount != 0) {
         for(int i = 0; i < paramCount; ++i) {
            String name = function.getParamOrVarName(i);
            Object val = i < args.length ? args[i] : Undefined.instance;
            this.defineProperty(name, val, 4);
         }
      }

      if (!super.has("arguments", this)) {
         this.defineProperty("arguments", new Arguments(this), 4);
      }

      if (paramAndVarCount != 0) {
         for(int i = paramCount; i < paramAndVarCount; ++i) {
            String name = function.getParamOrVarName(i);
            if (!super.has(name, this)) {
               if (function.getParamOrVarConst(i)) {
                  this.defineProperty(name, Undefined.instance, 13);
               } else {
                  this.defineProperty(name, Undefined.instance, 4);
               }
            }
         }
      }

   }

   public String getClassName() {
      return "Call";
   }

   protected int findPrototypeId(String s) {
      return s.equals("constructor") ? 1 : 0;
   }

   protected void initPrototypeId(int id) {
      if (id == 1) {
         int arity = 1;
         String s = "constructor";
         this.initPrototypeMethod(CALL_TAG, id, s, arity);
      } else {
         throw new IllegalArgumentException(String.valueOf(id));
      }
   }

   public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      if (!f.hasTag(CALL_TAG)) {
         return super.execIdCall(f, cx, scope, thisObj, args);
      } else {
         int id = f.methodId();
         if (id == 1) {
            if (thisObj != null) {
               throw Context.reportRuntimeError1("msg.only.from.new", "Call");
            } else {
               ScriptRuntime.checkDeprecated(cx, "Call");
               NativeCall result = new NativeCall();
               result.setPrototype(getObjectPrototype(scope));
               return result;
            }
         } else {
            throw new IllegalArgumentException(String.valueOf(id));
         }
      }
   }
}
