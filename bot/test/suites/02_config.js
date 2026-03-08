'use strict';

const { TestSuite } = require('../runner');
const h = require('../helpers');

module.exports = function register(ctx) {
  const suite = new TestSuite('02 設定コマンド');

  suite.afterEach(async () => { await ctx.cleanup(); });

  // ─── A. グローバル設定 ────────────────────────────────────────

  suite.test('TC-010: gamemode ADVENTURE が設定される', async () => {
    const msg = h.waitForMessage(ctx.admin, 'Gamemode set to', 3000);
    ctx.admin.chat('/kcdk config gamemode ADVENTURE');
    await msg;
    const cfg = await h.parseConfigShow(ctx.admin);
    h.assertEqual(cfg['Gamemode'], 'ADVENTURE', 'Gamemode');
  });

  suite.test('TC-012: timeLimit set が設定される', async () => {
    await ctx.cmd('/kcdk config timeLimit set 0 10 0');
    const cfg = await h.parseConfigShow(ctx.admin);
    h.assertTrue(cfg['Time Limit']?.includes('10'), `Time Limit: ${cfg['Time Limit']}`);
  });

  suite.test('TC-013: timeLimit remove で none になる', async () => {
    await ctx.cmd('/kcdk config timeLimit set 0 5 0');
    await ctx.cmd('/kcdk config timeLimit remove');
    const cfg = await h.parseConfigShow(ctx.admin);
    h.assertTrue(cfg['Time Limit'] === 'none' || cfg['Time Limit'] === 'None',
      `Time Limit: ${cfg['Time Limit']}`);
  });

  suite.test('TC-014: bossbar set が設定される', async () => {
    const msg = h.waitForMessage(ctx.admin, 'Bossbar MCID set to', 3000);
    ctx.admin.chat('/kcdk config bossbar set TestMobId');
    await msg;
    const cfg = await h.parseConfigShow(ctx.admin);
    h.assertTrue(cfg['Bossbar']?.includes('TestMobId'), `Bossbar: ${cfg['Bossbar']}`);
    await ctx.cmd('/kcdk config bossbar remove');
  });

  suite.test('TC-015: disableHunger true が設定される', async () => {
    await ctx.cmd('/kcdk config disableHunger true');
    const cfg = await h.parseConfigShow(ctx.admin);
    h.assertEqual(cfg['Disable Hunger'], 'true', 'Disable Hunger');
  });

  // TC-017/018: startupCommands・shutdownCommands が JSON import で設定される
  suite.test('TC-017/018: startupCommands・shutdownCommands がJSONインポートで設定される', async () => {
    const json = JSON.stringify({
      a: 'ADVENTURE',
      d: ['/say startup_test'],   // startupCommands
      e: ['/say shutdown_test'],  // shutdownCommands
    });
    const base64 = Buffer.from(json).toString('base64');
    const msg = h.waitForMessage(ctx.admin, 'imported from Base64', 5000);
    ctx.admin.chat(`/kcdk config import ${base64}`);
    await msg;

    const cfg = await h.parseConfigShow(ctx.admin);
    h.assertTrue(parseInt(cfg['Startup Commands'] || '0') >= 1,
      `Startup Commands: ${cfg['Startup Commands']}`);
    h.assertTrue(parseInt(cfg['Shutdown Commands'] || '0') >= 1,
      `Shutdown Commands: ${cfg['Shutdown Commands']}`);
  });

  // ─── B. チーム設定 ────────────────────────────────────────────

  suite.test('TC-020: チーム追加・削除', async () => {
    const added = h.waitForMessage(ctx.admin, 'Team added', 3000);
    ctx.admin.chat('/kcdk config team add testteam');
    await added;

    let cfg = await h.parseConfigShow(ctx.admin);
    const countAfterAdd = parseInt(cfg['Teams'] || '0');
    h.assertTrue(countAfterAdd >= 1, `Teams after add: ${countAfterAdd}`);

    await ctx.cmd('/kcdk config team remove testteam');
    cfg = await h.parseConfigShow(ctx.admin);
    const countAfterRemove = parseInt(cfg['Teams'] || '0');
    h.assertTrue(countAfterRemove < countAfterAdd,
      `Teams count should decrease: ${countAfterAdd} → ${countAfterRemove}`);
  });

  suite.test('TC-021: displayName が設定される', async () => {
    await ctx.cmd('/kcdk config team clear');
    await ctx.cmd('/kcdk config team add red');
    const msg = h.waitForMessage(ctx.admin, 'Display name set to', 3000);
    ctx.admin.chat('/kcdk config team red displayName RedTeamTest');
    await msg;
  });

  suite.test('TC-023: respawnCount が設定される', async () => {
    await ctx.cmd('/kcdk config team clear');
    await ctx.cmd('/kcdk config team add red');
    const msg = h.waitForMessage(ctx.admin, 'Respawn count set to', 3000);
    ctx.admin.chat('/kcdk config team red respawnCount -1');
    await msg;
  });

  suite.test('TC-026: readyLocation が設定・削除される', async () => {
    await ctx.cmd('/kcdk config team clear');
    await ctx.cmd('/kcdk config team add red');
    const set = h.waitForMessage(ctx.admin, 'Ready location set', 3000);
    ctx.admin.chat('/kcdk config team red readyLocation world 0 64 0 0 0 0 0 30');
    await set;

    const removed = h.waitForMessage(ctx.admin, 'Ready location removed', 3000);
    ctx.admin.chat('/kcdk config team red readyLocation remove');
    await removed;
  });

  suite.test('TC-027: respawnLocation が設定される', async () => {
    await ctx.cmd('/kcdk config team clear');
    await ctx.cmd('/kcdk config team add red');
    const msg = h.waitForMessage(ctx.admin, 'Respawn location set', 3000);
    ctx.admin.chat('/kcdk config team red respawnLocation world 100 64 0 0 0');
    await msg;
  });

  // ─── C. ロール設定 ────────────────────────────────────────────

  suite.test('TC-030: ロール追加・削除', async () => {
    await ctx.cmd('/kcdk config team clear');
    await ctx.cmd('/kcdk config team add red');

    const added = h.waitForMessage(ctx.admin, 'Role added', 3000);
    ctx.admin.chat('/kcdk config team red role add captain');
    await added;

    const removed = h.waitForMessage(ctx.admin, 'Role removed', 3000);
    ctx.admin.chat('/kcdk config team red role remove captain');
    await removed;
  });

  suite.test('TC-031: ロール respawnLocation が設定される', async () => {
    await ctx.cmd('/kcdk config team clear');
    await ctx.cmd('/kcdk config team add red');
    await ctx.cmd('/kcdk config team red role add captain');

    const set = h.waitForMessage(ctx.admin, 'Role respawn location set', 3000);
    ctx.admin.chat('/kcdk config team red role captain respawnLocation world 110 64 0 0 0');
    await set;
  });

  suite.test('TC-032: ロール respawnCount の設定と null 継承', async () => {
    await ctx.cmd('/kcdk config team clear');
    await ctx.cmd('/kcdk config team add red');
    await ctx.cmd('/kcdk config team red role add captain');

    const set = h.waitForMessage(ctx.admin, 'Role respawn count set to', 3000);
    ctx.admin.chat('/kcdk config team red role captain respawnCount 1');
    await set;

    const removed = h.waitForMessage(ctx.admin, 'inherit', 5000);
    ctx.admin.chat('/kcdk config team red role captain respawnCount remove');
    await removed;
  });

  suite.test('TC-035: extendsItem=true が設定される', async () => {
    await ctx.cmd('/kcdk config team clear');
    await ctx.cmd('/kcdk config team add red');
    await ctx.cmd('/kcdk config team red role add captain');
    const msg = h.waitForMessage(ctx.admin, 'extendsItem set to', 3000);
    ctx.admin.chat('/kcdk config team red role captain extendsItem true');
    await msg;
  });

  // ─── D. 終了条件設定 ──────────────────────────────────────────

  suite.test('TC-040: beacon 終了条件が追加される', async () => {
    await ctx.cmd('/kcdk config endCondition clear');
    await ctx.cmd('/kcdk config endCondition add beacon TestMsg world 0 64 0 0 0 100');
    const cfg = await h.parseConfigShow(ctx.admin);
    const count = parseInt(cfg['End Conditions'] || '0');
    h.assertTrue(count >= 1, `End Conditions: ${count}`);
  });

  suite.test('TC-041: extermination 終了条件が追加される', async () => {
    await ctx.cmd('/kcdk config team clear');
    await ctx.cmd('/kcdk config team add blue');
    await ctx.cmd('/kcdk config endCondition clear');
    await ctx.cmd('/kcdk config endCondition add extermination TestMsg blue');
    const cfg = await h.parseConfigShow(ctx.admin);
    const count = parseInt(cfg['End Conditions'] || '0');
    h.assertTrue(count >= 1, `End Conditions: ${count}`);
  });

  suite.test('TC-042: ticket 終了条件が追加される', async () => {
    await ctx.cmd('/kcdk config team clear');
    await ctx.cmd('/kcdk config team add red');
    await ctx.cmd('/kcdk config endCondition clear');
    await ctx.cmd('/kcdk config endCondition add ticket TestMsg red 50');
    const cfg = await h.parseConfigShow(ctx.admin);
    const count = parseInt(cfg['End Conditions'] || '0');
    h.assertTrue(count >= 1, `End Conditions: ${count}`);
  });

  suite.test('TC-043: endCondition 削除・クリア', async () => {
    await ctx.cmd('/kcdk config team clear');
    await ctx.cmd('/kcdk config team add red');
    await ctx.cmd('/kcdk config endCondition clear');
    await ctx.cmd('/kcdk config endCondition add extermination Msg1 red');
    await ctx.cmd('/kcdk config endCondition add extermination Msg2 red');
    await ctx.cmd('/kcdk config endCondition remove 0');
    let cfg = await h.parseConfigShow(ctx.admin);
    h.assertTrue(parseInt(cfg['End Conditions']) === 1, `after remove: ${cfg['End Conditions']}`);
    await ctx.cmd('/kcdk config endCondition clear');
    cfg = await h.parseConfigShow(ctx.admin);
    h.assertTrue(parseInt(cfg['End Conditions']) === 0, `after clear: ${cfg['End Conditions']}`);
  });

  // ─── E. 保存・リロード ────────────────────────────────────────

  suite.test('TC-050: save → reload でゲームモード設定が永続化される', async () => {
    await ctx.cmd('/kcdk config gamemode SURVIVAL');
    const saved = h.waitForMessage(ctx.admin, 'saved to config', 3000);
    ctx.admin.chat('/kcdk config save');
    await saved;

    const reloaded = h.waitForMessage(ctx.admin, 'reloaded from config', 3000);
    ctx.admin.chat('/kcdk config reload');
    await reloaded;

    const cfg = await h.parseConfigShow(ctx.admin);
    h.assertEqual(cfg['Gamemode'], 'SURVIVAL', 'Gamemode after reload');
    // 戻す
    await ctx.cmd('/kcdk config gamemode ADVENTURE');
    await ctx.cmd('/kcdk config save');
  });

  suite.test('TC-051: import (Base64 JSON) でチーム・設定が一括適用される', async () => {
    const json = JSON.stringify({
      a: 'ADVENTURE',
      f: [{ l: 'red', o: -1, q: { t: 'world', u: 100, v: 64, w: 0, A: 0, B: 0 } }],
    });
    const base64 = Buffer.from(json).toString('base64');

    const msg = h.waitForMessage(ctx.admin, 'imported from Base64', 5000);
    ctx.admin.chat(`/kcdk config import ${base64}`);
    await msg;

    const cfg = await h.parseConfigShow(ctx.admin);
    h.assertEqual(cfg['Gamemode'], 'ADVENTURE', 'Gamemode after import');
    h.assertTrue(parseInt(cfg['Teams']) >= 1, `Teams after import: ${cfg['Teams']}`);
  });

  return suite;
};
