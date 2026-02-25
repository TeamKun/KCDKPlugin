package net.kunmc.lab.app.command.subcommand;

import net.kunmc.lab.app.command.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Collections;
import java.util.List;

public class SetupCommand implements SubCommand {

    private static final String[][] TEAM_DEFS = {
        {"kcdk.red", "RED", "true"},
        {"kcdk.blue", "BLUE", "true"},
        {"kcdk.green", "GREEN", "true"},
        {"kcdk.yellow", "YELLOW", "true"},
        {"kcdk.admin", "AQUA", "false"},
    };

    @Override
    public String getName() {
        return "setup";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        setupTeams();
        sender.sendMessage("§aセットアップが完了しました。");
        return true;
    }

    /**
     * Scoreboardチームを作成・更新する。コマンドとプラグイン起動時の両方から呼ばれる。
     */
    public static void setupTeams() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        for (String[] def : TEAM_DEFS) {
            String name = def[0];
            ChatColor color = ChatColor.valueOf(def[1]);
            boolean hasCaptain = Boolean.parseBoolean(def[2]);

            Team team = scoreboard.getTeam(name);
            if (team == null) {
                team = scoreboard.registerNewTeam(name);
            }

            team.setColor(color);
            team.setDisplayName(color + name);
            team.setPrefix(color.toString());
            team.setAllowFriendlyFire(false);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

            if (hasCaptain) {
                String captainName = name + ".captain";
                Team captainTeam = scoreboard.getTeam(captainName);
                if (captainTeam == null) {
                    captainTeam = scoreboard.registerNewTeam(captainName);
                }

                captainTeam.setColor(color);
                captainTeam.setDisplayName(color + captainName);
                captainTeam.setPrefix(color + "★ ");
                captainTeam.setAllowFriendlyFire(false);
                captainTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
