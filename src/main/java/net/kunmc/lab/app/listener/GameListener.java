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
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import net.kunmc.lab.app.game.PlayerData;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
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

        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();

        // 攻撃者を特定（直接攻撃 or 飛び道具）
        Player attacker = null;
        boolean isArrow = false;
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) event.getDamager();
            if (proj.getShooter() instanceof Player) {
                attacker = (Player) proj.getShooter();
                isArrow = event.getDamager() instanceof Arrow;
            }
        }

        if (attacker == null) return;

        PlayerData victimData = gm.getPlayers().get(victim.getUniqueId());
        PlayerData attackerData = gm.getPlayers().get(attacker.getUniqueId());

        if (victimData == null || attackerData == null) return;

        // 同一チームならFriendlyFire禁止（ロールが違っても同一チーム扱い）
        if (victimData.getTeamName().equals(attackerData.getTeamName())) {
            event.setCancelled(true);
            return;
        }

        // 矢がプレイヤーに命中した場合、射手に経験値オーブの音を鳴らす
        if (isArrow) {
            attacker.playSound(attacker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
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
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        GameManager gm = Store.gameManager;
        if (gm == null || gm.getState() != GameState.RUNNING) return;
        if (!Store.config.getGameConfig().isDisableHunger()) return;

        if (event.getEntity() instanceof Player) {
            event.setFoodLevel(20);
            event.setCancelled(true);
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
