package com.earth2me.essentials.api;

import com.earth2me.essentials.IConf;
import com.earth2me.essentials.commands.WarpNotFoundException;
import java.io.File;
import java.util.Collection;
import org.bukkit.Location;

public interface IWarps extends IConf {
   Location getWarp(String var1) throws WarpNotFoundException, net.ess3.api.InvalidWorldException;

   Collection getList();

   int getCount();

   void removeWarp(String var1) throws Exception;

   void setWarp(String var1, Location var2) throws Exception;

   boolean isEmpty();

   File getWarpFile(String var1) throws net.ess3.api.InvalidNameException;
}
