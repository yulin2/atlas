package org.atlasapi.remotesite.itv;

import java.util.List;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.ContentWriters;
import org.atlasapi.remotesite.SiteSpecificAdapter;

public class ItvUpdater implements Runnable {
    public static final String ITV_URI = "http://www.itv.com/_data/xml/CatchUpData/CatchUp360/CatchUpMenu.xml";

    private final RemoteSiteClient<List<ItvProgramme>> client;

    private final ContentWriters writer;

    private final SiteSpecificAdapter<Brand> brandAdapter;

    private final AdapterLog log;

    public ItvUpdater(SiteSpecificAdapter<Brand> brandAdapter, ContentWriters writer, AdapterLog log) {
        this(new ItvCatchupClient(), brandAdapter, writer, log);
    }

    public ItvUpdater(RemoteSiteClient<List<ItvProgramme>> client, SiteSpecificAdapter<Brand> brandAdapter, ContentWriters writer, AdapterLog log) {
        this.client = client;
        this.brandAdapter = brandAdapter;
        this.writer = writer;
        this.log = log;
    }

    @Override
    public void run() {
        try {
            List<ItvProgramme> itvBrands = client.get(ITV_URI);

            for (ItvProgramme programme : itvBrands) {
                try {
                    Brand brand = brandAdapter.fetch(ItvMercuryBrandAdapter.BASE_URL + programme.programmeId());
                    if (brand != null) {
                        writer.createOrUpdate(brand, true);
                    }
                } catch (Exception e) {
                    log.record(new AdapterLogEntry(Severity.WARN).withDescription("Unable to update ITV brand: "+programme.programmeId()).withCause(e));
                }
            }
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.WARN).withDescription("Unable to update ITV content").withCause(e));
        }
    }
}
