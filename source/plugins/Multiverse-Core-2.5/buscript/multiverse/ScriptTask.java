package buscript.multiverse;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bukkit.plugin.Plugin;

class ScriptTask implements Runnable {
   private Buscript buscript;
   private Plugin plugin;
   private int id = -1;

   ScriptTask(Buscript buscript) {
      super();
      this.plugin = buscript.getPlugin();
      this.buscript = buscript;
   }

   void start() {
      this.id = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, this, 20L, 20L);
   }

   void kill() {
      this.plugin.getServer().getScheduler().cancelTask(this.id);
   }

   public void run() {
      if (!this.buscript.runTasks) {
         this.kill();
      } else {
         long time = System.currentTimeMillis();

         for(Map.Entry entry : this.buscript.delayedScripts.entrySet()) {
            boolean removed = false;
            Iterator<Map<String, Object>> scriptsIt = ((List)entry.getValue()).iterator();

            while(scriptsIt.hasNext()) {
               Map<String, Object> script = (Map)scriptsIt.next();
               if (script.get("time") != null) {
                  try {
                     long scriptTime = (Long)script.get("time");
                     if (time >= scriptTime) {
                        if (script.get("file") != null) {
                           final File scriptFile = new File(script.get("file").toString());
                           if (scriptFile.exists()) {
                              try {
                                 final List<Map<String, Object>> replacements = (List)script.get("replacements");
                                 final Map<String, Object> metaData = (Map)script.get("metaData");
                                 this.buscript.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                                    public void run() {
                                       ScriptTask.this.buscript.executeDelayedScript(scriptFile, replacements, metaData);
                                    }
                                 });
                                 scriptsIt.remove();
                                 removed = true;
                              } catch (ClassCastException var14) {
                                 this.plugin.getLogger().warning("Invalid delayed script entry");
                                 scriptsIt.remove();
                                 removed = true;
                              }
                           } else {
                              try {
                                 scriptFile.createNewFile();
                              } catch (IOException var16) {
                              }

                              if (scriptFile.exists()) {
                                 try {
                                    final List<Map<String, Object>> replacements = (List)script.get("replacements");
                                    final Map<String, Object> metaData = (Map)script.get("metaData");
                                    this.buscript.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                                       public void run() {
                                          ScriptTask.this.buscript.executeDelayedScript(scriptFile, replacements, metaData);
                                       }
                                    });
                                    scriptsIt.remove();
                                    removed = true;
                                 } catch (ClassCastException var15) {
                                    scriptsIt.remove();
                                    removed = true;
                                    System.out.println("could not cast");
                                 }
                              } else {
                                 this.plugin.getLogger().warning("Missing script file: " + scriptFile);
                                 scriptsIt.remove();
                                 removed = true;
                              }
                           }
                        } else {
                           this.plugin.getLogger().warning("Invalid delayed script entry");
                           scriptsIt.remove();
                           removed = true;
                        }
                     }
                  } catch (NumberFormatException var17) {
                     this.plugin.getLogger().warning("Invalid delayed script entry");
                     scriptsIt.remove();
                     removed = true;
                  }
               } else {
                  this.plugin.getLogger().warning("Invalid delayed script entry");
                  scriptsIt.remove();
                  removed = true;
               }
            }

            if (removed) {
               this.buscript.saveData();
            }
         }

      }
   }
}
