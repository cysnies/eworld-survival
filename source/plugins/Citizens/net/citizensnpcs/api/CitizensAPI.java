package net.citizensnpcs.api;

import java.io.File;
import net.citizensnpcs.api.ai.speech.SpeechFactory;
import net.citizensnpcs.api.npc.NPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.npc.NPCSelector;
import net.citizensnpcs.api.scripting.ScriptCompiler;
import net.citizensnpcs.api.trait.TraitFactory;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public final class CitizensAPI {
   private CitizensPlugin implementation;
   private static final CitizensAPI instance = new CitizensAPI();
   private static ScriptCompiler scriptCompiler;

   private CitizensAPI() {
      super();
   }

   public static NPCRegistry createAnonymousNPCRegistry(NPCDataStore store) {
      return getImplementation().createAnonymousNPCRegistry(store);
   }

   public static NPCRegistry createNamedNPCRegistry(String name, NPCDataStore store) {
      return getImplementation().createNamedNPCRegistry(name, store);
   }

   public static File getDataFolder() {
      return getImplementation().getDataFolder();
   }

   public static NPCSelector getDefaultNPCSelector() {
      return getImplementation().getDefaultNPCSelector();
   }

   private static CitizensPlugin getImplementation() {
      if (instance.implementation == null) {
         throw new IllegalStateException("no implementation set");
      } else {
         return instance.implementation;
      }
   }

   private static ClassLoader getImplementationClassLoader() {
      return getImplementation().getOwningClassLoader();
   }

   public static NPCRegistry getNamedNPCRegistry(String name) {
      return getImplementation().getNamedNPCRegistry(name);
   }

   public static NPCRegistry getNPCRegistry() {
      return getImplementation().getNPCRegistry();
   }

   public static Plugin getPlugin() {
      return getImplementation();
   }

   public static ScriptCompiler getScriptCompiler() {
      if (scriptCompiler == null && getImplementation() != null) {
         scriptCompiler = new ScriptCompiler(getImplementationClassLoader());
      }

      return scriptCompiler;
   }

   public static File getScriptFolder() {
      return getImplementation().getScriptFolder();
   }

   public static SpeechFactory getSpeechFactory() {
      return getImplementation().getSpeechFactory();
   }

   public static TraitFactory getTraitFactory() {
      return getImplementation().getTraitFactory();
   }

   public static boolean hasImplementation() {
      return instance.implementation != null;
   }

   public static void registerEvents(Listener listener) {
      if (Bukkit.getServer() != null && getPlugin() != null) {
         Bukkit.getPluginManager().registerEvents(listener, getPlugin());
      }

   }

   public static void removeNamedNPCRegistry(String name) {
      getImplementation().removeNamedNPCRegistry(name);
   }

   public static void setImplementation(CitizensPlugin implementation) {
      if (implementation != null && hasImplementation()) {
         getImplementation().onImplementationChanged();
      }

      instance.implementation = implementation;
   }

   public static void shutdown() {
      if (scriptCompiler != null) {
         instance.implementation = null;
         scriptCompiler.interrupt();
         scriptCompiler = null;
      }
   }
}
