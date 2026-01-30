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
            section.contains("hours") ? section.getInt("hours") : null,
            section.contains("minutes") ? section.getInt("minutes") : null,
            section.contains("seconds") ? section.getInt("seconds") : null
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
            section.getInt("seconds"),
            section.getInt("amplifier"),
            section.getBoolean("hideParticles", false)
        );
    }

    private ReadyLocation loadReadyLocation(ConfigurationSection section) {
        if (section == null) return null;
        ReadyLocation loc = new ReadyLocation();
        loc.setWorld(section.getString("world", "world"));
        loc.setX(section.getDouble("x", 0.0));
        loc.setY(section.getDouble("y", 64.0));
        loc.setZ(section.getDouble("z", 0.0));
        loc.setYaw((float) section.getDouble("yaw", 0.0));
        loc.setPitch((float) section.getDouble("pitch", 0.0));
        if (section.contains("waitingTime")) {
            loc.setWaitingTime(loadTime(section.getConfigurationSection("waitingTime")));
        }
        return loc;
    }

    private Role loadRole(ConfigurationSection section) {
        Role role = new Role();
        role.setName(section.getString("name"));
        role.setDisplayName(section.getString("displayName"));

        if (section.contains("armorColor")) {
            role.setArmorColor(section.getString("armorColor"));
        }

        if (section.contains("readyLocation")) {
            role.setReadyLocation(loadReadyLocation(section.getConfigurationSection("readyLocation")));
        }

        role.setRespawnLocation(loadLocation(section.getConfigurationSection("respawnLocation")));

        if (section.contains("respawnCount")) {
            role.setRespawnCount(section.getInt("respawnCount"));
        }

        if (section.contains("hasArmor")) {
            role.setHasArmor(section.getBoolean("hasArmor"));
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
        team.setArmorColor(section.getString("armorColor"));

        if (section.contains("readyLocation")) {
            team.setReadyLocation(loadReadyLocation(section.getConfigurationSection("readyLocation")));
        }

        team.setRespawnLocation(loadLocation(section.getConfigurationSection("respawnLocation")));
        team.setRespawnCount(section.getInt("respawnCount", -1));
        team.setHasArmor(section.getBoolean("hasArmor", true));

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
            case "beacon":
                return new BeaconCondition(
                    message,
                    loadLocation(section.getConfigurationSection("location")),
                    section.getInt("hitpoint")
                );

            case "extermination":
                return new ExterminationCondition(
                    message,
                    section.getString("team")
                );

            case "ticket":
                return new TicketCondition(
                    message,
                    section.getString("team"),
                    section.getInt("count")
                );

            case "composite":
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
                String operator = section.getString("operator", "AND");
                return new CompositeCondition(message, conditions, operator);

            default:
                plugin.getLogger().warning("Unknown end condition type: " + type);
                return null;
        }
    }

    // ========== Save Methods ==========

    private void saveTime(String path, Time time) {
        config.set(path + ".hours", time.getHours());
        config.set(path + ".minutes", time.getMinutes());
        config.set(path + ".seconds", time.getSeconds());
    }

    private void saveTimeToSection(ConfigurationSection section, Time time) {
        section.set("hours", time.getHours());
        section.set("minutes", time.getMinutes());
        section.set("seconds", time.getSeconds());
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
        section.set("seconds", effect.getSeconds());
        section.set("amplifier", effect.getAmplifier());
        section.set("hideParticles", effect.isHideParticles());
    }

    private void saveReadyLocation(ConfigurationSection section, String path, ReadyLocation location) {
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
        if (location.getWaitingTime() != null) {
            ConfigurationSection timeSection = section.createSection(path + ".waitingTime");
            saveTimeToSection(timeSection, location.getWaitingTime());
        }
    }

    private void saveRole(ConfigurationSection section, Role role) {
        section.set("name", role.getName());
        section.set("displayName", role.getDisplayName());
        section.set("armorColor", role.getArmorColor());

        if (role.getReadyLocation() != null) {
            saveReadyLocation(section, "readyLocation", role.getReadyLocation());
        }

        saveLocation(section, "respawnLocation", role.getRespawnLocation());
        section.set("respawnCount", role.getRespawnCount());
        section.set("hasArmor", role.getHasArmor());

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
            saveReadyLocation(section, "readyLocation", team.getReadyLocation());
        }

        saveLocation(section, "respawnLocation", team.getRespawnLocation());
        section.set("respawnCount", team.getRespawnCount());
        section.set("hasArmor", team.isHasArmor());

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
            section.set("operator", cc.getOperator());
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
