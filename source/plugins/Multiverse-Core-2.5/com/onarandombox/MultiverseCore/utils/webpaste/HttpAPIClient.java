package com.onarandombox.MultiverseCore.utils.webpaste;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public abstract class HttpAPIClient {
   protected final String urlFormat;

   public HttpAPIClient(String urlFormat) {
      super();
      this.urlFormat = urlFormat;
   }

   protected final String exec(Object... args) throws IOException {
      URLConnection conn = (new URL(String.format(this.urlFormat, args))).openConnection();
      conn.connect();
      StringBuilder ret = new StringBuilder();
      BufferedReader reader = null;

      try {
         reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

         while(!reader.ready()) {
         }

         while(reader.ready()) {
            ret.append(reader.readLine()).append('\n');
         }
      } finally {
         if (reader != null) {
            try {
               reader.close();
            } catch (IOException var11) {
            }
         }

      }

      return ret.toString();
   }
}
