package net.kunmc.lab.app.command.subcommand;

import net.kunmc.lab.app.Store;
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
                Store.gameManager.start(sender);
                return true;

            case "stop":
                String title = args.length >= 2
                        ? String.join(" ", Arrays.copyOfRange(args, 1, args.length))
                        : "ゲームが強制終了されました。";
                Store.gameManager.stop(title);
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
