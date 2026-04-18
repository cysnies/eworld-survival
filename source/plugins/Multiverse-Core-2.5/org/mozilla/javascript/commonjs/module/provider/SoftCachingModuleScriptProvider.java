package org.mozilla.javascript.commonjs.module.provider;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.commonjs.module.ModuleScript;

public class SoftCachingModuleScriptProvider extends CachingModuleScriptProviderBase {
   private static final long serialVersionUID = 1L;
   private transient ReferenceQueue scriptRefQueue = new ReferenceQueue();
   private transient ConcurrentMap scripts = new ConcurrentHashMap(16, 0.75F, getConcurrencyLevel());

   public SoftCachingModuleScriptProvider(ModuleSourceProvider moduleSourceProvider) {
      super(moduleSourceProvider);
   }

   public ModuleScript getModuleScript(Context cx, String moduleId, URI uri, URI base, Scriptable paths) throws Exception {
      while(true) {
         ScriptReference ref = (ScriptReference)this.scriptRefQueue.poll();
         if (ref == null) {
            return super.getModuleScript(cx, moduleId, uri, base, paths);
         }

         this.scripts.remove(ref.getModuleId(), ref);
      }
   }

   protected CachingModuleScriptProviderBase.CachedModuleScript getLoadedModule(String moduleId) {
      ScriptReference scriptRef = (ScriptReference)this.scripts.get(moduleId);
      return scriptRef != null ? scriptRef.getCachedModuleScript() : null;
   }

   protected void putLoadedModule(String moduleId, ModuleScript moduleScript, Object validator) {
      this.scripts.put(moduleId, new ScriptReference(moduleScript.getScript(), moduleId, moduleScript.getUri(), moduleScript.getBase(), validator, this.scriptRefQueue));
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      this.scriptRefQueue = new ReferenceQueue();
      this.scripts = new ConcurrentHashMap();
      Map<String, CachingModuleScriptProviderBase.CachedModuleScript> serScripts = (Map)in.readObject();

      for(Map.Entry entry : serScripts.entrySet()) {
         CachingModuleScriptProviderBase.CachedModuleScript cachedModuleScript = (CachingModuleScriptProviderBase.CachedModuleScript)entry.getValue();
         this.putLoadedModule((String)entry.getKey(), cachedModuleScript.getModule(), cachedModuleScript.getValidator());
      }

   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      Map<String, CachingModuleScriptProviderBase.CachedModuleScript> serScripts = new HashMap();

      for(Map.Entry entry : this.scripts.entrySet()) {
         CachingModuleScriptProviderBase.CachedModuleScript cachedModuleScript = ((ScriptReference)entry.getValue()).getCachedModuleScript();
         if (cachedModuleScript != null) {
            serScripts.put(entry.getKey(), cachedModuleScript);
         }
      }

      out.writeObject(serScripts);
   }

   private static class ScriptReference extends SoftReference {
      private final String moduleId;
      private final URI uri;
      private final URI base;
      private final Object validator;

      ScriptReference(Script script, String moduleId, URI uri, URI base, Object validator, ReferenceQueue refQueue) {
         super(script, refQueue);
         this.moduleId = moduleId;
         this.uri = uri;
         this.base = base;
         this.validator = validator;
      }

      CachingModuleScriptProviderBase.CachedModuleScript getCachedModuleScript() {
         Script script = (Script)this.get();
         return script == null ? null : new CachingModuleScriptProviderBase.CachedModuleScript(new ModuleScript(script, this.uri, this.base), this.validator);
      }

      String getModuleId() {
         return this.moduleId;
      }
   }
}
