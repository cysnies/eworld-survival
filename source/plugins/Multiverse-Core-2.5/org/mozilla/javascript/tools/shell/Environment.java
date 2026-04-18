package org.mozilla.javascript.tools.shell;

import java.util.Map;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class Environment extends ScriptableObject {
   static final long serialVersionUID = -430727378460177065L;
   private Environment thePrototypeInstance = null;

   public static void defineClass(ScriptableObject scope) {
      try {
         ScriptableObject.defineClass(scope, Environment.class);
      } catch (Exception e) {
         throw new Error(e.getMessage());
      }
   }

   public String getClassName() {
      return "Environment";
   }

   public Environment() {
      super();
      if (this.thePrototypeInstance == null) {
         this.thePrototypeInstance = this;
      }

   }

   public Environment(ScriptableObject scope) {
      super();
      this.setParentScope(scope);
      Object ctor = ScriptRuntime.getTopLevelProp(scope, "Environment");
      if (ctor != null && ctor instanceof Scriptable) {
         Scriptable s = (Scriptable)ctor;
         this.setPrototype((Scriptable)s.get("prototype", s));
      }

   }

   public boolean has(String name, Scriptable start) {
      if (this == this.thePrototypeInstance) {
         return super.has(name, start);
      } else {
         return System.getProperty(name) != null;
      }
   }

   public Object get(String name, Scriptable start) {
      if (this == this.thePrototypeInstance) {
         return super.get(name, start);
      } else {
         String result = System.getProperty(name);
         return result != null ? ScriptRuntime.toObject(this.getParentScope(), result) : Scriptable.NOT_FOUND;
      }
   }

   public void put(String name, Scriptable start, Object value) {
      if (this == this.thePrototypeInstance) {
         super.put(name, start, value);
      } else {
         System.getProperties().put(name, ScriptRuntime.toString(value));
      }

   }

   private Object[] collectIds() {
      Map<Object, Object> props = System.getProperties();
      return props.keySet().toArray();
   }

   public Object[] getIds() {
      return this == this.thePrototypeInstance ? super.getIds() : this.collectIds();
   }

   public Object[] getAllIds() {
      return this == this.thePrototypeInstance ? super.getAllIds() : this.collectIds();
   }
}
