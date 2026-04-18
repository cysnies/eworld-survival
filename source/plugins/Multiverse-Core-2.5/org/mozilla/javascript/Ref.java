package org.mozilla.javascript;

import java.io.Serializable;

public abstract class Ref implements Serializable {
   static final long serialVersionUID = 4044540354730911424L;

   public Ref() {
      super();
   }

   public boolean has(Context cx) {
      return true;
   }

   public abstract Object get(Context var1);

   public abstract Object set(Context var1, Object var2);

   public boolean delete(Context cx) {
      return false;
   }
}
