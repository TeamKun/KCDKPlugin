package net.kunmc.lab.app.config.data;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private String name;
    private String displayName;
    private String armorColor;
    private boolean hasArmor = true;
    private int respawnCount;
    private ReadyLocation readyLocation;
    private GameLocation respawnLocation;
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

    public String getArmorColor() {
        return armorColor;
    }

    public void setArmorColor(String armorColor) {
        this.armorColor = armorColor;
    }

    /**
     * Minecraft標準色名からRGB整数値を取得
     */
    public int getArmorColorAsInt() {
        return net.kunmc.lab.app.util.ArmorUtil.colorNameToRGB(armorColor);
    }

    public boolean isHasArmor() {
        return hasArmor;
    }

    public void setHasArmor(boolean hasArmor) {
        this.hasArmor = hasArmor;
    }

    public int getRespawnCount() {
        return respawnCount;
    }

    public void setRespawnCount(int respawnCount) {
        this.respawnCount = respawnCount;
    }

    public ReadyLocation getReadyLocation() {
        return readyLocation;
    }

    public void setReadyLocation(ReadyLocation readyLocation) {
        this.readyLocation = readyLocation;
    }

    public GameLocation getRespawnLocation() {
        return respawnLocation;
    }

    public void setRespawnLocation(GameLocation respawnLocation) {
        this.respawnLocation = respawnLocation;
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

    /**
     * waitingTimeを取得（ReadyLocationから）
     * 後方互換性のためのヘルパー
     */
    public Time getWaitingTime() {
        return readyLocation != null ? readyLocation.getWaitingTime() : null;
    }
}
