package net.citizensnpcs.api.util;

import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class Translator {
   private final Locale defaultLocale;
   private final Map messageFormatCache = Maps.newHashMap();
   private ResourceBundle preferredBundle;
   private final File resourceFile;
   private static ResourceBundle defaultBundle;
   private static Translator instance;
   public static final String PREFIX = "messages";

   private Translator(File resourceFile, Locale locale) {
      super();
      this.resourceFile = resourceFile;
      this.defaultLocale = locale;

      try {
         this.preferredBundle = ResourceBundle.getBundle("messages", this.defaultLocale, new FileClassLoader(Translator.class.getClassLoader(), resourceFile));
      } catch (MissingResourceException var4) {
         this.preferredBundle = this.getDefaultBundle();
         Messaging.severe("Missing preferred location bundle.");
      }

   }

   private String format(String key, Locale locale, Object... msg) {
      String unreplaced = this.translate(key, locale);
      MessageFormat formatter = this.getFormatter(unreplaced);
      return formatter.format(msg);
   }

   private ResourceBundle getBundle(Locale locale) {
      try {
         ResourceBundle bundle = ResourceBundle.getBundle("messages", locale, new FileClassLoader(Translator.class.getClassLoader(), this.resourceFile));
         return bundle == null ? this.preferredBundle : bundle;
      } catch (MissingResourceException var3) {
         return this.preferredBundle;
      }
   }

   private ResourceBundle getDefaultBundle() {
      return getDefaultResourceBundle(this.resourceFile, "messages_en.properties");
   }

   private MessageFormat getFormatter(String unreplaced) {
      MessageFormat formatter = (MessageFormat)this.messageFormatCache.get(unreplaced);
      if (formatter == null) {
         this.messageFormatCache.put(unreplaced, formatter = new MessageFormat(unreplaced));
      }

      return formatter;
   }

   private String translate(String key, Locale locale) {
      ResourceBundle bundle = this.preferredBundle;
      if (locale != this.defaultLocale) {
         bundle = this.getBundle(locale);
      }

      try {
         return bundle.getString(key);
      } catch (MissingResourceException var7) {
         try {
            return this.getDefaultBundle().getString(key);
         } catch (MissingResourceException var6) {
            return "?" + key + "?";
         }
      }
   }

   private static void addTranslation(TranslationProvider from, File to) {
      Properties props = new Properties();
      InputStream in = from.createInputStream();

      try {
         props.load(in);
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         Closeables.closeQuietly(in);
      }

      if (to.exists()) {
         try {
            props.load(in = new FileInputStream(to));
         } catch (FileNotFoundException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         } finally {
            Closeables.closeQuietly(in);
         }
      }

      OutputStream out = null;

      try {
         props.store(out = new FileOutputStream(to), "");
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         Closeables.closeQuietly(out);
      }

   }

   public static void addTranslations(Collection providers) {
      for(TranslationProvider provider : providers) {
         addTranslation(provider, new File(instance.resourceFile, provider.getName()));
      }

      defaultBundle = null;
      setInstance(instance.resourceFile, instance.preferredBundle.getLocale());
   }

   public static void addTranslations(TranslationProvider... providers) {
      addTranslations((Collection)Arrays.asList(providers));
   }

   private static Properties getDefaultBundleProperties() {
      Properties defaults = new Properties();
      InputStream in = null;

      try {
         in = Translator.class.getResourceAsStream("/messages_en.properties");
         defaults.load(in);
      } catch (IOException var6) {
      } finally {
         Closeables.closeQuietly(in);
      }

      return defaults;
   }

   private static ResourceBundle getDefaultResourceBundle(File resourceDirectory, String fileName) {
      if (defaultBundle != null) {
         return defaultBundle;
      } else {
         resourceDirectory.mkdirs();
         File bundleFile = new File(resourceDirectory, fileName);
         if (!bundleFile.exists()) {
            try {
               bundleFile.createNewFile();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }

         populateDefaults(bundleFile);
         FileInputStream stream = null;

         try {
            stream = new FileInputStream(bundleFile);
            defaultBundle = new PropertyResourceBundle(stream);
         } catch (Exception e) {
            e.printStackTrace();
            defaultBundle = getFallbackResourceBundle();
         } finally {
            Closeables.closeQuietly(stream);
         }

         return defaultBundle;
      }
   }

   private static ResourceBundle getFallbackResourceBundle() {
      return new ListResourceBundle() {
         protected Object[][] getContents() {
            return new Object[0][0];
         }
      };
   }

   private static void populateDefaults(File bundleFile) {
      Properties properties = new Properties();
      InputStream in = null;

      try {
         in = new FileInputStream(bundleFile);
         properties.load(in);
      } catch (IOException var17) {
      } finally {
         Closeables.closeQuietly(in);
      }

      Properties defaults = getDefaultBundleProperties();

      for(Map.Entry entry : defaults.entrySet()) {
         if (!properties.containsKey(entry.getKey())) {
            properties.put(entry.getKey(), entry.getValue());
         }
      }

      OutputStream stream = null;

      try {
         stream = new FileOutputStream(bundleFile);
         properties.store(stream, "");
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         Closeables.closeQuietly(stream);
      }

   }

   public static void setInstance(File dataFolder, Locale preferredLocale) {
      instance = new Translator(dataFolder, preferredLocale);
   }

   public static String translate(String key, Locale preferredLocale, Object... msg) {
      return Colorizer.parseColors(msg.length == 0 ? instance.translate(key, preferredLocale) : instance.format(key, preferredLocale, msg));
   }

   public static String translate(String key, Object... msg) {
      return translate(key, instance.defaultLocale, msg);
   }

   private static class FileClassLoader extends ClassLoader {
      private final File folder;

      public FileClassLoader(ClassLoader classLoader, File folder) {
         super(classLoader);
         this.folder = folder;
      }

      public URL getResource(String string) {
         File file = new File(this.folder, string);
         if (file.exists()) {
            try {
               return file.toURI().toURL();
            } catch (MalformedURLException var4) {
            }
         } else {
            string = string.replaceFirst("/", "");
            URL test = Translator.class.getResource('/' + string);
            if (test != null) {
               return test;
            }
         }

         return super.getResource(string);
      }

      public InputStream getResourceAsStream(String string) {
         File file = new File(this.folder, string);
         if (file.exists()) {
            try {
               return new FileInputStream(file);
            } catch (FileNotFoundException var4) {
            }
         } else {
            string = string.replaceFirst("/", "");
            InputStream stream = Translator.class.getResourceAsStream('/' + string);
            if (stream != null) {
               (new Thread(new SaveResource(this.folder, string))).start();
               return stream;
            }
         }

         return super.getResourceAsStream(string);
      }
   }

   private static class SaveResource implements Runnable {
      private final String fileName;
      private final File rootFolder;

      private SaveResource(File rootFolder, String fileName) {
         super();
         this.rootFolder = rootFolder;
         this.fileName = fileName;
      }

      public void run() {
         File file = new File(this.rootFolder, this.fileName);
         if (!file.exists()) {
            final InputStream stream = Translator.class.getResourceAsStream('/' + this.fileName);
            if (stream != null) {
               InputSupplier<InputStream> in = new InputSupplier() {
                  public InputStream getInput() throws IOException {
                     return stream;
                  }
               };

               try {
                  this.rootFolder.mkdirs();
                  File to = File.createTempFile(this.fileName, (String)null, this.rootFolder);
                  to.deleteOnExit();
                  Files.copy(in, to);
                  if (!file.exists()) {
                     to.renameTo(file);
                  }
               } catch (IOException e) {
                  e.printStackTrace();
               }

            }
         }
      }
   }

   public interface TranslationProvider {
      InputStream createInputStream();

      String getName();
   }
}
