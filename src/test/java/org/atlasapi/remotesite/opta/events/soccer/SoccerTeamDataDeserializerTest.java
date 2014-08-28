package org.atlasapi.remotesite.opta.events.soccer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.atlasapi.remotesite.opta.events.soccer.model.SoccerTeamData;
import org.atlasapi.remotesite.opta.events.soccer.model.SoccerTeamDataDeserializer;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class SoccerTeamDataDeserializerTest {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(SoccerTeamData.class, new SoccerTeamDataDeserializer())
            .create();
    
    @Test
    public void testDeserializesArrayGoals() {
        String goalsArrayString = "{ \"Goal\": [ { \"@value\": \"\", \"@attributes\": { \"Period\": \"FirstHalf\", \"PlayerRef\": \"p51507\", \"Type\": \"Goal\" } }, { \"@value\": \"\", \"@attributes\": { \"Period\": \"SecondHalf\", \"PlayerRef\": \"p41792\", \"Type\": \"Goal\" } } ], \"@attributes\": { \"HalfScore\": \"1\", \"Score\": \"2\", \"Side\": \"Home\", \"TeamRef\": \"t3\" } }";
        SoccerTeamData data = gson.fromJson(goalsArrayString, SoccerTeamData.class);
     
        assertEquals(2, data.goals().size());
    }

    @Test
    public void testDeserializesObjectGoals() {
        String goalsObjectString = "{ \"Goal\": { \"@value\": \"\", \"@attributes\": { \"Period\": \"FirstHalf\", \"PlayerRef\": \"p15284\", \"Type\": \"Goal\" } }, \"@attributes\": { \"HalfScore\": \"1\", \"Score\": \"1\", \"Side\": \"Away\", \"TeamRef\": \"t31\" } }";
        SoccerTeamData data = gson.fromJson(goalsObjectString, SoccerTeamData.class);
     
        assertEquals(1, data.goals().size());
    }

    @Test
    public void testDeserializesWhenNoGoalKey() {
        String teamDataWithoutGoalsString = "{ \"@value\": \"\", \"@attributes\": { \"Side\": \"Home\", \"TeamRef\": \"t110\" } }";
        SoccerTeamData data = gson.fromJson(teamDataWithoutGoalsString, SoccerTeamData.class);
     
        assertTrue(data.goals().isEmpty());
    }
}
