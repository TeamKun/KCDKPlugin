package net.kunmc.lab.app.config.data.endcondition;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = BeaconCondition.class, name = "beacon"),
    @JsonSubTypes.Type(value = ExterminationCondition.class, name = "extermination"),
    @JsonSubTypes.Type(value = TicketCondition.class, name = "ticket"),
    @JsonSubTypes.Type(value = CompositeCondition.class, name = "composite")
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
