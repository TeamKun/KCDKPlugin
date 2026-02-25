# 02 設定コマンド

全設定項目を網羅的にテストする。各 TC は独立して実行可能。
設定変更後は `/kcdk config show` で反映を目視確認し、
`/kcdk config save` → `/kcdk config reload` で永続化も合わせて確認すること。

---

## A. グローバル設定

### TC-010: gamemode — ADVENTURE

**手順**
```
/kcdk config gamemode ADVENTURE
/kcdk config show
```

**期待結果**
- [ ] `Gamemode: ADVENTURE` と表示される
- [ ] ゲーム開始後プレイヤーのゲームモードが ADVENTURE になる（→ TC-030 参照）

---

### TC-011: gamemode — SURVIVAL

**手順**
```
/kcdk config gamemode SURVIVAL
/kcdk config show
```

**期待結果**
- [ ] `Gamemode: SURVIVAL` と表示される

---

### TC-012: timeLimit 設定

**手順**
```
/kcdk config timeLimit set 0 10 0
/kcdk config show
```

**期待結果**
- [ ] `Time Limit: 0h 10m 0s` と表示される

---

### TC-013: timeLimit 削除

**手順**
```
/kcdk config timeLimit remove
/kcdk config show
```

**期待結果**
- [ ] `Time Limit: none` と表示される
- [ ] ゲーム中は制限時間なし（経過時間表示）になる（→ TC-062 参照）

---

### TC-014: bossbar 設定

**手順**
```
/kcdk config bossbar set <MCIDのプレイヤー名>
/kcdk config show
```

**期待結果**
- [ ] `Bossbar: <設定したMCID>` と表示される
- [ ] ゲーム中ボスバーが表示される（→ TC-063 参照）

---

### TC-015: disableHunger — true

**手順**
```
/kcdk config disableHunger true
/kcdk config show
```

**期待結果**
- [ ] `Disable Hunger: true` と表示される
- [ ] ゲーム中、プレイヤーの空腹ゲージが減らない（→ TC-073 参照）

---

### TC-016: disableHunger — false

**手順**
```
/kcdk config disableHunger false
/kcdk config show
```

**期待結果**
- [ ] `Disable Hunger: false` と表示される
- [ ] ゲーム中、空腹ゲージが通常通り減る

---

### TC-017: startupCommands 設定

**手順**
```
/kcdk config startupCommand add /say ゲーム開始！
/kcdk config startupCommand add /effect give @a minecraft:speed 30 1
/kcdk config show
```

**期待結果**
- [ ] startupCommands に2件登録されている
- [ ] ゲーム開始時に両コマンドが順番に実行される（→ TC-031 参照）

---

### TC-018: shutdownCommands 設定

**手順**
```
/kcdk config shutdownCommand add /say ゲーム終了！
/kcdk config show
```

**期待結果**
- [ ] shutdownCommands に1件登録されている
- [ ] ゲーム終了時に実行される（→ TC-055 参照）

---

## B. チーム設定

### TC-020: チーム追加・削除

**手順**
```
/kcdk config team add red
/kcdk config team add blue
/kcdk config show
```

**期待結果**
- [ ] red, blue チームが設定に追加される

```
/kcdk config team remove blue
/kcdk config show
```

- [ ] blue チームが設定から削除される

---

### TC-021: displayName 設定

**手順**
```
/kcdk config team red displayName §c赤チーム
```

**期待結果**
- [ ] `show` に `displayName: §c赤チーム` と出る

---

### TC-022: armorColor 設定

**手順**
```
/kcdk config team red armorColor #ef4444
```

**期待結果**
- [ ] ゲーム開始後、赤チームのプレイヤーが赤色の革チェストプレートを装備する

---

### TC-023: respawnCount — 無限リスポーン

**手順**
```
/kcdk config team red stock -1
```

**期待結果**
- [ ] 赤チームプレイヤーは何度死んでもリスポーンできる（→ TC-042 参照）

---

### TC-024: respawnCount — 有限リスポーン

**手順**
```
/kcdk config team red stock 2
```

**期待結果**
- [ ] 赤チームプレイヤーは最大2回リスポーンできる（3回目の死亡でスペクテイター）（→ TC-043 参照）

---

### TC-025: respawnCount — 即スペクテイター

**手順**
```
/kcdk config team red stock 0
```

**期待結果**
- [ ] 赤チームプレイヤーは初回の死亡でスペクテイターになる（→ TC-044 参照）

---

### TC-026: readyLocation 設定・削除

**手順**（待機地点あり）
```
/kcdk config team red readyLocation world 0 64 0 0 0
/kcdk config team red waitingTime 0 0 30
```

**期待結果**
- [ ] ゲーム開始時に待機地点へTPする
- [ ] 30秒間その場から動けない
- [ ] 30秒後にリスポーン地点へTPする（→ TC-032 参照）

**手順**（待機地点削除）
```
/kcdk config team red readyLocation remove
```

**期待結果**
- [ ] ゲーム開始時に即リスポーン地点へTPする（→ TC-033 参照）

---

### TC-027: respawnLocation 設定

**手順**
```
/kcdk config team red respawnLocation world 100 64 0 180 0
```

**期待結果**
- [ ] ゲーム開始後・リスポーン時に (100, 64, 0) にTPする

---

### TC-028: エフェクト追加・削除

**手順**（追加）
```
/kcdk config team red effect add SPEED 999999 1 false
/kcdk config team red effect add RESISTANCE 999999 0 true
```

**期待結果**
- [ ] ゲーム開始後、赤チームに Speed II と Resistance I が付与される（→ TC-034 参照）

**手順**（削除）
```
/kcdk config team red effect remove SPEED
/kcdk config team red effect clear
```

**期待結果**
- [ ] 対象エフェクトが削除される

---

### TC-029: アイテム設定（GUIコマンド）

**前提条件**
- プレイヤーとしてログイン済み
- `kcdk.red` Scoreboardチームが存在する

**手順**
1. `/kcdk config item kcdk.red` を実行する
2. 開いたインベントリ（36スロット）にアイテムを配置する
3. インベントリを閉じる

**期待結果**
- [ ] インベントリを閉じた時 `§aアイテム設定を保存しました。` と表示される
- [ ] `config.yml` にアイテムデータが保存されている
- [ ] ゲーム開始後、赤チームプレイヤーに設定したアイテムが配布される（→ TC-035 参照）

---

## C. ロール設定

### TC-030: ロール追加・削除

**手順**
```
/kcdk config team red role add captain
/kcdk config show
```

**期待結果**
- [ ] `red` チームに `captain` ロールが追加される

```
/kcdk config team red role remove captain
```

- [ ] ロールが削除される

---

### TC-031: ロール respawnLocation（nullで親継承）

**手順**
```
/kcdk config team red role captain respawnLocation world 110 64 0 180 0
```

**期待結果**
- [ ] captainはリスポーン時に (110, 64, 0) にTPする

```
/kcdk config team red role captain respawnLocation remove
```

- [ ] captainは親チーム(red)のリスポーン地点を継承する

---

### TC-032: ロール respawnCount（nullで親継承）

**手順**
```
/kcdk config team red role captain stock 1
```

**期待結果**
- [ ] captainのリスポーン回数が1回になる（2回目の死亡でスペクテイター）

```
/kcdk config team red role captain stock remove
```

- [ ] captainが親チーム(red)のリスポーン回数を継承する

---

### TC-033: extendsEffects — true（エフェクト継承）

**前提条件**
- red チームに `SPEED 1` が設定されている
- captain ロールに `STRENGTH 0` が設定されている

**手順**
```
/kcdk config team red role captain extendsEffects true
```

**期待結果**
- [ ] ゲーム開始後、captainに Speed II と Strength I が両方付与される（→ TC-034 参照）

---

### TC-034: extendsEffects — false（エフェクト非継承）

**手順**
```
/kcdk config team red role captain extendsEffects false
```

**期待結果**
- [ ] ゲーム開始後、captainに Strength I のみ付与される（親チームの Speed は付与されない）

---

### TC-035: extendsItem — true（アイテム継承）

**前提条件**
- red チームにアイテムが設定されている
- captain ロールに別のアイテムが設定されている

**手順**
```
/kcdk config team red role captain extendsItem true
```

**期待結果**
- [ ] ゲーム開始後、captainにチームアイテムとロールアイテムの両方が配布される

---

### TC-036: extendsItem — false（アイテム非継承）

**手順**
```
/kcdk config team red role captain extendsItem false
```

**期待結果**
- [ ] ゲーム開始後、captainにロールアイテムのみ配布される（ロールに未設定の場合はチームアイテム）

---

### TC-037: ロールアイテム設定（GUIコマンド）

**前提条件**
- `kcdk.red.captain` Scoreboardチームが存在する

**手順**
1. `/kcdk config item kcdk.red.captain` を実行する
2. インベントリにアイテムを配置して閉じる

**期待結果**
- [ ] `§aアイテム設定を保存しました。` と表示される
- [ ] ゲーム開始後、captainに設定したアイテムが配布される

---

## D. 終了条件設定

### TC-040: beacon 条件追加

**手順**
```
/kcdk config endCondition add beacon §cビーコン破壊！ world 0 64 0 0 0 100
/kcdk config endCondition list
```

**期待結果**
- [ ] beacon 条件が追加されている
- [ ] hitpoint=100, world/座標が正しい

---

### TC-041: extermination 条件追加

**手順**
```
/kcdk config endCondition add extermination §4全滅！ blue
/kcdk config endCondition list
```

**期待結果**
- [ ] `blue` チームの extermination 条件が追加されている

---

### TC-042: ticket 条件追加

**手順**
```
/kcdk config endCondition add ticket §6チケット消費！ red 50
/kcdk config endCondition list
```

**期待結果**
- [ ] `red` チーム、count=50 の ticket 条件が追加されている

---

### TC-043: endCondition 削除・クリア

**手順**
```
/kcdk config endCondition remove 0
/kcdk config endCondition clear
/kcdk config endCondition list
```

**期待結果**
- [ ] インデックス0の条件が削除される
- [ ] clear 後は終了条件が0件になる

---

## E. 設定の保存・リロード

### TC-050: save → reload の永続化

**手順**
1. 任意の設定を変更する（例: `timeLimit set 0 5 0`）
2. 保存する
   ```
   /kcdk config save
   ```
3. リロードする
   ```
   /kcdk config reload
   ```
4. 確認する
   ```
   /kcdk config show
   ```

**期待結果**
- [ ] リロード後も変更した設定が反映されている

---

### TC-051: import（Base64 JSON一括設定）

**手順**
1. Web設定エディタで設定を作成してBase64文字列を取得する
2. コマンドで適用する
   ```
   /kcdk config import <Base64文字列>
   ```
3. 設定を確認する
   ```
   /kcdk config show
   ```

**期待結果**
- [ ] Web上で設定した内容と一致する
- [ ] チーム・ロール・終了条件・エフェクトすべて反映されている
