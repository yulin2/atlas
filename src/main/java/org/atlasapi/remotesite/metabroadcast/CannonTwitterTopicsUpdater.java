package org.atlasapi.remotesite.metabroadcast;

import java.util.List;

import org.atlasapi.feeds.UpdateProgress;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.metabroadcast.common.scheduling.ScheduledTask;

public class CannonTwitterTopicsUpdater extends ScheduledTask {

    private final MetaBroadcastTwitterTopicsUpdater idUpdater;
    private final CannonTwitterTopicsClient cannonTopicsClient;

    public CannonTwitterTopicsUpdater(CannonTwitterTopicsClient cannonTopicsClient, MetaBroadcastTwitterTopicsUpdater contentTopicsUpdater) {
        this.cannonTopicsClient = cannonTopicsClient;
        this.idUpdater = contentTopicsUpdater;
    }

    @Override
    protected void runTask() {
        reportStatus("Retrieving ID list");

        Optional<ContentWordsIdList> idList = cannonTopicsClient.getIdList();

        if (!idList.isPresent()) {
            reportStatus("Couldn't get ID list");
            throw new RuntimeException("Couldn't get ID list");
        }

        UpdateProgress updateProgress = UpdateProgress.START;

        ContentWordsIdList contentWordsIdList = idList.get();
        for (List<String> idPart : Lists.partition(contentWordsIdList.contentIds, 1)) {
            reportStatus(String.format("%s. %s total", updateProgress, contentWordsIdList.contentIds.size()));
            updateProgress = updateProgress.reduce(idUpdater.updateTopics(idPart));
        }

        reportStatus(String.format("%s. %s total", updateProgress, contentWordsIdList.contentIds.size()));
    }

    public static class ContentWordsIdList {

        List<String> contentIds;

    }

}
