package org.atlasapi.remotesite.opta.events.soccer;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.atlasapi.remotesite.opta.events.OptaDataTransformer;
import org.atlasapi.remotesite.opta.events.soccer.model.MatchDateDeserializer;
import org.atlasapi.remotesite.opta.events.soccer.model.OptaSoccerEventsFeed;
import org.atlasapi.remotesite.opta.events.soccer.model.SoccerMatchData;
import org.atlasapi.remotesite.opta.events.soccer.model.SoccerMatchInfo.MatchDate;
import org.atlasapi.remotesite.opta.events.soccer.model.SoccerTeam;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class OptaSoccerDataTransformer implements OptaDataTransformer<SoccerTeam, SoccerMatchData> {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(MatchDate.class, new MatchDateDeserializer())
            .create();
    
    public OptaSoccerDataTransformer() { }

    @Override
    public OptaSoccerEventsData transform(InputStream input) {
        OptaSoccerEventsFeed eventsFeed = gson.fromJson(new InputStreamReader(input), OptaSoccerEventsFeed.class);
        return new OptaSoccerEventsData(eventsFeed.feed().document().matchData(), eventsFeed.feed().document().teams());
    }
    
    
}
