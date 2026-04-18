package org.mozilla.javascript.debug;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public interface DebugFrame {
   void onEnter(Context var1, Scriptable var2, Scriptable var3, Object[] var4);

   void onLineChange(Context var1, int var2);

   void onExceptionThrown(Context var1, Throwable var2);

   void onExit(Context var1, boolean var2, Object var3);

   void onDebuggerStatement(Context var1);
}
