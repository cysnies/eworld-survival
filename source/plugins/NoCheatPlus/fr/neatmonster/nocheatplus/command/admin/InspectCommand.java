package fr.neatmonster.nocheatplus.command.admin;

import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.players.DataManager;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

public class InspectCommand extends BaseCommand {
   private final DecimalFormat f1 = new DecimalFormat("#.#");

   public InspectCommand(JavaPlugin plugin) {
      super(plugin, "inspect", "nocheatplus.command.inspect");
   }

   public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
      if (args.length == 1) {
         if (!(sender instanceof Player)) {
            sender.sendMessage("[NoCheatPlus] Please specify a player to inspect.");
            return true;
         }

         args = new String[]{args[0], sender.getName()};
      }

      String c1;
      if (sender instanceof Player) {
         c1 = ChatColor.GRAY.toString();
      } else {
         c1 = "";
      }

      for(int i = 1; i < args.length; ++i) {
         Player player = DataManager.getPlayer(args[i].trim().toLowerCase());
         if (player == null) {
            sender.sendMessage("(Not online: " + args[i] + ")");
         } else {
            StringBuilder builder = new StringBuilder(256);
            builder.append(player.getName() + c1);
            builder.append(" (" + (player.isOnline() ? "online" : "offline") + (player.isDead() ? ",dead" : "") + (player.isValid() ? "" : ",invalid") + (player.isInsideVehicle() ? ",vehicle=" + player.getVehicle().getType() : "") + "):");
            builder.append(" health=" + this.f1.format(player.getHealth()) + "/" + this.f1.format(player.getMaxHealth()));
            builder.append(" food=" + player.getFoodLevel());
            if (player.getExp() > 0.0F) {
               builder.append(" explvl=" + this.f1.format((long)player.getExpToLevel()) + "(exp=" + this.f1.format((double)player.getExp()) + ")");
            }

            if (player.isFlying()) {
               builder.append(" flying");
            }

            if (player.getAllowFlight()) {
               builder.append(" allowflight");
            }

            builder.append(" flyspeed=" + player.getFlySpeed());
            builder.append(" walkspeed=" + player.getWalkSpeed());
            Collection<PotionEffect> effects = player.getActivePotionEffects();
            if (!effects.isEmpty()) {
               builder.append(" effects=");

               for(PotionEffect effect : effects) {
                  builder.append(effect.getType() + "@" + effect.getAmplifier() + ",");
               }
            }

            Location loc = player.getLocation();
            builder.append(" pos=" + loc.getWorld().getName() + "/" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
            sender.sendMessage(builder.toString());
         }
      }

      return true;
   }

   public List onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      return null;
   }
}
