package org.mozilla.javascript;

public class DefiningClassLoader extends ClassLoader implements GeneratedClassLoader {
   private final ClassLoader parentLoader;

   public DefiningClassLoader() {
      super();
      this.parentLoader = this.getClass().getClassLoader();
   }

   public DefiningClassLoader(ClassLoader parentLoader) {
      super();
      this.parentLoader = parentLoader;
   }

   public Class defineClass(String name, byte[] data) {
      return super.defineClass(name, data, 0, data.length, SecurityUtilities.getProtectionDomain(this.getClass()));
   }

   public void linkClass(Class cl) {
      this.resolveClass(cl);
   }

   public Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
      Class<?> cl = this.findLoadedClass(name);
      if (cl == null) {
         if (this.parentLoader != null) {
            cl = this.parentLoader.loadClass(name);
         } else {
            cl = this.findSystemClass(name);
         }
      }

      if (resolve) {
         this.resolveClass(cl);
      }

      return cl;
   }
}
