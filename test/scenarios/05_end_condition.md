# 05 終了条件

各終了条件タイプとゲーム終了フロー（shutdownCommands・タブ更新）を検証する。

---

## 共通前提条件

- ゲームが RUNNING 状態
- shutdownCommands に `/say ゲーム終了！` が設定されている
- ゲーム終了後 `/kcdk game start` で再開してテストを繰り返す

---

## A. 時間制限（timeLimit）

### TC-300: 制限時間到達でゲーム終了

**設定**
```
/kcdk config timeLimit set 0 0 10
```

**手順**
1. ゲーム開始
2. 10秒待つ

**期待結果**
- [ ] 10秒後に全プレイヤーに `§e§lTimeUp!!!` というタイトルが表示される
- [ ] shutdownCommands が実行される（チャットに「ゲーム終了！」が表示）
- [ ] ゲーム状態が停止になる

---

### TC-301: 制限時間なし（timeLimit=null）

**設定**
```
/kcdk config timeLimit remove
```

**手順**
1. ゲーム開始
2. 制限時間なしで時間が経過することを確認

**期待結果**
- [ ] 時間経過によるゲーム終了が発生しない
- [ ] アクションバーに経過時間が表示される（→ TC-062）

---

## B. ビーコン破壊（beacon）

### TC-302: ビーコン破壊でHP減少 → 0でゲーム終了

**設定**
```
/kcdk config endCondition add beacon §cビーコンが破壊された！ world 0 64 0 0 0 3
```
（hitpoint=3 で即時確認しやすくする）

**手順**
1. ゲーム開始（座標 (0, 64, 0) にビーコンが自動設置される）
2. ビーコンをツールで攻撃する
3. ビーコンの耐久を0にする（3回攻撃）

**期待結果**
- [ ] ゲーム開始時に (0, 64, 0) にビーコンブロックが存在する
- [ ] 攻撃のたびにボスバー（設定時）またはアクションバーのHPが減る
- [ ] HP=0 到達後、全員に `§cビーコンが破壊された！` タイトルが表示される
- [ ] ゲームが終了する

---

### TC-303: ビーコン破壊 HP > 0 ではゲーム終了しない

**設定**
- hitpoint = 100

**手順**
1. ゲーム開始
2. ビーコンを数回攻撃する（HPが減るが0にはしない）

**期待結果**
- [ ] ゲームが終了しない
- [ ] HPが減った分だけ反映されている（ボスバーの長さが変わる）

---

## C. 全滅（extermination）

### TC-304: 対象チームの全員が死亡でゲーム終了

**設定**
- blue チーム: respawnCount = 0（死亡→即スペクテイター）
```
/kcdk config endCondition add extermination §4blueチーム全滅！ blue
```

**ボット構成**
```bash
node index.js --team blue --count 2 --team red --count 1
```

**手順**
1. ゲーム開始
2. 赤チームが青チームの2名を順番にキルする

**期待結果**
- [ ] 1名キル時: ゲームが終了しない
- [ ] 2名目をキルした瞬間: `§4blueチーム全滅！` タイトルが全員に表示される
- [ ] ゲームが終了する

**備考**
> バグ記録 #1「終了条件(全滅)を満たしてもゲームが終了しない」の回帰テスト

---

### TC-305: ロールを含む全滅（extermination with roles）

**設定**
- red チーム: 一般メンバー1名、captain ロール1名（respawnCount=0）
```
/kcdk config endCondition add extermination §4red全滅！ red
```

**ボット構成**
```bash
node index.js --team red --count 1 --team red --captain --count 1 --team blue --count 1
```

**手順**
1. ゲーム開始
2. 青チームが `Bot_red1`（一般）をキルする
3. 青チームが `Bot_redCap1`（captain）をキルする

**期待結果**
- [ ] `Bot_red1` をキルした時点ではゲームが終了しない
- [ ] `Bot_redCap1` をキルした瞬間にゲームが終了する（`kcdk.red.captain` を含む全員が戦線離脱）

**備考**
> バグ記録 #8「red.captainの全滅としているのに該当チームを全滅させてもゲームが終了しない」の回帰テスト

---

## D. チケット制（ticket）

### TC-306: チケット消費でゲーム終了

**設定**
- red チーム: respawnCount = -1（無限リスポーン）
```
/kcdk config endCondition add ticket §6redチケット消費！ red 3
```
（count=3 で即時確認）

**ボット構成**
```bash
node index.js --team red --count 1 --team blue --count 1
```

**手順**
1. ゲーム開始
2. 青チームが赤チームボットを3回キルする

**期待結果**
- [ ] 1回目・2回目のキル: ゲームが終了しない
- [ ] 3回目のキル後: `§6redチケット消費！` タイトルが全員に表示される
- [ ] ゲームが終了する

---

### TC-307: チケット0でゲーム終了（ギリギリ確認）

**設定**
- count=1

**手順**
1. ゲーム開始
2. 赤チームを1回キルする

**期待結果**
- [ ] 1回キルでゲームが終了する

---

## E. 複合条件（composite）

### TC-308: composite AND — 両方満たした場合のみ終了

**設定**
```
endConditions:
  - type: composite
    operator: AND
    message: §d複合達成！
    conditions:
      - type: extermination
        team: blue
        message: blue全滅
      - type: ticket
        team: red
        count: 5
        message: チケット消費
```

**ボット構成**
```bash
node index.js --team red --count 1 --team blue --count 1
```

**手順（ケースA: blue 全滅のみ、チケット未消費）**
1. ゲーム開始
2. blue ボットをキルする（blue 全滅）

**期待結果**
- [ ] ゲームが終了しない（チケット条件が未達成）

**手順（ケースB: 両方達成）**
1. ゲームを再開
2. red チームを5回キル（チケット消費）
3. blue チームを全滅させる

**期待結果**
- [ ] `§d複合達成！` タイトルが表示されゲームが終了する

---

### TC-309: composite OR — 片方で終了

**設定**
```
endConditions:
  - type: composite
    operator: OR
    message: §dどちらか達成！
    conditions:
      - type: extermination
        team: blue
        message: blue全滅
      - type: extermination
        team: red
        message: red全滅
```

**手順**
1. ゲーム開始
2. blue チームのみ全滅させる

**期待結果**
- [ ] `§dどちらか達成！` タイトルでゲームが終了する（red は全滅していなくても）

---

## F. ゲーム終了フロー

### TC-310: shutdownCommands 実行

**設定**
```
shutdownCommands:
  - /say ゲーム終了！
  - /tp @a 0 64 0
```

**手順**
1. 任意の終了条件を達成する

**期待結果**
- [ ] `ゲーム終了！` チャットが表示される
- [ ] 全員が (0, 64, 0) にTPする
- [ ] コマンドが順番に実行される

---

### TC-311: ゲーム終了後のタブ表示更新

**手順**
1. ゲームを実行し、K/D を発生させる（例: 2Kill/1Death）
2. ゲームを終了させる
3. タブリストを確認する（TABキー）

**期待結果**
- [ ] タブのプレイヤー名が `<MCID> k:2/d:1` 形式になっている
- [ ] ゲーム終了後に正しく反映されている

**備考**
> バグ記録 #7「ゲーム終了後にTabにk/d表示がない」の回帰テスト
