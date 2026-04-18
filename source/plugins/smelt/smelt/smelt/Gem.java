package smelt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import lib.config.ReloadConfigEvent;
import lib.realDamage.RealDamageEvent;
import lib.types.InvalidTypeException;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilNames;
import lib.util.UtilPer;
import lib.util.UtilSpeed;
import lib.util.UtilTypes;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import smelt.effect.Attack;
import smelt.effect.Blindness;
import smelt.effect.Clear;
import smelt.effect.Confusion;
import smelt.effect.Crit;
import smelt.effect.DamageRes;
import smelt.effect.Effect;
import smelt.effect.Health;
import smelt.effect.HeavyArmor;
import smelt.effect.Hide;
import smelt.effect.Hunger;
import smelt.effect.InstantRecover;
import smelt.effect.Poison;
import smelt.effect.RecoverAll;
import smelt.effect.Regeneration;
import smelt.effect.Slow;
import smelt.effect.Speed;
import smelt.effect.SpeedIncrease;
import smelt.effect.Suck;
import smelt.effect.Weakness;

public class Gem implements Listener {
   private static final HashMap HASH = new HashMap();
   private static final String SPEED = "gem";
   private Random r = new Random();
   private Server server;
   private String pn;
   private Star star;
   private Protect protect;
   private String per_smelt_vip;
   private int interval;
   private int vipAddEffect;
   private int vipAddUpgrade;
   private int vipAddCombine;
   private String attackType;
   private String bowType;
   private String armorType;
   private String upgradeUp;
   private String upgradeEnd;
   private int upgradeOver;
   private HashMap upgradeMax;
   private HashMap upgradeChance;
   private boolean gemTip;
   private HashMap combineChanceHash;
   private String check;
   private HashMap data;
   private HashMap displayHash;
   private HashMap nameHash;
   private String show;
   private HashMap effectChanceHash;
   private HashMap effectLimitHash;
   private String effectPrefix;
   private HashMap effectNameHash;
   private HashMap effectTypeHash;
   private HashMap attackHash;
   private HashMap bowHash;
   private HashMap armorHash;
   private HashMap toolHash;
   private static HashMap tempHash;
   private HashMap suitHash;

   static {
      HASH.put(1, "I");
      HASH.put(2, "II");
      HASH.put(3, "III");
      HASH.put(4, "IV");
      HASH.put(5, "V");
      HASH.put(6, "VI");
      HASH.put(7, "VII");
      HASH.put(8, "VIII");
      HASH.put(9, "IX");
      HASH.put(10, "X");
      tempHash = new HashMap();
   }

   public Gem(Main main) {
      super();
      this.server = main.getServer();
      this.pn = main.getPn();
      this.star = main.getStar();
      this.protect = main.getProtect();
      Attack attack = new Attack();
      tempHash.put(attack.getId(), attack);
      Speed speed = new Speed();
      tempHash.put(speed.getId(), speed);
      Health health = new Health();
      tempHash.put(health.getId(), health);
      Crit crit = new Crit(main);
      tempHash.put(crit.getId(), crit);
      InstantRecover ir = new InstantRecover(main);
      tempHash.put(ir.getId(), ir);
      Suck suck = new Suck(main);
      tempHash.put(suck.getId(), suck);
      HeavyArmor heavyArmor = new HeavyArmor();
      tempHash.put(heavyArmor.getId(), heavyArmor);
      RecoverAll recoverAll = new RecoverAll(main);
      tempHash.put(recoverAll.getId(), recoverAll);
      Poison poison = new Poison(main);
      tempHash.put(poison.getId(), poison);
      Blindness blindness = new Blindness(main);
      tempHash.put(blindness.getId(), blindness);
      Clear clear = new Clear(main);
      tempHash.put(clear.getId(), clear);
      Confusion confusion = new Confusion(main);
      tempHash.put(confusion.getId(), confusion);
      Hunger hunger = new Hunger(main);
      tempHash.put(hunger.getId(), hunger);
      Slow slow = new Slow(main);
      tempHash.put(slow.getId(), slow);
      Weakness weakness = new Weakness(main);
      tempHash.put(weakness.getId(), weakness);
      DamageRes damageRes = new DamageRes(main);
      tempHash.put(damageRes.getId(), damageRes);
      Regeneration regeneration = new Regeneration(main);
      tempHash.put(regeneration.getId(), regeneration);
      Hide hide = new Hide(main);
      tempHash.put(hide.getId(), hide);
      SpeedIncrease speedIncrease = new SpeedIncrease(main);
      tempHash.put(speedIncrease.getId(), speedIncrease);
      this.loadConfig(UtilConfig.getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, main);
      UtilSpeed.register(this.pn, "gem");
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
   public void onAttackLowest(RealDamageEvent e) {
      if (e.getVictim() instanceof Player) {
         Player p = (Player)e.getVictim();
         if (p.isOnline()) {
            this.checkAttacked(e, p, p.getInventory().getHelmet());
            this.checkAttacked(e, p, p.getInventory().getChestplate());
            this.checkAttacked(e, p, p.getInventory().getLeggings());
            this.checkAttacked(e, p, p.getInventory().getBoots());
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onAttack(RealDamageEvent e) {
      try {
         LivingEntity damager = e.getDamager();
         if (damager.isValid() && damager instanceof Player) {
            Player p = (Player)damager;
            if (p.isOnline()) {
               ItemStack is = p.getItemInHand();
               if (is == null) {
                  return;
               }

               if (!e.isProjectile()) {
                  if (!UtilTypes.checkItem(this.pn, this.attackType, String.valueOf(is.getTypeId()))) {
                     return;
                  }

                  ItemMeta im = is.getItemMeta();
                  if (im == null) {
                     return;
                  }

                  List<String> lore = im.getLore();
                  if (lore == null) {
                     return;
                  }

                  for(int i = 1; i < lore.size(); ++i) {
                     if (((String)lore.get(i)).startsWith(this.effectPrefix)) {
                        try {
                           Effect effect = (Effect)this.attackHash.get(((String)lore.get(i)).split(" ")[1]);
                           if (effect != null) {
                              String name = (String)this.effectNameHash.get(effect.getId());
                              int result = Integer.parseInt(((String)lore.get(i)).substring(this.effectPrefix.length(), ((String)lore.get(i)).length() - name.length()));
                              effect.onAttack(e, p, result);
                           }
                        } catch (Exception var12) {
                        }
                     }
                  }
               } else {
                  if (!UtilTypes.checkItem(this.pn, this.bowType, String.valueOf(is.getTypeId()))) {
                     return;
                  }

                  ItemMeta im = is.getItemMeta();
                  if (im == null) {
                     return;
                  }

                  List<String> lore = im.getLore();
                  if (lore == null) {
                     return;
                  }

                  for(int i = 1; i < lore.size(); ++i) {
                     if (((String)lore.get(i)).startsWith(this.effectPrefix)) {
                        try {
                           Effect effect = (Effect)this.bowHash.get(((String)lore.get(i)).split(" ")[1]);
                           if (effect != null) {
                              String name = (String)this.effectNameHash.get(effect.getId());
                              int result = Integer.parseInt(((String)lore.get(i)).substring(this.effectPrefix.length(), ((String)lore.get(i)).length() - name.length()));
                              effect.onBow(e, p, result);
                           }
                        } catch (Exception var11) {
                        }
                     }
                  }
               }
            }
         }
      } catch (Exception var13) {
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      if (!Main.isIgnored(e.getClickedBlock())) {
         if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            ItemStack is0 = e.getPlayer().getItemInHand();
            GemInfo gemInfo = this.getGemInfo(is0);
            if (gemInfo != null) {
               e.setCancelled(true);
               if (UtilSpeed.check(e.getPlayer(), this.pn, "gem", this.interval)) {
                  int id = is0.getTypeId();
                  ItemStack is = e.getPlayer().getInventory().getItem(0);
                  if (is != null && is.getTypeId() != 0) {
                     GemInfo gi = this.getGemInfo(is);
                     if (gi != null && gi.equals(gemInfo)) {
                        if (e.getPlayer().getInventory().getHeldItemSlot() != 0) {
                           if (UtilItems.getEmptySlots(e.getPlayer().getInventory()) <= 0) {
                              e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(130)}));
                           } else if (!gi.isUpgradeable()) {
                              e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(180)}));
                           } else {
                              int chance = (Integer)((List)this.combineChanceHash.get(gemInfo.getId())).get(gemInfo.getLevel() - 1);
                              if (UtilPer.hasPer(e.getPlayer(), this.per_smelt_vip)) {
                                 chance = chance * (100 + this.vipAddCombine) / 100;
                              }

                              if (this.r.nextInt(100) < chance) {
                                 boolean unupgradeable = false;
                                 if (this.upgradeMax.containsKey(gemInfo.getId())) {
                                    int max = (Integer)((HashMap)this.upgradeMax.get(gemInfo.getId())).get(id);
                                    if (gemInfo.getLevel() + 1 >= max) {
                                       unupgradeable = true;
                                    }
                                 }

                                 ItemStack result = is0.clone();
                                 result.setAmount(1);
                                 this.setLevel(result, gemInfo.getId(), gemInfo.getLevel() + 1);
                                 if (unupgradeable) {
                                    this.addUpgradeOver(result);
                                 }

                                 if (is0.getAmount() <= 1) {
                                    e.getPlayer().getInventory().setItemInHand((ItemStack)null);
                                 } else {
                                    is0.setAmount(is0.getAmount() - 1);
                                 }

                                 if (is.getAmount() <= 1) {
                                    e.getPlayer().getInventory().setItem(0, (ItemStack)null);
                                 } else {
                                    is.setAmount(is.getAmount() - 1);
                                 }

                                 e.getPlayer().getInventory().addItem(new ItemStack[]{result});
                                 e.getPlayer().updateInventory();
                                 String vip = this.get(45);
                                 if (UtilPer.hasPer(e.getPlayer(), this.per_smelt_vip)) {
                                    vip = "";
                                 }

                                 e.getPlayer().sendMessage(UtilFormat.format(this.pn, "combine1", new Object[]{vip}));
                                 if (this.gemTip && unupgradeable) {
                                    this.server.broadcastMessage(UtilFormat.format(this.pn, "combineTip", new Object[]{e.getPlayer().getName(), is0.getItemMeta().getDisplayName(), gemInfo.getLevel() + 1}));
                                 }

                                 e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.valueOf("SUCCESSFUL_HIT"), 2.0F, 1.0F);
                              } else {
                                 if (is0.getAmount() <= 1) {
                                    this.addUpgradeOver(is0);
                                 } else {
                                    is0.setAmount(is0.getAmount() - 1);
                                    ItemStack result = is0.clone();
                                    result.setAmount(1);
                                    this.addUpgradeOver(result);
                                    e.getPlayer().getInventory().addItem(new ItemStack[]{result});
                                 }

                                 if (is.getAmount() <= 1) {
                                    this.addUpgradeOver(is);
                                 } else {
                                    is.setAmount(is.getAmount() - 1);
                                    ItemStack result = is.clone();
                                    result.setAmount(1);
                                    this.addUpgradeOver(result);
                                    e.getPlayer().getInventory().addItem(new ItemStack[]{result});
                                 }

                                 e.getPlayer().updateInventory();
                                 String vip = this.get(45);
                                 if (UtilPer.hasPer(e.getPlayer(), this.per_smelt_vip)) {
                                    vip = "";
                                 }

                                 e.getPlayer().sendMessage(UtilFormat.format(this.pn, "combine2", new Object[]{vip}));
                                 e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.ZOMBIE_METAL, 1.8F, 1.0F);
                              }
                           }
                        }
                     } else if (is.getTypeId() == id) {
                        if (UtilItems.getEmptySlots(e.getPlayer().getInventory()) <= 0) {
                           e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(130)}));
                        } else if (!this.isNormal(is)) {
                           e.getPlayer().sendMessage(UtilFormat.format(this.pn, "gem1", new Object[]{UtilNames.getItemName(id, 0)}));
                        } else if (!gemInfo.isUpgradeable()) {
                           e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(95)}));
                        } else {
                           if (is.getAmount() <= 1) {
                              e.getPlayer().getInventory().setItem(0, (ItemStack)null);
                           } else {
                              is.setAmount(is.getAmount() - 1);
                           }

                           boolean unupgradeable = false;

                           try {
                              int chance = (Integer)((List)this.upgradeChance.get(gemInfo.getId())).get(gemInfo.getLevel() - 1);
                              if (UtilPer.hasPer(e.getPlayer(), this.per_smelt_vip)) {
                                 chance = chance * (100 + this.vipAddUpgrade) / 100;
                              }

                              if (this.r.nextInt(100) < chance) {
                                 if (this.upgradeMax.containsKey(gemInfo.getId())) {
                                    int max = (Integer)((HashMap)this.upgradeMax.get(gemInfo.getId())).get(id);
                                    if (gemInfo.getLevel() + 1 >= max) {
                                       unupgradeable = true;
                                    }
                                 }

                                 if (is0.getAmount() <= 1) {
                                    this.setLevel(is0, gemInfo.getId(), gemInfo.getLevel() + 1);
                                    if (unupgradeable) {
                                       this.addUpgradeOver(is0);
                                    }
                                 } else {
                                    is0.setAmount(is0.getAmount() - 1);
                                    ItemStack result = is0.clone();
                                    result.setAmount(1);
                                    this.setLevel(result, gemInfo.getId(), gemInfo.getLevel() + 1);
                                    if (unupgradeable) {
                                       this.addUpgradeOver(result);
                                    }

                                    e.getPlayer().getInventory().addItem(new ItemStack[]{result});
                                 }

                                 e.getPlayer().updateInventory();
                                 String vip = this.get(45);
                                 if (UtilPer.hasPer(e.getPlayer(), this.per_smelt_vip)) {
                                    vip = "";
                                 }

                                 e.getPlayer().sendMessage(UtilFormat.format(this.pn, "upgrade1", new Object[]{vip}));
                                 if (this.gemTip && unupgradeable) {
                                    this.server.broadcastMessage(UtilFormat.format(this.pn, "upgradeTip", new Object[]{e.getPlayer().getName(), is0.getItemMeta().getDisplayName(), gemInfo.getLevel() + 1}));
                                 }

                                 e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.valueOf("SUCCESSFUL_HIT"), 2.0F, 1.0F);
                                 return;
                              }
                           } catch (Exception var14) {
                              unupgradeable = true;
                           }

                           if (this.r.nextInt(100) < this.upgradeOver) {
                              unupgradeable = true;
                           }

                           if (unupgradeable) {
                              if (is0.getAmount() <= 1) {
                                 this.addUpgradeOver(is0);
                              } else {
                                 is0.setAmount(is0.getAmount() - 1);
                                 ItemStack result = is0.clone();
                                 result.setAmount(1);
                                 this.addUpgradeOver(result);
                                 e.getPlayer().getInventory().addItem(new ItemStack[]{result});
                              }
                           }

                           String vip = this.get(45);
                           if (UtilPer.hasPer(e.getPlayer(), this.per_smelt_vip)) {
                              vip = "";
                           }

                           e.getPlayer().sendMessage(UtilFormat.format(this.pn, "upgrade2", new Object[]{vip}));
                           e.getPlayer().updateInventory();
                           e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.ZOMBIE_METAL, 1.8F, 1.0F);
                        }
                     } else {
                        try {
                           if (!UtilTypes.checkItem(this.pn, (String)this.effectTypeHash.get(id), String.valueOf(is.getTypeId()))) {
                              e.getPlayer().sendMessage(UtilFormat.format(this.pn, "gemErr", new Object[]{is0.getItemMeta().getDisplayName(), UtilNames.getItemName(is.getTypeId(), is.getDurability())}));
                              return;
                           }
                        } catch (InvalidTypeException e1) {
                           e1.printStackTrace();
                        }

                        if (UtilItems.getEmptySlots(e.getPlayer().getInventory()) <= 0) {
                           e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(130)}));
                        } else {
                           try {
                              if (!UtilTypes.checkItem(this.pn, (String)this.suitHash.get(gemInfo.getId()), String.valueOf(is.getTypeId()))) {
                                 e.getPlayer().sendMessage(UtilFormat.format(this.pn, "gemErr2", new Object[]{is0.getItemMeta().getDisplayName(), UtilNames.getItemName(is.getTypeId(), 0)}));
                                 return;
                              }
                           } catch (InvalidTypeException e1) {
                              e1.printStackTrace();
                              return;
                           }

                           StarInfo starInfo = this.star.getStarInfo(is);
                           if (starInfo == null) {
                              e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(135)}));
                           } else if (starInfo.getTotal() <= starInfo.getFill()) {
                              e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(105)}));
                           } else {
                              int level = 1;
                              if (starInfo != null) {
                                 level = starInfo.getFill() + 1;
                              }

                              int chance = 0;
                              if (this.effectChanceHash.containsKey(level)) {
                                 chance = (Integer)this.effectChanceHash.get(level);
                                 if (UtilPer.hasPer(e.getPlayer(), this.per_smelt_vip)) {
                                    chance = chance * (100 + this.vipAddEffect) / 100;
                                 }
                              }

                              if (this.r.nextInt(100) < chance) {
                                 if (is0.getAmount() <= 1) {
                                    e.getPlayer().setItemInHand((ItemStack)null);
                                 } else {
                                    is0.setAmount(is0.getAmount() - 1);
                                 }

                                 starInfo.setFill(starInfo.getFill() + 1);
                                 this.star.setStar(is, starInfo);
                                 int data = this.getEffectData(is, gemInfo.getId());
                                 data += (Integer)((HashMap)this.data.get(gemInfo.getId())).get(gemInfo.getLevel());
                                 if (data > (Integer)this.effectLimitHash.get(gemInfo.getId())) {
                                    data = (Integer)this.effectLimitHash.get(gemInfo.getId());
                                 }

                                 ItemStack result = this.setEffectData(is, gemInfo.getId(), data);
                                 if (result != null) {
                                    e.getPlayer().getInventory().setItem(0, result);
                                 }

                                 e.getPlayer().updateInventory();
                                 String vip = this.get(45);
                                 if (UtilPer.hasPer(e.getPlayer(), this.per_smelt_vip)) {
                                    vip = "";
                                 }

                                 e.getPlayer().sendMessage(UtilFormat.format(this.pn, "effect1", new Object[]{vip}));
                                 e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.valueOf("SUCCESSFUL_HIT"), 2.0F, 1.0F);
                              } else {
                                 e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.ZOMBIE_METAL, 1.8F, 1.0F);
                                 String vip = this.get(45);
                                 if (UtilPer.hasPer(e.getPlayer(), this.per_smelt_vip)) {
                                    vip = "";
                                 }

                                 e.getPlayer().sendMessage(UtilFormat.format(this.pn, "effect2", new Object[]{vip}));
                                 ItemStack is1 = e.getPlayer().getInventory().getItem(1);
                                 boolean hasProtect = this.protect.isProtect(is1) && id == is1.getTypeId();
                                 if (hasProtect) {
                                    if (is1.getAmount() <= 1) {
                                       e.getPlayer().getInventory().setItem(1, (ItemStack)null);
                                    } else {
                                       is1.setAmount(is1.getAmount() - 1);
                                    }

                                    e.getPlayer().sendMessage(UtilFormat.format(this.pn, "tip", new Object[]{this.get(120)}));
                                    e.getPlayer().updateInventory();
                                 } else if (gemInfo.getLevel() <= 1) {
                                    if (is0.getAmount() <= 1) {
                                       e.getPlayer().setItemInHand((ItemStack)null);
                                    } else {
                                       is0.setAmount(is0.getAmount() - 1);
                                    }

                                    e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(125)}));
                                    e.getPlayer().updateInventory();
                                 } else {
                                    if (is0.getAmount() <= 1) {
                                       this.setLevel(is0, gemInfo.getId(), gemInfo.getLevel() - 1);
                                    } else {
                                       is0.setAmount(is0.getAmount() - 1);
                                       ItemStack result = is0.clone();
                                       result.setAmount(1);
                                       this.setLevel(result, gemInfo.getId(), gemInfo.getLevel() - 1);
                                       e.getPlayer().getInventory().addItem(new ItemStack[]{result});
                                    }

                                    e.getPlayer().sendMessage(UtilFormat.format(this.pn, "gemErr1", new Object[]{gemInfo.getLevel()}));
                                    e.getPlayer().updateInventory();
                                 }
                              }
                           }
                        }
                     }
                  } else {
                     e.getPlayer().sendMessage(UtilFormat.format(this.pn, "gemErr0", new Object[]{UtilNames.getItemName(id, 0)}));
                  }
               }
            }
         }
      }
   }

   public int getEffectData(ItemStack is, int id) {
      try {
         if (is == null) {
            return 0;
         }

         ItemMeta im = is.getItemMeta();
         if (im == null) {
            return 0;
         }

         List<String> lore = im.getLore();
         if (lore == null) {
            return 0;
         }

         String name = (String)this.effectNameHash.get(id);

         for(int i = 1; i < lore.size(); ++i) {
            if (((String)lore.get(i)).endsWith(name) && ((String)lore.get(i)).startsWith(this.effectPrefix)) {
               int result = Integer.parseInt(((String)lore.get(i)).substring(this.effectPrefix.length(), ((String)lore.get(i)).length() - name.length()));
               return result;
            }
         }
      } catch (Exception var8) {
      }

      return 0;
   }

   public boolean hasEffect(ItemStack is) {
      if (is != null) {
         ItemMeta im = is.getItemMeta();
         if (im != null) {
            List<String> lore = im.getLore();
            if (lore != null && lore.size() > 0) {
               for(int i = 0; i < lore.size(); ++i) {
                  if (((String)lore.get(i)).startsWith(this.effectPrefix)) {
                     try {
                        String check = ((String)lore.get(i)).split(" ")[1];
                        if (this.attackHash.containsKey(check) || this.bowHash.containsKey(check) || this.armorHash.containsKey(check) || this.toolHash.containsKey(check)) {
                           return true;
                        }
                     } catch (Exception var6) {
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   private void checkAttacked(RealDamageEvent RealDamageEvent, Player p, ItemStack armor) {
      if (armor != null && armor.getTypeId() != 0) {
         try {
            if (!UtilTypes.checkItem(this.pn, this.armorType, String.valueOf(armor.getTypeId()))) {
               return;
            }
         } catch (InvalidTypeException e) {
            e.printStackTrace();
            return;
         }

         ItemMeta im = armor.getItemMeta();
         if (im != null) {
            List<String> lore = im.getLore();
            if (lore != null) {
               for(int i = 1; i < lore.size(); ++i) {
                  if (((String)lore.get(i)).startsWith(this.effectPrefix)) {
                     try {
                        Effect effect = (Effect)this.armorHash.get(((String)lore.get(i)).split(" ")[1]);
                        if (effect != null) {
                           String name = (String)this.effectNameHash.get(effect.getId());
                           int result = Integer.parseInt(((String)lore.get(i)).substring(this.effectPrefix.length(), ((String)lore.get(i)).length() - name.length()));
                           effect.onAttacked(RealDamageEvent, p, result, armor);
                        }
                     } catch (Exception var10) {
                     }
                  }
               }

            }
         }
      }
   }

   private ItemStack setEffectData(ItemStack is, int id, int data) {
      try {
         if (is != null && data >= 0) {
            ItemMeta im = is.getItemMeta();
            if (im == null) {
               return null;
            } else {
               List<String> lore = im.getLore();
               if (lore == null) {
                  return null;
               } else {
                  String name = (String)this.effectNameHash.get(id);

                  for(int i = 1; i < lore.size(); ++i) {
                     if (((String)lore.get(i)).endsWith(name) && ((String)lore.get(i)).startsWith(this.effectPrefix)) {
                        if (data == 0) {
                           lore.remove(i);
                           im.setLore(lore);
                           is.setItemMeta(im);
                           return is;
                        }

                        lore.set(i, this.effectPrefix + data + name);
                        im.setLore(lore);
                        is.setItemMeta(im);
                        Effect effect = (Effect)tempHash.get(id);
                        if (effect != null) {
                           return effect.setEffectData(is, data);
                        }

                        return is;
                     }
                  }

                  if (data > 0) {
                     lore.add(this.effectPrefix + data + name);
                     im.setLore(lore);
                     is.setItemMeta(im);
                     Effect effect = (Effect)tempHash.get(id);
                     if (effect != null) {
                        return effect.setEffectData(is, data);
                     }
                  }

                  return is;
               }
            }
         } else {
            return null;
         }
      } catch (Exception var9) {
         return null;
      }
   }

   private void setLevel(ItemStack is, int id, int level) {
      try {
         ItemMeta im = is.getItemMeta();
         List<String> lore = im.getLore();
         String suffix = (String)HASH.get(level);
         if (suffix != null) {
            String pre = (String)this.displayHash.get(id);
            if (pre == null) {
               pre = "";
            }

            im.setDisplayName(pre + suffix);
         }

         lore.set(3, ((String)lore.get(3)).split(" ")[0] + " " + level);
         String name = (String)this.nameHash.get(id);
         if (name == null) {
            name = "";
         }

         int info = (Integer)((HashMap)this.data.get(id)).get(level);
         lore.set(4, this.show + " +" + info + name);
         im.setLore(lore);
         is.setItemMeta(im);
      } catch (Exception var9) {
      }

   }

   private void addUpgradeOver(ItemStack is) {
      try {
         ItemMeta im = is.getItemMeta();
         List<String> lore = im.getLore();
         lore.set(2, this.upgradeEnd);
         im.setLore(lore);
         is.setItemMeta(im);
      } catch (Exception var4) {
      }

   }

   private GemInfo getGemInfo(ItemStack is) {
      try {
         ItemMeta im = is.getItemMeta();
         if (im == null) {
            return null;
         } else {
            List<String> lore = im.getLore();
            if (lore != null && lore.size() >= 4) {
               if (!((String)lore.get(0)).equalsIgnoreCase(this.check)) {
                  return null;
               } else {
                  int id = Integer.parseInt(((String)lore.get(1)).split(" ")[1]);
                  boolean upgradeable = ((String)lore.get(2)).equalsIgnoreCase(this.upgradeUp);
                  int level = Integer.parseInt(((String)lore.get(3)).split(" ")[1]);
                  GemInfo gemInfo = new GemInfo(upgradeable, id, level);
                  return gemInfo;
               }
            } else {
               return null;
            }
         }
      } catch (Exception var8) {
         return null;
      }
   }

   private boolean isNormal(ItemStack is) {
      ItemMeta im = is.getItemMeta();
      if (im == null) {
         return true;
      } else {
         List<String> lore = im.getLore();
         return lore == null || lore.size() == 0;
      }
   }

   private void loadConfig(FileConfiguration config) {
      this.per_smelt_vip = config.getString("per_smelt_vip");
      this.interval = config.getInt("gem.interval");
      this.vipAddEffect = config.getInt("gem.vipAdd.effect");
      this.vipAddUpgrade = config.getInt("gem.vipAdd.upgrade");
      this.vipAddCombine = config.getInt("gem.vipAdd.combine");
      this.attackType = config.getString("gem.checkType.attack");
      this.bowType = config.getString("gem.checkType.bow");
      this.armorType = config.getString("gem.checkType.armor");
      this.upgradeUp = Util.convert(config.getString("gem.upgrade.up"));
      this.upgradeEnd = Util.convert(config.getString("gem.upgrade.end"));
      this.upgradeOver = config.getInt("gem.upgrade.over");
      this.upgradeMax = new HashMap();

      for(String s : config.getStringList("gem.upgrade.max")) {
         String[] ss = s.split(" ");
         int id = Integer.parseInt(ss[0]);
         HashMap<Integer, Integer> hash = new HashMap();
         int max = Integer.parseInt(ss[1]);
         hash.put(265, max);
         max = Integer.parseInt(ss[2]);
         hash.put(266, max);
         max = Integer.parseInt(ss[3]);
         hash.put(264, max);
         this.upgradeMax.put(id, hash);
      }

      this.upgradeChance = new HashMap();

      for(String s : config.getStringList("gem.upgrade.chance")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         String ss = s.split(" ")[1];
         List<Integer> list = new ArrayList();

         String[] var10;
         for(String sss : var10 = ss.split(",")) {
            int chance = Integer.parseInt(sss);
            list.add(chance);
         }

         this.upgradeChance.put(id, list);
      }

      this.combineChanceHash = new HashMap();

      for(String s : config.getStringList("gem.combine.chance")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         List<Integer> list = new ArrayList();

         String[] var83;
         for(String ss : var83 = s.split(" ")[1].split(",")) {
            int chance = Integer.parseInt(ss);
            list.add(chance);
         }

         this.combineChanceHash.put(id, list);
      }

      this.gemTip = config.getBoolean("gem.tip");
      this.check = Util.convert(config.getString("gem.check"));
      this.data = new HashMap();

      for(String s : config.getStringList("gem.data")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         HashMap<Integer, Integer> hash = new HashMap();
         boolean first = true;

         String[] var86;
         for(String ss : var86 = s.split(" ")) {
            if (first) {
               first = false;
            } else {
               int level = Integer.parseInt(ss.split(",")[0]);
               int info = Integer.parseInt(ss.split(",")[1]);
               hash.put(level, info);
            }
         }

         this.data.put(id, hash);
      }

      this.displayHash = new HashMap();

      for(String s : config.getStringList("gem.display")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         String name = Util.convert(s.split(" ")[1]);
         this.displayHash.put(id, name);
      }

      this.show = Util.convert(config.getString("gem.show"));
      this.nameHash = new HashMap();

      for(String s : config.getStringList("gem.name")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         String name = Util.convert(s.split(" ")[1]);
         this.nameHash.put(id, name);
      }

      this.effectChanceHash = new HashMap();

      for(String s : config.getStringList("effect.chance")) {
         int level = Integer.parseInt(s.split(" ")[0]);
         int chance = Integer.parseInt(s.split(" ")[1]);
         this.effectChanceHash.put(level, chance);
      }

      this.effectLimitHash = new HashMap();

      for(String s : config.getStringList("effect.limit")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         int limit = Integer.parseInt(s.split(" ")[1]);
         this.effectLimitHash.put(id, limit);
      }

      this.effectPrefix = Util.convert(config.getString("effect.prefix"));
      this.effectNameHash = new HashMap();

      for(String s : config.getStringList("effect.name")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         String name = Util.convert(Util.combine(s.split(" "), " ", 1, s.split(" ").length));
         this.effectNameHash.put(id, name);
      }

      this.effectTypeHash = new HashMap();

      for(String s : config.getStringList("effect.type")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         String type = Util.convert(s.split(" ")[1]);
         this.effectTypeHash.put(id, type);
      }

      this.suitHash = new HashMap();

      for(String s : config.getStringList("gem.type")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         String type = s.split(" ")[1];
         this.suitHash.put(id, type);
      }

      this.attackHash = new HashMap();

      for(String s : config.getStringList("effect.register.attack")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         String check = s.split(" ")[1];
         Effect effect = (Effect)tempHash.get(id);
         this.attackHash.put(check, effect);
      }

      this.bowHash = new HashMap();

      for(String s : config.getStringList("effect.register.bow")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         String check = s.split(" ")[1];
         Effect effect = (Effect)tempHash.get(id);
         this.bowHash.put(check, effect);
      }

      this.armorHash = new HashMap();

      for(String s : config.getStringList("effect.register.armor")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         String check = s.split(" ")[1];
         Effect effect = (Effect)tempHash.get(id);
         this.armorHash.put(check, effect);
      }

      this.toolHash = new HashMap();

      for(String s : config.getStringList("effect.register.tool")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         String check = s.split(" ")[1];
         Effect effect = (Effect)tempHash.get(id);
         this.toolHash.put(check, effect);
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   public ItemStack repair(ItemStack is) {
      try {
         int[] ids = new int[]{1, 30, 40};

         for(int id : ids) {
            int data = this.getEffectData(is, id);
            is = this.setEffectData(is, id, data);
         }
      } catch (Exception var8) {
      }

      return is;
   }

   class ItemInfo {
      private int id;
      private int chance;
      private String type;

      public ItemInfo(int id, int chance, String type) {
         super();
         this.id = id;
         this.chance = chance;
         this.type = type;
      }

      public int getId() {
         return this.id;
      }

      public int getChance() {
         return this.chance;
      }

      public String getType() {
         return this.type;
      }
   }

   class GemInfo {
      private boolean upgradeable;
      private int id;
      private int level;

      public GemInfo(boolean upgradeable, int id, int level) {
         super();
         this.upgradeable = upgradeable;
         this.id = id;
         this.level = level;
      }

      public boolean isUpgradeable() {
         return this.upgradeable;
      }

      public int getId() {
         return this.id;
      }

      public int getLevel() {
         return this.level;
      }

      public boolean equals(Object obj) {
         GemInfo gi = (GemInfo)obj;
         return this.id == gi.getId() && !(this.upgradeable ^ gi.isUpgradeable()) && this.level == gi.getLevel();
      }

      public int hashCode() {
         return this.upgradeable ? this.id + this.level : this.id + this.level + 1;
      }
   }
}
