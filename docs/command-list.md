## 1. セットアップコマンド

### 1.1 `/kcdk setup`
**説明:** ゲームの初期セットアップを実行

**構文:**
```
/kcdk setup
```

## 2. チーム割り当てコマンド

### 2.1 `/kcdk team assign`
**説明:** 全プレイヤーをチームに自動割り当て

**構文:**
```
/kcdk team assign
```

### 2.2 `/kcdk team change`
**説明:** プレイヤーのチームを変更（移動）

**権限:** `kcdk.admin`

**構文:**
```
/kcdk team change <player> <team>
```

**引数:**
- `<player>` - 対象プレイヤー名
- `<team>` - 移動先チーム

## 3. ゲーム制御コマンド

### 3.1 `/kcdk game start`

**構文:**
```
/kcdk team game start
```

### 3.2 `/kcdk game stop`

**構文:**
```
/kcdk game stop <title>
```

---

## 4. 設定コマンド

### 4.1 グローバル設定

#### `/kcdk config gamemode <gamemode>`
ゲームモードを設定

**引数:**
- `<gamemode>` - survival, adventure, creative, spectator

**例:**
```
/kcdk config gamemode survival
```

---

#### `/kcdk config showBossBar <true|false>`
ボスバー表示の有効/無効

**例:**
```
/kcdk config showBossBar true
```

---

### 4.2 時間制限設定

#### `/kcdk config timeLimit set <hour> <minutes> <second>`
時間制限を設定

**例:**
```
/kcdk config timeLimit set 0 10 0
```

---

#### `/kcdk config timeLimit remove`
時間制限を削除（制限なし）

**例:**
```
/kcdk config timeLimit remove
```

---

### 4.3 チーム管理

#### `/kcdk config team add <team>`
新しいチームを追加

**例:**
```
/kcdk config team add blue
```

---

#### `/kcdk config team remove <team>`
チームを削除

**例:**
```
/kcdk config team remove blue
```

---

#### `/kcdk config team clear`
全チームを削除

**例:**
```
/kcdk config team clear
```

---

### 4.4 チーム設定

#### `/kcdk config team <team> displayName <displayName>`
チームの表示名を設定

**例:**
```
/kcdk config team blue displayName "§9青チーム"
```

---

#### `/kcdk config team <team> armorColor <color>`
チームの革アーマーカラーを設定（RGB整数値）

**例:**
```
/kcdk config team blue armorColor 255
```

---

#### `/kcdk config team <team> readyLocation <world> <x> <y> <z> <yaw> <pitch>`
チームの待機地点を設定

**例:**
```
/kcdk config team blue readyLocation world 0 64 0 0 0
```

---

#### `/kcdk config team <team> readyLocation remove`
チームの待機地点を削除（null）

**例:**
```
/kcdk config team blue readyLocation remove
```

---

#### `/kcdk config team <team> respawnLocation <world> <x> <y> <z> <yaw> <pitch>`
チームのリスポーン地点を設定

**例:**
```
/kcdk config team blue respawnLocation world 0 64 0 0 0
```

---

#### `/kcdk config team <team> stock <count>`
チームのリスポーン回数を設定（負数=無限、0=スペクテイター）

**例:**
```
/kcdk config team blue stock -1
```

---

#### `/kcdk config team <team> waitingTime <hour> <minutes> <second>`
チームの待機時間を設定

**例:**
```
/kcdk config team blue waitingTime 0 0 30
```

---

### 4.5 チームエフェクト管理

#### `/kcdk config team <team> effect add <effectName> <second> <amplifier> <hideParticles>`
チームにエフェクトを追加

**例:**
```
/kcdk config team blue effect add SPEED 999999 1 false
```

---

#### `/kcdk config team <team> effect remove <effectName>`
チームからエフェクトを削除

**例:**
```
/kcdk config team blue effect remove SPEED
```

---

#### `/kcdk config team <team> effect clear`
チームの全エフェクトを削除

**例:**
```
/kcdk config team blue effect clear
```

---

### 4.6 ロール管理

#### `/kcdk config team <team> role add <role>`
チームにロールを追加

**例:**
```
/kcdk config team red role add captain
```

---

#### `/kcdk config team <team> role remove <role>`
チームからロールを削除

**例:**
```
/kcdk config team red role remove captain
```

---

#### `/kcdk config team <team> role clear`
チームの全ロールを削除

**例:**
```
/kcdk config team red role clear
```

---

### 4.7 ロール設定

#### `/kcdk config team <team> role <role> displayName <displayName>`
ロールの表示名を設定

**例:**
```
/kcdk config team red role captain displayName "§e大将"
```

---

#### `/kcdk config team <team> role <role> displayName remove`
ロールの表示名を削除（親チーム継承）

**例:**
```
/kcdk config team red role captain displayName remove
```

---

#### `/kcdk config team <team> role <role> armorColor <color>`
ロールの革アーマーカラーを設定

**例:**
```
/kcdk config team red role captain armorColor 16711680
```

---

#### `/kcdk config team <team> role <role> armorColor remove`
ロールのアーマーカラーを削除（親チーム継承）

**例:**
```
/kcdk config team red role captain armorColor remove
```

---

#### `/kcdk config team <team> role <role> readyLocation <world> <x> <y> <z> <yaw> <pitch>`
ロールの待機地点を設定

**例:**
```
/kcdk config team red role captain readyLocation world 0 64 0 0 0
```

---

#### `/kcdk config team <team> role <role> readyLocation remove`
ロールの待機地点を削除（親チーム継承）

**例:**
```
/kcdk config team red role captain readyLocation remove
```

---

#### `/kcdk config team <team> role <role> respawnLocation <world> <x> <y> <z> <yaw> <pitch>`
ロールのリスポーン地点を設定

**例:**
```
/kcdk config team red role captain respawnLocation world 110 64 0 180 0
```

---

#### `/kcdk config team <team> role <role> stock <count>`
ロールのリスポーン回数を設定

**例:**
```
/kcdk config team red role captain stock 3
```

---

#### `/kcdk config team <team> role <role> stock remove`
ロールのリスポーン回数を削除（親チーム継承）

**例:**
```
/kcdk config team red role captain stock remove
```

---

#### `/kcdk config team <team> role <role> waitingTime <hour> <minutes> <second>`
ロールの待機時間を設定

**例:**
```
/kcdk config team red role captain waitingTime 0 1 0
```

---

#### `/kcdk config team <team> role <role> waitingTime remove`
ロールの待機時間を削除（親チーム継承）

**例:**
```
/kcdk config team red role captain waitingTime remove
```

---

#### `/kcdk config team <team> role <role> extendsEffects <true|false>`
ロールが親チームのエフェクトを継承するか設定

**例:**
```
/kcdk config team red role captain extendsEffects true
```

---

#### `/kcdk config team <team> role <role> extendsItem <true|false>`
ロールが親チームのアイテムを継承するか設定

**例:**
```
/kcdk config team red role captain extendsItem true
```

---

### 4.8 ロールエフェクト管理

#### `/kcdk config team <team> role <role> effect add <effectName> <second> <amplifier> <hideParticles>`
ロールにエフェクトを追加

**例:**
```
/kcdk config team red role captain effect add SPEED 999999 2 false
```

---

#### `/kcdk config team <team> role <role> effect remove <effectName>`
ロールからエフェクトを削除

**例:**
```
/kcdk config team red role captain effect remove SPEED
```

---

#### `/kcdk config team <team> role <role> effect clear`
ロールの全エフェクトを削除

**例:**
```
/kcdk config team red role captain effect clear
```

---

### 4.9 終了条件管理

#### `/kcdk config endCondition add timeLimit <message>`
時間制限終了条件を追加

**例:**
```
/kcdk config endCondition add timeLimit "§e時間切れ！"
```

---

#### `/kcdk config endCondition add beacon <message> <world> <x> <y> <z> <yaw> <pitch> <hitpoint>`
ビーコン破壊終了条件を追加

**例:**
```
/kcdk config endCondition add beacon "§cビーコン破壊！" world 0 64 0 0 0 100
```

---

#### `/kcdk config endCondition add extermination <message> <team>`
チーム全滅終了条件を追加

**例:**
```
/kcdk config endCondition add extermination "§4全滅！" blue
```

---

#### `/kcdk config endCondition add ticket <message> <team> <count>`
チケット制終了条件を追加

**例:**
```
/kcdk config endCondition add ticket "§6チケット消費！" red 50
```

---

#### `/kcdk config endCondition remove <index>`
指定したインデックスの終了条件を削除

**例:**
```
/kcdk config endCondition remove 0
```

---

#### `/kcdk config endCondition clear`
全終了条件を削除

**例:**
```
/kcdk config endCondition clear
```

---

#### `/kcdk config endCondition list`
全終了条件を表示

**例:**
```
/kcdk config endCondition list
```

---

### 4.10 設定の表示・保存・リロード

#### `/kcdk config show`
現在の設定を表示

**例:**
```
/kcdk config show
```

---

#### `/kcdk config save`
現在の設定をconfig.ymlに保存

**例:**
```
/kcdk config save
```

---

#### `/kcdk config reload`
config.ymlから設定を再読み込み

**例:**
```
/kcdk config reload
```