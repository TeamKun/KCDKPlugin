package net.kunmc.lab.app.config.data;

import net.kunmc.lab.app.config.data.endcondition.EndCondition;
import java.util.ArrayList;
import java.util.List;

public class GameConfig {
    private int configVersion = 1;
    private String gamemode = "ADVENTURE";
    private Bossbar bossbar;
    private Time timeLimit;
    private List<String> startupCommands = new ArrayList<>();
    private List<String> shutdownCommands = new ArrayList<>();
    private List<Team> teams = new ArrayList<>();
    private List<EndCondition> endConditions = new ArrayList<>();

    public GameConfig() {
    }

    public int getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(int configVersion) {
        this.configVersion = configVersion;
    }

    public String getGamemode() {
        return gamemode;
    }

    public void setGamemode(String gamemode) {
        this.gamemode = gamemode;
    }

    public Bossbar getBossbar() {
        return bossbar;
    }

    public void setBossbar(Bossbar bossbar) {
        this.bossbar = bossbar;
    }

    public Time getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Time timeLimit) {
        this.timeLimit = timeLimit;
    }

    public List<String> getStartupCommands() {
        return startupCommands;
    }

    public void setStartupCommands(List<String> startupCommands) {
        this.startupCommands = startupCommands;
    }

    public List<String> getShutdownCommands() {
        return shutdownCommands;
    }

    public void setShutdownCommands(List<String> shutdownCommands) {
        this.shutdownCommands = shutdownCommands;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public List<EndCondition> getEndConditions() {
        return endConditions;
    }

    public void setEndConditions(List<EndCondition> endConditions) {
        this.endConditions = endConditions;
    }
}
