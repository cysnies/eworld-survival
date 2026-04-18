package com.onarandombox.MultiverseCore.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/** @deprecated */
@Deprecated
public class UpdateChecker {
   public static final Logger log = Logger.getLogger("Minecraft");
   private Timer timer = new Timer();
   private String name;
   private String cversion;

   public UpdateChecker(String name, String version) {
      super();
      this.name = name;
      this.cversion = version;
      int delay = 0;
      int period = 1800;
      this.timer.scheduleAtFixedRate(new TimerTask() {
         public void run() {
            UpdateChecker.this.checkUpdate();
         }
      }, (long)(delay * 1000), (long)(period * 1000));
   }

   public void checkUpdate() {
      BufferedReader rd = null;

      try {
         URL url = new URL("http://bukkit.onarandombox.com/multiverse/version.php?n=" + URLEncoder.encode(this.name, "UTF-8") + "&v=" + this.cversion);
         URLConnection conn = url.openConnection();
         conn.setReadTimeout(2000);
         int code = ((HttpURLConnection)conn).getResponseCode();
         if (code == 200) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String version = null;

            String line;
            while((line = rd.readLine()) != null) {
               if (version == null) {
                  version = line;
               }
            }

            if (version == null) {
               return;
            }

            String v1 = normalisedVersion(version);
            String v2 = normalisedVersion(this.cversion);
            int compare = v1.compareTo(v2);
            if (compare > 0) {
               CoreLogging.info("[%s] - Update Available (%s)", this.name, version);
            }

            rd.close();
            return;
         }
      } catch (Exception var20) {
         return;
      } finally {
         if (rd != null) {
            try {
               rd.close();
            } catch (IOException var19) {
            }
         }

      }

   }

   public static String normalisedVersion(String version) {
      return normalisedVersion(version, ".", 4);
   }

   public static String normalisedVersion(String version, String sep, int maxWidth) {
      String[] split = Pattern.compile(sep, 16).split(version);
      StringBuilder sb = new StringBuilder();

      for(String s : split) {
         sb.append(String.format("%" + maxWidth + 's', s));
      }

      return sb.toString();
   }
}
