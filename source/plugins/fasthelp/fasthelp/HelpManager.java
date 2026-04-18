package fasthelp;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import lib.util.UtilSpeed;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class HelpManager implements Listener {
   private static final String SPEED = "fh";
   private String pn;
   private String path;
   private ProtocolManager pm = ProtocolLibrary.getProtocolManager();
   private String per_fasthelp_get;
   private int speed;
   private String flag;
   private String mainPage;
   private int empty;
   private String check;
   private ItemStack item;
   private HashMap pageHash;
   private HashMap playerHash = new HashMap();

   public HelpManager(Main main) {
      super();
      this.pn = main.getPn();
      this.path = main.getPluginPath() + File.separator + this.pn + File.separator + "data";
      (new File(this.path)).mkdirs();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      Bukkit.getPluginManager().registerEvents(this, main);
      UtilSpeed.register(this.pn, "fh");
      this.pm.addPacketListener(new PacketAdapter(main, ConnectionSide.SERVER_SIDE, new Integer[]{3}) {
         public void onPacketSending(PacketEvent event) {
            if (HelpManager.this.playerHash.containsKey(event.getPlayer())) {
               event.setCancelled(true);
            }

         }
      });
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.playerHash.remove(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
      if (this.playerHash.containsKey(e.getPlayer())) {
         this.playerHash.remove(e.getPlayer());
         e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(55)}));
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerAnimation(PlayerAnimationEvent e) {
      if (this.playerHash.containsKey(e.getPlayer())) {
         this.playerHash.remove(e.getPlayer());
         e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(55)}));
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      if (this.playerHash.containsKey(e.getPlayer())) {
         this.playerHash.remove(e.getPlayer());
         e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(55)}));
         e.setCancelled(true);
      } else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && this.isHelpItem(e.getPlayer().getItemInHand())) {
         this.openSession(e.getPlayer());
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerChatTabComplete(PlayerChatTabCompleteEvent e) {
      String msg = e.getChatMessage().trim().toLowerCase();
      if (this.playerHash.containsKey(e.getPlayer())) {
         e.getTabCompletions().clear();
         e.getTabCompletions().add("\b");
         Page page = this.getPage((String)this.playerHash.get(e.getPlayer()));
         if (page != null) {
            Player p = e.getPlayer();
            if (!UtilSpeed.check(p, this.pn, "fh", this.speed, false)) {
               this.show(p, this.get(70));
            } else {
               String tar = (String)page.getToHash().get(msg);
               if (tar != null) {
                  Page tarPage = this.getPage(tar);
                  if (tarPage == null) {
                     this.show(p, UtilFormat.format(this.pn, "toErr", new Object[]{tar}));
                  } else {
                     this.playerHash.put(p, tar);
                     this.show(p, UtilFormat.format(this.pn, "tipTo", new Object[]{tar}));
                  }
               } else {
                  String tip = (String)page.getTipHash().get(msg);
                  if (tip != null) {
                     this.show(p, tip);
                  } else {
                     this.show(p, UtilFormat.format(this.pn, "tip2", new Object[]{msg}));
                  }
               }
            }
         } else {
            this.playerHash.remove(e.getPlayer());
            e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(55)}));
         }
      } else if (msg.equalsIgnoreCase(this.flag)) {
         e.getTabCompletions().clear();
         e.getTabCompletions().add("\b");
         this.openSession(e.getPlayer());
      }

   }

   public void openSession(Player p) {
      this.playerHash.put(p, this.mainPage);
      this.show(p, (String)null);
   }

   public void get(Player p) {
      if (UtilPer.checkPer(p, this.per_fasthelp_get)) {
         p.getInventory().addItem(new ItemStack[]{this.item});
         p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(85)}));
      }
   }

   private boolean isHelpItem(ItemStack is) {
      try {
         return ((String)is.getItemMeta().getLore().get(0)).equalsIgnoreCase(this.check);
      } catch (Exception var3) {
         return false;
      }
   }

   private void show(Player p, String tip) {
      Page page = this.getPage((String)this.playerHash.get(p));
      if (page != null) {
         String result = "";

         for(int i = 0; i < this.empty; ++i) {
            result = result + "\n";
         }

         result = result + UtilFormat.format(this.pn, "tip1", new Object[]{page.getName()});
         result = result + "\n";

         for(int index = 1; index <= 16; ++index) {
            result = result + UtilFormat.format(this.pn, "prefix", new Object[]{page.getContentHash().get(index)});
            result = result + "\n";
         }

         result = result + this.get(105);
         result = result + "\n";
         if (tip == null) {
            tip = this.get(60);
         }

         result = result + UtilFormat.format(this.pn, "tipPrefix", new Object[]{tip});
         result = result + "\n";
         result = result + this.get(110);
         this.sendMsg(p, result);
      }

   }

   private void sendMsg(Player p, String msg) {
      try {
         PacketContainer pc = new PacketContainer(3);
         pc.getStrings().write(0, this.getJsonMsg(msg));
         this.pm.sendServerPacket(p, pc, false);
      } catch (InvocationTargetException var4) {
      }

   }

   private Page getPage(String name) {
      return name == null ? null : (Page)this.pageHash.get(name);
   }

   private String getJsonMsg(String s) {
      return "{\"text\":\"" + s + "\"}";
   }

   private void loadConfig(YamlConfiguration config) {
      this.per_fasthelp_get = config.getString("per_fasthelp_get");
      this.speed = config.getInt("speed");
      this.flag = config.getString("flag");
      this.mainPage = config.getString("mainPage");
      this.empty = config.getInt("empty");
      this.check = Util.convert(config.getString("check"));
      this.item = ItemStack.deserialize(((MemorySection)config.get("item")).getValues(true));
      this.pageHash = new HashMap();

      File[] var5;
      for(File file : var5 = (new File(this.path)).listFiles()) {
         if (file.isFile() && file.getName().endsWith(".yml")) {
            String name = file.getName().substring(0, file.getName().length() - 4);
            YamlConfiguration c = new YamlConfiguration();

            try {
               c.load(file);
               HashMap<Integer, String> contentHash = new HashMap();
               int index = 1;

               for(String s : c.getStringList("msg")) {
                  contentHash.put(index++, Util.convert(s));
               }

               HashMap<String, String> toHash = new HashMap();

               for(String s : c.getStringList("to")) {
                  toHash.put(s.split(" ")[0].toLowerCase(), s.split(" ")[1]);
               }

               HashMap<String, String> tipHash = new HashMap();

               for(String s : c.getStringList("tip")) {
                  String[] ss = s.split(" ");
                  tipHash.put(ss[0].toLowerCase(), Util.convert(Util.combine(ss, " ", 1, ss.length)));
               }

               Page page = new Page(name, contentHash, toHash, tipHash);
               this.pageHash.put(name, page);
            } catch (FileNotFoundException e) {
               e.printStackTrace();
            } catch (IOException e) {
               e.printStackTrace();
            } catch (InvalidConfigurationException e) {
               e.printStackTrace();
            }
         }
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
