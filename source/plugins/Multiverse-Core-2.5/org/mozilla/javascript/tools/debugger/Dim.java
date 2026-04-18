package org.mozilla.javascript.tools.debugger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Kit;
import org.mozilla.javascript.NativeCall;
import org.mozilla.javascript.ObjArray;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.SecurityUtilities;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.debug.DebugFrame;
import org.mozilla.javascript.debug.DebuggableObject;
import org.mozilla.javascript.debug.DebuggableScript;
import org.mozilla.javascript.debug.Debugger;

public class Dim {
   public static final int STEP_OVER = 0;
   public static final int STEP_INTO = 1;
   public static final int STEP_OUT = 2;
   public static final int GO = 3;
   public static final int BREAK = 4;
   public static final int EXIT = 5;
   private static final int IPROXY_DEBUG = 0;
   private static final int IPROXY_LISTEN = 1;
   private static final int IPROXY_COMPILE_SCRIPT = 2;
   private static final int IPROXY_EVAL_SCRIPT = 3;
   private static final int IPROXY_STRING_IS_COMPILABLE = 4;
   private static final int IPROXY_OBJECT_TO_STRING = 5;
   private static final int IPROXY_OBJECT_PROPERTY = 6;
   private static final int IPROXY_OBJECT_IDS = 7;
   private GuiCallback callback;
   private boolean breakFlag;
   private ScopeProvider scopeProvider;
   private SourceProvider sourceProvider;
   private int frameIndex = -1;
   private volatile ContextData interruptedContextData;
   private ContextFactory contextFactory;
   private Object monitor = new Object();
   private Object eventThreadMonitor = new Object();
   private volatile int returnValue = -1;
   private boolean insideInterruptLoop;
   private String evalRequest;
   private StackFrame evalFrame;
   private String evalResult;
   private boolean breakOnExceptions;
   private boolean breakOnEnter;
   private boolean breakOnReturn;
   private final Map urlToSourceInfo = Collections.synchronizedMap(new HashMap());
   private final Map functionNames = Collections.synchronizedMap(new HashMap());
   private final Map functionToSource = Collections.synchronizedMap(new HashMap());
   private DimIProxy listener;

   public Dim() {
      super();
   }

   public void setGuiCallback(GuiCallback callback) {
      this.callback = callback;
   }

   public void setBreak() {
      this.breakFlag = true;
   }

   public void setScopeProvider(ScopeProvider scopeProvider) {
      this.scopeProvider = scopeProvider;
   }

   public void setSourceProvider(SourceProvider sourceProvider) {
      this.sourceProvider = sourceProvider;
   }

   public void contextSwitch(int frameIndex) {
      this.frameIndex = frameIndex;
   }

   public void setBreakOnExceptions(boolean breakOnExceptions) {
      this.breakOnExceptions = breakOnExceptions;
   }

   public void setBreakOnEnter(boolean breakOnEnter) {
      this.breakOnEnter = breakOnEnter;
   }

   public void setBreakOnReturn(boolean breakOnReturn) {
      this.breakOnReturn = breakOnReturn;
   }

   public void attachTo(ContextFactory factory) {
      this.detach();
      this.contextFactory = factory;
      this.listener = new DimIProxy(this, 1);
      factory.addListener(this.listener);
   }

   public void detach() {
      if (this.listener != null) {
         this.contextFactory.removeListener(this.listener);
         this.contextFactory = null;
         this.listener = null;
      }

   }

   public void dispose() {
      this.detach();
   }

   private FunctionSource getFunctionSource(DebuggableScript fnOrScript) {
      FunctionSource fsource = this.functionSource(fnOrScript);
      if (fsource == null) {
         String url = this.getNormalizedUrl(fnOrScript);
         SourceInfo si = this.sourceInfo(url);
         if (si == null && !fnOrScript.isGeneratedScript()) {
            String source = this.loadSource(url);
            if (source != null) {
               DebuggableScript top = fnOrScript;

               while(true) {
                  DebuggableScript parent = top.getParent();
                  if (parent == null) {
                     this.registerTopScript(top, source);
                     fsource = this.functionSource(fnOrScript);
                     break;
                  }

                  top = parent;
               }
            }
         }
      }

      return fsource;
   }

   private String loadSource(String sourceUrl) {
      String source = null;
      int hash = sourceUrl.indexOf(35);
      if (hash >= 0) {
         sourceUrl = sourceUrl.substring(0, hash);
      }

      try {
         InputStream is;
         label92: {
            if (sourceUrl.indexOf(58) < 0) {
               try {
                  if (sourceUrl.startsWith("~/")) {
                     String home = SecurityUtilities.getSystemProperty("user.home");
                     if (home != null) {
                        String pathFromHome = sourceUrl.substring(2);
                        File f = new File(new File(home), pathFromHome);
                        if (f.exists()) {
                           is = new FileInputStream(f);
                           break label92;
                        }
                     }
                  }

                  File f = new File(sourceUrl);
                  if (f.exists()) {
                     is = new FileInputStream(f);
                     break label92;
                  }
               } catch (SecurityException var13) {
               }

               if (sourceUrl.startsWith("//")) {
                  sourceUrl = "http:" + sourceUrl;
               } else if (sourceUrl.startsWith("/")) {
                  sourceUrl = "http://127.0.0.1" + sourceUrl;
               } else {
                  sourceUrl = "http://" + sourceUrl;
               }
            }

            is = (new URL(sourceUrl)).openStream();
         }

         try {
            source = Kit.readReader(new InputStreamReader(is));
         } finally {
            is.close();
         }
      } catch (IOException ex) {
         System.err.println("Failed to load source from " + sourceUrl + ": " + ex);
      }

      return source;
   }

   private void registerTopScript(DebuggableScript topScript, String source) {
      if (!topScript.isTopLevel()) {
         throw new IllegalArgumentException();
      } else {
         String url = this.getNormalizedUrl(topScript);
         DebuggableScript[] functions = getAllFunctions(topScript);
         if (this.sourceProvider != null) {
            String providedSource = this.sourceProvider.getSource(topScript);
            if (providedSource != null) {
               source = providedSource;
            }
         }

         SourceInfo sourceInfo = new SourceInfo(source, functions, url);
         synchronized(this.urlToSourceInfo) {
            SourceInfo old = (SourceInfo)this.urlToSourceInfo.get(url);
            if (old != null) {
               sourceInfo.copyBreakpointsFrom(old);
            }

            this.urlToSourceInfo.put(url, sourceInfo);

            for(int i = 0; i != sourceInfo.functionSourcesTop(); ++i) {
               FunctionSource fsource = sourceInfo.functionSource(i);
               String name = fsource.name();
               if (name.length() != 0) {
                  this.functionNames.put(name, fsource);
               }
            }
         }

         synchronized(this.functionToSource) {
            for(int i = 0; i != functions.length; ++i) {
               FunctionSource fsource = sourceInfo.functionSource(i);
               this.functionToSource.put(functions[i], fsource);
            }
         }

         this.callback.updateSourceText(sourceInfo);
      }
   }

   private FunctionSource functionSource(DebuggableScript fnOrScript) {
      return (FunctionSource)this.functionToSource.get(fnOrScript);
   }

   public String[] functionNames() {
      synchronized(this.urlToSourceInfo) {
         return (String[])this.functionNames.keySet().toArray(new String[this.functionNames.size()]);
      }
   }

   public FunctionSource functionSourceByName(String functionName) {
      return (FunctionSource)this.functionNames.get(functionName);
   }

   public SourceInfo sourceInfo(String url) {
      return (SourceInfo)this.urlToSourceInfo.get(url);
   }

   private String getNormalizedUrl(DebuggableScript fnOrScript) {
      String url = fnOrScript.getSourceName();
      if (url == null) {
         url = "<stdin>";
      } else {
         char evalSeparator = '#';
         StringBuffer sb = null;
         int urlLength = url.length();
         int cursor = 0;

         while(true) {
            int searchStart = url.indexOf(evalSeparator, cursor);
            if (searchStart < 0) {
               break;
            }

            String replace = null;

            int i;
            for(i = searchStart + 1; i != urlLength; ++i) {
               int c = url.charAt(i);
               if (48 > c || c > 57) {
                  break;
               }
            }

            if (i != searchStart + 1 && "(eval)".regionMatches(0, url, i, 6)) {
               cursor = i + 6;
               replace = "(eval)";
            }

            if (replace == null) {
               break;
            }

            if (sb == null) {
               sb = new StringBuffer();
               sb.append(url.substring(0, searchStart));
            }

            sb.append(replace);
         }

         if (sb != null) {
            if (cursor != urlLength) {
               sb.append(url.substring(cursor));
            }

            url = sb.toString();
         }
      }

      return url;
   }

   private static DebuggableScript[] getAllFunctions(DebuggableScript function) {
      ObjArray functions = new ObjArray();
      collectFunctions_r(function, functions);
      DebuggableScript[] result = new DebuggableScript[functions.size()];
      functions.toArray(result);
      return result;
   }

   private static void collectFunctions_r(DebuggableScript function, ObjArray array) {
      array.add(function);

      for(int i = 0; i != function.getFunctionCount(); ++i) {
         collectFunctions_r(function.getFunction(i), array);
      }

   }

   public void clearAllBreakpoints() {
      for(SourceInfo si : this.urlToSourceInfo.values()) {
         si.removeAllBreakpoints();
      }

   }

   private void handleBreakpointHit(StackFrame frame, Context cx) {
      this.breakFlag = false;
      this.interrupted(cx, frame, (Throwable)null);
   }

   private void handleExceptionThrown(Context cx, Throwable ex, StackFrame frame) {
      if (this.breakOnExceptions) {
         ContextData cd = frame.contextData();
         if (cd.lastProcessedException != ex) {
            this.interrupted(cx, frame, ex);
            cd.lastProcessedException = ex;
         }
      }

   }

   public ContextData currentContextData() {
      return this.interruptedContextData;
   }

   public void setReturnValue(int returnValue) {
      synchronized(this.monitor) {
         this.returnValue = returnValue;
         this.monitor.notify();
      }
   }

   public void go() {
      synchronized(this.monitor) {
         this.returnValue = 3;
         this.monitor.notifyAll();
      }
   }

   public String eval(String expr) {
      String result = "undefined";
      if (expr == null) {
         return result;
      } else {
         ContextData contextData = this.currentContextData();
         if (contextData != null && this.frameIndex < contextData.frameCount()) {
            StackFrame frame = contextData.getFrame(this.frameIndex);
            if (contextData.eventThreadFlag) {
               Context cx = Context.getCurrentContext();
               result = do_eval(cx, frame, expr);
            } else {
               synchronized(this.monitor) {
                  if (this.insideInterruptLoop) {
                     this.evalRequest = expr;
                     this.evalFrame = frame;
                     this.monitor.notify();

                     do {
                        try {
                           this.monitor.wait();
                        } catch (InterruptedException var8) {
                           Thread.currentThread().interrupt();
                           break;
                        }
                     } while(this.evalRequest != null);

                     result = this.evalResult;
                  }
               }
            }

            return result;
         } else {
            return result;
         }
      }
   }

   public void compileScript(String url, String text) {
      DimIProxy action = new DimIProxy(this, 2);
      action.url = url;
      action.text = text;
      action.withContext();
   }

   public void evalScript(String url, String text) {
      DimIProxy action = new DimIProxy(this, 3);
      action.url = url;
      action.text = text;
      action.withContext();
   }

   public String objectToString(Object object) {
      DimIProxy action = new DimIProxy(this, 5);
      action.object = object;
      action.withContext();
      return action.stringResult;
   }

   public boolean stringIsCompilableUnit(String str) {
      DimIProxy action = new DimIProxy(this, 4);
      action.text = str;
      action.withContext();
      return action.booleanResult;
   }

   public Object getObjectProperty(Object object, Object id) {
      DimIProxy action = new DimIProxy(this, 6);
      action.object = object;
      action.id = id;
      action.withContext();
      return action.objectResult;
   }

   public Object[] getObjectIds(Object object) {
      DimIProxy action = new DimIProxy(this, 7);
      action.object = object;
      action.withContext();
      return action.objectArrayResult;
   }

   private Object getObjectPropertyImpl(Context cx, Object object, Object id) {
      Scriptable scriptable = (Scriptable)object;
      Object result;
      if (id instanceof String) {
         String name = (String)id;
         if (name.equals("this")) {
            result = scriptable;
         } else if (name.equals("__proto__")) {
            result = scriptable.getPrototype();
         } else if (name.equals("__parent__")) {
            result = scriptable.getParentScope();
         } else {
            result = ScriptableObject.getProperty(scriptable, name);
            if (result == ScriptableObject.NOT_FOUND) {
               result = Undefined.instance;
            }
         }
      } else {
         int index = (Integer)id;
         result = ScriptableObject.getProperty(scriptable, index);
         if (result == ScriptableObject.NOT_FOUND) {
            result = Undefined.instance;
         }
      }

      return result;
   }

   private Object[] getObjectIdsImpl(Context cx, Object object) {
      if (object instanceof Scriptable && object != Undefined.instance) {
         Scriptable scriptable = (Scriptable)object;
         Object[] ids;
         if (scriptable instanceof DebuggableObject) {
            ids = ((DebuggableObject)scriptable).getAllIds();
         } else {
            ids = scriptable.getIds();
         }

         Scriptable proto = scriptable.getPrototype();
         Scriptable parent = scriptable.getParentScope();
         int extra = 0;
         if (proto != null) {
            ++extra;
         }

         if (parent != null) {
            ++extra;
         }

         if (extra != 0) {
            Object[] tmp = new Object[extra + ids.length];
            System.arraycopy(ids, 0, tmp, extra, ids.length);
            ids = tmp;
            extra = 0;
            if (proto != null) {
               tmp[extra++] = "__proto__";
            }

            if (parent != null) {
               tmp[extra++] = "__parent__";
            }
         }

         return ids;
      } else {
         return Context.emptyArgs;
      }
   }

   private void interrupted(Context cx, StackFrame frame, Throwable scriptException) {
      ContextData contextData = frame.contextData();
      boolean eventThreadFlag = this.callback.isGuiEventThread();
      contextData.eventThreadFlag = eventThreadFlag;
      boolean recursiveEventThreadCall = false;
      synchronized(this.eventThreadMonitor) {
         label499: {
            if (eventThreadFlag) {
               if (this.interruptedContextData != null) {
                  recursiveEventThreadCall = true;
                  break label499;
               }
            } else {
               while(this.interruptedContextData != null) {
                  try {
                     this.eventThreadMonitor.wait();
                  } catch (InterruptedException var51) {
                     return;
                  }
               }
            }

            this.interruptedContextData = contextData;
         }
      }

      if (!recursiveEventThreadCall) {
         if (this.interruptedContextData == null) {
            Kit.codeBug();
         }

         try {
            int frameCount = contextData.frameCount();
            this.frameIndex = frameCount - 1;
            String threadTitle = Thread.currentThread().toString();
            String alertMessage;
            if (scriptException == null) {
               alertMessage = null;
            } else {
               alertMessage = scriptException.toString();
            }

            int returnValue = -1;
            if (!eventThreadFlag) {
               synchronized(this.monitor) {
                  if (this.insideInterruptLoop) {
                     Kit.codeBug();
                  }

                  this.insideInterruptLoop = true;
                  this.evalRequest = null;
                  this.returnValue = -1;
                  this.callback.enterInterrupt(frame, threadTitle, alertMessage);

                  try {
                     while(true) {
                        try {
                           this.monitor.wait();
                        } catch (InterruptedException var52) {
                           Thread.currentThread().interrupt();
                           break;
                        }

                        if (this.evalRequest != null) {
                           this.evalResult = null;

                           try {
                              this.evalResult = do_eval(cx, this.evalFrame, this.evalRequest);
                           } finally {
                              this.evalRequest = null;
                              this.evalFrame = null;
                              this.monitor.notify();
                           }
                        } else if (this.returnValue != -1) {
                           returnValue = this.returnValue;
                           break;
                        }
                     }
                  } finally {
                     this.insideInterruptLoop = false;
                  }
               }
            } else {
               this.returnValue = -1;
               this.callback.enterInterrupt(frame, threadTitle, alertMessage);

               while(this.returnValue == -1) {
                  try {
                     this.callback.dispatchNextGuiEvent();
                  } catch (InterruptedException var50) {
                  }
               }

               returnValue = this.returnValue;
            }

            switch (returnValue) {
               case 0:
                  contextData.breakNextLine = true;
                  contextData.stopAtFrameDepth = contextData.frameCount();
                  break;
               case 1:
                  contextData.breakNextLine = true;
                  contextData.stopAtFrameDepth = -1;
                  break;
               case 2:
                  if (contextData.frameCount() > 1) {
                     contextData.breakNextLine = true;
                     contextData.stopAtFrameDepth = contextData.frameCount() - 1;
                  }
            }
         } finally {
            synchronized(this.eventThreadMonitor) {
               this.interruptedContextData = null;
               this.eventThreadMonitor.notifyAll();
            }
         }

      }
   }

   private static String do_eval(Context cx, StackFrame frame, String expr) {
      Debugger saved_debugger = cx.getDebugger();
      Object saved_data = cx.getDebuggerContextData();
      int saved_level = cx.getOptimizationLevel();
      cx.setDebugger((Debugger)null, (Object)null);
      cx.setOptimizationLevel(-1);
      cx.setGeneratingDebug(false);

      String resultString;
      try {
         Callable script = (Callable)cx.compileString(expr, "", 0, (Object)null);
         Object result = script.call(cx, frame.scope, frame.thisObj, ScriptRuntime.emptyArgs);
         if (result == Undefined.instance) {
            resultString = "";
         } else {
            resultString = ScriptRuntime.toString(result);
         }
      } catch (Exception exc) {
         resultString = exc.getMessage();
      } finally {
         cx.setGeneratingDebug(true);
         cx.setOptimizationLevel(saved_level);
         cx.setDebugger(saved_debugger, saved_data);
      }

      if (resultString == null) {
         resultString = "null";
      }

      return resultString;
   }

   private static class DimIProxy implements ContextAction, ContextFactory.Listener, Debugger {
      private Dim dim;
      private int type;
      private String url;
      private String text;
      private Object object;
      private Object id;
      private boolean booleanResult;
      private String stringResult;
      private Object objectResult;
      private Object[] objectArrayResult;

      private DimIProxy(Dim dim, int type) {
         super();
         this.dim = dim;
         this.type = type;
      }

      public Object run(Context cx) {
         switch (this.type) {
            case 2:
               cx.compileString(this.text, this.url, 1, (Object)null);
               break;
            case 3:
               Scriptable scope = null;
               if (this.dim.scopeProvider != null) {
                  scope = this.dim.scopeProvider.getScope();
               }

               if (scope == null) {
                  scope = new ImporterTopLevel(cx);
               }

               cx.evaluateString(scope, this.text, this.url, 1, (Object)null);
               break;
            case 4:
               this.booleanResult = cx.stringIsCompilableUnit(this.text);
               break;
            case 5:
               if (this.object == Undefined.instance) {
                  this.stringResult = "undefined";
               } else if (this.object == null) {
                  this.stringResult = "null";
               } else if (this.object instanceof NativeCall) {
                  this.stringResult = "[object Call]";
               } else {
                  this.stringResult = Context.toString(this.object);
               }
               break;
            case 6:
               this.objectResult = this.dim.getObjectPropertyImpl(cx, this.object, this.id);
               break;
            case 7:
               this.objectArrayResult = this.dim.getObjectIdsImpl(cx, this.object);
               break;
            default:
               throw Kit.codeBug();
         }

         return null;
      }

      private void withContext() {
         this.dim.contextFactory.call(this);
      }

      public void contextCreated(Context cx) {
         if (this.type != 1) {
            Kit.codeBug();
         }

         ContextData contextData = new ContextData();
         Debugger debugger = new DimIProxy(this.dim, 0);
         cx.setDebugger(debugger, contextData);
         cx.setGeneratingDebug(true);
         cx.setOptimizationLevel(-1);
      }

      public void contextReleased(Context cx) {
         if (this.type != 1) {
            Kit.codeBug();
         }

      }

      public DebugFrame getFrame(Context cx, DebuggableScript fnOrScript) {
         if (this.type != 0) {
            Kit.codeBug();
         }

         FunctionSource item = this.dim.getFunctionSource(fnOrScript);
         return item == null ? null : new StackFrame(cx, this.dim, item);
      }

      public void handleCompilationDone(Context cx, DebuggableScript fnOrScript, String source) {
         if (this.type != 0) {
            Kit.codeBug();
         }

         if (fnOrScript.isTopLevel()) {
            this.dim.registerTopScript(fnOrScript, source);
         }
      }
   }

   public static class ContextData {
      private ObjArray frameStack = new ObjArray();
      private boolean breakNextLine;
      private int stopAtFrameDepth = -1;
      private boolean eventThreadFlag;
      private Throwable lastProcessedException;

      public ContextData() {
         super();
      }

      public static ContextData get(Context cx) {
         return (ContextData)cx.getDebuggerContextData();
      }

      public int frameCount() {
         return this.frameStack.size();
      }

      public StackFrame getFrame(int frameNumber) {
         int num = this.frameStack.size() - frameNumber - 1;
         return (StackFrame)this.frameStack.get(num);
      }

      private void pushFrame(StackFrame frame) {
         this.frameStack.push(frame);
      }

      private void popFrame() {
         this.frameStack.pop();
      }
   }

   public static class StackFrame implements DebugFrame {
      private Dim dim;
      private ContextData contextData;
      private Scriptable scope;
      private Scriptable thisObj;
      private FunctionSource fsource;
      private boolean[] breakpoints;
      private int lineNumber;

      private StackFrame(Context cx, Dim dim, FunctionSource fsource) {
         super();
         this.dim = dim;
         this.contextData = Dim.ContextData.get(cx);
         this.fsource = fsource;
         this.breakpoints = fsource.sourceInfo().breakpoints;
         this.lineNumber = fsource.firstLine();
      }

      public void onEnter(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
         this.contextData.pushFrame(this);
         this.scope = scope;
         this.thisObj = thisObj;
         if (this.dim.breakOnEnter) {
            this.dim.handleBreakpointHit(this, cx);
         }

      }

      public void onLineChange(Context cx, int lineno) {
         this.lineNumber = lineno;
         if (!this.breakpoints[lineno] && !this.dim.breakFlag) {
            boolean lineBreak = this.contextData.breakNextLine;
            if (lineBreak && this.contextData.stopAtFrameDepth >= 0) {
               lineBreak = this.contextData.frameCount() <= this.contextData.stopAtFrameDepth;
            }

            if (!lineBreak) {
               return;
            }

            this.contextData.stopAtFrameDepth = -1;
            this.contextData.breakNextLine = false;
         }

         this.dim.handleBreakpointHit(this, cx);
      }

      public void onExceptionThrown(Context cx, Throwable exception) {
         this.dim.handleExceptionThrown(cx, exception, this);
      }

      public void onExit(Context cx, boolean byThrow, Object resultOrException) {
         if (this.dim.breakOnReturn && !byThrow) {
            this.dim.handleBreakpointHit(this, cx);
         }

         this.contextData.popFrame();
      }

      public void onDebuggerStatement(Context cx) {
         this.dim.handleBreakpointHit(this, cx);
      }

      public SourceInfo sourceInfo() {
         return this.fsource.sourceInfo();
      }

      public ContextData contextData() {
         return this.contextData;
      }

      public Object scope() {
         return this.scope;
      }

      public Object thisObj() {
         return this.thisObj;
      }

      public String getUrl() {
         return this.fsource.sourceInfo().url();
      }

      public int getLineNumber() {
         return this.lineNumber;
      }

      public String getFunctionName() {
         return this.fsource.name();
      }
   }

   public static class FunctionSource {
      private SourceInfo sourceInfo;
      private int firstLine;
      private String name;

      private FunctionSource(SourceInfo sourceInfo, int firstLine, String name) {
         super();
         if (name == null) {
            throw new IllegalArgumentException();
         } else {
            this.sourceInfo = sourceInfo;
            this.firstLine = firstLine;
            this.name = name;
         }
      }

      public SourceInfo sourceInfo() {
         return this.sourceInfo;
      }

      public int firstLine() {
         return this.firstLine;
      }

      public String name() {
         return this.name;
      }
   }

   public static class SourceInfo {
      private static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
      private String source;
      private String url;
      private boolean[] breakableLines;
      private boolean[] breakpoints;
      private FunctionSource[] functionSources;

      private SourceInfo(String source, DebuggableScript[] functions, String normilizedUrl) {
         super();
         this.source = source;
         this.url = normilizedUrl;
         int N = functions.length;
         int[][] lineArrays = new int[N][];

         for(int i = 0; i != N; ++i) {
            lineArrays[i] = functions[i].getLineNumbers();
         }

         int minAll = 0;
         int maxAll = -1;
         int[] firstLines = new int[N];

         for(int i = 0; i != N; ++i) {
            int[] lines = lineArrays[i];
            if (lines != null && lines.length != 0) {
               int max;
               int min = max = lines[0];

               for(int j = 1; j != lines.length; ++j) {
                  int line = lines[j];
                  if (line < min) {
                     min = line;
                  } else if (line > max) {
                     max = line;
                  }
               }

               firstLines[i] = min;
               if (minAll > maxAll) {
                  minAll = min;
                  maxAll = max;
               } else {
                  if (min < minAll) {
                     minAll = min;
                  }

                  if (max > maxAll) {
                     maxAll = max;
                  }
               }
            } else {
               firstLines[i] = -1;
            }
         }

         if (minAll > maxAll) {
            this.breakableLines = EMPTY_BOOLEAN_ARRAY;
            this.breakpoints = EMPTY_BOOLEAN_ARRAY;
         } else {
            if (minAll < 0) {
               throw new IllegalStateException(String.valueOf(minAll));
            }

            int linesTop = maxAll + 1;
            this.breakableLines = new boolean[linesTop];
            this.breakpoints = new boolean[linesTop];

            for(int i = 0; i != N; ++i) {
               int[] lines = lineArrays[i];
               if (lines != null && lines.length != 0) {
                  for(int j = 0; j != lines.length; ++j) {
                     int line = lines[j];
                     this.breakableLines[line] = true;
                  }
               }
            }
         }

         this.functionSources = new FunctionSource[N];

         for(int i = 0; i != N; ++i) {
            String name = functions[i].getFunctionName();
            if (name == null) {
               name = "";
            }

            this.functionSources[i] = new FunctionSource(this, firstLines[i], name);
         }

      }

      public String source() {
         return this.source;
      }

      public String url() {
         return this.url;
      }

      public int functionSourcesTop() {
         return this.functionSources.length;
      }

      public FunctionSource functionSource(int i) {
         return this.functionSources[i];
      }

      private void copyBreakpointsFrom(SourceInfo old) {
         int end = old.breakpoints.length;
         if (end > this.breakpoints.length) {
            end = this.breakpoints.length;
         }

         for(int line = 0; line != end; ++line) {
            if (old.breakpoints[line]) {
               this.breakpoints[line] = true;
            }
         }

      }

      public boolean breakableLine(int line) {
         return line < this.breakableLines.length && this.breakableLines[line];
      }

      public boolean breakpoint(int line) {
         if (!this.breakableLine(line)) {
            throw new IllegalArgumentException(String.valueOf(line));
         } else {
            return line < this.breakpoints.length && this.breakpoints[line];
         }
      }

      public boolean breakpoint(int line, boolean value) {
         if (!this.breakableLine(line)) {
            throw new IllegalArgumentException(String.valueOf(line));
         } else {
            synchronized(this.breakpoints) {
               boolean changed;
               if (this.breakpoints[line] != value) {
                  this.breakpoints[line] = value;
                  changed = true;
               } else {
                  changed = false;
               }

               return changed;
            }
         }
      }

      public void removeAllBreakpoints() {
         synchronized(this.breakpoints) {
            for(int line = 0; line != this.breakpoints.length; ++line) {
               this.breakpoints[line] = false;
            }

         }
      }
   }
}
