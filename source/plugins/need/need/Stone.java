package need;

import java.util.ArrayList;
import java.util.List;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Stone implements Listener {
   private static final ItemMeta IM = (new ItemStack(1)).getItemMeta();
   private String pn;
   private String per_need_admin;
   private String stoneFlagServer;
   private String stoneFlagPlayer;
   private String stoneTip;
   private String forever;

   public Stone(Main main) {
      super();
      this.pn = main.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      main.getPm().registerEvents(this, main);
   }

   public void onCommand(CommandSender sender, String[] args) {
      int length = args.length;
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(40)}));
      } else if (UtilPer.checkPer(p, this.per_need_admin)) {
         if (length != 1 || !args[0].equalsIgnoreCase("?")) {
            if (length == 1) {
               if (args[0].equalsIgnoreCase("server")) {
                  this.setStone(p, true);
                  return;
               }

               if (args[0].equalsIgnoreCase("player")) {
                  this.setStone(p, false);
                  return;
               }
            } else if (length >= 2) {
               if (length == 2 && args[0].equalsIgnoreCase("forever")) {
                  if (args[1].equalsIgnoreCase("true")) {
                     this.setForever(p, true);
                     return;
                  }

                  if (args[1].equalsIgnoreCase("false")) {
                     this.setForever(p, false);
                     return;
                  }
               } else {
                  if (args[0].equalsIgnoreCase("lore") && args[1].equalsIgnoreCase("remove")) {
                     this.removeLore(p);
                     return;
                  }

                  if (args[0].equalsIgnoreCase("name")) {
                     String content = Util.convert(Util.combine(args, " ", 1, length));
                     this.setName(p, content);
                     return;
                  }

                  if (args[0].equalsIgnoreCase("cmd")) {
                     String content = Util.convert(Util.combine(args, " ", 1, length));
                     this.setCmd(p, content);
                     return;
                  }

                  if (args[0].equalsIgnoreCase("lore") && args[1].equalsIgnoreCase("add")) {
                     String content = Util.convert(Util.combine(args, " ", 2, length));
                     this.addLore(p, content);
                     return;
                  }
               }
            }
         }

         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", new Object[]{this.get(3235)}));
         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(3240), this.get(3245)}));
         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(3250), this.get(3255)}));
         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(3260), this.get(3265)}));
         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(3270), this.get(3275)}));
         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(3280), this.get(3285)}));
         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(3325), this.get(3330)}));
      }
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
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void playerInteract(PlayerInteractEvent e) {
      if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
         ItemStack is = e.getItem();
         if (is != null && is.getTypeId() != 0 && is.hasItemMeta()) {
            ItemMeta im = is.getItemMeta();
            if (im.hasLore()) {
               List<String> lore = im.getLore();
               if (lore.size() >= 2) {
                  if (((String)lore.get(0)).equals(this.stoneFlagServer)) {
                     String cmd = this.getCmd((String)lore.get(1));
                     if (cmd != null) {
                        e.setCancelled(true);
                        cmd = cmd.replaceAll("<p>", e.getPlayer().getName());
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
                        if (!this.isForever(is)) {
                           int amount = is.getAmount();
                           if (amount > 1) {
                              e.getPlayer().getItemInHand().setAmount(amount - 1);
                           } else {
                              e.getPlayer().setItemInHand((ItemStack)null);
                           }

                           e.getPlayer().updateInventory();
                        }
                     }
                  } else if (((String)lore.get(0)).equalsIgnoreCase(this.stoneFlagPlayer)) {
                     String cmd = this.getCmd((String)lore.get(1));
                     if (cmd != null) {
                        e.setCancelled(true);
                        cmd = cmd.replaceAll("<p>", e.getPlayer().getName());
                        e.getPlayer().chat(cmd);
                        if (!this.isForever(is)) {
                           int amount = is.getAmount();
                           if (amount > 1) {
                              e.getPlayer().getItemInHand().setAmount(amount - 1);
                           } else {
                              e.getPlayer().setItemInHand((ItemStack)null);
                           }

                           e.getPlayer().updateInventory();
                        }
                     }
                  }
               }
            }
         }
      }

   }

   private void setForever(Player p, boolean forever) {
      ItemStack is = p.getItemInHand();
      if (is != null && is.getTypeId() != 0) {
         if (!(this.isForever(is) ^ forever)) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(3340)}));
         } else {
            this.setForever(is, forever);
            p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(3345)}));
         }
      } else {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(3335)}));
      }
   }

   private void setName(Player p, String content) {
      ItemStack is = p.getItemInHand();
      if (is != null && is.getTypeId() != 0) {
         ItemMeta im = is.getItemMeta();
         if (im == null) {
            im = IM.clone();
         }

         im.setDisplayName(content);
         is.setItemMeta(im);
         p.updateInventory();
         p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(3295)}));
      } else {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(3290)}));
      }
   }

   private void setCmd(Player p, String content) {
      ItemStack is = p.getItemInHand();
      if (is != null && is.getTypeId() != 0) {
         ItemMeta im = is.getItemMeta();
         if (im == null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(3310)}));
         } else {
            List<String> lore = im.getLore();
            if (lore != null && lore.size() >= 1) {
               String result = this.stoneTip + content;
               if (lore.size() == 1) {
                  lore.add(result);
               } else {
                  lore.set(1, result);
               }

               im.setLore(lore);
               is.setItemMeta(im);
               p.updateInventory();
               p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(3295)}));
            } else {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(3310)}));
            }
         }
      } else {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(3290)}));
      }
   }

   private void setStone(Player p, boolean server) {
      ItemStack is = p.getItemInHand();
      if (is != null && is.getTypeId() != 0) {
         ItemMeta im = is.getItemMeta();
         if (im == null) {
            im = IM.clone();
         }

         List<String> lore = im.getLore();
         if (lore == null) {
            lore = new ArrayList();
         }

         String result;
         if (server) {
            result = this.stoneFlagServer;
         } else {
            result = this.stoneFlagPlayer;
         }

         if (lore.size() >= 1) {
            lore.set(0, result);
         } else {
            lore.add(result);
         }

         im.setLore(lore);
         is.setItemMeta(im);
         p.updateInventory();
         p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(3295)}));
      } else {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(3290)}));
      }
   }

   private void addLore(Player p, String content) {
      ItemStack is = p.getItemInHand();
      if (is != null && is.getTypeId() != 0) {
         ItemMeta im = is.getItemMeta();
         if (im == null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(3315)}));
         } else {
            List<String> lore = im.getLore();
            if (lore != null && lore.size() >= 2) {
               lore.add(content);
               im.setLore(lore);
               is.setItemMeta(im);
               p.updateInventory();
               p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(3320)}));
            } else {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(3315)}));
            }
         }
      } else {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(3290)}));
      }
   }

   private void removeLore(Player p) {
      ItemStack is = p.getItemInHand();
      if (is != null && is.getTypeId() != 0) {
         ItemMeta im = is.getItemMeta();
         if (im == null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(3300)}));
         } else {
            List<String> lore = im.getLore();
            if (lore != null && lore.size() >= 3) {
               lore.remove(lore.size() - 1);
               im.setLore(lore);
               is.setItemMeta(im);
               p.updateInventory();
               p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(3305)}));
            } else {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(3300)}));
            }
         }
      } else {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(3290)}));
      }
   }

   private boolean isForever(ItemStack is) {
      if (is != null) {
         ItemMeta im = is.getItemMeta();
         if (im != null && im.hasLore()) {
            for(String s : im.getLore()) {
               if (s.equals(this.forever)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private void setForever(ItemStack is, boolean forever) {
      if (is != null) {
         if (this.isForever(is) ^ forever) {
            if (!is.hasItemMeta()) {
               is.setItemMeta(IM.clone());
            }

            ItemMeta im = is.getItemMeta();
            if (im.getLore() == null) {
               im.setLore(new ArrayList());
            }

            List<String> lore = im.getLore();
            if (forever) {
               lore.add(this.forever);
            } else {
               for(int i = 0; i < lore.size(); ++i) {
                  String s = (String)lore.get(i);
                  if (s.equals(this.forever)) {
                     lore.remove(i);
                     break;
                  }
               }
            }

            im.setLore(lore);
            is.setItemMeta(im);
         }

      }
   }

   private String getCmd(String s) {
      return s.length() > this.stoneTip.length() ? s.substring(this.stoneTip.length(), s.length()) : null;
   }

   private void loadConfig(FileConfiguration config) {
      this.per_need_admin = config.getString("per_need_admin");
      this.stoneFlagServer = Util.convert(config.getString("stone.flag.server"));
      this.stoneFlagPlayer = Util.convert(config.getString("stone.flag.player"));
      this.stoneTip = Util.convert(config.getString("stone.tip"));
      this.forever = Util.convert(config.getString("stone.forever"));
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
