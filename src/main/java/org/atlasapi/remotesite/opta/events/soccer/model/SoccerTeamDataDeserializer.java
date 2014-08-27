package org.atlasapi.remotesite.opta.events.soccer.model;

import java.lang.reflect.Type;

import org.atlasapi.remotesite.opta.events.soccer.model.SoccerTeamData.TeamDataAttributes;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;


public class SoccerTeamDataDeserializer implements JsonDeserializer<SoccerTeamData> {

    @Override
    public SoccerTeamData deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObj = json.getAsJsonObject();
        return new SoccerTeamData(parseGoals(jsonObj,context), parseAttributes(jsonObj, context));
    }

    /**
     * This is needed because Opta like to turn a singleton array of Goal objects into just an object,
     * which Gson understandably can't deserialize on its own. They also will not add the 'Goal' key at
     * all if there aren't any Goals records.
     * @param jsonObj
     * @param context
     * @return
     */
    private Iterable<SoccerGoal> parseGoals(JsonObject jsonObj, final JsonDeserializationContext context) {
        if (!jsonObj.has("Goal")) {
            return ImmutableList.of();
        }
        JsonElement goals = jsonObj.get("Goal");
        if (goals.isJsonArray()) {
            return Iterables.transform(goals.getAsJsonArray(), new Function<JsonElement, SoccerGoal>() {
                @Override
                public SoccerGoal apply(JsonElement input) {
                    return context.deserialize(input, SoccerGoal.class);
                }}
            );
        } else {
            return ImmutableList.of((SoccerGoal)context.deserialize(goals, SoccerGoal.class));
        }
    }

    private TeamDataAttributes parseAttributes(JsonObject jsonObj, JsonDeserializationContext context) {
        return context.deserialize(jsonObj.get("@attributes"), TeamDataAttributes.class);
    }

}
