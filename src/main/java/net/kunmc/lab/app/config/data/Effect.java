package net.kunmc.lab.app.config.data;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Effect {
    private String name;
    private int seconds;
    private int amplifier;
    private boolean hideParticles;

    public Effect() {
    }

    public Effect(String name, int seconds, int amplifier, boolean hideParticles) {
        this.name = name;
        this.seconds = seconds;
        this.amplifier = amplifier;
        this.hideParticles = hideParticles;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public void setAmplifier(int amplifier) {
        this.amplifier = amplifier;
    }

    public boolean isHideParticles() {
        return hideParticles;
    }

    public void setHideParticles(boolean hideParticles) {
        this.hideParticles = hideParticles;
    }

    /**
     * Bukkit PotionEffectに変換
     */
    public PotionEffect toPotionEffect() {
        PotionEffectType type = PotionEffectType.getByName(name);
        if (type == null) {
            throw new IllegalArgumentException("Invalid potion effect: " + name);
        }
        return new PotionEffect(type, seconds * 20, amplifier, true, !hideParticles);
    }
}
