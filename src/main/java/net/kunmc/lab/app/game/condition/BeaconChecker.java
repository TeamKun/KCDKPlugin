package net.kunmc.lab.app.game.condition;

import net.kunmc.lab.app.config.data.GameLocation;
import net.kunmc.lab.app.game.GameManager;

public class BeaconChecker implements ConditionChecker {
    private final GameLocation location;
    private final String message;

    public BeaconChecker(GameLocation location, String message) {
        this.location = location;
        this.message = message;
    }

    @Override
    public boolean isMet(GameManager gameManager) {
        Integer hp = gameManager.getBeaconHPs().get(location);
        return hp != null && hp <= 0;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
