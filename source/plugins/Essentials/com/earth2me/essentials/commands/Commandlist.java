package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.PlayerList;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import com.earth2me.essentials.utils.NumberUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandlist extends EssentialsCommand {
   public Commandlist() {
      super("list");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      boolean showHidden = true;
      if (sender instanceof Player) {
         showHidden = this.ess.getUser(sender).isAuthorized("essentials.list.hidden") || this.ess.getUser(sender).isAuthorized("essentials.vanish.interact");
      }

      sender.sendMessage(PlayerList.listSummary(this.ess, showHidden));
      Map<String, List<User>> playerList = PlayerList.getPlayerLists(this.ess, showHidden);
      if (args.length > 0) {
         sender.sendMessage(PlayerList.listGroupUsers(this.ess, playerList, args[0].toLowerCase()));
      } else {
         this.sendGroupedList(sender, commandLabel, playerList);
      }

   }

   private void sendGroupedList(CommandSender sender, String commandLabel, Map playerList) {
      Set<String> configGroups = this.ess.getSettings().getListGroupConfig().keySet();
      List<String> asterisk = new ArrayList();

      for(String oConfigGroup : configGroups) {
         String groupValue = this.ess.getSettings().getListGroupConfig().get(oConfigGroup).toString().trim();
         String configGroup = oConfigGroup.toLowerCase();
         if (groupValue.equals("*")) {
            asterisk.add(oConfigGroup);
         } else if (groupValue.equalsIgnoreCase("hidden")) {
            playerList.remove(groupValue);
         } else {
            List<User> outputUserList = new ArrayList();
            List<User> matchedList = (List)playerList.get(configGroup);
            if (NumberUtil.isInt(groupValue) && matchedList != null && !matchedList.isEmpty()) {
               playerList.remove(configGroup);
               outputUserList.addAll(matchedList);
               int limit = Integer.parseInt(groupValue);
               if (matchedList.size() > limit) {
                  sender.sendMessage(PlayerList.outputFormat(oConfigGroup, I18n._("groupNumber", matchedList.size(), commandLabel, FormatUtil.stripFormat(configGroup))));
               } else {
                  sender.sendMessage(PlayerList.outputFormat(oConfigGroup, PlayerList.listUsers(this.ess, outputUserList, ", ")));
               }
            } else {
               outputUserList = PlayerList.getMergedList(this.ess, playerList, configGroup);
               if (outputUserList != null && !outputUserList.isEmpty()) {
                  sender.sendMessage(PlayerList.outputFormat(oConfigGroup, PlayerList.listUsers(this.ess, outputUserList, ", ")));
               }
            }
         }
      }

      String[] onlineGroups = (String[])playerList.keySet().toArray(new String[0]);
      Arrays.sort(onlineGroups, String.CASE_INSENSITIVE_ORDER);
      if (!asterisk.isEmpty()) {
         List<User> asteriskUsers = new ArrayList();

         for(String onlineGroup : onlineGroups) {
            asteriskUsers.addAll((Collection)playerList.get(onlineGroup));
         }

         for(String key : asterisk) {
            playerList.put(key, asteriskUsers);
         }

         onlineGroups = (String[])asterisk.toArray(new String[0]);
      }

      for(String onlineGroup : onlineGroups) {
         List<User> users = (List)playerList.get(onlineGroup);
         String groupName = asterisk.isEmpty() ? ((User)users.get(0)).getGroup() : onlineGroup;
         if (this.ess.getPermissionsHandler().getName().equals("ConfigPermissions")) {
            groupName = I18n._("connectedPlayers");
         }

         if (users != null && !users.isEmpty()) {
            sender.sendMessage(PlayerList.outputFormat(groupName, PlayerList.listUsers(this.ess, users, ", ")));
         }
      }

   }
}
