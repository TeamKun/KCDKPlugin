package net.kunmc.lab.app.listener;

import net.kunmc.lab.app.Store;
import net.kunmc.lab.app.config.data.GameLocation;
import net.kunmc.lab.app.config.data.endcondition.BeaconCondition;
import net.kunmc.lab.app.config.data.endcondition.CompositeCondition;
import net.kunmc.lab.app.config.data.endcondition.EndCondition;
import net.kunmc.lab.app.game.GameManager;
import net.kunmc.lab.app.game.GameState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class GameListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        GameManager gm = Store.gameManager;
        if (gm == null || gm.getState() != GameState.RUNNING) return;

        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        gm.handleDeath(victim, killer);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        GameManager gm = Store.gameManager;
        if (gm == null || gm.getState() != GameState.RUNNING) return;

        Player player = event.getPlayer();
        if (gm.getPlayers().containsKey(player.getUniqueId())) {
            gm.handleRespawn(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        GameManager gm = Store.gameManager;
        if (gm == null || (gm.getState() != GameState.RUNNING && gm.getState() != GameState.STARTING)) return;

        gm.handleJoin(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        GameManager gm = Store.gameManager;
        if (gm == null || gm.getState() != GameState.RUNNING) return;

        gm.handleQuit(event.getPlayer());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        GameManager gm = Store.gameManager;
        if (gm == null || gm.getState() != GameState.RUNNING) return;

        Location loc = event.getBlock().getLocation();

        // ビーコン座標チェック
        for (GameLocation beaconLoc : gm.getBeaconHPs().keySet()) {
            Location bukkitBeaconLoc = beaconLoc.toBukkitLocation();
            if (bukkitBeaconLoc.getBlockX() == loc.getBlockX()
                    && bukkitBeaconLoc.getBlockY() == loc.getBlockY()
                    && bukkitBeaconLoc.getBlockZ() == loc.getBlockZ()
                    && bukkitBeaconLoc.getWorld() != null
                    && bukkitBeaconLoc.getWorld().equals(loc.getWorld())) {
                // ブロック破壊キャンセル
                event.setCancelled(true);
                // HP-1
                gm.handleBeaconBreak(beaconLoc);
                return;
            }
        }
    }
}
