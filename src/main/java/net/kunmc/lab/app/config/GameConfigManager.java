package net.kunmc.lab.app.config;

import net.kunmc.lab.app.config.data.*;
import net.kunmc.lab.app.config.data.endcondition.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameConfigManager {
    private final Plugin plugin;
    private final File configFile;
    private FileConfiguration config;
    private GameConfig gameConfig;

    public GameConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        loadConfig();
    }

    /**
     * 設定を読み込む
     */
    public void loadConfig() {
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        gameConfig = new GameConfig();

        // config-version
        gameConfig.setConfigVersion(config.getInt("config-version", 1));

        // gamemode
        gameConfig.setGamemode(config.getString("gamemode", "survival"));

        // timeLimit
        if (config.contains("timeLimit")) {
            gameConfig.setTimeLimit(loadTime(config.getConfigurationSection("timeLimit")));
        }

        // teams
        if (config.contains("teams")) {
            List<?> teamsList = config.getList("teams");
            if (teamsList != null) {
                for (Object obj : teamsList) {
                    if (obj instanceof ConfigurationSection) {
                        gameConfig.getTeams().add(loadTeam((ConfigurationSection) obj));
                    }
                }
            }
        }

        // endConditions
        if (config.contains("endConditions")) {
            List<?> conditionsList = config.getList("endConditions");
            if (conditionsList != null) {
                for (Object obj : conditionsList) {
                    if (obj instanceof ConfigurationSection) {
                        EndCondition condition = loadEndCondition((ConfigurationSection) obj);
                        if (condition != null) {
                            gameConfig.getEndConditions().add(condition);
                        }
                    }
                }
            }
        }
    }

    /**
     * 設定を保存する
     */
    public void saveConfig() {
        config.set("config-version", gameConfig.getConfigVersion());
        config.set("gamemode", gameConfig.getGamemode());

        // timeLimit
        if (gameConfig.getTimeLimit() != null) {
            saveTime("timeLimit", gameConfig.getTimeLimit());
        } else {
            config.set("timeLimit", null);
        }

        // teams
        List<ConfigurationSection> teamsList = new ArrayList<>();
        for (Team team : gameConfig.getTeams()) {
            ConfigurationSection teamSection = config.createSection("temp.team");
            saveTeam(teamSection, team);
            teamsList.add(teamSection);
        }
        config.set("teams", teamsList);

        // endConditions
        List<ConfigurationSection> conditionsList = new ArrayList<>();
        for (EndCondition condition : gameConfig.getEndConditions()) {
            ConfigurationSection conditionSection = config.createSection("temp.condition");
            saveEndCondition(conditionSection, condition);
            conditionsList.add(conditionSection);
        }
        config.set("endConditions", conditionsList);

        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save config: " + e.getMessage());
        }
    }

    // ========== Load Methods ==========

    private Time loadTime(ConfigurationSection section) {
        if (section == null) return null;
        return new Time(
            section.contains("hour") ? section.getInt("hour") : null,
            section.contains("minutes") ? section.getInt("minutes") : null,
            section.contains("second") ? section.getInt("second") : null
        );
    }

    private GameLocation loadLocation(ConfigurationSection section) {
        if (section == null) return null;
        return new GameLocation(
            section.getString("world", "world"),
            section.getDouble("x", 0.0),
            section.getDouble("y", 64.0),
            section.getDouble("z", 0.0),
            (float) section.getDouble("yaw", 0.0),
            (float) section.getDouble("pitch", 0.0)
        );
    }

    private Effect loadEffect(ConfigurationSection section) {
        return new Effect(
            section.getString("name"),
            section.getInt("second"),
            section.getInt("amplifier"),
            section.getBoolean("hideParticles", false)
        );
    }

    private Role loadRole(ConfigurationSection section) {
        Role role = new Role();
        role.setName(section.getString("name"));
        role.setDisplayName(section.getString("displayName"));

        if (section.contains("armorColor")) {
            role.setArmorColor(section.getInt("armorColor"));
        }

        if (section.contains("readyLocation")) {
            role.setReadyLocation(loadLocation(section.getConfigurationSection("readyLocation")));
        }

        role.setRespawnLocation(loadLocation(section.getConfigurationSection("respawnLocation")));

        if (section.contains("stock")) {
            role.setStock(section.getInt("stock"));
        }

        if (section.contains("waitingTime")) {
            role.setWaitingTime(loadTime(section.getConfigurationSection("waitingTime")));
        }

        // effects
        if (section.contains("effects")) {
            List<?> effectsList = section.getList("effects");
            if (effectsList != null) {
                for (Object obj : effectsList) {
                    if (obj instanceof ConfigurationSection) {
                        role.getEffects().add(loadEffect((ConfigurationSection) obj));
                    }
                }
            }
        }

        role.setExtendsEffects(section.getBoolean("extendsEffects", false));
        role.setExtendsItem(section.getBoolean("extendsItem", false));

        return role;
    }

    private Team loadTeam(ConfigurationSection section) {
        Team team = new Team();
        team.setName(section.getString("name"));
        team.setDisplayName(section.getString("displayName"));
        team.setArmorColor(section.getInt("armorColor"));

        if (section.contains("readyLocation")) {
            team.setReadyLocation(loadLocation(section.getConfigurationSection("readyLocation")));
        }

        team.setRespawnLocation(loadLocation(section.getConfigurationSection("respawnLocation")));
        team.setStock(section.getInt("stock", -1));
        team.setWaitingTime(loadTime(section.getConfigurationSection("waitingTime")));

        // effects
        if (section.contains("effects")) {
            List<?> effectsList = section.getList("effects");
            if (effectsList != null) {
                for (Object obj : effectsList) {
                    if (obj instanceof ConfigurationSection) {
                        team.getEffects().add(loadEffect((ConfigurationSection) obj));
                    }
                }
            }
        }

        // roles
        if (section.contains("roles")) {
            List<?> rolesList = section.getList("roles");
            if (rolesList != null) {
                for (Object obj : rolesList) {
                    if (obj instanceof ConfigurationSection) {
                        team.getRoles().add(loadRole((ConfigurationSection) obj));
                    }
                }
            }
        }

        return team;
    }

    private EndCondition loadEndCondition(ConfigurationSection section) {
        String type = section.getString("type");
        if (type == null) return null;

        String message = section.getString("message", "");

        switch (type) {
            case "TimeLimit":
                return new TimeLimitCondition(message);

            case "Beacon":
                return new BeaconCondition(
                    message,
                    loadLocation(section.getConfigurationSection("location")),
                    section.getInt("hitpoint")
                );

            case "Extermination":
                return new ExterminationCondition(
                    message,
                    section.getString("team")
                );

            case "Ticket":
                return new TicketCondition(
                    message,
                    section.getString("team"),
                    section.getInt("count")
                );

            case "Composite":
                List<EndCondition> conditions = new ArrayList<>();
                List<?> conditionsList = section.getList("conditions");
                if (conditionsList != null) {
                    for (Object obj : conditionsList) {
                        if (obj instanceof ConfigurationSection) {
                            EndCondition cond = loadEndCondition((ConfigurationSection) obj);
                            if (cond != null) {
                                conditions.add(cond);
                            }
                        }
                    }
                }
                return new CompositeCondition(message, conditions);

            default:
                plugin.getLogger().warning("Unknown end condition type: " + type);
                return null;
        }
    }

    // ========== Save Methods ==========

    private void saveTime(String path, Time time) {
        config.set(path + ".hour", time.getHour());
        config.set(path + ".minutes", time.getMinutes());
        config.set(path + ".second", time.getSecond());
    }

    private void saveLocation(ConfigurationSection section, String path, GameLocation location) {
        if (location == null) {
            section.set(path, null);
            return;
        }
        section.set(path + ".world", location.getWorld());
        section.set(path + ".x", location.getX());
        section.set(path + ".y", location.getY());
        section.set(path + ".z", location.getZ());
        section.set(path + ".yaw", location.getYaw());
        section.set(path + ".pitch", location.getPitch());
    }

    private void saveEffect(ConfigurationSection section, Effect effect) {
        section.set("name", effect.getName());
        section.set("second", effect.getSecond());
        section.set("amplifier", effect.getAmplifier());
        section.set("hideParticles", effect.isHideParticles());
    }

    private void saveRole(ConfigurationSection section, Role role) {
        section.set("name", role.getName());
        section.set("displayName", role.getDisplayName());
        section.set("armorColor", role.getArmorColor());

        if (role.getReadyLocation() != null) {
            saveLocation(section, "readyLocation", role.getReadyLocation());
        }

        saveLocation(section, "respawnLocation", role.getRespawnLocation());
        section.set("stock", role.getStock());

        if (role.getWaitingTime() != null) {
            ConfigurationSection timeSection = section.createSection("waitingTime");
            timeSection.set("hour", role.getWaitingTime().getHour());
            timeSection.set("minutes", role.getWaitingTime().getMinutes());
            timeSection.set("second", role.getWaitingTime().getSecond());
        }

        List<ConfigurationSection> effectsList = new ArrayList<>();
        for (Effect effect : role.getEffects()) {
            ConfigurationSection effectSection = section.createSection("temp.effect");
            saveEffect(effectSection, effect);
            effectsList.add(effectSection);
        }
        section.set("effects", effectsList);

        section.set("extendsEffects", role.isExtendsEffects());
        section.set("extendsItem", role.isExtendsItem());
    }

    private void saveTeam(ConfigurationSection section, Team team) {
        section.set("name", team.getName());
        section.set("displayName", team.getDisplayName());
        section.set("armorColor", team.getArmorColor());

        if (team.getReadyLocation() != null) {
            saveLocation(section, "readyLocation", team.getReadyLocation());
        }

        saveLocation(section, "respawnLocation", team.getRespawnLocation());
        section.set("stock", team.getStock());

        if (team.getWaitingTime() != null) {
            ConfigurationSection timeSection = section.createSection("waitingTime");
            timeSection.set("hour", team.getWaitingTime().getHour());
            timeSection.set("minutes", team.getWaitingTime().getMinutes());
            timeSection.set("second", team.getWaitingTime().getSecond());
        }

        List<ConfigurationSection> effectsList = new ArrayList<>();
        for (Effect effect : team.getEffects()) {
            ConfigurationSection effectSection = section.createSection("temp.effect");
            saveEffect(effectSection, effect);
            effectsList.add(effectSection);
        }
        section.set("effects", effectsList);

        List<ConfigurationSection> rolesList = new ArrayList<>();
        for (Role role : team.getRoles()) {
            ConfigurationSection roleSection = section.createSection("temp.role");
            saveRole(roleSection, role);
            rolesList.add(roleSection);
        }
        section.set("roles", rolesList);
    }

    private void saveEndCondition(ConfigurationSection section, EndCondition condition) {
        section.set("type", condition.getType());
        section.set("message", condition.getMessage());

        if (condition instanceof BeaconCondition) {
            BeaconCondition bc = (BeaconCondition) condition;
            saveLocation(section, "location", bc.getLocation());
            section.set("hitpoint", bc.getHitpoint());
        } else if (condition instanceof ExterminationCondition) {
            ExterminationCondition ec = (ExterminationCondition) condition;
            section.set("team", ec.getTeam());
        } else if (condition instanceof TicketCondition) {
            TicketCondition tc = (TicketCondition) condition;
            section.set("team", tc.getTeam());
            section.set("count", tc.getCount());
        } else if (condition instanceof CompositeCondition) {
            CompositeCondition cc = (CompositeCondition) condition;
            List<ConfigurationSection> conditionsList = new ArrayList<>();
            for (EndCondition cond : cc.getConditions()) {
                ConfigurationSection condSection = section.createSection("temp.condition");
                saveEndCondition(condSection, cond);
                conditionsList.add(condSection);
            }
            section.set("conditions", conditionsList);
        }
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    public void setGameConfig(GameConfig gameConfig) {
        this.gameConfig = gameConfig;
    }
}
