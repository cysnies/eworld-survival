package org.mozilla.javascript;

public class JavaScriptException extends RhinoException {
   static final long serialVersionUID = -7666130513694669293L;
   private Object value;

   /** @deprecated */
   public JavaScriptException(Object value) {
      this(value, "", 0);
   }

   public JavaScriptException(Object value, String sourceName, int lineNumber) {
      super();
      this.recordErrorOrigin(sourceName, lineNumber, (String)null, 0);
      this.value = value;
      if (value instanceof NativeError && Context.getContext().hasFeature(10)) {
         NativeError error = (NativeError)value;
         if (!error.has("fileName", error)) {
            error.put("fileName", error, sourceName);
         }

         if (!error.has("lineNumber", error)) {
            error.put("lineNumber", error, lineNumber);
         }

         error.setStackProvider(this);
      }

   }

   public String details() {
      if (this.value == null) {
         return "null";
      } else if (this.value instanceof NativeError) {
         return this.value.toString();
      } else {
         try {
            return ScriptRuntime.toString(this.value);
         } catch (RuntimeException var2) {
            return this.value instanceof Scriptable ? ScriptRuntime.defaultObjectToString((Scriptable)this.value) : this.value.toString();
         }
      }
   }

   public Object getValue() {
      return this.value;
   }

   /** @deprecated */
   public String getSourceName() {
      return this.sourceName();
   }

   /** @deprecated */
   public int getLineNumber() {
      return this.lineNumber();
   }
}
