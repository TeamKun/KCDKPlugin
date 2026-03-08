'use strict';

const mineflayer = require('mineflayer');

const DEFAULTS = { host: 'localhost', port: 25565, version: '1.21.11' };

// ─── 基本ユーティリティ ───────────────────────────────────────────

function sleep(ms) {
  return new Promise(r => setTimeout(r, ms));
}

/** 色コード(§X)を除去して trim */
function stripColors(str) {
  return String(str).replace(/§[0-9a-fklmnor]/gi, '').trim();
}

// ─── イベント待機 ────────────────────────────────────────────────

function waitForEvent(bot, event, timeoutMs = 10000, predicate = () => true) {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => {
      bot.off(event, handler);
      reject(new Error(`Timeout(${timeoutMs}ms) waiting for '${event}'`));
    }, timeoutMs);
    function handler(...args) {
      if (predicate(...args)) {
        clearTimeout(timer); bot.off(event, handler); resolve(args);
      }
    }
    bot.on(event, handler);
  });
}

/** ボットを生成してspawnを待つ */
function spawnBot(username, opts = {}) {
  return new Promise((resolve, reject) => {
    const bot = mineflayer.createBot({
      host: opts.host || DEFAULTS.host,
      port: opts.port || DEFAULTS.port,
      username,
      version: opts.version || DEFAULTS.version,
      auth: 'offline',
    });
    const timer = setTimeout(() => reject(new Error(`${username}: spawn timeout`)), 15000);
    bot.once('spawn', () => { clearTimeout(timer); resolve(bot); });
    bot.once('error', e => { clearTimeout(timer); reject(e); });
    bot.once('kicked', r => { clearTimeout(timer); reject(new Error(`Kicked: ${r}`)); });
  });
}

/** 指定座標の近くにいるまで待つ */
function waitForPosition(bot, { x, y, z }, tolerance = 3, timeoutMs = 12000) {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => {
      const p = bot.entity.position;
      reject(new Error(
        `Position timeout: want(${x},${y},${z}) got(${p.x.toFixed(1)},${p.y.toFixed(1)},${p.z.toFixed(1)})`
      ));
    }, timeoutMs);
    function check() {
      const p = bot.entity.position;
      const d = Math.sqrt((p.x - x) ** 2 + (p.y - y) ** 2 + (p.z - z) ** 2);
      if (d <= tolerance) { clearTimeout(timer); bot.off('move', check); resolve(p); }
    }
    bot.on('move', check);
    check();
  });
}

/**
 * 1.20.5+ の anonymousNbt 形式タイトルパケットから文字列を取り出す
 * NBT compound: { type:'compound', value:{ text:{type:'string',value:'...'}, ... } }
 * または文字列 / JSON文字列 も扱う
 */
function extractNbtText(node) {
  if (node === null || node === undefined) return '';
  if (typeof node === 'string') {
    // JSON文字列の場合はパース
    try {
      const p = JSON.parse(node);
      if (typeof p === 'string') return p;
      if (p && typeof p === 'object') return extractNbtText(p);
    } catch (_) {}
    return node;
  }
  if (typeof node !== 'object') return String(node);
  // NBT タグ: { type: '...', value: ... }
  if (node.type !== undefined && node.value !== undefined) {
    return extractNbtText(node.value);
  }
  // テキストコンポーネント: { text: ..., extra: [...], ... }
  let result = '';
  if (node.text !== undefined) result += extractNbtText(node.text);
  if (Array.isArray(node.extra)) {
    for (const child of node.extra) result += extractNbtText(child);
  } else if (Array.isArray(node)) {
    for (const child of node) result += extractNbtText(child);
  }
  return result || JSON.stringify(node);
}

/** タイトル (text, type) を受信するまで待つ。matcher は string か RegExp */
function waitForTitle(bot, matcher, timeoutMs = 15000) {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => { cleanup(); reject(new Error(`Title timeout: ${matcher}`)); }, timeoutMs);
    let done = false;

    function tryMatch(raw) {
      if (done) return;
      const s = stripColors(String(raw));
      const ok = matcher instanceof RegExp ? matcher.test(s) : s.includes(matcher);
      if (ok) { done = true; clearTimeout(timer); cleanup(); resolve({ text: s }); }
    }

    // Mineflayer の title イベント (パース済み)
    function titleHandler(text) { tryMatch(text.toString()); }

    // 生パケット (1.20.5+ anonymousNbt 対応)
    function rawHandler(packet) { tryMatch(extractNbtText(packet.text)); }

    function cleanup() {
      bot.off('title', titleHandler);
      if (bot._client) {
        bot._client.removeListener('set_title_text', rawHandler);
        bot._client.removeListener('set_title_subtitle', rawHandler);
      }
    }

    bot.on('title', titleHandler);
    if (bot._client) {
      bot._client.on('set_title_text', rawHandler);
      bot._client.on('set_title_subtitle', rawHandler);
    }
  });
}

/** actionBar に matcher を含むメッセージが来るまで待つ */
function waitForActionBar(bot, matcher, timeoutMs = 10000) {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => reject(new Error(`ActionBar timeout: ${matcher}`)), timeoutMs);
    function handler(msg) {
      const s = stripColors(msg.toString());
      const ok = matcher instanceof RegExp ? matcher.test(s) : s.includes(matcher);
      if (ok) { clearTimeout(timer); bot.off('actionBar', handler); resolve(s); }
    }
    bot.on('actionBar', handler);
  });
}

/**
 * システムメッセージ (プラグイン応答など) に matcher を含むものを待つ
 * 1.21.x では system_chat パケットが anonymousNbt 形式のため、
 * Mineflayer の message イベントに加えて生パケットも直接リッスンする
 */
function waitForMessage(bot, matcher, timeoutMs = 5000) {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => { cleanup(); reject(new Error(`Message timeout: ${matcher}`)); }, timeoutMs);
    let done = false;

    function tryMatch(raw) {
      if (done) return;
      const s = stripColors(String(raw));
      const ok = matcher instanceof RegExp ? matcher.test(s) : s.includes(matcher);
      if (ok) { done = true; clearTimeout(timer); cleanup(); resolve(s); }
    }

    function handler(msg) { tryMatch(msg.toString()); }
    // 生パケット: content フィールド (1.21.x) または formattedMessage (旧世代) を直接パース
    function rawHandler(packet) {
      tryMatch(extractNbtText(packet.content ?? packet.formattedMessage));
    }

    function cleanup() {
      bot.off('message', handler);
      if (bot._client) bot._client.removeListener('system_chat', rawHandler);
    }

    bot.on('message', handler);
    if (bot._client) bot._client.on('system_chat', rawHandler);
  });
}

/** 死亡イベントを待つ */
function waitForDeath(bot, timeoutMs = 15000) {
  return waitForEvent(bot, 'death', timeoutMs);
}

// ─── /kcdk config show パース ─────────────────────────────────────

/**
 * /kcdk config show を実行して結果をキーバリューで返す
 * 例: { Gamemode: 'ADVENTURE', 'Time Limit': '0h 10m 0s', ... }
 */
async function parseConfigShow(admin) {
  const lines = [];
  const handler = (msg) => {
    const s = stripColors(msg.toString());
    if (s.length > 0) lines.push(s);
  };
  admin.on('message', handler);
  admin.chat('/kcdk config show');
  await sleep(1200);
  admin.off('message', handler);

  const result = {};
  for (const line of lines) {
    const m = line.match(/^(.+?):\s*(.*)$/);
    if (m) result[m[1].trim()] = m[2].trim();
  }
  return result;
}

/**
 * コマンドを実行してコマンド応答メッセージを収集する
 */
async function runAndCapture(admin, command, waitMs = 1000) {
  const lines = [];
  const handler = (msg) => {
    const s = stripColors(msg.toString());
    if (s.length > 0) lines.push(s);
  };
  admin.on('message', handler);
  admin.chat(command);
  await sleep(waitMs);
  admin.off('message', handler);
  return lines;
}

// ─── アサーション ────────────────────────────────────────────────

function assertNear(actual, expected, tolerance = 3) {
  const d = Math.sqrt((actual.x - expected.x) ** 2 + (actual.y - expected.y) ** 2 + (actual.z - expected.z) ** 2);
  if (d > tolerance) {
    throw new Error(
      `Position not near (${expected.x},${expected.y},${expected.z}): ` +
      `got (${actual.x.toFixed(1)},${actual.y.toFixed(1)},${actual.z.toFixed(1)}) dist=${d.toFixed(2)}`
    );
  }
}

function assertEqual(actual, expected, msg = '') {
  if (actual !== expected) {
    throw new Error(`${msg ? msg + ': ' : ''}expected ${JSON.stringify(expected)}, got ${JSON.stringify(actual)}`);
  }
}

function assertTrue(val, msg = 'Assertion failed') {
  if (!val) throw new Error(msg);
}

function assertFalse(val, msg = 'Expected false') {
  if (val) throw new Error(msg);
}

module.exports = {
  sleep, stripColors,
  waitForEvent, spawnBot,
  waitForPosition, waitForTitle, waitForActionBar, waitForMessage, waitForDeath,
  parseConfigShow, runAndCapture,
  assertNear, assertEqual, assertTrue, assertFalse,
};
