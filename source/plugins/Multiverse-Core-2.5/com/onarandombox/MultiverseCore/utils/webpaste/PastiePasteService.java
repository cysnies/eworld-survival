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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PastiePasteService implements PasteService {
   private boolean isPrivate;

   public PastiePasteService(boolean isPrivate) {
      super();
      this.isPrivate = isPrivate;
   }

   public URL getPostURL() {
      try {
         return new URL("http://pastie.org/pastes");
      } catch (MalformedURLException var2) {
         return null;
      }
   }

   public String encodeData(String data) {
      try {
         String encData = URLEncoder.encode("paste[authorization]", "UTF-8") + "=" + URLEncoder.encode("burger", "UTF-8");
         encData = encData + "&" + URLEncoder.encode("paste[restricted]", "UTF-8") + "=" + URLEncoder.encode(this.isPrivate ? "1" : "0", "UTF-8");
         encData = encData + "&" + URLEncoder.encode("paste[parser_id]", "UTF-8") + "=" + URLEncoder.encode("6", "UTF-8");
         encData = encData + "&" + URLEncoder.encode("paste[body]", "UTF-8") + "=" + URLEncoder.encode(data, "UTF-8");
         return encData;
      } catch (UnsupportedEncodingException var3) {
         return "";
      }
   }

   public String postData(String encodedData, URL url) throws PasteFailedException {
      OutputStreamWriter wr = null;
      BufferedReader rd = null;

      String var23;
      try {
         URLConnection conn = url.openConnection();
         conn.setDoOutput(true);
         wr = new OutputStreamWriter(conn.getOutputStream());
         wr.write(encodedData);
         wr.flush();
         rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
         String pastieUrl = "";
         Pattern pastiePattern = this.getURLMatchingPattern();

         String line;
         while((line = rd.readLine()) != null) {
            Matcher m = pastiePattern.matcher(line);
            if (m.matches()) {
               String pastieID = m.group(1);
               pastieUrl = this.formatURL(pastieID);
            }
         }

         var23 = pastieUrl;
      } catch (Exception e) {
         throw new PasteFailedException(e);
      } finally {
         if (wr != null) {
            try {
               wr.close();
            } catch (IOException var20) {
            }
         }

         if (rd != null) {
            try {
               rd.close();
            } catch (IOException var19) {
            }
         }

      }

      return var23;
   }

   private Pattern getURLMatchingPattern() {
      return this.isPrivate ? Pattern.compile(".*http://pastie.org/.*key=([0-9a-z]+).*") : Pattern.compile(".*http://pastie.org/([0-9]+).*");
   }

   private String formatURL(String pastieID) {
      return "http://pastie.org/" + (this.isPrivate ? "private/" : "") + pastieID;
   }
}
