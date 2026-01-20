# KCDK Config Editor

KCDK Pluginの設定を視覚的に編集できるWebエディターです。

## 使い方

1. [エディターを開く](https://[YOUR-USERNAME].github.io/[YOUR-REPO-NAME]/editor/)
2. フォームで設定を編集
3. 「📋 コマンドをコピー」ボタンでMinecraftコマンドをコピー
4. Minecraftサーバーで `/kcdk config import <json>` を実行

## 機能

- 📝 **ビジュアル編集**: フォーム形式で簡単に設定を編集
- 📋 **コマンドコピー**: ワンクリックでインポートコマンドをコピー
- 🔗 **共有URL**: 設定をURLに埋め込んで共有可能
- 🔄 **リセット**: デフォルト設定に戻す

## 設定項目

- ゲームモード
- 時間制限
- チーム設定（名前、表示名、色、座標など）
- ロール設定（チーム内の特殊な役職）
- エフェクト（ポーション効果）
- 終了条件（TimeLimit、Beacon、Extermination、Ticket）

## GitHub Pagesでの公開方法

1. このリポジトリをGitHubにプッシュ
2. GitHubリポジトリの Settings > Pages
3. Source を `main` ブランチの `/editor` フォルダに設定
4. 公開されたURLにアクセス
