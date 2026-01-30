package net.kunmc.lab.app.listener;

import net.kunmc.lab.app.command.PointCommand;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;

public class PointListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        if (!PointCommand.isInspecting(player.getUniqueId())) return;

        Location loc = event.getBlock().getLocation();
        String coordText = String.format("%d %d %d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        PointCommand.sendCopyableMessage(player, coordText);

        // モード解除
        PointCommand.removeInspecting(player.getUniqueId());
        player.sendMessage("§7座標取得モードを§c解除§7しました。");
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent event) {
        if (PointCommand.isInspecting(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
