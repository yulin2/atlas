package org.atlasapi.remotesite.opta.events.sports;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.atlasapi.remotesite.opta.events.OptaDataTransformer;
import org.atlasapi.remotesite.opta.events.OptaEventsData;
import org.atlasapi.remotesite.opta.events.sports.model.OptaFixture;
import org.atlasapi.remotesite.opta.events.sports.model.OptaSportsFeed;
import org.atlasapi.remotesite.opta.events.sports.model.OptaSportsTeam;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public final class OptaSportsDataTransformer implements OptaDataTransformer<OptaSportsTeam, OptaFixture> {

    private final Gson gson = new GsonBuilder().create();

    @Override
    public OptaEventsData<OptaSportsTeam, OptaFixture> transform(InputStream input) {
        OptaSportsFeed eventsFeed = gson.fromJson(new InputStreamReader(input), OptaSportsFeed.class);
        return new OptaSportsEventsData(eventsFeed.fixtures().fixtures(), eventsFeed.fixtures().teams().teams());
    }
    
    
}
