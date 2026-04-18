package ticket;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import level.LevelManager;
import level.Main;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import lib.util.UtilRewards;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Ticket extends JavaPlugin implements Listener {
   private final String CHECK_BIG = "per.ticket.bigCheck";
   private String pn;
   private Server server;
   private PluginManager pm;
   private BukkitScheduler scheduler;
   private String dataFolder;
   private String pluginVersion;
   private String pluginPath;
   private static Dao dao;
   private static Code code;
   private static ShowManager showManager;
   private LevelManager levelManager;
   private String per_ticket_admin;
   private int price;
   private int priceYear;
   private int freePrice;
   private HashMap levelHash;
   private String levelCmd;
   private int bigGold;
   private int bigLevel;
   private int bigCost;
   private HashList bigCon;
   private HashMap bigItemSmelt;
   private HashMap bigItem;
   private static HashMap userHash;

   public Ticket() {
      super();
   }

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(this.pn));
      this.initDatabase();
      this.loadData();
      code = new Code(this);
      showManager = new ShowManager(this);
      this.levelManager = Main.getLevelManager();
      this.server.getPluginManager().registerEvents(this, this);
      Util.sendConsoleMessage(UtilFormat.format(this.pn, "pluginEnabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public void onDisable() {
      this.scheduler.cancelAllTasks();
      dao.close();
      Util.sendConsoleMessage(UtilFormat.format(this.pn, "pluginDisabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      try {
         Player p = null;
         if (sender instanceof Player) {
            p = (Player)sender;
         }

         String cmdName = cmd.getName();
         int length = args.length;
         if (cmdName.equalsIgnoreCase("ticket")) {
            if (length != 1 || !args[0].equalsIgnoreCase("?")) {
               if (length == 1) {
                  if (args[0].equalsIgnoreCase("reload")) {
                     this.reloadConfig(sender);
                     return true;
                  }
               } else if (length == 3) {
                  if (args[0].equalsIgnoreCase("add")) {
                     this.add(sender, args[1], Integer.parseInt(args[2]));
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("del")) {
                     this.del(sender, args[1], Integer.parseInt(args[2]));
                     return true;
                  }
               } else if (length == 4 && args[0].equalsIgnoreCase("code")) {
                  if (args[1].equalsIgnoreCase("create")) {
                     code.create(sender, Integer.parseInt(args[2]), args[3]);
                     return true;
                  }

                  if (args[1].equalsIgnoreCase("generate")) {
                     code.generate(sender, Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                     return true;
                  }
               }
            }

            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", new Object[]{this.get(10)}));
            if (p == null || UtilPer.hasPer(p, this.per_ticket_admin)) {
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(35), this.get(36)}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(165), this.get(170)}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(175), this.get(180)}));
            }
         }
      } catch (NumberFormatException var8) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(100)}));
      }

      return true;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig0(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      checkInit(e.getPlayer().getName());
   }

   public boolean add(CommandSender sender, String tar, int ticket, String plugin, String reason) {
      try {
         tar = Util.getRealName(sender, tar);
         if (tar == null) {
            return false;
         } else if (ticket <= 0) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(135)}));
            return false;
         } else {
            TicketUser ticketUser = checkInit(tar);
            ticketUser.setTicket(ticketUser.getTicket() + ticket);

            try {
               dao.addOrUpdateTicketUser(ticketUser);
            } catch (Exception var10) {
               ticketUser.setTicket(ticketUser.getTicket() - ticket);
               return false;
            }

            String from;
            if (sender instanceof Player) {
               from = ((Player)sender).getName();
            } else {
               from = this.get(130);
            }

            sender.sendMessage(UtilFormat.format(this.pn, "addTip1", new Object[]{tar, ticket, ticketUser.getTicket()}));
            if (this.server.getPlayer(tar) != null) {
               this.server.getPlayer(tar).sendMessage(UtilFormat.format(this.pn, "addTip2", new Object[]{from, ticket, ticketUser.getTicket()}));
            }

            String log = UtilFormat.format(this.pn, "log1", new Object[]{from, tar, ticket, ticketUser.getTicket(), plugin, reason});
            TicketLog ticketLog = new TicketLog(System.currentTimeMillis(), log);
            dao.addOrUpdateTicketLog(ticketLog);
            return true;
         }
      } catch (Exception var11) {
         return true;
      }
   }

   public boolean del(CommandSender sender, String tar, int ticket, String plugin, String reason) {
      try {
         tar = Util.getRealName(sender, tar);
         if (tar == null) {
            return false;
         } else if (ticket <= 0) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(140)}));
            return false;
         } else {
            TicketUser ticketUser = checkInit(tar);
            if (ticketUser.getTicket() < ticket) {
               sender.sendMessage(UtilFormat.format(this.pn, "err3", new Object[]{ticket, tar, ticketUser.getTicket()}));
               return false;
            } else {
               ticketUser.setTicket(ticketUser.getTicket() - ticket);

               try {
                  dao.addOrUpdateTicketUser(ticketUser);
               } catch (Exception var10) {
                  ticketUser.setTicket(ticketUser.getTicket() + ticket);
                  return false;
               }

               String from;
               if (sender instanceof Player) {
                  from = ((Player)sender).getName();
               } else {
                  from = this.get(130);
               }

               sender.sendMessage(UtilFormat.format(this.pn, "delTip1", new Object[]{tar, ticket, ticketUser.getTicket()}));
               if (this.server.getPlayer(tar) != null) {
                  this.server.getPlayer(tar).sendMessage(UtilFormat.format(this.pn, "delTip2", new Object[]{from, ticket, ticketUser.getTicket()}));
               }

               String log = UtilFormat.format(this.pn, "log2", new Object[]{from, tar, ticket, ticketUser.getTicket(), reason});
               TicketLog ticketLog = new TicketLog(System.currentTimeMillis(), log);
               dao.addOrUpdateTicketLog(ticketLog);
               return true;
            }
         }
      } catch (Exception var11) {
         return true;
      }
   }

   public void buy(Player p, int id) {
      TicketUser tu = checkInit(p.getName());
      int ticket = tu.getTicket();
      int price;
      if (id == 6) {
         price = this.freePrice;
      } else if (id >= 11 && id <= 15) {
         price = this.priceYear;
      } else {
         price = this.price;
      }

      if (ticket < price) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(420)}));
      } else if (id == 6 && this.levelManager.hasLevel(p.getName(), (Integer)((List)this.levelHash.get(6)).get(0))) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(422)}));
      } else {
         tu.setTicket(ticket - price);
         p.sendMessage(UtilFormat.format(this.pn, "del", new Object[]{price}));
         dao.addOrUpdateTicketUser(tu);
         int fixId;
         if (id <= 6) {
            fixId = id;
         } else {
            fixId = id - 10;
         }

         List<Integer> idList = (List)this.levelHash.get(fixId);
         if (idList != null) {
            int max = 1;
            if (id >= 11) {
               max = 12;
            }

            for(int times = 0; times < max; ++times) {
               for(int i : idList) {
                  String cmd = this.levelCmd.replace("<p>", p.getName()).replace("<level>", String.valueOf(i));
                  Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
               }
            }

            p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(430)}));
         }
      }
   }

   public void buyBig(Player p) {
      if (UtilPer.hasPer(p, "per.ticket.bigCheck")) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(460)}));
      } else {
         for(int id : this.bigCon) {
            if (!Main.getLevelManager().hasLevel(p.getName(), id)) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(465)}));
               return;
            }
         }

         if (getTicket(p.getName()) < this.bigCost) {
            p.sendMessage(UtilFormat.format(this.pn, "lagTicket", new Object[]{this.bigCost}));
         } else {
            if (UtilPer.add(p, "per.ticket.bigCheck")) {
               this.del(this.server.getConsoleSender(), p.getName(), this.bigCost);
               HashMap<Integer, ItemStack> itemsHash = new HashMap();
               int index = 0;

               for(String type : this.bigItemSmelt.keySet()) {
                  ItemStack is = UtilItems.getItem("smelt", type).clone();
                  is.setAmount((Integer)this.bigItemSmelt.get(type));
                  itemsHash.put(index++, is);
               }

               for(String type : this.bigItem.keySet()) {
                  ItemStack is = UtilItems.getItem(this.pn, type).clone();
                  is.setAmount((Integer)this.bigItem.get(type));
                  itemsHash.put(index++, is);
               }

               UtilRewards.addRewards(this.pn, (String)null, p.getName(), this.bigGold, 0, this.bigLevel, this.get(470), itemsHash, true);
            }

         }
      }
   }

   public String getPn() {
      return this.pn;
   }

   public Dao getDao() {
      return dao;
   }

   public String getPluginPath() {
      return this.pluginPath;
   }

   public PluginManager getPm() {
      return this.pm;
   }

   public static int getTicket(String name) {
      name = Util.getRealName((CommandSender)null, name);
      if (name == null) {
         return -1;
      } else {
         TicketUser ticketUser = checkInit(name);
         return ticketUser.getTicket();
      }
   }

   public static TicketUser checkInit(String name) {
      TicketUser user = (TicketUser)userHash.get(name);
      if (user == null) {
         user = new TicketUser(name, 0);
         userHash.put(name, user);
         dao.addOrUpdateTicketUser(user);
      }

      return user;
   }

   public static ShowManager getShowManager() {
      return showManager;
   }

   public static Code getCode() {
      return code;
   }

   private void loadData() {
      userHash = new HashMap();

      for(TicketUser ticketUser : dao.getAllTicketUsers()) {
         userHash.put(ticketUser.getName(), ticketUser);
      }

   }

   private void reloadConfig(CommandSender sender) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_ticket_admin)) {
         this.loadConfig(sender);
      }
   }

   private void loadConfig(CommandSender sender) {
      try {
         if (UtilConfig.loadConfig(this.pn)) {
            if (sender != null) {
               sender.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(25)}));
            }
         } else if (sender != null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(30)}));
         }
      } catch (InvalidConfigurationException e) {
         if (sender == null) {
            Util.sendConsoleMessage(e.getMessage());
         } else {
            sender.sendMessage(e.getMessage());
         }
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   private void loadConfig0(YamlConfiguration config) {
      this.per_ticket_admin = config.getString("per_ticket_admin");
      this.price = config.getInt("item.price");
      this.priceYear = config.getInt("item.priceYear");
      this.freePrice = config.getInt("item.free");
      this.levelHash = new HashMap();

      for(String s : config.getStringList("levelId")) {
         int index = Integer.parseInt(s.split(" ")[0]);
         List<Integer> list = new ArrayList();

         String[] var9;
         for(String ss : var9 = s.split(" ")[1].split(";")) {
            int id = Integer.parseInt(ss);
            list.add(id);
         }

         this.levelHash.put(index, list);
      }

      this.levelCmd = config.getString("levelCmd");
      this.bigGold = config.getInt("big.gold");
      this.bigLevel = config.getInt("big.level");
      this.bigCost = config.getInt("big.cost");
      this.bigCon = new HashListImpl();

      for(int id : config.getIntegerList("big.con")) {
         this.bigCon.add(id);
      }

      this.bigItemSmelt = new HashMap();

      for(String s : config.getStringList("big.itemSmelt")) {
         this.bigItemSmelt.put(s.split(" ")[0], Integer.parseInt(s.split(" ")[1]));
      }

      this.bigItem = new HashMap();

      for(String s : config.getStringList("big.item")) {
         this.bigItem.put(s.split(" ")[0], Integer.parseInt(s.split(" ")[1]));
      }

      UtilItems.reloadItems(this.pn, config);
   }

   private void initBasic() {
      this.server = this.getServer();
      this.pn = this.getName();
      this.pm = this.server.getPluginManager();
      this.scheduler = this.server.getScheduler();
      this.pluginPath = this.getFile().getParentFile().getAbsolutePath();
      this.dataFolder = this.pluginPath + File.separator + this.pn;
      this.pluginVersion = Util.getPluginVersion(this.getFile());
   }

   private void initDatabase() {
      dao = new Dao(this);
   }

   private void initConfig() {
      HashList<Pattern> filter = UtilConfig.getDefaultFilter();
      filter.add(Pattern.compile("gold.yml"));
      UtilConfig.register(new File(this.pluginPath + File.separator + this.pn + ".jar"), this.dataFolder, filter, this.pn);
      this.loadConfig((CommandSender)null);
   }

   private void add(CommandSender sender, String tar, int ticket) {
      if (!(sender instanceof Player) || UtilPer.checkPer((Player)sender, this.per_ticket_admin)) {
         this.add(sender, tar, ticket, this.pn, this.get(145));
      }
   }

   private void del(CommandSender sender, String tar, int ticket) {
      if (!(sender instanceof Player) || UtilPer.checkPer((Player)sender, this.per_ticket_admin)) {
         this.del(sender, tar, ticket, this.pn, this.get(150));
      }
   }
}
