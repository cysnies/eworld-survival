package net.citizensnpcs.api.trait.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Owner extends Trait {
   private String owner = "server";
   public static final String SERVER = "server";

   public Owner() {
      super("owner");
   }

   public String getOwner() {
      return this.owner;
   }

   public boolean isOwnedBy(CommandSender sender) {
      if (!(sender instanceof Player)) {
         return this.owner.equals("server");
      } else {
         return this.owner.equalsIgnoreCase(sender.getName()) || sender.hasPermission("citizens.admin") || this.owner.equals("server") && sender.hasPermission("citizens.admin");
      }
   }

   public boolean isOwnedBy(String name) {
      return this.owner.equalsIgnoreCase(name);
   }

   public void load(DataKey key) throws NPCLoadException {
      try {
         this.owner = key.getString("");
      } catch (Exception var3) {
         this.owner = "server";
         throw new NPCLoadException("Invalid owner.");
      }
   }

   public void save(DataKey key) {
      key.setString("", this.owner);
   }

   public void setOwner(String owner) {
      this.owner = owner;
   }

   public String toString() {
      return "Owner{" + this.owner + "}";
   }
}
