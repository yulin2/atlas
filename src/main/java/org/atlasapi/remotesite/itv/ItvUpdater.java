package org.atlasapi.remotesite.itv;

import static org.atlasapi.persistence.logging.AdapterLogEntry.infoEntry;

import java.util.List;

import org.atlasapi.media.content.Brand;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.SiteSpecificAdapter;

import com.metabroadcast.common.scheduling.ScheduledTask;

public class ItvUpdater extends ScheduledTask {
    public static final String ITV_URI = "http://www.itv.com/_data/xml/CatchUpData/CatchUp360/CatchUpMenu.xml";

    private final RemoteSiteClient<List<ItvProgramme>> client;

    private final SiteSpecificAdapter<Brand> brandAdapter;

    private final AdapterLog log;

    public ItvUpdater(SiteSpecificAdapter<Brand> brandAdapter, AdapterLog log) {
        this(new ItvCatchupClient(), brandAdapter, log);
    }

    public ItvUpdater(RemoteSiteClient<List<ItvProgramme>> client, SiteSpecificAdapter<Brand> brandAdapter, AdapterLog log) {
        this.client = client;
        this.brandAdapter = brandAdapter;
        this.log = log;
    }

    @Override
    public void runTask() {
        try {
            log.record(infoEntry().withSource(getClass()).withDescription("ITV Updater started"));
            
            reportStatus("Fetching brands...");
            List<ItvProgramme> itvBrands = client.get(ITV_URI);
            
            int total = itvBrands.size();
            log.record(AdapterLogEntry.debugEntry().withSource(getClass()).withDescription(String.format("Fetched %s ItvProgrammes", total)));
            
            int processed = 0;
            int failed = 0;
            for (ItvProgramme programme : itvBrands) {
                try {
                    brandAdapter.fetch(ItvMercuryBrandAdapter.BASE_URL + programme.programmeId());
                } catch (Exception e) {
                    log.record(new AdapterLogEntry(Severity.WARN).withDescription("Unable to update ITV brand: " + programme.programmeId()).withCause(e));
                    failed++;
                } finally {
                    reportStatus(String.format("Processed %s / %s. %s problems", ++processed, total, failed));
                }
            }
            
            log.record(infoEntry().withSource(getClass()).withDescription(String.format("ITV Updater finished. %s problems.", failed)));
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.WARN).withDescription("Unable to update ITV content").withCause(e));
        }
    }
}
