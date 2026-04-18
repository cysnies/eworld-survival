package org.mozilla.javascript.commonjs.module;

import java.net.URI;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public interface ModuleScriptProvider {
   ModuleScript getModuleScript(Context var1, String var2, URI var3, URI var4, Scriptable var5) throws Exception;
}
