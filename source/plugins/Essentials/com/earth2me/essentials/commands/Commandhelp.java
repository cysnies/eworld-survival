package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.textreader.HelpInput;
import com.earth2me.essentials.textreader.IText;
import com.earth2me.essentials.textreader.KeywordReplacer;
import com.earth2me.essentials.textreader.TextInput;
import com.earth2me.essentials.textreader.TextPager;
import com.earth2me.essentials.utils.NumberUtil;
import java.util.Locale;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class Commandhelp extends EssentialsCommand {
   public Commandhelp() {
      super("help");
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      String pageStr = args.length > 0 ? args[0] : null;
      String chapterPageStr = args.length > 1 ? args[1] : null;
      String command = commandLabel;
      IText input = new TextInput(user.getBase(), "help", false, this.ess);
      IText output;
      if (input.getLines().isEmpty()) {
         if (!NumberUtil.isInt(pageStr) && pageStr != null) {
            if (pageStr.length() > 26) {
               pageStr = pageStr.substring(0, 25);
            }

            output = new HelpInput(user, pageStr.toLowerCase(Locale.ENGLISH), this.ess);
            command = commandLabel.concat(" ").concat(pageStr);
            pageStr = chapterPageStr;
         } else {
            output = new HelpInput(user, "", this.ess);
         }

         chapterPageStr = null;
      } else {
         output = new KeywordReplacer(input, user.getBase(), this.ess);
      }

      TextPager pager = new TextPager(output);
      pager.showPage(pageStr, chapterPageStr, command, user.getBase());
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      sender.sendMessage(I18n._("helpConsole"));
   }
}
