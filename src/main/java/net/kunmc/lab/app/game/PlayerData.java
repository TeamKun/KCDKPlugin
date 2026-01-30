package net.kunmc.lab.app.game;

import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private String teamName;
    private String roleName;
    private int kills;
    private int deaths;
    private int remainingRespawns;
    private boolean alive;
    private boolean online;

    public PlayerData(UUID uuid, String teamName, String roleName, int remainingRespawns) {
        this.uuid = uuid;
        this.teamName = teamName;
        this.roleName = roleName;
        this.remainingRespawns = remainingRespawns;
        this.kills = 0;
        this.deaths = 0;
        this.alive = true;
        this.online = true;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        this.kills++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() {
        this.deaths++;
    }

    public int getRemainingRespawns() {
        return remainingRespawns;
    }

    public void consumeRespawn() {
        if (remainingRespawns > 0) {
            remainingRespawns--;
        }
    }

    public boolean hasRespawnsLeft() {
        return remainingRespawns == -1 || remainingRespawns > 0;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
