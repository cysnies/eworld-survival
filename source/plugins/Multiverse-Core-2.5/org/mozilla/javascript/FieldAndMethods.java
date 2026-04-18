package org.mozilla.javascript;

import java.lang.reflect.Field;

class FieldAndMethods extends NativeJavaMethod {
   static final long serialVersionUID = -9222428244284796755L;
   Field field;
   Object javaObject;

   FieldAndMethods(Scriptable scope, MemberBox[] methods, Field field) {
      super(methods);
      this.field = field;
      this.setParentScope(scope);
      this.setPrototype(ScriptableObject.getFunctionPrototype(scope));
   }

   public Object getDefaultValue(Class hint) {
      if (hint == ScriptRuntime.FunctionClass) {
         return this;
      } else {
         Object rval;
         Class<?> type;
         try {
            rval = this.field.get(this.javaObject);
            type = this.field.getType();
         } catch (IllegalAccessException var5) {
            throw Context.reportRuntimeError1("msg.java.internal.private", this.field.getName());
         }

         Context cx = Context.getContext();
         rval = cx.getWrapFactory().wrap(cx, this, rval, type);
         if (rval instanceof Scriptable) {
            rval = ((Scriptable)rval).getDefaultValue(hint);
         }

         return rval;
      }
   }
}
