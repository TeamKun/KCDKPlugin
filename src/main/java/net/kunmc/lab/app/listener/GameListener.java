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
import net.kunmc.lab.app.game.PlayerData;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
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
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        GameManager gm = Store.gameManager;
        if (gm == null || gm.getState() != GameState.RUNNING) return;

        // プレイヤー同士の攻撃のみ対象
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        PlayerData victimData = gm.getPlayers().get(victim.getUniqueId());
        PlayerData attackerData = gm.getPlayers().get(attacker.getUniqueId());

        if (victimData == null || attackerData == null) return;

        // 同一チームならFriendlyFire禁止（ロールが違っても同一チーム扱い）
        if (victimData.getTeamName().equals(attackerData.getTeamName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        GameManager gm = Store.gameManager;
        if (gm == null || gm.getState() != GameState.STARTING) return;

        // 参加プレイヤーのみ座標固定（視点変更は許可）
        if (!gm.getPlayers().containsKey(event.getPlayer().getUniqueId())) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            event.setTo(new Location(from.getWorld(), from.getX(), from.getY(), from.getZ(), to.getYaw(), to.getPitch()));
        }
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
