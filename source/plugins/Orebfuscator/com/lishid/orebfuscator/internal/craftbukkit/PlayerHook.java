package com.lishid.orebfuscator.internal.craftbukkit;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.internal.IPlayerHook;
import com.lishid.orebfuscator.utils.ReflectionHelper;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PlayerHook implements IPlayerHook {
   static boolean hookPacket = false;

   public PlayerHook() {
      super();
   }

   public void HookNM(Player p) {
      CraftPlayer player = (CraftPlayer)p;
      NetworkManager networkManager = (NetworkManager)player.getHandle().netServerHandler.networkManager;
      Field[] networkFields = networkManager.getClass().getDeclaredFields();

      for(Field field : networkFields) {
         try {
            if (List.class.isAssignableFrom(field.getType())) {
               List<Packet> list = new NetworkQueue(p);
               field.setAccessible(true);
               List<Packet> oldList = (List)field.get(networkManager);
               synchronized(ReflectionHelper.getPrivateField(networkManager, "h")) {
                  list.addAll(oldList);
                  oldList.clear();
               }

               field.set(networkManager, Collections.synchronizedList(list));
            }
         } catch (Exception e) {
            Orebfuscator.log((Throwable)e);
         }
      }

      this.hookPacket();
   }

   private void hookPacket() {
      if (!hookPacket) {
         hookPacket = true;
         Packet.l.a(14, Packet14Orebfuscator.class);
         Field[] packetFields = Packet.class.getDeclaredFields();

         for(Field field : packetFields) {
            try {
               if (Map.class.isAssignableFrom(field.getType())) {
                  field.setAccessible(true);
                  Map packets = (Map)field.get((Object)null);
                  packets.put(Packet14Orebfuscator.class, 14);
               }
            } catch (Exception e) {
               Orebfuscator.log((Throwable)e);
            }
         }

      }
   }

   public void HookChunkQueue(Player p) {
      CraftPlayer player = (CraftPlayer)p;
      ReflectionHelper.setPrivateFinal(player.getHandle(), "chunkCoordIntPairQueue", new ChunkQueue(player, player.getHandle().chunkCoordIntPairQueue));
   }
}
