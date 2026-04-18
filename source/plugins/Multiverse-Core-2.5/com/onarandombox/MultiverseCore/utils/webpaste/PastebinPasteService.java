package com.onarandombox.MultiverseCore.utils.webpaste;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class PastebinPasteService implements PasteService {
   private boolean isPrivate;

   public PastebinPasteService(boolean isPrivate) {
      super();
      this.isPrivate = isPrivate;
   }

   public URL getPostURL() {
      try {
         return new URL("http://pastebin.com/api/api_post.php");
      } catch (MalformedURLException var2) {
         return null;
      }
   }

   public String encodeData(String data) {
      try {
         String encData = URLEncoder.encode("api_dev_key", "UTF-8") + "=" + URLEncoder.encode("d61d68d31e8e0392b59b50b277411c71", "UTF-8");
         encData = encData + "&" + URLEncoder.encode("api_option", "UTF-8") + "=" + URLEncoder.encode("paste", "UTF-8");
         encData = encData + "&" + URLEncoder.encode("api_paste_code", "UTF-8") + "=" + URLEncoder.encode(data, "UTF-8");
         encData = encData + "&" + URLEncoder.encode("api_paste_private", "UTF-8") + "=" + URLEncoder.encode(this.isPrivate ? "1" : "0", "UTF-8");
         encData = encData + "&" + URLEncoder.encode("api_paste_format", "UTF-8") + "=" + URLEncoder.encode("yaml", "UTF-8");
         return encData;
      } catch (UnsupportedEncodingException var3) {
         return "";
      }
   }

   public String postData(String encodedData, URL url) throws PasteFailedException {
      OutputStreamWriter wr = null;
      BufferedReader rd = null;

      String var8;
      try {
         URLConnection conn = url.openConnection();
         conn.setDoOutput(true);
         wr = new OutputStreamWriter(conn.getOutputStream());
         wr.write(encodedData);
         wr.flush();
         rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

         String pastebinUrl;
         String line;
         for(pastebinUrl = ""; (line = rd.readLine()) != null; pastebinUrl = line) {
         }

         var8 = pastebinUrl;
      } catch (Exception e) {
         throw new PasteFailedException(e);
      } finally {
         if (wr != null) {
            try {
               wr.close();
            } catch (IOException var19) {
            }
         }

         if (rd != null) {
            try {
               rd.close();
            } catch (IOException var18) {
            }
         }

      }

      return var8;
   }
}
