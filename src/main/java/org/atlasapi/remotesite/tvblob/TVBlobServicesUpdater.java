package org.atlasapi.remotesite.tvblob;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;

import com.google.inject.internal.Lists;

public class TVBlobServicesUpdater implements Runnable {

    private static final String URL = "http://epgadmin.tvblob.com/api/services.json";
    private static final String DAY_URL_BASE = "http://epgadmin.tvblob.com/api/%s/programmes/schedules/%s.json";

    private final TVBlobServicesClient servicesClient;
    private final TVBlobDayAdapter adapter;

    static final Log LOG = LogFactory.getLog(TVBlobServicesUpdater.class);
    private final String day;

    public TVBlobServicesUpdater(ContentWriter contentStore, ContentResolver contentResolver, String day) {
        this(new TVBlobServicesClient(), new TVBlobDayAdapter(contentStore, contentResolver), day);
    }

    public TVBlobServicesUpdater(TVBlobServicesClient servicesClient, TVBlobDayAdapter adapter, String day) {
        this.servicesClient = servicesClient;
        this.adapter = adapter;
        this.day = day;
    }

    @Override
    public void run() {
        List<TVBlobService> services = Lists.newArrayList();
        try {
            services = servicesClient.get(URL);
        } catch (Exception e) {
            LOG.warn("Unable to retrieve TVBlob services", e);
        }

        for (TVBlobService service : services) {
            String dayUrl = String.format(DAY_URL_BASE, service.getSlug(), day);
            adapter.populate(dayUrl);
        }
    }

}
