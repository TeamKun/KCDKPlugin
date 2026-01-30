package net.kunmc.lab.app.game;

import net.kunmc.lab.app.Store;
import net.kunmc.lab.app.config.data.GameConfig;
import net.kunmc.lab.app.config.data.Team;
import net.kunmc.lab.app.config.data.Time;
import net.kunmc.lab.app.game.condition.ConditionChecker;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class GameTimer extends BukkitRunnable {
    private final GameManager gameManager;
    private long tickCount = 0;

    public GameTimer(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public void run() {
        if (gameManager.getState() != GameState.RUNNING) return;

        tickCount++;

        // 毎秒処理
        if (tickCount % 20 == 0) {
            long elapsedSeconds = tickCount / 20;

            // アクションバー更新
            updateActionBar(elapsedSeconds);

            // BossBar更新
            updateBossBar();

            // 制限時間チェック
            if (checkTimeLimit(elapsedSeconds)) {
                return;
            }

            // 終了条件チェック
            checkEndConditions();
        }
    }

    private void updateActionBar(long elapsedSeconds) {
        GameConfig config = Store.config.getGameConfig();
        Time timeLimit = config.getTimeLimit();

        String timeText;
        if (timeLimit != null) {
            long totalSeconds = timeLimit.getTotalSeconds();
            long remaining = totalSeconds - elapsedSeconds;
            if (remaining < 0) remaining = 0;

            long hours = remaining / 3600;
            long minutes = (remaining % 3600) / 60;
            long secs = remaining % 60;

            timeText = String.format("§e残り時間: §f%02d:%02d:%02d", hours, minutes, secs);
        } else {
            long hours = elapsedSeconds / 3600;
            long minutes = (elapsedSeconds % 3600) / 60;
            long secs = elapsedSeconds % 60;

            timeText = String.format("§e経過時間: §f%02d:%02d:%02d", hours, minutes, secs);
        }

        // チームごとの生存人数
        StringBuilder sb = new StringBuilder(timeText);
        GameConfig cfg = Store.config.getGameConfig();
        for (Team team : cfg.getTeams()) {
            TeamData td = gameManager.getTeams().get(team.getName());
            if (td == null) continue;
            int alive = 0;
            for (java.util.UUID uuid : td.getPlayers()) {
                PlayerData pd = gameManager.getPlayers().get(uuid);
                if (pd != null && pd.isAlive()) alive++;
            }
            String dn = team.getDisplayName() != null ? team.getDisplayName() : team.getName();
            sb.append(" §7| ").append(dn).append("§f ").append(alive).append("人");
        }
        String actionBarText = sb.toString();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionBarText));
        }
    }

    private void updateBossBar() {
        BossBar bossBar = gameManager.getBossBar();
        if (bossBar == null) return;

        GameConfig config = Store.config.getGameConfig();
        if (config.getBossbar() != null && config.getBossbar().getMcid() != null) {
            Player target = Bukkit.getPlayerExact(config.getBossbar().getMcid());
            if (target != null) {
                double health = target.getHealth();
                double maxHealth = target.getMaxHealth();
                bossBar.setProgress(Math.max(0, Math.min(1, health / maxHealth)));
                bossBar.setTitle(target.getName() + " §c" + String.format("%.1f", health) + "❤");
            }
        }
    }

    private boolean checkTimeLimit(long elapsedSeconds) {
        GameConfig config = Store.config.getGameConfig();
        Time timeLimit = config.getTimeLimit();
        if (timeLimit == null) return false;

        long totalSeconds = timeLimit.getTotalSeconds();
        if (elapsedSeconds >= totalSeconds) {
            gameManager.stop("§e§l時間切れ！");
            return true;
        }
        return false;
    }

    private void checkEndConditions() {
        for (ConditionChecker checker : gameManager.getConditionCheckers()) {
            if (checker.isMet(gameManager)) {
                gameManager.stop(checker.getMessage());
                return;
            }
        }
    }
}
