package org.atlasapi.remotesite.bbc;

public interface ProgressStore {

    void saveProgress(String channel, String pid);

    ChannelAndPid getProgress();
    
    void resetProgress();

}