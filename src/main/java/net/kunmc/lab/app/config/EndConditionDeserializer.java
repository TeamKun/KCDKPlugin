package net.kunmc.lab.app.config;

import com.google.gson.*;
import net.kunmc.lab.app.config.data.GameLocation;
import net.kunmc.lab.app.config.data.endcondition.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class EndConditionDeserializer implements JsonDeserializer<EndCondition> {

    @Override
    public EndCondition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("type").getAsString();
        String message = jsonObject.has("message") ? jsonObject.get("message").getAsString() : "";

        switch (type) {
            case "TimeLimit":
                return new TimeLimitCondition(message);

            case "Beacon":
                GameLocation location = context.deserialize(jsonObject.get("location"), GameLocation.class);
                int hitpoint = jsonObject.get("hitpoint").getAsInt();
                return new BeaconCondition(message, location, hitpoint);

            case "Extermination":
                String team = jsonObject.get("team").getAsString();
                return new ExterminationCondition(message, team);

            case "Ticket":
                String ticketTeam = jsonObject.get("team").getAsString();
                int count = jsonObject.get("count").getAsInt();
                return new TicketCondition(message, ticketTeam, count);

            case "Composite":
                JsonArray conditionsArray = jsonObject.getAsJsonArray("conditions");
                List<EndCondition> conditions = new ArrayList<>();
                for (JsonElement element : conditionsArray) {
                    conditions.add(context.deserialize(element, EndCondition.class));
                }
                return new CompositeCondition(message, conditions);

            default:
                throw new JsonParseException("Unknown EndCondition type: " + type);
        }
    }
}
