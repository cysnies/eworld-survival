package com.onarandombox.MultiverseCore.api;

import buscript.multiverse.Buscript;
import com.fernferret.allpay.multiverse.AllPay;
import com.fernferret.allpay.multiverse.GenericBank;
import com.onarandombox.MultiverseCore.destination.DestinationFactory;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import com.onarandombox.MultiverseCore.utils.MVPermissions;
import com.onarandombox.MultiverseCore.utils.MVPlayerSession;
import com.onarandombox.MultiverseCore.utils.VaultHandler;
import com.pneumaticraft.commandhandler.multiverse.CommandHandler;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public interface Core {
   /** @deprecated */
   @Deprecated
   FileConfiguration getMVConfiguration();

   /** @deprecated */
   @Deprecated
   GenericBank getBank();

   VaultHandler getVaultHandler();

   void loadConfigs();

   MultiverseMessaging getMessaging();

   MVPlayerSession getPlayerSession(Player var1);

   /** @deprecated */
   @Deprecated
   com.onarandombox.MultiverseCore.utils.SafeTTeleporter getTeleporter();

   MVPermissions getMVPerms();

   CommandHandler getCommandHandler();

   DestinationFactory getDestFactory();

   MVWorldManager getMVWorldManager();

   boolean saveMVConfigs();

   AnchorManager getAnchorManager();

   /** @deprecated */
   @Deprecated
   Boolean regenWorld(String var1, Boolean var2, Boolean var3, String var4);

   /** @deprecated */
   @Deprecated
   void setBank(GenericBank var1);

   /** @deprecated */
   @Deprecated
   AllPay getBanker();

   void decrementPluginCount();

   void incrementPluginCount();

   int getPluginCount();

   String getAuthors();

   BlockSafety getBlockSafety();

   void setBlockSafety(BlockSafety var1);

   LocationManipulation getLocationManipulation();

   void setLocationManipulation(LocationManipulation var1);

   SafeTTeleporter getSafeTTeleporter();

   void setSafeTTeleporter(SafeTTeleporter var1);

   MultiverseCoreConfig getMVConfig();

   Buscript getScriptAPI();
}
