package ticket;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import lib.util.UtilSpeed;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Code implements Listener {
   private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH-mm-ss");
   private static final String SPEED_INFO = "info";
   private static final String SPEED_USE = "use";
   private Dao dao;
   private Random r;
   private Ticket ticket;
   private Server server;
   private String pn;
   private String per_ticket_admin;
   private int interval;
   private String chars;
   private int lengthGenerateFix;
   private int lengthCreateMin;
   private int lengthCreateMax;
   private int maxTimes;
   private HashMap codeHash;

   public Code(Ticket ticket) {
      super();
      this.dao = ticket.getDao();
      this.r = new Random();
      this.ticket = ticket;
      this.server = ticket.getServer();
      this.pn = ticket.getPn();
      (new File(ticket.getPluginPath() + File.separator + this.pn + File.separator + "code")).mkdirs();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, ticket);
      this.loadData();
      UtilSpeed.register(this.pn, "info");
      UtilSpeed.register(this.pn, "use");
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public void use(Player p, String code) {
      if (UtilPer.hasPer(p, this.per_ticket_admin) || UtilSpeed.check(p, this.pn, "use", this.interval)) {
         TicketCode ticketCode = (TicketCode)this.codeHash.get(code);
         if (ticketCode == null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(195)}));
         } else if (ticketCode.getStatus() == 1) {
            p.sendMessage(UtilFormat.format(this.pn, "used", new Object[]{ticketCode.getUser()}));
         } else if (!this.ticket.add(this.server.getConsoleSender(), p.getName(), ticketCode.getTicket(), this.pn, this.get(215))) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(220)}));
         } else {
            ticketCode.setStatus(1);
            ticketCode.setUser(p.getName());
            ticketCode.setUseTime(System.currentTimeMillis());
            this.dao.addOrUpdateCode(ticketCode);
            p.sendMessage(UtilFormat.format(this.pn, "codeUse", new Object[]{ticketCode.getTicket()}));
         }
      }
   }

   public void create(CommandSender sender, int value, String code) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_ticket_admin)) {
         if (value <= 0) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(230)}));
         } else if (code.length() >= this.lengthCreateMin && code.length() <= this.lengthCreateMax) {
            char[] var8;
            for(char c : var8 = code.toCharArray()) {
               if (this.chars.indexOf(c) == -1) {
                  sender.sendMessage(UtilFormat.format(this.pn, "codeCharsErr", new Object[]{this.chars}));
                  return;
               }
            }

            TicketCode ticketCode = (TicketCode)this.codeHash.get(code);
            if (ticketCode != null) {
               sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(225)}));
            } else {
               ticketCode = new TicketCode(code, value, System.currentTimeMillis());
               this.codeHash.put(code, ticketCode);
               this.dao.addOrUpdateCode(ticketCode);
               sender.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(435)}));
            }
         } else {
            sender.sendMessage(UtilFormat.format(this.pn, "codeLengthErr", new Object[]{this.lengthCreateMin, this.lengthCreateMax}));
         }
      }
   }

   public void info(CommandSender sender, String code) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.hasPer(p, this.per_ticket_admin) || UtilSpeed.check(p, this.pn, "info", this.interval)) {
         TicketCode ticketCode = (TicketCode)this.codeHash.get(code);
         if (ticketCode == null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(235)}));
         } else {
            String status = "";
            String user = "";
            String useTime = "";
            if (ticketCode.getStatus() == 0) {
               status = this.get(205);
               user = this.get(210);
               useTime = this.get(210);
            } else if (ticketCode.getStatus() == 1) {
               status = this.get(200);
               user = ticketCode.getUser();
               useTime = Util.getDateTime(new Date(ticketCode.getUseTime()), 0, 0, 0);
            }

            String createTime = Util.getDateTime(new Date(ticketCode.getCreateTime()), 0, 0, 0);
            sender.sendMessage(UtilFormat.format(this.pn, "codeInfo", new Object[]{ticketCode.getCode(), status, ticketCode.getTicket(), user, createTime, useTime}));
         }
      }
   }

   public void generate(CommandSender sender, int value, int times) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_ticket_admin)) {
         if (value <= 0) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(230)}));
         } else if (times > 0 && times <= this.maxTimes) {
            String path = this.ticket.getPluginPath() + File.separator + this.pn + File.separator + "code";
            (new File(path)).mkdirs();
            File logFile = new File(path + File.separator + sdf.format(new Date()) + ".txt");

            try {
               logFile.createNewFile();
               FileOutputStream fos = new FileOutputStream(logFile);
               DataOutputStream dos = new DataOutputStream(fos);
               sender.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(240)}));
               int sum = 0;
               HashList<TicketCode> successList = new HashListImpl();

               for(int i = 0; i < times; ++i) {
                  String code = this.getRandomCode();
                  if (!this.codeHash.containsKey(code)) {
                     TicketCode tc = new TicketCode(code, value, System.currentTimeMillis());
                     successList.add(tc);
                     this.codeHash.put(code, tc);
                     sender.sendMessage(UtilFormat.format(this.pn, "generate1", new Object[]{code}));
                     dos.write(("a " + code + "\n").getBytes());
                     ++sum;
                  }
               }

               if (!successList.isEmpty()) {
                  this.dao.addOrUpdateTicketCodes(successList);
               }

               dos.close();
               fos.close();
               sender.sendMessage(UtilFormat.format(this.pn, "generate2", new Object[]{times, sum}));
            } catch (FileNotFoundException e) {
               e.printStackTrace();
            } catch (IOException e) {
               e.printStackTrace();
            }

         } else {
            sender.sendMessage(UtilFormat.format(this.pn, "timesErr", new Object[]{this.maxTimes}));
         }
      }
   }

   private String getRandomCode() {
      String result = "";
      int length = this.chars.length();

      for(int i = 0; i < this.lengthGenerateFix; ++i) {
         result = result + this.chars.charAt(this.r.nextInt(length));
      }

      return result;
   }

   private void loadConfig(YamlConfiguration config) {
      this.per_ticket_admin = config.getString("per_ticket_admin");
      this.interval = config.getInt("interval");
      this.chars = config.getString("chars");
      this.lengthGenerateFix = config.getInt("length.generate.fix");
      this.lengthCreateMin = config.getInt("length.create.min");
      this.lengthCreateMax = config.getInt("length.create.max");
      this.maxTimes = config.getInt("times.max");
   }

   private void loadData() {
      this.codeHash = new HashMap();

      for(TicketCode tc : this.dao.getAllTicketCodes()) {
         this.codeHash.put(tc.getCode(), tc);
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
