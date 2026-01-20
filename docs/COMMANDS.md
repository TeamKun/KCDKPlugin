# KCDK Plugin - コマンド仕様書

## 概要

本ドキュメントは、KCDKをSpigot Pluginとして実装する際のコマンド仕様を定義します。
**oldkcdkのデータパックで実際に存在する機能のみ**をコマンド化しています。

oldkcdkはfunctionを手動実行する方式のため、コマンド体系は存在しませんでした。
Spigot Plugin化に際して、これらのfunctionをコマンドとして再設計します。

---

## 前提: Minecraftの標準teamコマンドを使用

以下のチーム操作は**Minecraft標準のteamコマンド**で実現可能なため、プラグイン独自のコマンドは作成しません:

- `/team join <team> [player]` - チーム参加
- `/team leave [player]` - チーム離脱
- `/team empty <team>` - チーム全員削除
- `/team list [team]` - チーム一覧・メンバー表示
- `/team add <team>` - チーム作成
- `/team remove <team>` - チーム削除
- `/team modify <team> <option> <value>` - チーム設定変更

KCDKプラグインはこれらの標準コマンドを前提とし、ゲーム制御機能のみを提供します。

---

## コマンド体系

### 基本構造
```
/kcdk <サブコマンド> [引数...]
```

### 権限体系
- `kcdk.admin` - 管理者権限（全コマンド使用可能）
- `kcdk.use` - 基本使用権限（情報表示のみ）

---

## 1. セットアップコマンド

### 1.1 `/kcdk setup`
**説明:** ゲームの初期セットアップを実行

**対応function:** `kcdk:_setup`

**権限:** `kcdk.admin`

**構文:**
```
/kcdk setup
```

**処理内容:**
1. スコアボードオブジェクトの作成・初期化
   - kcdk.button, kcdk.config, kcdk.control, kcdk.death, kcdk.players, kcdk.role, health
2. Minecraftバージョンの検出（1.13-1.16）
3. ゲームルールの適用（doDaylightCycle=false等）
4. ボスバー `kcdk:general` の作成
5. チーム（blue, green, red, yellow, admin）の作成と設定
6. 拡張機能の初期化（ptp/setup）
7. 完了メッセージの表示

**実装メソッド:**
```java
public void executeSetup(CommandSender sender)
```

**成功時メッセージ:**
```
kcdk:_setupが実行されました
```

---

### 1.2 `/kcdk setup-format`
**説明:** フォーマット付きセットアップを実行

**対応function:** `kcdk:_setup_with_formatting`

**権限:** `kcdk.admin`

**構文:**
```
/kcdk setup-format
```

**処理内容:**
setupに加えて:
1. デフォルト設定の復元
   - difficulty normal
   - time set day
   - weather clear
   - worldborder設定
   - setworldspawn 0 4 0
2. ゲームルールをデフォルトに戻す
3. その後、ゲーム用設定を再適用

**実装メソッド:**
```java
public void executeSetupWithFormatting(CommandSender sender)
```

---

### 1.3 `/kcdk get`
**説明:** KCDK構造物ブロックを取得

**対応function:** `kcdk:_get`

**権限:** `kcdk.admin`

**構文:**
```
/kcdk get
```

**処理内容:**
実行者に構造物ブロック（structure block）を付与
- 名前: "KCDK 2.2.0"
- NBT: `{BlockEntityTag:{name:"kcdk:2.2.0",posX:0,posY:1,posZ:0,sizeX:27,sizeY:13,sizeZ:7,mode:"LOAD"}}`

**実装メソッド:**
```java
public void executeGet(Player player)
```

---

## 2. ゲーム制御コマンド

### 2.1 `/kcdk reset`
**説明:** ゲーム状態をリセット

**対応function:** `kcdk:score/reset_control`

**権限:** `kcdk.admin`

**構文:**
```
/kcdk reset
```

**処理内容:**
以下のkcdk.controlスコアを0にリセット:
- arg, phase, return, tmp
- time_m, time_s, time_t
- timer_m, timer_s, timer_t
- victory

**実装メソッド:**
```java
public void executeReset(CommandSender sender)
```

**引数型:**
なし

---

### 2.2 `/kcdk exit`
**説明:** ゲーム終了処理

**対応function:** `kcdk:common/exit`

**権限:** `kcdk.admin`

**構文:**
```
/kcdk exit
```

**処理内容:**
exitファイル自体が再帰呼び出しのため、実際には何も実行しない
（データパック設計上の空関数）

**実装メソッド:**
```java
public void executeExit(CommandSender sender)
```

---

## 3. チーム割り当てコマンド

### 3.1 `/kcdk team assign`
**説明:** 全プレイヤーをチームに自動割り当て

**対応function:** `kcdk:team/assign/gate_1` → `gate_2` → `assign`

**権限:** `kcdk.admin`

**構文:**
```
/kcdk team assign
```

**前提条件:**
- 有効チーム（`teams` スコア）が1つ以上選択されていること
- 選択がない場合はエラーメッセージ表示

**処理内容:**
1. 有効チーム数をチェック（`score/return/number_of_selected_teams`）
2. チーム未所属プレイヤー（`@a[team=]`）を検索
3. 最も人数の少ないチームに順次割り当て
4. 人数が同数の場合はランダム
5. 完了メッセージと各プレイヤーへの参加通知

**実装メソッド:**
```java
public void executeTeamAssign(CommandSender sender)
```

**メッセージ:**
- 成功: `message/tellraw/announce/team_assigned`
- 個人通知: `message/tellraw/tell/joined_to_team`
- サブタイトル: `message/subtitle/joined_to_team`
- タイトル: `message/title/number_of_team_players`

---

### 3.2 `/kcdk team change`
**説明:** プレイヤーのチームを変更（移動）

**対応function:** `kcdk:team/change/gate_1` → `gate_2` → `change`

**権限:** `kcdk.admin`

**構文:**
```
/kcdk team change <player> <team>
```

**引数:**
- `<player>` - 対象プレイヤー名
- `<team>` - 移動先チーム（blue/green/red/yellow）

**前提条件:**
- 有効チームが選択されていること
- 対象プレイヤーが存在すること（`score/return/player_exists/search`で確認）
- 移動先チームが有効化されていること

**処理内容:**
1. 有効チーム数チェック
2. プレイヤー存在確認
3. チーム移動処理（`team/change/<pattern>.mcfunction`で分岐）
4. 完了メッセージ

**実装メソッド:**
```java
public void executeTeamChange(CommandSender sender, Player target, Team team)
```

**引数型:**
- `target`: `Player`
- `team`: `Team` (enum: BLUE, GREEN, RED, YELLOW)

**メッセージ:**
- 成功: `message/tellraw/announce/team_changed`
- 個人通知: `message/tellraw/tell/changed_to_team`
- サブタイトル: `message/subtitle/changed_to_team`
- エラー: `message/tellraw/error/no_team_selected` / `player_does_not_exist` / `failed_to_change_teams`

---

### 3.3 `/kcdk team empty`
**説明:** 指定チームを空にする

**対応function:** `kcdk:team/empty/empty`

**権限:** `kcdk.admin`

**構文:**
```
/kcdk team empty <team>
```

**引数:**
- `<team>` - 対象チーム（blue/green/red/yellow）

**処理内容:**
1. 指定チームのメンバー全員を離脱（標準の`team leave`相当）
2. 完了メッセージ表示

**実装メソッド:**
```java
public void executeTeamEmpty(CommandSender sender, Team team)
```

**引数型:**
- `team`: `Team` (enum)

**メッセージ:**
- 成功: `message/tellraw/announce/team_emptied`

**補足:**
標準の `/team empty <team>` で代替可能だが、KCDKのメッセージ表示のために独自実装

---

## 4. プレイヤー操作コマンド

### 4.1 `/kcdk player deploy`
**説明:** プレイヤーのデプロイ地点を現在位置に設定

**対応function:** `kcdk:player/set_deploy`

**権限:** `kcdk.admin`

**構文:**
```
/kcdk player deploy
```

**処理内容:**
1. 実行者（プレイヤー）の現在位置をスポーン地点に設定
2. 同時にテレポート（座標の確認用）

```mcfunction
spawnpoint @s ~ ~ ~
tp @s ~ ~ ~
```

**実装メソッド:**
```java
public void executePlayerDeploy(Player player)
```

---

### 4.2 `/kcdk player equip`
**説明:** チーム色の皮装備を付与

**対応function:** `kcdk:player/set_leather_chestplate`

**権限:** `kcdk.admin`

**構文:**
```
/kcdk player equip [--no-curse]
```

**オプション:**
- `--no-curse` - 束縛の呪いなしで付与（`set_leather_chestplate_no_enchant`）

**処理内容:**
各チームのプレイヤーに革の胸当てを付与:
- 耐久無限（Unbreakable:true）
- 束縛の呪い（デフォルト）
- チーム色（display:{color:<RGB>}）
  - 青: 255 (0x0000FF)
  - 緑: 32768 (0x008000)
  - 赤: 16711680 (0xFF0000)
  - 黄: 16776960 (0xFFFF00)
- エンチャント等を非表示（HideFlags:7）

**実装メソッド:**
```java
public void executePlayerEquip(CommandSender sender, boolean noCurse)
```

**引数型:**
- `noCurse`: `boolean` (default: false)

---

### 4.3 `/kcdk player spectate`
**説明:** プレイヤーを観戦モードに設定

**対応function:** `kcdk:player/spectate`

**権限:** `kcdk.admin`

**構文:**
```
/kcdk player spectate <player>
```

**処理内容:**
指定プレイヤーをスペクテイターモードに変更

**実装メソッド:**
```java
public void executePlayerSpectate(CommandSender sender, Player target)
```

**引数型:**
- `target`: `Player`

---

### 4.4 `/kcdk player start`
**説明:** ゲーム開始演出を実行

**対応function:** `kcdk:player/game_start`

**権限:** `kcdk.admin`

**構文:**
```
/kcdk player start
```

**処理内容:**
1. 全プレイヤーにサウンド再生（`minecraft:block.anvil.place`、pitch=1.7）
2. サブタイトル表示（`message/subtitle/game_start`）

**実装メソッド:**
```java
public void executePlayerStart(CommandSender sender)
```

---

### 4.5 `/kcdk player victory`
**説明:** 勝利演出を実行

**対応function:** `kcdk:player/victory`

**権限:** `kcdk.admin`

**構文:**
```
/kcdk player victory [team]
```

**引数:**
- `[team]` - 勝利チーム（省略時=引き分け）

**処理内容:**
1. 勝利タイトル表示（`message/title/victory`）
2. 勝利通知（`message/tellraw/announce/victory`）

**実装メソッド:**
```java
public void executePlayerVictory(CommandSender sender, Team team)
```

**引数型:**
- `team`: `Team` (nullable)

---

## 5. 情報表示コマンド

### 5.1 `/kcdk info`
**説明:** 現在のゲーム状態を表示

**対応function:** なし（新規機能）

**権限:** `kcdk.use`

**構文:**
```
/kcdk info
```

**出力内容:**
- ゲームフェーズ（phase）
- 有効チーム（teamsビットフラグ）
- 各チームプレイヤー数
- 経過時間（time_m:time_s）
- 残り時間（timer_m:timer_s）
- 勝利チーム（victory）

**実装メソッド:**
```java
public void executeInfo(CommandSender sender)
```

---

### 5.2 `/kcdk version`
**説明:** プラグインバージョンとMinecraftバージョンを表示

**対応function:** なし（新規機能）

**権限:** `kcdk.use`

**構文:**
```
/kcdk version
```

**出力例:**
```
KCDK Plugin v2.2.0
Minecraft Version: 1.16.5 (Detected: 16)
```

**実装メソッド:**
```java
public void executeVersion(CommandSender sender)
```

---

## 6. スコアボード操作コマンド

### 6.1 `/kcdk score set`
**説明:** kcdk.controlスコアを設定

**対応function:** なし（scoreboardコマンドの簡易ラッパー）

**権限:** `kcdk.admin`

**構文:**
```
/kcdk score set <変数> <値>
```

**引数:**
- `<変数>` - phase/teams/time_m/time_s/timer_m/timer_s/victory
- `<値>` - 整数値

**例:**
```
/kcdk score set phase 1
/kcdk score set teams 1100
/kcdk score set timer_m 10
```

**実装メソッド:**
```java
public void executeScoreSet(CommandSender sender, String variable, int value)
```

**引数型:**
- `variable`: `String`
- `value`: `int`

**補足:**
標準の `/scoreboard players set <target> <objective> <score>` で代替可能だが、
KCDKの変数名に特化した簡易版として提供

---

### 6.2 `/kcdk score get`
**説明:** kcdk.controlスコアを取得

**対応function:** なし

**権限:** `kcdk.use`

**構文:**
```
/kcdk score get <変数>
```

**実装メソッド:**
```java
public void executeScoreGet(CommandSender sender, String variable)
```

---

## 7. タイマー制御コマンド

### 7.1 `/kcdk timer set`
**説明:** カウントダウンタイマーを設定

**対応function:** なし（スコアボード操作）

**権限:** `kcdk.admin`

**構文:**
```
/kcdk timer set <分> [秒]
```

**引数:**
- `<分>` - 分（0-99）
- `[秒]` - 秒（0-59、省略時0）

**処理内容:**
`timer_m` と `timer_s` スコアを設定

**実装メソッド:**
```java
public void executeTimerSet(CommandSender sender, int minutes, int seconds)
```

**引数型:**
- `minutes`: `int` (0-99)
- `seconds`: `int` (0-59, default: 0)

---

### 7.2 `/kcdk time`
**説明:** 経過時間を表示

**対応function:** なし（スコア表示）

**権限:** `kcdk.use`

**構文:**
```
/kcdk time
```

**出力例:**
```
経過時間: 03:45
```

**実装メソッド:**
```java
public void executeTime(CommandSender sender)
```

---

## コマンド引数型まとめ

| 型名 | Java型 | 説明 |
|-----|--------|------|
| `<player>` | `Player` | オンラインプレイヤー |
| `<team>` | `Team` (enum) | BLUE/GREEN/RED/YELLOW |
| `<変数>` | `String` | kcdk.control変数名 |
| `<値>` | `int` | 整数値 |
| `<分>` | `int` | 整数（分） |
| `<秒>` | `int` | 整数（秒） |
| `[オプション]` | nullable | 省略可能な引数 |
| `--flag` | `boolean` | フラグオプション |

---

## タブ補完仕様

| コマンド | 補完対象 |
|---------|---------|
| `/kcdk <TAB>` | setup, setup-format, get, reset, exit, team, player, info, version, score, timer, time |
| `/kcdk team <TAB>` | assign, change, empty |
| `/kcdk team change <TAB>` | オンラインプレイヤー一覧 |
| `/kcdk team change <player> <TAB>` | blue, green, red, yellow |
| `/kcdk team empty <TAB>` | blue, green, red, yellow |
| `/kcdk player <TAB>` | deploy, equip, spectate, start, victory |
| `/kcdk player spectate <TAB>` | オンラインプレイヤー一覧 |
| `/kcdk player victory <TAB>` | blue, green, red, yellow |
| `/kcdk score <TAB>` | set, get |
| `/kcdk score set <TAB>` | phase, teams, time_m, time_s, timer_m, timer_s, victory |

---

## エラーメッセージ

| エラー | メッセージ | 対応function |
|-------|-----------|-------------|
| チーム未選択 | `有効なチームが選択されていません` | `message/tellraw/error/no_team_selected` |
| プレイヤー不在 | `プレイヤーが存在しません` | `message/tellraw/error/player_does_not_exist` |
| チーム変更失敗 | `チーム変更に失敗しました` | `message/tellraw/error/failed_to_change_teams` |
| 権限不足 | `このコマンドを実行する権限がありません` | （新規） |

---

## 実装推奨クラス構成

```java
// コマンドエグゼキューター
public class KCDKCommandExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command,
                            String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("使用方法: /kcdk <サブコマンド>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setup":
                return executeSetup(sender);
            case "setup-format":
                return executeSetupWithFormatting(sender);
            case "get":
                return executeGet(sender);
            case "reset":
                return executeReset(sender);
            // ... 他のサブコマンド
        }
    }
}

// チームenum
public enum Team {
    BLUE(8, 255, ChatColor.BLUE),
    GREEN(4, 32768, ChatColor.GREEN),
    RED(2, 16711680, ChatColor.RED),
    YELLOW(1, 16776960, ChatColor.YELLOW);

    private final int bit;
    private final int armorColor;
    private final ChatColor color;

    public boolean isEnabled(int teams) {
        return (teams & bit) != 0;
    }
}
```

---

## plugin.yml定義

```yaml
name: KCDK
version: 2.2.0
main: com.example.kcdk.KCDKPlugin
api-version: 1.16
commands:
  kcdk:
    description: KCDK main command
    usage: /kcdk <subcommand>
    permission: kcdk.use
permissions:
  kcdk.admin:
    description: Admin permission
    default: op
  kcdk.use:
    description: Basic usage permission
    default: true
```

---

**以上、KCDK Plugin コマンド仕様書（oldkcdk実機能限定版）**
