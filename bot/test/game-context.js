'use strict';

const { spawnBot, sleep, waitForMessage } = require('./helpers');

// テスト用の標準座標
const COORDS = {
  redSpawn:  { x: 100, y: 64, z: 0,   world: 'world' },
  blueSpawn: { x: -100, y: 64, z: 0,  world: 'world' },
  redReady:  { x: 100, y: 64, z: 50,  world: 'world' },
  blueReady: { x: -100, y: 64, z: 50, world: 'world' },
  beacon:    { x: 50, y: 64, z: 0,    world: 'world' },
};

const DEFAULTS = {
  host: 'localhost',
  port: 25565,
  version: '1.21.11',
  adminName: 'KCDKTestAdmin',  // 事前に /op KCDKTestAdmin が必要
  cmdDelay: 300,
};

class GameContext {
  constructor(opts = {}) {
    this.opts  = { ...DEFAULTS, ...opts };
    this.admin = null;
    this.bots  = new Map(); // name → bot
    this.COORDS = COORDS;
  }

  // ─── 接続管理 ────────────────────────────────────────────────

  async connect() {
    this.admin = await spawnBot(this.opts.adminName, this.opts);
    this.admin.on('error', e => console.warn(`[admin] error: ${e.message}`));
    console.log(`  [ctx] admin '${this.opts.adminName}' connected`);
  }

  async disconnect() {
    await this.removeBots();
    if (this.admin) { try { this.admin.quit(); } catch (_) {} this.admin = null; }
  }

  // ─── ボット管理 ──────────────────────────────────────────────

  /**
   * テストボットを接続して Scoreboard チームに割り当てる
   * @param {string} name - ボット名 (例: 'TestRed1')
   * @param {string} team - チーム名 (例: 'red')
   * @param {boolean} captain - キャプテンチームに入れるか
   */
  async addBot(name, team, captain = false) {
    const bot = await spawnBot(name, this.opts);
    this.bots.set(name, bot);

    // 死亡時に自動リスポーン
    bot.on('death', () => setTimeout(() => { try { bot.respawn(); } catch (_) {} }, 600));
    bot.on('error', e => console.warn(`[${name}] error: ${e.message}`));

    // admin がチームに割り当て
    const sbTeam = captain ? `kcdk.${team}.captain` : `kcdk.${team}`;
    await this.cmd(`/team join ${sbTeam} ${name}`);

    return bot;
  }

  /** 複数ボットを順番に接続 */
  async addBots(specs /* [{name, team, captain?}] */) {
    const result = {};
    for (const s of specs) {
      result[s.name] = await this.addBot(s.name, s.team, s.captain || false);
      await sleep(400);
    }
    return result;
  }

  /** テストボットを全て切断 */
  async removeBots() {
    for (const [, bot] of this.bots) { try { bot.quit(); } catch (_) {} }
    this.bots.clear();
    await sleep(600);
  }

  /** 名前でボットを取得 */
  bot(name) { return this.bots.get(name); }

  // ─── ゲーム操作 ──────────────────────────────────────────────

  async startGame() {
    await this.cmd('/kcdk game start');
    await sleep(800);
  }

  async stopGame() {
    try { await this.cmd('/kcdk game stop'); await sleep(500); } catch (_) {}
  }

  /** ボットをキルする */
  async kill(name) { await this.cmd(`/kill ${name}`); }

  /** ボットを指定座標にTP */
  async tp(name, x, y, z) { await this.cmd(`/tp ${name} ${x} ${y} ${z}`); }

  /** 管理コマンド実行 + delay */
  async cmd(command, delayMs) {
    this.admin.chat(command);
    await sleep(delayMs ?? this.opts.cmdDelay);
  }

  // ─── ゲーム設定 ──────────────────────────────────────────────

  /**
   * テスト用のゲーム設定をまとめて適用する
   *
   * config = {
   *   gamemode: 'ADVENTURE' | 'SURVIVAL',
   *   disableHunger: boolean,
   *   timeLimit: { hours, minutes, seconds } | null,
   *   startupCommands: string[],
   *   shutdownCommands: string[],
   *   teams: [{
   *     name, respawnCount, respawnLocation, readyLocation, waitingTime,
   *     roles: [{ name, respawnCount, respawnLocation }]
   *   }],
   *   endConditions: [{
   *     type: 'extermination'|'ticket'|'beacon',
   *     message, team?, count?, location?, hitpoint?
   *   }],
   * }
   */
  async setupConfig(config = {}) {
    if (config.gamemode !== undefined)
      await this.cmd(`/kcdk config gamemode ${config.gamemode}`);

    if (config.disableHunger !== undefined)
      await this.cmd(`/kcdk config disableHunger ${config.disableHunger}`);

    if (config.timeLimit === null)
      await this.cmd('/kcdk config timeLimit remove');
    else if (config.timeLimit)
      await this.cmd(`/kcdk config timeLimit set ${config.timeLimit.hours ?? 0} ${config.timeLimit.minutes ?? 0} ${config.timeLimit.seconds ?? 0}`);

    // startup / shutdown commands: CLI コマンドが存在せず JSON import が全設定を置き換えるため
    // setupConfig では設定しない。個別テストで ctx.importConfig() を使うこと。

    // チームを全クリアして追加
    await this.cmd('/kcdk config team clear');
    for (const t of (config.teams || [])) {
      await this.cmd(`/kcdk config team add ${t.name}`);

        if (t.respawnCount !== undefined)
        await this.cmd(`/kcdk config team ${t.name} respawnCount ${t.respawnCount}`);

      if (t.respawnLocation) {
        const { world = 'world', x, y, z, yaw = 0, pitch = 0 } = t.respawnLocation;
        await this.cmd(`/kcdk config team ${t.name} respawnLocation ${world} ${x} ${y} ${z} ${yaw} ${pitch}`);
      }

      if (t.readyLocation) {
        // readyLocation コマンド: <world> <x> <y> <z> <yaw> <pitch> <hours> <minutes> <seconds>
        const { world = 'world', x, y, z, yaw = 0, pitch = 0 } = t.readyLocation;
        const { hours = 0, minutes = 0, seconds = 5 } = t.waitingTime || {};
        await this.cmd(`/kcdk config team ${t.name} readyLocation ${world} ${x} ${y} ${z} ${yaw} ${pitch} ${hours} ${minutes} ${seconds}`);
      }

      for (const r of (t.roles || [])) {
        await this.cmd(`/kcdk config team ${t.name} role add ${r.name}`);
        if (r.respawnCount !== undefined)
          await this.cmd(`/kcdk config team ${t.name} role ${r.name} respawnCount ${r.respawnCount}`);
        if (r.respawnLocation) {
          const { world = 'world', x, y, z, yaw = 0, pitch = 0 } = r.respawnLocation;
          await this.cmd(`/kcdk config team ${t.name} role ${r.name} respawnLocation ${world} ${x} ${y} ${z} ${yaw} ${pitch}`);
        }
      }
    }

    // 終了条件をクリアして追加
    await this.cmd('/kcdk config endCondition clear');
    for (const ec of (config.endConditions || [])) {
      switch (ec.type) {
        case 'extermination':
          await this.cmd(`/kcdk config endCondition add extermination ${ec.message} ${ec.team}`);
          break;
        case 'ticket':
          await this.cmd(`/kcdk config endCondition add ticket ${ec.message} ${ec.team} ${ec.count}`);
          break;
        case 'beacon': {
          const { world = 'world', x, y, z, yaw = 0, pitch = 0 } = ec.location;
          await this.cmd(`/kcdk config endCondition add beacon ${ec.message} ${world} ${x} ${y} ${z} ${yaw} ${pitch} ${ec.hitpoint}`);
          break;
        }
      }
    }

    await this.cmd('/kcdk config save');
    await sleep(300);
  }

  /** テスト間クリーンアップ (ゲーム停止 + ボット削除) */
  async cleanup() {
    await this.stopGame();
    await this.removeBots();
    await this.cmd('/kcdk config team clear');
    await this.cmd('/kcdk config endCondition clear');
    await sleep(300);
  }
}

module.exports = { GameContext, COORDS };
