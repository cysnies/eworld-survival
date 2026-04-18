package net.citizensnpcs.npc.ai;

import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.util.PlayerAnimation;
import net.minecraft.server.v1_6_R2.Enchantment;
import net.minecraft.server.v1_6_R2.EnchantmentManager;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.EntityPlayer;
import net.minecraft.server.v1_6_R2.ItemStack;
import net.minecraft.server.v1_6_R2.MobEffectList;
import net.minecraft.server.v1_6_R2.World;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BlockBreaker extends BehaviorGoalAdapter {
   private final Configuration configuration;
   private int currentDamage;
   private int currentTick;
   private final EntityLiving entity;
   private boolean isDigging;
   private int startDigTick;
   private final int x;
   private final int y;
   private final int z;
   private static final Configuration EMPTY = new Configuration();

   private BlockBreaker(LivingEntity entity, Block target, Configuration config) {
      super();
      this.entity = ((CraftLivingEntity)entity).getHandle();
      this.x = target.getX();
      this.y = target.getY();
      this.z = target.getZ();
      this.startDigTick = (int)(System.currentTimeMillis() / 50L);
      this.configuration = config;
   }

   private double distanceSquared() {
      return Math.pow(this.entity.locX - (double)this.x, (double)2.0F) + Math.pow(this.entity.locY - (double)this.y, (double)2.0F) + Math.pow(this.entity.locZ - (double)this.z, (double)2.0F);
   }

   private ItemStack getCurrentItem() {
      return this.configuration.item() != null ? CraftItemStack.asNMSCopy(this.configuration.item()) : this.entity.getEquipment(0);
   }

   private float getStrength(net.minecraft.server.v1_6_R2.Block block) {
      float base = block.l((World)null, 0, 0, 0);
      return base < 0.0F ? 0.0F : (!this.isDestroyable(block) ? 1.0F / base / 100.0F : this.strengthMod(block) / base / 30.0F);
   }

   private boolean isDestroyable(net.minecraft.server.v1_6_R2.Block block) {
      if (block.material.isAlwaysDestroyable()) {
         return true;
      } else {
         ItemStack current = this.getCurrentItem();
         return current != null ? current.b(block) : false;
      }
   }

   public boolean isFinished() {
      return !this.isDigging;
   }

   public void reset() {
      if (this.configuration.callback() != null) {
         this.configuration.callback().run();
      }

      this.isDigging = false;
      this.setBlockDamage(this.currentDamage = -1);
   }

   public BehaviorStatus run() {
      if (!this.isDigging) {
         this.reset();
         return BehaviorStatus.SUCCESS;
      } else {
         this.currentTick = (int)(System.currentTimeMillis() / 50L);
         if (this.configuration.radiusSquared() > (double)0.0F && this.distanceSquared() >= this.configuration.radiusSquared()) {
            this.startDigTick = this.currentTick;
            return BehaviorStatus.RUNNING;
         } else {
            if (this.entity instanceof EntityPlayer) {
               PlayerAnimation.ARM_SWING.play((Player)this.entity.getBukkitEntity());
            }

            net.minecraft.server.v1_6_R2.Block block = net.minecraft.server.v1_6_R2.Block.byId[this.entity.world.getTypeId(this.x, this.y, this.z)];
            if (block == null) {
               return BehaviorStatus.SUCCESS;
            } else {
               int tickDifference = this.currentTick - this.startDigTick;
               float damage = this.getStrength(block) * (float)(tickDifference + 1);
               if (damage >= 1.0F) {
                  this.entity.world.getWorld().getBlockAt(this.x, this.y, this.z).breakNaturally(CraftItemStack.asCraftMirror(this.getCurrentItem()));
                  return BehaviorStatus.SUCCESS;
               } else {
                  int modifiedDamage = (int)(damage * 10.0F);
                  if (modifiedDamage != this.currentDamage) {
                     this.setBlockDamage(modifiedDamage);
                     this.currentDamage = modifiedDamage;
                  }

                  return BehaviorStatus.RUNNING;
               }
            }
         }
      }
   }

   private void setBlockDamage(int modifiedDamage) {
      this.entity.world.f(this.entity.id, this.x, this.y, this.z, modifiedDamage);
   }

   public boolean shouldExecute() {
      return Material.getMaterial(this.entity.world.getTypeId(this.x, this.y, this.z)) != null;
   }

   private float strengthMod(net.minecraft.server.v1_6_R2.Block block) {
      ItemStack itemstack = this.getCurrentItem();
      float strength = itemstack != null ? itemstack.a(block) : 1.0F;
      int ench = EnchantmentManager.getEnchantmentLevel(Enchantment.DURABILITY.id, this.getCurrentItem());
      if (ench > 0 && itemstack != null) {
         float levelSquared = (float)(ench * ench + 1);
         if (!itemstack.b(block) && strength <= 1.0F) {
            strength += levelSquared * 0.08F;
         } else {
            strength += levelSquared;
         }
      }

      if (this.entity.hasEffect(MobEffectList.FASTER_DIG)) {
         strength *= 1.0F + (float)(this.entity.getEffect(MobEffectList.FASTER_DIG).getAmplifier() + 1) * 0.2F;
      }

      if (this.entity.hasEffect(MobEffectList.SLOWER_DIG)) {
         strength *= 1.0F - (float)(this.entity.getEffect(MobEffectList.SLOWER_DIG).getAmplifier() + 1) * 0.2F;
      }

      if (this.entity.a(net.minecraft.server.v1_6_R2.Material.WATER) && !EnchantmentManager.hasWaterWorkerEnchantment(this.entity)) {
         strength /= 5.0F;
      }

      if (!this.entity.onGround) {
         strength /= 5.0F;
      }

      return strength;
   }

   public static BlockBreaker create(LivingEntity entity, Block target) {
      return createWithConfiguration(entity, target, EMPTY);
   }

   public static BlockBreaker createWithConfiguration(LivingEntity entity, Block target, Configuration config) {
      return new BlockBreaker(entity, target, config);
   }

   public static class Configuration {
      private Runnable callback;
      private org.bukkit.inventory.ItemStack itemStack;
      private double radius;

      public Configuration() {
         super();
      }

      private Runnable callback() {
         return this.callback;
      }

      public Configuration callback(Runnable callback) {
         this.callback = callback;
         return this;
      }

      private org.bukkit.inventory.ItemStack item() {
         return this.itemStack;
      }

      public Configuration item(org.bukkit.inventory.ItemStack stack) {
         this.itemStack = stack;
         return this;
      }

      public Configuration radius(double radius) {
         this.radius = radius;
         return this;
      }

      private double radiusSquared() {
         return Math.pow(this.radius, (double)2.0F);
      }
   }
}
