package net.citizensnpcs.api.jnbt;

public abstract class Tag {
   private final String name;

   public Tag(String name) {
      super();
      this.name = name;
   }

   public final String getName() {
      return this.name;
   }

   public abstract Object getValue();
}
