'use strict';

const { TestSuite } = require('../runner');
const h = require('../helpers');

module.exports = function register(ctx) {
  const suite = new TestSuite('01 セットアップ');

  suite.afterEach(async () => { await ctx.cleanup(); });

  // ──────────────────────────────────────────────────────────────

  // TC-002: /kcdk setup コマンド
  suite.test('TC-002: /kcdk setup でセットアップ完了メッセージが返る', async () => {
    const msg = h.waitForMessage(ctx.admin, 'セットアップが完了しました', 5000);
    await ctx.cmd('/kcdk setup');
    await msg; // メッセージが届けば OK
  });

  // TC-004: ボット参加と Scoreboard チーム割り当て
  suite.test('TC-004: ボット接続後に /team join でチーム参加できる', async () => {
    // addBot 内で /team join を実行している
    await ctx.addBot('TC004Bot', 'red');

    // /team list kcdk.red の出力に TC004Bot が含まれることを確認
    const lines = await h.runAndCapture(ctx.admin, '/team list kcdk.red', 1500);
    const combined = lines.join(' ');
    h.assertTrue(
      combined.includes('TC004Bot'),
      `TC004Bot が kcdk.red に見つからない。出力: ${lines.slice(0, 5).join(' | ')}`
    );
  });

  return suite;
};
