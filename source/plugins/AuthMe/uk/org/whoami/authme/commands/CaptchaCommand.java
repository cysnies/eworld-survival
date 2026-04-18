package uk.org.whoami.authme.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.security.RandomString;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.Settings;

public class CaptchaCommand implements CommandExecutor {
   public AuthMe plugin;
   private Messages m = Messages.getInstance();
   public static RandomString rdm;

   static {
      rdm = new RandomString(Settings.captchaLength);
   }

   public CaptchaCommand(AuthMe plugin) {
      super();
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
      if (!(sender instanceof Player)) {
         return true;
      } else {
         Player player = (Player)sender;
         String name = player.getName().toLowerCase();
         if (args.length == 0) {
            player.sendMessage(this.m._("usage_captcha"));
            return true;
         } else if (PlayerCache.getInstance().isAuthenticated(name)) {
            player.sendMessage(this.m._("logged_in"));
            return true;
         } else if (!this.plugin.authmePermissible(player, "authme." + label.toLowerCase())) {
            player.sendMessage(this.m._("no_perm"));
            return true;
         } else if (!Settings.useCaptcha) {
            player.sendMessage(this.m._("usage_log"));
            return true;
         } else if (!this.plugin.cap.containsKey(name)) {
            player.sendMessage(this.m._("usage_log"));
            return true;
         } else if (Settings.useCaptcha && !args[0].equals(this.plugin.cap.get(name))) {
            this.plugin.cap.remove(name);
            this.plugin.cap.put(name, rdm.nextString());
            player.sendMessage(this.m._("wrong_captcha").replaceAll("THE_CAPTCHA", (String)this.plugin.cap.get(name)));
            return true;
         } else {
            try {
               this.plugin.captcha.remove(name);
               this.plugin.cap.remove(name);
            } catch (NullPointerException var8) {
            }

            player.sendMessage(this.m._("valid_captcha"));
            player.sendMessage(this.m._("login_msg"));
            return true;
         }
      }
   }
}
