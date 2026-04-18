package com.lishid.orebfuscator.hithack;

import com.lishid.orebfuscator.Orebfuscator;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlayerBlockTracking {
   private Block block;
   private int hackingIndicator;
   private Player player;
   private long lastTime = System.currentTimeMillis();

   public PlayerBlockTracking(Player player) {
      super();
      this.player = player;
   }

   public Player getPlayer() {
      return this.player;
   }

   public int getHackingIndicator() {
      return this.hackingIndicator;
   }

   public Block getBlock() {
      return this.block;
   }

   public boolean isBlock(Block block) {
      return block != null && this.block != null ? block.equals(this.block) : false;
   }

   public void setBlock(Block block) {
      this.block = block;
   }

   public void incrementHackingIndicator(int value) {
      this.hackingIndicator += value;
      if (this.hackingIndicator >= 16384) {
         Orebfuscator.log("Player \"" + this.player.getName() + "\" tried to hack with packet spamming.");
         Orebfuscator.log("Player \"" + this.player.getName() + "\" kicked.");
         this.player.kickPlayer("End of Stream");
      }

   }

   public void incrementHackingIndicator() {
      this.incrementHackingIndicator(1);
   }

   public void decrementHackingIndicator(int value) {
      this.hackingIndicator -= value;
      if (this.hackingIndicator < 0) {
         this.hackingIndicator = 0;
      }

   }

   public void updateTime() {
      this.lastTime = System.currentTimeMillis();
   }

   public long getTimeDifference() {
      return System.currentTimeMillis() - this.lastTime;
   }
}
