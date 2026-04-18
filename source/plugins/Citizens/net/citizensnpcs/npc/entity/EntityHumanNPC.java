package net.citizensnpcs.npc.entity;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import net.citizensnpcs.Settings;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.npc.network.EmptyNetHandler;
import net.citizensnpcs.npc.network.EmptyNetworkManager;
import net.citizensnpcs.npc.network.EmptySocket;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.citizensnpcs.util.nms.PlayerControllerJump;
import net.citizensnpcs.util.nms.PlayerControllerLook;
import net.citizensnpcs.util.nms.PlayerControllerMove;
import net.citizensnpcs.util.nms.PlayerEntitySenses;
import net.citizensnpcs.util.nms.PlayerNavigation;
import net.minecraft.server.v1_6_R2.AttributeInstance;
import net.minecraft.server.v1_6_R2.Connection;
import net.minecraft.server.v1_6_R2.Entity;
import net.minecraft.server.v1_6_R2.EntityPlayer;
import net.minecraft.server.v1_6_R2.EnumGamemode;
import net.minecraft.server.v1_6_R2.GenericAttributes;
import net.minecraft.server.v1_6_R2.MathHelper;
import net.minecraft.server.v1_6_R2.MinecraftServer;
import net.minecraft.server.v1_6_R2.Navigation;
import net.minecraft.server.v1_6_R2.NetworkManager;
import net.minecraft.server.v1_6_R2.Packet;
import net.minecraft.server.v1_6_R2.Packet201PlayerInfo;
import net.minecraft.server.v1_6_R2.Packet35EntityHeadRotation;
import net.minecraft.server.v1_6_R2.Packet5EntityEquipment;
import net.minecraft.server.v1_6_R2.PlayerInteractManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_6_R2.CraftServer;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class EntityHumanNPC extends EntityPlayer implements NPCHolder {
   private PlayerControllerJump controllerJump;
   private PlayerControllerLook controllerLook;
   private PlayerControllerMove controllerMove;
   private PlayerEntitySenses entitySenses;
   private boolean gravity = true;
   private int jumpTicks = 0;
   private PlayerNavigation navigation;
   private final CitizensNPC npc;
   private final Location packetLocationCache = new Location((World)null, (double)0.0F, (double)0.0F, (double)0.0F);
   private int packetUpdateCount;
   private int sensesUpdateCount = 0;
   private int useListName = -1;
   private static final float EPSILON = 0.005F;
   private static final Location LOADED_LOCATION = new Location((World)null, (double)0.0F, (double)0.0F, (double)0.0F);

   public EntityHumanNPC(MinecraftServer minecraftServer, net.minecraft.server.v1_6_R2.World world, String string, PlayerInteractManager playerInteractManager, NPC npc) {
      super(minecraftServer, world, string, playerInteractManager);
      playerInteractManager.setGameMode(EnumGamemode.SURVIVAL);
      this.npc = (CitizensNPC)npc;
      if (npc != null) {
         this.initialise(minecraftServer);
      }

   }

   public void collide(Entity entity) {
      super.collide(entity);
      if (this.npc != null) {
         Util.callCollisionEvent(this.npc, entity.getBukkitEntity());
      }

   }

   public void g(double x, double y, double z) {
      if (this.npc == null) {
         super.g(x, y, z);
      } else if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0) {
         if (!(Boolean)this.npc.data().get("protected", true)) {
            super.g(x, y, z);
         }

      } else {
         Vector vector = new Vector(x, y, z);
         NPCPushEvent event = Util.callPushEvent(this.npc, vector);
         if (!event.isCancelled()) {
            vector = event.getCollisionVector();
            super.g(vector.getX(), vector.getY(), vector.getZ());
         }

      }
   }

   public CraftPlayer getBukkitEntity() {
      if (this.npc != null && this.bukkitEntity == null) {
         this.bukkitEntity = new PlayerNPC(this);
      }

      return super.getBukkitEntity();
   }

   public PlayerControllerJump getControllerJump() {
      return this.controllerJump;
   }

   public Navigation getNavigation() {
      return this.navigation;
   }

   public NPC getNPC() {
      return this.npc;
   }

   private void initialise(MinecraftServer minecraftServer) {
      Socket socket = new EmptySocket();
      NetworkManager conn = null;

      try {
         conn = new EmptyNetworkManager(minecraftServer.getLogger(), socket, "npc mgr", new Connection() {
            public boolean a() {
               return false;
            }
         }, minecraftServer.H().getPrivate());
         this.playerConnection = new EmptyNetHandler(minecraftServer, conn, this);
         conn.a(this.playerConnection);
      } catch (IOException var6) {
      }

      NMS.setStepHeight(this, 1.0F);

      try {
         socket.close();
      } catch (IOException var5) {
      }

      AttributeInstance range = this.getAttributeInstance(GenericAttributes.b);
      if (range == null) {
         range = this.aW().b(GenericAttributes.b);
      }

      range.setValue(Settings.Setting.DEFAULT_PATHFINDING_RANGE.asDouble());
      this.controllerJump = new PlayerControllerJump(this);
      this.controllerLook = new PlayerControllerLook(this);
      this.controllerMove = new PlayerControllerMove(this);
      this.entitySenses = new PlayerEntitySenses(this);
      this.navigation = new PlayerNavigation(this, this.world);
   }

   public boolean isNavigating() {
      return this.npc.getNavigator().isNavigating();
   }

   public void l_() {
      super.l_();
      if (this.npc != null) {
         boolean navigating = this.npc.getNavigator().isNavigating();
         this.updatePackets(navigating);
         if (this.gravity && !navigating && this.getBukkitEntity() != null && Util.isLoaded(this.getBukkitEntity().getLocation(LOADED_LOCATION)) && !NMS.inWater(this.getBukkitEntity())) {
            this.move((double)0.0F, -0.2, (double)0.0F);
         }

         if (!(Boolean)this.npc.data().get("removefromplayerlist", Settings.Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean())) {
            this.h();
         }

         if (Math.abs(this.motX) < (double)0.005F && Math.abs(this.motY) < (double)0.005F && Math.abs(this.motZ) < (double)0.005F) {
            this.motX = this.motY = this.motZ = (double)0.0F;
         }

         if (navigating) {
            if (!NMS.isNavigationFinished(this.navigation)) {
               NMS.updateNavigation(this.navigation);
            }

            this.moveOnCurrentHeading();
         } else if (this.motX != (double)0.0F || this.motZ != (double)0.0F || this.motY != (double)0.0F) {
            this.e(0.0F, 0.0F);
         }

         if (this.noDamageTicks > 0) {
            --this.noDamageTicks;
         }

         this.npc.update();
      }
   }

   private void moveOnCurrentHeading() {
      NMS.updateAI(this);
      if (this.bd) {
         if (this.onGround && this.jumpTicks == 0) {
            this.bd();
            this.jumpTicks = 10;
         }
      } else {
         this.jumpTicks = 0;
      }

      this.be *= 0.98F;
      this.bf *= 0.98F;
      this.bg *= 0.9F;
      this.e(this.be, this.bf);
      NMS.setHeadYaw(this, this.yaw);
      if (this.jumpTicks > 0) {
         --this.jumpTicks;
      }

   }

   public void setMoveDestination(double x, double y, double z, float speed) {
      this.controllerMove.a(x, y, z, (double)speed);
   }

   public void setShouldJump() {
      this.controllerJump.a();
   }

   public void setTargetLook(Entity target, float yawOffset, float renderOffset) {
      this.controllerLook.a(target, yawOffset, renderOffset);
   }

   public void updateAI() {
      if (++this.sensesUpdateCount == 5) {
         this.sensesUpdateCount = 0;
         this.entitySenses.a();
      }

      this.controllerMove.c();
      this.controllerLook.a();
      this.controllerJump.b();
   }

   private void updatePackets(boolean navigating) {
      if (++this.packetUpdateCount >= 30) {
         Location current = this.getBukkitEntity().getLocation(this.packetLocationCache);
         Packet[] packets = new Packet[navigating ? 6 : 7];
         if (!navigating) {
            packets[6] = new Packet35EntityHeadRotation(this.id, (byte)MathHelper.d(NMS.getHeadYaw(this) * 256.0F / 360.0F));
         }

         for(int i = 0; i < 5; ++i) {
            packets[i] = new Packet5EntityEquipment(this.id, i, this.getEquipment(i));
         }

         boolean removeFromPlayerList = Settings.Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean();
         NMS.addOrRemoveFromPlayerList(this.getBukkitEntity(), (Boolean)this.npc.data().get("removefromplayerlist", removeFromPlayerList));
         int useListName = removeFromPlayerList ? 0 : 1;
         if (useListName != this.useListName || this.useListName == -1) {
            this.useListName = useListName;
            packets[5] = new Packet201PlayerInfo(this.getBukkitEntity().getPlayerListName(), !removeFromPlayerList, removeFromPlayerList ? 9999 : this.ping);
         }

         NMS.sendPacketsNearby(current, packets);
         this.packetUpdateCount = 0;
      }

   }

   public void updatePathfindingRange(float pathfindingRange) {
      this.navigation.setRange(pathfindingRange);
   }

   public static class PlayerNPC extends CraftPlayer implements NPCHolder {
      private final CraftServer cserver;
      private final CitizensNPC npc;

      private PlayerNPC(EntityHumanNPC entity) {
         super((CraftServer)Bukkit.getServer(), entity);
         this.npc = entity.npc;
         this.cserver = (CraftServer)Bukkit.getServer();
      }

      public EntityHumanNPC getHandle() {
         return (EntityHumanNPC)this.entity;
      }

      public List getMetadata(String metadataKey) {
         return this.cserver.getEntityMetadata().getMetadata(this, metadataKey);
      }

      public NPC getNPC() {
         return this.npc;
      }

      public boolean hasLineOfSight(org.bukkit.entity.Entity other) {
         return this.getHandle().entitySenses.canSee(((CraftEntity)other).getHandle());
      }

      public boolean hasMetadata(String metadataKey) {
         return this.cserver.getEntityMetadata().hasMetadata(this, metadataKey);
      }

      public void removeMetadata(String metadataKey, Plugin owningPlugin) {
         this.cserver.getEntityMetadata().removeMetadata(this, metadataKey, owningPlugin);
      }

      public void setGravityEnabled(boolean enabled) {
         this.getHandle().gravity = enabled;
      }

      public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
         this.cserver.getEntityMetadata().setMetadata(this, metadataKey, newMetadataValue);
      }
   }
}
