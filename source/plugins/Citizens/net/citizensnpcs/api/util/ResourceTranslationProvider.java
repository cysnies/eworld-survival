package net.citizensnpcs.api.util;

import java.io.InputStream;

public class ResourceTranslationProvider implements Translator.TranslationProvider {
   private final Class clazz;
   private final String name;

   public ResourceTranslationProvider(String name, Class clazz) {
      super();
      this.name = name;
      this.clazz = clazz;
   }

   public InputStream createInputStream() {
      return this.clazz.getResourceAsStream('/' + this.name);
   }

   public String getName() {
      return this.name;
   }
}
