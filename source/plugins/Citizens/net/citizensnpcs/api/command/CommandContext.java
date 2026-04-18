package net.citizensnpcs.api.command;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import net.citizensnpcs.api.command.exception.CommandException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandContext {
   protected String[] args;
   protected final Set flags;
   private Location location;
   private final CommandSender sender;
   protected final Map valueFlags;
   private static final Pattern FLAG = Pattern.compile("^-[a-zA-Z]+$");
   private static final Splitter LOCATION_SPLITTER = Splitter.on(Pattern.compile("[,]|[:]")).omitEmptyStrings();
   private static final Pattern VALUE_FLAG = Pattern.compile("^--[a-zA-Z0-9]+$");

   public CommandContext(CommandSender sender, String[] args) {
      super();
      this.flags = new HashSet();
      this.location = null;
      this.valueFlags = Maps.newHashMap();
      this.sender = sender;

      for(int i = 1; i < args.length; ++i) {
         args[i] = args[i].trim();
         if (args[i].length() != 0 && (args[i].charAt(0) == '\'' || args[i].charAt(0) == '"')) {
            char quote = args[i].charAt(0);
            String quoted = args[i].substring(1);
            if (quoted.length() > 0 && quoted.charAt(quoted.length() - 1) == quote) {
               args[i] = quoted.substring(0, quoted.length() - 1);
            } else {
               for(int inner = i + 1; inner < args.length; ++inner) {
                  if (!args[inner].isEmpty()) {
                     String test = args[inner].trim();
                     quoted = quoted + " " + test;
                     if (test.charAt(test.length() - 1) == quote) {
                        args[i] = quoted.substring(0, quoted.length() - 1);

                        for(int j = i + 1; j != inner; ++j) {
                           args[j] = "";
                        }
                        break;
                     }
                  }
               }
            }
         }
      }

      for(int var9 = 1; var9 < args.length; ++var9) {
         int length = args[var9].length();
         if (length != 0) {
            if (var9 + 1 < args.length && length > 2 && VALUE_FLAG.matcher(args[var9]).matches()) {
               int inner = var9 + 1;

               while(args[inner].length() == 0) {
                  ++inner;
                  if (inner >= args.length) {
                     inner = -1;
                     break;
                  }
               }

               if (inner != -1) {
                  this.valueFlags.put(args[var9].toLowerCase().substring(2), args[inner]);
                  args[var9] = "";
                  args[inner] = "";
               }
            } else if (FLAG.matcher(args[var9]).matches()) {
               for(int k = 1; k < args[var9].length(); ++k) {
                  this.flags.add(args[var9].charAt(k));
               }

               args[var9] = "";
            }
         }
      }

      List<String> copied = Lists.newArrayList();

      for(String arg : args) {
         arg = arg.trim();
         if (arg != null && !arg.isEmpty()) {
            copied.add(arg.trim());
         }
      }

      this.args = (String[])copied.toArray(new String[copied.size()]);
   }

   public CommandContext(String[] args) {
      this((CommandSender)null, args);
   }

   public int argsLength() {
      return this.args.length - 1;
   }

   public String getCommand() {
      return this.args[0];
   }

   public double getDouble(int index) throws NumberFormatException {
      return Double.parseDouble(this.args[index + 1]);
   }

   public double getDouble(int index, double def) throws NumberFormatException {
      return index + 1 < this.args.length ? Double.parseDouble(this.args[index + 1]) : def;
   }

   public String getFlag(String ch) {
      return (String)this.valueFlags.get(ch);
   }

   public String getFlag(String ch, String def) {
      String value = (String)this.valueFlags.get(ch);
      return value == null ? def : value;
   }

   public double getFlagDouble(String ch) throws NumberFormatException {
      return Double.parseDouble((String)this.valueFlags.get(ch));
   }

   public double getFlagDouble(String ch, double def) throws NumberFormatException {
      String value = (String)this.valueFlags.get(ch);
      return value == null ? def : Double.parseDouble(value);
   }

   public int getFlagInteger(String ch) throws NumberFormatException {
      return Integer.parseInt((String)this.valueFlags.get(ch));
   }

   public int getFlagInteger(String ch, int def) throws NumberFormatException {
      String value = (String)this.valueFlags.get(ch);
      return value == null ? def : Integer.parseInt(value);
   }

   public Set getFlags() {
      return this.flags;
   }

   public int getInteger(int index) throws NumberFormatException {
      return Integer.parseInt(this.args[index + 1]);
   }

   public int getInteger(int index, int def) throws NumberFormatException {
      if (index + 1 < this.args.length) {
         try {
            return Integer.parseInt(this.args[index + 1]);
         } catch (NumberFormatException var4) {
         }
      }

      return def;
   }

   public String getJoinedStrings(int initialIndex) {
      return this.getJoinedStrings(initialIndex, ' ');
   }

   public String getJoinedStrings(int initialIndex, char delimiter) {
      ++initialIndex;
      StringBuilder buffer = new StringBuilder(this.args[initialIndex]);

      for(int i = initialIndex + 1; i < this.args.length; ++i) {
         buffer.append(delimiter).append(this.args[i]);
      }

      return buffer.toString().trim();
   }

   public String[] getPaddedSlice(int index, int padding) {
      String[] slice = new String[this.args.length - index + padding];
      System.arraycopy(this.args, index, slice, padding, this.args.length - index);
      return slice;
   }

   public Location getSenderLocation() throws CommandException {
      if (this.location == null && this.sender != null) {
         if (this.sender instanceof Player) {
            this.location = ((Player)this.sender).getLocation();
         } else if (this.sender instanceof BlockCommandSender) {
            this.location = ((BlockCommandSender)this.sender).getBlock().getLocation();
         }

         if (this.hasValueFlag("location")) {
            this.location = parseLocation(this.location, this.getFlag("location"));
         }

         return this.location;
      } else {
         return this.location;
      }
   }

   public Location getSenderTargetBlockLocation() {
      if (this.sender == null) {
         return this.location;
      } else {
         if (this.sender instanceof Player) {
            this.location = ((Player)this.sender).getTargetBlock((HashSet)null, 50).getLocation();
         } else if (this.sender instanceof BlockCommandSender) {
            this.location = ((BlockCommandSender)this.sender).getBlock().getLocation();
         }

         return this.location;
      }
   }

   public String[] getSlice(int index) {
      String[] slice = new String[this.args.length - index];
      System.arraycopy(this.args, index, slice, 0, this.args.length - index);
      return slice;
   }

   public String getString(int index) {
      return this.args[index + 1];
   }

   public String getString(int index, String def) {
      return index + 1 < this.args.length ? this.args[index + 1] : def;
   }

   public Map getValueFlags() {
      return this.valueFlags;
   }

   public boolean hasFlag(char ch) {
      return this.flags.contains(ch);
   }

   public boolean hasValueFlag(String ch) {
      return this.valueFlags.containsKey(ch);
   }

   public int length() {
      return this.args.length;
   }

   public boolean matches(String command) {
      return this.args[0].equalsIgnoreCase(command);
   }

   public static Location parseLocation(Location currentLocation, String flag) throws CommandException {
      boolean denizen = flag.startsWith("l@");
      String[] parts = (String[])Iterables.toArray(LOCATION_SPLITTER.split(flag.replaceFirst("l@", "")), String.class);
      if (parts.length > 0) {
         String worldName = currentLocation != null ? currentLocation.getWorld().getName() : "";
         double x = (double)0.0F;
         double y = (double)0.0F;
         double z = (double)0.0F;
         float yaw = 0.0F;
         float pitch = 0.0F;
         switch (parts.length) {
            case 6:
               if (denizen) {
                  worldName = parts[5].replaceFirst("w@", "");
               } else {
                  pitch = Float.parseFloat(parts[5]);
               }
            case 5:
               if (denizen) {
                  pitch = Float.parseFloat(parts[4]);
               } else {
                  yaw = Float.parseFloat(parts[4]);
               }
            case 4:
               if (denizen && parts.length > 4) {
                  yaw = Float.parseFloat(parts[3]);
               } else {
                  worldName = parts[3].replaceFirst("w@", "");
               }
            case 3:
               x = Double.parseDouble(parts[0]);
               y = Double.parseDouble(parts[1]);
               z = Double.parseDouble(parts[2]);
               World world = Bukkit.getWorld(worldName);
               if (world == null) {
                  throw new CommandException("citizens.commands.npc.create.invalid-location");
               }

               return new Location(world, x, y, z, yaw, pitch);
            default:
               throw new CommandException("citizens.commands.npc.create.invalid-location");
         }
      } else {
         Player search = Bukkit.getPlayerExact(flag);
         if (search == null) {
            throw new CommandException("citizens.commands.npc.create.no-player-for-spawn");
         } else {
            return search.getLocation();
         }
      }
   }
}
