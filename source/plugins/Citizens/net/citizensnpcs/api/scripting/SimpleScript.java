package net.citizensnpcs.api.scripting;

import com.google.common.base.Throwables;
import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class SimpleScript implements Script {
   private final Bindings bindings;
   private final ScriptEngine engine;
   private final Invocable invocable;
   private final Object root;

   public SimpleScript(CompiledScript src, ContextProvider[] providers) throws ScriptException {
      super();
      this.engine = src.getEngine();
      this.invocable = (Invocable)this.engine;
      this.bindings = this.engine.createBindings();

      for(ContextProvider provider : providers) {
         provider.provide(this);
      }

      this.root = src.eval(this.bindings);
   }

   public Object convertToInterface(Object obj, Class expected) {
      if (obj != null && expected != null) {
         if (expected.isAssignableFrom(obj.getClass())) {
            return expected.cast(obj);
         } else {
            synchronized(this.engine) {
               Bindings old = this.engine.getBindings(100);
               this.engine.setBindings(this.bindings, 100);
               T t = (T)this.invocable.getInterface(expected);
               this.engine.setBindings(old, 100);
               return t;
            }
         }
      } else {
         throw new IllegalArgumentException("arguments should not be null");
      }
   }

   public Object getAttribute(String name) {
      if (name == null) {
         throw new IllegalArgumentException("name should not be null");
      } else {
         return this.bindings.get(name);
      }
   }

   public Object invoke(Object instance, String name, Object... args) {
      if (instance != null && name != null) {
         try {
            synchronized(this.engine) {
               Bindings old = this.engine.getBindings(100);
               this.engine.setBindings(this.bindings, 100);
               Object ret = this.invocable.invokeMethod(instance, name, args);
               this.engine.setBindings(old, 100);
               return ret;
            }
         } catch (ScriptException e) {
            Throwables.getRootCause(e).printStackTrace();
         } catch (NoSuchMethodException e) {
            e.printStackTrace();
         }

         return null;
      } else {
         throw new IllegalArgumentException("instance and method name should not be null");
      }
   }

   public Object invoke(String name, Object... args) {
      if (name == null) {
         throw new IllegalArgumentException("name should not be null");
      } else {
         try {
            synchronized(this.engine) {
               Bindings old = this.engine.getBindings(100);
               this.engine.setBindings(this.bindings, 100);
               Object ret = this.invocable.invokeFunction(name, args);
               this.engine.setBindings(old, 100);
               return ret;
            }
         } catch (ScriptException e) {
            Throwables.getRootCause(e).printStackTrace();
         } catch (NoSuchMethodException e) {
            e.printStackTrace();
         }

         return null;
      }
   }

   public void setAttribute(String name, Object value) {
      if (name != null && value != null) {
         this.bindings.put(name, value);
      } else {
         throw new IllegalArgumentException("arguments should not be null");
      }
   }
}
