package org.mozilla.javascript;

import java.io.CharArrayWriter;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RhinoException extends RuntimeException {
   static final long serialVersionUID = 1883500631321581169L;
   private static boolean useMozillaStackStyle = false;
   private String sourceName;
   private int lineNumber;
   private String lineSource;
   private int columnNumber;
   Object interpreterStackInfo;
   int[] interpreterLineData;

   RhinoException() {
      super();
      Evaluator e = Context.createInterpreter();
      if (e != null) {
         e.captureStackInfo(this);
      }

   }

   RhinoException(String details) {
      super(details);
      Evaluator e = Context.createInterpreter();
      if (e != null) {
         e.captureStackInfo(this);
      }

   }

   public final String getMessage() {
      String details = this.details();
      if (this.sourceName != null && this.lineNumber > 0) {
         StringBuffer buf = new StringBuffer(details);
         buf.append(" (");
         if (this.sourceName != null) {
            buf.append(this.sourceName);
         }

         if (this.lineNumber > 0) {
            buf.append('#');
            buf.append(this.lineNumber);
         }

         buf.append(')');
         return buf.toString();
      } else {
         return details;
      }
   }

   public String details() {
      return super.getMessage();
   }

   public final String sourceName() {
      return this.sourceName;
   }

   public final void initSourceName(String sourceName) {
      if (sourceName == null) {
         throw new IllegalArgumentException();
      } else if (this.sourceName != null) {
         throw new IllegalStateException();
      } else {
         this.sourceName = sourceName;
      }
   }

   public final int lineNumber() {
      return this.lineNumber;
   }

   public final void initLineNumber(int lineNumber) {
      if (lineNumber <= 0) {
         throw new IllegalArgumentException(String.valueOf(lineNumber));
      } else if (this.lineNumber > 0) {
         throw new IllegalStateException();
      } else {
         this.lineNumber = lineNumber;
      }
   }

   public final int columnNumber() {
      return this.columnNumber;
   }

   public final void initColumnNumber(int columnNumber) {
      if (columnNumber <= 0) {
         throw new IllegalArgumentException(String.valueOf(columnNumber));
      } else if (this.columnNumber > 0) {
         throw new IllegalStateException();
      } else {
         this.columnNumber = columnNumber;
      }
   }

   public final String lineSource() {
      return this.lineSource;
   }

   public final void initLineSource(String lineSource) {
      if (lineSource == null) {
         throw new IllegalArgumentException();
      } else if (this.lineSource != null) {
         throw new IllegalStateException();
      } else {
         this.lineSource = lineSource;
      }
   }

   final void recordErrorOrigin(String sourceName, int lineNumber, String lineSource, int columnNumber) {
      if (lineNumber == -1) {
         lineNumber = 0;
      }

      if (sourceName != null) {
         this.initSourceName(sourceName);
      }

      if (lineNumber != 0) {
         this.initLineNumber(lineNumber);
      }

      if (lineSource != null) {
         this.initLineSource(lineSource);
      }

      if (columnNumber != 0) {
         this.initColumnNumber(columnNumber);
      }

   }

   private String generateStackTrace() {
      CharArrayWriter writer = new CharArrayWriter();
      super.printStackTrace(new PrintWriter(writer));
      String origStackTrace = writer.toString();
      Evaluator e = Context.createInterpreter();
      return e != null ? e.getPatchedStack(this, origStackTrace) : null;
   }

   public String getScriptStackTrace() {
      StringBuilder buffer = new StringBuilder();
      String lineSeparator = SecurityUtilities.getSystemProperty("line.separator");
      ScriptStackElement[] stack = this.getScriptStack();

      for(ScriptStackElement elem : stack) {
         if (useMozillaStackStyle) {
            elem.renderMozillaStyle(buffer);
         } else {
            elem.renderJavaStyle(buffer);
         }

         buffer.append(lineSeparator);
      }

      return buffer.toString();
   }

   /** @deprecated */
   public String getScriptStackTrace(FilenameFilter filter) {
      return this.getScriptStackTrace();
   }

   public ScriptStackElement[] getScriptStack() {
      List<ScriptStackElement> list = new ArrayList();
      ScriptStackElement[][] interpreterStack = (ScriptStackElement[][])null;
      if (this.interpreterStackInfo != null) {
         Evaluator interpreter = Context.createInterpreter();
         if (interpreter instanceof Interpreter) {
            interpreterStack = ((Interpreter)interpreter).getScriptStackElements(this);
         }
      }

      int interpreterStackIndex = 0;
      StackTraceElement[] stack = this.getStackTrace();
      Pattern pattern = Pattern.compile("_c_(.*)_\\d+");

      for(StackTraceElement e : stack) {
         String fileName = e.getFileName();
         if (e.getMethodName().startsWith("_c_") && e.getLineNumber() > -1 && fileName != null && !fileName.endsWith(".java")) {
            String methodName = e.getMethodName();
            Matcher match = pattern.matcher(methodName);
            methodName = !"_c_script_0".equals(methodName) && match.find() ? match.group(1) : null;
            list.add(new ScriptStackElement(fileName, methodName, e.getLineNumber()));
         } else if ("org.mozilla.javascript.Interpreter".equals(e.getClassName()) && "interpretLoop".equals(e.getMethodName()) && interpreterStack != null && interpreterStack.length > interpreterStackIndex) {
            for(ScriptStackElement elem : interpreterStack[interpreterStackIndex++]) {
               list.add(elem);
            }
         }
      }

      return (ScriptStackElement[])list.toArray(new ScriptStackElement[list.size()]);
   }

   public void printStackTrace(PrintWriter s) {
      if (this.interpreterStackInfo == null) {
         super.printStackTrace(s);
      } else {
         s.print(this.generateStackTrace());
      }

   }

   public void printStackTrace(PrintStream s) {
      if (this.interpreterStackInfo == null) {
         super.printStackTrace(s);
      } else {
         s.print(this.generateStackTrace());
      }

   }

   public static boolean usesMozillaStackStyle() {
      return useMozillaStackStyle;
   }

   public static void useMozillaStackStyle(boolean flag) {
      useMozillaStackStyle = flag;
   }
}
