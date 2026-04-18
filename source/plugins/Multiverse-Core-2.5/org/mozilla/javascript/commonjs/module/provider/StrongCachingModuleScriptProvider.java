package org.mozilla.javascript.commonjs.module.provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.mozilla.javascript.commonjs.module.ModuleScript;

public class StrongCachingModuleScriptProvider extends CachingModuleScriptProviderBase {
   private static final long serialVersionUID = 1L;
   private final Map modules = new ConcurrentHashMap(16, 0.75F, getConcurrencyLevel());

   public StrongCachingModuleScriptProvider(ModuleSourceProvider moduleSourceProvider) {
      super(moduleSourceProvider);
   }

   protected CachingModuleScriptProviderBase.CachedModuleScript getLoadedModule(String moduleId) {
      return (CachingModuleScriptProviderBase.CachedModuleScript)this.modules.get(moduleId);
   }

   protected void putLoadedModule(String moduleId, ModuleScript moduleScript, Object validator) {
      this.modules.put(moduleId, new CachingModuleScriptProviderBase.CachedModuleScript(moduleScript, validator));
   }
}
