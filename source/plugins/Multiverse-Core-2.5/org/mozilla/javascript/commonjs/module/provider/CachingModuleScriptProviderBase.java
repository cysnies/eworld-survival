package org.mozilla.javascript.commonjs.module.provider;

import java.io.Reader;
import java.io.Serializable;
import java.net.URI;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.commonjs.module.ModuleScript;
import org.mozilla.javascript.commonjs.module.ModuleScriptProvider;

public abstract class CachingModuleScriptProviderBase implements ModuleScriptProvider, Serializable {
   private static final long serialVersionUID = 1L;
   private static final int loadConcurrencyLevel = Runtime.getRuntime().availableProcessors() * 8;
   private static final int loadLockShift;
   private static final int loadLockMask;
   private static final int loadLockCount;
   private final Object[] loadLocks;
   private final ModuleSourceProvider moduleSourceProvider;

   protected CachingModuleScriptProviderBase(ModuleSourceProvider moduleSourceProvider) {
      super();
      this.loadLocks = new Object[loadLockCount];

      for(int i = 0; i < this.loadLocks.length; ++i) {
         this.loadLocks[i] = new Object();
      }

      this.moduleSourceProvider = moduleSourceProvider;
   }

   public ModuleScript getModuleScript(Context cx, String moduleId, URI moduleUri, URI baseUri, Scriptable paths) throws Exception {
      CachedModuleScript cachedModule1 = this.getLoadedModule(moduleId);
      Object validator1 = getValidator(cachedModule1);
      ModuleSource moduleSource = moduleUri == null ? this.moduleSourceProvider.loadSource(moduleId, paths, validator1) : this.moduleSourceProvider.loadSource(moduleUri, baseUri, validator1);
      if (moduleSource == ModuleSourceProvider.NOT_MODIFIED) {
         return cachedModule1.getModule();
      } else if (moduleSource == null) {
         return null;
      } else {
         Reader reader = moduleSource.getReader();

         ModuleScript var13;
         try {
            int idHash = moduleId.hashCode();
            synchronized(this.loadLocks[idHash >>> loadLockShift & loadLockMask]) {
               CachedModuleScript cachedModule2 = this.getLoadedModule(moduleId);
               if (cachedModule2 == null || equal(validator1, getValidator(cachedModule2))) {
                  URI sourceUri = moduleSource.getUri();
                  ModuleScript moduleScript = new ModuleScript(cx.compileReader(reader, sourceUri.toString(), 1, moduleSource.getSecurityDomain()), sourceUri, moduleSource.getBase());
                  this.putLoadedModule(moduleId, moduleScript, moduleSource.getValidator());
                  ModuleScript var15 = moduleScript;
                  return var15;
               }

               var13 = cachedModule2.getModule();
            }
         } finally {
            reader.close();
         }

         return var13;
      }
   }

   protected abstract void putLoadedModule(String var1, ModuleScript var2, Object var3);

   protected abstract CachedModuleScript getLoadedModule(String var1);

   private static Object getValidator(CachedModuleScript cachedModule) {
      return cachedModule == null ? null : cachedModule.getValidator();
   }

   private static boolean equal(Object o1, Object o2) {
      return o1 == null ? o2 == null : o1.equals(o2);
   }

   protected static int getConcurrencyLevel() {
      return loadLockCount;
   }

   static {
      int sshift = 0;

      int ssize;
      for(ssize = 1; ssize < loadConcurrencyLevel; ssize <<= 1) {
         ++sshift;
      }

      loadLockShift = 32 - sshift;
      loadLockMask = ssize - 1;
      loadLockCount = ssize;
   }

   public static class CachedModuleScript {
      private final ModuleScript moduleScript;
      private final Object validator;

      public CachedModuleScript(ModuleScript moduleScript, Object validator) {
         super();
         this.moduleScript = moduleScript;
         this.validator = validator;
      }

      ModuleScript getModule() {
         return this.moduleScript;
      }

      Object getValidator() {
         return this.validator;
      }
   }
}
