package com.onarandombox.MultiverseCore.configuration;

import java.util.Map;
import me.main__.util.multiverse.SerializationConfig.Property;
import me.main__.util.multiverse.SerializationConfig.SerializationConfig;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("MVEntryFee")
public class EntryFee extends SerializationConfig {
   @Property
   private double amount;
   @Property
   private int currency;

   public EntryFee() {
      super();
   }

   public EntryFee(Map values) {
      super(values);
   }

   protected void setDefaults() {
      this.amount = (double)0.0F;
      this.currency = -1;
   }

   public double getAmount() {
      return this.amount;
   }

   public int getCurrency() {
      return this.currency;
   }

   public void setAmount(double amount) {
      this.amount = amount;
   }

   public void setCurrency(int currency) {
      this.currency = currency;
   }
}
