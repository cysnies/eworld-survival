package org.mozilla.javascript;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.SecureClassLoader;
import java.util.Map;
import java.util.WeakHashMap;

public abstract class SecureCaller {
   private static final byte[] secureCallerImplBytecode = loadBytecode();
   private static final Map callers = new WeakHashMap();

   public SecureCaller() {
      super();
   }

   public abstract Object call(Callable var1, Context var2, Scriptable var3, Scriptable var4, Object[] var5);

   static Object callSecurely(final CodeSource codeSource, Callable callable, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      final Thread thread = Thread.currentThread();
      final ClassLoader classLoader = (ClassLoader)AccessController.doPrivileged(new PrivilegedAction() {
         public Object run() {
            return thread.getContextClassLoader();
         }
      });
      Map<ClassLoader, SoftReference<SecureCaller>> classLoaderMap;
      synchronized(callers) {
         classLoaderMap = (Map)callers.get(codeSource);
         if (classLoaderMap == null) {
            classLoaderMap = new WeakHashMap();
            callers.put(codeSource, classLoaderMap);
         }
      }

      SecureCaller caller;
      synchronized(classLoaderMap) {
         SoftReference<SecureCaller> ref = (SoftReference)classLoaderMap.get(classLoader);
         if (ref != null) {
            caller = (SecureCaller)ref.get();
         } else {
            caller = null;
         }

         if (caller == null) {
            try {
               caller = (SecureCaller)AccessController.doPrivileged(new PrivilegedExceptionAction() {
                  public Object run() throws Exception {
                     Class<?> thisClass = this.getClass();
                     ClassLoader effectiveClassLoader;
                     if (classLoader.loadClass(thisClass.getName()) != thisClass) {
                        effectiveClassLoader = thisClass.getClassLoader();
                     } else {
                        effectiveClassLoader = classLoader;
                     }

                     SecureClassLoaderImpl secCl = new SecureClassLoaderImpl(effectiveClassLoader);
                     Class<?> c = secCl.defineAndLinkClass(SecureCaller.class.getName() + "Impl", SecureCaller.secureCallerImplBytecode, codeSource);
                     return c.newInstance();
                  }
               });
               classLoaderMap.put(classLoader, new SoftReference(caller));
            } catch (PrivilegedActionException ex) {
               throw new UndeclaredThrowableException(ex.getCause());
            }
         }
      }

      return caller.call(callable, cx, scope, thisObj, args);
   }

   private static byte[] loadBytecode() {
      return (byte[])AccessController.doPrivileged(new PrivilegedAction() {
         public Object run() {
            return SecureCaller.loadBytecodePrivileged();
         }
      });
   }

   private static byte[] loadBytecodePrivileged() {
      URL url = SecureCaller.class.getResource("SecureCallerImpl.clazz");

      try {
         InputStream in = url.openStream();

         try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();

            while(true) {
               int r = in.read();
               if (r == -1) {
                  byte[] var4 = bout.toByteArray();
                  return var4;
               }

               bout.write(r);
            }
         } finally {
            in.close();
         }
      } catch (IOException e) {
         throw new UndeclaredThrowableException(e);
      }
   }

   private static class SecureClassLoaderImpl extends SecureClassLoader {
      SecureClassLoaderImpl(ClassLoader parent) {
         super(parent);
      }

      Class defineAndLinkClass(String name, byte[] bytes, CodeSource cs) {
         Class<?> cl = this.defineClass(name, bytes, 0, bytes.length, cs);
         this.resolveClass(cl);
         return cl;
      }
   }
}
