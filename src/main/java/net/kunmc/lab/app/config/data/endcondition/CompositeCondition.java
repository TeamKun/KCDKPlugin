package net.kunmc.lab.app.config.data.endcondition;

import java.util.ArrayList;
import java.util.List;

public class CompositeCondition extends EndCondition {
    private List<EndCondition> conditions = new ArrayList<>();

    public CompositeCondition() {
    }

    public CompositeCondition(String message, List<EndCondition> conditions) {
        super(message);
        this.conditions = conditions;
    }

    public List<EndCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<EndCondition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public String getType() {
        return "Composite";
    }
}
