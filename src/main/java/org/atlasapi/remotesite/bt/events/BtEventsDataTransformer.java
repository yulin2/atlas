package org.atlasapi.remotesite.bt.events;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.atlasapi.remotesite.bt.events.feedModel.BtEvent;
import org.atlasapi.remotesite.bt.events.feedModel.BtEventsFeed;
import org.atlasapi.remotesite.bt.events.feedModel.BtTeam;
import org.atlasapi.remotesite.bt.events.model.BtEventsData;
import org.atlasapi.remotesite.events.EventsDataTransformer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class BtEventsDataTransformer implements EventsDataTransformer<BtTeam, BtEvent> {

    private final Gson gson = new GsonBuilder().create();
    
    @Override
    public BtEventsData transform(InputStream input) {
        BtEventsFeed eventsFeed = gson.fromJson(new InputStreamReader(input), BtEventsFeed.class);
        return new BtEventsData(eventsFeed.response().docs());
    }

}
