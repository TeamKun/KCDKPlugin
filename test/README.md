# KCDK シナリオテスト

ゲームの設定からゲーム終了まで、全機能を網羅したシナリオテスト集。

---

## ディレクトリ構成

```
test/
├── README.md              ← このファイル（概要・凡例）
└── scenarios/
    ├── 01_setup.md        セットアップ
    ├── 02_config.md       設定コマンド全般
    ├── 03_game_start.md   ゲーム開始フロー
    ├── 04_combat.md       戦闘・リスポーン
    ├── 05_end_condition.md 終了条件
    └── 06_ui.md           UI・表示
```

---

## 凡例

### テスト項目の書き方

```markdown
### TC-XXX: テスト名

**前提条件**
- サーバー起動済み
- OPプレイヤーが1名いる

**ボット構成** *(手動テストのみの場合は省略)*
```bash
node index.js --team red --count 2 --team blue --count 2
```

**手順**
1. コマンドや操作の説明

**期待結果**
- [ ] 確認すべき事項A
- [ ] 確認すべき事項B

**備考** *(省略可)*
```

### ステータス記号

| 記号 | 意味 |
|------|------|
| `[ ]` | 未確認 |
| `[x]` | 確認済み（Pass） |
| `[!]` | 確認済み（Fail / バグあり） |
| `[-]` | スキップ（対象外） |

---

## テスト実行の前提条件

- Minecraft サーバー (Paper 1.21.11) が `localhost:25565` で起動している
- `online-mode=false`（ボット用）
- テスト実行者は OP 権限を持つプレイヤー、またはコンソールで操作する
- `bot/` ディレクトリで `npm install` 済み

---

## シナリオ一覧

| ID | ファイル | 概要 |
|----|---------|------|
| 01 | [01_setup.md](scenarios/01_setup.md) | セットアップとScoreboardチーム初期化 |
| 02 | [02_config.md](scenarios/02_config.md) | 全設定項目の設定・検証 |
| 03 | [03_game_start.md](scenarios/03_game_start.md) | ゲーム開始から戦闘開始までのフロー |
| 04 | [04_combat.md](scenarios/04_combat.md) | 戦闘・FriendlyFire・リスポーン |
| 05 | [05_end_condition.md](scenarios/05_end_condition.md) | 終了条件の各タイプ |
| 06 | [06_ui.md](scenarios/06_ui.md) | アクションバー・ボスバー・タブ・タイトル |
