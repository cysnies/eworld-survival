package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.StringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Commandpowertool extends EssentialsCommand {
   public Commandpowertool() {
      super("powertool");
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      String command = getFinalArg(args, 0);
      ItemStack itemStack = user.getItemInHand();
      this.powertool(server, user.getBase(), user, commandLabel, itemStack, command);
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 3) {
         throw new Exception("When running from console, usage is: /" + commandLabel + " <player> <itemid> <command>");
      } else {
         User user = this.getPlayer(server, args, 0, true, true);
         ItemStack itemStack = this.ess.getItemDb().get(args[1]);
         String command = getFinalArg(args, 2);
         this.powertool(server, sender, user, commandLabel, itemStack, command);
      }
   }

   protected void powertool(Server server, CommandSender sender, User user, String commandLabel, ItemStack itemStack, String command) throws Exception {
      if (command != null && command.equalsIgnoreCase("d:")) {
         user.clearAllPowertools();
         sender.sendMessage(I18n._("powerToolClearAll"));
      } else if (itemStack != null && itemStack.getType() != Material.AIR) {
         String itemName = itemStack.getType().toString().toLowerCase(Locale.ENGLISH).replaceAll("_", " ");
         List<String> powertools = user.getPowertool(itemStack);
         if (command != null && !command.isEmpty()) {
            if (command.equalsIgnoreCase("l:")) {
               if (powertools != null && !powertools.isEmpty()) {
                  sender.sendMessage(I18n._("powerToolList", StringUtil.joinList(powertools), itemName));
                  throw new NoChargeException();
               }

               throw new Exception(I18n._("powerToolListEmpty", itemName));
            }

            if (command.startsWith("r:")) {
               command = command.substring(2);
               if (!powertools.contains(command)) {
                  throw new Exception(I18n._("powerToolNoSuchCommandAssigned", command, itemName));
               }

               powertools.remove(command);
               sender.sendMessage(I18n._("powerToolRemove", command, itemName));
            } else {
               if (command.startsWith("a:")) {
                  if (sender instanceof Player && !this.ess.getUser(sender).isAuthorized("essentials.powertool.append")) {
                     throw new Exception(I18n._("noPerm", "essentials.powertool.append"));
                  }

                  command = command.substring(2);
                  if (powertools.contains(command)) {
                     throw new Exception(I18n._("powerToolAlreadySet", command, itemName));
                  }
               } else if (powertools != null && !powertools.isEmpty()) {
                  powertools.clear();
               } else {
                  powertools = new ArrayList();
               }

               powertools.add(command);
               sender.sendMessage(I18n._("powerToolAttach", StringUtil.joinList(powertools), itemName));
            }
         } else {
            if (powertools != null) {
               powertools.clear();
            }

            sender.sendMessage(I18n._("powerToolRemoveAll", itemName));
         }

         if (!user.arePowerToolsEnabled()) {
            user.setPowerToolsEnabled(true);
            user.sendMessage(I18n._("powerToolsEnabled"));
         }

         user.setPowertool(itemStack, powertools);
      } else {
         throw new Exception(I18n._("powerToolAir"));
      }
   }
}
