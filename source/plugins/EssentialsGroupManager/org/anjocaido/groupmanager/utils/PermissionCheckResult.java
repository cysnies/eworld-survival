package org.anjocaido.groupmanager.utils;

import org.anjocaido.groupmanager.data.DataUnit;

public class PermissionCheckResult {
   public DataUnit owner;
   public String accessLevel;
   public String askedPermission;
   public Type resultType;

   public PermissionCheckResult() {
      super();
      this.resultType = PermissionCheckResult.Type.NOTFOUND;
   }

   public static enum Type {
      EXCEPTION,
      NEGATION,
      FOUND,
      NOTFOUND;

      private Type() {
      }
   }
}
