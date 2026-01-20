package net.kunmc.lab.app.command.subcommand;

import net.kunmc.lab.app.Store;
import net.kunmc.lab.app.command.KCDKCommand;
import net.kunmc.lab.app.command.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
                // TODO: チーム自動割り当て処理を実装
                sender.sendMessage("§aTeam assign command executed (not implemented yet)");
                return true;

            case "change":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /kcdk team change <player> <team>");
                    return true;
                }
                // TODO: チーム変更処理を実装
                String playerName = args[1];
                String teamName = args[2];
                sender.sendMessage("§aTeam change command executed for " + playerName + " to " + teamName + " (not implemented yet)");
                return true;

            default:
                sender.sendMessage("§cUnknown action: " + action);
                return true;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // assign, change
            return KCDKCommand.filterStartingWith(args[0], Arrays.asList("assign", "change"));
        }

        if (args.length >= 2) {
            String action = args[0].toLowerCase();

            if ("change".equals(action)) {
                if (args.length == 2) {
                    // プレイヤー名の補完
                    List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList());
                    return KCDKCommand.filterStartingWith(args[1], playerNames);
                } else if (args.length == 3) {
                    // チーム名の補完（kcdk.で始まるチームのみ）
                    return getKcdkTeams(args[2]);
                }
            }
        }

        return Collections.emptyList();
    }

    /**
     * kcdk.で始まるチーム一覧を取得
     */
    private List<String> getKcdkTeams(String prefix) {
        if (Bukkit.getScoreboardManager() == null) {
            return Collections.emptyList();
        }

        return Bukkit.getScoreboardManager().getMainScoreboard().getTeams().stream()
                .map(Team::getName)
                .filter(name -> name.startsWith("kcdk."))
                .map(name -> name.substring(5)) // "kcdk."を除去
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> KCDKCommand.filterStartingWith(prefix, list)
                ));
    }
}
