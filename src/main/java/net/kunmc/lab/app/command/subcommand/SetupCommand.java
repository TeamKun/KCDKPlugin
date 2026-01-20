package net.kunmc.lab.app.command.subcommand;

import net.kunmc.lab.app.command.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class SetupCommand implements SubCommand {

    @Override
    public String getName() {
        return "setup";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // TODO: セットアップ処理を実装
        sender.sendMessage("§aSetup command executed (not implemented yet)");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
