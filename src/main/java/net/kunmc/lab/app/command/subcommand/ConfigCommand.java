package net.kunmc.lab.app.command.subcommand;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.kunmc.lab.app.Store;
import net.kunmc.lab.app.command.KCDKCommand;
import net.kunmc.lab.app.command.SubCommand;
import net.kunmc.lab.app.config.EndConditionDeserializer;
import net.kunmc.lab.app.config.data.*;
import net.kunmc.lab.app.config.data.endcondition.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigCommand implements SubCommand {

    @Override
    public String getName() {
        return "config";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /kcdk config <...>");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        // 特殊なコマンド
        switch (subCommand) {
            case "show":
                return handleShow(sender);

            case "save":
                return handleSave(sender);

            case "reload":
                return handleReload(sender);

            case "import":
                return handleImport(sender, args);
        }

        // gamemode
        if ("gamemode".equals(subCommand)) {
            return handleGamemode(sender, args);
        }

        // showBossBar
        if ("showbossbar".equals(subCommand)) {
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /kcdk config showBossBar <true|false>");
                return true;
            }
            // TODO: showBossBarはGameConfigにないため、後で実装
            sender.sendMessage("§eShowBossBar is not implemented in GameConfig yet");
            return true;
        }

        // timeLimit
        if ("timelimit".equals(subCommand)) {
            return handleTimeLimit(sender, args);
        }

        // team
        if ("team".equals(subCommand)) {
            return handleTeamCommand(sender, Arrays.copyOfRange(args, 1, args.length));
        }

        // endCondition
        if ("endcondition".equals(subCommand)) {
            return handleEndConditionCommand(sender, Arrays.copyOfRange(args, 1, args.length));
        }

        sender.sendMessage("§cUnknown config subcommand: " + subCommand);
        return true;
    }

    // ========== 基本コマンド ==========

    private boolean handleShow(CommandSender sender) {
        GameConfig config = Store.config.getGameConfig();
        sender.sendMessage("§a=== KCDK Configuration ===");
        sender.sendMessage("§eConfig Version: §f" + config.getConfigVersion());
        sender.sendMessage("§eGamemode: §f" + config.getGamemode());
        sender.sendMessage("§eTime Limit: §f" + (config.getTimeLimit() != null ?
                config.getTimeLimit().getHour() + "h " + config.getTimeLimit().getMinutes() + "m " + config.getTimeLimit().getSecond() + "s" : "none"));
        sender.sendMessage("§eTeams: §f" + config.getTeams().size());
        for (Team team : config.getTeams()) {
            sender.sendMessage("  §7- §f" + team.getName() + " §7(§f" + team.getDisplayName() + "§7)");
        }
        sender.sendMessage("§eEnd Conditions: §f" + config.getEndConditions().size());
        return true;
    }

    private boolean handleSave(CommandSender sender) {
        try {
            Store.config.saveGameConfig();
            sender.sendMessage("§aConfiguration saved to config.yml");
        } catch (Exception e) {
            sender.sendMessage("§cFailed to save configuration: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        try {
            Store.config.reloadGameConfig();
            sender.sendMessage("§aConfiguration reloaded from config.yml");
        } catch (Exception e) {
            sender.sendMessage("§cFailed to reload configuration: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    private boolean handleImport(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /kcdk config import <json>");
            return true;
        }

        // 全ての引数を結合してJSONとして扱う
        String json = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        try {
            // GsonでGameConfigをデシリアライズ
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(EndCondition.class, new EndConditionDeserializer())
                    .create();

            GameConfig importedConfig = gson.fromJson(json, GameConfig.class);

            if (importedConfig == null) {
                sender.sendMessage("§cFailed to parse JSON: result is null");
                return true;
            }

            // 現在の設定を置き換え
            Store.config.setGameConfig(importedConfig);

            // YAMLに保存
            Store.config.saveGameConfig();

            sender.sendMessage("§aConfiguration imported from JSON successfully");
            sender.sendMessage("§eTeams imported: §f" + importedConfig.getTeams().size());
            sender.sendMessage("§eEnd conditions imported: §f" + importedConfig.getEndConditions().size());

        } catch (JsonSyntaxException e) {
            sender.sendMessage("§cInvalid JSON format: " + e.getMessage());
        } catch (Exception e) {
            sender.sendMessage("§cFailed to import configuration: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    // ========== Gamemode ==========

    private boolean handleGamemode(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /kcdk config gamemode <gamemode>");
            return true;
        }

        String gamemode = args[1].toLowerCase();
        if (!Arrays.asList("survival", "adventure", "creative", "spectator").contains(gamemode)) {
            sender.sendMessage("§cInvalid gamemode. Use: survival, adventure, creative, spectator");
            return true;
        }

        Store.config.getGameConfig().setGamemode(gamemode);
        sender.sendMessage("§aGamemode set to: §f" + gamemode);
        return true;
    }

    // ========== TimeLimit ==========

    private boolean handleTimeLimit(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /kcdk config timeLimit <set|remove>");
            return true;
        }

        String action = args[1].toLowerCase();
        if ("set".equals(action)) {
            if (args.length < 5) {
                sender.sendMessage("§cUsage: /kcdk config timeLimit set <hour> <minutes> <second>");
                return true;
            }

            try {
                int hour = Integer.parseInt(args[2]);
                int minutes = Integer.parseInt(args[3]);
                int second = Integer.parseInt(args[4]);

                Time timeLimit = new Time(hour, minutes, second);
                Store.config.getGameConfig().setTimeLimit(timeLimit);
                sender.sendMessage("§aTime limit set to: §f" + hour + "h " + minutes + "m " + second + "s");
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid number format");
            }
            return true;
        } else if ("remove".equals(action)) {
            Store.config.getGameConfig().setTimeLimit(null);
            sender.sendMessage("§aTime limit removed");
            return true;
        }

        sender.sendMessage("§cUnknown action: " + action);
        return true;
    }

    // ========== Team Commands ==========

    private boolean handleTeamCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /kcdk config team <add|remove|clear|<team>>");
            return true;
        }

        String action = args[0].toLowerCase();

        // team add
        if ("add".equals(action)) {
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /kcdk config team add <team>");
                return true;
            }

            String teamName = args[1];
            Team newTeam = new Team();
            newTeam.setName(teamName);
            newTeam.setDisplayName(teamName);
            newTeam.setArmorColor(16777215); // 白色
            newTeam.setStock(-1);
            newTeam.setWaitingTime(new Time(0, 0, 0));

            // デフォルトのリスポーン地点
            GameLocation defaultLoc = new GameLocation("world", 0, 64, 0, 0, 0);
            newTeam.setRespawnLocation(defaultLoc);

            Store.config.getGameConfig().getTeams().add(newTeam);
            sender.sendMessage("§aTeam added: §f" + teamName);
            return true;
        }

        // team remove
        if ("remove".equals(action)) {
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /kcdk config team remove <team>");
                return true;
            }

            String teamName = args[1];
            boolean removed = Store.config.getGameConfig().getTeams().removeIf(t -> t.getName().equalsIgnoreCase(teamName));

            if (removed) {
                sender.sendMessage("§aTeam removed: §f" + teamName);
            } else {
                sender.sendMessage("§cTeam not found: " + teamName);
            }
            return true;
        }

        // team clear
        if ("clear".equals(action)) {
            Store.config.getGameConfig().getTeams().clear();
            sender.sendMessage("§aAll teams cleared");
            return true;
        }

        // team <team> ...
        String teamName = args[0];
        Team team = getTeam(teamName);
        if (team == null) {
            sender.sendMessage("§cTeam not found: " + teamName);
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /kcdk config team <team> <property>");
            return true;
        }

        String property = args[1].toLowerCase();

        // role サブコマンド
        if ("role".equals(property)) {
            return handleRoleCommand(sender, team, Arrays.copyOfRange(args, 2, args.length));
        }

        // effect サブコマンド
        if ("effect".equals(property)) {
            return handleEffectCommand(sender, team.getEffects(), teamName, null, Arrays.copyOfRange(args, 2, args.length));
        }

        // その他のプロパティ
        return handleTeamProperty(sender, team, property, Arrays.copyOfRange(args, 2, args.length));
    }

    private boolean handleTeamProperty(CommandSender sender, Team team, String property, String[] args) {
        switch (property) {
            case "displayname":
                if (args.length < 1) {
                    sender.sendMessage("§cUsage: /kcdk config team <team> displayName <displayName>");
                    return true;
                }
                String displayName = String.join(" ", args);
                team.setDisplayName(displayName);
                sender.sendMessage("§aDisplay name set to: §f" + displayName);
                return true;

            case "armorcolor":
                if (args.length < 1) {
                    sender.sendMessage("§cUsage: /kcdk config team <team> armorColor <color>");
                    return true;
                }
                try {
                    int color = Integer.parseInt(args[0]);
                    team.setArmorColor(color);
                    sender.sendMessage("§aArmor color set to: §f" + color);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid color value");
                }
                return true;

            case "readylocation":
                if (args.length > 0 && "remove".equals(args[0].toLowerCase())) {
                    team.setReadyLocation(null);
                    sender.sendMessage("§aReady location removed");
                    return true;
                }
                if (args.length < 6) {
                    sender.sendMessage("§cUsage: /kcdk config team <team> readyLocation <world> <x> <y> <z> <yaw> <pitch>");
                    return true;
                }
                try {
                    GameLocation loc = new GameLocation(
                            args[0],
                            Double.parseDouble(args[1]),
                            Double.parseDouble(args[2]),
                            Double.parseDouble(args[3]),
                            Float.parseFloat(args[4]),
                            Float.parseFloat(args[5])
                    );
                    team.setReadyLocation(loc);
                    sender.sendMessage("§aReady location set");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid location values");
                }
                return true;

            case "respawnlocation":
                if (args.length < 6) {
                    sender.sendMessage("§cUsage: /kcdk config team <team> respawnLocation <world> <x> <y> <z> <yaw> <pitch>");
                    return true;
                }
                try {
                    GameLocation loc = new GameLocation(
                            args[0],
                            Double.parseDouble(args[1]),
                            Double.parseDouble(args[2]),
                            Double.parseDouble(args[3]),
                            Float.parseFloat(args[4]),
                            Float.parseFloat(args[5])
                    );
                    team.setRespawnLocation(loc);
                    sender.sendMessage("§aRespawn location set");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid location values");
                }
                return true;

            case "stock":
                if (args.length < 1) {
                    sender.sendMessage("§cUsage: /kcdk config team <team> stock <count>");
                    return true;
                }
                try {
                    int stock = Integer.parseInt(args[0]);
                    team.setStock(stock);
                    sender.sendMessage("§aStock set to: §f" + stock);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid number");
                }
                return true;

            case "waitingtime":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /kcdk config team <team> waitingTime <hour> <minutes> <second>");
                    return true;
                }
                try {
                    Time time = new Time(
                            Integer.parseInt(args[0]),
                            Integer.parseInt(args[1]),
                            Integer.parseInt(args[2])
                    );
                    team.setWaitingTime(time);
                    sender.sendMessage("§aWaiting time set to: §f" + args[0] + "h " + args[1] + "m " + args[2] + "s");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid time values");
                }
                return true;

            default:
                sender.sendMessage("§cUnknown property: " + property);
                return true;
        }
    }

    // ========== Role Commands ==========

    private boolean handleRoleCommand(CommandSender sender, Team team, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /kcdk config team <team> role <add|remove|clear|<role>>");
            return true;
        }

        String action = args[0].toLowerCase();

        // role add
        if ("add".equals(action)) {
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /kcdk config team <team> role add <role>");
                return true;
            }

            String roleName = args[1];
            Role newRole = new Role();
            newRole.setName(roleName);
            newRole.setRespawnLocation(team.getRespawnLocation());
            newRole.setExtendsEffects(false);
            newRole.setExtendsItem(false);

            team.getRoles().add(newRole);
            sender.sendMessage("§aRole added to team " + team.getName() + ": §f" + roleName);
            return true;
        }

        // role remove
        if ("remove".equals(action)) {
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /kcdk config team <team> role remove <role>");
                return true;
            }

            String roleName = args[1];
            boolean removed = team.getRoles().removeIf(r -> r.getName().equalsIgnoreCase(roleName));

            if (removed) {
                sender.sendMessage("§aRole removed from team " + team.getName() + ": §f" + roleName);
            } else {
                sender.sendMessage("§cRole not found: " + roleName);
            }
            return true;
        }

        // role clear
        if ("clear".equals(action)) {
            team.getRoles().clear();
            sender.sendMessage("§aAll roles cleared from team " + team.getName());
            return true;
        }

        // role <role> <property>
        String roleName = args[0];
        Role role = team.getRoles().stream()
                .filter(r -> r.getName().equalsIgnoreCase(roleName))
                .findFirst()
                .orElse(null);

        if (role == null) {
            sender.sendMessage("§cRole not found: " + roleName);
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /kcdk config team <team> role <role> <property>");
            return true;
        }

        String property = args[1].toLowerCase();

        // effect サブコマンド
        if ("effect".equals(property)) {
            return handleEffectCommand(sender, role.getEffects(), team.getName(), roleName, Arrays.copyOfRange(args, 2, args.length));
        }

        // その他のプロパティ
        return handleRoleProperty(sender, role, team.getName(), property, Arrays.copyOfRange(args, 2, args.length));
    }

    private boolean handleRoleProperty(CommandSender sender, Role role, String teamName, String property, String[] args) {
        switch (property) {
            case "displayname":
                if (args.length > 0 && "remove".equals(args[0].toLowerCase())) {
                    role.setDisplayName(null);
                    sender.sendMessage("§aRole display name removed (will inherit from team)");
                    return true;
                }
                if (args.length < 1) {
                    sender.sendMessage("§cUsage: /kcdk config team <team> role <role> displayName <displayName>");
                    return true;
                }
                String displayName = String.join(" ", args);
                role.setDisplayName(displayName);
                sender.sendMessage("§aRole display name set to: §f" + displayName);
                return true;

            case "armorcolor":
                if (args.length > 0 && "remove".equals(args[0].toLowerCase())) {
                    role.setArmorColor(null);
                    sender.sendMessage("§aRole armor color removed (will inherit from team)");
                    return true;
                }
                if (args.length < 1) {
                    sender.sendMessage("§cUsage: /kcdk config team <team> role <role> armorColor <color>");
                    return true;
                }
                try {
                    int color = Integer.parseInt(args[0]);
                    role.setArmorColor(color);
                    sender.sendMessage("§aRole armor color set to: §f" + color);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid color value");
                }
                return true;

            case "readylocation":
                if (args.length > 0 && "remove".equals(args[0].toLowerCase())) {
                    role.setReadyLocation(null);
                    sender.sendMessage("§aRole ready location removed (will inherit from team)");
                    return true;
                }
                if (args.length < 6) {
                    sender.sendMessage("§cUsage: /kcdk config team <team> role <role> readyLocation <world> <x> <y> <z> <yaw> <pitch>");
                    return true;
                }
                try {
                    GameLocation loc = new GameLocation(
                            args[0],
                            Double.parseDouble(args[1]),
                            Double.parseDouble(args[2]),
                            Double.parseDouble(args[3]),
                            Float.parseFloat(args[4]),
                            Float.parseFloat(args[5])
                    );
                    role.setReadyLocation(loc);
                    sender.sendMessage("§aRole ready location set");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid location values");
                }
                return true;

            case "respawnlocation":
                if (args.length < 6) {
                    sender.sendMessage("§cUsage: /kcdk config team <team> role <role> respawnLocation <world> <x> <y> <z> <yaw> <pitch>");
                    return true;
                }
                try {
                    GameLocation loc = new GameLocation(
                            args[0],
                            Double.parseDouble(args[1]),
                            Double.parseDouble(args[2]),
                            Double.parseDouble(args[3]),
                            Float.parseFloat(args[4]),
                            Float.parseFloat(args[5])
                    );
                    role.setRespawnLocation(loc);
                    sender.sendMessage("§aRole respawn location set");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid location values");
                }
                return true;

            case "stock":
                if (args.length > 0 && "remove".equals(args[0].toLowerCase())) {
                    role.setStock(null);
                    sender.sendMessage("§aRole stock removed (will inherit from team)");
                    return true;
                }
                if (args.length < 1) {
                    sender.sendMessage("§cUsage: /kcdk config team <team> role <role> stock <count>");
                    return true;
                }
                try {
                    int stock = Integer.parseInt(args[0]);
                    role.setStock(stock);
                    sender.sendMessage("§aRole stock set to: §f" + stock);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid number");
                }
                return true;

            case "waitingtime":
                if (args.length > 0 && "remove".equals(args[0].toLowerCase())) {
                    role.setWaitingTime(null);
                    sender.sendMessage("§aRole waiting time removed (will inherit from team)");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /kcdk config team <team> role <role> waitingTime <hour> <minutes> <second>");
                    return true;
                }
                try {
                    Time time = new Time(
                            Integer.parseInt(args[0]),
                            Integer.parseInt(args[1]),
                            Integer.parseInt(args[2])
                    );
                    role.setWaitingTime(time);
                    sender.sendMessage("§aRole waiting time set to: §f" + args[0] + "h " + args[1] + "m " + args[2] + "s");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid time values");
                }
                return true;

            case "extendseffects":
                if (args.length < 1) {
                    sender.sendMessage("§cUsage: /kcdk config team <team> role <role> extendsEffects <true|false>");
                    return true;
                }
                boolean extendsEffects = Boolean.parseBoolean(args[0]);
                role.setExtendsEffects(extendsEffects);
                sender.sendMessage("§aRole extendsEffects set to: §f" + extendsEffects);
                return true;

            case "extendsitem":
                if (args.length < 1) {
                    sender.sendMessage("§cUsage: /kcdk config team <team> role <role> extendsItem <true|false>");
                    return true;
                }
                boolean extendsItem = Boolean.parseBoolean(args[0]);
                role.setExtendsItem(extendsItem);
                sender.sendMessage("§aRole extendsItem set to: §f" + extendsItem);
                return true;

            default:
                sender.sendMessage("§cUnknown property: " + property);
                return true;
        }
    }

    // ========== Effect Commands ==========

    private boolean handleEffectCommand(CommandSender sender, List<Effect> effects, String teamName, String roleName, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: ... effect <add|remove|clear>");
            return true;
        }

        String action = args[0].toLowerCase();
        String target = roleName != null ? "role " + roleName : "team " + teamName;

        // effect add
        if ("add".equals(action)) {
            if (args.length < 5) {
                sender.sendMessage("§cUsage: ... effect add <effectName> <second> <amplifier> <hideParticles>");
                return true;
            }

            String effectName = args[1].toUpperCase();
            PotionEffectType type = PotionEffectType.getByName(effectName);
            if (type == null) {
                sender.sendMessage("§cInvalid effect name: " + effectName);
                return true;
            }

            try {
                int second = Integer.parseInt(args[2]);
                int amplifier = Integer.parseInt(args[3]);
                boolean hideParticles = Boolean.parseBoolean(args[4]);

                Effect effect = new Effect(effectName, second, amplifier, hideParticles);
                effects.add(effect);
                sender.sendMessage("§aEffect added to " + target + ": §f" + effectName);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid number values");
            }
            return true;
        }

        // effect remove
        if ("remove".equals(action)) {
            if (args.length < 2) {
                sender.sendMessage("§cUsage: ... effect remove <effectName>");
                return true;
            }

            String effectName = args[1].toUpperCase();
            boolean removed = effects.removeIf(e -> e.getName().equalsIgnoreCase(effectName));

            if (removed) {
                sender.sendMessage("§aEffect removed from " + target + ": §f" + effectName);
            } else {
                sender.sendMessage("§cEffect not found: " + effectName);
            }
            return true;
        }

        // effect clear
        if ("clear".equals(action)) {
            effects.clear();
            sender.sendMessage("§aAll effects cleared from " + target);
            return true;
        }

        sender.sendMessage("§cUnknown action: " + action);
        return true;
    }

    // ========== EndCondition Commands ==========

    private boolean handleEndConditionCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /kcdk config endCondition <add|remove|clear|list>");
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "add":
                return handleEndConditionAdd(sender, Arrays.copyOfRange(args, 1, args.length));

            case "remove":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /kcdk config endCondition remove <index>");
                    return true;
                }
                try {
                    int index = Integer.parseInt(args[1]);
                    List<EndCondition> conditions = Store.config.getGameConfig().getEndConditions();
                    if (index < 0 || index >= conditions.size()) {
                        sender.sendMessage("§cInvalid index: " + index);
                        return true;
                    }
                    conditions.remove(index);
                    sender.sendMessage("§aEnd condition removed at index: §f" + index);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid index");
                }
                return true;

            case "clear":
                Store.config.getGameConfig().getEndConditions().clear();
                sender.sendMessage("§aAll end conditions cleared");
                return true;

            case "list":
                List<EndCondition> conditions = Store.config.getGameConfig().getEndConditions();
                sender.sendMessage("§a=== End Conditions (" + conditions.size() + ") ===");
                for (int i = 0; i < conditions.size(); i++) {
                    EndCondition cond = conditions.get(i);
                    sender.sendMessage("§e[" + i + "] §f" + cond.getType() + " §7- §f" + cond.getMessage());
                }
                return true;

            default:
                sender.sendMessage("§cUnknown action: " + action);
                return true;
        }
    }

    private boolean handleEndConditionAdd(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /kcdk config endCondition add <type>");
            return true;
        }

        String type = args[0].toLowerCase();

        switch (type) {
            case "timelimit":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /kcdk config endCondition add timeLimit <message>");
                    return true;
                }
                String timeLimitMsg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                Store.config.getGameConfig().getEndConditions().add(new TimeLimitCondition(timeLimitMsg));
                sender.sendMessage("§aTimeLimit condition added");
                return true;

            case "beacon":
                if (args.length < 9) {
                    sender.sendMessage("§cUsage: /kcdk config endCondition add beacon <message> <world> <x> <y> <z> <yaw> <pitch> <hitpoint>");
                    return true;
                }
                try {
                    String beaconMsg = args[1];
                    GameLocation loc = new GameLocation(
                            args[2],
                            Double.parseDouble(args[3]),
                            Double.parseDouble(args[4]),
                            Double.parseDouble(args[5]),
                            Float.parseFloat(args[6]),
                            Float.parseFloat(args[7])
                    );
                    int hitpoint = Integer.parseInt(args[8]);
                    Store.config.getGameConfig().getEndConditions().add(new BeaconCondition(beaconMsg, loc, hitpoint));
                    sender.sendMessage("§aBeacon condition added");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid number values");
                }
                return true;

            case "extermination":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /kcdk config endCondition add extermination <message> <team>");
                    return true;
                }
                String exterminationMsg = args[1];
                String exterminationTeam = args[2];
                Store.config.getGameConfig().getEndConditions().add(new ExterminationCondition(exterminationMsg, exterminationTeam));
                sender.sendMessage("§aExtermination condition added for team: §f" + exterminationTeam);
                return true;

            case "ticket":
                if (args.length < 4) {
                    sender.sendMessage("§cUsage: /kcdk config endCondition add ticket <message> <team> <count>");
                    return true;
                }
                try {
                    String ticketMsg = args[1];
                    String ticketTeam = args[2];
                    int count = Integer.parseInt(args[3]);
                    Store.config.getGameConfig().getEndConditions().add(new TicketCondition(ticketMsg, ticketTeam, count));
                    sender.sendMessage("§aTicket condition added for team: §f" + ticketTeam + " §7(count: " + count + ")");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid count value");
                }
                return true;

            default:
                sender.sendMessage("§cUnknown end condition type: " + type);
                return true;
        }
    }

    // ========== TAB Completion ==========

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return KCDKCommand.filterStartingWith(args[0], Arrays.asList(
                    "gamemode", "showBossBar", "timeLimit", "team", "endCondition", "show", "save", "reload", "import"
            ));
        }

        String subCommand = args[0].toLowerCase();

        if ("gamemode".equals(subCommand) && args.length == 2) {
            return KCDKCommand.filterStartingWith(args[1], Arrays.asList("survival", "adventure", "creative", "spectator"));
        }

        if ("showbossbar".equals(subCommand) && args.length == 2) {
            return KCDKCommand.filterStartingWith(args[1], Arrays.asList("true", "false"));
        }

        if ("timelimit".equals(subCommand)) {
            if (args.length == 2) {
                return KCDKCommand.filterStartingWith(args[1], Arrays.asList("set", "remove"));
            }
        }

        if ("team".equals(subCommand)) {
            return tabCompleteTeam(args);
        }

        if ("endcondition".equals(subCommand)) {
            return tabCompleteEndCondition(args);
        }

        return Collections.emptyList();
    }

    private List<String> tabCompleteTeam(String[] args) {
        if (args.length == 2) {
            List<String> options = new ArrayList<>(Arrays.asList("add", "remove", "clear"));
            if (Store.config != null && Store.config.getGameConfig() != null) {
                options.addAll(Store.config.getGameConfig().getTeams().stream()
                        .map(Team::getName)
                        .collect(Collectors.toList()));
            }
            return KCDKCommand.filterStartingWith(args[1], options);
        }

        String action = args[1].toLowerCase();

        if ("add".equals(action)) {
            return Collections.emptyList();
        }

        if ("remove".equals(action) && args.length == 3) {
            return getTeamNames(args[2]);
        }

        if (args.length == 3 && !Arrays.asList("add", "remove", "clear").contains(action)) {
            return KCDKCommand.filterStartingWith(args[2], Arrays.asList(
                    "displayName", "armorColor", "readyLocation", "respawnLocation",
                    "stock", "waitingTime", "effect", "role"
            ));
        }

        if (args.length >= 4) {
            String teamName = args[1];
            String property = args[2].toLowerCase();

            if ("role".equals(property)) {
                return tabCompleteRole(teamName, Arrays.copyOfRange(args, 3, args.length));
            }

            if ("effect".equals(property)) {
                return tabCompleteEffect(Arrays.copyOfRange(args, 3, args.length));
            }

            if (("readylocation".equals(property) || "respawnlocation".equals(property)) && args.length == 4) {
                List<String> options = new ArrayList<>(Collections.singletonList("remove"));
                options.addAll(Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()));
                return KCDKCommand.filterStartingWith(args[3], options);
            }
        }

        return Collections.emptyList();
    }

    private List<String> tabCompleteRole(String teamName, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(Arrays.asList("add", "remove", "clear"));
            Team team = getTeam(teamName);
            if (team != null) {
                options.addAll(team.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()));
            }
            return KCDKCommand.filterStartingWith(args[0], options);
        }

        String action = args[0].toLowerCase();

        if ("add".equals(action)) {
            return Collections.emptyList();
        }

        if ("remove".equals(action) && args.length == 2) {
            return getRoleNames(teamName, args[1]);
        }

        if (args.length == 2 && !Arrays.asList("add", "remove", "clear").contains(action)) {
            return KCDKCommand.filterStartingWith(args[1], Arrays.asList(
                    "displayName", "armorColor", "readyLocation", "respawnLocation",
                    "stock", "waitingTime", "extendsEffects", "extendsItem", "effect"
            ));
        }

        if (args.length >= 3) {
            String property = args[1].toLowerCase();

            if ("effect".equals(property)) {
                return tabCompleteEffect(Arrays.copyOfRange(args, 2, args.length));
            }

            if (args.length == 3 && Arrays.asList("displayname", "armorcolor", "readylocation",
                    "stock", "waitingtime").contains(property)) {
                return KCDKCommand.filterStartingWith(args[2], Collections.singletonList("remove"));
            }

            if (("readylocation".equals(property) || "respawnlocation".equals(property)) && args.length == 3) {
                List<String> options = new ArrayList<>(Collections.singletonList("remove"));
                options.addAll(Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()));
                return KCDKCommand.filterStartingWith(args[2], options);
            }

            if (("extendseffects".equals(property) || "extendsitem".equals(property)) && args.length == 3) {
                return KCDKCommand.filterStartingWith(args[2], Arrays.asList("true", "false"));
            }
        }

        return Collections.emptyList();
    }

    private List<String> tabCompleteEffect(String[] args) {
        if (args.length == 1) {
            return KCDKCommand.filterStartingWith(args[0], Arrays.asList("add", "remove", "clear"));
        }

        String action = args[0].toLowerCase();

        if ("add".equals(action) && args.length == 2) {
            return KCDKCommand.filterStartingWith(args[1], getPotionEffectTypes());
        }

        if ("add".equals(action) && args.length == 5) {
            return KCDKCommand.filterStartingWith(args[4], Arrays.asList("true", "false"));
        }

        if ("remove".equals(action) && args.length == 2) {
            return KCDKCommand.filterStartingWith(args[1], getPotionEffectTypes());
        }

        return Collections.emptyList();
    }

    private List<String> tabCompleteEndCondition(String[] args) {
        if (args.length == 2) {
            return KCDKCommand.filterStartingWith(args[1], Arrays.asList("add", "remove", "clear", "list"));
        }

        String action = args[1].toLowerCase();

        if ("add".equals(action) && args.length == 3) {
            return KCDKCommand.filterStartingWith(args[2], Arrays.asList("timeLimit", "beacon", "extermination", "ticket"));
        }

        if ("add".equals(action) && args.length >= 4) {
            String type = args[2].toLowerCase();

            if ("extermination".equals(type) && args.length == 4) {
                return getTeamNames(args[3]);
            }

            if ("ticket".equals(type) && args.length == 4) {
                return getTeamNames(args[3]);
            }

            if ("beacon".equals(type) && args.length == 4) {
                return KCDKCommand.filterStartingWith(args[3],
                        Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()));
            }
        }

        return Collections.emptyList();
    }

    // ========== Helper Methods ==========

    private List<String> getTeamNames(String prefix) {
        if (Store.config == null || Store.config.getGameConfig() == null) {
            return Collections.emptyList();
        }

        return Store.config.getGameConfig().getTeams().stream()
                .map(Team::getName)
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> KCDKCommand.filterStartingWith(prefix, list)
                ));
    }

    private List<String> getRoleNames(String teamName, String prefix) {
        Team team = getTeam(teamName);
        if (team == null) {
            return Collections.emptyList();
        }

        return team.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> KCDKCommand.filterStartingWith(prefix, list)
                ));
    }

    private Team getTeam(String teamName) {
        if (Store.config == null || Store.config.getGameConfig() == null) {
            return null;
        }

        return Store.config.getGameConfig().getTeams().stream()
                .filter(t -> t.getName().equalsIgnoreCase(teamName))
                .findFirst()
                .orElse(null);
    }

    private List<String> getPotionEffectTypes() {
        List<String> effects = new ArrayList<>();
        for (PotionEffectType type : PotionEffectType.values()) {
            if (type != null) {
                effects.add(type.getName());
            }
        }
        return effects;
    }
}
