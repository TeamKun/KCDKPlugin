package net.kunmc.lab.app.game;

import net.kunmc.lab.app.Store;
import net.kunmc.lab.app.config.data.*;
import net.kunmc.lab.app.config.data.endcondition.*;
import net.kunmc.lab.app.game.condition.*;
import net.kunmc.lab.app.util.ArmorUtil;
import net.kunmc.lab.app.util.EffectUtil;
import net.kunmc.lab.app.util.ScoreboardUtil;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class GameManager {
    private GameState state = GameState.IDLE;
    private final Map<UUID, PlayerData> players = new HashMap<>();
    private final Map<String, TeamData> teams = new HashMap<>();
    private final Map<GameLocation, Integer> beaconHPs = new HashMap<>();
    private final List<ConditionChecker> conditionCheckers = new ArrayList<>();
    private BossBar bossBar;
    private long startTimeTick;
    private GameTimer timer;
    private org.bukkit.scheduler.BukkitRunnable waitingTimer;

    public GameState getState() {
        return state;
    }

    public Map<UUID, PlayerData> getPlayers() {
        return players;
    }

    public Map<String, TeamData> getTeams() {
        return teams;
    }

    public Map<GameLocation, Integer> getBeaconHPs() {
        return beaconHPs;
    }

    public List<ConditionChecker> getConditionCheckers() {
        return conditionCheckers;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public long getStartTimeTick() {
        return startTimeTick;
    }

    public void start(org.bukkit.command.CommandSender sender) {
        if (state != GameState.IDLE) {
            sender.sendMessage("§cゲームは既に開始されています。");
            return;
        }

        GameConfig config = Store.config.getGameConfig();
        if (config.getTeams().isEmpty()) {
            sender.sendMessage("§cチームが設定されていません。");
            return;
        }

        state = GameState.STARTING;
        players.clear();
        teams.clear();
        beaconHPs.clear();
        conditionCheckers.clear();

        // チームデータ初期化
        for (Team team : config.getTeams()) {
            TeamData td = new TeamData(team.getName(), 0);
            teams.put(team.getName(), td);
        }

        // 終了条件チェッカー構築
        buildConditionCheckers(config.getEndConditions());

        // ビーコンHP初期化
        for (EndCondition ec : config.getEndConditions()) {
            initBeaconHPs(ec);
        }

        // チケット初期化
        for (EndCondition ec : config.getEndConditions()) {
            initTickets(ec);
        }

        // プレイヤーをチームに割り当て（Scoreboardチームから取得）
        Scoreboard scoreboard = ScoreboardUtil.getMainScoreboard();
        if (scoreboard != null) {
            for (Team team : config.getTeams()) {
                TeamData td = teams.get(team.getName());

                // チーム設定（カラー・FF・衝突）
                org.bukkit.scoreboard.Team sbTeam = scoreboard.getTeam("kcdk." + team.getName());
                if (sbTeam != null) {
                    if (team.getArmorColor() != null) {
                        sbTeam.setColor(ArmorUtil.colorNameToChatColor(team.getArmorColor()));
                    }
                    sbTeam.setAllowFriendlyFire(false);
                    sbTeam.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.FOR_OTHER_TEAMS);
                }

                // チーム直属メンバー
                if (sbTeam != null) {
                    for (String entry : sbTeam.getEntries()) {
                        Player player = Bukkit.getPlayerExact(entry);
                        if (player != null) {
                            int respawns = team.getRespawnCount();
                            PlayerData pd = new PlayerData(player.getUniqueId(), team.getName(), null, respawns);
                            players.put(player.getUniqueId(), pd);
                            td.addPlayer(player.getUniqueId());
                        }
                    }
                }

                // ロールメンバー
                for (Role role : team.getRoles()) {
                    org.bukkit.scoreboard.Team sbRole = scoreboard.getTeam("kcdk." + team.getName() + "." + role.getName());
                    if (sbRole != null) {
                        // ロール設定（カラー・FF・衝突）— 同一チーム扱い
                        String roleColor = role.getArmorColor() != null ? role.getArmorColor() : team.getArmorColor();
                        if (roleColor != null) {
                            sbRole.setColor(ArmorUtil.colorNameToChatColor(roleColor));
                        }
                        sbRole.setAllowFriendlyFire(false);
                        sbRole.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.FOR_OTHER_TEAMS);
                        for (String entry : sbRole.getEntries()) {
                            Player player = Bukkit.getPlayerExact(entry);
                            if (player != null) {
                                int respawns = role.getRespawnCount() != null ? role.getRespawnCount() : team.getRespawnCount();
                                PlayerData pd = new PlayerData(player.getUniqueId(), team.getName(), role.getName(), respawns);
                                players.put(player.getUniqueId(), pd);
                                td.addPlayer(player.getUniqueId());
                            }
                        }
                    }
                }
            }
        }

        if (players.isEmpty()) {
            sender.sendMessage("§cScoreboardチームにプレイヤーが見つかりません。");
            state = GameState.IDLE;
            return;
        }

        // 待機地点へテレポート（スペクテイターモードで待機）
        boolean hasReadyLocations = false;
        for (Team team : config.getTeams()) {
            for (Map.Entry<UUID, PlayerData> entry : players.entrySet()) {
                PlayerData pd = entry.getValue();
                if (!pd.getTeamName().equals(team.getName())) continue;

                Player player = Bukkit.getPlayer(pd.getUuid());
                if (player == null) continue;

                // ロールごとの待機地点を優先
                ReadyLocation readyLoc = null;
                if (pd.getRoleName() != null) {
                    Role role = findRole(team, pd.getRoleName());
                    if (role != null && role.getReadyLocation() != null) {
                        readyLoc = role.getReadyLocation();
                    }
                }
                if (readyLoc == null && team.getReadyLocation() != null) {
                    readyLoc = team.getReadyLocation();
                }

                if (readyLoc != null) {
                    hasReadyLocations = true;
                    player.teleport(readyLoc.toBukkitLocation());
                    player.setGameMode(GameMode.SPECTATOR);
                }
            }
        }

        // スタートアップコマンド実行
        for (String cmd : config.getStartupCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        if (!hasReadyLocations) {
            // 待機地点がなければ即RUNNING
            beginRunning(config);
        } else {
            // チーム(＋ロール)ごとに個別の待機時間でプレイヤーを初期化
            GameMode gameMode = "ADVENTURE".equalsIgnoreCase(config.getGamemode()) ? GameMode.ADVENTURE : GameMode.SURVIVAL;

            for (Team team : config.getTeams()) {
                // チーム直属プレイヤーの待機時間
                int teamWaitTicks = 60; // デフォルト3秒
                if (team.getWaitingTime() != null) {
                    teamWaitTicks = team.getWaitingTime().getTotalSeconds() * 20;
                }

                // チーム直属メンバー（ロールなし）をスケジュール
                final int teamTicks = teamWaitTicks;
                scheduleTeamInit(config, team, null, teamTicks, gameMode);

                // ロールごとにスケジュール
                for (Role role : team.getRoles()) {
                    int roleTicks = teamTicks;
                    if (role.getReadyLocation() != null && role.getReadyLocation().getWaitingTime() != null) {
                        roleTicks = role.getReadyLocation().getWaitingTime().getTotalSeconds() * 20;
                    }
                    scheduleTeamInit(config, team, role, roleTicks, gameMode);
                }
            }

            // 待機中アクションバーカウントダウン
            int maxWaitTicks = getMaxWaitingTimeTicks(config);
            final int totalWaitTicks = maxWaitTicks;
            waitingTimer = new org.bukkit.scheduler.BukkitRunnable() {
                private int elapsed = 0;
                @Override
                public void run() {
                    if (state != GameState.STARTING) {
                        cancel();
                        return;
                    }
                    int remainingTicks = totalWaitTicks - elapsed;
                    if (remainingTicks < 0) remainingTicks = 0;
                    long remainSecs = remainingTicks / 20;
                    long m = remainSecs / 60;
                    long s = remainSecs % 60;
                    String text = String.format("§e試合開始まで: §f%02d:%02d", m, s);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.spigot().sendMessage(
                                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(text)
                        );
                    }
                    elapsed++;
                }
            };
            waitingTimer.runTaskTimer(Store.plugin, 0L, 1L);

            // 最大待機時間後にstate=RUNNING、タイマー開始
            Bukkit.getScheduler().runTaskLater(Store.plugin, () -> {
                if (state == GameState.STARTING) {
                    // 待機タイマー停止
                    if (waitingTimer != null) {
                        waitingTimer.cancel();
                        waitingTimer = null;
                    }

                    state = GameState.RUNNING;
                    startTimeTick = Bukkit.getWorlds().get(0).getFullTime();

                    // BossBar設定
                    if (config.getBossbar() != null && config.getBossbar().getMcid() != null) {
                        Player bossbarTarget = Bukkit.getPlayerExact(config.getBossbar().getMcid());
                        if (bossbarTarget != null) {
                            bossBar = Bukkit.createBossBar(
                                    bossbarTarget.getName(),
                                    org.bukkit.boss.BarColor.RED,
                                    org.bukkit.boss.BarStyle.SOLID
                            );
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                bossBar.addPlayer(p);
                            }
                        }
                    }

                    // タイマー開始
                    timer = new GameTimer(this);
                    timer.runTaskTimer(Store.plugin, 0L, 1L);

                    Bukkit.broadcastMessage("§a§l試合開始！");
                }
            }, maxWaitTicks);
        }

        Bukkit.broadcastMessage("§a§lゲームを開始します！");
        broadcastEndConditions(config);
    }

    private void beginRunning(GameConfig config) {
        state = GameState.RUNNING;
        startTimeTick = Bukkit.getWorlds().get(0).getFullTime();

        // ゲームモード設定
        GameMode gameMode = "ADVENTURE".equalsIgnoreCase(config.getGamemode()) ? GameMode.ADVENTURE : GameMode.SURVIVAL;

        // BossBar設定
        if (config.getBossbar() != null && config.getBossbar().getMcid() != null) {
            Player bossbarTarget = Bukkit.getPlayerExact(config.getBossbar().getMcid());
            if (bossbarTarget != null) {
                bossBar = Bukkit.createBossBar(
                        bossbarTarget.getName(),
                        BarColor.RED,
                        BarStyle.SOLID
                );
                for (Player p : Bukkit.getOnlinePlayers()) {
                    bossBar.addPlayer(p);
                }
            }
        }

        // プレイヤーにリスポーン地点TP、装備、エフェクト付与
        for (Map.Entry<UUID, PlayerData> entry : players.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) continue;

            PlayerData pd = entry.getValue();
            Team team = findTeamConfig(pd.getTeamName());
            if (team == null) continue;

            Role role = pd.getRoleName() != null ? findRole(team, pd.getRoleName()) : null;

            // TP
            GameLocation respawnLoc = (role != null && role.getRespawnLocation() != null) ? role.getRespawnLocation() : team.getRespawnLocation();
            if (respawnLoc != null) {
                player.teleport(respawnLoc.toBukkitLocation());
            }

            // ゲームモード
            player.setGameMode(gameMode);

            // インベントリクリア+装備
            player.getInventory().clear();
            equipPlayer(player, team, role);

            // エフェクト
            List<PotionEffect> effects = EffectUtil.resolveEffects(team, role);
            EffectUtil.applyEffects(player, effects);

            // タブ名設定
            ScoreboardUtil.setPlayerTabName(player, player.getName());
        }

        // タイマー開始
        timer = new GameTimer(this);
        timer.runTaskTimer(Store.plugin, 0L, 1L);

        Bukkit.broadcastMessage("§a§l試合開始！");
    }

    public void stop(String message) {
        if (state == GameState.IDLE) return;

        state = GameState.ENDING;

        // タイマー停止
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (waitingTimer != null) {
            waitingTimer.cancel();
            waitingTimer = null;
        }

        // BossBar削除
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }

        // シャットダウンコマンド
        GameConfig config = Store.config.getGameConfig();
        for (String cmd : config.getShutdownCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        // タイトル表示
        if (message != null && !message.isEmpty()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle(message, "", 10, 70, 20);
            }
        }

        // プレイヤーリセット＋K/D表示
        for (Map.Entry<UUID, PlayerData> entry : players.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                player.setGameMode(GameMode.SURVIVAL);
                player.getInventory().clear();
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }
                PlayerData pd = entry.getValue();
                ScoreboardUtil.setPlayerTabName(player, player.getName() + " k:" + pd.getKills() + "/d:" + pd.getDeaths());
            }
        }

        players.clear();
        teams.clear();
        beaconHPs.clear();
        conditionCheckers.clear();

        state = GameState.IDLE;
        Bukkit.broadcastMessage("§c§lゲームが終了しました。");
    }

    public void handleDeath(Player victim, Player killer) {
        if (state != GameState.RUNNING) return;

        PlayerData victimData = players.get(victim.getUniqueId());
        if (victimData == null) return;

        victimData.addDeath();

        // キル記録
        if (killer != null) {
            PlayerData killerData = players.get(killer.getUniqueId());
            if (killerData != null) {
                killerData.addKill();
                killer.sendTitle("§c§l" + killerData.getKills() + " Kill", "§f" + victim.getName(), 0, 10, 10);
            }
        }

        // リスポーン消費
        victimData.consumeRespawn();

        // チケット消費
        TeamData td = teams.get(victimData.getTeamName());
        if (td != null) {
            td.consumeTicket();
        }

        // リスポーン残なし→スペクテイター
        if (!victimData.hasRespawnsLeft()) {
            victimData.setAlive(false);
        }
    }

    public void handleRespawn(Player player) {
        if (state != GameState.RUNNING) return;

        PlayerData pd = players.get(player.getUniqueId());
        if (pd == null) return;

        if (!pd.isAlive()) {
            // スペクテイターモードへ
            Bukkit.getScheduler().runTaskLater(Store.plugin, () -> {
                player.setGameMode(GameMode.SPECTATOR);
            }, 1L);
            return;
        }

        Team team = findTeamConfig(pd.getTeamName());
        if (team == null) return;
        Role role = pd.getRoleName() != null ? findRole(team, pd.getRoleName()) : null;

        // リスポーン地点
        GameLocation respawnLoc = (role != null && role.getRespawnLocation() != null) ? role.getRespawnLocation() : team.getRespawnLocation();
        if (respawnLoc != null) {
            player.teleport(respawnLoc.toBukkitLocation());
        }

        // 1tick遅延で装備復元
        GameConfig config = Store.config.getGameConfig();
        GameMode gameMode = "ADVENTURE".equalsIgnoreCase(config.getGamemode()) ? GameMode.ADVENTURE : GameMode.SURVIVAL;
        Bukkit.getScheduler().runTaskLater(Store.plugin, () -> {
            player.getInventory().clear();
            player.setGameMode(gameMode);
            equipPlayer(player, team, role);
            List<PotionEffect> effects = EffectUtil.resolveEffects(team, role);
            EffectUtil.applyEffects(player, effects);
        }, 1L);
    }

    public void handleJoin(Player player) {
        if (state != GameState.RUNNING && state != GameState.STARTING) return;

        PlayerData pd = players.get(player.getUniqueId());
        if (pd == null) return;

        pd.setOnline(true);

        Team team = findTeamConfig(pd.getTeamName());
        if (team == null) return;
        Role role = pd.getRoleName() != null ? findRole(team, pd.getRoleName()) : null;

        if (!pd.isAlive()) {
            player.setGameMode(GameMode.SPECTATOR);
            return;
        }

        // 状態復元
        GameConfig config = Store.config.getGameConfig();
        GameMode gameMode = "ADVENTURE".equalsIgnoreCase(config.getGamemode()) ? GameMode.ADVENTURE : GameMode.SURVIVAL;

        GameLocation respawnLoc = (role != null && role.getRespawnLocation() != null) ? role.getRespawnLocation() : team.getRespawnLocation();
        if (respawnLoc != null) {
            player.teleport(respawnLoc.toBukkitLocation());
        }

        player.setGameMode(gameMode);
        player.getInventory().clear();
        equipPlayer(player, team, role);
        List<PotionEffect> effects = EffectUtil.resolveEffects(team, role);
        EffectUtil.applyEffects(player, effects);

        ScoreboardUtil.setPlayerTabName(player, player.getName());

        if (bossBar != null) {
            bossBar.addPlayer(player);
        }
    }

    public void handleQuit(Player player) {
        if (state != GameState.RUNNING) return;

        PlayerData pd = players.get(player.getUniqueId());
        if (pd == null) return;

        pd.setOnline(false);

        // 退出=死亡扱い
        pd.addDeath();
        pd.consumeRespawn();

        TeamData td = teams.get(pd.getTeamName());
        if (td != null) {
            td.consumeTicket();
        }

        if (!pd.hasRespawnsLeft()) {
            pd.setAlive(false);
        }
    }

    public void handleBeaconBreak(GameLocation location) {
        Integer hp = beaconHPs.get(location);
        if (hp == null) return;

        hp--;
        beaconHPs.put(location, hp);

        // ビーコンブロックを再設置
        Location bukkit = location.toBukkitLocation();
        bukkit.getBlock().setType(Material.BEACON);
    }

    public void equipPlayer(Player player, Team team, Role role) {
        // hasArmor判定：ロール→チーム
        boolean shouldEquipArmor;
        if (role != null && role.getHasArmor() != null) {
            shouldEquipArmor = role.getHasArmor();
        } else {
            shouldEquipArmor = team.isHasArmor();
        }

        if (shouldEquipArmor) {
            // アーマーカラー解決：ロール→チーム
            int colorInt;
            if (role != null && role.getArmorColorAsInt() != null) {
                colorInt = role.getArmorColorAsInt();
            } else {
                colorInt = team.getArmorColorAsInt();
            }
            player.getInventory().setChestplate(ArmorUtil.createColoredChestplate(colorInt));
        }
    }

    private void scheduleTeamInit(GameConfig config, Team team, Role role, int delayTicks, GameMode gameMode) {
        Bukkit.getScheduler().runTaskLater(Store.plugin, () -> {
            if (state != GameState.STARTING) return;
            for (Map.Entry<UUID, PlayerData> entry : players.entrySet()) {
                PlayerData pd = entry.getValue();
                if (!pd.getTeamName().equals(team.getName())) continue;

                // ロールフィルタ: roleがnullならロールなしプレイヤー、非nullなら一致するロールのみ
                if (role == null) {
                    if (pd.getRoleName() != null) continue;
                } else {
                    if (!role.getName().equals(pd.getRoleName())) continue;
                }

                Player player = Bukkit.getPlayer(pd.getUuid());
                if (player == null) continue;

                // リスポーン地点TP
                GameLocation respawnLoc = (role != null && role.getRespawnLocation() != null) ? role.getRespawnLocation() : team.getRespawnLocation();
                if (respawnLoc != null) {
                    player.teleport(respawnLoc.toBukkitLocation());
                }

                // ゲームモード
                player.setGameMode(gameMode);

                // インベントリクリア+装備
                player.getInventory().clear();
                equipPlayer(player, team, role);

                // エフェクト
                List<PotionEffect> effects = EffectUtil.resolveEffects(team, role);
                EffectUtil.applyEffects(player, effects);

                // タブ名設定
                ScoreboardUtil.setPlayerTabName(player, player.getName());
            }
        }, delayTicks);
    }

    // ========== Private helpers ==========

    private void broadcastEndConditions(GameConfig config) {
        List<EndCondition> conditions = config.getEndConditions();
        if (conditions.isEmpty()) return;

        Bukkit.broadcastMessage("§e§l=== 終了条件 ===");
        for (EndCondition ec : conditions) {
            broadcastCondition(ec, 0);
        }
        Time timeLimit = config.getTimeLimit();
        if (timeLimit != null) {
            long total = timeLimit.getTotalSeconds();
            long h = total / 3600;
            long m = (total % 3600) / 60;
            long s = total % 60;
            Bukkit.broadcastMessage("§e制限時間: §f" + String.format("%02d:%02d:%02d", h, m, s));
        }
    }

    private void broadcastCondition(EndCondition ec, int depth) {
        String indent = depth > 0 ? "  ".repeat(depth) : "";
        if (ec instanceof ExterminationCondition) {
            ExterminationCondition ext = (ExterminationCondition) ec;
            Bukkit.broadcastMessage("§7" + indent + "- §f" + ext.getTeam() + " §7の全滅");
        } else if (ec instanceof TicketCondition) {
            TicketCondition tc = (TicketCondition) ec;
            Bukkit.broadcastMessage("§7" + indent + "- §f" + tc.getTeam() + " §7のチケット(残" + tc.getCount() + ")消費");
        } else if (ec instanceof BeaconCondition) {
            BeaconCondition bc = (BeaconCondition) ec;
            GameLocation loc = bc.getLocation();
            Bukkit.broadcastMessage("§7" + indent + "- §fビーコン§7(HP:" + bc.getHitpoint() + ") §7の破壊");
        } else if (ec instanceof CompositeCondition) {
            CompositeCondition cc = (CompositeCondition) ec;
            String op = "AND".equalsIgnoreCase(cc.getOperator()) ? "すべて満たす" : "いずれか満たす";
            Bukkit.broadcastMessage("§7" + indent + "- §e" + op + ":");
            for (EndCondition child : cc.getConditions()) {
                broadcastCondition(child, depth + 1);
            }
        }
    }

    private void buildConditionCheckers(List<EndCondition> endConditions) {
        for (EndCondition ec : endConditions) {
            ConditionChecker checker = buildChecker(ec);
            if (checker != null) {
                conditionCheckers.add(checker);
            }
        }
    }

    private ConditionChecker buildChecker(EndCondition ec) {
        if (ec instanceof ExterminationCondition) {
            ExterminationCondition ext = (ExterminationCondition) ec;
            return new ExterminationChecker(ext.getTeam(), ext.getMessage());
        } else if (ec instanceof TicketCondition) {
            TicketCondition tc = (TicketCondition) ec;
            return new TicketChecker(tc.getTeam(), tc.getMessage());
        } else if (ec instanceof BeaconCondition) {
            BeaconCondition bc = (BeaconCondition) ec;
            return new BeaconChecker(bc.getLocation(), bc.getMessage());
        } else if (ec instanceof CompositeCondition) {
            CompositeCondition cc = (CompositeCondition) ec;
            List<ConditionChecker> children = new ArrayList<>();
            for (EndCondition child : cc.getConditions()) {
                ConditionChecker childChecker = buildChecker(child);
                if (childChecker != null) {
                    children.add(childChecker);
                }
            }
            return new CompositeChecker(children, cc.getMessage());
        }
        return null;
    }

    private void initBeaconHPs(EndCondition ec) {
        if (ec instanceof BeaconCondition) {
            BeaconCondition bc = (BeaconCondition) ec;
            beaconHPs.put(bc.getLocation(), bc.getHitpoint());
        } else if (ec instanceof CompositeCondition) {
            for (EndCondition child : ((CompositeCondition) ec).getConditions()) {
                initBeaconHPs(child);
            }
        }
    }

    private void initTickets(EndCondition ec) {
        if (ec instanceof TicketCondition) {
            TicketCondition tc = (TicketCondition) ec;
            TeamData td = teams.get(tc.getTeam());
            if (td != null) {
                td.setTicketCount(tc.getCount());
            }
        } else if (ec instanceof CompositeCondition) {
            for (EndCondition child : ((CompositeCondition) ec).getConditions()) {
                initTickets(child);
            }
        }
    }

    private int getMaxWaitingTimeTicks(GameConfig config) {
        int maxSeconds = 0;
        for (Team team : config.getTeams()) {
            Time wt = team.getWaitingTime();
            if (wt != null) {
                maxSeconds = Math.max(maxSeconds, wt.getTotalSeconds());
            }
            for (Role role : team.getRoles()) {
                if (role.getReadyLocation() != null && role.getReadyLocation().getWaitingTime() != null) {
                    maxSeconds = Math.max(maxSeconds, role.getReadyLocation().getWaitingTime().getTotalSeconds());
                }
            }
        }
        return maxSeconds > 0 ? maxSeconds * 20 : 60; // デフォルト3秒
    }

    private Team findTeamConfig(String teamName) {
        GameConfig config = Store.config.getGameConfig();
        for (Team team : config.getTeams()) {
            if (team.getName().equals(teamName)) {
                return team;
            }
        }
        return null;
    }

    private Role findRole(Team team, String roleName) {
        for (Role role : team.getRoles()) {
            if (role.getName().equals(roleName)) {
                return role;
            }
        }
        return null;
    }

    public boolean isBeaconLocation(GameLocation loc) {
        return beaconHPs.containsKey(loc);
    }
}
