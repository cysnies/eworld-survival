package com.earth2me.essentials;

import com.earth2me.essentials.commands.IEssentialsCommand;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.ess3.api.ITeleport;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface IUser {
   boolean isAuthorized(String var1);

   boolean isAuthorized(IEssentialsCommand var1);

   boolean isAuthorized(IEssentialsCommand var1, String var2);

   void healCooldown() throws Exception;

   void giveMoney(BigDecimal var1);

   void giveMoney(BigDecimal var1, CommandSender var2);

   void payUser(User var1, BigDecimal var2) throws Exception;

   void takeMoney(BigDecimal var1);

   void takeMoney(BigDecimal var1, CommandSender var2);

   boolean canAfford(BigDecimal var1);

   Boolean canSpawnItem(int var1);

   void setLastLocation();

   void setLogoutLocation();

   void requestTeleport(User var1, boolean var2);

   ITeleport getTeleport();

   BigDecimal getMoney();

   void setMoney(BigDecimal var1);

   void setAfk(boolean var1);

   boolean isHidden();

   void setHidden(boolean var1);

   boolean isGodModeEnabled();

   String getGroup();

   boolean inGroup(String var1);

   boolean canBuild();

   long getTeleportRequestTime();

   void enableInvulnerabilityAfterTeleport();

   void resetInvulnerabilityAfterTeleport();

   boolean hasInvulnerabilityAfterTeleport();

   boolean isVanished();

   void setVanished(boolean var1);

   boolean isIgnoreExempt();

   void sendMessage(String var1);

   Location getHome(String var1) throws Exception;

   Location getHome(Location var1) throws Exception;

   List getHomes();

   void setHome(String var1, Location var2);

   void delHome(String var1) throws Exception;

   boolean hasHome();

   Location getLastLocation();

   Location getLogoutLocation();

   long getLastTeleportTimestamp();

   void setLastTeleportTimestamp(long var1);

   String getJail();

   void setJail(String var1);

   List getMails();

   void addMail(String var1);

   boolean isAfk();

   void setConfigProperty(String var1, Object var2);

   Set getConfigKeys();

   Map getConfigMap();

   Map getConfigMap(String var1);

   Player getBase();

   String getName();
}
