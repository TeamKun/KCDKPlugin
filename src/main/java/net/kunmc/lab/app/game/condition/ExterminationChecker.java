package net.kunmc.lab.app.game.condition;

import net.kunmc.lab.app.game.GameManager;
import net.kunmc.lab.app.game.PlayerData;
import net.kunmc.lab.app.game.TeamData;

public class ExterminationChecker implements ConditionChecker {
    private final String teamName;
    private final String message;

    public ExterminationChecker(String teamName, String message) {
        this.teamName = teamName;
        this.message = message;
    }

    @Override
    public boolean isMet(GameManager gameManager) {
        // "red.captain" のようにドットを含む場合はチーム名+ロール名として扱う
        String actualTeam;
        String roleName;
        int dotIndex = teamName.indexOf('.');
        if (dotIndex >= 0) {
            actualTeam = teamName.substring(0, dotIndex);
            roleName = teamName.substring(dotIndex + 1);
        } else {
            actualTeam = teamName;
            roleName = null;
        }

        TeamData teamData = gameManager.getTeams().get(actualTeam);
        if (teamData == null) return false;
        if (teamData.getPlayers().isEmpty()) return false;

        boolean hasTarget = false;
        for (java.util.UUID uuid : teamData.getPlayers()) {
            PlayerData pd = gameManager.getPlayers().get(uuid);
            if (pd == null) continue;

            // ロール指定がある場合はロールでフィルタ
            if (roleName != null) {
                if (!roleName.equals(pd.getRoleName())) continue;
            }

            hasTarget = true;
            if (pd.isAlive()) {
                return false;
            }
        }
        // ロール指定で該当プレイヤーがいなかった場合はfalse
        return hasTarget;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
