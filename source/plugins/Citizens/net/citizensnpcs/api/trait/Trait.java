package net.citizensnpcs.api.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.event.Listener;

public abstract class Trait implements Listener, Runnable {
   private final String name;
   protected NPC npc = null;
   private boolean runImplemented = true;

   protected Trait(String name) {
      super();
      this.name = name;
   }

   public final String getName() {
      return this.name;
   }

   public final NPC getNPC() {
      return this.npc;
   }

   public boolean isRunImplemented() {
      this.run();
      return this.runImplemented;
   }

   public void linkToNPC(NPC npc) {
      if (this.npc != null) {
         throw new IllegalArgumentException("npc may only be set once");
      } else {
         this.npc = npc;
         this.onAttach();
      }
   }

   public void load(DataKey key) throws NPCLoadException {
   }

   public void onAttach() {
   }

   public void onCopy() {
   }

   public void onDespawn() {
   }

   public void onRemove() {
   }

   public void onSpawn() {
   }

   public void run() {
      this.runImplemented = false;
   }

   public void save(DataKey key) {
   }
}
