package com.khorn.terraincontrol.forge.util;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.Configuration;

public class Metrics {
   private static final int REVISION = 7;
   private static final String BASE_URL = "http://report.mcstats.org";
   private static final String REPORT_URL = "/plugin/%s";
   private static final int PING_INTERVAL = 15;
   private final String modname;
   private final String modversion;
   private final Set graphs = Collections.synchronizedSet(new HashSet());
   private final Configuration configuration;
   private final File configurationFile;
   private final String guid;
   private final boolean debug;
   private volatile IScheduledTickHandler task = null;
   private boolean stopped = false;

   public Metrics(String modname, String modversion) throws IOException {
      super();
      if (modname != null && modversion != null) {
         this.modname = modname;
         this.modversion = modversion;
         this.configurationFile = this.getConfigFile();
         this.configuration = new Configuration(this.configurationFile);
         this.configuration.get("general", "opt-out", false, "Set to true to disable all reporting");
         this.guid = this.configuration.get("general", "guid", UUID.randomUUID().toString(), "Server unique ID").getString();
         this.debug = this.configuration.get("general", "debug", false, "Set to true for verbose debug").getBoolean(false);
         this.configuration.save();
      } else {
         throw new IllegalArgumentException("modname and modversion cannot be null");
      }
   }

   public Graph createGraph(String name) {
      if (name == null) {
         throw new IllegalArgumentException("Graph name cannot be null");
      } else {
         Graph graph = new Graph(name);
         this.graphs.add(graph);
         return graph;
      }
   }

   public void addGraph(Graph graph) {
      if (graph == null) {
         throw new IllegalArgumentException("Graph cannot be null");
      } else {
         this.graphs.add(graph);
      }
   }

   public boolean start() {
      if (this.isOptOut()) {
         return false;
      } else {
         this.stopped = false;
         if (this.task != null) {
            return true;
         } else {
            this.task = new IScheduledTickHandler() {
               private boolean firstPost = true;
               private Thread thrd = null;

               public void tickStart(EnumSet type, Object... tickData) {
               }

               public void tickEnd(EnumSet type, Object... tickData) {
                  if (!Metrics.this.stopped) {
                     if (!Metrics.this.isOptOut()) {
                        if (this.thrd == null) {
                           this.thrd = new Thread(new Runnable() {
                              public void run() {
                                 try {
                                    Metrics.this.postPlugin(!firstPost);
                                    firstPost = false;
                                 } catch (IOException e) {
                                    if (Metrics.this.debug) {
                                       FMLLog.info("[Metrics] Exception - %s", new Object[]{e.getMessage()});
                                    }
                                 } finally {
                                    thrd = null;
                                 }

                              }
                           });
                           this.thrd.start();
                        }

                     } else {
                        for(Graph graph : Metrics.this.graphs) {
                           graph.onOptOut();
                        }

                        Metrics.this.stopped = true;
                     }
                  }
               }

               public EnumSet ticks() {
                  return EnumSet.of(TickType.SERVER);
               }

               public String getLabel() {
                  return Metrics.this.modname + " Metrics";
               }

               public int nextTickSpacing() {
                  return this.firstPost ? 100 : 18000;
               }
            };
            TickRegistry.registerScheduledTickHandler(this.task, Side.SERVER);
            return true;
         }
      }
   }

   public void stop() {
      this.stopped = true;
   }

   public boolean isOptOut() {
      this.configuration.load();
      return this.configuration.get("general", "opt-out", false).getBoolean(false);
   }

   public void enable() throws IOException {
      if (this.isOptOut()) {
         this.configuration.getCategory("general").get("opt-out").set("false");
         this.configuration.save();
      }

      if (this.task == null) {
         this.start();
      }

   }

   public void disable() throws IOException {
      if (!this.isOptOut()) {
         this.configuration.getCategory("general").get("opt-out").set("true");
         this.configuration.save();
      }

   }

   public File getConfigFile() {
      return new File(Loader.instance().getConfigDir(), "PluginMetrics.cfg");
   }

   private void postPlugin(boolean isPing) throws IOException {
      String pluginName = this.modname;
      boolean onlineMode = MinecraftServer.func_71276_C().func_71266_T();
      String pluginVersion = this.modversion;
      String serverVersion;
      if (MinecraftServer.func_71276_C().func_71262_S()) {
         serverVersion = "MinecraftForge (MC: " + MinecraftServer.func_71276_C().func_71249_w() + ")";
      } else {
         serverVersion = "MinecraftForgeSSP (MC: " + MinecraftServer.func_71276_C().func_71249_w() + ")";
      }

      int playersOnline = MinecraftServer.func_71276_C().func_71233_x();
      StringBuilder json = new StringBuilder(1024);
      json.append('{');
      appendJSONPair(json, "guid", this.guid);
      appendJSONPair(json, "plugin_version", pluginVersion);
      appendJSONPair(json, "server_version", serverVersion);
      appendJSONPair(json, "players_online", Integer.toString(playersOnline));
      String osname = System.getProperty("os.name");
      String osarch = System.getProperty("os.arch");
      String osversion = System.getProperty("os.version");
      String java_version = System.getProperty("java.version");
      int coreCount = Runtime.getRuntime().availableProcessors();
      if (osarch.equals("amd64")) {
         osarch = "x86_64";
      }

      appendJSONPair(json, "osname", osname);
      appendJSONPair(json, "osarch", osarch);
      appendJSONPair(json, "osversion", osversion);
      appendJSONPair(json, "cores", Integer.toString(coreCount));
      appendJSONPair(json, "auth_mode", onlineMode ? "1" : "0");
      appendJSONPair(json, "java_version", java_version);
      if (isPing) {
         appendJSONPair(json, "ping", "1");
      }

      if (this.graphs.size() > 0) {
         synchronized(this.graphs) {
            json.append(',');
            json.append('"');
            json.append("graphs");
            json.append('"');
            json.append(':');
            json.append('{');
            boolean firstGraph = true;

            for(Graph graph : this.graphs) {
               StringBuilder graphJson = new StringBuilder();
               graphJson.append('{');

               for(Plotter plotter : graph.getPlotters()) {
                  appendJSONPair(graphJson, plotter.getColumnName(), Integer.toString(plotter.getValue()));
               }

               graphJson.append('}');
               if (!firstGraph) {
                  json.append(',');
               }

               json.append(escapeJSON(graph.getName()));
               json.append(':');
               json.append(graphJson);
               firstGraph = false;
            }

            json.append('}');
         }
      }

      json.append('}');
      URL url = new URL("http://report.mcstats.org" + String.format("/plugin/%s", urlEncode(pluginName)));
      URLConnection connection;
      if (this.isMineshafterPresent()) {
         connection = url.openConnection(Proxy.NO_PROXY);
      } else {
         connection = url.openConnection();
      }

      byte[] uncompressed = json.toString().getBytes();
      byte[] compressed = gzip(json.toString());
      connection.addRequestProperty("User-Agent", "MCStats/7");
      connection.addRequestProperty("Content-Type", "application/json");
      connection.addRequestProperty("Content-Encoding", "gzip");
      connection.addRequestProperty("Content-Length", Integer.toString(compressed.length));
      connection.addRequestProperty("Accept", "application/json");
      connection.addRequestProperty("Connection", "close");
      connection.setDoOutput(true);
      if (this.debug) {
         System.out.println("[Metrics] Prepared request for " + pluginName + " uncompressed=" + uncompressed.length + " compressed=" + compressed.length);
      }

      OutputStream os = connection.getOutputStream();
      os.write(compressed);
      os.flush();
      BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String response = reader.readLine();
      os.close();
      reader.close();
      if (response != null && !response.startsWith("ERR") && !response.startsWith("7")) {
         if (response.equals("1") || response.contains("This is your first update this hour")) {
            synchronized(this.graphs) {
               for(Graph graph : this.graphs) {
                  for(Plotter plotter : graph.getPlotters()) {
                     plotter.reset();
                  }
               }
            }
         }

      } else {
         if (response == null) {
            response = "null";
         } else if (response.startsWith("7")) {
            response = response.substring(response.startsWith("7,") ? 2 : 1);
         }

         throw new IOException(response);
      }
   }

   public static byte[] gzip(String input) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      GZIPOutputStream gzos = null;

      try {
         gzos = new GZIPOutputStream(baos);
         gzos.write(input.getBytes("UTF-8"));
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         if (gzos != null) {
            try {
               gzos.close();
            } catch (IOException var11) {
            }
         }

      }

      return baos.toByteArray();
   }

   private boolean isMineshafterPresent() {
      try {
         Class.forName("mineshafter.MineServer");
         return true;
      } catch (Exception var2) {
         return false;
      }
   }

   private static void appendJSONPair(StringBuilder json, String key, String value) throws UnsupportedEncodingException {
      boolean isValueNumeric = false;

      try {
         if (value.equals("0") || !value.endsWith("0")) {
            Double.parseDouble(value);
            isValueNumeric = true;
         }
      } catch (NumberFormatException var5) {
         isValueNumeric = false;
      }

      if (json.charAt(json.length() - 1) != '{') {
         json.append(',');
      }

      json.append(escapeJSON(key));
      json.append(':');
      if (isValueNumeric) {
         json.append(value);
      } else {
         json.append(escapeJSON(value));
      }

   }

   private static String escapeJSON(String text) {
      StringBuilder builder = new StringBuilder();
      builder.append('"');

      for(int index = 0; index < text.length(); ++index) {
         char chr = text.charAt(index);
         switch (chr) {
            case '\b':
               builder.append("\\b");
               break;
            case '\t':
               builder.append("\\t");
               break;
            case '\n':
               builder.append("\\n");
               break;
            case '\r':
               builder.append("\\r");
               break;
            case '"':
            case '\\':
               builder.append('\\');
               builder.append(chr);
               break;
            default:
               if (chr < ' ') {
                  String t = "000" + Integer.toHexString(chr);
                  builder.append("\\u" + t.substring(t.length() - 4));
               } else {
                  builder.append(chr);
               }
         }
      }

      builder.append('"');
      return builder.toString();
   }

   private static String urlEncode(String text) throws UnsupportedEncodingException {
      return URLEncoder.encode(text, "UTF-8");
   }

   public static class Graph {
      private final String name;
      private final Set plotters;

      private Graph(String name) {
         super();
         this.plotters = new LinkedHashSet();
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      public void addPlotter(Plotter plotter) {
         this.plotters.add(plotter);
      }

      public void removePlotter(Plotter plotter) {
         this.plotters.remove(plotter);
      }

      public Set getPlotters() {
         return Collections.unmodifiableSet(this.plotters);
      }

      public int hashCode() {
         return this.name.hashCode();
      }

      public boolean equals(Object object) {
         if (!(object instanceof Graph)) {
            return false;
         } else {
            Graph graph = (Graph)object;
            return graph.name.equals(this.name);
         }
      }

      protected void onOptOut() {
      }
   }

   public abstract static class Plotter {
      private final String name;

      public Plotter() {
         this("Default");
      }

      public Plotter(String name) {
         super();
         this.name = name;
      }

      public abstract int getValue();

      public String getColumnName() {
         return this.name;
      }

      public void reset() {
      }

      public int hashCode() {
         return this.getColumnName().hashCode();
      }

      public boolean equals(Object object) {
         if (!(object instanceof Plotter)) {
            return false;
         } else {
            Plotter plotter = (Plotter)object;
            return plotter.name.equals(this.name) && plotter.getValue() == this.getValue();
         }
      }
   }
}
