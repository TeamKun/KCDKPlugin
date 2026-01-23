package net.kunmc.lab.app.config.data;

import org.bukkit.Location;

/**
 * 待機地点（waitingTime付きのLocation）
 */
public class ReadyLocation extends GameLocation {
    private Time waitingTime;

    public ReadyLocation() {
    }

    public ReadyLocation(String world, double x, double y, double z, float yaw, float pitch, Time waitingTime) {
        super(world, x, y, z, yaw, pitch);
        this.waitingTime = waitingTime;
    }

    public ReadyLocation(Location location, Time waitingTime) {
        super(location);
        this.waitingTime = waitingTime;
    }

    public Time getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(Time waitingTime) {
        this.waitingTime = waitingTime;
    }
}
