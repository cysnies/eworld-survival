package org.mozilla.javascript;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;

public class SecurityUtilities {
   public SecurityUtilities() {
      super();
   }

   public static String getSystemProperty(final String name) {
      return (String)AccessController.doPrivileged(new PrivilegedAction() {
         public String run() {
            return System.getProperty(name);
         }
      });
   }

   public static ProtectionDomain getProtectionDomain(final Class clazz) {
      return (ProtectionDomain)AccessController.doPrivileged(new PrivilegedAction() {
         public ProtectionDomain run() {
            return clazz.getProtectionDomain();
         }
      });
   }

   public static ProtectionDomain getScriptProtectionDomain() {
      final SecurityManager securityManager = System.getSecurityManager();
      return securityManager instanceof RhinoSecurityManager ? (ProtectionDomain)AccessController.doPrivileged(new PrivilegedAction() {
         public ProtectionDomain run() {
            Class c = ((RhinoSecurityManager)securityManager).getCurrentScriptClass();
            return c == null ? null : c.getProtectionDomain();
         }
      }) : null;
   }
}
