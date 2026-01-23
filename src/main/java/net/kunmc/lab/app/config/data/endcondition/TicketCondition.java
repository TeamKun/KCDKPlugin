package net.kunmc.lab.app.config.data.endcondition;

public class TicketCondition extends EndCondition {
    private String team;
    private int count;

    public TicketCondition() {
    }

    public TicketCondition(String message, String team, int count) {
        super(message);
        this.team = team;
        this.count = count;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String getType() {
        return "ticket";
    }
}
