package org.mozilla.javascript;

import java.lang.ref.SoftReference;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.SecureClassLoader;
import java.util.Map;
import java.util.WeakHashMap;
import org.mozilla.classfile.ClassFileWriter;

public class PolicySecurityController extends SecurityController {
   private static final byte[] secureCallerImplBytecode = loadBytecode();
   private static final Map callers = new WeakHashMap();

   public PolicySecurityController() {
      super();
   }

   public Class getStaticSecurityDomainClassInternal() {
      return CodeSource.class;
   }

   public GeneratedClassLoader createClassLoader(final ClassLoader parent, final Object securityDomain) {
      return (Loader)AccessController.doPrivileged(new PrivilegedAction() {
         public Object run() {
            return new Loader(parent, (CodeSource)securityDomain);
         }
      });
   }

   public Object getDynamicSecurityDomain(Object securityDomain) {
      return securityDomain;
   }

   public Object callWithDomain(Object securityDomain, final Context cx, Callable callable, Scriptable scope, Scriptable thisObj, Object[] args) {
      final ClassLoader classLoader = (ClassLoader)AccessController.doPrivileged(new PrivilegedAction() {
         public Object run() {
            return cx.getApplicationClassLoader();
         }
      });
      final CodeSource codeSource = (CodeSource)securityDomain;
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
                     Loader loader = new Loader(classLoader, codeSource);
                     Class<?> c = loader.defineClass(SecureCaller.class.getName() + "Impl", PolicySecurityController.secureCallerImplBytecode);
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
      String secureCallerClassName = SecureCaller.class.getName();
      ClassFileWriter cfw = new ClassFileWriter(secureCallerClassName + "Impl", secureCallerClassName, "<generated>");
      cfw.startMethod("<init>", "()V", (short)1);
      cfw.addALoad(0);
      cfw.addInvoke(183, secureCallerClassName, "<init>", "()V");
      cfw.add(177);
      cfw.stopMethod((short)1);
      String callableCallSig = "Lorg/mozilla/javascript/Context;Lorg/mozilla/javascript/Scriptable;Lorg/mozilla/javascript/Scriptable;[Ljava/lang/Object;)Ljava/lang/Object;";
      cfw.startMethod("call", "(Lorg/mozilla/javascript/Callable;" + callableCallSig, (short)17);

      for(int i = 1; i < 6; ++i) {
         cfw.addALoad(i);
      }

      cfw.addInvoke(185, "org/mozilla/javascript/Callable", "call", "(" + callableCallSig);
      cfw.add(176);
      cfw.stopMethod((short)6);
      return cfw.toByteArray();
   }

   private static class Loader extends SecureClassLoader implements GeneratedClassLoader {
      private final CodeSource codeSource;

      Loader(ClassLoader parent, CodeSource codeSource) {
         super(parent);
         this.codeSource = codeSource;
      }

      public Class defineClass(String name, byte[] data) {
         return this.defineClass(name, data, 0, data.length, this.codeSource);
      }

      public void linkClass(Class cl) {
         this.resolveClass(cl);
      }
   }

   public abstract static class SecureCaller {
      public SecureCaller() {
         super();
      }

      public abstract Object call(Callable var1, Context var2, Scriptable var3, Scriptable var4, Object[] var5);
   }
}
