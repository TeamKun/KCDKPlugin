package net.kunmc.lab.app.config.data;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private String name;
    private String displayName;
    private String armorColor;
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
     * HEX形式の文字列をRGB整数に変換
     * 例: "#ef4444" -> 15684676
     */
    public int getArmorColorAsInt() {
        if (armorColor == null || armorColor.isEmpty()) {
            return 16777215; // 白
        }
        String hex = armorColor.startsWith("#") ? armorColor.substring(1) : armorColor;
        try {
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            return 16777215; // 白
        }
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
