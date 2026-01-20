# KCDK Plugin - 設定仕様書

## config.yml

```yaml
# KCDK Plugin Configuration
config-version: 1
# Java型: int

# ゲームモード設定
gamemode: survival
# Java型: String
# 選択肢: survival, adventure, creative, spectator

# 時間制限設定（null可）
timeLimit:
  hour: 0
  minutes: 10
  second: 0
  # Java型: Integer, Integer, Integer (null許容)

# チーム設定
teams:
  - name: blue
    displayName: "§9青チーム"
    armorColor: 255
    readyLocation:
      world: world
      x: 0.0
      y: 64.0
      z: 0.0
      yaw: 0.0
      pitch: 0.0
    respawnLocation:
      world: world
      x: 0.0
      y: 64.0
      z: 0.0
      yaw: 0.0
      pitch: 0.0
    stock: -1
    waitingTime:
      hour: 0
      minutes: 0
      second: 30
    effects: []
    roles: []
    # Java型: String, String, int, Location|null, Location, int, Time, List<Effect>, List<Role>
    # stock: 負数=無限リスポーン, 0=スペクテイター化
    # waitingTime: ゲーム開始からrespawnLocationへのテレポートまでの待機時間
    # readyLocationがnullまたはwaitingTimeが0の場合、開始と同時にrespawnLocationへテレポート
    # effects: ゲーム開始時に付与されるエフェクト

  - name: red
    displayName: "§c赤チーム"
    armorColor: 16711680
    readyLocation:
      world: world
      x: 100.0
      y: 64.0
      z: 0.0
      yaw: 180.0
      pitch: 0.0
    respawnLocation:
      world: world
      x: 100.0
      y: 64.0
      z: 0.0
      yaw: 180.0
      pitch: 0.0
    stock: -1
    waitingTime:
      hour: 0
      minutes: 0
      second: 30
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
        stock: null
        waitingTime: null
        effects:
          - name: SPEED
            second: 999999
            amplifier: 1
            hideParticles: false
          - name: STRENGTH
            second: 999999
            amplifier: 0
            hideParticles: false
        extendsEffects: true
        extendsItem: true
        # Java型: String, String|null, int|null, Location|null, Location, int|null, Time|null, List<Effect>, boolean, boolean
        # null設定時は親チームの設定を継承
        # extendsEffects: 親チームのエフェクトを継承するか
        # extendsItem: 親チームのアイテムを継承するか

# 終了条件設定
endConditions:
  # TimeLimit（timeLimitが設定されている場合自動適用）
  - type: TimeLimit
    message: "§e時間切れ！"
    # Java型: String

  # Beacon破壊条件
  - type: Beacon
    message: "§cビーコンが破壊された！"
    location:
      world: world
      x: 0.0
      y: 64.0
      z: 0.0
      yaw: 0.0
      pitch: 0.0
    hitpoint: 100
    # Java型: String, Location, int

  # 殲滅条件
  - type: Extermination
    message: "§4全滅！"
    team: blue
    # Java型: String, String

  # チケット制条件
  - type: Ticket
    message: "§6チケットが尽きた！"
    team: red
    count: 50
    # Java型: String, String, int

  # 複合条件（AND結合）
  - type: Composite
    message: "§d複合条件達成！"
    conditions:
      - type: Beacon
        message: "ビーコン破壊"
        location:
          world: world
          x: 0.0
          y: 64.0
          z: 0.0
          yaw: 0.0
          pitch: 0.0
        hitpoint: 100
      - type: Ticket
        message: "チケット消費"
        team: blue
        count: 30
    # Java型: String, List<EndCondition>
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
- ロールの各プロパティ（displayName, armorColor, readyLocation, stock, waitingTime）がnullの場合、親チームの設定を自動継承
- `extendsEffects: true` の場合、親チームのeffectsに加えてロール独自のeffectsも付与
- `extendsItem: true` の場合、親チームのアイテムに加えてロール独自のアイテムも付与

**終了条件での扱い:**
例えば`Extermination`条件で`team: red`を指定した場合、`kcdk.red`だけでなく`kcdk.red.captain`などの全ロールを含めて全滅判定が行われます。

---

## 設定項目一覧

| キー | 型 | 説明 |
|-----|----|----|
| `config-version` | int | 設定バージョン |
| `gamemode` | String | ゲームモード (survival/adventure/creative/spectator) |
| `timeLimit` | Time | 時間制限（null可） |
| `timeLimit.hour` | Integer | 時間 |
| `timeLimit.minutes` | Integer | 分 |
| `timeLimit.second` | Integer | 秒 |
| `teams` | List\<Team\> | チームリスト |
| `teams[].name` | String | チーム識別名 |
| `teams[].displayName` | String | チーム表示名 |
| `teams[].armorColor` | int | 革装備色（RGB） |
| `teams[].readyLocation` | Location\|null | 待機地点（null可） |
| `teams[].respawnLocation` | Location | リスポーン地点 |
| `teams[].stock` | int | リスポーン回数（負数=無限、0=スペクテイター） |
| `teams[].waitingTime` | Time | ゲーム開始からrespawnLocationへのテレポート待機時間 |
| `teams[].effects` | List\<Effect\> | ゲーム開始時付与エフェクト |
| `teams[].roles` | List\<Role\> | チーム内ロール（親チームの子として扱われる） |
| `teams[].roles[].name` | String | ロール識別名 |
| `teams[].roles[].displayName` | String\|null | ロール表示名（null時は親チーム継承） |
| `teams[].roles[].armorColor` | int\|null | 革装備色（null時は親チーム継承） |
| `teams[].roles[].readyLocation` | Location\|null | 待機地点（null時は親チーム継承） |
| `teams[].roles[].respawnLocation` | Location | リスポーン地点 |
| `teams[].roles[].stock` | int\|null | リスポーン回数（null時は親チーム継承） |
| `teams[].roles[].waitingTime` | Time\|null | 待機時間（null時は親チーム継承） |
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
| `endConditions[].type` | String | 条件タイプ (TimeLimit/Beacon/Extermination/Ticket/Composite) |
| `endConditions[].message` | String | 条件達成時メッセージ |
| `effect.name` | String | エフェクト名 |
| `effect.second` | int | 持続時間（秒） |
| `effect.amplifier` | int | 効果レベル |
| `effect.hideParticles` | boolean | パーティクル非表示 |

---

## EndCondition タイプ詳細

### TimeLimit
時間制限による終了（timeLimitが設定されている場合自動適用）

```yaml
- type: TimeLimit
  message: "時間切れ"
```

### Beacon
ビーコン破壊による終了

```yaml
- type: Beacon
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

### Extermination
チーム全滅による終了

```yaml
- type: Extermination
  message: "全滅"
  team: blue
```

### Ticket
チケット制による終了

```yaml
- type: Ticket
  message: "チケット消費"
  team: red
  count: 50
```

### Composite
複合条件（AND結合）

```yaml
- type: Composite
  message: "複合条件達成"
  conditions:
    - type: Beacon
      message: "ビーコン破壊"
      location: {...}
      hitpoint: 100
    - type: Ticket
      message: "チケット消費"
      team: blue
      count: 30
```

---

## JSON形式（コマンド用）

```json
{
  "gamemode": "survival",
  "timeLimit": {
    "hour": 0,
    "minutes": 10,
    "second": 0
  },
  "teams": [
    {
      "name": "blue",
      "displayName": "§9青チーム",
      "armorColor": 255,
      "readyLocation": {"world": "world", "x": 0.0, "y": 64.0, "z": 0.0, "yaw": 0.0, "pitch": 0.0},
      "respawnLocation": {"world": "world", "x": 0.0, "y": 64.0, "z": 0.0, "yaw": 0.0, "pitch": 0.0},
      "stock": -1,
      "waitingTime": {"hour": 0, "minutes": 0, "second": 30},
      "effects": [],
      "roles": []
    },
    {
      "name": "red",
      "displayName": "§c赤チーム",
      "armorColor": 16711680,
      "readyLocation": {"world": "world", "x": 100.0, "y": 64.0, "z": 0.0, "yaw": 180.0, "pitch": 0.0},
      "respawnLocation": {"world": "world", "x": 100.0, "y": 64.0, "z": 0.0, "yaw": 180.0, "pitch": 0.0},
      "stock": -1,
      "waitingTime": {"hour": 0, "minutes": 0, "second": 30},
      "effects": [],
      "roles": [
        {
          "name": "captain",
          "displayName": "§e大将",
          "armorColor": null,
          "readyLocation": null,
          "respawnLocation": {"world": "world", "x": 110.0, "y": 64.0, "z": 0.0, "yaw": 180.0, "pitch": 0.0},
          "stock": null,
          "waitingTime": null,
          "effects": [
            {"name": "SPEED", "second": 999999, "amplifier": 1, "hideParticles": false},
            {"name": "STRENGTH", "second": 999999, "amplifier": 0, "hideParticles": false}
          ],
          "extendsEffects": true,
          "extendsItem": true
        }
      ]
    }
  ],
  "endConditions": [
    {
      "type": "TimeLimit",
      "message": "§e時間切れ！"
    },
    {
      "type": "Beacon",
      "message": "§cビーコン破壊",
      "location": {"world": "world", "x": 0.0, "y": 64.0, "z": 0.0, "yaw": 0.0, "pitch": 0.0},
      "hitpoint": 100
    },
    {
      "type": "Extermination",
      "message": "§4全滅",
      "team": "blue"
    },
    {
      "type": "Ticket",
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
# JSON一括設定
/kcdk config import <json>

# 個別設定
/kcdk config set <key> <value>

# 設定取得
/kcdk config get <key>

# 設定表示
/kcdk config show

# リロード
/kcdk config reload
```
