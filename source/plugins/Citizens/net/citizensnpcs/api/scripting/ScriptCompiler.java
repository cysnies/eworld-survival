package net.citizensnpcs.api.scripting;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;
import net.citizensnpcs.api.util.Messaging;

public class ScriptCompiler {
   private final WeakReference classLoader;
   private final ScriptEngineManager engineManager;
   private final Map engines = Maps.newHashMap();
   private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
      int n = 1;

      public Thread newThread(Runnable r) {
         Thread created = new Thread(r, "Citizens Script Compiler #" + this.n++);
         return created;
      }
   });
   private final Function fileEngineConverter = new Function() {
      public ScriptSource apply(File file) {
         if (!file.isFile()) {
            return null;
         } else {
            String fileName = file.getName();
            String extension = fileName.substring(fileName.lastIndexOf(46) + 1);
            ScriptEngine engine = ScriptCompiler.this.loadEngine(extension);
            return engine == null ? null : new ScriptSource(file, engine);
         }
      }
   };
   private final List globalContextProviders = Lists.newArrayList();
   private static final Map CACHE = (new MapMaker()).weakValues().makeMap();
   private static boolean CLASSLOADER_OVERRIDE_ENABLED;
   private static Method GET_APPLICATION_CLASS_LOADER;
   private static Method GET_GLOBAL;
   private static Method INIT_APPLICATION_CLASS_LOADER;

   public ScriptCompiler(ClassLoader overrideClassLoader) {
      super();
      this.engineManager = new ScriptEngineManager(overrideClassLoader);
      this.classLoader = new WeakReference(overrideClassLoader);
   }

   public CompileTaskBuilder compile(File file) {
      if (file == null) {
         throw new IllegalArgumentException("file should not be null");
      } else {
         ScriptSource source = (ScriptSource)this.fileEngineConverter.apply(file);
         if (source == null) {
            throw new IllegalArgumentException("could not recognise file");
         } else {
            return new CompileTaskBuilder(source);
         }
      }
   }

   public CompileTaskBuilder compile(String src, String identifier, String extension) {
      if (src == null) {
         throw new IllegalArgumentException("source must not be null");
      } else {
         return new CompileTaskBuilder(new ScriptSource(src, identifier, this.loadEngine(extension)));
      }
   }

   public void interrupt() {
      this.executor.shutdownNow();
   }

   private ScriptEngine loadEngine(String extension) {
      ScriptEngine engine = (ScriptEngine)this.engines.get(extension);
      if (engine != null) {
         return engine;
      } else {
         ScriptEngine search = this.engineManager.getEngineByExtension(extension);
         if (search != null && (!(search instanceof Compilable) || !(search instanceof Invocable))) {
            search = null;
         }

         this.engines.put(extension, search);
         ClassLoader cl = (ClassLoader)this.classLoader.get();
         if (cl != null) {
            this.updateSunClassLoader(cl);
         }

         return search;
      }
   }

   public void registerGlobalContextProvider(ContextProvider provider) {
      if (!this.globalContextProviders.contains(provider)) {
         this.globalContextProviders.add(provider);
      }

   }

   public void run(String code, String extension) throws ScriptException {
      this.run(code, extension, (Map)null);
   }

   public void run(String code, String extension, Map vars) throws ScriptException {
      ScriptEngine engine = this.loadEngine(extension);
      if (engine == null) {
         throw new ScriptException("Couldn't load engine with extension " + extension);
      } else {
         ScriptContext context = new SimpleScriptContext();
         if (vars != null) {
            context.setBindings(new SimpleBindings(vars), 100);
         }

         engine.eval(extension, context);
      }
   }

   private void updateSunClassLoader(ClassLoader cl) {
      if (CLASSLOADER_OVERRIDE_ENABLED) {
         try {
            Object global = GET_GLOBAL.invoke((Object)null);
            if (GET_APPLICATION_CLASS_LOADER.invoke(global) == null) {
               INIT_APPLICATION_CLASS_LOADER.invoke(global, cl);
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

   }

   public static void main(String[] args) {
   }

   static {
      try {
         Class<?> CONTEXT_FACTORY = Class.forName("sun.org.mozilla.javascript.internal.ContextFactory");
         GET_APPLICATION_CLASS_LOADER = CONTEXT_FACTORY.getDeclaredMethod("getApplicationClassLoader");
         GET_APPLICATION_CLASS_LOADER.setAccessible(true);
         GET_GLOBAL = CONTEXT_FACTORY.getDeclaredMethod("getGlobal");
         GET_GLOBAL.setAccessible(true);
         INIT_APPLICATION_CLASS_LOADER = CONTEXT_FACTORY.getDeclaredMethod("initApplicationClassLoader", ClassLoader.class);
         INIT_APPLICATION_CLASS_LOADER.setAccessible(true);
         CLASSLOADER_OVERRIDE_ENABLED = true;
      } catch (Exception var1) {
         Messaging.severe("Unable to load Rhino script classes - javascript scripts will only be able to access CraftBukkit");
      }

   }

   private class CompileTask implements Callable {
      private final boolean cache;
      private final CompileCallback[] callbacks;
      private final ContextProvider[] contextProviders;
      private final ScriptSource engine;

      public CompileTask(CompileTaskBuilder builder) {
         super();
         List<ContextProvider> copy = Lists.newArrayList(builder.contextProviders);
         copy.addAll(ScriptCompiler.this.globalContextProviders);
         this.contextProviders = (ContextProvider[])copy.toArray(new ContextProvider[copy.size()]);
         this.callbacks = (CompileCallback[])builder.callbacks.toArray(new CompileCallback[builder.callbacks.size()]);
         this.engine = builder.engine;
         this.cache = builder.cache;
      }

      public ScriptFactory call() {
         if (this.cache && ScriptCompiler.CACHE.containsKey(this.engine.getIdentifier())) {
            return (ScriptFactory)ScriptCompiler.CACHE.get(this.engine.getIdentifier());
         } else {
            Compilable compiler = (Compilable)this.engine.engine;
            Reader reader = null;

            try {
               CompiledScript src = compiler.compile(reader = this.engine.getReader());
               ScriptFactory compiled = new SimpleScriptFactory(src, this.contextProviders);
               if (this.cache) {
                  ScriptCompiler.CACHE.put(this.engine.getIdentifier(), compiled);
               }

               for(CompileCallback callback : this.callbacks) {
                  callback.onScriptCompiled(this.engine.getIdentifier(), compiled);
               }

               Object var18 = compiled;
               return (ScriptFactory)var18;
            } catch (IOException e) {
               Messaging.severe("IO error while reading a file for scripting.");
               e.printStackTrace();
            } catch (ScriptException e) {
               Messaging.severe("Compile error while parsing script.");
               Throwables.getRootCause(e).printStackTrace();
            } catch (Throwable t) {
               Messaging.severe("Unexpected error while parsing script.");
               t.printStackTrace();
            } finally {
               Closeables.closeQuietly(reader);
            }

            return null;
         }
      }
   }

   public class CompileTaskBuilder {
      private boolean cache;
      private final List callbacks;
      private final List contextProviders;
      private final ScriptSource engine;

      private CompileTaskBuilder(ScriptSource engine) {
         super();
         this.callbacks = Lists.newArrayList();
         this.contextProviders = Lists.newArrayList();
         this.engine = engine;
      }

      public Future beginWithFuture() {
         CompileTask task = ScriptCompiler.this.new CompileTask(this);
         return ScriptCompiler.this.executor.submit(task);
      }

      public CompileTaskBuilder cache(boolean cache) {
         this.cache = cache;
         return this;
      }

      public CompileTaskBuilder withCallback(CompileCallback callback) {
         this.callbacks.add(callback);
         return this;
      }

      public CompileTaskBuilder withContextProvider(ContextProvider provider) {
         this.contextProviders.add(provider);
         return this;
      }
   }

   private static class ScriptSource {
      private final ScriptEngine engine;
      private final File file;
      private final String identifier;
      private final String src;

      private ScriptSource(File file, ScriptEngine engine) {
         super();
         this.file = file;
         this.identifier = file.getAbsolutePath();
         this.engine = engine;
         this.src = null;
      }

      private ScriptSource(String src, String identifier, ScriptEngine engine) {
         super();
         this.src = src;
         this.identifier = identifier;
         this.engine = engine;
         this.file = null;
      }

      public String getIdentifier() {
         return this.identifier;
      }

      public Reader getReader() throws FileNotFoundException {
         return (Reader)(this.file == null ? new StringReader(this.src) : new FileReader(this.file));
      }
   }
}
