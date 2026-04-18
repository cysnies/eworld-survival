package level;

import java.util.HashMap;
import java.util.UUID;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import net.minecraft.server.v1_6_R2.AttributeInstance;
import net.minecraft.server.v1_6_R2.AttributeModifier;
import net.minecraft.server.v1_6_R2.GenericAttributes;
import net.minecraft.server.v1_6_R2.IAttribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

public class Effect {
   private static final UUID UUID_ATTACK = UUID.fromString("a18b05e6-5d2a-411b-b873-256d10271ba9");
   private static final UUID UUID_HEALTH = UUID.fromString("37d25004-adca-41a0-b2ba-baf96eb8ea05");
   private static final UUID UUID_ATTACK2 = UUID.fromString("a18b05e6-5d2a-411b-b873-256d10271baa");
   private static final UUID UUID_HEALTH2 = UUID.fromString("37d25004-adca-41a0-b2ba-baf96eb8ea0a");
   private Type type;
   private int operation;
   private double amount;
   private static HashMap typeHash;
   private static HashMap uidHash;
   private static HashMap uidHash2;
   private static HashMap showHash;

   public static void init(Main main) {
      typeHash = new HashMap();
      typeHash.put(Effect.Type.attack, GenericAttributes.e);
      typeHash.put(Effect.Type.health, GenericAttributes.a);
      uidHash = new HashMap();
      uidHash.put(Effect.Type.attack, UUID_ATTACK);
      uidHash.put(Effect.Type.health, UUID_HEALTH);
      uidHash2 = new HashMap();
      uidHash2.put(Effect.Type.attack, UUID_ATTACK2);
      uidHash2.put(Effect.Type.health, UUID_HEALTH2);
      loadConfig(UtilConfig.getConfig(Main.getPn()));
   }

   public Effect(Type type, int operation, double amount) {
      super();
      this.type = type;
      this.operation = operation;
      this.amount = amount;
   }

   public String getShow() {
      String show = (String)showHash.get(this.type);
      String result;
      if (this.type.equals(Effect.Type.miss)) {
         result = UtilFormat.format(Main.getPn(), "effectShow2", new Object[]{Util.getDouble(this.amount, 2), show});
      } else if (this.operation == 0) {
         result = UtilFormat.format(Main.getPn(), "effectShow1", new Object[]{this.amount, show});
      } else {
         result = UtilFormat.format(Main.getPn(), "effectShow2", new Object[]{Util.getDouble(this.amount * (double)100.0F, 2), show});
      }

      return result;
   }

   public void apply(LivingEntity le) {
      IAttribute ia = (IAttribute)typeHash.get(this.type);
      UUID uid;
      if (this.operation == 0) {
         uid = (UUID)uidHash.get(this.type);
      } else {
         uid = (UUID)uidHash2.get(this.type);
      }

      AttributeInstance ai = ((CraftLivingEntity)le).getHandle().getAttributeInstance(ia);
      AttributeModifier am = new AttributeModifier(uid, "none", this.amount, this.operation);
      ai.b(am);
      ai.a(am);
   }

   public Type getType() {
      return this.type;
   }

   public int getOperation() {
      return this.operation;
   }

   public double getAmount() {
      return this.amount;
   }

   private static void loadConfig(YamlConfiguration config) {
      showHash = new HashMap();
      showHash.put(Effect.Type.attack, config.getString("effect.names.attack"));
      showHash.put(Effect.Type.health, config.getString("effect.names.health"));
      showHash.put(Effect.Type.miss, config.getString("effect.names.miss"));
   }

   public static enum Type {
      attack,
      health,
      miss;

      private Type() {
      }
   }
}
