package net.citizensnpcs.api.trait;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

public final class TraitInfo {
   private boolean defaultTrait;
   private String name;
   private Supplier supplier;
   private final Class trait;

   private TraitInfo(Class trait) {
      super();
      this.trait = trait;
   }

   public TraitInfo asDefaultTrait() {
      this.defaultTrait = true;
      return this;
   }

   public Class getTraitClass() {
      return this.trait;
   }

   public String getTraitName() {
      return this.name;
   }

   public boolean isDefaultTrait() {
      return this.defaultTrait;
   }

   public Trait tryCreateInstance() {
      if (this.supplier != null) {
         return (Trait)this.supplier.get();
      } else {
         try {
            return (Trait)this.trait.newInstance();
         } catch (Exception ex) {
            ex.printStackTrace();
            return null;
         }
      }
   }

   public TraitInfo withName(String name) {
      Preconditions.checkNotNull(name);
      this.name = name.toLowerCase();
      return this;
   }

   public TraitInfo withSupplier(Supplier supplier) {
      this.supplier = supplier;
      return this;
   }

   public static TraitInfo create(Class trait) {
      Preconditions.checkNotNull(trait);

      try {
         trait.getConstructor();
      } catch (NoSuchMethodException var2) {
         throw new IllegalArgumentException("Trait class must have a no-arguments constructor");
      }

      return new TraitInfo(trait);
   }
}
