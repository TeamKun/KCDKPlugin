package net.kunmc.lab.app.command.subcommand;

import net.kunmc.lab.app.command.KCDKCommand;
import net.kunmc.lab.app.command.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GameCommand implements SubCommand {

    @Override
    public String getName() {
        return "game";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /kcdk game <start|stop>");
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "start":
                // TODO: ゲーム開始処理を実装
                sender.sendMessage("§aGame start command executed (not implemented yet)");
                return true;

            case "stop":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /kcdk game stop <title>");
                    return true;
                }
                String title = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                // TODO: ゲーム停止処理を実装
                sender.sendMessage("§aGame stop command executed with title: " + title + " (not implemented yet)");
                return true;

            default:
                sender.sendMessage("§cUnknown action: " + action);
                return true;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return KCDKCommand.filterStartingWith(args[0], Arrays.asList("start", "stop"));
        }

        return Collections.emptyList();
    }
}
