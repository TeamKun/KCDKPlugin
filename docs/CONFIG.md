# KCDK Plugin - 設定仕様書

## config.yml

```yaml
# KCDK Plugin Configuration
config-version: 1
# Java型: int

# ゲームモード設定
gamemode: ADVENTURE
# Java型: String
# 選択肢: ADVENTURE, SURVIVAL

# ボスバー設定（null可）
bossbar:
  mcid: "Steve"
  # Java型: String
  # 対象プレイヤーのMCID（Minecraft ID）

# 時間制限設定（null可）
timeLimit:
  hours: 0
  minutes: 10
  seconds: 0
  # Java型: Integer, Integer, Integer (null許容)

# スタートアップコマンド（ゲーム開始時に実行）
startupCommands:
  - "/say ゲーム開始！"
  - "/effect give @a minecraft:regeneration 5 1"
  # Java型: List<String>

# シャットダウンコマンド（ゲーム終了時に実行）
shutdownCommands:
  - "/say ゲーム終了！"
  # Java型: List<String>

# チーム設定
teams:
  - name: blue
    displayName: "§9青チーム"
    armorColor: "#3b82f6"
    respawnCount: -1
    readyLocation:
      world: world
      x: 0.0
      y: 64.0
      z: 0.0
      yaw: 0.0
      pitch: 0.0
      waitingTime:
        hours: 0
        minutes: 0
        seconds: 30
    respawnLocation:
      world: world
      x: 0.0
      y: 64.0
      z: 0.0
      yaw: 0.0
      pitch: 0.0
    effects: []
    roles: []
    # Java型: String, String, String, int, ReadyLocation|null, Location, List<Effect>, List<Role>
    # respawnCount: 負数=無限リスポーン, 0=スペクテイター化
    # readyLocation.waitingTime: ゲーム開始からrespawnLocationへのテレポートまでの待機時間
    # readyLocationがnullの場合、開始と同時にrespawnLocationへテレポート
    # effects: ゲーム開始時に付与されるエフェクト

  - name: red
    displayName: "§c赤チーム"
    armorColor: "#ef4444"
    respawnCount: -1
    readyLocation:
      world: world
      x: 100.0
      y: 64.0
      z: 0.0
      yaw: 180.0
      pitch: 0.0
      waitingTime:
        hours: 0
        minutes: 0
        seconds: 30
    respawnLocation:
      world: world
      x: 100.0
      y: 64.0
      z: 0.0
      yaw: 180.0
      pitch: 0.0
    effects: []
    roles:
      - name: captain
        displayName: "§e大将"
        armorColor: null
        readyLocation: null
        respawnLocation:
          world: world
          x: 110.0
          y: 64.0
          z: 0.0
          yaw: 180.0
          pitch: 0.0
        respawnCount: null
        effects:
          - name: SPEED
            seconds: 999999
            amplifier: 1
            hideParticles: false
          - name: STRENGTH
            seconds: 999999
            amplifier: 0
            hideParticles: false
        extendsEffects: true
        extendsItem: true
        # Java型: String, String|null, String|null, ReadyLocation|null, Location|null, int|null, List<Effect>, boolean, boolean
        # null設定時は親チームの設定を継承
        # extendsEffects: 親チームのエフェクトを継承するか
        # extendsItem: 親チームのアイテムを継承するか

# 終了条件設定
endConditions:
  # Beacon破壊条件
  - type: beacon
    message: "§cビーコンが破壊された！"
    location:
      world: world
      x: 0.0
      y: 64.0
      z: 0.0
      yaw: 0.0
      pitch: 0.0
    hitpoint: 100
    # Java型: String, String, Location, int

  # 殲滅条件
  - type: extermination
    message: "§4全滅！"
    team: blue
    # Java型: String, String, String

  # チケット制条件
  - type: ticket
    message: "§6チケットが尽きた！"
    team: red
    count: 50
    # Java型: String, String, String, int

  # 複合条件（AND/OR結合）
  - type: composite
    message: "§d複合条件達成！"
    operator: AND
    conditions:
      - type: beacon
        message: "ビーコン破壊"
        location:
          world: world
          x: 0.0
          y: 64.0
          z: 0.0
          yaw: 0.0
          pitch: 0.0
        hitpoint: 100
      - type: ticket
        message: "チケット消費"
        team: blue
        count: 30
    # Java型: String, String, String (AND|OR), List<EndCondition>
```

---

## 内部チーム管理

プラグインは設定されたチームとロールを元に、Minecraftの内部Teamを自動生成します。

### 命名規則

- **ベースチーム**: `kcdk.{team.name}`
- **ロール付きチーム**: `kcdk.{team.name}.{role.name}`

### 例

```yaml
teams:
  - name: red
    roles:
      - name: captain
```

上記の設定により以下のMinecraft Teamが生成されます:
- `kcdk.red` - 赤チームのベースチーム
- `kcdk.red.captain` - 赤チームのcaptainロール

ロールが設定されたプレイヤーは、ベースチームではなくロール専用チーム（`kcdk.{team}.{role}`）に所属します。

### 親子関係と継承

ロールは親チームの子として扱われます。

**設定の継承:**
- ロールの各プロパティ（displayName, armorColor, readyLocation, respawnCount）がnullの場合、親チームの設定を自動継承
- `extendsEffects: true` の場合、親チームのeffectsに加えてロール独自のeffectsも付与
- `extendsItem: true` の場合、親チームのアイテムに加えてロール独自のアイテムも付与

**終了条件での扱い:**
例えば`extermination`条件で`team: red`を指定した場合、`kcdk.red`だけでなく`kcdk.red.captain`などの全ロールを含めて全滅判定が行われます。

---

## 設定項目一覧

| キー | 型 | 説明 |
|-----|----|----|
| `config-version` | int | 設定バージョン |
| `gamemode` | String | ゲームモード (ADVENTURE/SURVIVAL) |
| `bossbar` | Bossbar\|null | ボスバー設定（null可） |
| `bossbar.mcid` | String | 対象プレイヤーのMCID |
| `timeLimit` | Time\|null | 時間制限（null可） |
| `timeLimit.hours` | int | 時間 |
| `timeLimit.minutes` | int | 分 |
| `timeLimit.seconds` | int | 秒 |
| `startupCommands` | List\<String\> | ゲーム開始時実行コマンド |
| `shutdownCommands` | List\<String\> | ゲーム終了時実行コマンド |
| `teams` | List\<Team\> | チームリスト |
| `teams[].name` | String | チーム識別名 |
| `teams[].displayName` | String | チーム表示名 |
| `teams[].armorColor` | String | 革アーマーカラー（HEX形式: #RRGGBB） |
| `teams[].respawnCount` | int | リスポーン回数（負数=無限、0=スペクテイター） |
| `teams[].readyLocation` | ReadyLocation\|null | 待機地点（null可） |
| `teams[].readyLocation.waitingTime` | Time | 待機時間 |
| `teams[].respawnLocation` | Location | リスポーン地点 |
| `teams[].effects` | List\<Effect\> | ゲーム開始時付与エフェクト |
| `teams[].roles` | List\<Role\> | チーム内ロール |
| `teams[].roles[].name` | String | ロール識別名 |
| `teams[].roles[].displayName` | String\|null | ロール表示名（null時は親チーム継承） |
| `teams[].roles[].armorColor` | String\|null | 革アーマーカラー（null時は親チーム継承） |
| `teams[].roles[].readyLocation` | ReadyLocation\|null | 待機地点（null時は親チーム継承） |
| `teams[].roles[].respawnLocation` | Location\|null | リスポーン地点（null時は親チーム継承） |
| `teams[].roles[].respawnCount` | int\|null | リスポーン回数（null時は親チーム継承） |
| `teams[].roles[].effects` | List\<Effect\> | ゲーム開始時付与エフェクト |
| `teams[].roles[].extendsEffects` | boolean | 親チームのエフェクトを継承するか |
| `teams[].roles[].extendsItem` | boolean | 親チームのアイテムを継承するか |
| `location.world` | String | ワールド名 |
| `location.x` | double | X座標 |
| `location.y` | double | Y座標 |
| `location.z` | double | Z座標 |
| `location.yaw` | float | 向き（水平） |
| `location.pitch` | float | 向き（垂直） |
| `endConditions` | List\<EndCondition\> | 終了条件リスト |
| `endConditions[].type` | String | 条件タイプ (beacon/extermination/ticket/composite) |
| `endConditions[].message` | String | 条件達成時メッセージ |
| `effect.name` | String | エフェクト名 |
| `effect.seconds` | int | 持続時間（秒） |
| `effect.amplifier` | int | 効果レベル |
| `effect.hideParticles` | boolean | パーティクル非表示 |

---

## EndCondition タイプ詳細

### beacon
ビーコン破壊による終了

```yaml
- type: beacon
  message: "ビーコン破壊"
  location:
    world: world
    x: 0.0
    y: 64.0
    z: 0.0
    yaw: 0.0
    pitch: 0.0
  hitpoint: 100
```

### extermination
チーム全滅による終了

```yaml
- type: extermination
  message: "全滅"
  team: blue
```

### ticket
チケット制による終了

```yaml
- type: ticket
  message: "チケット消費"
  team: red
  count: 50
```

### composite
複合条件（AND/OR結合）

```yaml
- type: composite
  message: "複合条件達成"
  operator: AND
  conditions:
    - type: beacon
      message: "ビーコン破壊"
      location: {...}
      hitpoint: 100
    - type: ticket
      message: "チケット消費"
      team: blue
      count: 30
```

---

## JSON形式（コマンド用）

```json
{
  "gamemode": "ADVENTURE",
  "bossbar": {
    "mcid": "Steve"
  },
  "timeLimit": {
    "hours": 0,
    "minutes": 10,
    "seconds": 0
  },
  "startupCommands": ["/say ゲーム開始！"],
  "shutdownCommands": ["/say ゲーム終了！"],
  "teams": [
    {
      "name": "blue",
      "displayName": "§9青チーム",
      "armorColor": "#3b82f6",
      "respawnCount": -1,
      "readyLocation": {
        "world": "world",
        "x": 0.0,
        "y": 64.0,
        "z": 0.0,
        "yaw": 0.0,
        "pitch": 0.0,
        "waitingTime": {"hours": 0, "minutes": 0, "seconds": 30}
      },
      "respawnLocation": {"world": "world", "x": 0.0, "y": 64.0, "z": 0.0, "yaw": 0.0, "pitch": 0.0},
      "effects": [],
      "roles": []
    },
    {
      "name": "red",
      "displayName": "§c赤チーム",
      "armorColor": "#ef4444",
      "respawnCount": -1,
      "readyLocation": {
        "world": "world",
        "x": 100.0,
        "y": 64.0,
        "z": 0.0,
        "yaw": 180.0,
        "pitch": 0.0,
        "waitingTime": {"hours": 0, "minutes": 0, "seconds": 30}
      },
      "respawnLocation": {"world": "world", "x": 100.0, "y": 64.0, "z": 0.0, "yaw": 180.0, "pitch": 0.0},
      "effects": [],
      "roles": [
        {
          "name": "captain",
          "displayName": "§e大将",
          "armorColor": null,
          "readyLocation": null,
          "respawnLocation": {"world": "world", "x": 110.0, "y": 64.0, "z": 0.0, "yaw": 180.0, "pitch": 0.0},
          "respawnCount": null,
          "effects": [
            {"name": "SPEED", "seconds": 999999, "amplifier": 1, "hideParticles": false},
            {"name": "STRENGTH", "seconds": 999999, "amplifier": 0, "hideParticles": false}
          ],
          "extendsEffects": true,
          "extendsItem": true
        }
      ]
    }
  ],
  "endConditions": [
    {
      "type": "beacon",
      "message": "§cビーコン破壊",
      "location": {"world": "world", "x": 0.0, "y": 64.0, "z": 0.0, "yaw": 0.0, "pitch": 0.0},
      "hitpoint": 100
    },
    {
      "type": "extermination",
      "message": "§4全滅",
      "team": "blue"
    },
    {
      "type": "ticket",
      "message": "§6チケット消費",
      "team": "red",
      "count": 50
    }
  ]
}
```

---

## コマンドでの設定変更

```
# JSON一括設定（Base64エンコード済み短縮JSON）
/kcdk config import <base64>

# 個別設定
/kcdk config set <key> <value>

# 設定取得
/kcdk config get <key>

# 設定表示
/kcdk config show

# リロード
/kcdk config reload
```

---

## JSON短縮キー対応表

`/kcdk config import`コマンドで使用されるBase64エンコード済みJSONは、データ量削減のためキーが1文字に短縮されています。

| 元のキー | 短縮キー | 説明 |
|---------|---------|------|
| `gamemode` | `a` | ゲームモード |
| `bossbar` | `b` | ボスバー設定 |
| `timeLimit` | `c` | 時間制限 |
| `startupCommands` | `d` | 開始時コマンド |
| `shutdownCommands` | `e` | 終了時コマンド |
| `teams` | `f` | チームリスト |
| `endConditions` | `g` | 終了条件 |
| `mcid` | `h` | プレイヤーMCID |
| `hours` | `i` | 時間 |
| `minutes` | `j` | 分 |
| `seconds` | `k` | 秒 |
| `name` | `l` | 名前 |
| `displayName` | `m` | 表示名 |
| `armorColor` | `n` | アーマーカラー |
| `respawnCount` | `o` | リスポーン回数 |
| `readyLocation` | `p` | 待機地点 |
| `respawnLocation` | `q` | リスポーン地点 |
| `effects` | `r` | エフェクト |
| `roles` | `s` | ロール |
| `world` | `t` | ワールド名 |
| `x` | `u` | X座標 |
| `y` | `v` | Y座標 |
| `z` | `w` | Z座標 |
| `yaw` | `A` | 向き（水平） |
| `pitch` | `B` | 向き（垂直） |
| `waitingTime` | `C` | 待機時間 |
| `extendsEffects` | `D` | エフェクト継承 |
| `extendsItem` | `E` | アイテム継承 |
| `amplifier` | `F` | 効果レベル |
| `hideParticles` | `G` | パーティクル非表示 |
| `type` | `H` | 条件タイプ |
| `message` | `I` | メッセージ |
| `conditions` | `J` | 複合条件リスト |
| `operator` | `K` | 結合演算子 |
| `location` | `L` | 位置 |
| `hitpoint` | `M` | HP |
| `team` | `N` | チーム名 |
| `count` | `O` | カウント |

### デコード処理（Java実装例）

```java
import java.util.Base64;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ConfigDecoder {
    private static final Map<String, String> REVERSE_KEY_MAP = Map.ofEntries(
        Map.entry("a", "gamemode"),
        Map.entry("b", "bossbar"),
        Map.entry("c", "timeLimit"),
        Map.entry("d", "startupCommands"),
        Map.entry("e", "shutdownCommands"),
        Map.entry("f", "teams"),
        Map.entry("g", "endConditions"),
        Map.entry("h", "mcid"),
        Map.entry("i", "hours"),
        Map.entry("j", "minutes"),
        Map.entry("k", "seconds"),
        Map.entry("l", "name"),
        Map.entry("m", "displayName"),
        Map.entry("n", "armorColor"),
        Map.entry("o", "respawnCount"),
        Map.entry("p", "readyLocation"),
        Map.entry("q", "respawnLocation"),
        Map.entry("r", "effects"),
        Map.entry("s", "roles"),
        Map.entry("t", "world"),
        Map.entry("u", "x"),
        Map.entry("v", "y"),
        Map.entry("w", "z"),
        Map.entry("A", "yaw"),
        Map.entry("B", "pitch"),
        Map.entry("C", "waitingTime"),
        Map.entry("D", "extendsEffects"),
        Map.entry("E", "extendsItem"),
        Map.entry("F", "amplifier"),
        Map.entry("G", "hideParticles"),
        Map.entry("H", "type"),
        Map.entry("I", "message"),
        Map.entry("J", "conditions"),
        Map.entry("K", "operator"),
        Map.entry("L", "location"),
        Map.entry("M", "hitpoint"),
        Map.entry("N", "team"),
        Map.entry("O", "count")
    );

    public static String decodeBase64(String encoded) {
        byte[] decoded = Base64.getDecoder().decode(encoded);
        return new String(decoded, StandardCharsets.UTF_8);
    }

    public static JsonObject expandKeys(JsonObject minified) {
        // 再帰的にキーを展開する処理
        // 実装省略
    }
}
```
