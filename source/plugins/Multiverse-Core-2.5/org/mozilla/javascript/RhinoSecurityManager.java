package org.mozilla.javascript;

public class RhinoSecurityManager extends SecurityManager {
   public RhinoSecurityManager() {
      super();
   }

   protected Class getCurrentScriptClass() {
      Class[] context = this.getClassContext();

      for(Class c : context) {
         if (c != InterpretedFunction.class && NativeFunction.class.isAssignableFrom(c) || PolicySecurityController.SecureCaller.class.isAssignableFrom(c)) {
            return c;
         }
      }

      return null;
   }
}
