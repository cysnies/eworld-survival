package org.mozilla.javascript.tools.shell;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.mozilla.javascript.Kit;
import org.mozilla.javascript.Scriptable;

public class ShellLine {
   public ShellLine() {
      super();
   }

   public static InputStream getStream(Scriptable scope) {
      ClassLoader classLoader = ShellLine.class.getClassLoader();
      if (classLoader == null) {
         classLoader = ClassLoader.getSystemClassLoader();
      }

      if (classLoader == null) {
         return null;
      } else {
         Class<?> readerClass = Kit.classOrNull(classLoader, "jline.ConsoleReader");
         if (readerClass == null) {
            return null;
         } else {
            try {
               Constructor<?> c = readerClass.getConstructor();
               Object reader = c.newInstance();
               Method m = readerClass.getMethod("setBellEnabled", Boolean.TYPE);
               m.invoke(reader, Boolean.FALSE);
               Class<?> completorClass = Kit.classOrNull(classLoader, "jline.Completor");
               m = readerClass.getMethod("addCompletor", completorClass);
               Object completor = Proxy.newProxyInstance(classLoader, new Class[]{completorClass}, new FlexibleCompletor(completorClass, scope));
               m.invoke(reader, completor);
               Class<?> inputStreamClass = Kit.classOrNull(classLoader, "jline.ConsoleReaderInputStream");
               c = inputStreamClass.getConstructor(readerClass);
               return (InputStream)c.newInstance(reader);
            } catch (NoSuchMethodException var9) {
            } catch (InstantiationException var10) {
            } catch (IllegalAccessException var11) {
            } catch (InvocationTargetException var12) {
            }

            return null;
         }
      }
   }
}
