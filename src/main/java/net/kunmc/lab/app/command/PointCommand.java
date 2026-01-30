package net.kunmc.lab.app.command;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class PointCommand implements CommandExecutor, TabCompleter {

    private static final Set<UUID> inspectMode = new HashSet<>();

    public static boolean isInspecting(UUID uuid) {
        return inspectMode.contains(uuid);
    }

    public static void removeInspecting(UUID uuid) {
        inspectMode.remove(uuid);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cこのコマンドはプレイヤーのみ使用できます。");
            return true;
        }

        Player player = (Player) sender;

        // /point i — 座標取得モード切替
        if (args.length >= 1 && "i".equalsIgnoreCase(args[0])) {
            UUID uuid = player.getUniqueId();
            if (inspectMode.contains(uuid)) {
                inspectMode.remove(uuid);
                player.sendMessage("§7座標取得モードを§c解除§7しました。");
            } else {
                inspectMode.add(uuid);
                player.sendMessage("§7座標取得モードに§a入りました§7。ブロックを殴ると座標を取得します。");
            }
            return true;
        }

        Location loc = player.getLocation();
        boolean rotation = args.length >= 1 && "--rotation".equalsIgnoreCase(args[0]);

        String coordText;
        if (rotation) {
            coordText = String.format("%.2f %.2f %.2f %.1f %.1f",
                    loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        } else {
            coordText = String.format("%.2f %.2f %.2f",
                    loc.getX(), loc.getY(), loc.getZ());
        }

        sendCopyableMessage(player, coordText);
        return true;
    }

    public static void sendCopyableMessage(Player player, String text) {
        TextComponent message = new TextComponent("§a[座標] §f" + text + " §7(クリックでコピー)");
        message.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("クリックしてコピー")));
        player.spigot().sendMessage(message);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            for (String opt : Arrays.asList("i", "--rotation")) {
                if (opt.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(opt);
                }
            }
            return completions;
        }
        return Collections.emptyList();
    }
}
