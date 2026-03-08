'use strict';

const { TestSuite } = require('../runner');
const h = require('../helpers');
const { COORDS } = require('../game-context');

const C = COORDS;

module.exports = function register(ctx) {
  const suite = new TestSuite('05 終了条件');

  suite.afterEach(async () => { await ctx.cleanup(); });

  // ─── A. 時間制限 ─────────────────────────────────────────────

  // TC-300: 制限時間到達でゲーム終了
  suite.test('TC-300: 制限時間 8秒 到達で "TimeUp" タイトルが表示される', async () => {
    await ctx.setupConfig({
      gamemode: 'ADVENTURE',
      timeLimit: { hours: 0, minutes: 0, seconds: 8 },
      teams: [{ name: 'red', respawnCount: -1, respawnLocation: C.redSpawn }],
    });
    const botRed = await ctx.addBot('T300R', 'red');
    await ctx.startGame();
    await h.waitForPosition(botRed, C.redSpawn, 5, 10000);

    const titlePromise = h.waitForTitle(botRed, /TimeUp|時間切れ/i, 20000);
    await titlePromise;
  });

  // TC-301: timeLimit=null でゲームが時間経過で終了しない
  suite.test('TC-301: timeLimit=null でゲームが時間経過で自動終了しない', async () => {
    await ctx.setupConfig({
      gamemode: 'ADVENTURE',
      timeLimit: null,
      teams: [{ name: 'red', respawnCount: -1, respawnLocation: C.redSpawn }],
    });
    await ctx.addBot('T301R', 'red');
    await ctx.startGame();

    let ended = false;
    const admin = ctx.admin;
    function titleHandler(text) {
      const s = h.stripColors(text.toString());
      if (/TimeUp|時間切れ/i.test(s)) ended = true;
    }
    admin.on('title', titleHandler);
    await h.sleep(6000);
    admin.off('title', titleHandler);

    h.assertFalse(ended, 'timeLimit=null なのにゲームが時間切れで終了した');
  });

  // ─── B. ビーコン破壊 ─────────────────────────────────────────

  // TC-302: ビーコン破壊 HP=1 でゲーム終了
  suite.test('TC-302: ビーコン破壊 (hitpoint=1) でゲームが終了する', async () => {
    const bLoc = C.beacon;
    await ctx.setupConfig({
      gamemode: 'SURVIVAL',
      teams: [
        { name: 'red',  respawnCount: -1, respawnLocation: C.redSpawn  },
        { name: 'blue', respawnCount: -1, respawnLocation: C.blueSpawn },
      ],
      endConditions: [{
        type: 'beacon', message: 'BeaconDestroyed',
        location: bLoc, hitpoint: 1,
      }],
    });
    const botRed = await ctx.addBot('T302R', 'red');
    await ctx.addBot('T302B', 'blue');

    // ゲーム開始前にビーコン周辺を整地
    await ctx.cmd(`/fill ${bLoc.x - 2} ${bLoc.y - 1} ${bLoc.z - 2} ${bLoc.x + 2} ${bLoc.y - 1} ${bLoc.z + 2} minecraft:stone`);
    await ctx.cmd(`/fill ${bLoc.x - 2} ${bLoc.y} ${bLoc.z - 2} ${bLoc.x + 2} ${bLoc.y + 5} ${bLoc.z + 2} minecraft:air`);

    await ctx.startGame();
    await h.sleep(1500); // ビーコン設置待ち

    // ボットをビーコン隣にTP & ツールを持たせる
    await ctx.tp('T302R', bLoc.x + 1, bLoc.y, bLoc.z);
    await ctx.cmd('/give T302R minecraft:netherite_pickaxe 1');
    await h.sleep(1500); // チャンクロード + アイテム同期待ち

    const pickaxe = botRed.inventory.slots.find(s => s && s.name === 'netherite_pickaxe');
    if (pickaxe) await botRed.equip(pickaxe, 'hand');

    // admin をビーコン直上に TP してチャンクを確実にロード → blockAt で検証
    await ctx.tp(ctx.opts.adminName, bLoc.x, bLoc.y + 1, bLoc.z);
    await h.sleep(500);
    const aPos = ctx.admin.entity.position;
    const beaconVec = aPos.offset(bLoc.x - aPos.x, bLoc.y - aPos.y, bLoc.z - aPos.z);
    const block = ctx.admin.blockAt(beaconVec);
    if (!block || block.name !== 'beacon') {
      throw new Error(`ビーコンブロックが見つからない: ${block ? block.name : 'null'}`);
    }

    // botRed 側でも同座標を blockAt で取得して掘る
    const rPos = botRed.entity.position;
    const rVec = rPos.offset(bLoc.x - rPos.x, bLoc.y - rPos.y, bLoc.z - rPos.z);
    const rBlock = botRed.blockAt(rVec);
    if (!rBlock || rBlock.name !== 'beacon') {
      throw new Error('botRed 側でビーコンが取得できない');
    }

    const titlePromise = h.waitForTitle(botRed, 'BeaconDestroyed', 20000);
    await botRed.dig(rBlock);
    await titlePromise;
  });

  // ─── C. 全滅 ─────────────────────────────────────────────────

  // TC-304: 対象チームの全員が死亡でゲーム終了
  suite.test('TC-304: blue 全滅 (extermination) でゲームが終了する', async () => {
    await ctx.setupConfig({
      gamemode: 'ADVENTURE',
      teams: [
        { name: 'red',  respawnCount: -1, respawnLocation: C.redSpawn  },
        { name: 'blue', respawnCount:  0, respawnLocation: C.blueSpawn },
      ],
      endConditions: [{ type: 'extermination', message: 'BlueExterminated', team: 'blue' }],
    });
    await ctx.addBot('T304R', 'red');
    await ctx.addBot('T304B1', 'blue');
    await ctx.addBot('T304B2', 'blue');
    await ctx.startGame();
    await h.sleep(1000);

    const titlePromise = h.waitForTitle(ctx.admin, 'BlueExterminated', 20000);

    await ctx.kill('T304B1');
    await h.sleep(800);
    await ctx.kill('T304B2');

    await titlePromise;
  });

  // TC-305: ロールを含む全滅 (extermination with roles) — バグ #8 回帰テスト
  suite.test('TC-305: red (ベース+captain) 全滅でゲームが終了する [バグ#8 回帰]', async () => {
    await ctx.setupConfig({
      gamemode: 'ADVENTURE',
      teams: [
        {
          name: 'red', respawnCount: 0, respawnLocation: C.redSpawn,
          roles: [{ name: 'captain', respawnLocation: C.redSpawn }],
        },
        { name: 'blue', respawnCount: -1, respawnLocation: C.blueSpawn },
      ],
      endConditions: [{ type: 'extermination', message: 'RedExterminated', team: 'red' }],
    });
    await ctx.addBot('T305RBase', 'red');
    await ctx.addBot('T305RCap',  'red', true);
    await ctx.addBot('T305B',     'blue');
    await ctx.startGame();
    await h.sleep(1000);

    const titlePromise = h.waitForTitle(ctx.admin, 'RedExterminated', 25000);

    await ctx.kill('T305RBase');
    await h.sleep(800);
    await ctx.kill('T305RCap');

    await titlePromise;
  });

  // ─── D. チケット制 ────────────────────────────────────────────

  // TC-306: チケット消費でゲーム終了
  suite.test('TC-306: red チケット 3回消費でゲームが終了する', async () => {
    await ctx.setupConfig({
      gamemode: 'ADVENTURE',
      teams: [
        { name: 'red',  respawnCount: -1, respawnLocation: C.redSpawn  },
        { name: 'blue', respawnCount: -1, respawnLocation: C.blueSpawn },
      ],
      endConditions: [{ type: 'ticket', message: 'RedTicketEmpty', team: 'red', count: 3 }],
    });
    await ctx.addBot('T306R', 'red');
    await ctx.addBot('T306B', 'blue');
    await ctx.startGame();
    await h.sleep(1000);

    const titlePromise = h.waitForTitle(ctx.admin, 'RedTicketEmpty', 25000);

    await ctx.kill('T306R'); await h.sleep(1200);
    await ctx.kill('T306R'); await h.sleep(1200);
    await ctx.kill('T306R');

    await titlePromise;
  });

  // ─── E. 複合条件 ─────────────────────────────────────────────
  // TC-308: composite AND — 手動テストに移管（manual_only.md 参照）
  // TC-309: composite OR  — 手動テストに移管（manual_only.md 参照）

  // ─── F. ゲーム終了フロー ──────────────────────────────────────
  // TC-310: shutdownCommands — 手動テストに移管（manual_only.md 参照）

  return suite;
};
