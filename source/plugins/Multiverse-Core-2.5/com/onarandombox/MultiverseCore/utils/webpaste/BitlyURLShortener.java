package com.onarandombox.MultiverseCore.utils.webpaste;

import java.io.IOException;

public class BitlyURLShortener extends HttpAPIClient implements URLShortener {
   private static final String GENERIC_BITLY_REQUEST_FORMAT = "https://api-ssl.bitly.com/v3/shorten?format=txt&apiKey=%s&login=%s&longUrl=%s";
   private static final String USERNAME = "multiverse2";
   private static final String API_KEY = "R_9dbff4862a3bc0c4218a7d78cc10d0e0";

   public BitlyURLShortener() {
      super(String.format("https://api-ssl.bitly.com/v3/shorten?format=txt&apiKey=%s&login=%s&longUrl=%s", "R_9dbff4862a3bc0c4218a7d78cc10d0e0", "multiverse2", "%s"));
   }

   public String shorten(String longUrl) {
      try {
         String result = this.exec(new Object[]{longUrl});
         if (!result.startsWith("http://j.mp/")) {
            throw new IOException(result);
         } else {
            return result;
         }
      } catch (IOException e) {
         e.printStackTrace();
         return longUrl;
      }
   }
}
