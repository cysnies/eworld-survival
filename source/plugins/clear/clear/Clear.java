package clear;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import land.Land;
import landMain.LandMain;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilNames;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Clear implements Listener {
   private static final int DELAY_SHOW = 35;
   private static final int CHEST_ID = 54;
   private static final String LIB = "lib";
   private static final String BAN_CLEAR = "banClear";
   private Random r = new Random();
   private Main main;
   private Server server;
   private String pn;
   private ServerManager serverManager;
   private boolean tip;
   private HashList ignoreWorlds;
   private int checkInterval;
   private int startClearEntitys;
   private int mustClearAmount;
   private int mustClearLevel;
   private HashList levelList;
   private int gridSize;
   private HashList clearList;
   private int ticksLived;
   private int clearMode;
   private HashList clearWhite;
   private HashList clearBlack;
   private HashList clearMonsterList;
   private int maxPerGrid;
   private boolean firstAll;
   private int heightMax;
   private int heightMin;
   private HashMap clearTypes;
   private ClearTimer clearTimer;
   private HashList airBlocks;
   private HashMap banClearModeHash;

   public Clear(Main main) {
      super();
      this.main = main;
      this.server = main.getServer();
      this.pn = main.getPn();
      this.serverManager = main.getServerManager();
      this.clearTimer = new ClearTimer();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      main.getServer().getScheduler().scheduleSyncDelayedTask(main, this.clearTimer, (long)(this.checkInterval * 20));
      main.getServer().getPluginManager().registerEvents(this, main);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public void info(CommandSender sender) {
      HashMap<String, Integer> entityHash = new HashMap();
      int total = 0;

      for(World w : this.server.getWorlds()) {
         List<Entity> list = w.getEntities();
         total += list.size();

         for(Entity e : list) {
            if (!entityHash.containsKey(e.getType().getName())) {
               entityHash.put(e.getType().getName(), 0);
            }

            entityHash.put(e.getType().getName(), (Integer)entityHash.get(e.getType().getName()) + 1);
         }
      }

      sender.sendMessage(UtilFormat.format("lib", "success", new Object[]{this.get(1200)}));

      for(String s : entityHash.keySet()) {
         sender.sendMessage(UtilFormat.format(this.pn, "broadcastInfo", new Object[]{s, entityHash.get(s)}));
      }

      sender.sendMessage(UtilFormat.format(this.pn, "clearInfo2", new Object[]{total}));
   }

   public void clear(boolean force, int clearLevel) {
      HashMap<Short, Integer> startHash = new HashMap();
      int startTotal = 0;

      for(World w : this.server.getWorlds()) {
         List<Entity> list = w.getEntities();
         startTotal += list.size();

         for(Entity e : list) {
            if (!startHash.containsKey(e.getType().getTypeId())) {
               startHash.put(e.getType().getTypeId(), 0);
            }

            startHash.put(e.getType().getTypeId(), (Integer)startHash.get(e.getType().getTypeId()) + 1);
         }
      }

      if (startTotal >= this.mustClearAmount) {
         clearLevel = this.mustClearLevel;
      } else {
         if (!force && this.serverManager.getServerStatus() == 0) {
            return;
         }

         if (!force && startTotal < this.startClearEntitys) {
            return;
         }
      }

      if (clearLevel == -1) {
         clearLevel = this.serverManager.getServerStatus();
      } else if (clearLevel < 0) {
         clearLevel = 0;
      } else if (clearLevel > 3) {
         clearLevel = 3;
      }

      this.server.broadcastMessage(UtilFormat.format("lib", "success", new Object[]{this.get(1205)}));
      this.server.broadcastMessage(UtilFormat.format(this.pn, "clearLevel", new Object[]{((Level)this.levelList.get(clearLevel)).getShow()}));
      if (((Level)this.levelList.get(clearLevel)).isEntity()) {
         if (this.tip) {
            this.server.broadcastMessage(this.get(1295) + this.get(1890));
         } else {
            Util.sendConsoleMessage(this.get(1295) + this.get(1890));
         }

         for(World w : this.server.getWorlds()) {
            if (!this.ignoreWorlds.has(w.getName())) {
               Iterator<Entity> it = w.getEntities().iterator();

               while(it.hasNext()) {
                  Entity e = (Entity)it.next();

                  try {
                     int id = e.getType().getTypeId();
                     if (id == 1) {
                        Item item = (Item)e;
                        if (item.getTicksLived() >= this.ticksLived) {
                           ItemStack is = item.getItemStack();
                           int itemId = is.getTypeId();
                           if (this.clearMode == 1 && !this.clearWhite.has(itemId) || this.clearMode == 2 && this.clearBlack.has(itemId)) {
                              BanClearMode bcm = this.canClear(e.getLocation());
                              if (bcm == null || !bcm.items) {
                                 e.remove();
                                 it.remove();
                              }
                           }
                        }
                     } else if (this.clearList.has(id)) {
                        BanClearMode bcm = this.canClear(e.getLocation());
                        if (bcm == null || !bcm.clear) {
                           e.remove();
                           it.remove();
                        }
                     }
                  } catch (Exception var23) {
                  }
               }
            }
         }
      } else if (this.tip) {
         this.server.broadcastMessage(this.get(1295) + this.get(1891));
      } else {
         Util.sendConsoleMessage(this.get(1295) + this.get(1891));
      }

      if (((Level)this.levelList.get(clearLevel)).isMonster()) {
         if (this.tip) {
            this.server.broadcastMessage(this.get(1305) + this.get(1890));
         } else {
            Util.sendConsoleMessage(this.get(1305) + this.get(1890));
         }

         for(World w : this.server.getWorlds()) {
            if (!this.ignoreWorlds.has(w.getName())) {
               Iterator<Monster> it = w.getEntitiesByClass(Monster.class).iterator();

               while(it.hasNext()) {
                  Monster mon = (Monster)it.next();
                  if (this.clearMonsterList.has(mon.getType().getTypeId())) {
                     BanClearMode bcm = this.canClear(mon.getLocation());
                     if (bcm == null || !bcm.monster) {
                        mon.remove();
                        it.remove();
                     }
                  }
               }
            }
         }
      } else if (this.tip) {
         this.server.broadcastMessage(this.get(1305) + this.get(1891));
      } else {
         Util.sendConsoleMessage(this.get(1305) + this.get(1891));
      }

      if (((Level)this.levelList.get(clearLevel)).isAnimal()) {
         if (this.tip) {
            this.server.broadcastMessage(this.get(1310) + this.get(1890));
         } else {
            Util.sendConsoleMessage(this.get(1310) + this.get(1890));
         }

         for(World w : this.server.getWorlds()) {
            HashMap<Integer, HashMap<Integer, Integer>> amountHash = new HashMap();
            HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> generateHash = new HashMap();
            HashMap<Integer, HashMap<Integer, Location>> locHash = new HashMap();
            if (!this.ignoreWorlds.has(w.getName())) {
               Iterator<Animals> it = w.getEntitiesByClass(Animals.class).iterator();

               while(it.hasNext()) {
                  Animals animals = (Animals)it.next();
                  int id = animals.getType().getTypeId();
                  if (this.clearTypes.containsKey(id)) {
                     int x = animals.getLocation().getBlockX() / this.gridSize;
                     int z = animals.getLocation().getBlockZ() / this.gridSize;
                     if (!amountHash.containsKey(x)) {
                        amountHash.put(x, new HashMap());
                     }

                     if (!((HashMap)amountHash.get(x)).containsKey(z)) {
                        ((HashMap)amountHash.get(x)).put(z, 0);
                     }

                     int current;
                     if ((current = (Integer)((HashMap)amountHash.get(x)).get(z)) >= this.maxPerGrid) {
                        BanClearMode bcm = this.canClear(animals.getLocation());
                        if (bcm == null || !bcm.animal) {
                           animals.remove();
                           it.remove();
                           if (this.r.nextInt(1000) < (Integer)this.clearTypes.get(id)) {
                              if (!generateHash.containsKey(x)) {
                                 generateHash.put(x, new HashMap());
                              }

                              if (!((HashMap)generateHash.get(x)).containsKey(z)) {
                                 ((HashMap)generateHash.get(x)).put(z, new HashMap());
                              }

                              if (!((HashMap)((HashMap)generateHash.get(x)).get(z)).containsKey(id)) {
                                 ((HashMap)((HashMap)generateHash.get(x)).get(z)).put(id, 0);
                              }

                              ((HashMap)((HashMap)generateHash.get(x)).get(z)).put(id, (Integer)((HashMap)((HashMap)generateHash.get(x)).get(z)).get(id) + 1);
                              if (!locHash.containsKey(x)) {
                                 locHash.put(x, new HashMap());
                              }

                              ((HashMap)locHash.get(x)).put(z, animals.getLocation());
                           }
                        }
                     } else {
                        ((HashMap)amountHash.get(x)).put(z, current + 1);
                     }
                  }
               }

               for(int x2 : generateHash.keySet()) {
                  for(int z2 : ((HashMap)generateHash.get(x2)).keySet()) {
                     try {
                        this.checkGenerateChest(w, x2, z2, (HashMap)((HashMap)generateHash.get(x2)).get(z2), (Location)((HashMap)locHash.get(x2)).get(z2));
                     } catch (Exception var22) {
                     }
                  }
               }
            }
         }
      } else if (this.tip) {
         this.server.broadcastMessage(this.get(1310) + this.get(1891));
      } else {
         Util.sendConsoleMessage(this.get(1310) + this.get(1891));
      }

      this.server.getScheduler().scheduleSyncDelayedTask(this.main, new DelayShow(startHash, startTotal), 35L);
   }

   private BanClearMode canClear(Location l) {
      if (l == null) {
         return null;
      } else {
         Land land = LandMain.getLandManager().getHighestPriorityLand(l);
         return land != null && land.hasFlag("banClear") ? (BanClearMode)this.banClearModeHash.get(land.getFlag("banClear")) : null;
      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.tip = config.getBoolean("clear.tip");
      this.ignoreWorlds = new HashListImpl();

      for(String s : config.getStringList("clear.ignoreWorlds")) {
         this.ignoreWorlds.add(s);
      }

      this.checkInterval = config.getInt("clear.checkInterval");
      this.startClearEntitys = config.getInt("clear.startClearEntitys");
      this.mustClearAmount = config.getInt("clear.mustClear.amount");
      this.mustClearLevel = config.getInt("clear.mustClear.level");
      this.levelList = new HashListImpl();

      String[] var9;
      for(String s : var9 = new String[]{"good", "fine", "bad", "unknown"}) {
         String show = Util.convert(config.getString("clear.clear." + s + ".show"));
         boolean entity = config.getBoolean("clear.clear." + s + ".entity");
         boolean monster = config.getBoolean("clear.clear." + s + ".monster");
         boolean animal = config.getBoolean("clear.clear." + s + ".animal");
         Level level = new Level(show, entity, monster, animal);
         this.levelList.add(level);
      }

      this.clearList = new HashListImpl();

      for(int i : config.getIntegerList("clear.entity.clear")) {
         this.clearList.add(i);
      }

      this.ticksLived = config.getInt("clear.entity.items.ticksLived");
      this.clearMode = config.getInt("clear.entity.items.mode");
      this.clearWhite = new HashListImpl();
      this.clearBlack = new HashListImpl();

      for(int i : config.getIntegerList("clear.entity.items.white")) {
         this.clearWhite.add(i);
      }

      for(int i : config.getIntegerList("clear.entity.items.black")) {
         this.clearBlack.add(i);
      }

      this.clearMonsterList = new HashListImpl();

      for(int i : config.getIntegerList("clear.monster.clear")) {
         this.clearMonsterList.add((short)i);
      }

      this.gridSize = config.getInt("clear.animal.gridSize");
      this.maxPerGrid = config.getInt("clear.animal.maxPerGrid");
      this.firstAll = config.getBoolean("clear.animal.firstAll");
      this.heightMax = config.getInt("clear.animal.heightMax");
      this.heightMin = config.getInt("clear.animal.heightMin");
      this.clearTypes = new HashMap();

      for(String s : config.getStringList("clear.animal.clearTypes")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         int chance = Integer.parseInt(s.split(" ")[1]);
         this.clearTypes.put(id, chance);
      }

      this.airBlocks = new HashListImpl();

      for(int i : config.getIntegerList("clear.animal.airBlocks")) {
         this.airBlocks.add(i);
      }

      this.banClearModeHash = new HashMap();
      MemorySection ms = (MemorySection)config.get("clear.banClear");

      for(String key : ms.getValues(false).keySet()) {
         int id = Integer.parseInt(key.substring(4));
         boolean clear = ms.getBoolean(key + ".clear");
         boolean items = ms.getBoolean(key + ".items");
         boolean mon = ms.getBoolean(key + ".monster");
         boolean ani = ms.getBoolean(key + ".animal");
         BanClearMode banClearMode = new BanClearMode(clear, items, mon, ani);
         this.banClearModeHash.put(id, banClearMode);
      }

   }

   private void checkGenerateChest(World w, int x, int z, HashMap hash, Location l) {
      if (this.firstAll) {
         for(int y2 = l.getBlockY() - this.heightMin; y2 <= l.getBlockY() + this.heightMax; ++y2) {
            for(int x2 = x * this.gridSize; x2 < x * (this.gridSize + 1); ++x2) {
               for(int z2 = z * this.gridSize; z2 < z * (this.gridSize + 1); ++z2) {
                  if (w.getBlockTypeIdAt(x2, y2, z2) == 54) {
                     Chest chest = (Chest)w.getBlockAt(x2, y2, z2).getState();
                     Inventory inventory = chest.getBlockInventory();

                     for(int id : hash.keySet()) {
                        short type = (short)id;
                        inventory.addItem(new ItemStack[]{new ItemStack(383, (Integer)hash.get(id), type)});
                     }

                     return;
                  }
               }
            }
         }
      }

      int xx = l.getBlockX();
      int zz = l.getBlockZ();
      if (this.airBlocks.has(w.getBlockAt(xx, l.getBlockY(), zz).getTypeId())) {
         for(int yy = l.getBlockY() - 1; yy > 0; --yy) {
            if (!this.airBlocks.has(w.getBlockAt(xx, yy, zz).getTypeId())) {
               if (w.getBlockAt(xx, yy, zz).getTypeId() != 54) {
                  ++yy;
                  w.getBlockAt(xx, yy, zz).setTypeId(54);
               }

               Chest chest = (Chest)w.getBlockAt(xx, yy, zz).getState();
               Inventory inventory = chest.getBlockInventory();

               for(int id : hash.keySet()) {
                  short type = (short)id;
                  inventory.addItem(new ItemStack[]{new ItemStack(383, (Integer)hash.get(id), type)});
               }

               return;
            }
         }

         for(int yy = 254; yy > l.getBlockY(); --yy) {
            if (!this.airBlocks.has(w.getBlockAt(xx, yy, zz).getTypeId())) {
               if (w.getBlockAt(xx, yy, zz).getTypeId() != 54) {
                  ++yy;
                  w.getBlockAt(xx, yy, zz).setTypeId(54);
               }

               Chest chest = (Chest)w.getBlockAt(xx, yy, zz).getState();
               Inventory inventory = chest.getBlockInventory();

               for(int id : hash.keySet()) {
                  short type = (short)id;
                  inventory.addItem(new ItemStack[]{new ItemStack(383, (Integer)hash.get(id), type)});
               }

               return;
            }
         }
      } else {
         for(int yy = l.getBlockY() + 1; yy < 255; ++yy) {
            if (this.airBlocks.has(w.getBlockAt(xx, yy, zz).getTypeId())) {
               if (w.getBlockAt(xx, yy - 1, zz).getTypeId() == 54) {
                  --yy;
               } else {
                  w.getBlockAt(xx, yy, zz).setTypeId(54);
               }

               Chest chest = (Chest)w.getBlockAt(xx, yy, zz).getState();
               Inventory inventory = chest.getBlockInventory();

               for(int id : hash.keySet()) {
                  short type = (short)id;
                  inventory.addItem(new ItemStack[]{new ItemStack(383, (Integer)hash.get(id), type)});
               }

               return;
            }
         }

         for(int yy = l.getBlockY() - 1; yy > 0; --yy) {
            if (this.airBlocks.has(w.getBlockAt(xx, yy, zz).getTypeId())) {
               for(int yyy = yy - 1; yyy > 0; --yyy) {
                  if (!this.airBlocks.has(w.getBlockAt(xx, yyy, zz).getTypeId())) {
                     if (w.getBlockAt(xx, yyy, zz).getTypeId() != 54) {
                        ++yyy;
                        w.getBlockAt(xx, yyy, zz).setTypeId(54);
                     }

                     Chest chest = (Chest)w.getBlockAt(xx, yyy, zz).getState();
                     Inventory inventory = chest.getBlockInventory();

                     for(int id : hash.keySet()) {
                        short type = (short)id;
                        inventory.addItem(new ItemStack[]{new ItemStack(383, (Integer)hash.get(id), type)});
                     }

                     return;
                  }
               }
            }
         }
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   private class BanClearMode {
      boolean clear;
      boolean items;
      boolean monster;
      boolean animal;

      public BanClearMode(boolean clear, boolean items, boolean monster, boolean animal) {
         super();
         this.clear = clear;
         this.items = items;
         this.monster = monster;
         this.animal = animal;
      }
   }

   class Level {
      private String show;
      private boolean entity;
      private boolean monster;
      private boolean animal;

      public Level(String show, boolean entity, boolean monster, boolean animal) {
         super();
         this.show = show;
         this.entity = entity;
         this.monster = monster;
         this.animal = animal;
      }

      public String getShow() {
         return this.show;
      }

      public boolean isEntity() {
         return this.entity;
      }

      public boolean isMonster() {
         return this.monster;
      }

      public boolean isAnimal() {
         return this.animal;
      }
   }

   class ClearTimer implements Runnable {
      ClearTimer() {
         super();
      }

      public void run() {
         Clear.this.clear(false, -1);
         Clear.this.main.getServer().getScheduler().scheduleSyncDelayedTask(Clear.this.main, Clear.this.clearTimer, (long)(Clear.this.checkInterval * 20));
      }
   }

   class DelayShow implements Runnable {
      HashMap startHash;
      int startTotal;

      public DelayShow(HashMap startHash, int startTotal) {
         super();
         this.startHash = startHash;
         this.startTotal = startTotal;
      }

      public void run() {
         HashMap<Short, Integer> endHash = new HashMap();
         int endTotal = 0;

         for(World w : Clear.this.server.getWorlds()) {
            List<Entity> list2 = w.getEntities();
            endTotal += list2.size();

            for(Entity e : list2) {
               if (!endHash.containsKey(e.getType().getTypeId())) {
                  endHash.put(e.getType().getTypeId(), 0);
               }

               endHash.put(e.getType().getTypeId(), (Integer)endHash.get(e.getType().getTypeId()) + 1);
            }
         }

         Clear.this.server.broadcastMessage(UtilFormat.format("lib", "success", new Object[]{Clear.this.get(1210)}));

         for(short s : this.startHash.keySet()) {
            int end;
            if (endHash.containsKey(s)) {
               end = (Integer)endHash.get(s);
            } else {
               end = 0;
            }

            String show = UtilNames.getEntityName(s);
            if (Clear.this.tip) {
               Clear.this.server.broadcastMessage(UtilFormat.format(Clear.this.pn, "clearInfo", new Object[]{show, this.startHash.get(s), end}));
            } else {
               Util.sendConsoleMessage(UtilFormat.format(Clear.this.pn, "clearInfo", new Object[]{show, this.startHash.get(s), end}));
            }

            endHash.remove(s);
         }

         for(Short s : endHash.keySet()) {
            String show = UtilNames.getEntityName(s);
            if (Clear.this.tip) {
               Clear.this.server.broadcastMessage(UtilFormat.format(Clear.this.pn, "clearInfo", new Object[]{show, 0, endHash.get(s)}));
            } else {
               Util.sendConsoleMessage(UtilFormat.format(Clear.this.pn, "clearInfo", new Object[]{show, 0, endHash.get(s)}));
            }
         }

         Clear.this.server.broadcastMessage(UtilFormat.format(Clear.this.pn, "clearInfo3", new Object[]{this.startTotal, endTotal}));
      }
   }
}
