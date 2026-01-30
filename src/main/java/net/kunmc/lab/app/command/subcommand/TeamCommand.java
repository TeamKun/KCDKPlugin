package net.kunmc.lab.app.command.subcommand;

import net.kunmc.lab.app.Store;
import net.kunmc.lab.app.command.KCDKCommand;
import net.kunmc.lab.app.command.SubCommand;
import net.kunmc.lab.app.util.ScoreboardUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.stream.Collectors;

public class TeamCommand implements SubCommand {

    @Override
    public String getName() {
        return "team";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /kcdk team <assign|change>");
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "assign":
                return handleAssign(sender);

            case "change":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /kcdk team change <player> <team>");
                    return true;
                }
                return handleChange(sender, args[1], args[2]);

            default:
                sender.sendMessage("§cUnknown action: " + action);
                return true;
        }
    }

    private boolean handleAssign(CommandSender sender) {
        Scoreboard scoreboard = ScoreboardUtil.getMainScoreboard();
        if (scoreboard == null) {
            sender.sendMessage("§cScoreboardが利用できません。");
            return true;
        }

        // kcdk.で始まるチーム（ロールチームを除く）
        List<Team> kcdkTeams = scoreboard.getTeams().stream()
                .filter(t -> t.getName().startsWith("kcdk."))
                .filter(t -> t.getName().chars().filter(c -> c == '.').count() == 1)
                .collect(Collectors.toList());

        if (kcdkTeams.isEmpty()) {
            sender.sendMessage("§ckcdk.で始まるScoreboardチームが見つかりません。先にconfigをimportしてください。");
            return true;
        }

        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (onlinePlayers.isEmpty()) {
            sender.sendMessage("§cオンラインプレイヤーがいません。");
            return true;
        }

        // 既存のkcdk.チームからプレイヤーを除去
        for (Team team : scoreboard.getTeams()) {
            if (team.getName().startsWith("kcdk.")) {
                for (String entry : new ArrayList<>(team.getEntries())) {
                    team.removeEntry(entry);
                }
            }
        }

        // シャッフルして均等分配
        Collections.shuffle(onlinePlayers);
        for (int i = 0; i < onlinePlayers.size(); i++) {
            Team team = kcdkTeams.get(i % kcdkTeams.size());
            team.addEntry(onlinePlayers.get(i).getName());
        }

        sender.sendMessage("§a" + onlinePlayers.size() + "人のプレイヤーを" + kcdkTeams.size() + "チームに分配しました。");
        for (Team team : kcdkTeams) {
            sender.sendMessage("  §7" + team.getName() + ": §f" + String.join(", ", team.getEntries()));
        }

        return true;
    }

    private boolean handleChange(CommandSender sender, String playerName, String teamName) {
        Scoreboard scoreboard = ScoreboardUtil.getMainScoreboard();
        if (scoreboard == null) {
            sender.sendMessage("§cScoreboardが利用できません。");
            return true;
        }

        String fullTeamName = teamName.startsWith("kcdk.") ? teamName : "kcdk." + teamName;
        Team team = scoreboard.getTeam(fullTeamName);
        if (team == null) {
            sender.sendMessage("§cチームが見つかりません: " + fullTeamName);
            return true;
        }

        // 既存チームから除去
        for (Team t : scoreboard.getTeams()) {
            if (t.getName().startsWith("kcdk.")) {
                t.removeEntry(playerName);
            }
        }

        team.addEntry(playerName);
        sender.sendMessage("§a" + playerName + " を " + fullTeamName + " に移動しました。");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return KCDKCommand.filterStartingWith(args[0], Arrays.asList("assign", "change"));
        }

        if (args.length >= 2) {
            String action = args[0].toLowerCase();

            if ("change".equals(action)) {
                if (args.length == 2) {
                    List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList());
                    return KCDKCommand.filterStartingWith(args[1], playerNames);
                } else if (args.length == 3) {
                    return getKcdkTeams(args[2]);
                }
            }
        }

        return Collections.emptyList();
    }

    private List<String> getKcdkTeams(String prefix) {
        if (Bukkit.getScoreboardManager() == null) {
            return Collections.emptyList();
        }

        return Bukkit.getScoreboardManager().getMainScoreboard().getTeams().stream()
                .map(Team::getName)
                .filter(name -> name.startsWith("kcdk."))
                .map(name -> name.substring(5))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> KCDKCommand.filterStartingWith(prefix, list)
                ));
    }
}
