package net.kunmc.lab.app.command;

import net.kunmc.lab.app.command.subcommand.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class KCDKCommand implements CommandExecutor, TabCompleter {

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public KCDKCommand() {
        // サブコマンドの登録
        registerSubCommand(new SetupCommand());
        registerSubCommand(new TeamCommand());
        registerSubCommand(new GameCommand());
        registerSubCommand(new ConfigCommand());
    }

    private void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName(), subCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /kcdk <setup|team|game|config>");
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            sender.sendMessage("§cUnknown subcommand: " + subCommandName);
            return true;
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        return subCommand.execute(sender, subArgs);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            // メインサブコマンドの補完
            return filterStartingWith(args[0], subCommands.keySet());
        }

        if (args.length >= 2) {
            // サブコマンドのTAB補完を委譲
            String subCommandName = args[0].toLowerCase();
            SubCommand subCommand = subCommands.get(subCommandName);

            if (subCommand != null) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return subCommand.tabComplete(sender, subArgs);
            }
        }

        return Collections.emptyList();
    }

    /**
     * 指定された文字列で始まる要素をフィルタリング
     */
    public static List<String> filterStartingWith(String prefix, Collection<String> options) {
        String lowerPrefix = prefix.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(lowerPrefix)) {
                result.add(option);
            }
        }
        Collections.sort(result);
        return result;
    }
}
