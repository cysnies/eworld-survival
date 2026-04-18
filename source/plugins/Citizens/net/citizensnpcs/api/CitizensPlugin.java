package net.citizensnpcs.api;

import java.io.File;
import net.citizensnpcs.api.ai.speech.SpeechFactory;
import net.citizensnpcs.api.npc.NPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.npc.NPCSelector;
import net.citizensnpcs.api.trait.TraitFactory;
import org.bukkit.plugin.Plugin;

public interface CitizensPlugin extends Plugin {
   NPCRegistry createAnonymousNPCRegistry(NPCDataStore var1);

   NPCRegistry createNamedNPCRegistry(String var1, NPCDataStore var2);

   NPCSelector getDefaultNPCSelector();

   NPCRegistry getNamedNPCRegistry(String var1);

   NPCRegistry getNPCRegistry();

   ClassLoader getOwningClassLoader();

   File getScriptFolder();

   SpeechFactory getSpeechFactory();

   TraitFactory getTraitFactory();

   void onImplementationChanged();

   void removeNamedNPCRegistry(String var1);
}
