package org.mozilla.javascript;

public class EcmaError extends RhinoException {
   static final long serialVersionUID = -6261226256957286699L;
   private String errorName;
   private String errorMessage;

   EcmaError(String errorName, String errorMessage, String sourceName, int lineNumber, String lineSource, int columnNumber) {
      super();
      this.recordErrorOrigin(sourceName, lineNumber, lineSource, columnNumber);
      this.errorName = errorName;
      this.errorMessage = errorMessage;
   }

   /** @deprecated */
   public EcmaError(Scriptable nativeError, String sourceName, int lineNumber, int columnNumber, String lineSource) {
      this("InternalError", ScriptRuntime.toString(nativeError), sourceName, lineNumber, lineSource, columnNumber);
   }

   public String details() {
      return this.errorName + ": " + this.errorMessage;
   }

   public String getName() {
      return this.errorName;
   }

   public String getErrorMessage() {
      return this.errorMessage;
   }

   /** @deprecated */
   public String getSourceName() {
      return this.sourceName();
   }

   /** @deprecated */
   public int getLineNumber() {
      return this.lineNumber();
   }

   /** @deprecated */
   public int getColumnNumber() {
      return this.columnNumber();
   }

   /** @deprecated */
   public String getLineSource() {
      return this.lineSource();
   }

   /** @deprecated */
   public Scriptable getErrorObject() {
      return null;
   }
}
