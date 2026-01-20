package net.kunmc.lab.app.command;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {
    /**
     * サブコマンド名を取得
     */
    String getName();

    /**
     * コマンドを実行
     */
    boolean execute(CommandSender sender, String[] args);

    /**
     * TAB補完候補を返す
     */
    List<String> tabComplete(CommandSender sender, String[] args);
}
