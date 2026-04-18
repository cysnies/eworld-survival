package org.mozilla.javascript;

class NativeScript extends BaseFunction {
   static final long serialVersionUID = -6795101161980121700L;
   private static final Object SCRIPT_TAG = "Script";
   private static final int Id_constructor = 1;
   private static final int Id_toString = 2;
   private static final int Id_compile = 3;
   private static final int Id_exec = 4;
   private static final int MAX_PROTOTYPE_ID = 4;
   private Script script;

   static void init(Scriptable scope, boolean sealed) {
      NativeScript obj = new NativeScript((Script)null);
      obj.exportAsJSClass(4, scope, sealed);
   }

   private NativeScript(Script script) {
      super();
      this.script = script;
   }

   public String getClassName() {
      return "Script";
   }

   public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      return this.script != null ? this.script.exec(cx, scope) : Undefined.instance;
   }

   public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
      throw Context.reportRuntimeError0("msg.script.is.not.constructor");
   }

   public int getLength() {
      return 0;
   }

   public int getArity() {
      return 0;
   }

   String decompile(int indent, int flags) {
      return this.script instanceof NativeFunction ? ((NativeFunction)this.script).decompile(indent, flags) : super.decompile(indent, flags);
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
            arity = 1;
            s = "compile";
            break;
         case 4:
            arity = 0;
            s = "exec";
            break;
         default:
            throw new IllegalArgumentException(String.valueOf(id));
      }

      this.initPrototypeMethod(SCRIPT_TAG, id, s, arity);
   }

   public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      if (!f.hasTag(SCRIPT_TAG)) {
         return super.execIdCall(f, cx, scope, thisObj, args);
      } else {
         int id = f.methodId();
         switch (id) {
            case 1:
               String source = args.length == 0 ? "" : ScriptRuntime.toString(args[0]);
               Script script = compile(cx, source);
               NativeScript nscript = new NativeScript(script);
               ScriptRuntime.setObjectProtoAndParent(nscript, scope);
               return nscript;
            case 2:
               NativeScript real = realThis(thisObj, f);
               Script realScript = real.script;
               if (realScript == null) {
                  return "";
               }

               return cx.decompileScript(realScript, 0);
            case 3:
               NativeScript real = realThis(thisObj, f);
               String source = ScriptRuntime.toString(args, 0);
               real.script = compile(cx, source);
               return real;
            case 4:
               throw Context.reportRuntimeError1("msg.cant.call.indirect", "exec");
            default:
               throw new IllegalArgumentException(String.valueOf(id));
         }
      }
   }

   private static NativeScript realThis(Scriptable thisObj, IdFunctionObject f) {
      if (!(thisObj instanceof NativeScript)) {
         throw incompatibleCallError(f);
      } else {
         return (NativeScript)thisObj;
      }
   }

   private static Script compile(Context cx, String source) {
      int[] linep = new int[]{0};
      String filename = Context.getSourcePositionFromStack(linep);
      if (filename == null) {
         filename = "<Script object>";
         linep[0] = 1;
      }

      ErrorReporter reporter = DefaultErrorReporter.forEval(cx.getErrorReporter());
      return cx.compileString(source, (Evaluator)null, reporter, filename, linep[0], (Object)null);
   }

   protected int findPrototypeId(String s) {
      int id = 0;
      String X = null;
      switch (s.length()) {
         case 4:
            X = "exec";
            id = 4;
         case 5:
         case 6:
         case 9:
         case 10:
         default:
            break;
         case 7:
            X = "compile";
            id = 3;
            break;
         case 8:
            X = "toString";
            id = 2;
            break;
         case 11:
            X = "constructor";
            id = 1;
      }

      if (X != null && X != s && !X.equals(s)) {
         id = 0;
      }

      return id;
   }
}
