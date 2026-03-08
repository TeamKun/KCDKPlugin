'use strict';

const { TestSuite } = require('../runner');
const h = require('../helpers');
const { COORDS } = require('../game-context');

module.exports = function register(ctx) {
  const suite = new TestSuite('06 UI・表示');

  suite.afterEach(async () => { await ctx.cleanup(); });

  // ─── A. アクションバー ────────────────────────────────────────

  /**
   * TC-062: チームごとの生存人数がアクションバーに表示される
   * チーム名が含まれていると判断できれば十分とする
   */
  suite.test('TC-062: アクションバーにチーム情報が表示される', async () => {
    await ctx.setupConfig({
      gamemode: 'ADVENTURE',
      teams: [
        { name: 'red',  respawnCount: 0, respawnLocation: COORDS.redSpawn  },
        { name: 'blue', respawnCount: 0, respawnLocation: COORDS.blueSpawn },
      ],
    });
    await ctx.addBots([
      { name: 'Bot_red1',  team: 'red' },
      { name: 'Bot_red2',  team: 'red' },
      { name: 'Bot_blue1', team: 'blue' },
      { name: 'Bot_blue2', team: 'blue' },
      { name: 'Bot_blue3', team: 'blue' },
    ]);
    await ctx.startGame();

    // アクションバーに両チーム名が含まれていると判断できれば合格
    const msg = await h.waitForActionBar(ctx.admin, /red.*blue|blue.*red/, 8000);
    h.assertTrue(/red/i.test(msg) && /blue/i.test(msg),
      `アクションバーに両チーム名が含まれない: "${msg}"`);
  });

  // ─── B. ボスバー ──────────────────────────────────────────────

  /**
   * TC-064: ボスバー非表示（bossbar=null）
   */
  suite.test('TC-064: bossbar=null のときボスバーが表示されない', async () => {
    await ctx.setupConfig({
      gamemode: 'ADVENTURE',
      teams: [
        { name: 'red',  respawnCount: -1, respawnLocation: COORDS.redSpawn  },
        { name: 'blue', respawnCount: -1, respawnLocation: COORDS.blueSpawn },
      ],
    });
    await ctx.cmd('/kcdk config bossbar remove');

    await ctx.addBots([
      { name: 'Bot_red1',  team: 'red' },
      { name: 'Bot_blue1', team: 'blue' },
    ]);
    await ctx.startGame();
    await h.sleep(1500);

    const bossBars = Object.values(ctx.admin.bossBars || {});
    h.assertEqual(bossBars.length, 0, `ボスバーが表示されている (${bossBars.length}件)`);
  });

  // ─── C. タイトル演出 ──────────────────────────────────────────

  /**
   * TC-076: ゲーム終了タイトルが全員に表示される
   */
  suite.test('TC-076: ゲーム終了タイトルが全員に表示される', async () => {
    await ctx.setupConfig({
      gamemode: 'ADVENTURE',
      teams: [
        { name: 'red',  respawnCount: 0, respawnLocation: COORDS.redSpawn  },
        { name: 'blue', respawnCount: 0, respawnLocation: COORDS.blueSpawn },
      ],
      endConditions: [{ type: 'extermination', message: 'EndTestMsg', team: 'blue' }],
    });
    const bots = await ctx.addBots([
      { name: 'Bot_red1',  team: 'red' },
      { name: 'Bot_blue1', team: 'blue' },
    ]);
    await ctx.startGame();

    const adminTitle = h.waitForTitle(ctx.admin,          /EndTestMsg/i, 10000);
    const botTitle   = h.waitForTitle(bots['Bot_red1'],   /EndTestMsg/i, 10000);

    await ctx.kill('Bot_blue1');

    await Promise.all([adminTitle, botTitle]);
  });

  return suite;
};
