# KCDK (Kojosen Command Development Kit) 仕様書

## 概要

KCDKは、攻城戦（攻守に分かれて戦うゲーム）を簡単に作成する目的で作られたMinecraftデータパックのコマンドセットです。本ドキュメントは、Spigot Pluginへのリプレースを目的とした詳細仕様書です。

### バージョン情報
- **対象バージョン**: KCDK 2.x (データパック版)
- **サポートMinecraftバージョン**: 1.13 - 1.16
- **pack_format**: 5
- **著作権**: © 2019-2022 Nishitox

---

## 1. アーキテクチャ概要

### 1.1 基本構造

KCDKは以下の主要コンポーネントで構成されています:

- **コア機能** (`data/kcdk/functions/`)
  - スコアボード管理
  - チーム管理
  - タイマー/時間管理
  - メッセージシステム
  - プレイヤー管理
  - ボスバー表示

- **拡張機能** (`data/kcdkp/functions/`)
  - bm (Block Modification): ブロック変更システム
  - igui (Inventory GUI): インベントリGUI
  - ke (Kill Effect): キルエフェクト
  - sv (Survival): サバイバル機能
  - dmn (Domination): 陣地支配モード
  - join: 参加管理

### 1.2 命名規則

#### ファイル名
- スネークケース使用（例: `hoge_piyo.mcfunction`）

#### スコアボード/タグ/ボスバー名
- プレフィックス: `kcdk.` (基本機能)
- プレフィックス: `kcdkp.` (拡張機能)
- 形式: `kcdk.<任意の名前>` (例: `kcdk.control`)

---

## 2. スコアボードシステム

### 2.1 スコアボードオブジェクト一覧

| オブジェクト名 | タイプ | 用途 | Spigot実装方法 |
|--------------|--------|------|---------------|
| `kcdk.button` | trigger | プレイヤー操作トリガー（0-29番予約済み） | `Player.setScoreboard()` + カスタムトリガーイベント |
| `kcdk.config` | dummy | 設定値保存 | YAML設定ファイル or データベース |
| `kcdk.control` | dummy | ゲーム制御変数 | メモリ内変数（HashMap） |
| `kcdk.death` | deathCount | 死亡回数カウント | `PlayerDeathEvent`リスナー |
| `kcdk.players` | dummy | チーム別プレイヤー数 | チームマネージャークラス |
| `kcdk.role` | dummy | プレイヤーロール | プレイヤーメタデータ |
| `health` | health | 体力表示（belowName） | スコアボード体力表示 |

### 2.2 制御変数 (`kcdk.control`)

| 変数名 | 初期値 | 用途 | Spigot実装 |
|--------|--------|------|-----------|
| `arg` | 0 | 関数引数（同一tick内で使用） | メソッド引数 |
| `phase` | 0 | ゲームフェーズ（0=待機, 1=進行中, 2=終了） | GameStateクラス（enum） |
| `return` | 0 | 関数戻り値（同一tick内で使用） | メソッド戻り値 |
| `teams` | 0 | 有効チームビットフラグ（例: 1111=全チーム） | ビットフラグ or Set<Team> |
| `tmp` | 0 | 一時変数（同一tick内で使用） | ローカル変数 |
| `time_m` | 0 | 経過時間（分） | long型タイムスタンプ |
| `time_s` | 0 | 経過時間（秒） | long型タイムスタンプ |
| `time_t` | 0 | 経過時間（tick, 0-19） | BukkitScheduler |
| `timer_m` | 0 | 残り時間（分） | long型タイムスタンプ |
| `timer_s` | 0 | 残り時間（秒） | long型タイムスタンプ |
| `timer_t` | 0 | 残り時間（tick, 0-19） | BukkitScheduler |
| `version` | 0 | Minecraftバージョン（13-16） | `Bukkit.getVersion()` |
| `victory` | 0 | 勝利チームID | GameStateクラス |

### 2.3 プレイヤー数管理 (`kcdk.players`)

| 変数名 | 用途 | 更新タイミング |
|--------|------|--------------|
| `everyone` | 全プレイヤー数 | 毎tick |
| `blue` | 青チームプレイヤー数 | チーム変更時 + 毎tick |
| `green` | 緑チームプレイヤー数 | チーム変更時 + 毎tick |
| `red` | 赤チームプレイヤー数 | チーム変更時 + 毎tick |
| `yellow` | 黄チームプレイヤー数 | チーム変更時 + 毎tick |

**Spigot実装方法:**
```java
public class TeamManager {
    private Map<Team, Set<UUID>> teamPlayers = new HashMap<>();

    public int getPlayerCount(Team team) {
        return teamPlayers.getOrDefault(team, Collections.emptySet()).size();
    }

    public int getTotalPlayerCount() {
        return Bukkit.getOnlinePlayers().size();
    }
}
```

---

## 3. チームシステム

### 3.1 チーム定義

| チーム名 | カラー | 皮装備色（RGB） | 設定 |
|---------|-------|----------------|------|
| `blue` | blue | 255 (0x0000FF) | friendlyFire: false, seeFriendlyInvisibles: true |
| `green` | green | 32768 (0x008000) | friendlyFire: false, seeFriendlyInvisibles: true |
| `red` | red | 16711680 (0xFF0000) | friendlyFire: false, seeFriendlyInvisibles: true |
| `yellow` | yellow | 16776960 (0xFFFF00) | friendlyFire: false, seeFriendlyInvisibles: true |
| `admin` | white | - | 管理者用 |

### 3.2 チーム設定詳細

各チーム共通設定:
```mcfunction
team modify <team> collisionRule pushOwnTeam
team modify <team> color <color>
team modify <team> deathMessageVisibility never
team modify <team> friendlyFire false
team modify <team> nametagVisibility always
team modify <team> seeFriendlyInvisibles true
team modify <team> displayName "<team>"
team modify <team> prefix ""
team modify <team> suffix ""
```

**Spigot実装:**
```java
public void setupTeam(Team team, ChatColor color, int armorColor) {
    team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OWN_TEAM);
    team.setColor(color);
    team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.NEVER);
    team.setAllowFriendlyFire(false);
    team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
    team.setCanSeeFriendlyInvisibles(true);
    team.setDisplayName(team.getName());
    team.setPrefix("");
    team.setSuffix("");
}
```

### 3.3 チーム識別ビットフラグ

`teams` スコアは4ビットで各チームの有効/無効を表現:

| ビット位置 | チーム | 値 |
|-----------|--------|---|
| bit 3 | blue | 1000 (8) |
| bit 2 | green | 0100 (4) |
| bit 1 | red | 0010 (2) |
| bit 0 | yellow | 0001 (1) |

**例:**
- `1111` (15): 全チーム有効
- `1100` (12): 青・緑のみ
- `0011` (3): 赤・黄のみ

**Spigot実装:**
```java
public enum TeamFlag {
    BLUE(8), GREEN(4), RED(2), YELLOW(1);

    private final int bit;
    TeamFlag(int bit) { this.bit = bit; }

    public boolean isEnabled(int teams) {
        return (teams & bit) != 0;
    }
}
```

### 3.4 チーム自動割り当てアルゴリズム

`kcdk:team/assign/assign.mcfunction`の処理フロー:

1. **1チームモードの場合**: 全員を該当チームに割り当て
2. **複数チームモードの場合**:
   - 各プレイヤーを最も人数の少ないチームに順次割り当て
   - 人数が同じ場合はランダム選択
   - チーム未所属プレイヤー (`@a[team=]`) がいなくなるまで繰り返し

**Spigot実装案:**
```java
public void assignPlayersToTeams(Set<Team> enabledTeams) {
    List<Player> unassigned = getUnassignedPlayers();

    while (!unassigned.isEmpty()) {
        Team smallestTeam = findSmallestTeam(enabledTeams);
        Player randomPlayer = unassigned.remove(random.nextInt(unassigned.size()));
        smallestTeam.addPlayer(randomPlayer);
    }
}
```

---

## 4. タイマーシステム

### 4.1 経過時間 (time)

**仕組み:**
- `time_t`: 0-19 (tick単位、20tick = 1秒)
- `time_s`: 0-59 (秒)
- `time_m`: 0-60+ (分)

**更新ロジック** (`kcdk:score/time.mcfunction`):
```mcfunction
# tickが20に達したら秒に変換
execute if score time_t matches 20 run scoreboard players add time_s 1
execute if score time_t matches 20 run scoreboard players set time_t 0

# 秒が60に達したら分に変換
execute if score time_s matches 60 run scoreboard players set time_s 0
execute if score time_s matches 0 if score time_t matches 20 run scoreboard players add time_m 1

# 毎tick増加
execute run scoreboard players add time_t 1
```

**Spigot実装:**
```java
public class GameTimer {
    private long startTick;

    public void start() {
        startTick = System.currentTimeMillis();
    }

    public int getElapsedMinutes() {
        return (int) ((System.currentTimeMillis() - startTick) / 60000);
    }

    public int getElapsedSeconds() {
        return (int) (((System.currentTimeMillis() - startTick) / 1000) % 60);
    }

    public int getElapsedTicks() {
        return (int) (((System.currentTimeMillis() - startTick) / 50) % 20);
    }
}
```

### 4.2 カウントダウンタイマー (timer)

**仕組み:**
- `timer_t`: 0-20 (tick単位、逆カウント)
- `timer_s`: 0-60 (秒、逆カウント)
- `timer_m`: 0-59+ (分、逆カウント)

**更新ロジック** (`kcdk:score/timer.mcfunction`):
```mcfunction
# tick減算
execute if score timer_t matches 1..20 run scoreboard players remove timer_t 1

# tick=0かつ秒>0の場合、秒を減らしてtickリセット
execute if score timer_s matches 01..60 if score timer_t matches 0 run scoreboard players remove timer_s 1
execute if score timer_s matches 01..59 if score timer_t matches 0 run scoreboard players set timer_t 20

# 秒=0かつ分>0の場合、分を減らして秒リセット
execute if score timer_m matches 1..59 if score timer_s matches 00 if score timer_t matches 0 run scoreboard players set timer_s 60
execute if score timer_m matches 1..59 if score timer_s matches 60 if score timer_t matches 0 run scoreboard players remove timer_m 1
```

**Spigot実装:**
```java
public class CountdownTimer {
    private long endTick;

    public void start(int minutes, int seconds) {
        long durationMs = (minutes * 60 + seconds) * 1000;
        endTick = System.currentTimeMillis() + durationMs;
    }

    public int getRemainingMinutes() {
        long remaining = Math.max(0, endTick - System.currentTimeMillis());
        return (int) (remaining / 60000);
    }

    public int getRemainingSeconds() {
        long remaining = Math.max(0, endTick - System.currentTimeMillis());
        return (int) ((remaining / 1000) % 60);
    }

    public boolean isFinished() {
        return System.currentTimeMillis() >= endTick;
    }
}
```

---

## 5. ゲームルール設定

### 5.1 初期設定 (ゲーム用)

`kcdk:common/initial_settings/gamerules.mcfunction`:

| ゲームルール | 値 | 理由 |
|------------|----|----|
| `commandBlockOutput` | false | コマンド出力を非表示 |
| `doDaylightCycle` | false | 時間固定 |
| `doFireTick` | false | 火の延焼防止 |
| `doMobSpawning` | false | Mob自然スポーン無効 |
| `doWeatherCycle` | false | 天候固定 |
| `keepInventory` | true | 死亡時インベントリ保持 |
| `maxEntityCramming` | 0 | 窒息ダメージ無効 |
| `mobGriefing` | false | Mobによる破壊無効 |
| `spawnRadius` | 0 | スポーン地点固定 |

### 5.2 デフォルト設定（通常ワールド用）

`kcdk:common/default_settings/gamerules.mcfunction`:

全てMinecraftデフォルト値に戻す（詳細は省略、全27項目）

### 5.3 バージョン別追加設定

- **1.14以降**: `gamerules_1.14.mcfunction`
  - `disableRaids`: true
  - `doInsomnia`: false
  - `doImmediateRespawn`: false
  - `drowningDamage`: true
  - `fallDamage`: true
  - `fireDamage`: true

- **1.15以降**: `gamerules_1.15.mcfunction`
  - `doPatrolSpawning`: false
  - `doTraderSpawning`: false

- **1.16以降**: `gamerules_1.16.mcfunction`
  - `forgiveDeadPlayers`: true
  - `universalAnger`: false

**Spigot実装:**
```java
public void applyGameRules(World world, boolean forGame) {
    if (forGame) {
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.KEEP_INVENTORY, true);
        world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 0);
        world.setGameRule(GameRule.MOB_GRIEFING, false);
        world.setGameRule(GameRule.SPAWN_RADIUS, 0);
    } else {
        // デフォルト値に戻す
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        // ... (以下略)
    }
}
```

---

## 6. メッセージシステム

### 6.1 メッセージ種別

| 種別 | 実装場所 | 用途 |
|------|---------|------|
| **actionbar** | `message/actionbar/` | ゲーム中常時表示情報 |
| **subtitle** | `message/subtitle/` | 重要イベント通知（サブタイトル） |
| **title** | `message/title/` | 重要イベント通知（タイトル） |
| **tellraw** | `message/tellraw/` | チャット通知（announce/tell/error） |

### 6.2 Actionbar表示パターン

3つの表示モード:
1. **low_info**: 低情報モード（プレイヤー数のみ）
2. **time**: 経過時間表示
3. **timer**: カウントダウン表示

**チーム数別ファイル構成:**
- `no_team`: チーム未選択時
- `1_team/`: 1チーム有効時（4パターン: 0001, 0010, 0100, 1000）
- `2_teams/`: 2チーム有効時（6パターン: 0011, 0101, 0110, 1001, 1010, 1100）
- `3_teams/`: 3チーム有効時（4パターン: 0111, 1011, 1101, 1110）
- `4_teams/`: 4チーム有効時（1パターン: 1111）

**例** (`low_info/1_team/0001.mcfunction`):
```mcfunction
title @a actionbar ["",{"score":{"name":"everyone","objective":"kcdk.players"}},{"text":"人が参加中 黄色チーム:"},{"score":{"name":"yellow","objective":"kcdk.players"}}]
```

**Spigot実装:**
```java
public void sendActionBar(Player player, String message) {
    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
        new TextComponent(message));
}

public String buildTeamInfoMessage(int teams) {
    StringBuilder sb = new StringBuilder();
    sb.append(getTotalPlayerCount()).append("人が参加中");

    if ((teams & 8) != 0) sb.append(" 青:").append(getTeamCount(Team.BLUE));
    if ((teams & 4) != 0) sb.append(" 緑:").append(getTeamCount(Team.GREEN));
    if ((teams & 2) != 0) sb.append(" 赤:").append(getTeamCount(Team.RED));
    if ((teams & 1) != 0) sb.append(" 黄:").append(getTeamCount(Team.YELLOW));

    return sb.toString();
}
```

### 6.3 Tellrawメッセージ

#### Announceメッセージ（全体通知）
- `elapsed_time`: 経過時間通知
- `setup_executed`: セットアップ完了
- `setup_with_formatting_executed`: フォーマット付きセットアップ完了
- `team_assigned`: チーム自動割り当て完了
- `team_changed`: チーム変更通知
- `team_emptied`: チーム空き通知
- `time_left`: 残り時間通知
- `victory`: 勝利通知

#### Tellメッセージ（個人通知）
- `changed_to_team`: チーム変更完了
- `joined_to_team`: チーム参加完了

#### Errorメッセージ
- `failed_to_change_teams`: チーム変更失敗
- `no_team_selected`: チーム未選択エラー
- `player_does_not_exist`: プレイヤー不在エラー

---

## 7. プレイヤー管理

### 7.1 プレイヤー機能

| 機能 | ファイル | 処理内容 |
|------|---------|---------|
| ゲーム開始 | `player/game_start.mcfunction` | サウンド再生 + サブタイトル表示 |
| デプロイ設定 | `player/set_deploy.mcfunction` | スポーン地点設定 + テレポート |
| 皮装備付与 | `player/set_leather_chestplate.mcfunction` | チーム色皮装備付与（呪い付き） |
| 皮装備付与(非エンチャ) | `player/set_leather_chestplate_no_enchant.mcfunction` | チーム色皮装備付与（エンチャなし） |
| 観戦モード | `player/spectate.mcfunction` | スペクテイターモード設定 |
| 勝利処理 | `player/victory.mcfunction` | 勝利時演出 |

### 7.2 皮装備付与詳細

`player/set_leather_chestplate.mcfunction`:
```mcfunction
replaceitem entity @a[team=blue] armor.chest minecraft:leather_chestplate{
    Unbreakable:true,
    Enchantments:[{id:"minecraft:binding_curse",lvl:1s}],
    display:{color:255},
    HideFlags:7
}
```

**NBTタグ詳細:**
- `Unbreakable:true`: 耐久値無限
- `Enchantments:[{id:"minecraft:binding_curse",lvl:1s}]`: 束縛の呪い（脱げない）
- `display:{color:<RGB>}`: チーム色
- `HideFlags:7`: エンチャント・耐久値等非表示

**Spigot実装:**
```java
public ItemStack createTeamChestplate(Team team, boolean withCurse) {
    ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
    LeatherArmorMeta meta = (LeatherArmorMeta) chestplate.getItemMeta();

    meta.setColor(Color.fromRGB(team.getArmorColor()));
    meta.setUnbreakable(true);

    if (withCurse) {
        meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
    }

    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS,
                      ItemFlag.HIDE_UNBREAKABLE,
                      ItemFlag.HIDE_ATTRIBUTES);

    chestplate.setItemMeta(meta);
    return chestplate;
}

public void equipTeamArmor(Player player, Team team) {
    player.getInventory().setChestplate(createTeamChestplate(team, true));
}
```

### 7.3 デプロイ（スポーン地点）設定

`player/set_deploy.mcfunction`:
```mcfunction
spawnpoint @s ~ ~ ~
tp @s ~ ~ ~
```

**処理内容:**
1. 実行位置をプレイヤーのスポーン地点に設定
2. プレイヤーを実行位置にテレポート

**Spigot実装:**
```java
public void setPlayerDeploy(Player player, Location location) {
    player.setBedSpawnLocation(location, true);
    player.teleport(location);
}
```

---

## 8. ボスバーシステム

### 8.1 ボスバー定義

`bossbar/add/general.mcfunction`:
```mcfunction
bossbar add kcdk:general "general"
bossbar set kcdk:general color white
bossbar set kcdk:general max 20
bossbar set kcdk:general name "general"
bossbar set kcdk:general players
bossbar set kcdk:general style progress
bossbar set kcdk:general value 0
bossbar set kcdk:general visible true
```

**プロパティ:**
- ID: `kcdk:general`
- カラー: white
- 最大値: 20
- スタイル: progress
- 初期値: 0
- 可視性: true
- 対象プレイヤー: 空（後から設定）

**Spigot実装:**
```java
public BossBar createGameBossBar(String title) {
    BossBar bossBar = Bukkit.createBossBar(
        title,
        BarColor.WHITE,
        BarStyle.SOLID
    );
    bossBar.setProgress(0.0);
    bossBar.setVisible(true);
    return bossBar;
}

public void updateBossBar(BossBar bossBar, int value, int max) {
    bossBar.setProgress((double) value / max);
}
```

---

## 9. バージョン検出システム

### 9.1 検出方法

`score/detect_version.mcfunction`:
```mcfunction
scoreboard players set version kcdk.control 0
summon minecraft:item ~ ~ ~ {Tags:["kcdk.version"],Item:{Count:1b,id:"minecraft:blue_ice",tag:{display:{Name:'{"text":"kcdk.version.13"}'}}}}
summon minecraft:item ~ ~ ~ {Tags:["kcdk.version"],Item:{Count:1b,id:"minecraft:bamboo",tag:{display:{Name:'{"text":"kcdk.version.14"}'}}}}
summon minecraft:item ~ ~ ~ {Tags:["kcdk.version"],Item:{Count:1b,id:"minecraft:beehive",tag:{display:{Name:'{"text":"kcdk.version.15"}'}}}}
summon minecraft:item ~ ~ ~ {Tags:["kcdk.version"],Item:{Count:1b,id:"minecraft:basalt",tag:{display:{Name:'{"text":"kcdk.version.16"}'}}}}

execute if entity @e[type=minecraft:item,nbt={Item:{tag:{display:{Name:'{"text":"kcdk.version.13"}'}}}}] run scoreboard players set version kcdk.control 13
execute if entity @e[type=minecraft:item,nbt={Item:{tag:{display:{Name:'{"text":"kcdk.version.14"}'}}}}] run scoreboard players set version kcdk.control 14
execute if entity @e[type=minecraft:item,nbt={Item:{tag:{display:{Name:'{"text":"kcdk.version.15"}'}}}}] run scoreboard players set version kcdk.control 15
execute if entity @e[type=minecraft:item,nbt={Item:{tag:{display:{Name:'{"text":"kcdk.version.16"}'}}}}] run scoreboard players set version kcdk.control 16
kill @e[type=minecraft:item,tag=kcdk.version]
```

**仕組み:**
- 各バージョン固有のアイテムをsummonして存在確認
- 成功したバージョン番号をスコアに格納
- 検出後アイテムをkill

**バージョン判定アイテム:**
| バージョン | アイテム | 理由 |
|-----------|---------|------|
| 1.13 | blue_ice | 1.13で追加 |
| 1.14 | bamboo | 1.14で追加 |
| 1.15 | beehive | 1.15で追加 |
| 1.16 | basalt | 1.16で追加 |

**Spigot実装:**
```java
public int detectMinecraftVersion() {
    String version = Bukkit.getVersion();

    if (version.contains("1.16")) return 16;
    if (version.contains("1.15")) return 15;
    if (version.contains("1.14")) return 14;
    if (version.contains("1.13")) return 13;

    return 0; // 未対応
}
```

---

## 10. セットアップシステム

### 10.1 基本セットアップ

`_setup.mcfunction`:
```mcfunction
function kcdk:common/initial_settings/scoreboard
function kcdk:score/detect_version
function kcdk:common/initial_settings/gamerules
function kcdk:bossbar/add/general
function kcdk:team/add/batch
function kcdk:ptp/setup
function kcdk:message/tellraw/announce/setup_executed
```

**処理フロー:**
1. スコアボード初期化
2. Minecraftバージョン検出
3. ゲームルール適用
4. ボスバー作成
5. 全チーム作成
6. プラグイン(ptp)セットアップ
7. 完了メッセージ

**Spigot実装:**
```java
public void setupGame(World world) {
    // 1. スコアボード初期化
    scoreboardManager.initialize();

    // 2. バージョン検出
    int version = detectMinecraftVersion();

    // 3. ゲームルール適用
    applyGameRules(world, true);

    // 4. ボスバー作成
    BossBar bossBar = createGameBossBar("ゲーム進行中");

    // 5. チーム作成
    teamManager.createAllTeams();

    // 6. プラグイン初期化
    pluginManager.initialize();

    // 7. 完了通知
    Bukkit.broadcastMessage("§a[KCDK] セットアップが完了しました");
}
```

### 10.2 フォーマット付きセットアップ

`_setup_with_formatting.mcfunction`:
- 基本セットアップに加えて追加のフォーマット処理を実行
- 具体的な処理内容は拡張機能依存

---

## 11. PTP (Plugin to Plugin) システム

### 11.1 常時実行関数

`ptp/always.mcfunction` (tick.jsonから毎tick実行):
```mcfunction
function kcdkp:igui/gate
function kcdkp:join/gate
function kcdkp:ke/gate_2
```

**呼び出される拡張機能:**
- `igui`: Inventory GUI処理
- `join`: 参加管理処理
- `ke`: キルエフェクト処理

### 11.2 セットアップフック

`ptp/setup.mcfunction`:
```mcfunction
function kcdkp:igui/setup
function kcdkp:join/setup
function kcdkp:ke/setup
```

セットアップ時に各拡張機能の初期化を実行

**Spigot実装:**
拡張機能をインターフェース化:
```java
public interface KCDKPlugin {
    void onSetup();
    void onTick();
    void onGameStart();
    void onGameEnd();
}

public class PluginManager {
    private List<KCDKPlugin> plugins = new ArrayList<>();

    public void registerPlugin(KCDKPlugin plugin) {
        plugins.add(plugin);
    }

    public void setupAll() {
        plugins.forEach(KCDKPlugin::onSetup);
    }

    public void tickAll() {
        plugins.forEach(KCDKPlugin::onTick);
    }
}
```

---

## 12. 拡張機能概要

### 12.1 BM (Block Modification)

**機能:**
- `deploy`: ブロック配置システム
- `down`: ブロック下降システム
- `up`: ブロック上昇システム
- `offdef`: 攻守ブロック変更
- `opt`: 最適化処理

**主要用途:**
- 試合開始時のブロック配置
- 動的なマップ変更
- 攻守交代時のギミック

### 12.2 IGUI (Inventory GUI)

**機能:**
- インベントリベースのGUIシステム
- アイテム取得管理

### 12.3 KE (Kill Effect)

**機能:**
- キル時のエフェクト表示
- 連続キル表示

### 12.4 JOIN (Join Management)

**機能:**
- プレイヤー参加時の処理
- チーム自動割り当て

### 12.5 SV (Survival)

**機能:**
- サバイバル関連機能
- （詳細は拡張機能ドキュメント参照）

### 12.6 DMN (Domination)

**機能:**
- 陣地支配ゲームモード
- フラグ（A/B/C）管理
- チーム別占領状態管理
- ボスバーによる占領状況表示

**主要コンポーネント:**
- フラグ制御システム
- 占領判定システム
- スコア管理

---

## 13. Spigot Plugin実装時の推奨設計

### 13.1 パッケージ構成

```
com.kcdk.plugin/
├── core/
│   ├── KCDKPlugin.java (メインクラス)
│   ├── GameState.java (ゲーム状態管理)
│   └── ConfigManager.java (設定管理)
├── team/
│   ├── Team.java (チームenum)
│   ├── TeamManager.java (チーム管理)
│   └── TeamColor.java (色定義)
├── scoreboard/
│   ├── ScoreboardManager.java
│   └── ObjectiveType.java
├── timer/
│   ├── GameTimer.java (経過時間)
│   └── CountdownTimer.java (カウントダウン)
├── player/
│   ├── PlayerManager.java
│   └── PlayerData.java
├── message/
│   ├── MessageManager.java
│   └── MessageType.java
├── bossbar/
│   └── BossBarManager.java
├── gamerule/
│   └── GameRuleManager.java
└── extension/
    ├── ExtensionInterface.java
    ├── BlockModification.java
    ├── InventoryGUI.java
    └── KillEffect.java
```

### 13.2 主要クラス設計例

#### GameState.java
```java
public enum GamePhase {
    WAITING(0),
    RUNNING(1),
    ENDED(2);

    private final int id;
    GamePhase(int id) { this.id = id; }
    public int getId() { return id; }
}

public class GameState {
    private GamePhase phase = GamePhase.WAITING;
    private int enabledTeams = 0; // ビットフラグ
    private int victoryTeam = 0;

    // ゲッター/セッター
}
```

#### TeamManager.java
```java
public class TeamManager {
    private final Map<Team, org.bukkit.scoreboard.Team> teams = new HashMap<>();
    private final Map<Team, Set<UUID>> teamPlayers = new HashMap<>();

    public void createTeam(Team team, ChatColor color, int armorColor);
    public void assignPlayersToTeams(int enabledTeams);
    public int getPlayerCount(Team team);
    public void equipTeamArmor(Player player, Team team);
}
```

### 13.3 設定ファイル (config.yml)

```yaml
# KCDK Plugin Configuration

# ゲーム設定
game:
  # 有効チーム (blue, green, red, yellow)
  enabled-teams:
    - blue
    - green

  # 自動チーム割り当て
  auto-assign: true

  # タイマー設定（分）
  timer:
    enabled: true
    duration: 10

  # 皮装備自動付与
  auto-equip-armor: true

# チーム色設定
teams:
  blue:
    color: '0x0000FF'
    display-name: '青チーム'
  green:
    color: '0x008000'
    display-name: '緑チーム'
  red:
    color: '0xFF0000'
    display-name: '赤チーム'
  yellow:
    color: '0xFFFF00'
    display-name: '黄チーム'

# メッセージ設定
messages:
  prefix: '§7[§bKCDK§7]§r '
  game-start: 'ゲームを開始します！'
  game-end: 'ゲーム終了！'
  team-assigned: 'チームに割り当てられました: {team}'

# ゲームルール自動適用
auto-apply-gamerules: true

# デバッグモード
debug: false
```

### 13.4 コマンド設計

| コマンド | 説明 | 権限 |
|---------|------|------|
| `/kcdk setup` | ゲームセットアップ | `kcdk.admin` |
| `/kcdk start` | ゲーム開始 | `kcdk.admin` |
| `/kcdk stop` | ゲーム停止 | `kcdk.admin` |
| `/kcdk team <team>` | チーム参加 | `kcdk.player` |
| `/kcdk assign` | 自動チーム割り当て | `kcdk.admin` |
| `/kcdk timer <minutes>` | タイマー設定 | `kcdk.admin` |
| `/kcdk reload` | 設定リロード | `kcdk.admin` |

### 13.5 イベントリスナー

```java
public class GameEventListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // チーム自動割り当て処理
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // デスカウント更新
        // キルエフェクト発動
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // デプロイ地点にリスポーン
        // チーム装備再付与
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // チームプレイヤー数更新
    }
}
```

### 13.6 タスクスケジューラー

```java
public class GameTask extends BukkitRunnable {

    @Override
    public void run() {
        // 毎tick実行（20回/秒）

        // 1. プレイヤー数更新
        teamManager.updatePlayerCounts();

        // 2. タイマー更新
        if (gameTimer.isRunning()) {
            gameTimer.tick();
        }

        // 3. Actionbar更新
        messageManager.updateActionBar();

        // 4. 拡張機能tick処理
        pluginManager.tickAll();

        // 5. 勝利条件チェック
        checkVictoryCondition();
    }
}

// 起動
new GameTask().runTaskTimer(plugin, 0L, 1L); // 1tick = 0.05秒
```

---

## 14. データパックからの移行対応表

| データパック要素 | Spigot実装 | 備考 |
|----------------|-----------|------|
| scoreboard objectives | HashMap / PlayerData | メモリ内管理 |
| team | Bukkit Scoreboard API | 完全互換 |
| bossbar | BukkitBossBar API | 完全互換 |
| title/subtitle | Player.sendTitle() | 完全互換 |
| tellraw | Player.sendMessage() | JSON解析が必要 |
| execute if score | Java条件分岐 | if文で実装 |
| function呼び出し | メソッド呼び出し | 直接呼び出し |
| tick.json | BukkitRunnable | runTaskTimer使用 |
| gamerule | World.setGameRule() | 完全互換 |
| replaceitem | Player.getInventory() | 完全互換 |
| tag | PersistentDataContainer | NBT代替 |

---

## 15. 重要な実装ポイント

### 15.1 汎用変数の扱い

データパックの `arg`, `return`, `tmp` 変数は同一tick内でのみ使用される前提:

**Spigot実装:**
- メソッドの引数・戻り値として直接実装
- tick跨ぎが発生しないため、スレッドセーフティ考慮不要

### 15.2 チームビットフラグ

4ビットフラグ（0-15）で4チームの有効/無効を管理:

```java
public boolean isTeamEnabled(int teams, Team team) {
    return (teams & team.getBit()) != 0;
}

public int enableTeam(int teams, Team team) {
    return teams | team.getBit();
}

public int disableTeam(int teams, Team team) {
    return teams & ~team.getBit();
}
```

### 15.3 パフォーマンス最適化

- Actionbarは毎tick更新されるため、文字列生成を最適化
- プレイヤー数カウントはキャッシュして必要時のみ更新
- 拡張機能のtick処理は軽量に保つ

---

## 16. まとめ

KCDKは攻城戦ゲームに特化した包括的なシステムであり、Spigot Pluginへの移行には以下の要素が必要:

### 必須機能
1. ✅ チーム管理（4チーム、自動割り当て）
2. ✅ スコアボードシステム（プレイヤー数、制御変数）
3. ✅ タイマーシステム（経過時間、カウントダウン）
4. ✅ メッセージシステム（Actionbar、Title、Chat）
5. ✅ ゲームルール自動設定
6. ✅ プレイヤー装備管理（チーム色皮装備）
7. ✅ ボスバー表示
8. ✅ バージョン検出

### 推奨実装順序
1. コアシステム（チーム、スコアボード、ゲーム状態）
2. タイマー・メッセージシステム
3. プレイヤー管理・装備システム
4. ゲームルール・セットアップ
5. 拡張機能インターフェース
6. 個別拡張機能の実装

### 拡張性確保
- プラグインインターフェースによる拡張機能の追加
- 設定ファイルによるカスタマイズ
- イベント駆動アーキテクチャ

---

**以上、KCDK仕様書 - Spigot Plugin実装ガイド**
