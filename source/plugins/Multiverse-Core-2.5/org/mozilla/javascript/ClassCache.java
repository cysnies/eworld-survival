package org.mozilla.javascript;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ClassCache implements Serializable {
   private static final long serialVersionUID = -8866246036237312215L;
   private static final Object AKEY = "ClassCache";
   private volatile boolean cachingIsEnabled = true;
   private transient HashMap classTable;
   private transient HashMap classAdapterCache;
   private transient HashMap interfaceAdapterCache;
   private int generatedClassSerial;
   private Scriptable associatedScope;

   public ClassCache() {
      super();
   }

   public static ClassCache get(Scriptable scope) {
      ClassCache cache = (ClassCache)ScriptableObject.getTopScopeValue(scope, AKEY);
      if (cache == null) {
         throw new RuntimeException("Can't find top level scope for ClassCache.get");
      } else {
         return cache;
      }
   }

   public boolean associate(ScriptableObject topScope) {
      if (topScope.getParentScope() != null) {
         throw new IllegalArgumentException();
      } else if (this == topScope.associateValue(AKEY, this)) {
         this.associatedScope = topScope;
         return true;
      } else {
         return false;
      }
   }

   public synchronized void clearCaches() {
      this.classTable = null;
      this.classAdapterCache = null;
      this.interfaceAdapterCache = null;
   }

   public final boolean isCachingEnabled() {
      return this.cachingIsEnabled;
   }

   public synchronized void setCachingEnabled(boolean enabled) {
      if (enabled != this.cachingIsEnabled) {
         if (!enabled) {
            this.clearCaches();
         }

         this.cachingIsEnabled = enabled;
      }
   }

   Map getClassCacheMap() {
      if (this.classTable == null) {
         this.classTable = new HashMap();
      }

      return this.classTable;
   }

   Map getInterfaceAdapterCacheMap() {
      if (this.classAdapterCache == null) {
         this.classAdapterCache = new HashMap();
      }

      return this.classAdapterCache;
   }

   /** @deprecated */
   public boolean isInvokerOptimizationEnabled() {
      return false;
   }

   /** @deprecated */
   public synchronized void setInvokerOptimizationEnabled(boolean enabled) {
   }

   public final synchronized int newClassSerialNumber() {
      return ++this.generatedClassSerial;
   }

   Object getInterfaceAdapter(Class cl) {
      return this.interfaceAdapterCache == null ? null : this.interfaceAdapterCache.get(cl);
   }

   synchronized void cacheInterfaceAdapter(Class cl, Object iadapter) {
      if (this.cachingIsEnabled) {
         if (this.interfaceAdapterCache == null) {
            this.interfaceAdapterCache = new HashMap();
         }

         this.interfaceAdapterCache.put(cl, iadapter);
      }

   }

   Scriptable getAssociatedScope() {
      return this.associatedScope;
   }
}
