package net.citizensnpcs.api.scripting;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Callables;
import java.util.concurrent.Callable;

public class ObjectProvider implements ContextProvider {
   private final String name;
   private final Callable provider;

   public ObjectProvider(String name, Callable provider) {
      super();
      Preconditions.checkNotNull(provider, "provider cannot be null");
      Preconditions.checkNotNull(name, "name cannot be null");
      this.name = name;
      this.provider = provider;
   }

   public ObjectProvider(String name, Object obj) {
      super();
      Preconditions.checkNotNull(obj, "provided object cannot be null");
      Preconditions.checkNotNull(name, "name cannot be null");
      this.name = name;
      this.provider = Callables.returning(obj);
   }

   public void provide(Script script) {
      Object res = null;

      try {
         res = this.provider.call();
      } catch (Exception e) {
         e.printStackTrace();
      }

      if (res != null) {
         script.setAttribute(this.name, res);
      }
   }
}
