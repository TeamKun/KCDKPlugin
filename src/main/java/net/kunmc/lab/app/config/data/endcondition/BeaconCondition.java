package net.kunmc.lab.app.config.data.endcondition;

import net.kunmc.lab.app.config.data.GameLocation;

public class BeaconCondition extends EndCondition {
    private GameLocation location;
    private int hitpoint;

    public BeaconCondition() {
    }

    public BeaconCondition(String message, GameLocation location, int hitpoint) {
        super(message);
        this.location = location;
        this.hitpoint = hitpoint;
    }

    public GameLocation getLocation() {
        return location;
    }

    public void setLocation(GameLocation location) {
        this.location = location;
    }

    public int getHitpoint() {
        return hitpoint;
    }

    public void setHitpoint(int hitpoint) {
        this.hitpoint = hitpoint;
    }

    @Override
    public String getType() {
        return "beacon";
    }
}
