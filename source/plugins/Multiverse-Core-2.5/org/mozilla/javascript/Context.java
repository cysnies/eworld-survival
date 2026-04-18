package org.mozilla.javascript;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.ScriptNode;
import org.mozilla.javascript.debug.DebuggableScript;
import org.mozilla.javascript.debug.Debugger;
import org.mozilla.javascript.xml.XMLLib;

public class Context {
   public static final int VERSION_UNKNOWN = -1;
   public static final int VERSION_DEFAULT = 0;
   public static final int VERSION_1_0 = 100;
   public static final int VERSION_1_1 = 110;
   public static final int VERSION_1_2 = 120;
   public static final int VERSION_1_3 = 130;
   public static final int VERSION_1_4 = 140;
   public static final int VERSION_1_5 = 150;
   public static final int VERSION_1_6 = 160;
   public static final int VERSION_1_7 = 170;
   public static final int VERSION_1_8 = 180;
   public static final int FEATURE_NON_ECMA_GET_YEAR = 1;
   public static final int FEATURE_MEMBER_EXPR_AS_FUNCTION_NAME = 2;
   public static final int FEATURE_RESERVED_KEYWORD_AS_IDENTIFIER = 3;
   public static final int FEATURE_TO_STRING_AS_SOURCE = 4;
   public static final int FEATURE_PARENT_PROTO_PROPERTIES = 5;
   /** @deprecated */
   public static final int FEATURE_PARENT_PROTO_PROPRTIES = 5;
   public static final int FEATURE_E4X = 6;
   public static final int FEATURE_DYNAMIC_SCOPE = 7;
   public static final int FEATURE_STRICT_VARS = 8;
   public static final int FEATURE_STRICT_EVAL = 9;
   public static final int FEATURE_LOCATION_INFORMATION_IN_ERROR = 10;
   public static final int FEATURE_STRICT_MODE = 11;
   public static final int FEATURE_WARNING_AS_ERROR = 12;
   public static final int FEATURE_ENHANCED_JAVA_ACCESS = 13;
   public static final String languageVersionProperty = "language version";
   public static final String errorReporterProperty = "error reporter";
   public static final Object[] emptyArgs;
   private static Class codegenClass;
   private static Class interpreterClass;
   private static String implementationVersion;
   private final ContextFactory factory;
   private boolean sealed;
   private Object sealKey;
   Scriptable topCallScope;
   boolean isContinuationsTopCall;
   NativeCall currentActivationCall;
   XMLLib cachedXMLLib;
   ObjToIntMap iterating;
   Object interpreterSecurityDomain;
   int version;
   private SecurityController securityController;
   private boolean hasClassShutter;
   private ClassShutter classShutter;
   private ErrorReporter errorReporter;
   RegExpProxy regExpProxy;
   private Locale locale;
   private boolean generatingDebug;
   private boolean generatingDebugChanged;
   private boolean generatingSource;
   boolean useDynamicScope;
   private int optimizationLevel;
   private int maximumInterpreterStackDepth;
   private WrapFactory wrapFactory;
   Debugger debugger;
   private Object debuggerData;
   private int enterCount;
   private Object propertyListeners;
   private Map threadLocalMap;
   private ClassLoader applicationClassLoader;
   Set activationNames;
   Object lastInterpreterFrame;
   ObjArray previousInterpreterInvocations;
   int instructionCount;
   int instructionThreshold;
   int scratchIndex;
   long scratchUint32;
   Scriptable scratchScriptable;
   public boolean generateObserverCount;

   /** @deprecated */
   public Context() {
      this(ContextFactory.getGlobal());
   }

   protected Context(ContextFactory factory) {
      super();
      this.generatingSource = true;
      this.generateObserverCount = false;
      if (factory == null) {
         throw new IllegalArgumentException("factory == null");
      } else {
         this.factory = factory;
         this.version = 0;
         this.optimizationLevel = codegenClass != null ? 0 : -1;
         this.maximumInterpreterStackDepth = Integer.MAX_VALUE;
      }
   }

   public static Context getCurrentContext() {
      Object helper = VMBridge.instance.getThreadContextHelper();
      return VMBridge.instance.getContext(helper);
   }

   public static Context enter() {
      return enter((Context)null);
   }

   /** @deprecated */
   public static Context enter(Context cx) {
      return enter(cx, ContextFactory.getGlobal());
   }

   static final Context enter(Context cx, ContextFactory factory) {
      Object helper = VMBridge.instance.getThreadContextHelper();
      Context old = VMBridge.instance.getContext(helper);
      if (old != null) {
         cx = old;
      } else {
         if (cx == null) {
            cx = factory.makeContext();
            if (cx.enterCount != 0) {
               throw new IllegalStateException("factory.makeContext() returned Context instance already associated with some thread");
            }

            factory.onContextCreated(cx);
            if (factory.isSealed() && !cx.isSealed()) {
               cx.seal((Object)null);
            }
         } else if (cx.enterCount != 0) {
            throw new IllegalStateException("can not use Context instance already associated with some thread");
         }

         VMBridge.instance.setContext(helper, cx);
      }

      ++cx.enterCount;
      return cx;
   }

   public static void exit() {
      Object helper = VMBridge.instance.getThreadContextHelper();
      Context cx = VMBridge.instance.getContext(helper);
      if (cx == null) {
         throw new IllegalStateException("Calling Context.exit without previous Context.enter");
      } else {
         if (cx.enterCount < 1) {
            Kit.codeBug();
         }

         if (--cx.enterCount == 0) {
            VMBridge.instance.setContext(helper, (Context)null);
            cx.factory.onContextReleased(cx);
         }

      }
   }

   /** @deprecated */
   public static Object call(ContextAction action) {
      return call(ContextFactory.getGlobal(), action);
   }

   public static Object call(ContextFactory factory, final Callable callable, final Scriptable scope, final Scriptable thisObj, final Object[] args) {
      if (factory == null) {
         factory = ContextFactory.getGlobal();
      }

      return call(factory, new ContextAction() {
         public Object run(Context cx) {
            return callable.call(cx, scope, thisObj, args);
         }
      });
   }

   static Object call(ContextFactory factory, ContextAction action) {
      Context cx = enter((Context)null, factory);

      Object var3;
      try {
         var3 = action.run(cx);
      } finally {
         exit();
      }

      return var3;
   }

   /** @deprecated */
   public static void addContextListener(ContextListener listener) {
      String DBG = "org.mozilla.javascript.tools.debugger.Main";
      if (DBG.equals(listener.getClass().getName())) {
         Class<?> cl = listener.getClass();
         Class<?> factoryClass = Kit.classOrNull("org.mozilla.javascript.ContextFactory");
         Class<?>[] sig = new Class[]{factoryClass};
         Object[] args = new Object[]{ContextFactory.getGlobal()};

         try {
            Method m = cl.getMethod("attachTo", sig);
            m.invoke(listener, args);
         } catch (Exception ex) {
            RuntimeException rex = new RuntimeException();
            Kit.initCause(rex, ex);
            throw rex;
         }
      } else {
         ContextFactory.getGlobal().addListener(listener);
      }
   }

   /** @deprecated */
   public static void removeContextListener(ContextListener listener) {
      ContextFactory.getGlobal().addListener(listener);
   }

   public final ContextFactory getFactory() {
      return this.factory;
   }

   public final boolean isSealed() {
      return this.sealed;
   }

   public final void seal(Object sealKey) {
      if (this.sealed) {
         onSealedMutation();
      }

      this.sealed = true;
      this.sealKey = sealKey;
   }

   public final void unseal(Object sealKey) {
      if (sealKey == null) {
         throw new IllegalArgumentException();
      } else if (this.sealKey != sealKey) {
         throw new IllegalArgumentException();
      } else if (!this.sealed) {
         throw new IllegalStateException();
      } else {
         this.sealed = false;
         this.sealKey = null;
      }
   }

   static void onSealedMutation() {
      throw new IllegalStateException();
   }

   public final int getLanguageVersion() {
      return this.version;
   }

   public void setLanguageVersion(int version) {
      if (this.sealed) {
         onSealedMutation();
      }

      checkLanguageVersion(version);
      Object listeners = this.propertyListeners;
      if (listeners != null && version != this.version) {
         this.firePropertyChangeImpl(listeners, "language version", this.version, version);
      }

      this.version = version;
   }

   public static boolean isValidLanguageVersion(int version) {
      switch (version) {
         case 0:
         case 100:
         case 110:
         case 120:
         case 130:
         case 140:
         case 150:
         case 160:
         case 170:
         case 180:
            return true;
         default:
            return false;
      }
   }

   public static void checkLanguageVersion(int version) {
      if (!isValidLanguageVersion(version)) {
         throw new IllegalArgumentException("Bad language version: " + version);
      }
   }

   public final String getImplementationVersion() {
      if (implementationVersion == null) {
         implementationVersion = ScriptRuntime.getMessage0("implementation.version");
      }

      return implementationVersion;
   }

   public final ErrorReporter getErrorReporter() {
      return (ErrorReporter)(this.errorReporter == null ? DefaultErrorReporter.instance : this.errorReporter);
   }

   public final ErrorReporter setErrorReporter(ErrorReporter reporter) {
      if (this.sealed) {
         onSealedMutation();
      }

      if (reporter == null) {
         throw new IllegalArgumentException();
      } else {
         ErrorReporter old = this.getErrorReporter();
         if (reporter == old) {
            return old;
         } else {
            Object listeners = this.propertyListeners;
            if (listeners != null) {
               this.firePropertyChangeImpl(listeners, "error reporter", old, reporter);
            }

            this.errorReporter = reporter;
            return old;
         }
      }
   }

   public final Locale getLocale() {
      if (this.locale == null) {
         this.locale = Locale.getDefault();
      }

      return this.locale;
   }

   public final Locale setLocale(Locale loc) {
      if (this.sealed) {
         onSealedMutation();
      }

      Locale result = this.locale;
      this.locale = loc;
      return result;
   }

   public final void addPropertyChangeListener(PropertyChangeListener l) {
      if (this.sealed) {
         onSealedMutation();
      }

      this.propertyListeners = Kit.addListener(this.propertyListeners, l);
   }

   public final void removePropertyChangeListener(PropertyChangeListener l) {
      if (this.sealed) {
         onSealedMutation();
      }

      this.propertyListeners = Kit.removeListener(this.propertyListeners, l);
   }

   final void firePropertyChange(String property, Object oldValue, Object newValue) {
      Object listeners = this.propertyListeners;
      if (listeners != null) {
         this.firePropertyChangeImpl(listeners, property, oldValue, newValue);
      }

   }

   private void firePropertyChangeImpl(Object listeners, String property, Object oldValue, Object newValue) {
      int i = 0;

      while(true) {
         Object l = Kit.getListener(listeners, i);
         if (l == null) {
            return;
         }

         if (l instanceof PropertyChangeListener) {
            PropertyChangeListener pcl = (PropertyChangeListener)l;
            pcl.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
         }

         ++i;
      }
   }

   public static void reportWarning(String message, String sourceName, int lineno, String lineSource, int lineOffset) {
      Context cx = getContext();
      if (cx.hasFeature(12)) {
         reportError(message, sourceName, lineno, lineSource, lineOffset);
      } else {
         cx.getErrorReporter().warning(message, sourceName, lineno, lineSource, lineOffset);
      }

   }

   public static void reportWarning(String message) {
      int[] linep = new int[]{0};
      String filename = getSourcePositionFromStack(linep);
      reportWarning(message, filename, linep[0], (String)null, 0);
   }

   public static void reportWarning(String message, Throwable t) {
      int[] linep = new int[]{0};
      String filename = getSourcePositionFromStack(linep);
      Writer sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      pw.println(message);
      t.printStackTrace(pw);
      pw.flush();
      reportWarning(sw.toString(), filename, linep[0], (String)null, 0);
   }

   public static void reportError(String message, String sourceName, int lineno, String lineSource, int lineOffset) {
      Context cx = getCurrentContext();
      if (cx != null) {
         cx.getErrorReporter().error(message, sourceName, lineno, lineSource, lineOffset);
      } else {
         throw new EvaluatorException(message, sourceName, lineno, lineSource, lineOffset);
      }
   }

   public static void reportError(String message) {
      int[] linep = new int[]{0};
      String filename = getSourcePositionFromStack(linep);
      reportError(message, filename, linep[0], (String)null, 0);
   }

   public static EvaluatorException reportRuntimeError(String message, String sourceName, int lineno, String lineSource, int lineOffset) {
      Context cx = getCurrentContext();
      if (cx != null) {
         return cx.getErrorReporter().runtimeError(message, sourceName, lineno, lineSource, lineOffset);
      } else {
         throw new EvaluatorException(message, sourceName, lineno, lineSource, lineOffset);
      }
   }

   static EvaluatorException reportRuntimeError0(String messageId) {
      String msg = ScriptRuntime.getMessage0(messageId);
      return reportRuntimeError(msg);
   }

   static EvaluatorException reportRuntimeError1(String messageId, Object arg1) {
      String msg = ScriptRuntime.getMessage1(messageId, arg1);
      return reportRuntimeError(msg);
   }

   static EvaluatorException reportRuntimeError2(String messageId, Object arg1, Object arg2) {
      String msg = ScriptRuntime.getMessage2(messageId, arg1, arg2);
      return reportRuntimeError(msg);
   }

   static EvaluatorException reportRuntimeError3(String messageId, Object arg1, Object arg2, Object arg3) {
      String msg = ScriptRuntime.getMessage3(messageId, arg1, arg2, arg3);
      return reportRuntimeError(msg);
   }

   static EvaluatorException reportRuntimeError4(String messageId, Object arg1, Object arg2, Object arg3, Object arg4) {
      String msg = ScriptRuntime.getMessage4(messageId, arg1, arg2, arg3, arg4);
      return reportRuntimeError(msg);
   }

   public static EvaluatorException reportRuntimeError(String message) {
      int[] linep = new int[]{0};
      String filename = getSourcePositionFromStack(linep);
      return reportRuntimeError(message, filename, linep[0], (String)null, 0);
   }

   public final ScriptableObject initStandardObjects() {
      return this.initStandardObjects((ScriptableObject)null, false);
   }

   public final Scriptable initStandardObjects(ScriptableObject scope) {
      return this.initStandardObjects(scope, false);
   }

   public ScriptableObject initStandardObjects(ScriptableObject scope, boolean sealed) {
      return ScriptRuntime.initStandardObjects(this, scope, sealed);
   }

   public static Object getUndefinedValue() {
      return Undefined.instance;
   }

   public final Object evaluateString(Scriptable scope, String source, String sourceName, int lineno, Object securityDomain) {
      Script script = this.compileString(source, sourceName, lineno, securityDomain);
      return script != null ? script.exec(this, scope) : null;
   }

   public final Object evaluateReader(Scriptable scope, Reader in, String sourceName, int lineno, Object securityDomain) throws IOException {
      Script script = this.compileReader(scope, in, sourceName, lineno, securityDomain);
      return script != null ? script.exec(this, scope) : null;
   }

   public Object executeScriptWithContinuations(Script script, Scriptable scope) throws ContinuationPending {
      if (script instanceof InterpretedFunction && ((InterpretedFunction)script).isScript()) {
         return this.callFunctionWithContinuations((InterpretedFunction)script, scope, ScriptRuntime.emptyArgs);
      } else {
         throw new IllegalArgumentException("Script argument was not a script or was not created by interpreted mode ");
      }
   }

   public Object callFunctionWithContinuations(Callable function, Scriptable scope, Object[] args) throws ContinuationPending {
      if (!(function instanceof InterpretedFunction)) {
         throw new IllegalArgumentException("Function argument was not created by interpreted mode ");
      } else if (ScriptRuntime.hasTopCall(this)) {
         throw new IllegalStateException("Cannot have any pending top calls when executing a script with continuations");
      } else {
         this.isContinuationsTopCall = true;
         return ScriptRuntime.doTopCall(function, this, scope, scope, args);
      }
   }

   public ContinuationPending captureContinuation() {
      return new ContinuationPending(Interpreter.captureContinuation(this));
   }

   public Object resumeContinuation(Object continuation, Scriptable scope, Object functionResult) throws ContinuationPending {
      Object[] args = new Object[]{functionResult};
      return Interpreter.restartContinuation((NativeContinuation)continuation, this, scope, args);
   }

   public final boolean stringIsCompilableUnit(String source) {
      boolean errorseen = false;
      CompilerEnvirons compilerEnv = new CompilerEnvirons();
      compilerEnv.initFromContext(this);
      compilerEnv.setGeneratingSource(false);
      Parser p = new Parser(compilerEnv, DefaultErrorReporter.instance);

      try {
         p.parse((String)source, (String)null, 1);
      } catch (EvaluatorException var6) {
         errorseen = true;
      }

      return !errorseen || !p.eof();
   }

   /** @deprecated */
   public final Script compileReader(Scriptable scope, Reader in, String sourceName, int lineno, Object securityDomain) throws IOException {
      return this.compileReader(in, sourceName, lineno, securityDomain);
   }

   public final Script compileReader(Reader in, String sourceName, int lineno, Object securityDomain) throws IOException {
      if (lineno < 0) {
         lineno = 0;
      }

      return (Script)this.compileImpl((Scriptable)null, in, (String)null, sourceName, lineno, securityDomain, false, (Evaluator)null, (ErrorReporter)null);
   }

   public final Script compileString(String source, String sourceName, int lineno, Object securityDomain) {
      if (lineno < 0) {
         lineno = 0;
      }

      return this.compileString(source, (Evaluator)null, (ErrorReporter)null, sourceName, lineno, securityDomain);
   }

   final Script compileString(String source, Evaluator compiler, ErrorReporter compilationErrorReporter, String sourceName, int lineno, Object securityDomain) {
      try {
         return (Script)this.compileImpl((Scriptable)null, (Reader)null, source, sourceName, lineno, securityDomain, false, compiler, compilationErrorReporter);
      } catch (IOException var8) {
         throw new RuntimeException();
      }
   }

   public final Function compileFunction(Scriptable scope, String source, String sourceName, int lineno, Object securityDomain) {
      return this.compileFunction(scope, source, (Evaluator)null, (ErrorReporter)null, sourceName, lineno, securityDomain);
   }

   final Function compileFunction(Scriptable scope, String source, Evaluator compiler, ErrorReporter compilationErrorReporter, String sourceName, int lineno, Object securityDomain) {
      try {
         return (Function)this.compileImpl(scope, (Reader)null, source, sourceName, lineno, securityDomain, true, compiler, compilationErrorReporter);
      } catch (IOException var9) {
         throw new RuntimeException();
      }
   }

   public final String decompileScript(Script script, int indent) {
      NativeFunction scriptImpl = (NativeFunction)script;
      return scriptImpl.decompile(indent, 0);
   }

   public final String decompileFunction(Function fun, int indent) {
      return fun instanceof BaseFunction ? ((BaseFunction)fun).decompile(indent, 0) : "function " + fun.getClassName() + "() {\n\t[native code]\n}\n";
   }

   public final String decompileFunctionBody(Function fun, int indent) {
      if (fun instanceof BaseFunction) {
         BaseFunction bf = (BaseFunction)fun;
         return bf.decompile(indent, 1);
      } else {
         return "[native code]\n";
      }
   }

   public Scriptable newObject(Scriptable scope) {
      NativeObject result = new NativeObject();
      ScriptRuntime.setBuiltinProtoAndParent(result, scope, TopLevel.Builtins.Object);
      return result;
   }

   public Scriptable newObject(Scriptable scope, String constructorName) {
      return this.newObject(scope, constructorName, ScriptRuntime.emptyArgs);
   }

   public Scriptable newObject(Scriptable scope, String constructorName, Object[] args) {
      scope = ScriptableObject.getTopLevelScope(scope);
      Function ctor = ScriptRuntime.getExistingCtor(this, scope, constructorName);
      if (args == null) {
         args = ScriptRuntime.emptyArgs;
      }

      return ctor.construct(this, scope, args);
   }

   public Scriptable newArray(Scriptable scope, int length) {
      NativeArray result = new NativeArray((long)length);
      ScriptRuntime.setBuiltinProtoAndParent(result, scope, TopLevel.Builtins.Array);
      return result;
   }

   public Scriptable newArray(Scriptable scope, Object[] elements) {
      if (elements.getClass().getComponentType() != ScriptRuntime.ObjectClass) {
         throw new IllegalArgumentException();
      } else {
         NativeArray result = new NativeArray(elements);
         ScriptRuntime.setBuiltinProtoAndParent(result, scope, TopLevel.Builtins.Array);
         return result;
      }
   }

   public final Object[] getElements(Scriptable object) {
      return ScriptRuntime.getArrayElements(object);
   }

   public static boolean toBoolean(Object value) {
      return ScriptRuntime.toBoolean(value);
   }

   public static double toNumber(Object value) {
      return ScriptRuntime.toNumber(value);
   }

   public static String toString(Object value) {
      return ScriptRuntime.toString(value);
   }

   public static Scriptable toObject(Object value, Scriptable scope) {
      return ScriptRuntime.toObject(scope, value);
   }

   /** @deprecated */
   public static Scriptable toObject(Object value, Scriptable scope, Class staticType) {
      return ScriptRuntime.toObject(scope, value);
   }

   public static Object javaToJS(Object value, Scriptable scope) {
      if (!(value instanceof String) && !(value instanceof Number) && !(value instanceof Boolean) && !(value instanceof Scriptable)) {
         if (value instanceof Character) {
            return String.valueOf((Character)value);
         } else {
            Context cx = getContext();
            return cx.getWrapFactory().wrap(cx, scope, value, (Class)null);
         }
      } else {
         return value;
      }
   }

   public static Object jsToJava(Object value, Class desiredType) throws EvaluatorException {
      return NativeJavaObject.coerceTypeImpl(desiredType, value);
   }

   /** @deprecated */
   public static Object toType(Object value, Class desiredType) throws IllegalArgumentException {
      try {
         return jsToJava(value, desiredType);
      } catch (EvaluatorException ex) {
         IllegalArgumentException ex2 = new IllegalArgumentException(ex.getMessage());
         Kit.initCause(ex2, ex);
         throw ex2;
      }
   }

   public static RuntimeException throwAsScriptRuntimeEx(Throwable e) {
      while(e instanceof InvocationTargetException) {
         e = ((InvocationTargetException)e).getTargetException();
      }

      if (e instanceof Error) {
         Context cx = getContext();
         if (cx == null || !cx.hasFeature(13)) {
            throw (Error)e;
         }
      }

      if (e instanceof RhinoException) {
         throw (RhinoException)e;
      } else {
         throw new WrappedException(e);
      }
   }

   public final boolean isGeneratingDebug() {
      return this.generatingDebug;
   }

   public final void setGeneratingDebug(boolean generatingDebug) {
      if (this.sealed) {
         onSealedMutation();
      }

      this.generatingDebugChanged = true;
      if (generatingDebug && this.getOptimizationLevel() > 0) {
         this.setOptimizationLevel(0);
      }

      this.generatingDebug = generatingDebug;
   }

   public final boolean isGeneratingSource() {
      return this.generatingSource;
   }

   public final void setGeneratingSource(boolean generatingSource) {
      if (this.sealed) {
         onSealedMutation();
      }

      this.generatingSource = generatingSource;
   }

   public final int getOptimizationLevel() {
      return this.optimizationLevel;
   }

   public final void setOptimizationLevel(int optimizationLevel) {
      if (this.sealed) {
         onSealedMutation();
      }

      if (optimizationLevel == -2) {
         optimizationLevel = -1;
      }

      checkOptimizationLevel(optimizationLevel);
      if (codegenClass == null) {
         optimizationLevel = -1;
      }

      this.optimizationLevel = optimizationLevel;
   }

   public static boolean isValidOptimizationLevel(int optimizationLevel) {
      return -1 <= optimizationLevel && optimizationLevel <= 9;
   }

   public static void checkOptimizationLevel(int optimizationLevel) {
      if (!isValidOptimizationLevel(optimizationLevel)) {
         throw new IllegalArgumentException("Optimization level outside [-1..9]: " + optimizationLevel);
      }
   }

   public final int getMaximumInterpreterStackDepth() {
      return this.maximumInterpreterStackDepth;
   }

   public final void setMaximumInterpreterStackDepth(int max) {
      if (this.sealed) {
         onSealedMutation();
      }

      if (this.optimizationLevel != -1) {
         throw new IllegalStateException("Cannot set maximumInterpreterStackDepth when optimizationLevel != -1");
      } else if (max < 1) {
         throw new IllegalArgumentException("Cannot set maximumInterpreterStackDepth to less than 1");
      } else {
         this.maximumInterpreterStackDepth = max;
      }
   }

   public final void setSecurityController(SecurityController controller) {
      if (this.sealed) {
         onSealedMutation();
      }

      if (controller == null) {
         throw new IllegalArgumentException();
      } else if (this.securityController != null) {
         throw new SecurityException("Can not overwrite existing SecurityController object");
      } else if (SecurityController.hasGlobal()) {
         throw new SecurityException("Can not overwrite existing global SecurityController object");
      } else {
         this.securityController = controller;
      }
   }

   public final synchronized void setClassShutter(ClassShutter shutter) {
      if (this.sealed) {
         onSealedMutation();
      }

      if (shutter == null) {
         throw new IllegalArgumentException();
      } else if (this.hasClassShutter) {
         throw new SecurityException("Cannot overwrite existing ClassShutter object");
      } else {
         this.classShutter = shutter;
         this.hasClassShutter = true;
      }
   }

   final synchronized ClassShutter getClassShutter() {
      return this.classShutter;
   }

   public final synchronized ClassShutterSetter getClassShutterSetter() {
      if (this.hasClassShutter) {
         return null;
      } else {
         this.hasClassShutter = true;
         return new ClassShutterSetter() {
            public void setClassShutter(ClassShutter shutter) {
               Context.this.classShutter = shutter;
            }

            public ClassShutter getClassShutter() {
               return Context.this.classShutter;
            }
         };
      }
   }

   public final Object getThreadLocal(Object key) {
      return this.threadLocalMap == null ? null : this.threadLocalMap.get(key);
   }

   public final synchronized void putThreadLocal(Object key, Object value) {
      if (this.sealed) {
         onSealedMutation();
      }

      if (this.threadLocalMap == null) {
         this.threadLocalMap = new HashMap();
      }

      this.threadLocalMap.put(key, value);
   }

   public final void removeThreadLocal(Object key) {
      if (this.sealed) {
         onSealedMutation();
      }

      if (this.threadLocalMap != null) {
         this.threadLocalMap.remove(key);
      }
   }

   /** @deprecated */
   public static void setCachingEnabled(boolean cachingEnabled) {
   }

   public final void setWrapFactory(WrapFactory wrapFactory) {
      if (this.sealed) {
         onSealedMutation();
      }

      if (wrapFactory == null) {
         throw new IllegalArgumentException();
      } else {
         this.wrapFactory = wrapFactory;
      }
   }

   public final WrapFactory getWrapFactory() {
      if (this.wrapFactory == null) {
         this.wrapFactory = new WrapFactory();
      }

      return this.wrapFactory;
   }

   public final Debugger getDebugger() {
      return this.debugger;
   }

   public final Object getDebuggerContextData() {
      return this.debuggerData;
   }

   public final void setDebugger(Debugger debugger, Object contextData) {
      if (this.sealed) {
         onSealedMutation();
      }

      this.debugger = debugger;
      this.debuggerData = contextData;
   }

   public static DebuggableScript getDebuggableView(Script script) {
      return script instanceof NativeFunction ? ((NativeFunction)script).getDebuggableView() : null;
   }

   public boolean hasFeature(int featureIndex) {
      ContextFactory f = this.getFactory();
      return f.hasFeature(this, featureIndex);
   }

   public XMLLib.Factory getE4xImplementationFactory() {
      return this.getFactory().getE4xImplementationFactory();
   }

   public final int getInstructionObserverThreshold() {
      return this.instructionThreshold;
   }

   public final void setInstructionObserverThreshold(int threshold) {
      if (this.sealed) {
         onSealedMutation();
      }

      if (threshold < 0) {
         throw new IllegalArgumentException();
      } else {
         this.instructionThreshold = threshold;
         this.setGenerateObserverCount(threshold > 0);
      }
   }

   public void setGenerateObserverCount(boolean generateObserverCount) {
      this.generateObserverCount = generateObserverCount;
   }

   protected void observeInstructionCount(int instructionCount) {
      ContextFactory f = this.getFactory();
      f.observeInstructionCount(this, instructionCount);
   }

   public GeneratedClassLoader createClassLoader(ClassLoader parent) {
      ContextFactory f = this.getFactory();
      return f.createClassLoader(parent);
   }

   public final ClassLoader getApplicationClassLoader() {
      if (this.applicationClassLoader == null) {
         ContextFactory f = this.getFactory();
         ClassLoader loader = f.getApplicationClassLoader();
         if (loader == null) {
            ClassLoader threadLoader = VMBridge.instance.getCurrentThreadClassLoader();
            if (threadLoader != null && Kit.testIfCanLoadRhinoClasses(threadLoader)) {
               return threadLoader;
            }

            Class<?> fClass = f.getClass();
            if (fClass != ScriptRuntime.ContextFactoryClass) {
               loader = fClass.getClassLoader();
            } else {
               loader = this.getClass().getClassLoader();
            }
         }

         this.applicationClassLoader = loader;
      }

      return this.applicationClassLoader;
   }

   public final void setApplicationClassLoader(ClassLoader loader) {
      if (this.sealed) {
         onSealedMutation();
      }

      if (loader == null) {
         this.applicationClassLoader = null;
      } else if (!Kit.testIfCanLoadRhinoClasses(loader)) {
         throw new IllegalArgumentException("Loader can not resolve Rhino classes");
      } else {
         this.applicationClassLoader = loader;
      }
   }

   static Context getContext() {
      Context cx = getCurrentContext();
      if (cx == null) {
         throw new RuntimeException("No Context associated with current Thread");
      } else {
         return cx;
      }
   }

   private Object compileImpl(Scriptable scope, Reader sourceReader, String sourceString, String sourceName, int lineno, Object securityDomain, boolean returnFunction, Evaluator compiler, ErrorReporter compilationErrorReporter) throws IOException {
      if (sourceName == null) {
         sourceName = "unnamed script";
      }

      if (securityDomain != null && this.getSecurityController() == null) {
         throw new IllegalArgumentException("securityDomain should be null if setSecurityController() was never called");
      } else {
         if (!(sourceReader == null ^ sourceString == null)) {
            Kit.codeBug();
         }

         if (!(scope == null ^ returnFunction)) {
            Kit.codeBug();
         }

         CompilerEnvirons compilerEnv = new CompilerEnvirons();
         compilerEnv.initFromContext(this);
         if (compilationErrorReporter == null) {
            compilationErrorReporter = compilerEnv.getErrorReporter();
         }

         if (this.debugger != null && sourceReader != null) {
            sourceString = Kit.readReader(sourceReader);
            sourceReader = null;
         }

         Parser p = new Parser(compilerEnv, compilationErrorReporter);
         if (returnFunction) {
            p.calledByCompileFunction = true;
         }

         AstRoot ast;
         if (sourceString != null) {
            ast = p.parse(sourceString, sourceName, lineno);
         } else {
            ast = p.parse(sourceReader, sourceName, lineno);
         }

         if (!returnFunction || ast.getFirstChild() != null && ast.getFirstChild().getType() == 109) {
            IRFactory irf = new IRFactory(compilerEnv, compilationErrorReporter);
            ScriptNode tree = irf.transformTree(ast);
            Parser var17 = null;
            ast = null;
            IRFactory var19 = null;
            if (compiler == null) {
               compiler = this.createCompiler();
            }

            Object bytecode = compiler.compile(compilerEnv, tree, tree.getEncodedSource(), returnFunction);
            if (this.debugger != null) {
               if (sourceString == null) {
                  Kit.codeBug();
               }

               if (!(bytecode instanceof DebuggableScript)) {
                  throw new RuntimeException("NOT SUPPORTED");
               }

               DebuggableScript dscript = (DebuggableScript)bytecode;
               notifyDebugger_r(this, dscript, sourceString);
            }

            Object result;
            if (returnFunction) {
               result = compiler.createFunctionObject(this, scope, bytecode, securityDomain);
            } else {
               result = compiler.createScriptObject(bytecode, securityDomain);
            }

            return result;
         } else {
            throw new IllegalArgumentException("compileFunction only accepts source with single JS function: " + sourceString);
         }
      }
   }

   private static void notifyDebugger_r(Context cx, DebuggableScript dscript, String debugSource) {
      cx.debugger.handleCompilationDone(cx, dscript, debugSource);

      for(int i = 0; i != dscript.getFunctionCount(); ++i) {
         notifyDebugger_r(cx, dscript.getFunction(i), debugSource);
      }

   }

   private Evaluator createCompiler() {
      Evaluator result = null;
      if (this.optimizationLevel >= 0 && codegenClass != null) {
         result = (Evaluator)Kit.newInstanceOrNull(codegenClass);
      }

      if (result == null) {
         result = createInterpreter();
      }

      return result;
   }

   static Evaluator createInterpreter() {
      return (Evaluator)Kit.newInstanceOrNull(interpreterClass);
   }

   static String getSourcePositionFromStack(int[] linep) {
      Context cx = getCurrentContext();
      if (cx == null) {
         return null;
      } else {
         if (cx.lastInterpreterFrame != null) {
            Evaluator evaluator = createInterpreter();
            if (evaluator != null) {
               return evaluator.getSourcePositionFromStack(cx, linep);
            }
         }

         CharArrayWriter writer = new CharArrayWriter();
         RuntimeException re = new RuntimeException();
         re.printStackTrace(new PrintWriter(writer));
         String s = writer.toString();
         int open = -1;
         int close = -1;
         int colon = -1;

         for(int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c == ':') {
               colon = i;
            } else if (c == '(') {
               open = i;
            } else if (c == ')') {
               close = i;
            } else if (c == '\n' && open != -1 && close != -1 && colon != -1 && open < colon && colon < close) {
               String fileStr = s.substring(open + 1, colon);
               if (!fileStr.endsWith(".java")) {
                  String lineStr = s.substring(colon + 1, close);

                  try {
                     linep[0] = Integer.parseInt(lineStr);
                     if (linep[0] < 0) {
                        linep[0] = 0;
                     }

                     return fileStr;
                  } catch (NumberFormatException var13) {
                  }
               }

               colon = -1;
               close = -1;
               open = -1;
            }
         }

         return null;
      }
   }

   RegExpProxy getRegExpProxy() {
      if (this.regExpProxy == null) {
         Class<?> cl = Kit.classOrNull("org.mozilla.javascript.regexp.RegExpImpl");
         if (cl != null) {
            this.regExpProxy = (RegExpProxy)Kit.newInstanceOrNull(cl);
         }
      }

      return this.regExpProxy;
   }

   final boolean isVersionECMA1() {
      return this.version == 0 || this.version >= 130;
   }

   SecurityController getSecurityController() {
      SecurityController global = SecurityController.global();
      return global != null ? global : this.securityController;
   }

   public final boolean isGeneratingDebugChanged() {
      return this.generatingDebugChanged;
   }

   public void addActivationName(String name) {
      if (this.sealed) {
         onSealedMutation();
      }

      if (this.activationNames == null) {
         this.activationNames = new HashSet();
      }

      this.activationNames.add(name);
   }

   public final boolean isActivationNeeded(String name) {
      return this.activationNames != null && this.activationNames.contains(name);
   }

   public void removeActivationName(String name) {
      if (this.sealed) {
         onSealedMutation();
      }

      if (this.activationNames != null) {
         this.activationNames.remove(name);
      }

   }

   static {
      emptyArgs = ScriptRuntime.emptyArgs;
      codegenClass = Kit.classOrNull("org.mozilla.javascript.optimizer.Codegen");
      interpreterClass = Kit.classOrNull("org.mozilla.javascript.Interpreter");
   }

   public interface ClassShutterSetter {
      void setClassShutter(ClassShutter var1);

      ClassShutter getClassShutter();
   }
}
