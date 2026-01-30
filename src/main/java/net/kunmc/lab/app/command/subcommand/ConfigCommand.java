package net.kunmc.lab.app.command.subcommand;

import com.google.gson.*;
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
import org.bukkit.scoreboard.Scoreboard;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigCommand implements SubCommand {

    // import-part用の部分データ保持
    private static final Map<UUID, PartialImport> partialImports = new HashMap<>();
    private static final long PARTIAL_IMPORT_TIMEOUT_MS = 5 * 60 * 1000; // 5分

    private static class PartialImport {
        final String[] parts;
        final int totalParts;
        final long timestamp;
        int receivedCount;

        PartialImport(int totalParts) {
            this.parts = new String[totalParts];
            this.totalParts = totalParts;
            this.timestamp = System.currentTimeMillis();
            this.receivedCount = 0;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > PARTIAL_IMPORT_TIMEOUT_MS;
        }

        boolean isComplete() {
            return receivedCount == totalParts;
        }

        String getCombined() {
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                sb.append(part);
            }
            return sb.toString();
        }
    }

    // JSON短縮キー対応表（逆引き用）
    private static final Map<String, String> REVERSE_KEY_MAP = createReverseKeyMap();

    private static Map<String, String> createReverseKeyMap() {
        Map<String, String> map = new HashMap<>();
        map.put("a", "gamemode");
        map.put("b", "bossbar");
        map.put("c", "timeLimit");
        map.put("d", "startupCommands");
        map.put("e", "shutdownCommands");
        map.put("f", "teams");
        map.put("g", "endConditions");
        map.put("h", "mcid");
        map.put("i", "hours");
        map.put("j", "minutes");
        map.put("k", "seconds");
        map.put("l", "name");
        map.put("m", "displayName");
        map.put("n", "armorColor");
        map.put("o", "respawnCount");
        map.put("p", "readyLocation");
        map.put("q", "respawnLocation");
        map.put("r", "effects");
        map.put("s", "roles");
        map.put("t", "world");
        map.put("u", "x");
        map.put("v", "y");
        map.put("w", "z");
        map.put("A", "yaw");
        map.put("B", "pitch");
        map.put("C", "waitingTime");
        map.put("D", "extendsEffects");
        map.put("E", "extendsItem");
        map.put("F", "amplifier");
        map.put("G", "hideParticles");
        map.put("H", "type");
        map.put("I", "message");
        map.put("J", "conditions");
        map.put("K", "operator");
        map.put("L", "location");
        map.put("M", "hitpoint");
        map.put("N", "team");
        map.put("O", "count");
        map.put("P", "hasArmor");
        return Collections.unmodifiableMap(map);
    }

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

            case "import-part":
                return handleImportPart(sender, args);
        }

        // gamemode
        if ("gamemode".equals(subCommand)) {
            return handleGamemode(sender, args);
        }

        // bossbar
        if ("bossbar".equals(subCommand)) {
            return handleBossbar(sender, args);
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
        sender.sendMessage("§eBossbar: §f" + (config.getBossbar() != null ? config.getBossbar().getMcid() : "none"));
        sender.sendMessage("§eTime Limit: §f" + (config.getTimeLimit() != null ?
                config.getTimeLimit().getHours() + "h " + config.getTimeLimit().getMinutes() + "m " + config.getTimeLimit().getSeconds() + "s" : "none"));
        sender.sendMessage("§eStartup Commands: §f" + config.getStartupCommands().size());
        sender.sendMessage("§eShutdown Commands: §f" + config.getShutdownCommands().size());
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
            sender.sendMessage("§cUsage: /kcdk config import <base64>");
            return true;
        }

        // Base64エンコードされた短縮JSONを受け取る
        String encoded = args[1];

        try {
            // Base64デコード
            String json = decodeBase64(encoded);

            // 短縮キーを展開
            JsonObject expanded = expandKeys(JsonParser.parseString(json));

            // GsonでGameConfigをデシリアライズ
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(EndCondition.class, new EndConditionDeserializer())
                    .create();

            GameConfig importedConfig = gson.fromJson(expanded, GameConfig.class);

            if (importedConfig == null) {
                sender.sendMessage("§cFailed to parse JSON: result is null");
                return true;
            }

            // 現在の設定を置き換え
            Store.config.setGameConfig(importedConfig);

            // YAMLに保存
            Store.config.saveGameConfig();

            // Scoreboardチーム自動作成
            createScoreboardTeams(importedConfig);

            sender.sendMessage("§aConfiguration imported from Base64 successfully");
            sender.sendMessage("§eTeams imported: §f" + importedConfig.getTeams().size());
            sender.sendMessage("§eEnd conditions imported: §f" + importedConfig.getEndConditions().size());

        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid Base64 format: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            sender.sendMessage("§cInvalid JSON format: " + e.getMessage());
        } catch (Exception e) {
            sender.sendMessage("§cFailed to import configuration: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    private boolean handleImportPart(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /kcdk config import-part <current>/<total> <data>");
            return true;
        }

        // パーツ番号をパース (例: "1/3")
        String[] partInfo = args[1].split("/");
        if (partInfo.length != 2) {
            sender.sendMessage("§cInvalid part format. Use: <current>/<total>");
            return true;
        }

        int currentPart;
        int totalParts;
        try {
            currentPart = Integer.parseInt(partInfo[0]);
            totalParts = Integer.parseInt(partInfo[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid part numbers");
            return true;
        }

        if (currentPart < 1 || currentPart > totalParts || totalParts < 1) {
            sender.sendMessage("§cInvalid part range: " + currentPart + "/" + totalParts);
            return true;
        }

        String data = args[2];

        // プレイヤーでない場合はUUIDなし
        UUID senderId;
        if (sender instanceof org.bukkit.entity.Player) {
            senderId = ((org.bukkit.entity.Player) sender).getUniqueId();
        } else {
            senderId = new UUID(0, 0); // コンソール用
        }

        // タイムアウトした古いデータをクリア
        partialImports.entrySet().removeIf(entry -> entry.getValue().isExpired());

        // 既存のPartialImportを取得または新規作成
        PartialImport partial = partialImports.get(senderId);
        if (partial == null || partial.totalParts != totalParts || partial.isExpired()) {
            partial = new PartialImport(totalParts);
            partialImports.put(senderId, partial);
        }

        // パーツを格納
        int index = currentPart - 1;
        if (partial.parts[index] == null) {
            partial.receivedCount++;
        }
        partial.parts[index] = data;

        int remaining = partial.totalParts - partial.receivedCount;

        if (partial.isComplete()) {
            // 全パーツ揃った → 結合してimport処理に委譲
            String combined = partial.getCombined();
            partialImports.remove(senderId);

            sender.sendMessage("§a全パーツを受信しました。インポートを開始します...");
            return handleImport(sender, new String[]{"import", combined});
        } else {
            sender.sendMessage("§eパート " + currentPart + "/" + totalParts + " を受信しました。残り: §f" + remaining + " パート");
            return true;
        }
    }

    /**
     * Base64デコード
     */
    private String decodeBase64(String encoded) {
        byte[] decoded = Base64.getDecoder().decode(encoded);
        return new String(decoded, StandardCharsets.UTF_8);
    }

    /**
     * 短縮キーを展開（再帰的）
     */
    private JsonObject expandKeys(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            JsonObject result = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                String key = entry.getKey();
                String expandedKey = REVERSE_KEY_MAP.getOrDefault(key, key);
                result.add(expandedKey, expandKeysRecursive(entry.getValue()));
            }
            return result;
        }
        return element.getAsJsonObject();
    }

    private JsonElement expandKeysRecursive(JsonElement element) {
        if (element.isJsonObject()) {
            return expandKeys(element);
        } else if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();
            JsonArray result = new JsonArray();
            for (JsonElement item : arr) {
                result.add(expandKeysRecursive(item));
            }
            return result;
        }
        return element;
    }

    // ========== Gamemode ==========

    private boolean handleGamemode(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /kcdk config gamemode <ADVENTURE|SURVIVAL>");
            return true;
        }

        String gamemode = args[1].toUpperCase();
        if (!Arrays.asList("ADVENTURE", "SURVIVAL").contains(gamemode)) {
            sender.sendMessage("§cInvalid gamemode. Use: ADVENTURE, SURVIVAL");
            return true;
        }

        Store.config.getGameConfig().setGamemode(gamemode);
        sender.sendMessage("§aGamemode set to: §f" + gamemode);
        return true;
    }

    // ========== Bossbar ==========

    private boolean handleBossbar(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /kcdk config bossbar <set|remove> [mcid]");
            return true;
        }

        String action = args[1].toLowerCase();
        if ("set".equals(action)) {
            if (args.length < 3) {
                sender.sendMessage("§cUsage: /kcdk config bossbar set <mcid>");
                return true;
            }
            String mcid = args[2];
            Bossbar bossbar = new Bossbar(mcid);
            Store.config.getGameConfig().setBossbar(bossbar);
            sender.sendMessage("§aBossbar MCID set to: §f" + mcid);
            return true;
        } else if ("remove".equals(action)) {
            Store.config.getGameConfig().setBossbar(null);
            sender.sendMessage("§aBossbar removed");
            return true;
        }

        sender.sendMessage("§cUnknown action: " + action);
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
                sender.sendMessage("§cUsage: /kcdk config timeLimit set <hours> <minutes> <seconds>");
                return true;
            }

            try {
                int hours = Integer.parseInt(args[2]);
                int minutes = Integer.parseInt(args[3]);
                int seconds = Integer.parseInt(args[4]);

                Time timeLimit = new Time(hours, minutes, seconds);
                Store.config.getGameConfig().setTimeLimit(timeLimit);
                sender.sendMessage("§aTime limit set to: §f" + hours + "h " + minutes + "m " + seconds + "s");
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
            newTeam.setArmorColor("#ffffff");
            newTeam.setRespawnCount(-1);

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
                    sender.sendMessage("§7Valid colors: BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE");
                    return true;
                }
                String color = args[0].toUpperCase();
                if (net.kunmc.lab.app.util.ArmorUtil.colorNameToChatColor(color) == org.bukkit.ChatColor.WHITE && !"WHITE".equals(color)) {
                    sender.sendMessage("§cInvalid color: " + args[0]);
                    sender.sendMessage("§7Valid colors: BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE");
                    return true;
                }
                team.setArmorColor(color);
                sender.sendMessage("§aArmor color set to: §f" + color);
                return true;

            case "readylocation":
                if (args.length > 0 && "remove".equals(args[0].toLowerCase())) {
                    team.setReadyLocation(null);
                    sender.sendMessage("§aReady location removed");
                    return true;
                }
                if (args.length < 9) {
                    sender.sendMessage("§cUsage: /kcdk config team <team> readyLocation <world> <x> <y> <z> <yaw> <pitch> <hours> <minutes> <seconds>");
                    return true;
                }
                try {
                    Time waitingTime = new Time(
                            Integer.parseInt(args[6]),
                            Integer.parseInt(args[7]),
                            Integer.parseInt(args[8])
                    );
                    ReadyLocation loc = new ReadyLocation(
                            args[0],
                            Double.parseDouble(args[1]),
                            Double.parseDouble(args[2]),
                            Double.parseDouble(args[3]),
                            Float.parseFloat(args[4]),
                            Float.parseFloat(args[5]),
                            waitingTime
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

            case "respawncount":
                if (args.length < 1) {
                    sender.sendMessage("§cUsage: /kcdk config team <team> respawnCount <count>");
                    return true;
                }
                try {
                    int respawnCount = Integer.parseInt(args[0]);
                    team.setRespawnCount(respawnCount);
                    sender.sendMessage("§aRespawn count set to: §f" + respawnCount);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid number");
                }
                return true;

            case "hasarmor":
                if (args.length < 1) {
                    sender.sendMessage("§cUsage: /kcdk config team <team> hasArmor <true|false>");
                    return true;
                }
                boolean hasArmor = Boolean.parseBoolean(args[0]);
                team.setHasArmor(hasArmor);
                sender.sendMessage("§ahasArmor set to: §f" + hasArmor);
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
                    sender.sendMessage("§7Valid colors: BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE");
                    return true;
                }
                String roleColor = args[0].toUpperCase();
                if (net.kunmc.lab.app.util.ArmorUtil.colorNameToChatColor(roleColor) == org.bukkit.ChatColor.WHITE && !"WHITE".equals(roleColor)) {
                    sender.sendMessage("§cInvalid color: " + args[0]);
                    sender.sendMessage("§7Valid colors: BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE");
                    return true;
                }
                role.setArmorColor(roleColor);
                sender.sendMessage("§aRole armor color set to: §f" + roleColor);
                return true;

            case "readylocation":
                if (args.length > 0 && "remove".equals(args[0].toLowerCase())) {
                    role.setReadyLocation(null);
                    sender.sendMessage("§aRole ready location removed (will inherit from team)");
                    return true;
                }
                if (args.length < 9) {
                    sender.sendMessage("§cUsage: /kcdk config team <team> role <role> readyLocation <world> <x> <y> <z> <yaw> <pitch> <hours> <minutes> <seconds>");
                    return true;
                }
                try {
                    Time waitingTime = new Time(
                            Integer.parseInt(args[6]),
                            Integer.parseInt(args[7]),
                            Integer.parseInt(args[8])
                    );
                    ReadyLocation loc = new ReadyLocation(
                            args[0],
                            Double.parseDouble(args[1]),
                            Double.parseDouble(args[2]),
                            Double.parseDouble(args[3]),
                            Float.parseFloat(args[4]),
                            Float.parseFloat(args[5]),
                            waitingTime
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

            case "respawncount":
                if (args.length > 0 && "remove".equals(args[0].toLowerCase())) {
                    role.setRespawnCount(null);
                    sender.sendMessage("§aRole respawn count removed (will inherit from team)");
                    return true;
                }
                if (args.length < 1) {
                    sender.sendMessage("§cUsage: /kcdk config team <team> role <role> respawnCount <count>");
                    return true;
                }
                try {
                    int respawnCount = Integer.parseInt(args[0]);
                    role.setRespawnCount(respawnCount);
                    sender.sendMessage("§aRole respawn count set to: §f" + respawnCount);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid number");
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

            case "hasarmor":
                if (args.length > 0 && "remove".equals(args[0].toLowerCase())) {
                    role.setHasArmor(null);
                    sender.sendMessage("§aRole hasArmor removed (will inherit from team)");
                    return true;
                }
                if (args.length < 1) {
                    sender.sendMessage("§cUsage: /kcdk config team <team> role <role> hasArmor <true|false|remove>");
                    return true;
                }
                boolean roleHasArmor = Boolean.parseBoolean(args[0]);
                role.setHasArmor(roleHasArmor);
                sender.sendMessage("§aRole hasArmor set to: §f" + roleHasArmor);
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
                sender.sendMessage("§cUsage: ... effect add <effectName> <seconds> <amplifier> <hideParticles>");
                return true;
            }

            String effectName = args[1].toUpperCase();
            PotionEffectType type = PotionEffectType.getByName(effectName);
            if (type == null) {
                sender.sendMessage("§cInvalid effect name: " + effectName);
                return true;
            }

            try {
                int seconds = Integer.parseInt(args[2]);
                int amplifier = Integer.parseInt(args[3]);
                boolean hideParticles = Boolean.parseBoolean(args[4]);

                Effect effect = new Effect(effectName, seconds, amplifier, hideParticles);
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
                    "gamemode", "bossbar", "timeLimit", "team", "endCondition", "show", "save", "reload", "import", "import-part"
            ));
        }

        String subCommand = args[0].toLowerCase();

        if ("gamemode".equals(subCommand) && args.length == 2) {
            return KCDKCommand.filterStartingWith(args[1], Arrays.asList("ADVENTURE", "SURVIVAL"));
        }

        if ("bossbar".equals(subCommand) && args.length == 2) {
            return KCDKCommand.filterStartingWith(args[1], Arrays.asList("set", "remove"));
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
                    "respawnCount", "hasArmor", "effect", "role"
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

            if ("hasarmor".equals(property) && args.length == 4) {
                return KCDKCommand.filterStartingWith(args[3], Arrays.asList("true", "false"));
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
                    "respawnCount", "hasArmor", "extendsEffects", "extendsItem", "effect"
            ));
        }

        if (args.length >= 3) {
            String property = args[1].toLowerCase();

            if ("effect".equals(property)) {
                return tabCompleteEffect(Arrays.copyOfRange(args, 2, args.length));
            }

            if (args.length == 3 && Arrays.asList("displayname", "armorcolor", "readylocation",
                    "respawncount").contains(property)) {
                return KCDKCommand.filterStartingWith(args[2], Collections.singletonList("remove"));
            }

            if (("readylocation".equals(property) || "respawnlocation".equals(property)) && args.length == 3) {
                List<String> options = new ArrayList<>(Collections.singletonList("remove"));
                options.addAll(Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()));
                return KCDKCommand.filterStartingWith(args[2], options);
            }

            if (("extendseffects".equals(property) || "extendsitem".equals(property) || "hasarmor".equals(property)) && args.length == 3) {
                List<String> boolOpts = new ArrayList<>(Arrays.asList("true", "false"));
                if ("hasarmor".equals(property)) boolOpts.add("remove");
                return KCDKCommand.filterStartingWith(args[2], boolOpts);
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
            return KCDKCommand.filterStartingWith(args[2], Arrays.asList("beacon", "extermination", "ticket"));
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

    // ========== Scoreboard Team Creation ==========

    private void createScoreboardTeams(GameConfig config) {
        if (Bukkit.getScoreboardManager() == null) return;
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        for (Team team : config.getTeams()) {
            String sbTeamName = "kcdk." + team.getName();
            org.bukkit.scoreboard.Team sbTeam = scoreboard.getTeam(sbTeamName);
            if (sbTeam == null) {
                sbTeam = scoreboard.registerNewTeam(sbTeamName);
            }
            // チームカラー設定
            if (team.getArmorColor() != null) {
                sbTeam.setColor(net.kunmc.lab.app.util.ArmorUtil.colorNameToChatColor(team.getArmorColor()));
            }

            for (Role role : team.getRoles()) {
                String sbRoleName = "kcdk." + team.getName() + "." + role.getName();
                org.bukkit.scoreboard.Team sbRole = scoreboard.getTeam(sbRoleName);
                if (sbRole == null) {
                    sbRole = scoreboard.registerNewTeam(sbRoleName);
                }
                // ロールカラー設定（未設定ならチームのカラーを継承）
                String colorName = role.getArmorColor() != null ? role.getArmorColor() : team.getArmorColor();
                if (colorName != null) {
                    sbRole.setColor(net.kunmc.lab.app.util.ArmorUtil.colorNameToChatColor(colorName));
                }
            }
        }
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
