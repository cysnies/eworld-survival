package chest;

import java.util.HashMap;
import java.util.Random;
import land.Pos;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.time.TimeEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import net.minecraft.server.v1_6_R2.Packet63WorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class ChestManager implements Listener {
   private Chest chest;
   private Random r = new Random();
   private Dao dao;
   private String pn;
   private String per_chest_admin;
   private boolean disappear;
   private boolean emptyRemove;
   private int pageSize;
   private int chestItem;
   private String name;
   private float offset;
   private float speed;
   private int count;
   private int showRange;
   private float volume;
   private float pitch;
   private HashMap chestHash;
   private HashMap posHash;
   private HashList chestList;

   public ChestManager(Chest chest) {
      super();
      this.chest = chest;
      this.dao = chest.getDao();
      this.pn = Chest.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      chest.getPm().registerEvents(this, chest);
      this.loadData();
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
      priority = EventPriority.HIGHEST
   )
   public void onInventoryClose(InventoryCloseEvent e) {
      if (this.disappear) {
         InventoryHolder ih = e.getInventory().getHolder();
         if (ih instanceof org.bukkit.block.Chest) {
            org.bukkit.block.Chest c = (org.bukkit.block.Chest)ih;
            Location l = c.getLocation();
            Pos pos = Pos.getPos(l);
            ChestInfo ci = (ChestInfo)this.posHash.get(pos);
            if (ci != null) {
               Disappear dis = new Disappear(l);
               Bukkit.getScheduler().scheduleSyncDelayedTask(this.chest, dis);
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent e) {
      for(ChestInfo ci : this.chestHash.values()) {
         if (TimeEvent.getTime() % (long)ci.getCheck() == 0L && this.r.nextInt(100) < ci.getChance()) {
            this.refresh(ci);
         }
      }

   }

   public void create(Player p) {
      if (UtilPer.checkPer(p, this.per_chest_admin)) {
         Location l = p.getLocation();
         Pos pos = Pos.getPos(l);
         ChestInfo ci = (ChestInfo)this.posHash.get(pos);
         if (ci != null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(140)}));
         } else {
            ci = new ChestInfo(pos, false, true, 60, 20, (String)null, (String)null);
            this.dao.addOrUpdateChestInfo(ci);
            this.chestHash.put(ci.getId(), ci);
            this.posHash.put(pos, ci);
            this.chestList.add(ci);
            p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(145)}));
            this.showInfo(p, ci);
         }
      }
   }

   public void showList(CommandSender sender, int page) {
      if (!(sender instanceof Player) || UtilPer.checkPer((Player)sender, this.per_chest_admin)) {
         if (this.chestList.isEmpty()) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(135)}));
         } else {
            int maxPage = this.chestList.getMaxPage(this.pageSize);
            if (page >= 1 && page <= maxPage) {
               sender.sendMessage(UtilFormat.format(this.pn, "listHeader", new Object[]{this.get(130), page, maxPage}));

               for(ChestInfo ci : this.chestList.getPage(page, this.pageSize)) {
                  this.showInfo(sender, ci);
               }

            } else {
               sender.sendMessage(UtilFormat.format(this.pn, "pageErr", new Object[]{maxPage}));
            }
         }
      }
   }

   public void tp(Player p, long id) {
      if (UtilPer.checkPer(p, this.per_chest_admin)) {
         ChestInfo ci = (ChestInfo)this.chestHash.get(id);
         if (ci == null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(150)}));
         } else {
            p.teleport(Pos.toLoc(ci.getPos()));
            p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(155)}));
         }
      }
   }

   public void del(CommandSender sender, long id) {
      if (!(sender instanceof Player) || UtilPer.checkPer((Player)sender, this.per_chest_admin)) {
         ChestInfo ci = (ChestInfo)this.chestHash.get(id);
         if (ci == null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(150)}));
         } else {
            this.chestHash.remove(id);
            this.posHash.remove(ci.getPos());
            this.chestList.remove(ci);
            this.dao.removeChestInfo(ci);
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(160)}));
         }
      }
   }

   public void info(Player p) {
      if (UtilPer.checkPer(p, this.per_chest_admin)) {
         Pos pos = Pos.getPos(p.getLocation());
         ChestInfo ci = (ChestInfo)this.posHash.get(pos);
         if (ci == null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(165)}));
         } else {
            p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(170)}));
            this.showInfo(p, ci);
         }
      }
   }

   public void info(CommandSender sender, long id) {
      if (!(sender instanceof Player) || UtilPer.checkPer((Player)sender, this.per_chest_admin)) {
         ChestInfo ci = (ChestInfo)this.chestHash.get(id);
         if (ci == null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(150)}));
         } else {
            this.showInfo(sender, ci);
         }
      }
   }

   public void refresh(CommandSender sender, long id) {
      if (!(sender instanceof Player) || UtilPer.checkPer((Player)sender, this.per_chest_admin)) {
         ChestInfo ci = (ChestInfo)this.chestHash.get(id);
         if (ci == null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(150)}));
         } else {
            this.refresh(ci);
            sender.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(175)}));
         }
      }
   }

   public void refreshAll(CommandSender sender) {
      if (!(sender instanceof Player) || UtilPer.checkPer((Player)sender, this.per_chest_admin)) {
         if (this.chestHash.isEmpty()) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(135)}));
         } else {
            for(ChestInfo ci : this.chestHash.values()) {
               this.refresh(ci);
            }

            sender.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(180)}));
         }
      }
   }

   public void show(Player p, int range) {
      if (UtilPer.checkPer(p, this.per_chest_admin)) {
         String worldName = p.getWorld().getName();
         Location l = p.getLocation();
         int amount = 0;

         for(ChestInfo ci : this.chestHash.values()) {
            if (worldName.equals(ci.getPos().getWorld()) && l.distance(Pos.toLoc(ci.getPos())) <= (double)range) {
               p.sendBlockChange(Pos.toLoc(ci.getPos()), this.chestItem, (byte)0);
               ++amount;
            }
         }

         p.sendMessage(UtilFormat.format(this.pn, "showSuccess", new Object[]{amount}));
      }
   }

   public void setVar(CommandSender sender, long id, String param, String value) {
      try {
         if (sender instanceof Player && !UtilPer.checkPer((Player)sender, this.per_chest_admin)) {
            return;
         }

         ChestInfo ci = (ChestInfo)this.chestHash.get(id);
         if (ci == null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(150)}));
            return;
         }

         if (param.equalsIgnoreCase("generate")) {
            ci.setGenerate(Boolean.parseBoolean(value));
         } else if (param.equalsIgnoreCase("refresh")) {
            ci.setRefresh(Boolean.parseBoolean(value));
         } else if (param.equalsIgnoreCase("check")) {
            int check = Integer.parseInt(value);
            if (check < 1) {
               check = 1;
            }

            ci.setCheck(check);
         } else if (param.equalsIgnoreCase("chance")) {
            int chance = Integer.parseInt(value);
            if (chance < 0) {
               chance = 0;
            } else if (chance > 100) {
               chance = 100;
            }

            ci.setChance(chance);
         } else if (param.equalsIgnoreCase("item")) {
            ci.setItemType(value);
         } else {
            if (!param.equalsIgnoreCase("enchant")) {
               sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(185)}));
               return;
            }

            ci.setEnchantType(value);
         }

         sender.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(195)}));
         this.dao.addOrUpdateChestInfo(ci);
      } catch (Exception var8) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(190)}));
      }

   }

   public void copy(Player p, String s) {
      if (UtilPer.checkPer(p, this.per_chest_admin)) {
         String[] ss = s.split(":");
         String start;
         String end;
         if (ss.length != 2) {
            if (!s.substring(s.length() - 1).equals(":")) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(210)}));
               return;
            }

            start = ss[0];
            end = "";
         } else {
            start = ss[0];
            end = ss[1];
         }

         long from = -1L;
         long to = -1L;

         try {
            if (!start.isEmpty()) {
               from = Long.parseLong(start);
            }

            if (!end.isEmpty()) {
               to = Long.parseLong(end);
            }
         } catch (NumberFormatException var13) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(210)}));
            return;
         }

         if (from == -1L) {
            Pos pos = Pos.getPos(p.getLocation());
            ChestInfo ci = (ChestInfo)this.posHash.get(pos);
            if (ci == null) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(165)}));
               return;
            }

            from = ci.getId();
         }

         ChestInfo fromInfo = (ChestInfo)this.chestHash.get(from);
         if (fromInfo == null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(215)}));
         } else {
            if (to == -1L) {
               Pos pos = Pos.getPos(p.getLocation());
               ChestInfo ci = (ChestInfo)this.posHash.get(pos);
               if (ci == null) {
                  ci = new ChestInfo(pos, false, true, 60, 20, (String)null, (String)null);
                  this.dao.addOrUpdateChestInfo(ci);
                  this.chestHash.put(ci.getId(), ci);
                  this.posHash.put(pos, ci);
                  this.chestList.add(ci);
               }

               to = ci.getId();
            }

            ChestInfo toInfo = (ChestInfo)this.chestHash.get(to);
            if (toInfo == null) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(220)}));
            } else if (from == to) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(225)}));
            } else {
               toInfo.setGenerate(fromInfo.isGenerate());
               toInfo.setRefresh(fromInfo.isRefresh());
               toInfo.setCheck(fromInfo.getCheck());
               toInfo.setChance(fromInfo.getChance());
               toInfo.setItemType(fromInfo.getItemType());
               toInfo.setEnchantType(fromInfo.getEnchantType());
               this.dao.addOrUpdateChestInfo(toInfo);
               p.sendMessage(UtilFormat.format(this.pn, "copy", new Object[]{from, to}));
               this.showInfo(p, toInfo);
            }
         }
      }
   }

   private void refresh(ChestInfo ci) {
      try {
         Location l = Pos.toLoc(ci.getPos());
         Block b = l.getBlock();
         if (b.getTypeId() != 54) {
            if (!ci.isGenerate()) {
               return;
            }

            b.setTypeId(54);
         }

         this.showEffect(l);
         Inventory inv = ((org.bukkit.block.Chest)b.getState()).getInventory();
         if (ci.isRefresh()) {
            inv.clear();
         }

         if (ci.getItemType() != null) {
            try {
               HashList<ItemStack> itemList = UtilItems.getItems(this.pn, ci.getItemType(), true, false, this.pn, ci.getEnchantType(), true, false);
               if (itemList != null && !itemList.isEmpty()) {
                  for(ItemStack is : itemList) {
                     inv.addItem(new ItemStack[]{is});
                  }

                  l.getWorld().playSound(l, Sound.LEVEL_UP, this.volume, this.pitch);
               }
            } catch (Exception var8) {
            }
         }

         if (this.emptyRemove && UtilItems.getEmptySlots(inv) == inv.getSize()) {
            Disappear dis = new Disappear(l);
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.chest, dis);
         }
      } catch (Exception var9) {
      }

   }

   private void showEffect(Location l) {
      Packet63WorldParticles packet = new Packet63WorldParticles(this.name, (float)l.getX(), (float)l.getY(), (float)l.getZ(), this.offset, this.offset, this.offset, this.speed, this.count);

      Player[] var6;
      for(Player p : var6 = Bukkit.getServer().getOnlinePlayers()) {
         if (p.getWorld().equals(l.getWorld()) && p.getLocation().distance(l) < (double)this.showRange) {
            ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
         }
      }

   }

   private void showInfo(CommandSender sender, ChestInfo ci) {
      sender.sendMessage(UtilFormat.format(this.pn, "chestList", new Object[]{ci.getId(), ci.getPos().getWorld(), ci.getPos().getX(), ci.getPos().getY(), ci.getPos().getZ(), ci.isGenerate(), ci.isRefresh(), ci.getCheck(), ci.getChance(), ci.getItemType(), ci.getEnchantType()}));
   }

   private void loadConfig(FileConfiguration config) {
      this.per_chest_admin = config.getString("per_chest_admin");
      this.disappear = config.getBoolean("disappear");
      this.emptyRemove = config.getBoolean("emptyRemove");
      this.pageSize = config.getInt("pageSize");
      this.chestItem = config.getInt("chestItem");
      this.name = config.getString("name");
      this.offset = (float)config.getDouble("offset");
      this.speed = (float)config.getDouble("speed");
      this.count = config.getInt("count");
      this.showRange = config.getInt("showRange");
      this.volume = (float)config.getDouble("volume");
      this.pitch = (float)config.getDouble("pitch");
   }

   private void loadData() {
      this.chestHash = new HashMap();
      this.posHash = new HashMap();
      this.chestList = new HashListImpl();

      for(ChestInfo chestInfo : this.dao.getAllChestInfos()) {
         this.chestHash.put(chestInfo.getId(), chestInfo);
         this.posHash.put(chestInfo.getPos(), chestInfo);
         this.chestList.add(chestInfo);
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   private class Disappear implements Runnable {
      Location l;

      public Disappear(Location l) {
         super();
         this.l = l;
      }

      public void run() {
         this.l.getBlock().setTypeId(0);
      }
   }
}
