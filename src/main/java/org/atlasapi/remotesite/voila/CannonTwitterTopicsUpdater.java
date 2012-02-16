package org.atlasapi.remotesite.voila;

import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;

import java.util.List;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.system.RemoteSiteClient;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.net.HostSpecifier;
import com.metabroadcast.common.scheduling.ScheduledTask;

public class CannonTwitterTopicsUpdater extends ScheduledTask {

    private final RemoteSiteClient<ContentWordsIdList> idListClient;
    private final String idListUri;
    private final AdapterLog log;
    private final ContentTwitterTopicsUpdater idUpdater;

    public CannonTwitterTopicsUpdater(RemoteSiteClient<ContentWordsIdList> client, HostSpecifier cannonHost, Optional<Integer> cannonPort, ContentTwitterTopicsUpdater idUpdater, AdapterLog log) {
        this.idListUri = String.format("http://%s%s/contentWordsList", cannonHost, cannonPort.isPresent() ? ":"+cannonPort.get():"");
        this.idListClient = client;
        this.idUpdater = idUpdater;
        this.log = log;
    }
    
    @Override
    protected void runTask() {
        reportStatus(String.format("Retrieving ID list from %s", idListUri));
        
        ContentWordsIdList contentWordsList = null;
        try {
            contentWordsList = idListClient.get(idListUri);
        } catch (Exception e) {
            log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Failed to get contentWordsList"));
            throw Throwables.propagate(e);
        }
        
        int done = 0;
        for (List<String> idPart : Lists.partition(contentWordsList.contentIds, 10)) {
            reportStatus(String.format("Fetched %s of %s IDs", done, contentWordsList.contentIds.size()));
            idUpdater.updateTopics(idPart);
            done += idPart.size();
        }
        
        reportStatus(String.format("Fetched %s IDs", done));
    }

    public static class ContentWordsIdList {
        
        List<String> contentIds;
        
    }
    
}
