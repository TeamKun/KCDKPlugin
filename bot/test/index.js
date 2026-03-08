'use strict';

/**
 * KCDK Mineflayer 自動テスト エントリーポイント
 *
 * 使い方:
 *   node test/index.js               # 全スイート実行
 *   node test/index.js 01            # 01_setup のみ
 *   node test/index.js 03 04         # 複数スイート指定
 *
 * 前提:
 *   - server.properties: online-mode=false
 *   - サーバー起動後 /op KCDKTestAdmin を実行済み
 *   - 初回は /kcdk setup 実行済み
 */

const { printReport } = require('./runner');
const { GameContext }  = require('./game-context');

const ALL_SUITES = [
  { id: '01', module: './suites/01_setup' },
  { id: '02', module: './suites/02_config' },
  { id: '03', module: './suites/03_game_start' },
  { id: '04', module: './suites/04_combat' },
  { id: '05', module: './suites/05_end_condition' },
  { id: '06', module: './suites/06_ui' },
];

async function runSuite(mod) {
  const register = require(mod);

  const ctx = new GameContext();
  await ctx.connect();

  let results = [];
  try {
    const suite = register(ctx);
    results = await suite.run();
  } finally {
    try { await ctx.cleanup(); } catch (_) {}
    await ctx.disconnect();
  }
  return results;
}

async function main() {
  const args = process.argv.slice(2);

  const targets = args.length === 0
    ? ALL_SUITES
    : ALL_SUITES.filter(s => args.some(a => s.id.startsWith(a) || a.startsWith(s.id)));

  if (targets.length === 0) {
    console.error(`指定されたスイートが見つかりません: ${args.join(', ')}`);
    console.error(`有効な ID: ${ALL_SUITES.map(s => s.id).join(', ')}`);
    process.exit(1);
  }

  console.log('='.repeat(60));
  console.log('KCDK 自動テスト');
  console.log(`対象スイート: ${targets.map(s => s.id).join(', ')}`);
  console.log('='.repeat(60));

  const allResults = [];

  for (const { id, module: mod } of targets) {
    console.log(`\n--- Suite ${id} ---`);
    try {
      const results = await runSuite(mod);
      allResults.push(...results);
    } catch (e) {
      console.error(`Suite ${id} で致命的エラー: ${e.message}`);
      console.error(e.stack);
    }
  }

  console.log('\n' + '='.repeat(60));
  const passed = printReport(allResults);
  process.exit(passed ? 0 : 1);
}

main().catch(e => {
  console.error('予期しないエラー:', e);
  process.exit(1);
});
