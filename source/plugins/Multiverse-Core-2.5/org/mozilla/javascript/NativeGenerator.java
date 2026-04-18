package org.mozilla.javascript;

public final class NativeGenerator extends IdScriptableObject {
   private static final long serialVersionUID = 1645892441041347273L;
   private static final Object GENERATOR_TAG = "Generator";
   public static final int GENERATOR_SEND = 0;
   public static final int GENERATOR_THROW = 1;
   public static final int GENERATOR_CLOSE = 2;
   private static final int Id_close = 1;
   private static final int Id_next = 2;
   private static final int Id_send = 3;
   private static final int Id_throw = 4;
   private static final int Id___iterator__ = 5;
   private static final int MAX_PROTOTYPE_ID = 5;
   private NativeFunction function;
   private Object savedState;
   private String lineSource;
   private int lineNumber;
   private boolean firstTime = true;
   private boolean locked;

   static NativeGenerator init(ScriptableObject scope, boolean sealed) {
      NativeGenerator prototype = new NativeGenerator();
      if (scope != null) {
         prototype.setParentScope(scope);
         prototype.setPrototype(getObjectPrototype(scope));
      }

      prototype.activatePrototypeMap(5);
      if (sealed) {
         prototype.sealObject();
      }

      if (scope != null) {
         scope.associateValue(GENERATOR_TAG, prototype);
      }

      return prototype;
   }

   private NativeGenerator() {
      super();
   }

   public NativeGenerator(Scriptable scope, NativeFunction function, Object savedState) {
      super();
      this.function = function;
      this.savedState = savedState;
      Scriptable top = ScriptableObject.getTopLevelScope(scope);
      this.setParentScope(top);
      NativeGenerator prototype = (NativeGenerator)ScriptableObject.getTopScopeValue(top, GENERATOR_TAG);
      this.setPrototype(prototype);
   }

   public String getClassName() {
      return "Generator";
   }

   protected void initPrototypeId(int id) {
      int arity;
      String s;
      switch (id) {
         case 1:
            arity = 1;
            s = "close";
            break;
         case 2:
            arity = 1;
            s = "next";
            break;
         case 3:
            arity = 0;
            s = "send";
            break;
         case 4:
            arity = 0;
            s = "throw";
            break;
         case 5:
            arity = 1;
            s = "__iterator__";
            break;
         default:
            throw new IllegalArgumentException(String.valueOf(id));
      }

      this.initPrototypeMethod(GENERATOR_TAG, id, s, arity);
   }

   public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      if (!f.hasTag(GENERATOR_TAG)) {
         return super.execIdCall(f, cx, scope, thisObj, args);
      } else {
         int id = f.methodId();
         if (!(thisObj instanceof NativeGenerator)) {
            throw incompatibleCallError(f);
         } else {
            NativeGenerator generator = (NativeGenerator)thisObj;
            switch (id) {
               case 1:
                  return generator.resume(cx, scope, 2, new GeneratorClosedException());
               case 2:
                  generator.firstTime = false;
                  return generator.resume(cx, scope, 0, Undefined.instance);
               case 3:
                  Object arg = args.length > 0 ? args[0] : Undefined.instance;
                  if (generator.firstTime && !arg.equals(Undefined.instance)) {
                     throw ScriptRuntime.typeError0("msg.send.newborn");
                  }

                  return generator.resume(cx, scope, 0, arg);
               case 4:
                  return generator.resume(cx, scope, 1, args.length > 0 ? args[0] : Undefined.instance);
               case 5:
                  return thisObj;
               default:
                  throw new IllegalArgumentException(String.valueOf(id));
            }
         }
      }
   }

   private Object resume(Context cx, Scriptable scope, int operation, Object value) {
      if (this.savedState == null) {
         if (operation == 2) {
            return Undefined.instance;
         } else {
            Object thrown;
            if (operation == 1) {
               thrown = value;
            } else {
               thrown = NativeIterator.getStopIterationObject(scope);
            }

            throw new JavaScriptException(thrown, this.lineSource, this.lineNumber);
         }
      } else {
         Object thrown;
         try {
            synchronized(this) {
               if (this.locked) {
                  throw ScriptRuntime.typeError0("msg.already.exec.gen");
               }

               this.locked = true;
            }

            thrown = this.function.resumeGenerator(cx, scope, operation, this.savedState, value);
         } catch (GeneratorClosedException var21) {
            Object var6 = Undefined.instance;
            return var6;
         } catch (RhinoException e) {
            this.lineNumber = e.lineNumber();
            this.lineSource = e.lineSource();
            this.savedState = null;
            throw e;
         } finally {
            synchronized(this) {
               this.locked = false;
            }

            if (operation == 2) {
               this.savedState = null;
            }

         }

         return thrown;
      }
   }

   protected int findPrototypeId(String s) {
      int id = 0;
      String X = null;
      int s_length = s.length();
      if (s_length == 4) {
         int c = s.charAt(0);
         if (c == 110) {
            X = "next";
            id = 2;
         } else if (c == 115) {
            X = "send";
            id = 3;
         }
      } else if (s_length == 5) {
         int c = s.charAt(0);
         if (c == 99) {
            X = "close";
            id = 1;
         } else if (c == 116) {
            X = "throw";
            id = 4;
         }
      } else if (s_length == 12) {
         X = "__iterator__";
         id = 5;
      }

      if (X != null && X != s && !X.equals(s)) {
         id = 0;
      }

      return id;
   }

   private static class CloseGeneratorAction implements ContextAction {
      private NativeGenerator generator;

      CloseGeneratorAction(NativeGenerator generator) {
         super();
         this.generator = generator;
      }

      public Object run(Context cx) {
         Scriptable scope = ScriptableObject.getTopLevelScope(this.generator);
         Callable closeGenerator = new Callable() {
            public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
               return ((NativeGenerator)thisObj).resume(cx, scope, 2, new GeneratorClosedException());
            }
         };
         return ScriptRuntime.doTopCall(closeGenerator, cx, scope, this.generator, (Object[])null);
      }
   }

   public static class GeneratorClosedException extends RuntimeException {
      private static final long serialVersionUID = 2561315658662379681L;

      public GeneratorClosedException() {
         super();
      }
   }
}
