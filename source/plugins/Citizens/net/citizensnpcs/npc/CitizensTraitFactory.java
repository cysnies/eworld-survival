package net.citizensnpcs.npc;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.citizensnpcs.Metrics;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitFactory;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.api.trait.trait.Speech;
import net.citizensnpcs.trait.Age;
import net.citizensnpcs.trait.Anchors;
import net.citizensnpcs.trait.Controllable;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.trait.Gravity;
import net.citizensnpcs.trait.HorseModifiers;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.NPCSkeletonType;
import net.citizensnpcs.trait.OcelotModifiers;
import net.citizensnpcs.trait.Poses;
import net.citizensnpcs.trait.Powered;
import net.citizensnpcs.trait.Saddle;
import net.citizensnpcs.trait.Sheared;
import net.citizensnpcs.trait.SlimeSize;
import net.citizensnpcs.trait.VillagerProfession;
import net.citizensnpcs.trait.WolfModifiers;
import net.citizensnpcs.trait.WoolColor;
import net.citizensnpcs.trait.ZombieModifier;
import net.citizensnpcs.trait.text.Text;
import net.citizensnpcs.trait.waypoint.Waypoints;

public class CitizensTraitFactory implements TraitFactory {
   private final List defaultTraits = Lists.newArrayList();
   private final Map registered = Maps.newHashMap();
   private static final Set INTERNAL_TRAITS = Sets.newHashSet();

   public CitizensTraitFactory() {
      super();
      this.registerTrait(TraitInfo.create(Age.class).withName("age"));
      this.registerTrait(TraitInfo.create(Anchors.class).withName("anchors"));
      this.registerTrait(TraitInfo.create(Controllable.class).withName("controllable"));
      this.registerTrait(TraitInfo.create(Equipment.class).withName("equipment"));
      this.registerTrait(TraitInfo.create(Gravity.class).withName("gravity"));
      this.registerTrait(TraitInfo.create(HorseModifiers.class).withName("horsemodifiers"));
      this.registerTrait(TraitInfo.create(Inventory.class).withName("inventory"));
      this.registerTrait(TraitInfo.create(CurrentLocation.class).withName("location"));
      this.registerTrait(TraitInfo.create(LookClose.class).withName("lookclose"));
      this.registerTrait(TraitInfo.create(OcelotModifiers.class).withName("ocelotmodifiers"));
      this.registerTrait(TraitInfo.create(Owner.class).withName("owner"));
      this.registerTrait(TraitInfo.create(Poses.class).withName("poses"));
      this.registerTrait(TraitInfo.create(Powered.class).withName("powered"));
      this.registerTrait(TraitInfo.create(VillagerProfession.class).withName("profession"));
      this.registerTrait(TraitInfo.create(Saddle.class).withName("saddle"));
      this.registerTrait(TraitInfo.create(Sheared.class).withName("sheared"));
      this.registerTrait(TraitInfo.create(NPCSkeletonType.class).withName("skeletontype"));
      this.registerTrait(TraitInfo.create(SlimeSize.class).withName("slimesize"));
      this.registerTrait(TraitInfo.create(Spawned.class).withName("spawned"));
      this.registerTrait(TraitInfo.create(Speech.class).withName("speech"));
      this.registerTrait(TraitInfo.create(Text.class).withName("text"));
      this.registerTrait(TraitInfo.create(MobType.class).withName("type").asDefaultTrait());
      this.registerTrait(TraitInfo.create(Waypoints.class).withName("waypoints"));
      this.registerTrait(TraitInfo.create(WoolColor.class).withName("woolcolor"));
      this.registerTrait(TraitInfo.create(WolfModifiers.class).withName("wolfmodifiers"));
      this.registerTrait(TraitInfo.create(ZombieModifier.class).withName("zombiemodifier"));

      for(String trait : this.registered.keySet()) {
         INTERNAL_TRAITS.add(trait);
      }

   }

   public void addDefaultTraits(NPC npc) {
      for(TraitInfo info : this.defaultTraits) {
         npc.addTrait(this.create(info));
      }

   }

   public void addPlotters(Metrics.Graph graph) {
      for(Map.Entry entry : this.registered.entrySet()) {
         if (!INTERNAL_TRAITS.contains(entry.getKey())) {
            final Class<? extends Trait> traitClass = ((TraitInfo)entry.getValue()).getTraitClass();
            graph.addPlotter(new Metrics.Plotter((String)entry.getKey()) {
               public int getValue() {
                  int numberUsingTrait = 0;

                  for(NPC npc : CitizensAPI.getNPCRegistry()) {
                     if (npc.hasTrait(traitClass)) {
                        ++numberUsingTrait;
                     }
                  }

                  return numberUsingTrait;
               }
            });
         }
      }

   }

   private Trait create(TraitInfo info) {
      return info.tryCreateInstance();
   }

   public Trait getTrait(Class clazz) {
      for(TraitInfo entry : this.registered.values()) {
         if (clazz == entry.getTraitClass()) {
            return this.create(entry);
         }
      }

      return null;
   }

   public Trait getTrait(String name) {
      TraitInfo info = (TraitInfo)this.registered.get(name);
      return info == null ? null : this.create(info);
   }

   public Class getTraitClass(String name) {
      TraitInfo info = (TraitInfo)this.registered.get(name.toLowerCase());
      return info == null ? null : info.getTraitClass();
   }

   public boolean isInternalTrait(Trait trait) {
      return INTERNAL_TRAITS.contains(trait.getName());
   }

   public void registerTrait(TraitInfo info) {
      Preconditions.checkNotNull(info, "info cannot be null");
      if (this.registered.containsKey(info.getTraitName())) {
         throw new IllegalArgumentException("trait name already registered");
      } else {
         this.registered.put(info.getTraitName(), info);
         if (info.isDefaultTrait()) {
            this.defaultTraits.add(info);
         }

      }
   }
}
