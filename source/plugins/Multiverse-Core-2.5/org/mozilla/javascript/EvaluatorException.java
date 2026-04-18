package org.mozilla.javascript;

public class EvaluatorException extends RhinoException {
   static final long serialVersionUID = -8743165779676009808L;

   public EvaluatorException(String detail) {
      super(detail);
   }

   public EvaluatorException(String detail, String sourceName, int lineNumber) {
      this(detail, sourceName, lineNumber, (String)null, 0);
   }

   public EvaluatorException(String detail, String sourceName, int lineNumber, String lineSource, int columnNumber) {
      super(detail);
      this.recordErrorOrigin(sourceName, lineNumber, lineSource, columnNumber);
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
}
