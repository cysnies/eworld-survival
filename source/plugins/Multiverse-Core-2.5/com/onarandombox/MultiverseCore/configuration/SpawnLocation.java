package com.onarandombox.MultiverseCore.configuration;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("MVSpawnLocation")
public class SpawnLocation extends Location implements ConfigurationSerializable {
   private Reference worldRef;

   public SpawnLocation(double x, double y, double z) {
      super((World)null, x, y, z);
   }

   public SpawnLocation(double x, double y, double z, float yaw, float pitch) {
      super((World)null, x, y, z, yaw, pitch);
   }

   public SpawnLocation(Location loc) {
      this(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
   }

   public World getWorld() {
      return this.worldRef != null ? (World)this.worldRef.get() : null;
   }

   public void setWorld(World world) {
      this.worldRef = new WeakReference(world);
   }

   public Chunk getChunk() {
      return this.worldRef != null && this.worldRef.get() != null ? ((World)this.worldRef.get()).getChunkAt(this) : null;
   }

   public Block getBlock() {
      return this.worldRef != null && this.worldRef.get() != null ? ((World)this.worldRef.get()).getBlockAt(this) : null;
   }

   public Map serialize() {
      Map<String, Object> serialized = new HashMap(5);
      serialized.put("x", this.getX());
      serialized.put("y", this.getY());
      serialized.put("z", this.getZ());
      serialized.put("pitch", this.getPitch());
      serialized.put("yaw", this.getYaw());
      return serialized;
   }

   public static SpawnLocation deserialize(Map args) {
      double x = ((Number)args.get("x")).doubleValue();
      double y = ((Number)args.get("y")).doubleValue();
      double z = ((Number)args.get("z")).doubleValue();
      float pitch = ((Number)args.get("pitch")).floatValue();
      float yaw = ((Number)args.get("yaw")).floatValue();
      return new SpawnLocation(x, y, z, yaw, pitch);
   }
}
