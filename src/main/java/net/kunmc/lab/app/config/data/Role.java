package net.kunmc.lab.app.config.data;

import java.util.ArrayList;
import java.util.List;

public class Role {
    private String name;
    private String displayName;
    private String armorColor;
    private ReadyLocation readyLocation;
    private GameLocation respawnLocation;
    private Integer respawnCount;
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
    public Integer getArmorColorAsInt() {
        if (armorColor == null || armorColor.isEmpty()) {
            return null;
        }
        String hex = armorColor.startsWith("#") ? armorColor.substring(1) : armorColor;
        try {
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            return null;
        }
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

    public Integer getRespawnCount() {
        return respawnCount;
    }

    public void setRespawnCount(Integer respawnCount) {
        this.respawnCount = respawnCount;
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
