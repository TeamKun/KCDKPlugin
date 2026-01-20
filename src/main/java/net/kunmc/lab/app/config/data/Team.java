package net.kunmc.lab.app.config.data;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private String name;
    private String displayName;
    private int armorColor;
    private GameLocation readyLocation;
    private GameLocation respawnLocation;
    private int stock;
    private Time waitingTime;
    private List<Effect> effects = new ArrayList<>();
    private List<Role> roles = new ArrayList<>();

    public Team() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getArmorColor() {
        return armorColor;
    }

    public void setArmorColor(int armorColor) {
        this.armorColor = armorColor;
    }

    public GameLocation getReadyLocation() {
        return readyLocation;
    }

    public void setReadyLocation(GameLocation readyLocation) {
        this.readyLocation = readyLocation;
    }

    public GameLocation getRespawnLocation() {
        return respawnLocation;
    }

    public void setRespawnLocation(GameLocation respawnLocation) {
        this.respawnLocation = respawnLocation;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Time getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(Time waitingTime) {
        this.waitingTime = waitingTime;
    }

    public List<Effect> getEffects() {
        return effects;
    }

    public void setEffects(List<Effect> effects) {
        this.effects = effects;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}
