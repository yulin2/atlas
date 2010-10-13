package org.atlasapi.remotesite.itv;

import java.util.List;

import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.query.uri.SavingFetcher;
import org.atlasapi.remotesite.FetchException;

public class ItvUpdater implements Runnable {
    public static final String ITV_URI = "http://www.itv.com/_data/xml/CatchUpData/CatchUp360/CatchUpMenu.xml";

    private final RemoteSiteClient<List<ItvProgramme>> client;

    private final SavingFetcher fetcher;

    public ItvUpdater(SavingFetcher savingFetcher) {
        this(new ItvCatchupClient(), savingFetcher);
    }

    public ItvUpdater(RemoteSiteClient<List<ItvProgramme>> client, SavingFetcher fetcher) {
        this.client = client;
        this.fetcher = fetcher;
    }

    @Override
    public void run() {
        try {
            List<ItvProgramme> itvBrands = client.get(ITV_URI);
            
            for (ItvProgramme programme: itvBrands) {
                fetcher.fetch(ItvMercuryBrandAdapter.BASE_URL+programme.programmeId());
            }
        } catch (Exception e) {
            throw new FetchException("Unable to update ITV content", e);
        }
    }
}
