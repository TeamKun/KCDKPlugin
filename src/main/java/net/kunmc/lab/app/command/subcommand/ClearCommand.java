package net.kunmc.lab.app.command.subcommand;

import net.kunmc.lab.app.command.SubCommand;
import net.kunmc.lab.app.util.ScoreboardUtil;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class ClearCommand implements SubCommand {

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        ScoreboardUtil.resetAllTabNames();
        sender.sendMessage("§a全プレイヤーのタブ名をリセットしました。");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
