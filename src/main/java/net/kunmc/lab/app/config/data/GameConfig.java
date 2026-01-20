package net.kunmc.lab.app.config.data;

import net.kunmc.lab.app.config.data.endcondition.EndCondition;
import java.util.ArrayList;
import java.util.List;

public class GameConfig {
    private int configVersion = 1;
    private String gamemode = "survival";
    private Time timeLimit;
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

    public Time getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Time timeLimit) {
        this.timeLimit = timeLimit;
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
