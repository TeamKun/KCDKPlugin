package net.kunmc.lab.app.game.condition;

import net.kunmc.lab.app.game.GameManager;
import net.kunmc.lab.app.game.TeamData;

public class TicketChecker implements ConditionChecker {
    private final String teamName;
    private final String message;

    public TicketChecker(String teamName, String message) {
        this.teamName = teamName;
        this.message = message;
    }

    @Override
    public boolean isMet(GameManager gameManager) {
        TeamData teamData = gameManager.getTeams().get(teamName);
        if (teamData == null) return false;
        return teamData.getTicketCount() <= 0;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
