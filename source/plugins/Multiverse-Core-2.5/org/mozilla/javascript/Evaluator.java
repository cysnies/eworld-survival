package org.mozilla.javascript;

import java.util.List;
import org.mozilla.javascript.ast.ScriptNode;

public interface Evaluator {
   Object compile(CompilerEnvirons var1, ScriptNode var2, String var3, boolean var4);

   Function createFunctionObject(Context var1, Scriptable var2, Object var3, Object var4);

   Script createScriptObject(Object var1, Object var2);

   void captureStackInfo(RhinoException var1);

   String getSourcePositionFromStack(Context var1, int[] var2);

   String getPatchedStack(RhinoException var1, String var2);

   List getScriptStack(RhinoException var1);

   void setEvalScriptFlag(Script var1);
}
