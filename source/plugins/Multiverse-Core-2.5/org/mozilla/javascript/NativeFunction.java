package org.mozilla.javascript;

import org.mozilla.javascript.debug.DebuggableScript;

public abstract class NativeFunction extends BaseFunction {
   static final long serialVersionUID = 8713897114082216401L;

   public NativeFunction() {
      super();
   }

   public final void initScriptFunction(Context cx, Scriptable scope) {
      ScriptRuntime.setFunctionProtoAndParent(this, scope);
   }

   final String decompile(int indent, int flags) {
      String encodedSource = this.getEncodedSource();
      if (encodedSource == null) {
         return super.decompile(indent, flags);
      } else {
         UintMap properties = new UintMap(1);
         properties.put(1, indent);
         return Decompiler.decompile(encodedSource, flags, properties);
      }
   }

   public int getLength() {
      int paramCount = this.getParamCount();
      if (this.getLanguageVersion() != 120) {
         return paramCount;
      } else {
         Context cx = Context.getContext();
         NativeCall activation = ScriptRuntime.findFunctionActivation(cx, this);
         return activation == null ? paramCount : activation.originalArgs.length;
      }
   }

   public int getArity() {
      return this.getParamCount();
   }

   /** @deprecated */
   public String jsGet_name() {
      return this.getFunctionName();
   }

   public String getEncodedSource() {
      return null;
   }

   public DebuggableScript getDebuggableView() {
      return null;
   }

   public Object resumeGenerator(Context cx, Scriptable scope, int operation, Object state, Object value) {
      throw new EvaluatorException("resumeGenerator() not implemented");
   }

   protected abstract int getLanguageVersion();

   protected abstract int getParamCount();

   protected abstract int getParamAndVarCount();

   protected abstract String getParamOrVarName(int var1);

   protected boolean getParamOrVarConst(int index) {
      return false;
   }
}
