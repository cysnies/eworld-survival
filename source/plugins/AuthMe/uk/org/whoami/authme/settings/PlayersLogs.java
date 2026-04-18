package uk.org.whoami.authme.settings;

import java.io.File;
import java.util.List;

public class PlayersLogs extends CustomConfiguration {
   private static PlayersLogs pllog = null;
   public static List players;

   public PlayersLogs() {
      super(new File("./plugins/AuthMe/players.yml"));
      pllog = this;
      this.load();
      this.save();
      players = this.getList("players");
   }

   public static PlayersLogs getInstance() {
      if (pllog == null) {
         pllog = new PlayersLogs();
      }

      return pllog;
   }
}
