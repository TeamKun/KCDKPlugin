/**
 * KCDK テスト用ボット
 *
 * 使い方:
 *   node index.js --team red --count 2
 *   node index.js --team red --count 1 --team blue --count 1
 *   node index.js --help
 */

const { spawnBot } = require('./bot');

// ---- 引数パース ----
const args = process.argv.slice(2);

if (args.includes('--help') || args.includes('-h')) {
    console.log(`
使い方:
  node index.js [オプション]

オプション:
  --team <name>      チーム名 (red / blue / green / yellow / admin)
  --count <n>        そのチームのボット数 (デフォルト: 1)
  --captain          直前の --team をキャプテンにする
  --host <host>      サーバーホスト (デフォルト: localhost)
  --port <port>      サーバーポート (デフォルト: 25565)
  --version <ver>    MCバージョン (デフォルト: 1.21.11)
  --delay <ms>       ボット起動間隔ms (デフォルト: 1500)
  --help             このヘルプを表示

例:
  node index.js --team red --count 2 --team blue --count 2
  node index.js --team red --captain --count 1 --team blue --count 3
`);
    process.exit(0);
}

// デフォルト設定
const config = {
    host: 'localhost',
    port: 25565,
    version: '1.21.11',
    delay: 1500,
};

// チーム指定リスト: [{ team, captain, count }, ...]
const teamSpecs = [];

for (let i = 0; i < args.length; i++) {
    switch (args[i]) {
        case '--host':    config.host    = args[++i]; break;
        case '--port':    config.port    = parseInt(args[++i]); break;
        case '--version': config.version = args[++i]; break;
        case '--delay':   config.delay   = parseInt(args[++i]); break;
        case '--team': {
            const team = args[++i];
            // 後続 --captain / --count を先読み
            let captain = false;
            let count   = 1;
            if (args[i + 1] === '--captain') { captain = true; i++; }
            if (args[i + 1] === '--count')   { count   = parseInt(args[i + 2]); i += 2; }
            teamSpecs.push({ team, captain, count });
            break;
        }
        case '--captain':
            if (teamSpecs.length > 0) teamSpecs[teamSpecs.length - 1].captain = true;
            break;
        case '--count':
            if (teamSpecs.length > 0) teamSpecs[teamSpecs.length - 1].count = parseInt(args[++i]);
            else i++; // 余分な引数はスキップ
            break;
    }
}

// チーム指定がなければ対話で尋ねる
if (teamSpecs.length === 0) {
    teamSpecs.push({ team: 'red', captain: false, count: 1 });
    console.log('[Info] チーム指定なし → デフォルト: red x1');
}

// ---- ボットを順番に起動 ----
const bots = [];
let index = 0;

function countLabel({ team, captain }) {
    return captain ? `kcdk.${team}.captain` : `kcdk.${team}`;
}

// スペックを展開して起動リストを作る
const spawnList = [];
const teamCounters = {};

for (const spec of teamSpecs) {
    const key = countLabel(spec);
    for (let n = 0; n < spec.count; n++) {
        teamCounters[key] = (teamCounters[key] || 0) + 1;
        const num = teamCounters[key];
        const username = `Bot_${spec.team}${spec.captain ? 'Cap' : ''}${num}`;
        spawnList.push({ username, team: spec.team, captain: spec.captain });
    }
}

console.log(`[KCDK Bot] ${spawnList.length} 体のボットを起動します`);
console.log(`  サーバー: ${config.host}:${config.port}  MC: ${config.version}`);
spawnList.forEach(s => console.log(`  - ${s.username} → kcdk.${s.team}${s.captain ? '.captain' : ''}`));
console.log('');

function spawnNext() {
    if (index >= spawnList.length) return;
    const { username, team, captain } = spawnList[index++];
    const bot = spawnBot({ ...config, username, team, captain });
    bots.push(bot);
    if (index < spawnList.length) {
        setTimeout(spawnNext, config.delay);
    }
}

spawnNext();

// Ctrl+C で全ボットを切断
process.on('SIGINT', () => {
    console.log('\n[KCDK Bot] 全ボットを切断します...');
    bots.forEach(b => { try { b.quit(); } catch (_) {} });
    setTimeout(() => process.exit(0), 500);
});
