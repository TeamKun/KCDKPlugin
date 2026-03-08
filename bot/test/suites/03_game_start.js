'use strict';

const { TestSuite } = require('../runner');
const h = require('../helpers');
const { COORDS } = require('../game-context');

const C = COORDS;

module.exports = function register(ctx) {
  const suite = new TestSuite('03 ゲーム開始フロー');

  suite.afterEach(async () => { await ctx.cleanup(); });

  // ─── TP 確認 ─────────────────────────────────────────────────

  // TC-100: 待機地点なし → 即 respawnLocation へTP
  suite.test('TC-100: readyLocation なし → ゲーム開始時に即 respawnLocation へTP', async () => {
    await ctx.setupConfig({
      gamemode: 'ADVENTURE',
      teams: [
        { name: 'red',  respawnCount: -1, respawnLocation: C.redSpawn  },
        { name: 'blue', respawnCount: -1, respawnLocation: C.blueSpawn },
      ],
    });

    const botRed  = await ctx.addBot('T100R', 'red');
    const botBlue = await ctx.addBot('T100B', 'blue');

    await ctx.startGame();

    await Promise.all([
      h.waitForPosition(botRed,  C.redSpawn,  5, 10000),
      h.waitForPosition(botBlue, C.blueSpawn, 5, 10000),
    ]);
  });

  // TC-101: 待機地点あり → 待機 → waitingTime 経過 → respawnLocation へTP
  suite.test('TC-101: readyLocation あり → 待機後に respawnLocation へTP', async () => {
    const WAIT_SEC = 5;
    await ctx.setupConfig({
      gamemode: 'ADVENTURE',
      teams: [{
        name: 'red', respawnCount: -1,
        readyLocation:   { ...C.redReady,  world: 'world' },
        waitingTime:     { hours: 0, minutes: 0, seconds: WAIT_SEC },
        respawnLocation: { ...C.redSpawn,  world: 'world' },
      }],
    });

    const botRed = await ctx.addBot('T101R', 'red');
    await ctx.startGame();

    // まず待機地点にTPしているか
    await h.waitForPosition(botRed, C.redReady, 5, 8000);

    // waitingTime 経過後に respawnLocation へTP
    await h.waitForPosition(botRed, C.redSpawn, 5, (WAIT_SEC + 5) * 1000);
  });

  // TC-102: チームごとの待機時間が独立して動く
  suite.test('TC-102: red 10秒 / blue 3秒 の待機時間が独立して動く', async () => {
    await ctx.setupConfig({
      gamemode: 'ADVENTURE',
      teams: [
        {
          name: 'red', respawnCount: -1,
          readyLocation:   { ...C.redReady,  world: 'world' },
          waitingTime:     { hours: 0, minutes: 0, seconds: 10 },
          respawnLocation: { ...C.redSpawn,  world: 'world' },
        },
        {
          name: 'blue', respawnCount: -1,
          readyLocation:   { ...C.blueReady, world: 'world' },
          waitingTime:     { hours: 0, minutes: 0, seconds: 3 },
          respawnLocation: { ...C.blueSpawn, world: 'world' },
        },
      ],
    });

    const botRed  = await ctx.addBot('T102R', 'red');
    const botBlue = await ctx.addBot('T102B', 'blue');
    await ctx.startGame();

    // blue が 3+3 秒以内に respawnLocation へTP、red はまだ readyLocation
    await h.waitForPosition(botBlue, C.blueSpawn, 5, 9000);

    // この時点で red はまだ待機中のはず
    const redPos = botRed.entity.position;
    const distToReady = Math.sqrt(
      (redPos.x - C.redReady.x) ** 2 + (redPos.z - C.redReady.z) ** 2
    );
    h.assertTrue(distToReady < 5,
      `red はまだ readyLocation 付近のはず。距離: ${distToReady.toFixed(1)}`);

    // red も最終的に respawnLocation へ
    await h.waitForPosition(botRed, C.redSpawn, 5, 15000);
  });

  // TC-103: startupCommands が実行される
  suite.test('TC-103: startupCommands がゲーム開始時に実行される', async () => {
    const { x: rx, y: ry, z: rz } = C.redSpawn;
    const json = JSON.stringify({
      a: 'ADVENTURE',
      d: ['say KCDK_STARTUP_TEST'],
      f: [{ l: 'red', o: -1, q: { t: 'world', u: rx, v: ry, w: rz, A: 0, B: 0 } }],
    });
    const base64 = Buffer.from(json).toString('base64');
    const importMsg = h.waitForMessage(ctx.admin, 'imported from Base64', 5000);
    ctx.admin.chat(`/kcdk config import ${base64}`);
    await importMsg;

    await ctx.addBot('T103R', 'red');

    const msg = h.waitForMessage(ctx.admin, 'KCDK_STARTUP_TEST', 8000);
    await ctx.startGame();
    await msg;
  });

  // TC-111: ゲーム開始時にビーコンが設置される
  suite.test('TC-111: ゲーム開始時に beacon 終了条件の座標にビーコンブロックが設置される', async () => {
    const bLoc = C.beacon;
    await ctx.setupConfig({
      gamemode: 'SURVIVAL',
      teams: [{ name: 'red', respawnCount: -1, respawnLocation: C.redSpawn }],
      endConditions: [{
        type: 'beacon', message: 'BeaconTest',
        location: bLoc, hitpoint: 10,
      }],
    });
    await ctx.addBot('T111R', 'red');
    await ctx.startGame();
    await h.sleep(1500);

    // admin を beacon 座標の真上に TP して chunk をロードさせる
    await ctx.tp(ctx.opts.adminName, bLoc.x, bLoc.y + 5, bLoc.z);
    await h.sleep(2000);

    const pos = ctx.admin.entity.position;
    const beaconVec = pos.offset(bLoc.x - pos.x, bLoc.y - pos.y, bLoc.z - pos.z);
    const block = ctx.admin.blockAt(beaconVec);
    h.assertTrue(
      block && block.name === 'beacon',
      `ビーコンブロックが期待位置にない。実際のブロック: ${block ? block.name : 'null'}`
    );
  });

  // TC-112: 途中参加 — ゲーム中ログインで自動チーム検出
  suite.test('TC-112: ゲーム中ログインしたプレイヤーが Scoreboard チームから自動検出されリスポーン地点へTP', async () => {
    await ctx.setupConfig({
      gamemode: 'ADVENTURE',
      teams: [{ name: 'blue', respawnCount: -1, respawnLocation: C.blueSpawn }],
    });

    await ctx.addBot('T112Anc', 'blue');
    await ctx.startGame();
    await h.sleep(500);

    await ctx.cmd('/team join kcdk.blue T112Late');
    await h.sleep(400);

    const lateBot = await h.spawnBot('T112Late', ctx.opts);
    ctx.bots.set('T112Late', lateBot);
    lateBot.on('death', () => setTimeout(() => { try { lateBot.respawn(); } catch (_) {} }, 600));
    lateBot.on('error', e => console.warn('[T112Late] error:', e.message));

    await h.waitForPosition(lateBot, C.blueSpawn, 5, 12000);
  });

  // TC-113: /kcdk game stop でゲームが強制終了する
  suite.test('TC-113: /kcdk game stop で強制終了タイトルが表示される', async () => {
    await ctx.setupConfig({
      gamemode: 'ADVENTURE',
      teams: [{ name: 'red', respawnCount: -1, respawnLocation: C.redSpawn }],
    });
    const botRed = await ctx.addBot('T113R', 'red');
    await ctx.startGame();
    await h.waitForPosition(botRed, C.redSpawn, 5, 10000);

    const titlePromise = h.waitForTitle(botRed, /強制終了|TimeUp|Stop/i, 8000)
      .catch(() => null);
    const msgPromise = h.waitForMessage(ctx.admin, /強制終了|ゲームが終了|game.*stop/i, 8000)
      .catch(() => null);
    await ctx.cmd('/kcdk game stop');
    const [t, m] = await Promise.all([titlePromise, msgPromise]);
    h.assertTrue(t !== null || m !== null,
      'game stop タイトルもブロードキャストメッセージも受信されなかった');
  });

  return suite;
};
