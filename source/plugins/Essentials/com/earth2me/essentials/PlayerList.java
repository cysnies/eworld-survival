package com.earth2me.essentials;

import com.earth2me.essentials.utils.FormatUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class PlayerList {
   public PlayerList() {
      super();
   }

   public static String listUsers(IEssentials ess, List users, String seperator) {
      StringBuilder groupString = new StringBuilder();
      Collections.sort(users);
      boolean needComma = false;

      for(User user : users) {
         if (needComma) {
            groupString.append(seperator);
         }

         needComma = true;
         if (user.isAfk()) {
            groupString.append(I18n._("listAfkTag"));
         }

         if (user.isHidden()) {
            groupString.append(I18n._("listHiddenTag"));
         }

         user.setDisplayNick();
         groupString.append(user.getDisplayName());
         groupString.append("§f");
      }

      return groupString.toString();
   }

   public static String listSummary(IEssentials ess, boolean showHidden) {
      Server server = ess.getServer();
      int playerHidden = 0;

      for(Player onlinePlayer : server.getOnlinePlayers()) {
         if (ess.getUser(onlinePlayer).isHidden()) {
            ++playerHidden;
         }
      }

      String online;
      if (showHidden && playerHidden > 0) {
         online = I18n._("listAmountHidden", server.getOnlinePlayers().length - playerHidden, playerHidden, server.getMaxPlayers());
      } else {
         online = I18n._("listAmount", server.getOnlinePlayers().length - playerHidden, server.getMaxPlayers());
      }

      return online;
   }

   public static Map getPlayerLists(IEssentials ess, boolean showHidden) {
      Server server = ess.getServer();
      Map<String, List<User>> playerList = new HashMap();

      for(Player onlinePlayer : server.getOnlinePlayers()) {
         User onlineUser = ess.getUser(onlinePlayer);
         if (!onlineUser.isHidden() || showHidden) {
            String group = FormatUtil.stripFormat(FormatUtil.stripEssentialsFormat(onlineUser.getGroup().toLowerCase()));
            List<User> list = (List)playerList.get(group);
            if (list == null) {
               list = new ArrayList();
               playerList.put(group, list);
            }

            list.add(onlineUser);
         }
      }

      return playerList;
   }

   public static List getMergedList(IEssentials ess, Map playerList, String groupName) {
      Set<String> configGroups = ess.getSettings().getListGroupConfig().keySet();
      List<User> users = new ArrayList();

      for(String configGroup : configGroups) {
         if (configGroup.equalsIgnoreCase(groupName)) {
            String[] groupValues = ess.getSettings().getListGroupConfig().get(configGroup).toString().trim().split(" ");

            for(String groupValue : groupValues) {
               groupValue = groupValue.toLowerCase(Locale.ENGLISH);
               if (groupValue != null && !groupValue.equals("")) {
                  List<User> u = (List)playerList.get(groupValue.trim());
                  if (u != null && !u.isEmpty()) {
                     playerList.remove(groupValue);
                     users.addAll(u);
                  }
               }
            }
         }
      }

      return users;
   }

   public static String listGroupUsers(IEssentials ess, Map playerList, String groupName) throws Exception {
      List<User> users = getMergedList(ess, playerList, groupName);
      List<User> groupUsers = (List)playerList.get(groupName);
      if (groupUsers != null && !groupUsers.isEmpty()) {
         users.addAll(groupUsers);
      }

      if (users != null && !users.isEmpty()) {
         StringBuilder displayGroupName = new StringBuilder();
         displayGroupName.append(Character.toTitleCase(groupName.charAt(0)));
         displayGroupName.append(groupName.substring(1));
         return outputFormat(displayGroupName.toString(), listUsers(ess, users, ", "));
      } else {
         throw new Exception(I18n._("groupDoesNotExist"));
      }
   }

   public static String outputFormat(String group, String message) {
      StringBuilder outputString = new StringBuilder();
      outputString.append(I18n._("listGroupTag", FormatUtil.replaceFormat(group)));
      outputString.append(message);
      return outputString.toString();
   }
}
