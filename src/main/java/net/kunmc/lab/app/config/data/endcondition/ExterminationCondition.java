package net.kunmc.lab.app.config.data.endcondition;

public class ExterminationCondition extends EndCondition {
    private String team;

    public ExterminationCondition() {
    }

    public ExterminationCondition(String message, String team) {
        super(message);
        this.team = team;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    @Override
    public String getType() {
        return "Extermination";
    }
}
