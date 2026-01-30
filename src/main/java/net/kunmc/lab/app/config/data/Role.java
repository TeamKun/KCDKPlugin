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
    private Boolean hasArmor;
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
     * Minecraft標準色名からRGB整数値を取得。未設定ならnull。
     */
    public Integer getArmorColorAsInt() {
        if (armorColor == null || armorColor.isEmpty()) {
            return null;
        }
        return net.kunmc.lab.app.util.ArmorUtil.colorNameToRGB(armorColor);
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

    public Boolean getHasArmor() {
        return hasArmor;
    }

    public void setHasArmor(Boolean hasArmor) {
        this.hasArmor = hasArmor;
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
