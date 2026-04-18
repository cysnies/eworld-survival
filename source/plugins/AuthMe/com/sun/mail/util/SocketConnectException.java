package com.sun.mail.util;

import java.io.IOException;

public class SocketConnectException extends IOException {
   private String host;
   private int port;
   private int cto;
   private static final long serialVersionUID = 3997871560538755463L;

   public SocketConnectException(String msg, Exception cause, String host, int port, int cto) {
      super(msg);
      this.initCause(cause);
      this.host = host;
      this.port = port;
      this.cto = cto;
   }

   public Exception getException() {
      assert this.getCause() instanceof Exception;

      return (Exception)this.getCause();
   }

   public String getHost() {
      return this.host;
   }

   public int getPort() {
      return this.port;
   }

   public int getConnectionTimeout() {
      return this.cto;
   }
}
