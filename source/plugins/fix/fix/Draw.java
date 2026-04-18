package fix;

import com.goncalomb.bukkit.nbteditor.nbt.TileNBTWrapper;
import java.util.HashMap;
import java.util.Random;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import lib.util.UtilSpeed;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Sign;
import org.yi.acru.bukkit.Lockette.Lockette;

public class Draw implements Listener {
   private static final String DRAW = "draw";
   private static final int DISPENSER = 23;
   private static HashList BF = new HashListImpl();
   private static HashMap DIR;
   private Random r = new Random();
   private Fix fix;
   private Server server;
   private String pn;
   private Lockette lockette;
   private String per;
   private String flag;
   private String check;
   private String name;
   private String flagUnlimit;
   private String checkUnlimit;
   private String price;
   private int maxPrice;
   private int cost;
   private int tax;
   private boolean sneak;
   private int interval;

   static {
      BF.add(BlockFace.EAST);
      BF.add(BlockFace.SOUTH);
      BF.add(BlockFace.WEST);
      BF.add(BlockFace.NORTH);
      DIR = new HashMap();
      DIR.put((byte)8, (byte)2);
      DIR.put((byte)12, (byte)5);
      DIR.put((byte)0, (byte)3);
      DIR.put((byte)4, (byte)4);
   }

   public Draw(Fix fix) {
      super();
      this.fix = fix;
      this.server = fix.getServer();
      this.pn = fix.getPn();
      this.lockette = (Lockette)this.server.getPluginManager().getPlugin("Lockette");
      this.loadConfig(UtilConfig.getConfig(this.pn));
      fix.getPm().registerEvents(this, fix);
      UtilSpeed.register(this.pn, "draw");
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
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onSignChange(SignChangeEvent e) {
      if (e.getLines()[0].equalsIgnoreCase(this.check)) {
         e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(800)}));
         e.setCancelled(true);
      } else {
         if (e.getLines()[0].equalsIgnoreCase(this.flag)) {
            if (!UtilPer.checkPer(e.getPlayer(), this.per)) {
               e.setCancelled(true);
               return;
            }

            try {
               int price = Integer.parseInt(e.getLines()[1]);
               if (price < 0 || price > this.maxPrice) {
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "costDraw1", new Object[]{this.maxPrice}));
                  e.setCancelled(true);
                  return;
               }

               Sign sign2 = (Sign)e.getBlock().getType().getNewData(e.getBlock().getData());
               if (sign2.isWallSign()) {
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(810)}));
                  e.setCancelled(true);
                  return;
               }

               if (!BF.has(sign2.getFacing())) {
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(820)}));
                  e.setCancelled(true);
                  return;
               }

               Block tar = e.getBlock().getRelative(sign2.getFacing().getOppositeFace());
               if (tar.getTypeId() != 23) {
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(825)}));
                  e.setCancelled(true);
                  return;
               }

               if (this.lockette != null && !Lockette.isOwner(tar, e.getPlayer().getName())) {
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(855)}));
                  e.setCancelled(true);
                  return;
               }

               if (e.getLines()[2].equalsIgnoreCase(this.checkUnlimit)) {
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(870)}));
                  e.setCancelled(true);
                  return;
               }

               boolean unlimit = false;
               if (e.getLines()[2] != null && e.getLines()[2].equalsIgnoreCase(this.flagUnlimit)) {
                  if (!e.getPlayer().isOp()) {
                     e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(865)}));
                     e.setCancelled(true);
                     return;
                  }

                  unlimit = true;
               }

               Dispenser dis = (Dispenser)tar.getState();
               if (this.getOwner(dis.getInventory()) != null) {
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(875)}));
                  e.setCancelled(true);
                  return;
               }

               if (this.cost > 0) {
                  if (UtilEco.get(e.getPlayer().getName()) < (double)this.cost) {
                     e.getPlayer().sendMessage(UtilFormat.format(this.pn, "costDraw3", new Object[]{this.cost}));
                     e.setCancelled(true);
                     return;
                  }

                  UtilEco.del(e.getPlayer().getName(), (double)this.cost);
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "delGold", new Object[]{this.cost}));
               }

               e.setCancelled(true);
               e.getBlock().setTypeIdAndData(68, (Byte)DIR.get(e.getBlock().getData()), true);
               this.setName(dis, this.name + " " + e.getPlayer().getName());
               e.getBlock().getState().update(true);
               org.bukkit.block.Sign sign = (org.bukkit.block.Sign)e.getBlock().getState();
               sign.setLine(0, this.check);
               sign.setLine(1, this.price.replaceAll("\\{0\\}", String.valueOf(price)));
               if (unlimit) {
                  sign.setLine(2, this.checkUnlimit);
               } else {
                  sign.setLine(2, "");
               }

               sign.setLine(3, e.getPlayer().getName());
               sign.update(true);
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(805)}));
            } catch (NumberFormatException var8) {
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(60)}));
               e.setCancelled(true);
               return;
            }
         }

      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
         int id = e.getClickedBlock().getTypeId();
         if (id == 68) {
            org.bukkit.block.Sign sign = (org.bukkit.block.Sign)e.getClickedBlock().getState();
            if (sign.getLines()[0].equals(this.check)) {
               if (!UtilSpeed.check(e.getPlayer(), this.pn, "draw", this.interval)) {
                  return;
               }

               if (!e.getPlayer().isSneaking() && this.sneak) {
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(815)}));
                  e.setCancelled(true);
                  return;
               }

               int price = this.getPrice(sign.getLines()[1]);
               if (price > -1) {
                  String owner = sign.getLines()[3];
                  if (UtilEco.get(e.getPlayer().getName()) < (double)price) {
                     e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(860)}));
                     e.setCancelled(true);
                     return;
                  }

                  Sign sign0 = (Sign)e.getClickedBlock().getType().getNewData(e.getClickedBlock().getData());
                  Block tar = e.getClickedBlock().getRelative(sign0.getFacing().getOppositeFace());
                  if (tar.getTypeId() != 23) {
                     e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(840)}));
                     e.setCancelled(true);
                     return;
                  }

                  Inventory inv = null;
                  Dispenser dis = null;
                  if (tar.getTypeId() != 23) {
                     return;
                  }

                  dis = (Dispenser)tar.getState();
                  inv = dis.getInventory();
                  String owner2 = this.getOwner(inv);
                  if (owner2 == null || !owner2.equals(owner)) {
                     e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(840)}));
                     e.setCancelled(true);
                     return;
                  }

                  if (UtilItems.getEmptySlots(inv) == inv.getSize()) {
                     e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(850)}));
                     e.setCancelled(true);
                     return;
                  }

                  if (UtilItems.getEmptySlots(e.getPlayer().getInventory()) <= 0) {
                     e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(830)}));
                     e.setCancelled(true);
                     return;
                  }

                  e.setCancelled(true);
                  if (price > 0) {
                     UtilEco.del(e.getPlayer().getName(), (double)price);
                     e.getPlayer().sendMessage(UtilFormat.format(this.pn, "delGold", new Object[]{price}));
                     int tax = price * this.tax / 100;
                     int left = price - tax;
                     UtilEco.add(owner, (double)left);
                     if (this.server.getPlayer(owner) != null) {
                        this.server.getPlayer(owner).sendMessage(UtilFormat.format(this.pn, "costDraw2", new Object[]{e.getPlayer().getName(), left, tax}));
                     }
                  }

                  boolean unlimit = sign.getLines()[2] != null && sign.getLines()[2].equalsIgnoreCase(this.checkUnlimit);
                  ItemStack is = this.getRandomItem(dis, unlimit);
                  if (is != null) {
                     e.getPlayer().getInventory().addItem(new ItemStack[]{is});
                     e.getPlayer().updateInventory();
                  }

                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(845)}));
               }
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onInventoryClick(InventoryClickEvent e) {
      if (e.getInventory().getType().equals(InventoryType.DISPENSER) && e.getWhoClicked() instanceof Player) {
         Player p = (Player)e.getWhoClicked();
         String owner = this.getOwner(e.getInventory());
         if (owner != null && !owner.equals(p.getName())) {
            e.setCancelled(true);
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(880)}));
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.fix, new Close(p));
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onBlockDispense(BlockDispenseEvent e) {
      try {
         if (e.getBlock().getType().equals(Material.DISPENSER)) {
            String name = this.getOwner(((Dispenser)e.getBlock().getState()).getInventory());
            if (name != null) {
               e.setCancelled(true);
            }
         }
      } catch (Exception var3) {
      }

   }

   private void setName(Dispenser dis, String name) {
      TileNBTWrapper tile = new TileNBTWrapper(dis.getBlock());
      tile.setCustomName(name);
      tile.save();
   }

   private String getOwner(Inventory inv) {
      String title = inv.getTitle();
      if (title == null) {
         return null;
      } else {
         String[] titles = title.split(" ");
         return titles.length == 2 && titles[0].equalsIgnoreCase(this.name) ? titles[1] : null;
      }
   }

   private ItemStack getRandomItem(Dispenser dis, boolean unlimit) {
      try {
         Inventory inv = dis.getInventory();
         int size = inv.getSize();
         int total = 0;

         for(int i = 0; i < size; ++i) {
            ItemStack is = inv.getItem(i);
            if (is != null && is.getTypeId() != 0) {
               total += is.getAmount();
            }
         }

         int tar = this.r.nextInt(total);
         int get = 0;

         for(int i = 0; i < size; ++i) {
            ItemStack is = inv.getItem(i);
            if (is != null && is.getTypeId() != 0) {
               get += is.getAmount();
            }

            if (get > tar) {
               ItemStack result = is.clone();
               result.setAmount(1);
               if (!unlimit) {
                  if (is.getAmount() <= 1) {
                     inv.setItem(i, (ItemStack)null);
                  } else {
                     is.setAmount(is.getAmount() - 1);
                  }
               }

               return result;
            }
         }

         return null;
      } catch (Exception var11) {
         return null;
      }
   }

   private int getPrice(String s) {
      try {
         return Integer.parseInt(s.substring(8, s.length() - 2));
      } catch (Exception var3) {
         return -1;
      }
   }

   private void loadConfig(FileConfiguration config) {
      this.per = config.getString("draw.per");
      this.flag = config.getString("draw.flag");
      this.check = Util.convert(config.getString("draw.check"));
      this.name = Util.convert(config.getString("draw.name"));
      this.flagUnlimit = config.getString("draw.flagUnlimit");
      this.checkUnlimit = Util.convert(config.getString("draw.checkUnlimit"));
      this.price = Util.convert(config.getString("draw.price"));
      this.maxPrice = config.getInt("draw.maxPrice");
      this.cost = config.getInt("draw.cost");
      this.tax = config.getInt("draw.tax");
      this.sneak = config.getBoolean("draw.sneak");
      this.interval = config.getInt("draw.interval");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   private class Close implements Runnable {
      private Player p;

      public Close(Player p) {
         super();
         this.p = p;
      }

      public void run() {
         if (this.p != null && this.p.isOnline()) {
            this.p.closeInventory();
         }

      }
   }
}
