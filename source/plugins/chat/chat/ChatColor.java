package chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.Color;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class ChatColor implements Listener {
   private static final int ITEM_COLOR = 351;
   private static final int BOLD_ITEM = 331;
   private static final String CHECK = "per.chat.color.boldCheck";
   private Dao dao;
   private String pn;
   private String per_chat_color;
   private String per_chat_item;
   private String colorChars;
   private String showColorChars;
   private char defaultColor;
   private HashMap chatHash;
   private HashMap colorHash;
   private HashList colorList;
   private HashMap itemColorHash;
   private ItemStack isBold;

   public ChatColor(Chat main) {
      super();
      this.dao = main.getDao();
      this.pn = main.getPn();
      this.loadData(this.dao);
      this.loadConfig(UtilConfig.getConfig(this.pn));
      main.getPm().registerEvents(this, main);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      if (!UtilPer.hasPer(e.getPlayer(), this.per_chat_color)) {
         UtilPer.remove(e.getPlayer(), "per.chat.color.boldCheck");
         if (this.chatHash.containsKey(e.getPlayer().getName())) {
            ChatUser cu = (ChatUser)this.chatHash.remove(e.getPlayer().getName());
            if (cu != null) {
               this.dao.removeChatUser(cu);
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
      try {
         Player p = e.getPlayer();
         if (UtilPer.hasPer(p, "per.chat.color.boldCheck")) {
            e.setMessage("§l" + e.getMessage());
         }

         if (UtilPer.hasPer(p, this.per_chat_color)) {
            String name = p.getName();
            char c;
            if (this.chatHash.containsKey(name)) {
               ChatUser chatUser = (ChatUser)this.chatHash.get(name);
               c = chatUser.getC();
            } else {
               c = this.defaultColor;
            }

            e.setMessage("§" + c + e.getMessage());
         }

         if (UtilPer.hasPer(p, this.per_chat_item)) {
            boolean update = false;
            ItemStack boldItem = p.getInventory().getItem(7);
            if (boldItem != null && boldItem.getTypeId() == 331) {
               update = true;
               if (boldItem.getAmount() > 1) {
                  boldItem.setAmount(boldItem.getAmount() - 1);
               } else {
                  p.getInventory().setItem(7, (ItemStack)null);
               }

               e.setMessage("§l" + e.getMessage());
            }

            ItemStack colorItem = p.getInventory().getItem(8);
            if (colorItem != null && colorItem.getTypeId() == 351) {
               int smallId = colorItem.getDurability();
               String color = (String)this.itemColorHash.get(smallId);
               if (color != null) {
                  update = true;
                  if (colorItem.getAmount() > 1) {
                     colorItem.setAmount(colorItem.getAmount() - 1);
                  } else {
                     p.getInventory().setItem(8, (ItemStack)null);
                  }

                  e.setMessage("§" + color + e.getMessage());
               }
            }

            if (update) {
               p.updateInventory();
            }
         }
      } catch (Exception e1) {
         e1.printStackTrace();
         System.out.println(">>>>>>>>>>>>异常位置3");
      }

   }

   public boolean set(Player p, String s) {
      if (!UtilPer.checkPer(p, this.per_chat_color)) {
         return false;
      } else {
         s = s.trim();
         if (s.isEmpty()) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(40)}));
            return false;
         } else {
            char c = s.charAt(0);
            if (this.colorChars.indexOf(c) == -1) {
               p.sendMessage(UtilFormat.format(this.pn, "invalidColor", new Object[]{this.showColorChars}));
               return false;
            } else {
               String name = p.getName();
               ChatUser chatUser = (ChatUser)this.chatHash.get(name);
               if (chatUser == null) {
                  chatUser = new ChatUser(name, c);
                  this.chatHash.put(name, chatUser);
               } else {
                  chatUser.setC(c);
               }

               this.dao.addOrUpdateChatUser(chatUser);
               p.sendMessage(UtilFormat.format(this.pn, "setColor", new Object[]{c}));
               return true;
            }
         }
      }
   }

   public void setBold(Player p) {
      if (UtilPer.checkPer(p, this.per_chat_color)) {
         String status;
         if (UtilPer.hasPer(p, "per.chat.color.boldCheck")) {
            UtilPer.remove(p, "per.chat.color.boldCheck");
            status = this.get(200);
         } else {
            UtilPer.add(p, "per.chat.color.boldCheck");
            status = this.get(195);
         }

         p.sendMessage(UtilFormat.format(this.pn, "toggleBold", new Object[]{status}));
      }
   }

   public String getPer_chat_color() {
      return this.per_chat_color;
   }

   public String getColorShow(String name) {
      ChatUser cu = (ChatUser)this.chatHash.get(name);
      return cu == null ? "f" : String.valueOf(cu.getC());
   }

   public HashList getColorList() {
      return this.colorList;
   }

   public String getColor(int pos) {
      String color = (String)this.colorHash.get(pos);
      return color == null ? "f" : color;
   }

   public boolean isBold(String name) {
      return UtilPer.hasPer(name, this.per_chat_color) && UtilPer.hasPer(name, "per.chat.color.boldCheck");
   }

   public ItemStack getIsBold(Player p) {
      ItemStack is = this.isBold.clone();
      ItemMeta im = is.getItemMeta();
      List<String> lore = new ArrayList();
      String bold;
      if (UtilPer.hasPer(p, "per.chat.color.boldCheck")) {
         bold = this.get(195);
      } else {
         bold = this.get(200);
      }

      lore.add(UtilFormat.format(this.pn, "isBold", new Object[]{bold}));
      im.setLore(lore);
      is.setItemMeta(im);
      return is;
   }

   public static String getCheck() {
      return "per.chat.color.boldCheck";
   }

   private void loadConfig(YamlConfiguration config) {
      this.per_chat_color = config.getString("per_chat_color");
      this.per_chat_item = config.getString("per_chat_item");
      this.colorChars = config.getString("colorChars");
      this.showColorChars = "";

      char[] var5;
      for(char c : var5 = this.colorChars.toCharArray()) {
         this.showColorChars = this.showColorChars + "§" + c + "§f";
      }

      this.defaultColor = config.getString("defaultColor").charAt(0);
      this.colorHash = new HashMap();
      this.colorList = new HashListImpl();
      int index = 0;

      for(String s : config.getStringList("item.colors")) {
         String color = s.split(" ")[0];
         int r = Integer.parseInt(s.split(" ")[1]);
         int g = Integer.parseInt(s.split(" ")[2]);
         int b = Integer.parseInt(s.split(" ")[3]);
         ItemStack is = new ItemStack(299);
         LeatherArmorMeta meta = (LeatherArmorMeta)is.getItemMeta();
         meta.setColor(Color.fromRGB(r, g, b));
         meta.setDisplayName("§" + color + color);
         is.setItemMeta(meta);
         this.colorHash.put(index, color);
         this.colorList.add(is);
         ++index;
         this.isBold = new ItemStack(311);
         ItemMeta im = this.isBold.getItemMeta();
         im.setDisplayName("§l" + this.get(190));
         this.isBold.setItemMeta(im);
      }

      this.itemColorHash = new HashMap();

      for(String s : config.getStringList("itemColor")) {
         int smallId = Integer.parseInt(s.split(" ")[0]);
         String color = s.split(" ")[1];
         this.itemColorHash.put(smallId, color);
      }

   }

   private void loadData(Dao dao) {
      this.chatHash = new HashMap();

      for(ChatUser chatUser : dao.getAllChatUsers()) {
         this.chatHash.put(chatUser.getName(), chatUser);
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
