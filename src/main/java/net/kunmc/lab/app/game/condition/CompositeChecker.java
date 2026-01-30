package net.kunmc.lab.app.game.condition;

import net.kunmc.lab.app.game.GameManager;

import java.util.List;

public class CompositeChecker implements ConditionChecker {
    private final List<ConditionChecker> children;
    private final String message;

    public CompositeChecker(List<ConditionChecker> children, String message) {
        this.children = children;
        this.message = message;
    }

    @Override
    public boolean isMet(GameManager gameManager) {
        for (ConditionChecker child : children) {
            if (!child.isMet(gameManager)) {
                return false;
            }
        }
        return !children.isEmpty();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
