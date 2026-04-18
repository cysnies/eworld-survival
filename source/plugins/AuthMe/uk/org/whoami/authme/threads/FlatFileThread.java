package uk.org.whoami.authme.threads;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.ConsoleLogger;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.settings.Settings;

public class FlatFileThread extends Thread implements DataSource {
   private File source;

   public FlatFileThread() {
      super();
   }

   public void run() {
      this.source = new File("./plugins/AuthMe/auths.db");

      try {
         this.source.createNewFile();
      } catch (IOException e) {
         ConsoleLogger.showError(e.getMessage());
         if (Settings.isStopEnabled) {
            ConsoleLogger.showError("Can't use FLAT FILE... SHUTDOWN...");
            AuthMe.getInstance().getServer().shutdown();
         }

         if (!Settings.isStopEnabled) {
            AuthMe.getInstance().getServer().getPluginManager().disablePlugin(AuthMe.getInstance());
         }

      }
   }

   public synchronized boolean isAuthAvailable(String user) {
      BufferedReader br = null;

      try {
         br = new BufferedReader(new FileReader(this.source));

         String line;
         while((line = br.readLine()) != null) {
            String[] args = line.split(":");
            if (args.length > 1 && args[0].equals(user)) {
               return true;
            }
         }

         return false;
      } catch (FileNotFoundException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } catch (IOException ex) {
         ConsoleLogger.showError(ex.getMessage());
         return false;
      } finally {
         if (br != null) {
            try {
               br.close();
            } catch (IOException var15) {
            }
         }

      }

      return false;
   }

   public synchronized boolean saveAuth(PlayerAuth auth) {
      if (this.isAuthAvailable(auth.getNickname())) {
         return false;
      } else {
         BufferedWriter bw = null;

         try {
            if (auth.getQuitLocY() == 0) {
               bw = new BufferedWriter(new FileWriter(this.source, true));
               bw.write(auth.getNickname() + ":" + auth.getHash() + ":" + auth.getIp() + ":" + auth.getLastLogin() + "\n");
            } else {
               bw = new BufferedWriter(new FileWriter(this.source, true));
               bw.write(auth.getNickname() + ":" + auth.getHash() + ":" + auth.getIp() + ":" + auth.getLastLogin() + ":" + auth.getQuitLocX() + ":" + auth.getQuitLocY() + ":" + auth.getQuitLocZ() + ":" + auth.getWorld() + "\n");
            }

            return true;
         } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
         } finally {
            if (bw != null) {
               try {
                  bw.close();
               } catch (IOException var11) {
               }
            }

         }

         return false;
      }
   }

   public synchronized boolean updatePassword(PlayerAuth auth) {
      if (!this.isAuthAvailable(auth.getNickname())) {
         return false;
      } else {
         PlayerAuth newAuth = null;
         BufferedReader br = null;

         label128: {
            try {
               br = new BufferedReader(new FileReader(this.source));
               String line = "";

               String[] args;
               do {
                  if ((line = br.readLine()) == null) {
                     break label128;
                  }

                  args = line.split(":");
               } while(!args[0].equals(auth.getNickname()));

               newAuth = new PlayerAuth(args[0], auth.getHash(), args[2], Long.parseLong(args[3]));
               break label128;
            } catch (FileNotFoundException ex) {
               ConsoleLogger.showError(ex.getMessage());
            } catch (IOException ex) {
               ConsoleLogger.showError(ex.getMessage());
               return false;
            } finally {
               if (br != null) {
                  try {
                     br.close();
                  } catch (IOException var15) {
                  }
               }

            }

            return false;
         }

         this.removeAuth(auth.getNickname());
         this.saveAuth(newAuth);
         return true;
      }
   }

   public boolean updateSession(PlayerAuth auth) {
      if (!this.isAuthAvailable(auth.getNickname())) {
         return false;
      } else {
         PlayerAuth newAuth = null;
         BufferedReader br = null;

         label128: {
            try {
               br = new BufferedReader(new FileReader(this.source));
               String line = "";

               String[] args;
               do {
                  if ((line = br.readLine()) == null) {
                     break label128;
                  }

                  args = line.split(":");
               } while(!args[0].equals(auth.getNickname()));

               newAuth = new PlayerAuth(args[0], args[1], auth.getIp(), auth.getLastLogin());
               break label128;
            } catch (FileNotFoundException ex) {
               ConsoleLogger.showError(ex.getMessage());
            } catch (IOException ex) {
               ConsoleLogger.showError(ex.getMessage());
               return false;
            } finally {
               if (br != null) {
                  try {
                     br.close();
                  } catch (IOException var15) {
                  }
               }

            }

            return false;
         }

         this.removeAuth(auth.getNickname());
         this.saveAuth(newAuth);
         return true;
      }
   }

   public boolean updateQuitLoc(PlayerAuth auth) {
      if (!this.isAuthAvailable(auth.getNickname())) {
         return false;
      } else {
         PlayerAuth newAuth = null;
         BufferedReader br = null;

         label128: {
            try {
               br = new BufferedReader(new FileReader(this.source));
               String line = "";

               String[] args;
               do {
                  if ((line = br.readLine()) == null) {
                     break label128;
                  }

                  args = line.split(":");
               } while(!args[0].equals(auth.getNickname()));

               newAuth = new PlayerAuth(args[0], args[1], args[2], Long.parseLong(args[3]), auth.getQuitLocX(), auth.getQuitLocY(), auth.getQuitLocZ(), auth.getWorld());
               break label128;
            } catch (FileNotFoundException ex) {
               ConsoleLogger.showError(ex.getMessage());
            } catch (IOException ex) {
               ConsoleLogger.showError(ex.getMessage());
               return false;
            } finally {
               if (br != null) {
                  try {
                     br.close();
                  } catch (IOException var15) {
                  }
               }

            }

            return false;
         }

         this.removeAuth(auth.getNickname());
         this.saveAuth(newAuth);
         return true;
      }
   }

   public int getIps(String ip) {
      BufferedReader br = null;
      int countIp = 0;

      try {
         br = new BufferedReader(new FileReader(this.source));

         String line;
         while((line = br.readLine()) != null) {
            String[] args = line.split(":");
            if (args.length > 3 && args[2].equals(ip)) {
               ++countIp;
            }
         }

         int var7 = countIp;
         return var7;
      } catch (FileNotFoundException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } catch (IOException ex) {
         ConsoleLogger.showError(ex.getMessage());
         return 0;
      } finally {
         if (br != null) {
            try {
               br.close();
            } catch (IOException var16) {
            }
         }

      }

      return 0;
   }

   public int purgeDatabase(long until) {
      BufferedReader br = null;
      BufferedWriter bw = null;
      ArrayList<String> lines = new ArrayList();
      int cleared = 0;

      int var30;
      try {
         br = new BufferedReader(new FileReader(this.source));

         String line;
         while((line = br.readLine()) != null) {
            String[] args = line.split(":");
            if (args.length == 4 && Long.parseLong(args[3]) >= until) {
               lines.add(line);
            } else {
               ++cleared;
            }
         }

         bw = new BufferedWriter(new FileWriter(this.source));

         for(String l : lines) {
            bw.write(l + "\n");
         }

         return cleared;
      } catch (FileNotFoundException ex) {
         ConsoleLogger.showError(ex.getMessage());
         var30 = cleared;
      } catch (IOException ex) {
         ConsoleLogger.showError(ex.getMessage());
         var30 = cleared;
         return var30;
      } finally {
         if (br != null) {
            try {
               br.close();
            } catch (IOException var25) {
            }
         }

         if (bw != null) {
            try {
               bw.close();
            } catch (IOException var24) {
            }
         }

      }

      return var30;
   }

   public synchronized boolean removeAuth(String user) {
      if (!this.isAuthAvailable(user)) {
         return false;
      } else {
         BufferedReader br = null;
         BufferedWriter bw = null;
         ArrayList<String> lines = new ArrayList();

         try {
            br = new BufferedReader(new FileReader(this.source));

            String line;
            while((line = br.readLine()) != null) {
               String[] args = line.split(":");
               if (args.length > 1 && !args[0].equals(user)) {
                  lines.add(line);
               }
            }

            bw = new BufferedWriter(new FileWriter(this.source));

            for(String l : lines) {
               bw.write(l + "\n");
            }

            return true;
         } catch (FileNotFoundException ex) {
            ConsoleLogger.showError(ex.getMessage());
         } catch (IOException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
         } finally {
            if (br != null) {
               try {
                  br.close();
               } catch (IOException var22) {
               }
            }

            if (bw != null) {
               try {
                  bw.close();
               } catch (IOException var21) {
               }
            }

         }

         return false;
      }
   }

   public synchronized PlayerAuth getAuth(String user) {
      BufferedReader br = null;

      try {
         br = new BufferedReader(new FileReader(this.source));

         String line;
         while((line = br.readLine()) != null) {
            String[] args = line.split(":");
            if (args[0].equals(user)) {
               switch (args.length) {
                  case 2:
                     PlayerAuth var27 = new PlayerAuth(args[0], args[1], "198.18.0.1", 0L);
                     return var27;
                  case 3:
                     PlayerAuth var26 = new PlayerAuth(args[0], args[1], args[2], 0L);
                     return var26;
                  case 4:
                     PlayerAuth var25 = new PlayerAuth(args[0], args[1], args[2], Long.parseLong(args[3]));
                     return var25;
                  case 5:
                  case 6:
                  default:
                     break;
                  case 7:
                     PlayerAuth var24 = new PlayerAuth(args[0], args[1], args[2], Long.parseLong(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), "unavailableworld");
                     return var24;
                  case 8:
                     PlayerAuth var6 = new PlayerAuth(args[0], args[1], args[2], Long.parseLong(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), args[7]);
                     return var6;
               }
            }
         }

         return null;
      } catch (FileNotFoundException ex) {
         ConsoleLogger.showError(ex.getMessage());
         return null;
      } catch (IOException ex) {
         ConsoleLogger.showError(ex.getMessage());
         return null;
      } finally {
         if (br != null) {
            try {
               br.close();
            } catch (IOException var20) {
            }
         }

      }
   }

   public synchronized void close() {
   }

   public void reload() {
   }

   public boolean updateEmail(PlayerAuth auth) {
      return false;
   }

   public boolean updateSalt(PlayerAuth auth) {
      return false;
   }

   public List getAllAuthsByName(PlayerAuth auth) {
      BufferedReader br = null;
      List<String> countIp = new ArrayList();

      List var20;
      try {
         br = new BufferedReader(new FileReader(this.source));

         String line;
         while((line = br.readLine()) != null) {
            String[] args = line.split(":");
            if (args.length > 3 && args[2].equals(auth.getIp())) {
               countIp.add(args[0]);
            }
         }

         var20 = countIp;
         return var20;
      } catch (FileNotFoundException ex) {
         ConsoleLogger.showError(ex.getMessage());
         var20 = new ArrayList();
      } catch (IOException ex) {
         ConsoleLogger.showError(ex.getMessage());
         var20 = new ArrayList();
         return var20;
      } finally {
         if (br != null) {
            try {
               br.close();
            } catch (IOException var16) {
            }
         }

      }

      return var20;
   }

   public List getAllAuthsByIp(String ip) {
      BufferedReader br = null;
      List<String> countIp = new ArrayList();

      List var20;
      try {
         br = new BufferedReader(new FileReader(this.source));

         String line;
         while((line = br.readLine()) != null) {
            String[] args = line.split(":");
            if (args.length > 3 && args[2].equals(ip)) {
               countIp.add(args[0]);
            }
         }

         var20 = countIp;
         return var20;
      } catch (FileNotFoundException ex) {
         ConsoleLogger.showError(ex.getMessage());
         var20 = new ArrayList();
      } catch (IOException ex) {
         ConsoleLogger.showError(ex.getMessage());
         var20 = new ArrayList();
         return var20;
      } finally {
         if (br != null) {
            try {
               br.close();
            } catch (IOException var16) {
            }
         }

      }

      return var20;
   }

   public List getAllAuthsByEmail(String email) {
      return new ArrayList();
   }

   public void purgeBanned(List banned) {
      BufferedReader br = null;
      BufferedWriter bw = null;
      ArrayList<String> lines = new ArrayList();

      try {
         br = new BufferedReader(new FileReader(this.source));

         String line;
         while((line = br.readLine()) != null) {
            String[] args = line.split(":");

            try {
               if (banned.contains(args[0])) {
                  lines.add(line);
               }
            } catch (NullPointerException var25) {
            } catch (ArrayIndexOutOfBoundsException var26) {
            }
         }

         bw = new BufferedWriter(new FileWriter(this.source));

         for(String l : lines) {
            bw.write(l + "\n");
         }

         return;
      } catch (FileNotFoundException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } catch (IOException ex) {
         ConsoleLogger.showError(ex.getMessage());
         return;
      } finally {
         if (br != null) {
            try {
               br.close();
            } catch (IOException var24) {
            }
         }

         if (bw != null) {
            try {
               bw.close();
            } catch (IOException var23) {
            }
         }

      }

   }
}
