package net.kunmc.lab.app.config;

import net.kunmc.lab.app.config.data.GameConfig;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class Config {
    private final GameConfigManager gameConfigManager;

    public Config(@NotNull Plugin plugin) {
        this.gameConfigManager = new GameConfigManager(plugin);
    }

    /**
     * ゲーム設定を取得
     */
    public GameConfig getGameConfig() {
        return gameConfigManager.getGameConfig();
    }

    /**
     * ゲーム設定を設定
     */
    public void setGameConfig(GameConfig gameConfig) {
        gameConfigManager.setGameConfig(gameConfig);
    }

    /**
     * ゲーム設定を再読み込み
     */
    public void reloadGameConfig() {
        gameConfigManager.loadConfig();
    }

    /**
     * ゲーム設定を保存
     */
    public void saveGameConfig() {
        gameConfigManager.saveConfig();
    }

    /**
     * GameConfigManagerを取得
     */
    public GameConfigManager getGameConfigManager() {
        return gameConfigManager;
    }
}
