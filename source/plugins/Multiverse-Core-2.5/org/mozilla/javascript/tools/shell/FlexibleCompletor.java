package org.mozilla.javascript.tools.shell;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

class FlexibleCompletor implements InvocationHandler {
   private Method completeMethod;
   private Scriptable global;

   FlexibleCompletor(Class completorClass, Scriptable global) throws NoSuchMethodException {
      super();
      this.global = global;
      this.completeMethod = completorClass.getMethod("complete", String.class, Integer.TYPE, List.class);
   }

   public Object invoke(Object proxy, Method method, Object[] args) {
      if (method.equals(this.completeMethod)) {
         int result = this.complete((String)args[0], (Integer)args[1], (List)args[2]);
         return result;
      } else {
         throw new NoSuchMethodError(method.toString());
      }
   }

   public int complete(String buffer, int cursor, List candidates) {
      int m;
      for(m = cursor - 1; m >= 0; --m) {
         char c = buffer.charAt(m);
         if (!Character.isJavaIdentifierPart(c) && c != '.') {
            break;
         }
      }

      String namesAndDots = buffer.substring(m + 1, cursor);
      String[] names = namesAndDots.split("\\.", -1);
      Scriptable obj = this.global;

      for(int i = 0; i < names.length - 1; ++i) {
         Object val = obj.get(names[i], this.global);
         if (!(val instanceof Scriptable)) {
            return buffer.length();
         }

         obj = (Scriptable)val;
      }

      Object[] ids = obj instanceof ScriptableObject ? ((ScriptableObject)obj).getAllIds() : obj.getIds();
      String lastPart = names[names.length - 1];

      for(int i = 0; i < ids.length; ++i) {
         if (ids[i] instanceof String) {
            String id = (String)ids[i];
            if (id.startsWith(lastPart)) {
               if (obj.get(id, obj) instanceof Function) {
                  id = id + "(";
               }

               candidates.add(id);
            }
         }
      }

      return buffer.length() - lastPart.length();
   }
}
