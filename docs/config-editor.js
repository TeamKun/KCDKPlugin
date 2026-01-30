// ============================================================
// キー難読化対応表
// JSONのキーを1文字に短縮してデータ量を削減する
// ============================================================
const KEY_MAP = {
    // Config
    gamemode: 'a',
    bossbar: 'b',
    timeLimit: 'c',
    startupCommands: 'd',
    shutdownCommands: 'e',
    teams: 'f',
    endConditions: 'g',

    // Bossbar
    mcid: 'h',

    // Time
    hours: 'i',
    minutes: 'j',
    seconds: 'k',

    // Team
    name: 'l',
    displayName: 'm',
    armorColor: 'n',
    respawnCount: 'o',
    readyLocation: 'p',
    respawnLocation: 'q',
    effects: 'r',
    roles: 's',

    // Location
    world: 't',
    x: 'u',
    y: 'v',
    z: 'w',
    yaw: 'A',
    pitch: 'B',

    // ReadyLocation (waitingTime)
    waitingTime: 'C',

    // Role
    extendsEffects: 'D',
    extendsItem: 'E',

    // Effect
    amplifier: 'F',
    hideParticles: 'G',

    // EndCondition
    type: 'H',
    message: 'I',
    conditions: 'J',
    operator: 'K',

    // BeaconEndCondition
    location: 'L',
    hitpoint: 'M',

    // ExterminationEndCondition / TicketEndCondition
    team: 'N',
    count: 'O',

    // Armor
    hasArmor: 'P'
};

// 逆引き用マップ（デコード時に使用）
const REVERSE_KEY_MAP = Object.fromEntries(
    Object.entries(KEY_MAP).map(([k, v]) => [v, k])
);

// ============================================================
// JSON難読化関数
// ============================================================

/**
 * オブジェクトのキーを短縮形に変換
 */
function minifyKeys(obj) {
    if (obj === null || obj === undefined) {
        return obj;
    }
    if (Array.isArray(obj)) {
        return obj.map(item => minifyKeys(item));
    }
    if (typeof obj === 'object') {
        const result = {};
        for (const [key, value] of Object.entries(obj)) {
            const shortKey = KEY_MAP[key] || key;
            result[shortKey] = minifyKeys(value);
        }
        return result;
    }
    return obj;
}

/**
 * 短縮キーを元に戻す（デコード用）
 */
function expandKeys(obj) {
    if (obj === null || obj === undefined) {
        return obj;
    }
    if (Array.isArray(obj)) {
        return obj.map(item => expandKeys(item));
    }
    if (typeof obj === 'object') {
        const result = {};
        for (const [key, value] of Object.entries(obj)) {
            const fullKey = REVERSE_KEY_MAP[key] || key;
            result[fullKey] = expandKeys(value);
        }
        return result;
    }
    return obj;
}

/**
 * ConfigオブジェクトをBase64エンコードされた短縮JSONに変換
 */
function encodeConfig(config) {
    const minified = minifyKeys(config);
    const json = JSON.stringify(minified);
    return btoa(unescape(encodeURIComponent(json)));
}

/**
 * Base64エンコードされた短縮JSONをConfigオブジェクトにデコード
 */
function decodeConfig(encoded) {
    const json = decodeURIComponent(escape(atob(encoded)));
    const minified = JSON.parse(json);
    return expandKeys(minified);
}

// ============================================================
// フォームからConfig取得
// ============================================================

/**
 * HTMLフォームからConfigオブジェクトを生成
 */
function collectConfig() {
    const config = {
        gamemode: document.getElementById('gamemode')?.value || 'ADVENTURE',
        bossbar: collectBossbar(),
        timeLimit: collectTimeLimit(),
        startupCommands: collectCommands('startup-commands-container'),
        shutdownCommands: collectCommands('shutdown-commands-container'),
        teams: collectTeams(),
        endConditions: collectEndConditions()
    };
    return config;
}

function collectBossbar() {
    const disable = document.getElementById('bossbar-disable');
    if (disable && disable.checked) {
        return null;
    }
    const mcid = document.getElementById('bossbar-mcid')?.value?.trim();
    if (!mcid) {
        return null;
    }
    return { mcid };
}

function collectTimeLimit() {
    const disable = document.getElementById('timelimit-disable');
    if (disable && disable.checked) {
        return null;
    }
    return {
        hours: parseInt(document.getElementById('timelimit-hours')?.value) || 0,
        minutes: parseInt(document.getElementById('timelimit-minutes')?.value) || 0,
        seconds: parseInt(document.getElementById('timelimit-seconds')?.value) || 0
    };
}

function collectCommands(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return [];
    const commands = [];
    container.querySelectorAll('input[type="text"]').forEach(input => {
        const cmd = input.value.trim();
        if (cmd) {
            commands.push(cmd);
        }
    });
    return commands;
}

function collectTeams() {
    const teams = [];
    document.querySelectorAll('.team-card').forEach(card => {
        teams.push(collectTeamFromCard(card));
    });
    return teams;
}

function collectTeamFromCard(card) {
    const hasArmorToggle = card.querySelector('input[name="team-has-armor"]');
    const team = {
        name: card.querySelector('input[name="team-name"]')?.value?.trim() || '',
        displayName: card.querySelector('input[name="team-display-name"]')?.value?.trim() || '',
        armorColor: card.querySelector('select[name="team-color"]')?.value || 'WHITE',
        hasArmor: hasArmorToggle ? hasArmorToggle.checked : true,
        respawnCount: parseInt(card.querySelector('input[name="team-respawn-count"]')?.value) || 0,
        readyLocation: collectReadyLocationFromCard(card),
        respawnLocation: collectRespawnLocationFromCard(card),
        effects: collectEffectsFromContainer(card),
        roles: collectRolesFromCard(card)
    };
    return team;
}

function collectReadyLocationFromCard(card) {
    const toggle = card.querySelector('.team-lobby-toggle');
    if (toggle && !toggle.checked) {
        return null;
    }
    return {
        world: card.querySelector('input[name="team-lobby-world"]')?.value?.trim() || 'world',
        x: parseFloat(card.querySelector('input[name="team-lobby-x"]')?.value) || 0,
        y: parseFloat(card.querySelector('input[name="team-lobby-y"]')?.value) || 64,
        z: parseFloat(card.querySelector('input[name="team-lobby-z"]')?.value) || 0,
        yaw: parseFloat(card.querySelector('input[name="team-lobby-yaw"]')?.value) || 0,
        pitch: parseFloat(card.querySelector('input[name="team-lobby-pitch"]')?.value) || 0,
        waitingTime: {
            hours: parseInt(card.querySelector('input[name="team-lobby-hours"]')?.value) || 0,
            minutes: parseInt(card.querySelector('input[name="team-lobby-minutes"]')?.value) || 0,
            seconds: parseInt(card.querySelector('input[name="team-lobby-seconds"]')?.value) || 0
        }
    };
}

function collectRespawnLocationFromCard(card) {
    return {
        world: card.querySelector('input[name="team-respawn-world"]')?.value?.trim() || 'world',
        x: parseFloat(card.querySelector('input[name="team-respawn-x"]')?.value) || 0,
        y: parseFloat(card.querySelector('input[name="team-respawn-y"]')?.value) || 64,
        z: parseFloat(card.querySelector('input[name="team-respawn-z"]')?.value) || 0,
        yaw: parseFloat(card.querySelector('input[name="team-respawn-yaw"]')?.value) || 0,
        pitch: parseFloat(card.querySelector('input[name="team-respawn-pitch"]')?.value) || 0
    };
}

function collectEffectsFromContainer(container) {
    const effects = [];
    container.querySelectorAll('.effect-card').forEach(card => {
        const effect = {
            name: card.querySelector('select[name="effect-name"]')?.value || '',
            seconds: parseInt(card.querySelector('input[name="effect-duration"]')?.value) || 0,
            amplifier: parseInt(card.querySelector('input[name="effect-level"]')?.value) || 0,
            hideParticles: card.querySelector('input[name="effect-hide-particles"]')?.checked || false
        };
        if (effect.name) {
            effects.push(effect);
        }
    });
    return effects;
}

function collectRolesFromCard(card) {
    const roles = [];
    card.querySelectorAll('.role-card').forEach(roleCard => {
        roles.push(collectRoleFromCard(roleCard));
    });
    return roles;
}

function collectRoleFromCard(card) {
    const inheritColor = card.querySelector('input[name="role-inherit-color"]')?.checked;
    const inheritRespawn = card.querySelector('input[name="role-inherit-respawn"]')?.checked;
    const inheritHasArmor = card.querySelector('input[name="role-inherit-has-armor"]')?.checked;

    const role = {
        name: card.querySelector('input[name="role-name"]')?.value?.trim() || '',
        displayName: card.querySelector('input[name="role-display-name"]')?.value?.trim() || null,
        armorColor: inheritColor ? null : (card.querySelector('select[name="role-color"]')?.value || null),
        hasArmor: inheritHasArmor ? null : (card.querySelector('input[name="role-has-armor"]')?.checked ?? null),
        readyLocation: null,
        respawnLocation: null,
        respawnCount: inheritRespawn ? null : (parseInt(card.querySelector('input[name="role-respawn-count"]')?.value) ?? null),
        effects: collectEffectsFromContainer(card),
        extendsEffects: card.querySelector('input[name="role-extends-effects"]')?.checked || false,
        extendsItem: card.querySelector('input[name="role-extends-item"]')?.checked || false
    };
    return role;
}

function collectEndConditions() {
    const conditions = [];
    document.querySelectorAll('.end-condition-card').forEach(card => {
        const condition = collectEndConditionFromCard(card);
        if (condition) {
            conditions.push(condition);
        }
    });
    return conditions;
}

function collectEndConditionFromCard(card) {
    const typeSelect = card.querySelector('.end-condition-type-select');
    const type = typeSelect?.value || '';
    const message = card.querySelector('input[name="end-condition-message"]')?.value?.trim() || '';

    switch (type) {
        case 'Beacon':
            return {
                type: 'beacon',
                message,
                location: {
                    world: card.querySelector('input[name="beacon-world"]')?.value?.trim() || 'world',
                    x: parseFloat(card.querySelector('input[name="beacon-x"]')?.value) || 0,
                    y: parseFloat(card.querySelector('input[name="beacon-y"]')?.value) || 64,
                    z: parseFloat(card.querySelector('input[name="beacon-z"]')?.value) || 0,
                    yaw: 0,
                    pitch: 0
                },
                hitpoint: parseInt(card.querySelector('input[name="beacon-hitpoint"]')?.value) || 100
            };
        case 'Extermination':
            return {
                type: 'extermination',
                message,
                team: card.querySelector('input[name="extermination-team"]')?.value?.trim() || ''
            };
        case 'Ticket':
            return {
                type: 'ticket',
                message,
                team: card.querySelector('input[name="ticket-team"]')?.value?.trim() || '',
                count: parseInt(card.querySelector('input[name="ticket-count"]')?.value) || 0
            };
        case 'Composite':
            return {
                type: 'composite',
                message,
                operator: 'AND',
                conditions: collectCompositeConditionsFromCard(card)
            };
        default:
            return null;
    }
}

function collectCompositeConditionsFromCard(card) {
    const conditions = [];
    card.querySelectorAll('.composite-condition-card').forEach(subCard => {
        const typeSelect = subCard.querySelector('.composite-sub-type-select');
        const type = typeSelect?.value || '';
        const message = '';

        switch (type) {
            case 'Beacon':
                conditions.push({
                    type: 'beacon',
                    message,
                    location: {
                        world: subCard.querySelector('input[name="composite-beacon-world"]')?.value?.trim() || 'world',
                        x: parseFloat(subCard.querySelector('input[name="composite-beacon-x"]')?.value) || 0,
                        y: parseFloat(subCard.querySelector('input[name="composite-beacon-y"]')?.value) || 64,
                        z: parseFloat(subCard.querySelector('input[name="composite-beacon-z"]')?.value) || 0,
                        yaw: 0,
                        pitch: 0
                    },
                    hitpoint: parseInt(subCard.querySelector('input[name="composite-beacon-hp"]')?.value) || 100
                });
                break;
            case 'Extermination':
                conditions.push({
                    type: 'extermination',
                    message,
                    team: subCard.querySelector('input[name="composite-extermination-team"]')?.value?.trim() || ''
                });
                break;
            case 'Ticket':
                conditions.push({
                    type: 'ticket',
                    message,
                    team: subCard.querySelector('input[name="composite-ticket-team"]')?.value?.trim() || '',
                    count: parseInt(subCard.querySelector('input[name="composite-ticket-count"]')?.value) || 0
                });
                break;
        }
    });
    return conditions;
}

/**
 * トースト通知を表示
 */
function showToast(message, type = 'success') {
    const existing = document.getElementById('toast-notification');
    if (existing) {
        existing.remove();
    }

    const toast = document.createElement('div');
    toast.id = 'toast-notification';
    toast.className = `fixed bottom-24 left-1/2 -translate-x-1/2 px-6 py-3 rounded-xl shadow-lg text-white font-semibold z-50 transition-opacity duration-300 ${type === 'success' ? 'bg-green-600' : 'bg-red-600'}`;
    toast.textContent = message;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('opacity-0');
        setTimeout(() => toast.remove(), 300);
    }, 2000);
}

/**
 * 設定用コマンドを生成してクリップボードにコピー
 */
async function generateAndCopyCommand() {
    const config = collectConfig();
    const encoded = encodeConfig(config);

    const MAX_CHUNK_SIZE = 200;

    if (encoded.length <= MAX_CHUNK_SIZE) {
        // 従来通り1コマンドでコピー
        const command = `/kcdk config import ${encoded}`;
        try {
            await navigator.clipboard.writeText(command);
            showToast('コマンドをクリップボードにコピーしました');
        } catch (err) {
            console.error('クリップボードへのコピーに失敗:', err);
            showToast('コピーに失敗しました', 'error');
        }
        return;
    }

    // 分割コマンドを生成
    const chunks = [];
    for (let i = 0; i < encoded.length; i += MAX_CHUNK_SIZE) {
        chunks.push(encoded.substring(i, i + MAX_CHUNK_SIZE));
    }

    const totalParts = chunks.length;
    const commands = chunks.map((chunk, index) =>
        `/kcdk config import-part ${index + 1}/${totalParts} ${chunk}`
    );

    showCommandModal(commands);
}

function showCommandModal(commands) {
    const list = document.getElementById('command-list');
    list.innerHTML = '';

    commands.forEach((cmd, index) => {
        const item = document.createElement('div');
        item.className = 'flex items-center gap-2 p-3 bg-gray-50 rounded-lg border border-gray-200';

        const label = document.createElement('span');
        label.className = 'text-xs font-semibold text-gray-500 shrink-0';
        label.textContent = `${index + 1}/${commands.length}`;

        const code = document.createElement('code');
        code.className = 'text-sm text-gray-800 break-all flex-1';
        code.textContent = cmd;

        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'shrink-0 rounded-lg bg-indigo-600 px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-indigo-700';
        btn.textContent = 'コピー';
        btn.addEventListener('click', async () => {
            try {
                await navigator.clipboard.writeText(cmd);
                btn.textContent = '✓';
                btn.classList.remove('bg-indigo-600', 'hover:bg-indigo-700');
                btn.classList.add('bg-green-600');
                setTimeout(() => {
                    btn.textContent = 'コピー';
                    btn.classList.remove('bg-green-600');
                    btn.classList.add('bg-indigo-600', 'hover:bg-indigo-700');
                }, 1500);
            } catch (err) {
                showToast('コピーに失敗しました', 'error');
            }
        });

        item.appendChild(label);
        item.appendChild(code);
        item.appendChild(btn);
        list.appendChild(item);
    });

    document.getElementById('command-modal').classList.remove('hidden');
}

function closeCommandModal() {
    document.getElementById('command-modal').classList.add('hidden');
}

// デバッグ用：コンソールでキー対応表を確認可能
if (typeof window !== 'undefined') {
    window.KCDK_KEY_MAP = KEY_MAP;
    window.KCDK_REVERSE_KEY_MAP = REVERSE_KEY_MAP;
    window.KCDK_encodeConfig = encodeConfig;
    window.KCDK_decodeConfig = decodeConfig;
    window.KCDK_collectConfig = collectConfig;
}

// ============================================================
// バリデーション関連
// ============================================================

// エラー表示を追加（入力欄に赤ボーダー）
function showValidationError(input) {
    if (!input) return;
    input.classList.add('border-red-500', 'focus:border-red-500', 'focus:ring-red-500/20');
    input.classList.remove('border-gray-300', 'focus:border-indigo-500', 'focus:ring-indigo-500/20');
}

// エラー表示を削除
function clearValidationError(input) {
    if (!input) return;
    input.classList.remove('border-red-500', 'focus:border-red-500', 'focus:ring-red-500/20');
    input.classList.add('border-gray-300', 'focus:border-indigo-500', 'focus:ring-indigo-500/20');
}

// 全エラーをクリア
function clearAllValidationErrors() {
    document.querySelectorAll('.border-red-500').forEach(input => {
        clearValidationError(input);
    });
}

// 全体のバリデーション（エラーリストを返す）
function validateAll() {
    const errors = [];

    // ボスバー設定：有効ならMCID必須
    const bossbarDisable = document.getElementById('bossbar-disable');
    const bossbarMcid = document.getElementById('bossbar-mcid');
    if (bossbarDisable && !bossbarDisable.checked && bossbarMcid) {
        if (!bossbarMcid.value.trim()) {
            errors.push({ section: '基本設定', message: 'ボスバーが有効な場合、MCIDは必須です', input: bossbarMcid });
        }
    }

    // 制限時間：有効なら時間必須
    const timelimitDisable = document.getElementById('timelimit-disable');
    const timelimitHours = document.getElementById('timelimit-hours');
    const timelimitMinutes = document.getElementById('timelimit-minutes');
    const timelimitSeconds = document.getElementById('timelimit-seconds');
    if (timelimitDisable && !timelimitDisable.checked) {
        const hasTime = (timelimitHours && timelimitHours.value) ||
            (timelimitMinutes && timelimitMinutes.value) ||
            (timelimitSeconds && timelimitSeconds.value);
        if (!hasTime) {
            errors.push({ section: '基本設定', message: '制限時間が有効な場合、時間は必須です', input: timelimitMinutes });
        }
    }

    // チーム設定のバリデーション
    document.querySelectorAll('.team-card').forEach((card, index) => {
        const teamErrors = validateTeamCard(card, index + 1);
        errors.push(...teamErrors);
    });

    // 終了条件：最低1つ必要
    const endConditionCards = document.querySelectorAll('.end-condition-card');
    if (endConditionCards.length === 0) {
        errors.push({ section: '終了条件', message: '終了条件を最低1つ設定してください', input: null });
    } else {
        // 各終了条件のバリデーション
        endConditionCards.forEach((card, index) => {
            const conditionErrors = validateEndConditionCard(card, index + 1);
            errors.push(...conditionErrors);
        });
    }

    return errors;
}

// チームカードのバリデーション（エラーリストを返す）
function validateTeamCard(card, teamNumber) {
    const errors = [];
    const section = `チーム ${teamNumber}`;

    // チーム名必須
    const teamName = card.querySelector('input[name="team-name"]');
    if (teamName && !teamName.value.trim()) {
        errors.push({ section, message: 'チーム名は必須です', input: teamName });
    }

    // 表示名必須
    const displayName = card.querySelector('input[name="team-display-name"]');
    if (displayName && !displayName.value.trim()) {
        errors.push({ section, message: '表示名は必須です', input: displayName });
    }

    // リスポーン可能回数必須
    const respawnCount = card.querySelector('input[name="team-respawn-count"]');
    if (respawnCount && respawnCount.value === '') {
        errors.push({ section, message: 'リスポーン可能回数は必須です', input: respawnCount });
    }

    // 待機地点：有効なら各項目必須
    const lobbyToggle = card.querySelector('.team-lobby-toggle');
    if (lobbyToggle && !lobbyToggle.checked) {
        const lobbyFields = [
            { name: 'team-lobby-world', label: '待機地点のワールド' },
            { name: 'team-lobby-x', label: '待機地点のX座標' },
            { name: 'team-lobby-y', label: '待機地点のY座標' },
            { name: 'team-lobby-z', label: '待機地点のZ座標' },
            { name: 'team-lobby-hours', label: '待機時間（時）' },
            { name: 'team-lobby-minutes', label: '待機時間（分）' },
            { name: 'team-lobby-seconds', label: '待機時間（秒）' }
        ];

        lobbyFields.forEach(field => {
            const input = card.querySelector(`input[name="${field.name}"]`);
            if (input && input.value === '') {
                errors.push({ section, message: `${field.label}は必須です`, input });
            }
        });
    }

    // リスポーン地点：各項目必須
    const respawnFields = [
        { name: 'team-respawn-world', label: 'リスポーン地点のワールド' },
        { name: 'team-respawn-x', label: 'リスポーン地点のX座標' },
        { name: 'team-respawn-y', label: 'リスポーン地点のY座標' },
        { name: 'team-respawn-z', label: 'リスポーン地点のZ座標' }
    ];

    respawnFields.forEach(field => {
        const input = card.querySelector(`input[name="${field.name}"]`);
        if (input && input.value === '') {
            errors.push({ section, message: `${field.label}は必須です`, input });
        }
    });

    // ロールのバリデーション
    card.querySelectorAll('.role-card').forEach((roleCard, index) => {
        const roleErrors = validateRoleCard(roleCard, teamNumber, index + 1);
        errors.push(...roleErrors);
    });

    // エフェクトのバリデーション
    card.querySelectorAll('.effect-card').forEach((effectCard, index) => {
        const effectErrors = validateEffectCard(effectCard, teamNumber, index + 1);
        errors.push(...effectErrors);
    });

    return errors;
}

// ロールカードのバリデーション（エラーリストを返す）
function validateRoleCard(card, teamNumber, roleNumber) {
    const errors = [];
    const section = `チーム ${teamNumber} ロール ${roleNumber}`;

    // ロール名必須
    const roleName = card.querySelector('input[name="role-name"]');
    if (roleName && !roleName.value.trim()) {
        errors.push({ section, message: 'ロール名は必須です', input: roleName });
    }

    // 表示名必須
    const displayName = card.querySelector('input[name="role-display-name"]');
    if (displayName && !displayName.value.trim()) {
        errors.push({ section, message: '表示名は必須です', input: displayName });
    }

    // 継承していない場合は各項目必須
    // アーマーカラー
    const colorInherit = card.querySelector('input[name="role-inherit-color"]');
    const colorInput = card.querySelector('select[name="role-color"]');
    if (colorInherit && !colorInherit.checked && colorInput && !colorInput.value) {
        errors.push({ section, message: 'アーマーカラーは必須です', input: colorInput });
    }

    // リスポーン回数
    const respawnInherit = card.querySelector('input[name="role-inherit-respawn"]');
    const respawnInput = card.querySelector('input[name="role-respawn-count"]');
    if (respawnInherit && !respawnInherit.checked && respawnInput && respawnInput.value === '') {
        errors.push({ section, message: 'リスポーン回数は必須です', input: respawnInput });
    }

    return errors;
}

// エフェクトカードのバリデーション（エラーリストを返す）
function validateEffectCard(card, teamNumber, effectNumber) {
    const errors = [];
    const section = `チーム ${teamNumber} エフェクト ${effectNumber}`;

    const effectName = card.querySelector('select[name="effect-name"]');
    const effectDuration = card.querySelector('input[name="effect-duration"]');
    const effectLevel = card.querySelector('input[name="effect-level"]');

    if (effectName && !effectName.value) {
        errors.push({ section, message: 'エフェクトを選択してください', input: effectName });
    }

    if (effectDuration && effectDuration.value === '') {
        errors.push({ section, message: '持続時間は必須です', input: effectDuration });
    }

    if (effectLevel && effectLevel.value === '') {
        errors.push({ section, message: 'レベルは必須です', input: effectLevel });
    }

    return errors;
}

// 終了条件カードのバリデーション（エラーリストを返す）
function validateEndConditionCard(card, conditionNumber) {
    const errors = [];
    const section = `終了条件 ${conditionNumber}`;

    const typeSelect = card.querySelector('.end-condition-type-select');
    const type = typeSelect ? typeSelect.value : '';

    switch (type) {
        case 'Beacon':
            const beaconFieldDefs = [
                { name: 'beacon-world', label: 'ワールド' },
                { name: 'beacon-x', label: 'X座標' },
                { name: 'beacon-y', label: 'Y座標' },
                { name: 'beacon-z', label: 'Z座標' },
                { name: 'beacon-hitpoint', label: 'HP' }
            ];
            beaconFieldDefs.forEach(field => {
                const input = card.querySelector(`input[name="${field.name}"]`);
                if (input && input.value === '') {
                    errors.push({ section, message: `${field.label}は必須です`, input });
                }
            });
            break;
        case 'Extermination':
            const extTeam = card.querySelector('input[name="extermination-team"]');
            if (extTeam && !extTeam.value.trim()) {
                errors.push({ section, message: 'チーム名は必須です', input: extTeam });
            }
            break;
        case 'Ticket':
            const ticketTeam = card.querySelector('input[name="ticket-team"]');
            const ticketCount = card.querySelector('input[name="ticket-count"]');
            if (ticketTeam && !ticketTeam.value.trim()) {
                errors.push({ section, message: 'チーム名は必須です', input: ticketTeam });
            }
            if (ticketCount && ticketCount.value === '') {
                errors.push({ section, message: 'チケット数は必須です', input: ticketCount });
            }
            break;
        case 'Composite':
            // Compositeは最低2つの条件が必要
            const compositeConditions = card.querySelectorAll('.composite-condition-card');
            if (compositeConditions.length < 2) {
                errors.push({ section, message: 'Composite条件は最低2つの条件が必要です', input: null });
            }
            // 各Composite内の条件もバリデーション
            compositeConditions.forEach((subCard, subIndex) => {
                const subErrors = validateCompositeSubCondition(subCard, conditionNumber, subIndex + 1);
                errors.push(...subErrors);
            });
            break;
    }

    return errors;
}

// Composite内のサブ条件のバリデーション
function validateCompositeSubCondition(card, conditionNumber, subIndex) {
    const errors = [];
    const section = `終了条件 ${conditionNumber} サブ条件 ${subIndex}`;

    const typeSelect = card.querySelector('.composite-sub-type-select');
    const type = typeSelect ? typeSelect.value : '';

    switch (type) {
        case 'Beacon':
            const beaconFieldDefs = [
                { name: 'composite-beacon-world', label: 'ワールド' },
                { name: 'composite-beacon-x', label: 'X座標' },
                { name: 'composite-beacon-y', label: 'Y座標' },
                { name: 'composite-beacon-z', label: 'Z座標' },
                { name: 'composite-beacon-hp', label: 'HP' }
            ];
            beaconFieldDefs.forEach(field => {
                const input = card.querySelector(`input[name="${field.name}"]`);
                if (input && input.value === '') {
                    errors.push({ section, message: `${field.label}は必須です`, input });
                }
            });
            break;
        case 'Extermination':
            const extTeam = card.querySelector('input[name="composite-extermination-team"]');
            if (extTeam && !extTeam.value.trim()) {
                errors.push({ section, message: 'チーム名は必須です', input: extTeam });
            }
            break;
        case 'Ticket':
            const ticketTeam = card.querySelector('input[name="composite-ticket-team"]');
            const ticketCount = card.querySelector('input[name="composite-ticket-count"]');
            if (ticketTeam && !ticketTeam.value.trim()) {
                errors.push({ section, message: 'チーム名は必須です', input: ticketTeam });
            }
            if (ticketCount && ticketCount.value === '') {
                errors.push({ section, message: 'チケット数は必須です', input: ticketCount });
            }
            break;
    }

    return errors;
}

// バリデーション警告モーダルを表示
function showValidationWarningModal(errors) {
    const modal = document.getElementById('validation-warning-modal');
    const errorList = document.getElementById('validation-error-list');

    if (!modal || !errorList) return;

    // エラーをクリア
    clearAllValidationErrors();

    // エラー一覧を作成
    errorList.innerHTML = '';
    errors.forEach(error => {
        const item = document.createElement('div');
        item.className = 'flex items-start gap-2 py-1';
        item.innerHTML = `
            <svg class="h-4 w-4 text-red-500 mt-0.5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
            </svg>
            <div>
                <span class="font-semibold text-red-700 text-sm">${error.section}</span>
                <span class="text-red-600 text-sm">: ${error.message}</span>
            </div>
        `;
        errorList.appendChild(item);

        // 入力欄にエラースタイルを追加
        if (error.input) {
            showValidationError(error.input);
        }
    });

    // モーダルを表示
    modal.classList.remove('hidden');
}

// setupValidationListenersは不要になったので削除（ボタンクリック時にバリデーションを実行）
function setupValidationListeners() {
    // 何もしない（互換性のため残す）
}

function setupToggleDisable(toggleId, targetIds) {
    const toggle = document.getElementById(toggleId);
    const targets = targetIds.map(id => document.getElementById(id));

    function updateState() {
        const isDisabled = toggle.checked;
        targets.forEach(target => {
            if (target) {
                target.disabled = isDisabled;
                if (isDisabled) {
                    target.classList.add('opacity-50', 'cursor-not-allowed', 'bg-gray-100');
                } else {
                    target.classList.remove('opacity-50', 'cursor-not-allowed', 'bg-gray-100');
                }
            }
        });
    }

    toggle.addEventListener('change', updateState);
    updateState();
}

// 座標ペーストボタン処理
// /point の出力 "x y z" or "x y z yaw pitch" をパースして入力欄に反映
function setupPasteButton(btn, prefix) {
    btn.addEventListener('click', async () => {
        try {
            const text = await navigator.clipboard.readText();
            const parts = text.trim().split(/\s+/);
            if (parts.length < 3) {
                alert('座標形式が不正です。"x y z" または "x y z yaw pitch" の形式でペーストしてください。');
                return;
            }
            // カード全体から該当prefixの入力欄を検索
            const card = btn.closest('.team-card, .end-condition-card, .role-card, .composite-sub-card')
                      || btn.closest('[class*="fields"]')
                      || btn.parentElement.parentElement;
            if (!card) return;
            const xInput = card.querySelector(`input[name="${prefix}-x"]`);
            const yInput = card.querySelector(`input[name="${prefix}-y"]`);
            const zInput = card.querySelector(`input[name="${prefix}-z"]`);
            if (xInput) xInput.value = parts[0];
            if (yInput) yInput.value = parts[1];
            if (zInput) zInput.value = parts[2];
            if (parts.length >= 5) {
                const yawInput = card.querySelector(`input[name="${prefix}-yaw"]`);
                const pitchInput = card.querySelector(`input[name="${prefix}-pitch"]`);
                if (yawInput) yawInput.value = parts[3];
                if (pitchInput) pitchInput.value = parts[4];
            }
        } catch (e) {
            alert('クリップボードの読み取りに失敗しました。ブラウザの権限を確認してください。');
        }
    });
}

// チーム管理
const defaultColors = ['RED', 'BLUE', 'GREEN', 'YELLOW', 'LIGHT_PURPLE', 'GOLD', 'AQUA', 'DARK_GREEN'];

// アーマーカラーを薄くした背景色を生成
function hexToRgba(hex, alpha) {
    const r = parseInt(hex.slice(1, 3), 16);
    const g = parseInt(hex.slice(3, 5), 16);
    const b = parseInt(hex.slice(5, 7), 16);
    return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

const MC_COLOR_HEX = {
    BLACK: '#1d1d21', DARK_BLUE: '#3c44aa', DARK_GREEN: '#5e7c16', DARK_AQUA: '#169c9c',
    DARK_RED: '#b02e26', DARK_PURPLE: '#8932b8', GOLD: '#f9801d', GRAY: '#9d9d97',
    DARK_GRAY: '#474f52', BLUE: '#3ab3da', GREEN: '#80c71f', AQUA: '#3ab3da',
    RED: '#b02e26', LIGHT_PURPLE: '#c74ebd', YELLOW: '#fed83d', WHITE: '#f9fffe'
};

// チームカードの背景色を更新
function updateTeamCardBackground(card) {
    const colorInput = card.querySelector('select[name="team-color"]');
    if (colorInput) {
        const hex = MC_COLOR_HEX[colorInput.value] || '#f9fffe';
        card.style.backgroundColor = hexToRgba(hex, 0.1);
        card.style.borderColor = hexToRgba(hex, 0.3);
    }
}

// アーマーカラー変更時のイベント設定
function setupTeamColorChange(card) {
    const colorInput = card.querySelector('select[name="team-color"]');
    if (colorInput) {
        colorInput.addEventListener('change', () => {
            updateTeamCardBackground(card);
        });
        // 初期背景色を設定
        updateTeamCardBackground(card);
    }
}

function getTeamCount() {
    return document.querySelectorAll('.team-card').length;
}

function updateTeamNumbers() {
    const cards = document.querySelectorAll('.team-card');
    cards.forEach((card, index) => {
        const title = card.querySelector('h3');
        if (title) {
            title.textContent = `チーム ${index + 1}`;
        }
    });
    updateDeleteButtons();
}

function updateDeleteButtons() {
    const cards = document.querySelectorAll('.team-card');
    const canDelete = cards.length > 2;
    cards.forEach(card => {
        const deleteBtn = card.querySelector('.delete-team-btn');
        if (deleteBtn) {
            deleteBtn.classList.toggle('hidden', !canDelete);
        }
    });
}

function createTeamCard(teamNumber) {
    const colorIndex = (teamNumber - 1) % defaultColors.length;
    const card = document.createElement('div');
    card.className = 'team-card w-full rounded-2xl border border-gray-200 bg-white px-6 py-6 shadow-sm';
    card.innerHTML = `
        <div class="mb-4 flex items-center justify-between">
            <h3 class="text-lg font-bold tracking-tight text-gray-900">チーム ${teamNumber}</h3>
            <button type="button" class="delete-team-btn text-gray-400 hover:text-red-500 transition">
                <svg class="h-5 w-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path d="M6 18L18 6M6 6l12 12" />
                </svg>
            </button>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <!-- 基本情報 -->
            <div class="space-y-4">
                <div>
                    <label class="mb-2 block text-sm font-semibold text-gray-800">チーム名</label>
                    <input type="text" name="team-name" placeholder="例: Team ${teamNumber}" class="block w-full rounded-xl border border-gray-300 bg-white px-4 py-2.5 text-gray-800 shadow-sm transition placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-4 focus:ring-indigo-500/20" />
                </div>
                <div>
                    <label class="mb-2 block text-sm font-semibold text-gray-800">表示名</label>
                    <input type="text" name="team-display-name" placeholder="例: チーム${teamNumber}" class="block w-full rounded-xl border border-gray-300 bg-white px-4 py-2.5 text-gray-800 shadow-sm transition placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-4 focus:ring-indigo-500/20" />
                </div>
                <div>
                    <label class="mb-2 block text-sm font-semibold text-gray-800">アーマーカラー</label>
                    <select name="team-color" class="block w-full rounded-xl border border-gray-300 bg-white px-4 py-2.5 text-gray-800 shadow-sm transition focus:border-indigo-500 focus:outline-none focus:ring-4 focus:ring-indigo-500/20">
                        <option value="BLACK">BLACK</option>
                        <option value="DARK_BLUE">DARK_BLUE</option>
                        <option value="DARK_GREEN">DARK_GREEN</option>
                        <option value="DARK_AQUA">DARK_AQUA</option>
                        <option value="DARK_RED">DARK_RED</option>
                        <option value="DARK_PURPLE">DARK_PURPLE</option>
                        <option value="GOLD">GOLD</option>
                        <option value="GRAY">GRAY</option>
                        <option value="DARK_GRAY">DARK_GRAY</option>
                        <option value="BLUE">BLUE</option>
                        <option value="GREEN">GREEN</option>
                        <option value="AQUA">AQUA</option>
                        <option value="RED" selected>RED</option>
                        <option value="LIGHT_PURPLE">LIGHT_PURPLE</option>
                        <option value="YELLOW">YELLOW</option>
                        <option value="WHITE">WHITE</option>
                    </select>
                </div>
                <div>
                    <label class="mb-2 block text-sm font-semibold text-gray-800">リスポーン可能回数(-1=無限)</label>
                    <input type="number" name="team-respawn-count" min="-1" placeholder="-1 = 無限" value="0" class="block w-full rounded-xl border border-gray-300 bg-white px-4 py-2.5 text-gray-800 shadow-sm transition placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-4 focus:ring-indigo-500/20" />
                </div>
                <div class="flex items-center justify-between">
                    <label class="text-sm font-semibold text-gray-800">防具を装備する</label>
                    <label class="inline-flex cursor-pointer items-center">
                        <input type="checkbox" name="team-has-armor" class="peer sr-only" checked />
                        <div class="relative h-5 w-9 rounded-full bg-gray-300 transition-colors duration-200 peer-checked:bg-indigo-600 after:absolute after:left-0.5 after:top-0.5 after:h-4 after:w-4 after:rounded-full after:bg-white after:transition-transform after:duration-200 peer-checked:after:translate-x-4"></div>
                    </label>
                </div>
            </div>

            <!-- 待機地点 -->
            <div class="space-y-3 rounded-xl border border-gray-200 bg-gray-50 p-4">
                <div class="flex items-center justify-between">
                    <span class="text-sm font-semibold text-gray-800">待機地点</span>
                    <label class="inline-flex cursor-pointer items-center gap-2">
                        <span class="text-xs text-gray-500">無効</span>
                        <input type="checkbox" name="team-lobby-disable" class="peer sr-only team-lobby-toggle" />
                        <div class="relative h-5 w-9 rounded-full bg-gray-300 transition-colors duration-200 peer-checked:bg-indigo-600 after:absolute after:left-0.5 after:top-0.5 after:h-4 after:w-4 after:rounded-full after:bg-white after:transition-transform after:duration-200 peer-checked:after:translate-x-4"></div>
                    </label>
                </div>
                <div class="team-lobby-fields space-y-2">
                    <div>
                        <label class="mb-1 block text-xs text-gray-500">ワールド</label>
                        <input type="text" name="team-lobby-world" placeholder="world" value="world" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                    </div>
                    <div>
                        <div class="flex items-center justify-between mb-1">
                            <label class="block text-xs text-gray-500">座標 (X / Y / Z)</label>
                            <button type="button" class="paste-coord-btn text-xs text-indigo-500 hover:text-indigo-700" data-prefix="team-lobby">📋 ペースト</button>
                        </div>
                        <div class="grid grid-cols-3 gap-2">
                            <input type="number" name="team-lobby-x" placeholder="X" step="0.01" value="0" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                            <input type="number" name="team-lobby-y" placeholder="Y" step="0.01" value="64" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                            <input type="number" name="team-lobby-z" placeholder="Z" step="0.01" value="0" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                        </div>
                    </div>
                    <div>
                        <label class="mb-1 block text-xs text-gray-500">向き (Yaw / Pitch)</label>
                        <div class="grid grid-cols-2 gap-2">
                            <input type="number" name="team-lobby-yaw" placeholder="Yaw" step="0.1" value="0" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                            <input type="number" name="team-lobby-pitch" placeholder="Pitch" step="0.1" value="180" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                        </div>
                    </div>
                    <div>
                        <label class="mb-1 block text-xs text-gray-500">待機時間 (HH:MM:SS)</label>
                        <div class="grid grid-cols-3 gap-2">
                            <input type="number" name="team-lobby-hours" min="0" max="99" placeholder="HH" value="0" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                            <input type="number" name="team-lobby-minutes" min="0" max="59" placeholder="MM" value="3" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                            <input type="number" name="team-lobby-seconds" min="0" max="59" placeholder="SS" value="0" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                        </div>
                    </div>
                </div>
            </div>

            <!-- リスポーン地点 -->
            <div class="space-y-3 rounded-xl border border-gray-200 bg-gray-50 p-4">
                <span class="text-sm font-semibold text-gray-800">リスポーン地点</span>
                <div class="space-y-2">
                    <div>
                        <label class="mb-1 block text-xs text-gray-500">ワールド</label>
                        <input type="text" name="team-respawn-world" placeholder="world" value="world" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                    </div>
                    <div>
                        <div class="flex items-center justify-between mb-1">
                            <label class="block text-xs text-gray-500">座標 (X / Y / Z)</label>
                            <button type="button" class="paste-coord-btn text-xs text-indigo-500 hover:text-indigo-700" data-prefix="team-respawn">📋 ペースト</button>
                        </div>
                        <div class="grid grid-cols-3 gap-2">
                            <input type="number" name="team-respawn-x" placeholder="X" step="0.01" value="0" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                            <input type="number" name="team-respawn-y" placeholder="Y" step="0.01" value="64" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                            <input type="number" name="team-respawn-z" placeholder="Z" step="0.01" value="0" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                        </div>
                    </div>
                    <div>
                        <label class="mb-1 block text-xs text-gray-500">向き (Yaw / Pitch)</label>
                        <div class="grid grid-cols-2 gap-2">
                            <input type="number" name="team-respawn-yaw" placeholder="Yaw" step="0.1" value="0" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                            <input type="number" name="team-respawn-pitch" placeholder="Pitch" step="0.1" value="180" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <!-- エフェクト -->
        <div class="mt-4 rounded-xl border border-gray-200 bg-gray-50 p-4">
            <span class="block text-sm font-semibold text-gray-800 mb-3">エフェクト</span>
            <div class="effects-container flex flex-wrap items-center gap-2">
                <!-- エフェクトがここに追加される -->
                <button type="button" class="add-effect-btn flex items-center justify-center w-48 h-42 rounded-lg border-2 border-dashed border-gray-300 text-gray-400 transition hover:border-indigo-400 hover:text-indigo-500">
                    <svg class="h-6 w-6" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                        <path d="M12 4v16m8-8H4" />
                    </svg>
                </button>
            </div>
        </div>
        <!-- ロール -->
        <div class="mt-4 rounded-xl border border-gray-200 bg-gray-50 p-4">
            <span class="block text-sm font-semibold text-gray-800 mb-3">ロール</span>
            <div class="roles-container flex flex-col gap-2">
                <!-- ロールがここに追加される -->
                <button type="button"
                    class="add-role-btn flex items-center justify-center h-20 rounded-lg border-2 border-dashed border-gray-300 text-gray-400 transition hover:border-indigo-400 hover:text-indigo-500">
                    <svg class="h-6 w-6" fill="none" stroke="currentColor" stroke-width="2"
                        viewBox="0 0 24 24">
                        <path d="M12 4v16m8-8H4" />
                    </svg>
                    <span class="font-semibold">ロールを追加</span>
                </button>
            </div>
        </div>
    `;

    // 削除ボタンのイベント
    const deleteBtn = card.querySelector('.delete-team-btn');
    deleteBtn.addEventListener('click', () => {
        card.remove();
        updateTeamNumbers();
    });

    // 待機地点トグルのイベント
    setupTeamLobbyToggle(card);

    // エフェクト管理のイベント
    setupEffectManagement(card);

    // ロール管理のイベント
    setupRoleManagement(card);

    // 座標ペーストボタン設定
    card.querySelectorAll('.paste-coord-btn').forEach(btn => {
        setupPasteButton(btn, btn.dataset.prefix);
    });

    // デフォルトカラー設定
    const colorSelect = card.querySelector('select[name="team-color"]');
    if (colorSelect) {
        colorSelect.value = defaultColors[colorIndex];
    }

    // アーマーカラー背景色の設定
    setupTeamColorChange(card);

    return card;
}

// 待機地点トグルの設定
function setupTeamLobbyToggle(card) {
    const toggle = card.querySelector('.team-lobby-toggle');
    const fields = card.querySelector('.team-lobby-fields');
    if (!toggle || !fields) return;

    function updateState() {
        const isDisabled = toggle.checked;
        const inputs = fields.querySelectorAll('input');
        inputs.forEach(input => {
            input.disabled = isDisabled;
            if (isDisabled) {
                input.classList.add('opacity-50', 'cursor-not-allowed', 'bg-gray-100');
            } else {
                input.classList.remove('opacity-50', 'cursor-not-allowed', 'bg-gray-100');
            }
        });
    }

    toggle.addEventListener('change', updateState);
    updateState();
}

function setupTeamManagement() {
    const container = document.getElementById('teams-container');
    const addBtn = document.getElementById('add-team-btn');

    if (!container || !addBtn) return;

    // 既存のチームカードにイベントを設定
    document.querySelectorAll('.team-card').forEach(card => {
        // 削除ボタン
        const deleteBtn = card.querySelector('.delete-team-btn');
        if (deleteBtn) {
            deleteBtn.addEventListener('click', () => {
                card.remove();
                updateTeamNumbers();
            });
        }
        // 待機地点トグル
        setupTeamLobbyToggle(card);
        // アーマーカラー背景色
        setupTeamColorChange(card);
        // 座標ペーストボタン
        card.querySelectorAll('.paste-coord-btn').forEach(btn => {
            setupPasteButton(btn, btn.dataset.prefix);
        });
    });

    // 追加ボタン
    addBtn.addEventListener('click', () => {
        const teamNumber = getTeamCount() + 1;
        const newCard = createTeamCard(teamNumber);
        // 追加ボタンの前に挿入
        addBtn.before(newCard);
        updateDeleteButtons();
    });

    updateDeleteButtons();
}

// エフェクト管理
const minecraftEffects = [
    { id: 'speed', name: '移動速度上昇' },
    { id: 'slowness', name: '移動速度低下' },
    { id: 'haste', name: '採掘速度上昇' },
    { id: 'mining_fatigue', name: '採掘速度低下' },
    { id: 'strength', name: '攻撃力上昇' },
    { id: 'instant_health', name: '即時回復' },
    { id: 'instant_damage', name: '即時ダメージ' },
    { id: 'jump_boost', name: '跳躍力上昇' },
    { id: 'nausea', name: '吐き気' },
    { id: 'regeneration', name: '再生能力' },
    { id: 'resistance', name: 'ダメージ軽減' },
    { id: 'fire_resistance', name: '火炎耐性' },
    { id: 'water_breathing', name: '水中呼吸' },
    { id: 'invisibility', name: '透明化' },
    { id: 'blindness', name: '盲目' },
    { id: 'night_vision', name: '暗視' },
    { id: 'hunger', name: '空腹' },
    { id: 'weakness', name: '弱体化' },
    { id: 'poison', name: '毒' },
    { id: 'wither', name: '衰弱' },
    { id: 'health_boost', name: '体力増強' },
    { id: 'absorption', name: '衝撃吸収' },
    { id: 'saturation', name: '満腹度回復' },
    { id: 'glowing', name: '発光' },
    { id: 'levitation', name: '浮遊' },
    { id: 'luck', name: '幸運' },
    { id: 'unluck', name: '不運' },
    { id: 'slow_falling', name: '落下速度低下' },
    { id: 'conduit_power', name: 'コンジットパワー' },
    { id: 'dolphins_grace', name: 'イルカの好意' },
    { id: 'bad_omen', name: '不吉な予感' },
    { id: 'hero_of_the_village', name: '村の英雄' },
    { id: 'darkness', name: '暗闇' },
    { id: 'trial_omen', name: 'トライアルの不吉な予感' },
    { id: 'raid_omen', name: '襲撃の不吉な予感' },
    { id: 'wind_charged', name: '風チャージ' },
    { id: 'weaving', name: 'ウィービング' },
    { id: 'oozing', name: 'ウージング' },
    { id: 'infested', name: '寄生' }
];

function createEffectRow() {
    const row = document.createElement('div');
    row.className = 'effect-card rounded-lg bg-white p-3 border border-gray-200 w-48';

    const optionsHtml = minecraftEffects.map(e =>
        `<option value="${e.id}">${e.name}</option>`
    ).join('');

    row.innerHTML = `
        <div class="flex items-center justify-between mb-2">
            <span class="text-xs font-semibold text-gray-600">エフェクト</span>
            <button type="button" class="delete-effect-btn text-gray-400 hover:text-red-500 transition">
                <svg class="h-4 w-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path d="M6 18L18 6M6 6l12 12" />
                </svg>
            </button>
        </div>
        <div class="space-y-2">
            <select name="effect-name" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm text-gray-800 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
                <option value="">選択してください</option>
                ${optionsHtml}
            </select>
            <input type="number" name="effect-duration" placeholder="持続時間(秒)" min="1" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
            <input type="number" name="effect-level" placeholder="レベル" min="1" max="255" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
        </div>
    `;

    // 削除ボタンのイベント
    const deleteBtn = row.querySelector('.delete-effect-btn');
    deleteBtn.addEventListener('click', () => {
        row.remove();
    });

    return row;
}

function setupEffectManagement(card) {
    const addBtn = card.querySelector('.add-effect-btn');
    const container = card.querySelector('.effects-container');

    if (!addBtn || !container) return;

    addBtn.addEventListener('click', () => {
        const newRow = createEffectRow();
        // 追加ボタンの前に挿入
        container.insertBefore(newRow, addBtn);
    });
}

// ロール管理
function createRoleCard() {
    const card = document.createElement('div');
    card.className = 'role-card rounded-lg bg-white p-4 border border-gray-200 w-full';

    card.innerHTML = `
        <div class="flex items-center justify-between mb-3">
            <span class="text-sm font-semibold text-gray-800">ロール</span>
            <button type="button" class="delete-role-btn text-gray-400 hover:text-red-500 transition">
                <svg class="h-4 w-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path d="M6 18L18 6M6 6l12 12" />
                </svg>
            </button>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 xl:grid-cols-7 gap-3">
            <!-- ロール名・表示名 -->
            <div class="space-y-2">
                <div>
                    <label class="mb-1 block text-xs font-semibold text-gray-600">ロール名</label>
                    <input type="text" name="role-name" placeholder="例: captain" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                </div>
                <div>
                    <label class="mb-1 block text-xs font-semibold text-gray-600">表示名</label>
                    <input type="text" name="role-display-name" placeholder="例: 大将" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                </div>
            </div>
            <!-- アーマーカラー・リスポーン回数 -->
            <div class="space-y-2">
                <div class="role-inherit-section" data-field="color">
                    <div class="flex items-center justify-between mb-1">
                        <label class="text-xs font-semibold text-gray-600">アーマーカラー</label>
                        <label class="inline-flex cursor-pointer items-center gap-1">
                            <span class="text-xs text-gray-400">所属チームの設定を継承</span>
                            <input type="checkbox" name="role-inherit-color" class="peer sr-only role-inherit-toggle" checked />
                            <div class="relative h-4 w-7 rounded-full bg-gray-300 transition-colors duration-200 peer-checked:bg-indigo-600 after:absolute after:left-0.5 after:top-0.5 after:h-3 after:w-3 after:rounded-full after:bg-white after:transition-transform after:duration-200 peer-checked:after:translate-x-3"></div>
                        </label>
                    </div>
                    <select name="role-color" class="role-inherit-field block w-full rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm text-gray-800 opacity-50 cursor-not-allowed" disabled>
                        <option value="BLACK">BLACK</option>
                        <option value="DARK_BLUE">DARK_BLUE</option>
                        <option value="DARK_GREEN">DARK_GREEN</option>
                        <option value="DARK_AQUA">DARK_AQUA</option>
                        <option value="DARK_RED">DARK_RED</option>
                        <option value="DARK_PURPLE">DARK_PURPLE</option>
                        <option value="GOLD">GOLD</option>
                        <option value="GRAY">GRAY</option>
                        <option value="DARK_GRAY">DARK_GRAY</option>
                        <option value="BLUE">BLUE</option>
                        <option value="GREEN">GREEN</option>
                        <option value="AQUA">AQUA</option>
                        <option value="RED" selected>RED</option>
                        <option value="LIGHT_PURPLE">LIGHT_PURPLE</option>
                        <option value="YELLOW">YELLOW</option>
                        <option value="WHITE">WHITE</option>
                    </select>
                </div>
                <div class="role-inherit-section" data-field="has-armor">
                    <div class="flex items-center justify-between mb-1">
                        <label class="text-xs font-semibold text-gray-600">防具を装備する</label>
                        <label class="inline-flex cursor-pointer items-center gap-1">
                            <span class="text-xs text-gray-400">所属チームの設定を継承</span>
                            <input type="checkbox" name="role-inherit-has-armor" class="peer sr-only role-inherit-toggle" checked />
                            <div class="relative h-4 w-7 rounded-full bg-gray-300 transition-colors duration-200 peer-checked:bg-indigo-600 after:absolute after:left-0.5 after:top-0.5 after:h-3 after:w-3 after:rounded-full after:bg-white after:transition-transform after:duration-200 peer-checked:after:translate-x-3"></div>
                        </label>
                    </div>
                    <label class="role-inherit-field inline-flex cursor-pointer items-center opacity-50">
                        <input type="checkbox" name="role-has-armor" class="peer sr-only" checked disabled />
                        <div class="relative h-4 w-7 rounded-full bg-gray-300 transition-colors duration-200 peer-checked:bg-indigo-600 after:absolute after:left-0.5 after:top-0.5 after:h-3 after:w-3 after:rounded-full after:bg-white after:transition-transform after:duration-200 peer-checked:after:translate-x-3"></div>
                    </label>
                </div>
                <div class="role-inherit-section" data-field="respawn">
                    <div class="flex items-center justify-between mb-1">
                        <label class="text-xs font-semibold text-gray-600">リスポーン回数（-1=無限）</label>
                        <label class="inline-flex cursor-pointer items-center gap-1">
                            <span class="text-xs text-gray-400">所属チームの設定を継承</span>
                            <input type="checkbox" name="role-inherit-respawn" class="peer sr-only role-inherit-toggle" checked />
                            <div class="relative h-4 w-7 rounded-full bg-gray-300 transition-colors duration-200 peer-checked:bg-indigo-600 after:absolute after:left-0.5 after:top-0.5 after:h-3 after:w-3 after:rounded-full after:bg-white after:transition-transform after:duration-200 peer-checked:after:translate-x-3"></div>
                        </label>
                    </div>
                    <input type="number" name="role-respawn-count" min="-1" value=0 class="role-inherit-field block w-full rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 opacity-50 cursor-not-allowed bg-gray-100" disabled />
                </div>
            </div>
            <!-- 待機地点 -->
            <div class="role-inherit-section" data-field="lobby">
                <div class="flex items-center justify-between mb-1">
                    <label class="text-xs font-semibold text-gray-600">待機地点</label>
                    <label class="inline-flex cursor-pointer items-center gap-1">
                        <span class="text-xs text-gray-400">所属チームの設定を継承</span>
                        <input type="checkbox" name="role-inherit-lobby" class="peer sr-only role-inherit-toggle" checked />
                        <div class="relative h-4 w-7 rounded-full bg-gray-300 transition-colors duration-200 peer-checked:bg-indigo-600 after:absolute after:left-0.5 after:top-0.5 after:h-3 after:w-3 after:rounded-full after:bg-white after:transition-transform after:duration-200 peer-checked:after:translate-x-3"></div>
                    </label>
                </div>
                <div class="role-inherit-field-group space-y-1 opacity-50">
                    <input type="text" name="role-lobby-world" placeholder="world" value="world" class="role-inherit-field block w-full rounded-lg border border-gray-300 bg-gray-100 px-2 py-1 text-xs text-gray-800 placeholder:text-gray-400" disabled />
                    <div class="flex items-center justify-between">
                        <span class="text-xs text-gray-400">座標</span>
                        <button type="button" class="paste-coord-btn text-xs text-indigo-500 hover:text-indigo-700" data-prefix="role-lobby">📋 ペースト</button>
                    </div>
                    <div class="grid grid-cols-3 gap-1">
                        <input type="number" name="role-lobby-x" placeholder="X" step="0.01" class="role-inherit-field block w-full rounded-lg border border-gray-300 bg-gray-100 px-2 py-1 text-xs" disabled />
                        <input type="number" name="role-lobby-y" placeholder="Y" step="0.01" class="role-inherit-field block w-full rounded-lg border border-gray-300 bg-gray-100 px-2 py-1 text-xs" disabled />
                        <input type="number" name="role-lobby-z" placeholder="Z" step="0.01" class="role-inherit-field block w-full rounded-lg border border-gray-300 bg-gray-100 px-2 py-1 text-xs" disabled />
                    </div>
                    <div class="grid grid-cols-2 gap-1">
                        <input type="number" name="role-lobby-yaw" placeholder="Yaw" step="0.1" class="role-inherit-field block w-full rounded-lg border border-gray-300 bg-gray-100 px-2 py-1 text-xs" disabled />
                        <input type="number" name="role-lobby-pitch" placeholder="Pitch" step="0.1" class="role-inherit-field block w-full rounded-lg border border-gray-300 bg-gray-100 px-2 py-1 text-xs" disabled />
                    </div>
                    <label class="text-xs font-semibold text-gray-600">待機時間</label>
                    <div class="grid grid-cols-3 gap-1">
                        <input type="number" name="role-lobby-hours" min="0" max="99" placeholder="HH" class="role-inherit-field block w-full rounded-lg border border-gray-300 bg-gray-100 px-2 py-1 text-xs" disabled />
                        <input type="number" name="role-lobby-minutes" min="0" max="59" placeholder="MM" class="role-inherit-field block w-full rounded-lg border border-gray-300 bg-gray-100 px-2 py-1 text-xs" disabled />
                        <input type="number" name="role-lobby-seconds" min="0" max="59" placeholder="SS" class="role-inherit-field block w-full rounded-lg border border-gray-300 bg-gray-100 px-2 py-1 text-xs" disabled />
                    </div>
                </div>
            </div>
            <!-- リスポーン地点 -->
            <div class="role-inherit-section" data-field="respawn-loc">
                <div class="flex items-center justify-between mb-1">
                    <label class="text-xs font-semibold text-gray-600">リスポーン地点</label>
                    <label class="inline-flex cursor-pointer items-center gap-1">
                        <span class="text-xs text-gray-400">所属チームの設定を継承</span>
                        <input type="checkbox" name="role-inherit-respawn-loc" class="peer sr-only role-inherit-toggle" checked />
                        <div class="relative h-4 w-7 rounded-full bg-gray-300 transition-colors duration-200 peer-checked:bg-indigo-600 after:absolute after:left-0.5 after:top-0.5 after:h-3 after:w-3 after:rounded-full after:bg-white after:transition-transform after:duration-200 peer-checked:after:translate-x-3"></div>
                    </label>
                </div>
                <div class="role-inherit-field-group space-y-1 opacity-50">
                    <input type="text" name="role-respawn-world" placeholder="world" value="world" class="role-inherit-field block w-full rounded-lg border border-gray-300 bg-gray-100 px-2 py-1 text-xs text-gray-800 placeholder:text-gray-400" disabled />
                    <div class="flex items-center justify-between">
                        <span class="text-xs text-gray-400">座標</span>
                        <button type="button" class="paste-coord-btn text-xs text-indigo-500 hover:text-indigo-700" data-prefix="role-respawn">📋 ペースト</button>
                    </div>
                    <div class="grid grid-cols-3 gap-1">
                        <input type="number" name="role-respawn-x" placeholder="X" step="0.01" class="role-inherit-field block w-full rounded-lg border border-gray-300 bg-gray-100 px-2 py-1 text-xs" disabled />
                        <input type="number" name="role-respawn-y" placeholder="Y" step="0.01" class="role-inherit-field block w-full rounded-lg border border-gray-300 bg-gray-100 px-2 py-1 text-xs" disabled />
                        <input type="number" name="role-respawn-z" placeholder="Z" step="0.01" class="role-inherit-field block w-full rounded-lg border border-gray-300 bg-gray-100 px-2 py-1 text-xs" disabled />
                    </div>
                    <div class="grid grid-cols-2 gap-1">
                        <input type="number" name="role-respawn-yaw" placeholder="Yaw" step="0.1" class="role-inherit-field block w-full rounded-lg border border-gray-300 bg-gray-100 px-2 py-1 text-xs" disabled />
                        <input type="number" name="role-respawn-pitch" placeholder="Pitch" step="0.1" class="role-inherit-field block w-full rounded-lg border border-gray-300 bg-gray-100 px-2 py-1 text-xs" disabled />
                    </div>
                </div>
            </div>
            <!-- エフェクト -->
            <div class="lg:col-span-2 xl:col-span-3">
                <div class="flex items-center gap-2 mb-1">
                    <label class="text-xs font-semibold text-gray-600">エフェクト</label>
                    <label class="inline-flex cursor-pointer items-center gap-1">
                        <span class="text-xs text-gray-400">所属チームの設定を継承</span>
                        <input type="checkbox" name="role-inherit-effects" class="peer sr-only" checked />
                        <div class="relative h-4 w-7 rounded-full bg-gray-300 transition-colors duration-200 peer-checked:bg-indigo-600 after:absolute after:left-0.5 after:top-0.5 after:h-3 after:w-3 after:rounded-full after:bg-white after:transition-transform after:duration-200 peer-checked:after:translate-x-3"></div>
                    </label>
                </div>
                <div class="role-effects-container flex flex-wrap items-center gap-2">
                    <!-- ロールエフェクトがここに追加される -->
                    <button type="button" class="add-role-effect-btn flex items-center justify-center w-36 h-30 rounded-lg border-2 border-dashed border-gray-300 text-gray-400 transition hover:border-indigo-400 hover:text-indigo-500">
                        <svg class="h-5 w-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                            <path d="M12 4v16m8-8H4" />
                        </svg>
                    </button>
                </div>
            </div>
        </div>
    `;

    // 削除ボタンのイベント
    const deleteBtn = card.querySelector('.delete-role-btn');
    deleteBtn.addEventListener('click', () => {
        card.remove();
    });

    // 継承トグルのイベント
    setupRoleInheritToggles(card);

    // ロールエフェクト管理
    setupRoleEffectManagement(card);

    // 座標ペーストボタン設定
    card.querySelectorAll('.paste-coord-btn').forEach(btn => {
        setupPasteButton(btn, btn.dataset.prefix);
    });

    return card;
}

function setupRoleInheritToggles(card) {
    const toggles = card.querySelectorAll('.role-inherit-toggle');
    toggles.forEach(toggle => {
        const section = toggle.closest('.role-inherit-section');
        if (!section) return;

        function updateState() {
            const isInherited = toggle.checked;
            const fields = section.querySelectorAll('.role-inherit-field');
            const fieldGroup = section.querySelector('.role-inherit-field-group');

            fields.forEach(field => {
                field.disabled = isInherited;
                if (isInherited) {
                    field.classList.add('opacity-50', 'cursor-not-allowed', 'bg-gray-100');
                } else {
                    field.classList.remove('opacity-50', 'cursor-not-allowed', 'bg-gray-100');
                }
            });

            if (fieldGroup) {
                if (isInherited) {
                    fieldGroup.classList.add('opacity-50');
                } else {
                    fieldGroup.classList.remove('opacity-50');
                }
            }
        }

        toggle.addEventListener('change', updateState);
        updateState();
    });
}

function createRoleEffectRow() {
    const row = document.createElement('div');
    row.className = 'role-effect-card rounded-lg bg-gray-100 p-2 border border-gray-200 w-36';

    const optionsHtml = minecraftEffects.map(e =>
        `<option value="${e.id}">${e.name}</option>`
    ).join('');

    row.innerHTML = `
        <div class="flex items-center justify-between mb-1">
            <span class="text-xs font-semibold text-gray-600">エフェクト</span>
            <button type="button" class="delete-role-effect-btn text-gray-400 hover:text-red-500 transition">
                <svg class="h-3 w-3" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path d="M6 18L18 6M6 6l12 12" />
                </svg>
            </button>
        </div>
        <div class="space-y-1">
            <select name="role-effect-name" class="block w-full rounded-lg border border-gray-300 bg-white px-2 py-1 text-xs text-gray-800 focus:border-indigo-500 focus:outline-none">
                <option value="">選択</option>
                ${optionsHtml}
            </select>
            <input type="number" name="role-effect-duration" placeholder="持続時間(秒)" min="1" class="block w-full rounded-lg border border-gray-300 bg-white px-2 py-1 text-xs text-gray-800 placeholder:text-gray-400" />
            <input type="number" name="role-effect-level" placeholder="レベル" min="1" max="255" class="block w-full rounded-lg border border-gray-300 bg-white px-2 py-1 text-xs text-gray-800 placeholder:text-gray-400" />
        </div>
    `;

    const deleteBtn = row.querySelector('.delete-role-effect-btn');
    deleteBtn.addEventListener('click', () => {
        row.remove();
    });

    return row;
}

function setupRoleEffectManagement(card) {
    const addBtn = card.querySelector('.add-role-effect-btn');
    const container = card.querySelector('.role-effects-container');

    if (!addBtn || !container) return;

    addBtn.addEventListener('click', () => {
        const newRow = createRoleEffectRow();
        container.insertBefore(newRow, addBtn);
    });
}

function setupRoleManagement(teamCard) {
    const addBtn = teamCard.querySelector('.add-role-btn');
    const container = teamCard.querySelector('.roles-container');

    if (!addBtn || !container) return;

    addBtn.addEventListener('click', () => {
        const newCard = createRoleCard();
        container.insertBefore(newCard, addBtn);
    });
}

// 終了条件管理
const endConditionTypes = [
    { id: 'Beacon', name: 'Beacon（ビーコン破壊）' },
    { id: 'Extermination', name: 'Extermination（全滅）' },
    { id: 'Ticket', name: 'Ticket（チケット）' },
    { id: 'Composite', name: 'Composite（複合条件 - AND）' }
];

function createEndConditionCard(conditionNumber) {
    const card = document.createElement('div');
    card.className = 'end-condition-card w-full rounded-2xl border border-gray-200 bg-white px-6 py-6 shadow-sm';

    const typeOptionsHtml = endConditionTypes.map(t =>
        `<option value="${t.id}">${t.name}</option>`
    ).join('');

    card.innerHTML = `
        <div class="mb-4 flex items-center justify-between">
            <h3 class="text-lg font-bold tracking-tight text-gray-900">終了条件 ${conditionNumber}</h3>
            <button type="button" class="delete-end-condition-btn text-gray-400 hover:text-red-500 transition">
                <svg class="h-5 w-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path d="M6 18L18 6M6 6l12 12" />
                </svg>
            </button>
        </div>

        <!-- 共通フィールド（上部） -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
            <div>
                <label class="mb-2 block text-sm font-semibold text-gray-800">条件タイプ</label>
                <select name="end-condition-type" class="end-condition-type-select block w-full rounded-xl border border-gray-300 bg-white px-4 py-2.5 text-gray-800 shadow-sm transition focus:border-indigo-500 focus:outline-none focus:ring-4 focus:ring-indigo-500/20">
                    ${typeOptionsHtml}
                </select>
            </div>
            <div class="md:col-span-2">
                <label class="mb-2 block text-sm font-semibold text-gray-800">メッセージ</label>
                <input type="text" name="end-condition-message" placeholder="例: §cビーコンが破壊された！" class="block w-full rounded-xl border border-gray-300 bg-white px-4 py-2.5 text-gray-800 shadow-sm transition placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-4 focus:ring-indigo-500/20" />
            </div>
        </div>

        <!-- 条件タイプ別フィールド（下部） -->
        <div class="end-condition-fields">
            <!-- Beacon用フィールド -->
            <div class="beacon-fields">
                <div class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-7 gap-3 items-end">
                    <div>
                        <label class="mb-1 block text-xs font-semibold text-gray-600">ワールド</label>
                        <input type="text" name="beacon-world" placeholder="world" value="world" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                    </div>
                    <div class="flex items-center justify-between">
                        <label class="text-xs font-semibold text-gray-600">座標</label>
                        <button type="button" class="paste-coord-btn text-xs text-indigo-500 hover:text-indigo-700" data-prefix="beacon">📋 ペースト</button>
                    </div>
                    <div>
                        <label class="mb-1 block text-xs font-semibold text-gray-600">X</label>
                        <input type="number" name="beacon-x" placeholder="0" step="0.01" value="0" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                    </div>
                    <div>
                        <label class="mb-1 block text-xs font-semibold text-gray-600">Y</label>
                        <input type="number" name="beacon-y" placeholder="64" step="0.01" value="64" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                    </div>
                    <div>
                        <label class="mb-1 block text-xs font-semibold text-gray-600">Z</label>
                        <input type="number" name="beacon-z" placeholder="0" step="0.01" value="0" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                    </div>
                    <div>
                        <label class="mb-1 block text-xs font-semibold text-gray-600">Yaw</label>
                        <input type="number" name="beacon-yaw" placeholder="0" step="0.1" value="0" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                    </div>
                    <div>
                        <label class="mb-1 block text-xs font-semibold text-gray-600">Pitch</label>
                        <input type="number" name="beacon-pitch" placeholder="0" step="0.1" value="180" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                    </div>
                    <div>
                        <label class="mb-1 block text-xs font-semibold text-gray-600">HP</label>
                        <input type="number" name="beacon-hitpoint" placeholder="100" min="1" value="100" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                    </div>
                </div>
            </div>

            <!-- Extermination用フィールド -->
            <div class="extermination-fields hidden">
                <div class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-7 gap-3 items-end">
                    <div class="col-span-2">
                        <label class="mb-1 block text-xs font-semibold text-gray-600">対象チーム名</label>
                        <input type="text" name="extermination-team" placeholder="チーム名を入力" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                    </div>
                </div>
            </div>

            <!-- Ticket用フィールド -->
            <div class="ticket-fields hidden">
                <div class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-7 gap-3 items-end">
                    <div class="col-span-2">
                        <label class="mb-1 block text-xs font-semibold text-gray-600">対象チーム名</label>
                        <input type="text" name="ticket-team" placeholder="チーム名を入力" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                    </div>
                    <div>
                        <label class="mb-1 block text-xs font-semibold text-gray-600">チケット数</label>
                        <input type="number" name="ticket-count" placeholder="50" min="1" value="100" class="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                    </div>
                </div>
            </div>

            <!-- Composite用フィールド -->
            <div class="composite-fields hidden">
                <div class="flex items-center gap-2 mb-2">
                    <span class="text-xs font-semibold text-gray-600">条件リスト（AND）</span>
                    <span class="text-xs text-gray-400">- すべての条件を達成で終了判定</span>
                </div>
                <div class="composite-conditions-container flex flex-wrap items-start gap-2">
                    <!-- 複合条件がここに追加される -->
                    <button type="button" class="add-composite-condition-btn flex items-center justify-center w-48 h-46 rounded-lg border-2 border-dashed border-gray-300 text-gray-400 transition hover:border-indigo-400 hover:text-indigo-500">
                        <svg class="h-5 w-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                            <path d="M12 4v16m8-8H4" />
                        </svg>
                    </button>
                </div>
            </div>
        </div>
    `;

    // 削除ボタンのイベント
    const deleteBtn = card.querySelector('.delete-end-condition-btn');
    deleteBtn.addEventListener('click', () => {
        card.remove();
        updateEndConditionNumbers();
    });

    // 条件タイプ変更時のイベント
    setupEndConditionTypeChange(card);

    // Composite条件管理
    setupCompositeConditionManagement(card);

    // 座標ペーストボタン設定
    card.querySelectorAll('.paste-coord-btn').forEach(btn => {
        setupPasteButton(btn, btn.dataset.prefix);
    });

    return card;
}

function setupEndConditionTypeChange(card) {
    const typeSelect = card.querySelector('.end-condition-type-select');
    const beaconFields = card.querySelector('.beacon-fields');
    const exterminationFields = card.querySelector('.extermination-fields');
    const ticketFields = card.querySelector('.ticket-fields');
    const compositeFields = card.querySelector('.composite-fields');

    function updateFields() {
        const type = typeSelect.value;

        // すべて非表示
        beaconFields.classList.add('hidden');
        exterminationFields.classList.add('hidden');
        ticketFields.classList.add('hidden');
        compositeFields.classList.add('hidden');

        // 選択されたタイプのフィールドを表示
        switch (type) {
            case 'Beacon':
                beaconFields.classList.remove('hidden');
                break;
            case 'Extermination':
                exterminationFields.classList.remove('hidden');
                break;
            case 'Ticket':
                ticketFields.classList.remove('hidden');
                break;
            case 'Composite':
                compositeFields.classList.remove('hidden');
                break;
        }
    }

    typeSelect.addEventListener('change', updateFields);
    updateFields();
}

// Composite内の条件カード作成
function createCompositeConditionCard() {
    const card = document.createElement('div');
    card.className = 'composite-condition-card rounded-lg bg-gray-50 p-3 border border-gray-200 w-48';

    const subTypeOptions = [
        { id: 'Beacon', name: 'Beacon' },
        { id: 'Extermination', name: 'Extermination' },
        { id: 'Ticket', name: 'Ticket' }
    ];

    const typeOptionsHtml = subTypeOptions.map(t =>
        `<option value="${t.id}">${t.name}</option>`
    ).join('');

    card.innerHTML = `
        <div class="flex items-center justify-between mb-2">
            <select name="composite-sub-type" class="composite-sub-type-select rounded-lg border border-gray-300 bg-white px-2 py-1 text-xs text-gray-800 focus:border-indigo-500 focus:outline-none">
                ${typeOptionsHtml}
            </select>
            <button type="button" class="delete-composite-condition-btn text-gray-400 hover:text-red-500 transition">
                <svg class="h-3 w-3" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path d="M6 18L18 6M6 6l12 12" />
                </svg>
            </button>
        </div>
        <div class="composite-sub-fields">
            <!-- Beacon -->
            <div class="composite-beacon-fields space-y-1">
                <input type="text" name="composite-beacon-world" placeholder="world" value="world" class="block w-full rounded border border-gray-300 bg-white px-2 py-1 text-xs" />
                <div class="flex items-center justify-between">
                    <label class="text-xs text-gray-500">座標 (X / Y / Z)</label>
                    <button type="button" class="paste-coord-btn text-xs text-indigo-500 hover:text-indigo-700" data-prefix="composite-beacon">📋 ペースト</button>
                </div>
                <div class="grid grid-cols-3 gap-1">
                    <input type="number" name="composite-beacon-x" placeholder="X" step="0.01" value="0" class="block w-full rounded border border-gray-300 bg-white px-1 py-1 text-xs" />
                    <input type="number" name="composite-beacon-y" placeholder="Y" step="0.01" value="64" class="block w-full rounded border border-gray-300 bg-white px-1 py-1 text-xs" />
                    <input type="number" name="composite-beacon-z" placeholder="Z" step="0.01" value="0" class="block w-full rounded border border-gray-300 bg-white px-1 py-1 text-xs" />
                </div>
                <label class="mb-1 block text-xs text-gray-500">(Yaw / Pitch / HP)</label>
                <div class="grid grid-cols-3 gap-1">
                    <input type="number" name="composite-beacon-yaw" placeholder="Yaw" step="0.1" value="0" class="block w-full rounded border border-gray-300 bg-white px-1 py-1 text-xs" />
                    <input type="number" name="composite-beacon-pitch" placeholder="Pitch" step="0.1" value="180" class="block w-full rounded border border-gray-300 bg-white px-1 py-1 text-xs" />
                    <input type="number" name="composite-beacon-hp" placeholder="HP" min="1" value="100" class="block w-full rounded border border-gray-300 bg-white px-1 py-1 text-xs" />
                </div>
            </div>
            <!-- Extermination -->
            <div class="composite-extermination-fields hidden space-y-1">
                <input type="text" name="composite-extermination-team" placeholder="チーム名" class="block w-full rounded border border-gray-300 bg-white px-2 py-1 text-xs" />
            </div>
            <!-- Ticket -->
            <div class="composite-ticket-fields hidden space-y-1">
                <input type="text" name="composite-ticket-team" placeholder="チーム名" class="block w-full rounded border border-gray-300 bg-white px-2 py-1 text-xs" />
                <input type="number" name="composite-ticket-count" placeholder="チケット数" min="1" value="100" class="block w-full rounded border border-gray-300 bg-white px-2 py-1 text-xs" />
            </div>
        </div>
    `;

    // 削除ボタン
    const deleteBtn = card.querySelector('.delete-composite-condition-btn');
    deleteBtn.addEventListener('click', () => {
        card.remove();
    });

    // サブタイプ変更
    const subTypeSelect = card.querySelector('.composite-sub-type-select');
    const beaconFields = card.querySelector('.composite-beacon-fields');
    const exterminationFields = card.querySelector('.composite-extermination-fields');
    const ticketFields = card.querySelector('.composite-ticket-fields');

    function updateSubFields() {
        const type = subTypeSelect.value;
        beaconFields.classList.add('hidden');
        exterminationFields.classList.add('hidden');
        ticketFields.classList.add('hidden');

        switch (type) {
            case 'Beacon':
                beaconFields.classList.remove('hidden');
                break;
            case 'Extermination':
                exterminationFields.classList.remove('hidden');
                break;
            case 'Ticket':
                ticketFields.classList.remove('hidden');
                break;
        }
    }

    subTypeSelect.addEventListener('change', updateSubFields);
    updateSubFields();

    // 座標ペーストボタン設定
    card.querySelectorAll('.paste-coord-btn').forEach(btn => {
        setupPasteButton(btn, btn.dataset.prefix);
    });

    return card;
}

function setupCompositeConditionManagement(parentCard) {
    const addBtn = parentCard.querySelector('.add-composite-condition-btn');
    const container = parentCard.querySelector('.composite-conditions-container');

    if (!addBtn || !container) return;

    addBtn.addEventListener('click', () => {
        const newCard = createCompositeConditionCard();
        container.insertBefore(newCard, addBtn);
    });
}

function updateEndConditionNumbers() {
    const cards = document.querySelectorAll('.end-condition-card');
    cards.forEach((card, index) => {
        const title = card.querySelector('h3');
        if (title) {
            title.textContent = `終了条件 ${index + 1}`;
        }
    });
}

function setupEndConditionManagement() {
    const container = document.getElementById('end-conditions-container');
    const addBtn = document.getElementById('add-end-condition-btn');

    if (!container || !addBtn) return;

    addBtn.addEventListener('click', () => {
        const conditionNumber = document.querySelectorAll('.end-condition-card').length + 1;
        const newCard = createEndConditionCard(conditionNumber);
        addBtn.before(newCard);
    });
}

// チーム基本設定
const teamPresets = [
    { name: 'red', displayName: '赤チーム', color: 'RED' },
    { name: 'blue', displayName: '青チーム', color: 'BLUE' },
    { name: 'green', displayName: '緑チーム', color: 'GREEN' },
    { name: 'yellow', displayName: '黄チーム', color: 'YELLOW' }
];

// 待機時間デフォルト（3分）
const defaultLobbyTime = { hours: 0, minutes: 3, seconds: 0 };

// クイック設定テンプレート
const quickSetupTemplates = {
    // チームデスマッチ: 全チームリスポーン0、全滅条件
    'team-deathmatch': {
        respawnCount: 0,
        endConditionType: 'Extermination'
    },
    // ビーコン破壊戦: 全チームリスポーン-1、ビーコン条件HP100
    'beacon-destroy': {
        respawnCount: -1,
        endConditionType: 'Beacon',
        beaconHp: 100
    },
    // チケット制: 全チームリスポーン-1、チケット条件100
    'ticket': {
        respawnCount: -1,
        endConditionType: 'Ticket',
        ticketCount: 100
    },
    // 大将戦: 守りチームはリスポーン0+大将ロール、攻めチームはリスポーン-1
    'captain': {
        defenseRespawnCount: 0,
        attackRespawnCount: -1,
        endConditionType: 'Extermination'
    }
};

// クイック設定モーダル管理
function setupQuickSetupModal() {
    const modal = document.getElementById('quick-setup-modal');
    const openBtn = document.getElementById('quick-setup-btn');
    const closeBtn = document.getElementById('quick-setup-cancel-btn');
    const closeBtnX = document.getElementById('quick-setup-close-btn');
    const applyBtn = document.getElementById('quick-setup-apply-btn');
    const timeLimitDisableToggle = document.getElementById('quick-timelimit-disable');
    const timeLimitField = document.getElementById('quick-timelimit-minutes');
    const gameRuleSelect = document.getElementById('quick-game-rule');
    const captainOptions = document.getElementById('quick-captain-options');

    if (!modal || !openBtn) return;

    // モーダルを開く
    openBtn.addEventListener('click', () => {
        modal.classList.remove('hidden');
    });

    // モーダルを閉じる（キャンセルボタン）
    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            modal.classList.add('hidden');
        });
    }

    // モーダルを閉じる（Xボタン）
    if (closeBtnX) {
        closeBtnX.addEventListener('click', () => {
            modal.classList.add('hidden');
        });
    }

    // 背景クリックでモーダルを閉じる
    modal.addEventListener('click', (e) => {
        if (e.target === modal || e.target.classList.contains('modal-overlay')) {
            modal.classList.add('hidden');
        }
    });

    // ゲームルール変更時に大将戦オプションを表示/非表示
    if (gameRuleSelect && captainOptions) {
        gameRuleSelect.addEventListener('change', () => {
            if (gameRuleSelect.value === 'captain') {
                captainOptions.classList.remove('hidden');
                updateCaptainTeamOptions();
            } else {
                captainOptions.classList.add('hidden');
            }
        });
    }

    // チーム数変更時に守りチーム選択肢を更新
    const teamCountRadios = document.querySelectorAll('input[name="quick-team-count"]');
    teamCountRadios.forEach(radio => {
        radio.addEventListener('change', () => {
            if (gameRuleSelect && gameRuleSelect.value === 'captain') {
                updateCaptainTeamOptions();
            }
        });
    });

    // 制限時間トグル
    if (timeLimitDisableToggle && timeLimitField) {
        timeLimitDisableToggle.addEventListener('change', () => {
            const isDisabled = timeLimitDisableToggle.checked;
            timeLimitField.disabled = isDisabled;
            if (isDisabled) {
                timeLimitField.classList.add('opacity-50', 'cursor-not-allowed', 'bg-gray-100');
            } else {
                timeLimitField.classList.remove('opacity-50', 'cursor-not-allowed', 'bg-gray-100');
            }
        });
    }

    // 適用ボタン
    if (applyBtn) {
        applyBtn.addEventListener('click', () => {
            applyQuickSetup();
            modal.classList.add('hidden');
        });
    }
}

// 守りチーム選択肢をチーム数に応じて更新
function updateCaptainTeamOptions() {
    const teamCount = parseInt(document.querySelector('input[name="quick-team-count"]:checked').value);
    const container = document.getElementById('quick-captain-team-container');
    if (!container) return;

    const teamOrder = ['red', 'blue', 'green', 'yellow'];

    teamOrder.forEach((teamName, index) => {
        const label = container.querySelector(`label[data-team="${teamName}"]`);
        if (!label) return;

        const checkbox = label.querySelector('input[type="checkbox"]');

        if (index < teamCount) {
            // チーム数内なら表示
            label.classList.remove('hidden');
        } else {
            // チーム数外なら非表示にしてチェックを外す
            label.classList.add('hidden');
            if (checkbox) checkbox.checked = false;
        }
    });

    // デフォルトで赤のみ選択状態にする（初回）
    const redCheckbox = container.querySelector('input[value="red"]');
    if (redCheckbox && !container.querySelector('input[name="quick-captain-team"]:checked')) {
        redCheckbox.checked = true;
    }
}

// クイック設定を適用
function applyQuickSetup() {
    const gameRule = document.getElementById('quick-game-rule').value;
    const teamCount = parseInt(document.querySelector('input[name="quick-team-count"]:checked').value);
    const timeLimitDisabled = document.getElementById('quick-timelimit-disable').checked;
    const timeLimit = document.getElementById('quick-timelimit-minutes').value;

    // 大将戦の場合、守りチームを取得
    let captainTeam = null;
    if (gameRule === 'captain') {
        const selectedRadio = document.querySelector('input[name="quick-captain-team"]:checked');
        if (selectedRadio) {
            captainTeam = selectedRadio.value;
        }
    }

    // テンプレートを取得
    const template = quickSetupTemplates[gameRule];
    if (!template) {
        console.warn('テンプレートが見つかりません:', gameRule);
        return;
    }

    // 制限時間を設定
    const timeLimitToggle = document.getElementById('timelimit-disable');
    if (timeLimitToggle) {
        timeLimitToggle.checked = timeLimitDisabled;
        timeLimitToggle.dispatchEvent(new Event('change'));
    }

    if (!timeLimitDisabled && timeLimit) {
        const minutes = parseInt(timeLimit);
        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;

        const hoursInput = document.getElementById('timelimit-hours');
        const minutesInput = document.getElementById('timelimit-minutes');
        const secondsInput = document.getElementById('timelimit-seconds');

        if (hoursInput) hoursInput.value = hours;
        if (minutesInput) minutesInput.value = mins;
        if (secondsInput) secondsInput.value = 0;
    }

    // チームをクリアして再作成
    const teamsContainer = document.getElementById('teams-container');
    const addTeamBtn = document.getElementById('add-team-btn');

    if (teamsContainer && addTeamBtn) {
        // 既存のチームカードを削除
        teamsContainer.querySelectorAll('.team-card').forEach(card => card.remove());

        // 新しいチームを作成
        for (let i = 0; i < teamCount; i++) {
            const preset = teamPresets[i];
            const newCard = createTeamCard(i + 1);
            addTeamBtn.before(newCard);

            // チーム名・表示名・カラー設定
            const nameInput = newCard.querySelector('input[name="team-name"]');
            const displayNameInput = newCard.querySelector('input[name="team-display-name"]');
            const colorInput = newCard.querySelector('select[name="team-color"]');
            const respawnInput = newCard.querySelector('input[name="team-respawn-count"]');

            if (nameInput) nameInput.value = preset.name;
            if (displayNameInput) displayNameInput.value = preset.displayName;
            if (colorInput) {
                colorInput.value = preset.color;
                colorInput.dispatchEvent(new Event('input'));
            }

            // 待機時間を設定
            const lobbyHoursInput = newCard.querySelector('input[name="team-lobby-hours"]');
            const lobbyMinutesInput = newCard.querySelector('input[name="team-lobby-minutes"]');
            const lobbySecondsInput = newCard.querySelector('input[name="team-lobby-seconds"]');
            if (lobbyHoursInput) lobbyHoursInput.value = defaultLobbyTime.hours;
            if (lobbyMinutesInput) lobbyMinutesInput.value = defaultLobbyTime.minutes;
            if (lobbySecondsInput) lobbySecondsInput.value = defaultLobbyTime.seconds;

            // リスポーン回数とロール設定（ルールによる）
            if (gameRule === 'captain') {
                const isCaptainTeam = captainTeam === preset.name;
                if (isCaptainTeam) {
                    // 守りチーム: リスポーン0、大将ロール追加
                    if (respawnInput) respawnInput.value = template.defenseRespawnCount;
                    addCaptainRole(newCard, preset.displayName);
                } else {
                    // 攻めチーム: リスポーン-1、待機地点無効
                    if (respawnInput) respawnInput.value = template.attackRespawnCount;
                    // 待機地点を無効化
                    const lobbyToggle = newCard.querySelector('.team-lobby-toggle');
                    if (lobbyToggle) {
                        lobbyToggle.checked = true;
                        lobbyToggle.dispatchEvent(new Event('change'));
                    }
                }
            } else {
                // 通常ルール
                if (respawnInput) respawnInput.value = template.respawnCount;
            }
        }
        updateDeleteButtons();
    }

    // 終了条件をクリアして再作成
    const endConditionsContainer = document.getElementById('end-conditions-container');
    const addEndConditionBtn = document.getElementById('add-end-condition-btn');

    if (endConditionsContainer && addEndConditionBtn) {
        // 既存の終了条件カードを削除
        endConditionsContainer.querySelectorAll('.end-condition-card').forEach(card => card.remove());

        if (gameRule === 'captain' && captainTeam) {
            // 大将戦: 守りチームの大将（captain）全滅条件を作成
            const preset = teamPresets.find(p => p.name === captainTeam);
            if (preset) {
                const conditionCard = createEndConditionCard(1);
                addEndConditionBtn.before(conditionCard);

                const typeSelect = conditionCard.querySelector('.end-condition-type-select');
                if (typeSelect) {
                    typeSelect.value = 'Extermination';
                    typeSelect.dispatchEvent(new Event('change'));
                }

                // チーム名.captain を設定
                const extTeamInput = conditionCard.querySelector('input[name="extermination-team"]');
                if (extTeamInput) extTeamInput.value = `${preset.name}.captain`;
            }
        } else {
            // 通常ルール: チーム数分の終了条件を作成
            for (let i = 0; i < teamCount; i++) {
                const preset = teamPresets[i];
                const conditionCard = createEndConditionCard(i + 1);
                addEndConditionBtn.before(conditionCard);

                const typeSelect = conditionCard.querySelector('.end-condition-type-select');
                if (typeSelect) {
                    typeSelect.value = template.endConditionType;
                    typeSelect.dispatchEvent(new Event('change'));
                }

                // 条件タイプごとのフィールド設定
                switch (template.endConditionType) {
                    case 'Extermination':
                        const extTeamInput = conditionCard.querySelector('input[name="extermination-team"]');
                        if (extTeamInput) extTeamInput.value = preset.name;
                        break;
                    case 'Beacon':
                        const beaconHpInput = conditionCard.querySelector('input[name="beacon-hitpoint"]');
                        if (beaconHpInput) beaconHpInput.value = template.beaconHp;
                        break;
                    case 'Ticket':
                        const ticketTeamInput = conditionCard.querySelector('input[name="ticket-team"]');
                        const ticketCountInput = conditionCard.querySelector('input[name="ticket-count"]');
                        if (ticketTeamInput) ticketTeamInput.value = preset.name;
                        if (ticketCountInput) ticketCountInput.value = template.ticketCount;
                        break;
                }
            }
        }
    }

    console.log('クイック設定を適用しました:', { gameRule, teamCount, timeLimitDisabled, timeLimit, captainTeam });
}

// 大将ロールを追加
function addCaptainRole(teamCard, teamDisplayName) {
    const addRoleBtn = teamCard.querySelector('.add-role-btn');
    const rolesContainer = teamCard.querySelector('.roles-container');

    if (!addRoleBtn || !rolesContainer) return;

    // ロールカードを作成
    const roleCard = createRoleCard();
    rolesContainer.insertBefore(roleCard, addRoleBtn);

    // ロール名・表示名を設定
    const roleNameInput = roleCard.querySelector('input[name="role-name"]');
    const roleDisplayNameInput = roleCard.querySelector('input[name="role-display-name"]');

    if (roleNameInput) roleNameInput.value = 'captain';
    if (roleDisplayNameInput) roleDisplayNameInput.value = `${teamDisplayName}の大将`;

    // リスポーン回数継承をオフにして0を設定
    const respawnInheritToggle = roleCard.querySelector('input[name="role-inherit-respawn"]');
    const respawnInput = roleCard.querySelector('input[name="role-respawn-count"]');

    if (respawnInheritToggle) {
        respawnInheritToggle.checked = false;
        respawnInheritToggle.dispatchEvent(new Event('change'));
    }
    if (respawnInput) {
        respawnInput.value = 0;
    }
}

// コマンド生成ボタンのセットアップ
function setupGenerateCommandButton() {
    const generateBtn = document.getElementById('generate-command-btn');
    const warningModal = document.getElementById('validation-warning-modal');
    const closeBtn = document.getElementById('validation-warning-close-btn');

    if (!generateBtn) return;

    // ボタンクリック時にバリデーションを実行
    generateBtn.addEventListener('click', () => {
        const errors = validateAll();

        if (errors.length > 0) {
            // エラーがある場合は警告モーダルを表示
            showValidationWarningModal(errors);
        } else {
            // エラーがない場合はコマンドを生成してクリップボードにコピー
            generateAndCopyCommand();
        }
    });

    // 警告モーダルの閉じるボタン
    if (closeBtn && warningModal) {
        closeBtn.addEventListener('click', () => {
            warningModal.classList.add('hidden');
        });

        // 背景クリックで閉じる
        warningModal.addEventListener('click', (e) => {
            if (e.target === warningModal || e.target.classList.contains('modal-overlay')) {
                warningModal.classList.add('hidden');
            }
        });
    }
}

// コマンドオプション管理
function createCommandInput(containerId, value = '') {
    const container = document.getElementById(containerId);
    const addBtn = container.querySelector('button');

    const wrapper = document.createElement('div');
    wrapper.className = 'command-item flex items-center gap-2';
    wrapper.innerHTML = `
        <input type="text" name="${containerId === 'startup-commands-container' ? 'startup-command' : 'shutdown-command'}"
            value="${value}"
            placeholder="例: /say ゲーム開始！"
            class="flex-1 block rounded-xl border border-gray-300 bg-white px-4 py-2.5 text-gray-800 shadow-sm transition placeholder:text-gray-400 focus:border-indigo-500 focus:outline-none focus:ring-4 focus:ring-indigo-500/20 font-mono text-sm" />
        <button type="button" class="delete-command-btn text-gray-400 hover:text-red-500 transition p-1">
            <svg class="h-5 w-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path d="M6 18L18 6M6 6l12 12" />
            </svg>
        </button>
    `;

    container.insertBefore(wrapper, addBtn);

    wrapper.querySelector('.delete-command-btn').addEventListener('click', () => {
        wrapper.remove();
    });

    return wrapper;
}

function setupCommandManagement() {
    const startupAddBtn = document.getElementById('add-startup-command-btn');
    const shutdownAddBtn = document.getElementById('add-shutdown-command-btn');

    if (startupAddBtn) {
        startupAddBtn.addEventListener('click', () => {
            const wrapper = createCommandInput('startup-commands-container');
            wrapper.querySelector('input').focus();
        });
    }

    if (shutdownAddBtn) {
        shutdownAddBtn.addEventListener('click', () => {
            const wrapper = createCommandInput('shutdown-commands-container');
            wrapper.querySelector('input').focus();
        });
    }
}

// ========== localStorage 永続化 ==========

const STORAGE_KEY = 'kcdk-config-editor-state';

function collectFullState() {
    const state = {
        gamemode: document.getElementById('gamemode')?.value || 'ADVENTURE',
        bossbarDisable: document.getElementById('bossbar-disable')?.checked || false,
        bossbarMcid: document.getElementById('bossbar-mcid')?.value || '',
        timelimitDisable: document.getElementById('timelimit-disable')?.checked || false,
        timelimitHours: document.getElementById('timelimit-hours')?.value || '0',
        timelimitMinutes: document.getElementById('timelimit-minutes')?.value || '0',
        timelimitSeconds: document.getElementById('timelimit-seconds')?.value || '0',
        startupCommands: [],
        shutdownCommands: [],
        teams: [],
        endConditions: []
    };

    // コマンド収集
    document.querySelectorAll('#startup-commands-container input[name="startup-command"]').forEach(input => {
        state.startupCommands.push(input.value);
    });
    document.querySelectorAll('#shutdown-commands-container input[name="shutdown-command"]').forEach(input => {
        state.shutdownCommands.push(input.value);
    });

    // チーム収集（UI状態含む）
    document.querySelectorAll('.team-card').forEach(card => {
        const team = {
            name: card.querySelector('input[name="team-name"]')?.value || '',
            displayName: card.querySelector('input[name="team-display-name"]')?.value || '',
            armorColor: card.querySelector('select[name="team-color"]')?.value || 'WHITE',
            hasArmor: card.querySelector('input[name="team-has-armor"]')?.checked ?? true,
            respawnCount: card.querySelector('input[name="team-respawn-count"]')?.value || '0',
            lobbyDisable: card.querySelector('.team-lobby-toggle')?.checked || false,
            lobbyWorld: card.querySelector('input[name="team-lobby-world"]')?.value || 'world',
            lobbyX: card.querySelector('input[name="team-lobby-x"]')?.value || '0',
            lobbyY: card.querySelector('input[name="team-lobby-y"]')?.value || '64',
            lobbyZ: card.querySelector('input[name="team-lobby-z"]')?.value || '0',
            lobbyYaw: card.querySelector('input[name="team-lobby-yaw"]')?.value || '0',
            lobbyPitch: card.querySelector('input[name="team-lobby-pitch"]')?.value || '180',
            lobbyHours: card.querySelector('input[name="team-lobby-hours"]')?.value || '0',
            lobbyMinutes: card.querySelector('input[name="team-lobby-minutes"]')?.value || '3',
            lobbySeconds: card.querySelector('input[name="team-lobby-seconds"]')?.value || '0',
            respawnWorld: card.querySelector('input[name="team-respawn-world"]')?.value || 'world',
            respawnX: card.querySelector('input[name="team-respawn-x"]')?.value || '0',
            respawnY: card.querySelector('input[name="team-respawn-y"]')?.value || '64',
            respawnZ: card.querySelector('input[name="team-respawn-z"]')?.value || '0',
            respawnYaw: card.querySelector('input[name="team-respawn-yaw"]')?.value || '0',
            respawnPitch: card.querySelector('input[name="team-respawn-pitch"]')?.value || '180',
            effects: [],
            roles: []
        };

        // エフェクト
        card.querySelectorAll('.effect-card').forEach(ec => {
            team.effects.push({
                name: ec.querySelector('select[name="effect-name"]')?.value || '',
                duration: ec.querySelector('input[name="effect-duration"]')?.value || '',
                level: ec.querySelector('input[name="effect-level"]')?.value || ''
            });
        });

        // ロール
        card.querySelectorAll('.role-card').forEach(rc => {
            const role = {
                name: rc.querySelector('input[name="role-name"]')?.value || '',
                displayName: rc.querySelector('input[name="role-display-name"]')?.value || '',
                inheritColor: rc.querySelector('input[name="role-inherit-color"]')?.checked ?? true,
                color: rc.querySelector('select[name="role-color"]')?.value || 'RED',
                inheritHasArmor: rc.querySelector('input[name="role-inherit-has-armor"]')?.checked ?? true,
                hasArmor: rc.querySelector('input[name="role-has-armor"]')?.checked ?? true,
                inheritRespawn: rc.querySelector('input[name="role-inherit-respawn"]')?.checked ?? true,
                respawnCount: rc.querySelector('input[name="role-respawn-count"]')?.value || '0',
                inheritLobby: rc.querySelector('input[name="role-inherit-lobby"]')?.checked ?? true,
                lobbyWorld: rc.querySelector('input[name="role-lobby-world"]')?.value || 'world',
                lobbyX: rc.querySelector('input[name="role-lobby-x"]')?.value || '',
                lobbyY: rc.querySelector('input[name="role-lobby-y"]')?.value || '',
                lobbyZ: rc.querySelector('input[name="role-lobby-z"]')?.value || '',
                lobbyYaw: rc.querySelector('input[name="role-lobby-yaw"]')?.value || '',
                lobbyPitch: rc.querySelector('input[name="role-lobby-pitch"]')?.value || '',
                lobbyHours: rc.querySelector('input[name="role-lobby-hours"]')?.value || '',
                lobbyMinutes: rc.querySelector('input[name="role-lobby-minutes"]')?.value || '',
                lobbySeconds: rc.querySelector('input[name="role-lobby-seconds"]')?.value || '',
                inheritRespawnLoc: rc.querySelector('input[name="role-inherit-respawn-loc"]')?.checked ?? true,
                respawnWorld: rc.querySelector('input[name="role-respawn-world"]')?.value || 'world',
                respawnX: rc.querySelector('input[name="role-respawn-x"]')?.value || '',
                respawnY: rc.querySelector('input[name="role-respawn-y"]')?.value || '',
                respawnZ: rc.querySelector('input[name="role-respawn-z"]')?.value || '',
                respawnYaw: rc.querySelector('input[name="role-respawn-yaw"]')?.value || '',
                respawnPitch: rc.querySelector('input[name="role-respawn-pitch"]')?.value || '',
                inheritEffects: rc.querySelector('input[name="role-inherit-effects"]')?.checked ?? true,
                extendsEffects: rc.querySelector('input[name="role-extends-effects"]')?.checked || false,
                extendsItem: rc.querySelector('input[name="role-extends-item"]')?.checked || false,
                effects: []
            };
            rc.querySelectorAll('.role-effect-card').forEach(rec => {
                role.effects.push({
                    name: rec.querySelector('select[name="role-effect-name"]')?.value || '',
                    duration: rec.querySelector('input[name="role-effect-duration"]')?.value || '',
                    level: rec.querySelector('input[name="role-effect-level"]')?.value || ''
                });
            });
            team.roles.push(role);
        });

        state.teams.push(team);
    });

    // 終了条件収集
    document.querySelectorAll('.end-condition-card').forEach(card => {
        const type = card.querySelector('.end-condition-type-select')?.value || 'Beacon';
        const cond = {
            type,
            message: card.querySelector('input[name="end-condition-message"]')?.value || '',
            // Beacon
            beaconWorld: card.querySelector('input[name="beacon-world"]')?.value || 'world',
            beaconX: card.querySelector('input[name="beacon-x"]')?.value || '0',
            beaconY: card.querySelector('input[name="beacon-y"]')?.value || '64',
            beaconZ: card.querySelector('input[name="beacon-z"]')?.value || '0',
            beaconHitpoint: card.querySelector('input[name="beacon-hitpoint"]')?.value || '100',
            // Extermination
            exterminationTeam: card.querySelector('input[name="extermination-team"]')?.value || '',
            // Ticket
            ticketTeam: card.querySelector('input[name="ticket-team"]')?.value || '',
            ticketCount: card.querySelector('input[name="ticket-count"]')?.value || '100',
            // Composite
            compositeConditions: []
        };

        card.querySelectorAll('.composite-condition-card').forEach(sc => {
            cond.compositeConditions.push({
                type: sc.querySelector('.composite-sub-type-select')?.value || 'Beacon',
                beaconWorld: sc.querySelector('input[name="composite-beacon-world"]')?.value || 'world',
                beaconX: sc.querySelector('input[name="composite-beacon-x"]')?.value || '0',
                beaconY: sc.querySelector('input[name="composite-beacon-y"]')?.value || '64',
                beaconZ: sc.querySelector('input[name="composite-beacon-z"]')?.value || '0',
                beaconYaw: sc.querySelector('input[name="composite-beacon-yaw"]')?.value || '0',
                beaconPitch: sc.querySelector('input[name="composite-beacon-pitch"]')?.value || '180',
                beaconHp: sc.querySelector('input[name="composite-beacon-hp"]')?.value || '100',
                exterminationTeam: sc.querySelector('input[name="composite-extermination-team"]')?.value || '',
                ticketTeam: sc.querySelector('input[name="composite-ticket-team"]')?.value || '',
                ticketCount: sc.querySelector('input[name="composite-ticket-count"]')?.value || '100'
            });
        });

        state.endConditions.push(cond);
    });

    return state;
}

let saveTimeout = null;
function saveStateToLocalStorage() {
    if (saveTimeout) clearTimeout(saveTimeout);
    saveTimeout = setTimeout(() => {
        try {
            const state = collectFullState();
            localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
        } catch (e) {
            // ignore
        }
    }, 300);
}

function restoreStateFromLocalStorage() {
    let state;
    try {
        const raw = localStorage.getItem(STORAGE_KEY);
        if (!raw) return false;
        state = JSON.parse(raw);
    } catch (e) {
        return false;
    }

    // 基本フィールド
    const gamemode = document.getElementById('gamemode');
    if (gamemode) gamemode.value = state.gamemode || 'ADVENTURE';

    const bossbarDisable = document.getElementById('bossbar-disable');
    if (bossbarDisable) bossbarDisable.checked = state.bossbarDisable || false;

    const bossbarMcid = document.getElementById('bossbar-mcid');
    if (bossbarMcid) bossbarMcid.value = state.bossbarMcid || '';

    const timelimitDisable = document.getElementById('timelimit-disable');
    if (timelimitDisable) timelimitDisable.checked = state.timelimitDisable || false;

    const setVal = (id, val) => { const el = document.getElementById(id); if (el) el.value = val; };
    setVal('timelimit-hours', state.timelimitHours || '0');
    setVal('timelimit-minutes', state.timelimitMinutes || '0');
    setVal('timelimit-seconds', state.timelimitSeconds || '0');

    // コマンド復元
    (state.startupCommands || []).forEach(cmd => {
        createCommandInput('startup-commands-container', cmd);
    });
    (state.shutdownCommands || []).forEach(cmd => {
        createCommandInput('shutdown-commands-container', cmd);
    });

    // 既存のチームカードを削除
    document.querySelectorAll('.team-card').forEach(c => c.remove());

    // チーム復元
    const teamsContainer = document.getElementById('teams-container');
    const addTeamBtn = document.getElementById('add-team-btn');
    (state.teams || []).forEach((team, index) => {
        const card = createTeamCard(index + 1);
        teamsContainer.insertBefore(card, addTeamBtn);

        // 基本情報
        const setInput = (name, val) => { const el = card.querySelector(`input[name="${name}"]`); if (el) el.value = val; };
        setInput('team-name', team.name || '');
        setInput('team-display-name', team.displayName || '');
        const teamColorSelect = card.querySelector('select[name="team-color"]');
        if (teamColorSelect) teamColorSelect.value = team.armorColor || 'WHITE';
        const hasArmorToggle = card.querySelector('input[name="team-has-armor"]');
        if (hasArmorToggle) hasArmorToggle.checked = team.hasArmor ?? true;
        setInput('team-respawn-count', team.respawnCount || '0');

        // 待機地点トグル
        const lobbyToggle = card.querySelector('.team-lobby-toggle');
        if (lobbyToggle) {
            lobbyToggle.checked = team.lobbyDisable || false;
            lobbyToggle.dispatchEvent(new Event('change'));
        }

        setInput('team-lobby-world', team.lobbyWorld || 'world');
        setInput('team-lobby-x', team.lobbyX || '0');
        setInput('team-lobby-y', team.lobbyY || '64');
        setInput('team-lobby-z', team.lobbyZ || '0');
        setInput('team-lobby-yaw', team.lobbyYaw || '0');
        setInput('team-lobby-pitch', team.lobbyPitch || '180');
        setInput('team-lobby-hours', team.lobbyHours || '0');
        setInput('team-lobby-minutes', team.lobbyMinutes || '3');
        setInput('team-lobby-seconds', team.lobbySeconds || '0');

        setInput('team-respawn-world', team.respawnWorld || 'world');
        setInput('team-respawn-x', team.respawnX || '0');
        setInput('team-respawn-y', team.respawnY || '64');
        setInput('team-respawn-z', team.respawnZ || '0');
        setInput('team-respawn-yaw', team.respawnYaw || '0');
        setInput('team-respawn-pitch', team.respawnPitch || '180');

        // 背景色更新
        updateTeamCardBackground(card);

        // エフェクト復元
        const effectsContainer = card.querySelector('.effects-container');
        const addEffectBtn = card.querySelector('.add-effect-btn');
        (team.effects || []).forEach(eff => {
            const row = createEffectRow();
            effectsContainer.insertBefore(row, addEffectBtn);
            const sel = row.querySelector('select[name="effect-name"]');
            if (sel) sel.value = eff.name || '';
            const dur = row.querySelector('input[name="effect-duration"]');
            if (dur) dur.value = eff.duration || '';
            const lvl = row.querySelector('input[name="effect-level"]');
            if (lvl) lvl.value = eff.level || '';
        });

        // ロール復元
        const rolesContainer = card.querySelector('.roles-container');
        const addRoleBtn = card.querySelector('.add-role-btn');
        (team.roles || []).forEach(role => {
            const rc = createRoleCard();
            rolesContainer.insertBefore(rc, addRoleBtn);

            const setRoleInput = (name, val) => { const el = rc.querySelector(`input[name="${name}"]`); if (el) el.value = val; };
            const setRoleCheck = (name, val) => { const el = rc.querySelector(`input[name="${name}"]`); if (el) el.checked = val; };

            setRoleInput('role-name', role.name || '');
            setRoleInput('role-display-name', role.displayName || '');
            setRoleCheck('role-inherit-color', role.inheritColor ?? true);
            const roleColorSelect = rc.querySelector('select[name="role-color"]');
            if (roleColorSelect) roleColorSelect.value = role.color || 'RED';
            setRoleCheck('role-inherit-has-armor', role.inheritHasArmor ?? true);
            setRoleCheck('role-has-armor', role.hasArmor ?? true);
            setRoleCheck('role-inherit-respawn', role.inheritRespawn ?? true);
            setRoleInput('role-respawn-count', role.respawnCount || '0');
            setRoleCheck('role-inherit-lobby', role.inheritLobby ?? true);
            setRoleInput('role-lobby-world', role.lobbyWorld || 'world');
            setRoleInput('role-lobby-x', role.lobbyX || '');
            setRoleInput('role-lobby-y', role.lobbyY || '');
            setRoleInput('role-lobby-z', role.lobbyZ || '');
            setRoleInput('role-lobby-yaw', role.lobbyYaw || '');
            setRoleInput('role-lobby-pitch', role.lobbyPitch || '');
            setRoleInput('role-lobby-hours', role.lobbyHours || '');
            setRoleInput('role-lobby-minutes', role.lobbyMinutes || '');
            setRoleInput('role-lobby-seconds', role.lobbySeconds || '');
            setRoleCheck('role-inherit-respawn-loc', role.inheritRespawnLoc ?? true);
            setRoleInput('role-respawn-world', role.respawnWorld || 'world');
            setRoleInput('role-respawn-x', role.respawnX || '');
            setRoleInput('role-respawn-y', role.respawnY || '');
            setRoleInput('role-respawn-z', role.respawnZ || '');
            setRoleInput('role-respawn-yaw', role.respawnYaw || '');
            setRoleInput('role-respawn-pitch', role.respawnPitch || '');
            setRoleCheck('role-inherit-effects', role.inheritEffects ?? true);
            setRoleCheck('role-extends-effects', role.extendsEffects || false);
            setRoleCheck('role-extends-item', role.extendsItem || false);

            // 継承トグルの状態を反映
            rc.querySelectorAll('.role-inherit-toggle').forEach(t => t.dispatchEvent(new Event('change')));

            // ロールエフェクト復元
            const roleEffectsContainer = rc.querySelector('.role-effects-container');
            const addRoleEffectBtn = rc.querySelector('.add-role-effect-btn');
            (role.effects || []).forEach(eff => {
                const row = createRoleEffectRow();
                roleEffectsContainer.insertBefore(row, addRoleEffectBtn);
                const sel = row.querySelector('select[name="role-effect-name"]');
                if (sel) sel.value = eff.name || '';
                const dur = row.querySelector('input[name="role-effect-duration"]');
                if (dur) dur.value = eff.duration || '';
                const lvl = row.querySelector('input[name="role-effect-level"]');
                if (lvl) lvl.value = eff.level || '';
            });
        });
    });
    updateDeleteButtons();

    // 終了条件復元
    document.querySelectorAll('.end-condition-card').forEach(c => c.remove());
    const endContainer = document.getElementById('end-conditions-container');
    const addEndBtn = document.getElementById('add-end-condition-btn');
    (state.endConditions || []).forEach((cond, index) => {
        const card = createEndConditionCard(index + 1);
        endContainer.insertBefore(card, addEndBtn);

        const typeSelect = card.querySelector('.end-condition-type-select');
        if (typeSelect) {
            typeSelect.value = cond.type || 'Beacon';
            typeSelect.dispatchEvent(new Event('change'));
        }

        const setInput = (name, val) => { const el = card.querySelector(`input[name="${name}"]`); if (el) el.value = val; };
        setInput('end-condition-message', cond.message || '');
        setInput('beacon-world', cond.beaconWorld || 'world');
        setInput('beacon-x', cond.beaconX || '0');
        setInput('beacon-y', cond.beaconY || '64');
        setInput('beacon-z', cond.beaconZ || '0');
        setInput('beacon-hitpoint', cond.beaconHitpoint || '100');
        setInput('extermination-team', cond.exterminationTeam || '');
        setInput('ticket-team', cond.ticketTeam || '');
        setInput('ticket-count', cond.ticketCount || '100');

        // Composite条件復元
        const compositeContainer = card.querySelector('.composite-conditions-container');
        const addCompositeBtn = card.querySelector('.add-composite-condition-btn');
        (cond.compositeConditions || []).forEach(sc => {
            const subCard = createCompositeConditionCard();
            compositeContainer.insertBefore(subCard, addCompositeBtn);

            const subTypeSelect = subCard.querySelector('.composite-sub-type-select');
            if (subTypeSelect) {
                subTypeSelect.value = sc.type || 'Beacon';
                subTypeSelect.dispatchEvent(new Event('change'));
            }

            const setSubInput = (name, val) => { const el = subCard.querySelector(`input[name="${name}"]`); if (el) el.value = val; };
            setSubInput('composite-beacon-world', sc.beaconWorld || 'world');
            setSubInput('composite-beacon-x', sc.beaconX || '0');
            setSubInput('composite-beacon-y', sc.beaconY || '64');
            setSubInput('composite-beacon-z', sc.beaconZ || '0');
            setSubInput('composite-beacon-yaw', sc.beaconYaw || '0');
            setSubInput('composite-beacon-pitch', sc.beaconPitch || '180');
            setSubInput('composite-beacon-hp', sc.beaconHp || '100');
            setSubInput('composite-extermination-team', sc.exterminationTeam || '');
            setSubInput('composite-ticket-team', sc.ticketTeam || '');
            setSubInput('composite-ticket-count', sc.ticketCount || '100');
        });
    });

    return true;
}

function setupAutoSave() {
    document.addEventListener('input', saveStateToLocalStorage);
    document.addEventListener('change', saveStateToLocalStorage);

    // DOM変更（カード追加/削除）を監視
    const observer = new MutationObserver(() => saveStateToLocalStorage());
    const targets = ['teams-container', 'end-conditions-container', 'startup-commands-container', 'shutdown-commands-container'];
    targets.forEach(id => {
        const el = document.getElementById(id);
        if (el) observer.observe(el, { childList: true, subtree: true });
    });
}

document.addEventListener('DOMContentLoaded', () => {
    setupCommandManagement();

    const restored = restoreStateFromLocalStorage();

    if (!restored) {
        // 初回: 既存HTMLのチームカードにイベント設定
        document.querySelectorAll('.team-card').forEach(card => {
            setupEffectManagement(card);
            setupRoleManagement(card);
        });
    }

    setupToggleDisable('bossbar-disable', ['bossbar-mcid']);
    setupToggleDisable('timelimit-disable', ['timelimit-hours', 'timelimit-minutes', 'timelimit-seconds']);
    setupTeamManagement();
    setupEndConditionManagement();
    setupQuickSetupModal();
    setupGenerateCommandButton();
    setupValidationListeners();

    setupAutoSave();
});
