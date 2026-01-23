package net.kunmc.lab.app.config.data.endcondition;

import java.util.ArrayList;
import java.util.List;

public class CompositeCondition extends EndCondition {
    private List<EndCondition> conditions = new ArrayList<>();
    private String operator = "AND";

    public CompositeCondition() {
    }

    public CompositeCondition(String message, List<EndCondition> conditions, String operator) {
        super(message);
        this.conditions = conditions;
        this.operator = operator;
    }

    public List<EndCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<EndCondition> conditions) {
        this.conditions = conditions;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @Override
    public String getType() {
        return "composite";
    }
}
