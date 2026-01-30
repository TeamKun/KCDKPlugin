package net.kunmc.lab.app.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeamData {
    private final String teamName;
    private final List<UUID> players = new ArrayList<>();
    private int ticketCount;

    public TeamData(String teamName, int ticketCount) {
        this.teamName = teamName;
        this.ticketCount = ticketCount;
    }

    public String getTeamName() {
        return teamName;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public void addPlayer(UUID uuid) {
        if (!players.contains(uuid)) {
            players.add(uuid);
        }
    }

    public int getTicketCount() {
        return ticketCount;
    }

    public void setTicketCount(int ticketCount) {
        this.ticketCount = ticketCount;
    }

    public void consumeTicket() {
        if (ticketCount > 0) {
            ticketCount--;
        }
    }
}
