package org.atlasapi.remotesite.bbc;

import java.util.Map.Entry;

public interface ProgressStore {

    void saveProgress(String channel, String pid);

    Entry<String, String> getProgress();

}