package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.bukkit.Server;
import org.bukkit.WeatherType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandpweather extends EssentialsCommand {
   public static final Set getAliases = new HashSet();
   public static final Map weatherAliases = new HashMap();

   public Commandpweather() {
      super("pweather");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      String userSelector = null;
      if (args.length == 2) {
         userSelector = args[1];
      }

      Set<User> users = this.getUsers(server, sender, userSelector);
      if (args.length == 0) {
         this.getUsersWeather(sender, users);
      } else if (getAliases.contains(args[0])) {
         this.getUsersWeather(sender, users);
      } else {
         User user = this.ess.getUser(sender);
         if (user != null && (!users.contains(user) || users.size() > 1) && !user.isAuthorized("essentials.pweather.others")) {
            user.sendMessage(I18n._("pWeatherOthersPermission"));
         } else {
            this.setUsersWeather(sender, users, args[0].toLowerCase());
         }
      }
   }

   private void getUsersWeather(CommandSender sender, Collection users) {
      if (users.size() > 1) {
         sender.sendMessage(I18n._("pWeatherPlayers"));
      }

      for(User user : users) {
         if (user.getPlayerWeather() == null) {
            sender.sendMessage(I18n._("pWeatherNormal", user.getName()));
         } else {
            sender.sendMessage(I18n._("pWeatherCurrent", user.getName(), user.getPlayerWeather().toString().toLowerCase(Locale.ENGLISH)));
         }
      }

   }

   private void setUsersWeather(CommandSender sender, Collection users, String weatherType) throws Exception {
      StringBuilder msg = new StringBuilder();

      for(User user : users) {
         if (msg.length() > 0) {
            msg.append(", ");
         }

         msg.append(user.getName());
      }

      if (weatherType.equalsIgnoreCase("reset")) {
         for(User user : users) {
            user.resetPlayerWeather();
         }

         sender.sendMessage(I18n._("pWeatherReset", msg));
      } else {
         if (!weatherAliases.containsKey(weatherType)) {
            throw new NotEnoughArgumentsException(I18n._("pWeatherInvalidAlias"));
         }

         for(User user : users) {
            user.setPlayerWeather((WeatherType)weatherAliases.get(weatherType));
         }

         sender.sendMessage(I18n._("pWeatherSet", weatherType, msg.toString()));
      }

   }

   private Set getUsers(Server server, CommandSender sender, String selector) throws Exception {
      Set<User> users = new TreeSet(new UserNameComparator());
      if (selector == null) {
         User user = this.ess.getUser(sender);
         if (user == null) {
            for(Player player : server.getOnlinePlayers()) {
               users.add(this.ess.getUser(player));
            }
         } else {
            users.add(user);
         }

         return users;
      } else {
         User user = null;
         List<Player> matchedPlayers = server.matchPlayer(selector);
         if (!matchedPlayers.isEmpty()) {
            user = this.ess.getUser(matchedPlayers.get(0));
         }

         if (user != null) {
            users.add(user);
         } else {
            if (!selector.equalsIgnoreCase("*") && !selector.equalsIgnoreCase("all")) {
               throw new PlayerNotFoundException();
            }

            for(Player player : server.getOnlinePlayers()) {
               users.add(this.ess.getUser(player));
            }
         }

         return users;
      }
   }

   static {
      getAliases.add("get");
      getAliases.add("list");
      getAliases.add("show");
      getAliases.add("display");
      weatherAliases.put("sun", WeatherType.CLEAR);
      weatherAliases.put("clear", WeatherType.CLEAR);
      weatherAliases.put("storm", WeatherType.DOWNFALL);
      weatherAliases.put("thunder", WeatherType.DOWNFALL);
   }
}
