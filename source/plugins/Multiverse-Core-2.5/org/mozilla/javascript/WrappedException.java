package org.mozilla.javascript;

public class WrappedException extends EvaluatorException {
   static final long serialVersionUID = -1551979216966520648L;
   private Throwable exception;

   public WrappedException(Throwable exception) {
      super("Wrapped " + exception.toString());
      this.exception = exception;
      Kit.initCause(this, exception);
      int[] linep = new int[]{0};
      String sourceName = Context.getSourcePositionFromStack(linep);
      int lineNumber = linep[0];
      if (sourceName != null) {
         this.initSourceName(sourceName);
      }

      if (lineNumber != 0) {
         this.initLineNumber(lineNumber);
      }

   }

   public Throwable getWrappedException() {
      return this.exception;
   }

   /** @deprecated */
   public Object unwrap() {
      return this.getWrappedException();
   }
}
