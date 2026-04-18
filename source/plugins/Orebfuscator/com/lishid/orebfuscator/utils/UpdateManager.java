package com.lishid.orebfuscator.utils;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import java.io.File;

public class UpdateManager {
   public Updater updater;

   public UpdateManager() {
      super();
   }

   public void Initialize(Orebfuscator plugin, File file) {
      this.updater = new Updater(plugin, Orebfuscator.logger, "orebfuscator", file);
      plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
         public void run() {
            if (OrebfuscatorConfig.CheckForUpdates) {
               Updater.UpdateResult result = UpdateManager.this.updater.update(Updater.UpdateType.DEFAULT);
               if (result != Updater.UpdateResult.NO_UPDATE) {
                  Orebfuscator.log(result.toString());
               }
            }

         }
      }, 0L, 1200000L);
   }
}
