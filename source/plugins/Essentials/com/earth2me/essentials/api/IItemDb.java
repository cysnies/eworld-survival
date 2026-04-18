package com.earth2me.essentials.api;

import com.earth2me.essentials.User;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public interface IItemDb {
   ItemStack get(String var1, int var2) throws Exception;

   ItemStack get(String var1) throws Exception;

   String names(ItemStack var1);

   String name(ItemStack var1);

   List getMatching(User var1, String[] var2) throws Exception;
}
