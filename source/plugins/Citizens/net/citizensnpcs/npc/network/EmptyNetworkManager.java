package net.citizensnpcs.npc.network;

import java.io.IOException;
import java.net.Socket;
import java.security.PrivateKey;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_6_R2.Connection;
import net.minecraft.server.v1_6_R2.IConsoleLogManager;
import net.minecraft.server.v1_6_R2.NetworkManager;
import net.minecraft.server.v1_6_R2.Packet;

public class EmptyNetworkManager extends NetworkManager {
   public EmptyNetworkManager(IConsoleLogManager logManager, Socket socket, String string, Connection conn, PrivateKey key) throws IOException {
      super(logManager, socket, string, conn, key);
      NMS.stopNetworkThreads(this);
   }

   public void a() {
   }

   public void a(Connection conn) {
   }

   public void a(String s, Object... objects) {
   }

   public void b() {
   }

   public void d() {
   }

   public int e() {
      return 0;
   }

   public void queue(Packet packet) {
   }
}
