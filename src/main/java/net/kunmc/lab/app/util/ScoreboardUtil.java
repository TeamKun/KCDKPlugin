package net.kunmc.lab.app.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class ScoreboardUtil {

    public static Scoreboard getMainScoreboard() {
        if (Bukkit.getScoreboardManager() == null) return null;
        return Bukkit.getScoreboardManager().getMainScoreboard();
    }

    public static Team getOrCreateTeam(String name) {
        Scoreboard sb = getMainScoreboard();
        if (sb == null) return null;

        Team team = sb.getTeam(name);
        if (team == null) {
            team = sb.registerNewTeam(name);
        }
        return team;
    }

    public static List<String> getTeamMembers(String teamName) {
        Scoreboard sb = getMainScoreboard();
        if (sb == null) return Collections.emptyList();

        Team team = sb.getTeam(teamName);
        if (team == null) return Collections.emptyList();

        return new ArrayList<>(team.getEntries());
    }

    public static Map<String, String> getPlayerTeamMap() {
        Scoreboard sb = getMainScoreboard();
        if (sb == null) return Collections.emptyMap();

        Map<String, String> map = new HashMap<>();
        for (Team team : sb.getTeams()) {
            if (team.getName().startsWith("kcdk.")) {
                for (String entry : team.getEntries()) {
                    map.put(entry, team.getName());
                }
            }
        }
        return map;
    }

    public static void setPlayerTabName(Player player, String displayName) {
        player.setPlayerListName(displayName);
    }

    public static void resetPlayerTabName(Player player) {
        player.setPlayerListName(null);
    }

    public static void resetAllTabNames() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            resetPlayerTabName(player);
        }
    }
}
