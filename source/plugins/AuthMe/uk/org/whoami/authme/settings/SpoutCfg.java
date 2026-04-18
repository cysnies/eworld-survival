package uk.org.whoami.authme.settings;

import java.io.File;
import java.util.ArrayList;

public class SpoutCfg extends CustomConfiguration {
   private static SpoutCfg instance = null;

   public SpoutCfg(File file) {
      super(file);
      this.loadDefaults();
      this.load();
      this.save();
   }

   private void loadDefaults() {
      this.set("Spout GUI enabled", true);
      this.set("LoginScreen.enabled", true);
      this.set("LoginScreen.exit button", "Quit");
      this.set("LoginScreen.exit message", "Good Bye");
      this.set("LoginScreen.login button", "Login");
      this.set("LoginScreen.title", "LOGIN");
      this.set("LoginScreen.text", new ArrayList() {
         {
            this.add("Sample text");
            this.add("Change this at spout.yml");
            this.add("--- AuthMe Reloaded by ---");
            this.add("Xephi59");
         }
      });
   }

   public static SpoutCfg getInstance() {
      if (instance == null) {
         instance = new SpoutCfg(new File("plugins/AuthMe", "spout.yml"));
      }

      return instance;
   }
}
