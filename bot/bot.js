const mineflayer = require('mineflayer');

/**
 * テスト用ボットを1体生成する
 *
 * @param {object} opts
 * @param {string} opts.host
 * @param {number} opts.port
 * @param {string} opts.version
 * @param {string} opts.username
 * @param {string} opts.team       - チーム名 (red / blue / ...)
 * @param {boolean} opts.captain   - キャプテンチームに入れるか
 * @returns {import('mineflayer').Bot}
 */
function spawnBot({ host, port, version, username, team, captain }) {
    const sbTeam = captain ? `kcdk.${team}.captain` : `kcdk.${team}`;

    const bot = mineflayer.createBot({
        host,
        port,
        username,
        version,
        auth: 'offline',
    });

    const log = (msg) => console.log(`[${username}] ${msg}`);

    // ---- ログイン ----
    bot.once('spawn', () => {
        log(`スポーン完了 → チーム ${sbTeam} に参加中...`);
        // ops権限がある場合のみ /team join が使えるため、
        // コンソールまたはOPプレイヤーが実行する想定でチャットに送る
        bot.chat(`/team join ${sbTeam} ${username}`);
    });

    // ---- 死亡時の自動リスポーン ----
    bot.on('death', () => {
        log('死亡 → 1秒後にリスポーン');
        setTimeout(() => bot.respawn(), 1000);
    });

    // ---- チャットを受信してコマンド実行 ----
    bot.on('chat', (sender, message) => {
        if (sender === username) return;

        // !all <cmd> で全ボットに実行
        if (message.startsWith('!all ')) {
            const cmd = message.slice(5);
            handleCommand(cmd);
            return;
        }

        // !<botname> <cmd> で特定ボットに実行
        if (message.startsWith(`!${username} `)) {
            const cmd = message.slice(username.length + 2);
            handleCommand(cmd);
        }
    });

    function handleCommand(cmd) {
        if (cmd === 'stop') {
            log('切断します');
            bot.quit('stop command');
        } else if (cmd === 'respawn') {
            bot.respawn();
        } else if (cmd === 'pos') {
            const p = bot.entity.position;
            bot.chat(`${username}: ${p.x.toFixed(1)}, ${p.y.toFixed(1)}, ${p.z.toFixed(1)}`);
        } else if (cmd.startsWith('say ')) {
            bot.chat(cmd.slice(4));
        } else if (cmd.startsWith('/')) {
            bot.chat(cmd);
        }
    }

    // ---- エラー・切断ハンドリング ----
    bot.on('error', (err) => {
        log(`エラー: ${err.message}`);
    });

    bot.on('kicked', (reason) => {
        log(`キック: ${reason}`);
    });

    bot.on('end', (reason) => {
        log(`切断 (${reason})`);
    });

    return bot;
}

module.exports = { spawnBot };
