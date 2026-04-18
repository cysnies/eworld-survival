package net.citizensnpcs.npc.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class EmptySocket extends Socket {
   private static final byte[] EMPTY = new byte[50];

   public EmptySocket() {
      super();
   }

   public InputStream getInputStream() {
      return new ByteArrayInputStream(EMPTY);
   }

   public OutputStream getOutputStream() {
      return new ByteArrayOutputStream(10);
   }
}
