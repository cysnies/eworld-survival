package net.citizensnpcs.trait;

import com.google.common.collect.Maps;
import java.lang.reflect.Constructor;
import java.util.Map;
import net.citizensnpcs.Settings;
import net.citizensnpcs.api.command.CommandConfigurable;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.NPCVehicleExitEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_6_R2.EntityEnderDragon;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class Controllable extends Trait implements Toggleable, CommandConfigurable {
   private MovementController controller;
   @Persist
   private boolean enabled;
   private EntityType explicitType;
   private static final Map controllerTypes = Maps.newEnumMap(EntityType.class);

   public Controllable() {
      super("controllable");
      this.controller = new GroundController();
      this.enabled = true;
   }

   public Controllable(boolean enabled) {
      this();
      this.enabled = enabled;
   }

   public void configure(CommandContext args) {
      if (args.hasFlag('f')) {
         this.explicitType = EntityType.BLAZE;
      } else if (args.hasFlag('g')) {
         this.explicitType = EntityType.OCELOT;
      } else if (args.hasFlag('o')) {
         this.explicitType = EntityType.UNKNOWN;
      } else if (args.hasFlag('r')) {
         this.explicitType = null;
      } else if (args.hasValueFlag("explicittype")) {
         this.explicitType = Util.matchEntityType(args.getFlag("explicittype"));
      }

      if (this.npc.isSpawned()) {
         this.loadController();
      }

   }

   private void enterOrLeaveVehicle(Player player) {
      EntityPlayer handle = ((CraftPlayer)player).getHandle();
      if (this.getHandle().passenger != null) {
         if (this.getHandle().passenger == handle) {
            player.leaveVehicle();
            Bukkit.getPluginManager().callEvent(new NPCVehicleExitEvent(this.npc, player));
         }

      } else {
         if (((Owner)this.npc.getTrait(Owner.class)).isOwnedBy((CommandSender)handle.getBukkitEntity())) {
            handle.setPassengerOf(this.getHandle());
         }

      }
   }

   private EntityLiving getHandle() {
      return ((CraftLivingEntity)this.npc.getBukkitEntity()).getHandle();
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void load(DataKey key) throws NPCLoadException {
      if (key.keyExists("explicittype")) {
         this.explicitType = Util.matchEntityType(key.getString("explicittype"));
      }

   }

   private void loadController() {
      EntityType type = this.npc.getBukkitEntity().getType();
      if (this.explicitType != null) {
         type = this.explicitType;
      }

      Class<? extends MovementController> clazz = (Class)controllerTypes.get(type);
      if (clazz == null) {
         this.controller = new GroundController();
      } else {
         Constructor<? extends MovementController> innerConstructor = null;

         try {
            innerConstructor = clazz.getConstructor(Controllable.class);
            innerConstructor.setAccessible(true);
         } catch (Exception e) {
            e.printStackTrace();
         }

         try {
            if (innerConstructor == null) {
               this.controller = (MovementController)clazz.newInstance();
            } else {
               this.controller = (MovementController)innerConstructor.newInstance(this);
            }
         } catch (Exception e) {
            e.printStackTrace();
            this.controller = new GroundController();
         }

      }
   }

   public boolean mount(Player toMount) {
      Entity passenger = this.npc.getBukkitEntity().getPassenger();
      if (passenger != null && passenger != toMount) {
         return false;
      } else {
         this.enterOrLeaveVehicle(toMount);
         return true;
      }
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (this.npc.isSpawned() && this.enabled) {
         EntityPlayer handle = ((CraftPlayer)event.getPlayer()).getHandle();
         Action performed = event.getAction();
         if (handle.equals(this.getHandle().passenger)) {
            switch (performed) {
               case RIGHT_CLICK_BLOCK:
               case RIGHT_CLICK_AIR:
                  this.controller.rightClick(event);
                  break;
               case LEFT_CLICK_BLOCK:
               case LEFT_CLICK_AIR:
                  this.controller.leftClick(event);
            }

         }
      }
   }

   @EventHandler
   public void onRightClick(NPCRightClickEvent event) {
      if (this.enabled && this.npc.isSpawned() && event.getNPC().equals(this.npc)) {
         this.controller.rightClickEntity(event);
      }
   }

   public void onSpawn() {
      this.loadController();
   }

   public void run() {
      if (this.enabled && this.npc.isSpawned() && this.getHandle().passenger != null) {
         this.controller.run((Player)this.getHandle().passenger.getBukkitEntity());
      }
   }

   public void save(DataKey key) {
      if (this.explicitType == null) {
         key.removeKey("explicittype");
      } else {
         key.setString("explicittype", this.explicitType.name());
      }

   }

   public boolean setEnabled(boolean enabled) {
      this.enabled = enabled;
      return enabled;
   }

   private void setMountedYaw(EntityLiving handle) {
      if (!(handle instanceof EntityEnderDragon) && Settings.Setting.USE_BOAT_CONTROLS.asBoolean()) {
         double tX = handle.locX + handle.motX;
         double tZ = handle.locZ + handle.motZ;
         if (handle.locZ > tZ) {
            handle.yaw = (float)(-Math.toDegrees(Math.atan((handle.locX - tX) / (handle.locZ - tZ)))) + 180.0F;
         } else if (handle.locZ < tZ) {
            handle.yaw = (float)(-Math.toDegrees(Math.atan((handle.locX - tX) / (handle.locZ - tZ))));
         }

         NMS.setHeadYaw(handle, handle.yaw);
      }
   }

   public boolean toggle() {
      this.enabled = !this.enabled;
      if (!this.enabled && this.getHandle().passenger != null) {
         this.getHandle().passenger.getBukkitEntity().leaveVehicle();
      }

      return this.enabled;
   }

   private double updateHorizontralSpeed(EntityLiving handle, double speed, float speedMod) {
      double oldSpeed = Math.sqrt(handle.motX * handle.motX + handle.motZ * handle.motZ);
      double horizontal = (double)((EntityLiving)handle.passenger).bf;
      if (horizontal > (double)0.0F) {
         double dXcos = -Math.sin((double)handle.passenger.yaw * Math.PI / (double)180.0F);
         double dXsin = Math.cos((double)handle.passenger.yaw * Math.PI / (double)180.0F);
         handle.motX += dXcos * speed * (double)0.5F;
         handle.motZ += dXsin * speed * (double)0.5F;
      }

      handle.motX += handle.passenger.motX * (double)speedMod;
      handle.motZ += handle.passenger.motZ * (double)speedMod;
      double newSpeed = Math.sqrt(handle.motX * handle.motX + handle.motZ * handle.motZ);
      if (newSpeed > 0.35) {
         double movementFactor = 0.35 / newSpeed;
         handle.motX *= movementFactor;
         handle.motZ *= movementFactor;
         newSpeed = 0.35;
      }

      return newSpeed > oldSpeed && speed < 0.35 ? (double)((float)Math.min(0.35, speed + (0.35 - speed) / (double)35.0F)) : (double)((float)Math.max(0.07, speed - (speed - 0.07) / (double)35.0F));
   }

   static {
      controllerTypes.put(EntityType.BAT, PlayerInputAirController.class);
      controllerTypes.put(EntityType.BLAZE, PlayerInputAirController.class);
      controllerTypes.put(EntityType.ENDER_DRAGON, PlayerInputAirController.class);
      controllerTypes.put(EntityType.GHAST, PlayerInputAirController.class);
      controllerTypes.put(EntityType.WITHER, PlayerInputAirController.class);
      controllerTypes.put(EntityType.UNKNOWN, LookAirController.class);
   }

   public class GroundController implements MovementController {
      private int jumpTicks = 0;
      private double speed = 0.07;
      private static final float AIR_SPEED = 1.5F;
      private static final float GROUND_SPEED = 4.0F;
      private static final float JUMP_VELOCITY = 0.6F;

      public GroundController() {
         super();
      }

      public void leftClick(PlayerInteractEvent event) {
      }

      public void rightClick(PlayerInteractEvent event) {
      }

      public void rightClickEntity(NPCRightClickEvent event) {
         Controllable.this.enterOrLeaveVehicle(event.getClicker());
      }

      public void run(Player rider) {
         EntityLiving handle = Controllable.this.getHandle();
         boolean onGround = handle.onGround;
         float speedMod = Controllable.this.npc.getNavigator().getDefaultParameters().modifiedSpeed(onGround ? 4.0F : 1.5F);
         this.speed = Controllable.this.updateHorizontralSpeed(handle, this.speed, speedMod);
         boolean shouldJump = NMS.shouldJump(handle.passenger);
         if (shouldJump) {
            if (handle.onGround && this.jumpTicks == 0) {
               Controllable.this.getHandle().motY = (double)0.6F;
               this.jumpTicks = 10;
            }
         } else {
            this.jumpTicks = 0;
         }

         this.jumpTicks = Math.max(0, this.jumpTicks - 1);
         Controllable.this.setMountedYaw(handle);
      }
   }

   public class LookAirController implements MovementController {
      boolean paused = false;

      public LookAirController() {
         super();
      }

      public void leftClick(PlayerInteractEvent event) {
         this.paused = !this.paused;
      }

      public void rightClick(PlayerInteractEvent event) {
         this.paused = !this.paused;
      }

      public void rightClickEntity(NPCRightClickEvent event) {
         Controllable.this.enterOrLeaveVehicle(event.getClicker());
      }

      public void run(Player rider) {
         if (this.paused) {
            Controllable.this.getHandle().motY = 0.001;
         } else {
            Vector dir = rider.getEyeLocation().getDirection();
            dir.multiply(Controllable.this.npc.getNavigator().getDefaultParameters().speedModifier());
            EntityLiving handle = Controllable.this.getHandle();
            handle.motX = dir.getX();
            handle.motY = dir.getY();
            handle.motZ = dir.getZ();
            Controllable.this.setMountedYaw(handle);
         }
      }
   }

   public class PlayerInputAirController implements MovementController {
      boolean paused = false;
      private double speed;

      public PlayerInputAirController() {
         super();
      }

      public void leftClick(PlayerInteractEvent event) {
         this.paused = !this.paused;
      }

      public void rightClick(PlayerInteractEvent event) {
         Controllable.this.getHandle().motY = (double)-0.3F;
      }

      public void rightClickEntity(NPCRightClickEvent event) {
         Controllable.this.enterOrLeaveVehicle(event.getClicker());
      }

      public void run(Player rider) {
         if (this.paused) {
            Controllable.this.getHandle().motY = 0.001;
         } else {
            EntityLiving handle = Controllable.this.getHandle();
            this.speed = Controllable.this.updateHorizontralSpeed(handle, this.speed, 1.0F);
            boolean shouldJump = NMS.shouldJump(handle.passenger);
            if (shouldJump) {
               handle.motY = (double)0.3F;
            }

            handle.motY *= (double)0.98F;
         }
      }
   }

   public interface MovementController {
      void leftClick(PlayerInteractEvent var1);

      void rightClick(PlayerInteractEvent var1);

      void rightClickEntity(NPCRightClickEvent var1);

      void run(Player var1);
   }
}
