package net.kunmc.lab.app.config.data.endcondition;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TimeLimitCondition.class, name = "TimeLimit"),
    @JsonSubTypes.Type(value = BeaconCondition.class, name = "Beacon"),
    @JsonSubTypes.Type(value = ExterminationCondition.class, name = "Extermination"),
    @JsonSubTypes.Type(value = TicketCondition.class, name = "Ticket"),
    @JsonSubTypes.Type(value = CompositeCondition.class, name = "Composite")
})
public abstract class EndCondition {
    private String message;

    public EndCondition() {
    }

    public EndCondition(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public abstract String getType();
}
