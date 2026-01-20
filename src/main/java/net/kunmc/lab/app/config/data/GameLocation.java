package net.kunmc.lab.app.config.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class GameLocation {
    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public GameLocation() {
    }

    public GameLocation(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public GameLocation(Location location) {
        this.world = location.getWorld() != null ? location.getWorld().getName() : "world";
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    /**
     * Bukkit Locationに変換
     */
    public Location toBukkitLocation() {
        World bukkitWorld = Bukkit.getWorld(world);
        if (bukkitWorld == null) {
            throw new IllegalStateException("World not found: " + world);
        }
        return new Location(bukkitWorld, x, y, z, yaw, pitch);
    }
}
