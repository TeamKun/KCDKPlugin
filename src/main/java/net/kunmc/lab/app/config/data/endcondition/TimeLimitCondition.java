package net.kunmc.lab.app.config.data.endcondition;

public class TimeLimitCondition extends EndCondition {

    public TimeLimitCondition() {
    }

    public TimeLimitCondition(String message) {
        super(message);
    }

    @Override
    public String getType() {
        return "TimeLimit";
    }
}
