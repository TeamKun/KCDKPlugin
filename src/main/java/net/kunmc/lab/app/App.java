package net.kunmc.lab.app;

import java.util.Objects;
import net.kunmc.lab.app.command.KCDKCommand;
import net.kunmc.lab.app.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class App extends JavaPlugin {

    @Override
    public void onEnable() {
        Store.pluginName = this.getName();
        Store.plugin = this;
        Store.config = new Config(this);

        // コマンド登録
        KCDKCommand kcdkCommand = new KCDKCommand();
        PluginCommand command = getCommand("kcdk");
        if (command != null) {
            command.setExecutor(kcdkCommand);
            command.setTabCompleter(kcdkCommand);
        }
    }

    @Override
    public void onDisable() {
    }

    public static void print(Object obj) {
        if (Objects.equals(System.getProperty("plugin.env"), "DEV")) {
            System.out.printf("[%s] %s%n", App.class.getSimpleName(), obj);
        }
    }

    public static void broadcast(Object obj) {
        if (Objects.equals(System.getProperty("plugin.env"), "DEV")) {
            Bukkit.broadcastMessage(
                String.format("[%s] %s", App.class.getSimpleName(), obj));
        }
    }
}
