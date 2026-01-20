package net.kunmc.lab.app.config.data;

import java.util.ArrayList;
import java.util.List;

public class Role {
    private String name;
    private String displayName;
    private Integer armorColor;
    private GameLocation readyLocation;
    private GameLocation respawnLocation;
    private Integer stock;
    private Time waitingTime;
    private List<Effect> effects = new ArrayList<>();
    private boolean extendsEffects;
    private boolean extendsItem;

    public Role() {
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

    public Integer getArmorColor() {
        return armorColor;
    }

    public void setArmorColor(Integer armorColor) {
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

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
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

    public boolean isExtendsEffects() {
        return extendsEffects;
    }

    public void setExtendsEffects(boolean extendsEffects) {
        this.extendsEffects = extendsEffects;
    }

    public boolean isExtendsItem() {
        return extendsItem;
    }

    public void setExtendsItem(boolean extendsItem) {
        this.extendsItem = extendsItem;
    }
}
