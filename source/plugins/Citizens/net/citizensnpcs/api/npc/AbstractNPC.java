package net.citizensnpcs.api.npc;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.GoalController;
import net.citizensnpcs.api.ai.SimpleGoalController;
import net.citizensnpcs.api.ai.speech.SimpleSpeechController;
import net.citizensnpcs.api.ai.speech.SpeechController;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCAddTraitEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCRemoveTraitEvent;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Speech;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.MemoryDataKey;
import net.citizensnpcs.api.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.metadata.FixedMetadataValue;

public abstract class AbstractNPC implements NPC {
   private final GoalController goalController = new SimpleGoalController();
   private final int id;
   private final MetadataStore metadata = new SimpleMetadataStore() {
      public void remove(String key) {
         super.remove(key);
         if (AbstractNPC.this.getBukkitEntity() != null) {
            AbstractNPC.this.getBukkitEntity().removeMetadata(key, CitizensAPI.getPlugin());
         }

      }

      public void set(String key, Object data) {
         super.set(key, data);
         if (AbstractNPC.this.getBukkitEntity() != null) {
            AbstractNPC.this.getBukkitEntity().setMetadata(key, new FixedMetadataValue(CitizensAPI.getPlugin(), data));
         }

      }

      public void setPersistent(String key, Object data) {
         super.setPersistent(key, data);
         if (AbstractNPC.this.getBukkitEntity() != null) {
            AbstractNPC.this.getBukkitEntity().setMetadata(key, new FixedMetadataValue(CitizensAPI.getPlugin(), data));
         }

      }
   };
   private String name;
   private final List removedTraits = Lists.newArrayList();
   private final List runnables = Lists.newArrayList();
   private final SpeechController speechController = new SimpleSpeechController(this);
   protected final Map traits = Maps.newHashMap();

   protected AbstractNPC(int id, String name) {
      super();
      if (name.length() > 16) {
         Messaging.severe("ID", id, "created with name length greater than 16, truncating", name, "to", name.substring(0, 15));
         name = name.substring(0, 15);
      }

      this.id = id;
      this.name = name;
      CitizensAPI.getTraitFactory().addDefaultTraits(this);
   }

   public void addTrait(Class clazz) {
      this.addTrait(this.getTraitFor(clazz));
   }

   public void addTrait(Trait trait) {
      if (trait == null) {
         Messaging.severe("Cannot register a null trait. Was it registered properly?");
      } else {
         if (trait.getNPC() == null) {
            trait.linkToNPC(this);
         }

         Trait replaced = (Trait)this.traits.get(trait.getClass());
         Bukkit.getPluginManager().registerEvents(trait, CitizensAPI.getPlugin());
         this.traits.put(trait.getClass(), trait);
         if (this.isSpawned()) {
            trait.onSpawn();
         }

         if (trait.isRunImplemented()) {
            if (replaced != null) {
               this.runnables.remove(replaced);
            }

            this.runnables.add(trait);
         }

         Bukkit.getPluginManager().callEvent(new NPCAddTraitEvent(this, trait));
      }
   }

   public NPC clone() {
      NPC copy = CitizensAPI.getNPCRegistry().createNPC(((MobType)this.getTrait(MobType.class)).getType(), this.getFullName());
      DataKey key = new MemoryDataKey();
      this.save(key);
      copy.load(key);

      for(Trait trait : copy.getTraits()) {
         trait.onCopy();
      }

      return copy;
   }

   public MetadataStore data() {
      return this.metadata;
   }

   public boolean despawn() {
      return this.despawn(DespawnReason.PLUGIN);
   }

   public void destroy() {
      Bukkit.getPluginManager().callEvent(new NPCRemoveEvent(this));
      this.runnables.clear();

      for(Trait trait : this.traits.values()) {
         HandlerList.unregisterAll(trait);
         trait.onRemove();
      }

      this.traits.clear();
      CitizensAPI.getNPCRegistry().deregister(this);
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         AbstractNPC other = (AbstractNPC)obj;
         if (this.id != other.id) {
            return false;
         } else {
            if (this.name == null) {
               if (other.name != null) {
                  return false;
               }
            } else if (!this.name.equals(other.name)) {
               return false;
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public GoalController getDefaultGoalController() {
      return this.goalController;
   }

   public SpeechController getDefaultSpeechController() {
      if (!this.hasTrait(Speech.class)) {
         this.addTrait(Speech.class);
      }

      return this.speechController;
   }

   public String getFullName() {
      return this.name;
   }

   public int getId() {
      return this.id;
   }

   public String getName() {
      String parsed = this.name;

      for(ChatColor color : ChatColor.values()) {
         if (parsed.contains("<" + color.getChar() + ">")) {
            parsed = parsed.replace("<" + color.getChar() + ">", "");
         }
      }

      return parsed;
   }

   public Trait getTrait(Class clazz) {
      Trait trait = (Trait)this.traits.get(clazz);
      if (trait == null) {
         trait = this.getTraitFor(clazz);
         this.addTrait(trait);
      }

      return trait != null ? (Trait)clazz.cast(trait) : null;
   }

   protected Trait getTraitFor(Class clazz) {
      return CitizensAPI.getTraitFactory().getTrait(clazz);
   }

   public Iterable getTraits() {
      return this.traits.values();
   }

   public int hashCode() {
      int prime = 31;
      return 31 * (31 + this.id) + (this.name == null ? 0 : this.name.hashCode());
   }

   public boolean hasTrait(Class trait) {
      return this.traits.containsKey(trait);
   }

   public boolean isProtected() {
      return (Boolean)this.data().get("protected", true);
   }

   public void load(final DataKey root) {
      this.metadata.loadFrom(root.getRelative("metadata"));
      String traitNames = root.getString("traitnames");
      Set<DataKey> keys = Sets.newHashSet(root.getRelative("traits").getSubKeys());
      Iterables.addAll(keys, Iterables.transform(Splitter.on(',').split(traitNames), new Function() {
         public DataKey apply(@Nullable String input) {
            return root.getRelative("traits." + input);
         }
      }));

      for(DataKey traitKey : keys) {
         if (!traitKey.keyExists("enabled") || traitKey.getBoolean("enabled") || !(traitKey.getRaw("enabled") instanceof Boolean)) {
            Class<? extends Trait> clazz = CitizensAPI.getTraitFactory().getTraitClass(traitKey.name());
            Trait trait;
            if (this.hasTrait(clazz)) {
               trait = this.getTrait(clazz);
            } else {
               trait = CitizensAPI.getTraitFactory().getTrait(clazz);
               if (trait == null) {
                  Messaging.severeTr("citizens.notifications.trait-load-failed", traitKey.name(), this.getId());
                  continue;
               }

               this.addTrait(trait);
            }

            this.loadTrait(trait, traitKey);
         }
      }

   }

   private void loadTrait(Trait trait, DataKey traitKey) {
      try {
         trait.load(traitKey);
         PersistenceLoader.load((Object)trait, traitKey);
      } catch (Throwable var4) {
         Messaging.logTr("citizens.notifications.trait-load-failed", traitKey.name(), this.getId());
      }

   }

   public void removeTrait(Class traitClass) {
      Trait trait = (Trait)this.traits.remove(traitClass);
      if (trait != null) {
         Bukkit.getPluginManager().callEvent(new NPCRemoveTraitEvent(this, trait));
         this.removedTraits.add(trait.getName());
         if (trait.isRunImplemented()) {
            this.runnables.remove(trait);
         }

         HandlerList.unregisterAll(trait);
         trait.onRemove();
      }

   }

   public void save(DataKey root) {
      this.metadata.saveTo(root.getRelative("metadata"));
      root.setString("name", this.getFullName());
      StringBuilder traitNames = new StringBuilder();

      for(Trait trait : this.traits.values()) {
         DataKey traitKey = root.getRelative("traits." + trait.getName());
         trait.save(traitKey);
         PersistenceLoader.save(trait, traitKey);
         this.removedTraits.remove(trait.getName());
         traitNames.append(trait.getName() + ",");
      }

      if (traitNames.length() > 0) {
         root.setString("traitnames", traitNames.substring(0, traitNames.length() - 1));
      } else {
         root.setString("traitnames", "");
      }

      for(String name : this.removedTraits) {
         root.removeKey("traits." + name);
      }

      this.removedTraits.clear();
   }

   public void setName(String name) {
      this.name = name;
      if (this.isSpawned()) {
         LivingEntity bukkitEntity = this.getBukkitEntity();
         bukkitEntity.setCustomName(this.getFullName());
         if (bukkitEntity.getType() == EntityType.PLAYER) {
            Location old = bukkitEntity.getLocation();
            this.despawn(DespawnReason.PENDING_RESPAWN);
            this.spawn(old);
         }

      }
   }

   public void setProtected(boolean isProtected) {
      this.data().setPersistent("protected", isProtected);
   }

   public void update() {
      for(int i = 0; i < this.runnables.size(); ++i) {
         ((Runnable)this.runnables.get(i)).run();
      }

      if (this.isSpawned()) {
         this.goalController.run();
      }

   }
}
