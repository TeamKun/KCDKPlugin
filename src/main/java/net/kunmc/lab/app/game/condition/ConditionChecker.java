package net.kunmc.lab.app.game.condition;

import net.kunmc.lab.app.game.GameManager;

public interface ConditionChecker {
    boolean isMet(GameManager gameManager);
    String getMessage();
}
