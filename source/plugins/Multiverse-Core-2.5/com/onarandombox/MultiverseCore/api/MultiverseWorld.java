package com.onarandombox.MultiverseCore.api;

import com.onarandombox.MultiverseCore.configuration.MVConfigProperty;
import com.onarandombox.MultiverseCore.enums.AllowedPortalType;
import com.onarandombox.MultiverseCore.exceptions.PropertyDoesNotExistException;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

public interface MultiverseWorld {
   World getCBWorld();

   String getName();

   WorldType getWorldType();

   World.Environment getEnvironment();

   void setEnvironment(World.Environment var1);

   Difficulty getDifficulty();

   /** @deprecated */
   @Deprecated
   boolean setDifficulty(String var1);

   boolean setDifficulty(Difficulty var1);

   long getSeed();

   void setSeed(long var1);

   String getGenerator();

   void setGenerator(String var1);

   String getPropertyHelp(String var1) throws PropertyDoesNotExistException;

   String getPropertyValue(String var1) throws PropertyDoesNotExistException;

   boolean setPropertyValue(String var1, String var2) throws PropertyDoesNotExistException;

   /** @deprecated */
   @Deprecated
   MVConfigProperty getProperty(String var1, Class var2) throws PropertyDoesNotExistException;

   /** @deprecated */
   @Deprecated
   boolean setProperty(String var1, String var2, CommandSender var3) throws PropertyDoesNotExistException;

   /** @deprecated */
   @Deprecated
   boolean addToVariable(String var1, String var2);

   /** @deprecated */
   @Deprecated
   boolean removeFromVariable(String var1, String var2);

   /** @deprecated */
   @Deprecated
   boolean clearVariable(String var1);

   /** @deprecated */
   @Deprecated
   boolean clearList(String var1);

   String getPermissibleName();

   Permission getAccessPermission();

   Permission getExemptPermission();

   String getAlias();

   void setAlias(String var1);

   ChatColor getColor();

   boolean setColor(String var1);

   ChatColor getStyle();

   boolean setStyle(String var1);

   /** @deprecated */
   @Deprecated
   boolean isValidAliasColor(String var1);

   String getColoredWorldString();

   boolean canAnimalsSpawn();

   void setAllowAnimalSpawn(boolean var1);

   List getAnimalList();

   boolean canMonstersSpawn();

   void setAllowMonsterSpawn(boolean var1);

   List getMonsterList();

   boolean isPVPEnabled();

   void setPVPMode(boolean var1);

   /** @deprecated */
   @Deprecated
   boolean getFakePVP();

   boolean isHidden();

   void setHidden(boolean var1);

   boolean isWeatherEnabled();

   void setEnableWeather(boolean var1);

   boolean isKeepingSpawnInMemory();

   void setKeepSpawnInMemory(boolean var1);

   Location getSpawnLocation();

   void setSpawnLocation(Location var1);

   boolean getHunger();

   void setHunger(boolean var1);

   GameMode getGameMode();

   /** @deprecated */
   @Deprecated
   boolean setGameMode(String var1);

   boolean setGameMode(GameMode var1);

   double getPrice();

   void setPrice(double var1);

   int getCurrency();

   void setCurrency(int var1);

   World getRespawnToWorld();

   boolean setRespawnToWorld(String var1);

   double getScaling();

   boolean setScaling(double var1);

   boolean getAutoHeal();

   void setAutoHeal(boolean var1);

   boolean getAdjustSpawn();

   void setAdjustSpawn(boolean var1);

   boolean getAutoLoad();

   void setAutoLoad(boolean var1);

   boolean getBedRespawn();

   void setBedRespawn(boolean var1);

   void setPlayerLimit(int var1);

   int getPlayerLimit();

   String getTime();

   boolean setTime(String var1);

   void allowPortalMaking(AllowedPortalType var1);

   AllowedPortalType getAllowedPortals();

   List getWorldBlacklist();

   String getAllPropertyNames();
}
