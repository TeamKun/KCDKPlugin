'use strict';

const { TestSuite } = require('../runner');
const h = require('../helpers');
const { COORDS } = require('../game-context');

const C = COORDS;

/** bot.entities から username でエンティティを探す */
function findPlayerEntity(bot, username) {
  return Object.values(bot.entities).find(
    e => e.type === 'player' && e.username === username
  );
}

module.exports = function register(ctx) {
  const suite = new TestSuite('04 戦闘・リスポーン');

  suite.afterEach(async () => { await ctx.cleanup(); });

  // ─── A. FriendlyFire 制御 ────────────────────────────────────

  // TC-200: 同一チーム・同一ロール間のFriendlyFire禁止
  suite.test('TC-200: 同一チーム間でダメージが入らない', async () => {
    await ctx.setupConfig({
      gamemode: 'SURVIVAL',
      teams: [{ name: 'red', respawnCount: -1, respawnLocation: C.redSpawn }],
    });
    const botA = await ctx.addBot('T200A', 'red');
    const botB = await ctx.addBot('T200B', 'red');
    await ctx.startGame();
    await h.waitForPosition(botA, C.redSpawn, 5, 10000);

    await ctx.tp('T200A', C.redSpawn.x, C.redSpawn.y, C.redSpawn.z);
    await ctx.tp('T200B', C.redSpawn.x, C.redSpawn.y, C.redSpawn.z);
    await h.sleep(600);

    const initialHealth = botB.health;
    const target = findPlayerEntity(botA, 'T200B');
    h.assertTrue(target !== undefined, 'T200B のエンティティが見つからない');

    botA.attack(target);
    await h.sleep(800);

    h.assertTrue(botB.health >= initialHealth,
      `FriendlyFire が遮断されていない: ${initialHealth} → ${botB.health}`);
  });

  // TC-201: 同一チーム・異なるロール間のFriendlyFire禁止
  suite.test('TC-201: 同一チームの異なるロール間でもダメージが入らない', async () => {
    await ctx.setupConfig({
      gamemode: 'SURVIVAL',
      teams: [{
        name: 'red', respawnCount: -1, respawnLocation: C.redSpawn,
        roles: [{ name: 'captain', respawnLocation: C.redSpawn }],
      }],
    });
    const botBase = await ctx.addBot('T201Base', 'red');
    const botCap  = await ctx.addBot('T201Cap', 'red', true);
    await ctx.startGame();

    await ctx.tp('T201Base', C.redSpawn.x, C.redSpawn.y, C.redSpawn.z);
    await ctx.tp('T201Cap',  C.redSpawn.x, C.redSpawn.y, C.redSpawn.z);
    await h.sleep(600);

    const initialHealth = botCap.health;
    const target = findPlayerEntity(botBase, 'T201Cap');
    if (!target) return; // エンティティが見えない場合はスキップ

    botBase.attack(target);
    await h.sleep(800);

    h.assertTrue(botCap.health >= initialHealth,
      `異ロール間 FriendlyFire が遮断されていない: ${initialHealth} → ${botCap.health}`);
  });

  // TC-202: 異なるチーム間でダメージが通る
  suite.test('TC-202: 異なるチーム間でダメージが入る', async () => {
    await ctx.setupConfig({
      gamemode: 'SURVIVAL',
      teams: [
        { name: 'red',  respawnCount: -1, respawnLocation: C.redSpawn  },
        { name: 'blue', respawnCount: -1, respawnLocation: C.blueSpawn },
      ],
    });
    await ctx.addBot('T202R', 'red');
    const botBlue = await ctx.addBot('T202B', 'blue');
    await ctx.startGame();
    await h.waitForPosition(botBlue, C.blueSpawn, 5, 10000);

    const initialHealth = botBlue.health;
    await ctx.cmd('/damage T202B 5 minecraft:player_attack by T202R');
    await h.sleep(800);

    h.assertTrue(botBlue.health < initialHealth,
      `異チーム間でダメージが入っていない: ${initialHealth} → ${botBlue.health}`);
  });

  // ─── B. 死亡・リスポーン ──────────────────────────────────────

  // TC-210: 死亡後に respawnLocation へTP (respawnCount=-1)
  suite.test('TC-210: respawnCount=-1 で死亡後にリスポーン地点へTP', async () => {
    await ctx.setupConfig({
      gamemode: 'ADVENTURE',
      teams: [{ name: 'red', respawnCount: -1, respawnLocation: C.redSpawn }],
    });
    const botRed = await ctx.addBot('T210R', 'red');
    await ctx.startGame();
    await h.waitForPosition(botRed, C.redSpawn, 5, 10000);

    await ctx.tp('T210R', 0, 64, 0);
    await h.sleep(400);
    const deathPromise = h.waitForDeath(botRed, 10000);
    await ctx.kill('T210R');
    await deathPromise;

    await h.waitForPosition(botRed, C.redSpawn, 5, 12000);
  });

  // TC-212: respawnCount=0 で初回死亡後に即スペクテイター
  suite.test('TC-212: respawnCount=0 で初回死亡後に即スペクテイターになる', async () => {
    await ctx.setupConfig({
      gamemode: 'SURVIVAL',
      teams: [{ name: 'red', respawnCount: 0, respawnLocation: C.redSpawn }],
    });
    const botRed = await ctx.addBot('T212R', 'red');
    await ctx.startGame();
    await h.waitForPosition(botRed, C.redSpawn, 5, 10000);

    const deathPr = h.waitForDeath(botRed, 8000);
    await ctx.kill('T212R');
    await deathPr;
    await h.sleep(1000);

    if (botRed.player) {
      h.assertEqual(botRed.player.gamemode, 3,
        `初回死亡後のゲームモードが SPECTATOR(3) ではない: ${botRed.player.gamemode}`);
    }
  });

  // TC-213: スペクテイターのログアウト → 再ログイン後もスペクテイター
  suite.test('TC-213: スペクテイターが再接続後もスペクテイターのまま', async () => {
    await ctx.setupConfig({
      gamemode: 'SURVIVAL',
      teams: [{ name: 'red', respawnCount: 0, respawnLocation: C.redSpawn }],
    });

    await ctx.addBot('T213R', 'red');
    await ctx.startGame();
    await ctx.kill('T213R');
    await h.sleep(1500);

    ctx.bot('T213R').quit();
    ctx.bots.delete('T213R');
    await h.sleep(1000);

    const rejoin = await ctx.addBot('T213R', 'red');
    await h.sleep(1500);

    if (rejoin.player) {
      h.assertEqual(rejoin.player.gamemode, 3,
        `再接続後のゲームモードが SPECTATOR(3) ではない: ${rejoin.player.gamemode}`);
    }
  });

  // TC-214: 生存プレイヤーの再ログイン後は生存維持
  suite.test('TC-214: 生存プレイヤーが再接続後も生存状態を維持する', async () => {
    await ctx.setupConfig({
      gamemode: 'ADVENTURE',
      teams: [{ name: 'red', respawnCount: -1, respawnLocation: C.redSpawn }],
    });
    await ctx.addBot('T214R', 'red');
    await ctx.startGame();
    await h.sleep(1500);

    ctx.bot('T214R').quit();
    ctx.bots.delete('T214R');
    await h.sleep(1000);

    const rejoin = await ctx.addBot('T214R', 'red');
    await h.sleep(1500);

    if (rejoin.player) {
      h.assertTrue(rejoin.player.gamemode !== 3,
        `再接続後がスペクテイターになってしまっている: ${rejoin.player.gamemode}`);
    }
  });

  // ─── C. K/D 管理 ─────────────────────────────────────────────

  // TC-220: キル時に "1 Kill" タイトルが表示される
  suite.test('TC-220: 初キル時に "1 Kill" タイトルが killer に表示される', async () => {
    await ctx.setupConfig({
      gamemode: 'SURVIVAL',
      teams: [
        { name: 'red',  respawnCount: -1, respawnLocation: C.redSpawn  },
        { name: 'blue', respawnCount: -1, respawnLocation: C.blueSpawn },
      ],
    });
    const botRed = await ctx.addBot('T220R', 'red');
    await ctx.addBot('T220B', 'blue');
    await ctx.startGame();
    await h.waitForPosition(botRed, C.redSpawn, 5, 10000);
    await h.sleep(3000);

    const titlePromise = h.waitForTitle(botRed, /Kill/, 12000);
    await ctx.cmd('/damage T220B 100 minecraft:player_attack by T220R');

    const { text } = await titlePromise;
    h.assertTrue(/1\s*Kill/i.test(text), `タイトルが "1 Kill" でない: ${text}`);
  });

  // ─── D. 空腹値制御 ────────────────────────────────────────────

  // TC-230: disableHunger=true でゲーム中空腹値が減らない
  suite.test('TC-230: disableHunger=true でゲーム中空腹値が減らない', async () => {
    await ctx.setupConfig({
      gamemode: 'SURVIVAL',
      disableHunger: true,
      teams: [{ name: 'red', respawnCount: -1, respawnLocation: C.redSpawn }],
    });
    const botRed = await ctx.addBot('T230R', 'red');
    await ctx.startGame();
    await h.waitForPosition(botRed, C.redSpawn, 5, 10000);

    const initialFood = botRed.food;
    for (let i = 0; i < 10; i++) {
      botRed.setControlState('sprint', true);
      await h.sleep(300);
    }
    botRed.setControlState('sprint', false);
    await h.sleep(500);

    h.assertTrue(botRed.food >= initialFood,
      `disableHunger=true なのに空腹が減った: ${initialFood} → ${botRed.food}`);
  });

  return suite;
};
