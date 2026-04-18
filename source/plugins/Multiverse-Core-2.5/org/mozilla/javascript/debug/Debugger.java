package org.mozilla.javascript.debug;

import org.mozilla.javascript.Context;

public interface Debugger {
   void handleCompilationDone(Context var1, DebuggableScript var2, String var3);

   DebugFrame getFrame(Context var1, DebuggableScript var2);
}
