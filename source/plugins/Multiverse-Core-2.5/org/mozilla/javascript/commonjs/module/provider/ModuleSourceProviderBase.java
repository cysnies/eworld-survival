package org.mozilla.javascript.commonjs.module.provider;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public abstract class ModuleSourceProviderBase implements ModuleSourceProvider, Serializable {
   private static final long serialVersionUID = 1L;

   public ModuleSourceProviderBase() {
      super();
   }

   public ModuleSource loadSource(String moduleId, Scriptable paths, Object validator) throws IOException, URISyntaxException {
      if (!this.entityNeedsRevalidation(validator)) {
         return NOT_MODIFIED;
      } else {
         ModuleSource moduleSource = this.loadFromPrivilegedLocations(moduleId, validator);
         if (moduleSource != null) {
            return moduleSource;
         } else {
            if (paths != null) {
               moduleSource = this.loadFromPathArray(moduleId, paths, validator);
               if (moduleSource != null) {
                  return moduleSource;
               }
            }

            return this.loadFromFallbackLocations(moduleId, validator);
         }
      }
   }

   public ModuleSource loadSource(URI uri, URI base, Object validator) throws IOException, URISyntaxException {
      return this.loadFromUri(uri, base, validator);
   }

   private ModuleSource loadFromPathArray(String moduleId, Scriptable paths, Object validator) throws IOException {
      long llength = ScriptRuntime.toUint32(ScriptableObject.getProperty(paths, "length"));
      int ilength = llength > 2147483647L ? Integer.MAX_VALUE : (int)llength;

      for(int i = 0; i < ilength; ++i) {
         String path = ensureTrailingSlash((String)ScriptableObject.getTypedProperty(paths, i, String.class));

         try {
            URI uri = new URI(path);
            if (!uri.isAbsolute()) {
               uri = (new File(path)).toURI().resolve("");
            }

            ModuleSource moduleSource = this.loadFromUri(uri.resolve(moduleId), uri, validator);
            if (moduleSource != null) {
               return moduleSource;
            }
         } catch (URISyntaxException e) {
            throw new MalformedURLException(e.getMessage());
         }
      }

      return null;
   }

   private static String ensureTrailingSlash(String path) {
      return path.endsWith("/") ? path : path.concat("/");
   }

   protected boolean entityNeedsRevalidation(Object validator) {
      return true;
   }

   protected abstract ModuleSource loadFromUri(URI var1, URI var2, Object var3) throws IOException, URISyntaxException;

   protected ModuleSource loadFromPrivilegedLocations(String moduleId, Object validator) throws IOException, URISyntaxException {
      return null;
   }

   protected ModuleSource loadFromFallbackLocations(String moduleId, Object validator) throws IOException, URISyntaxException {
      return null;
   }
}
