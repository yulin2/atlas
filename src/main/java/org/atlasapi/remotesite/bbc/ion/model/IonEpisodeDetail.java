package org.atlasapi.remotesite.bbc.ion.model;

import java.util.List;

public class IonEpisodeDetail extends IonEpisode {

    private List<IonVersion> versions;
    private List<IonEpisode> clips;
    
    public void setVersions(List<IonVersion> versions) {
        this.versions = versions;
    }

    public List<IonVersion> getVersions() {
        return versions;
    }
 
    public void setClips(List<IonEpisode> clips) {
        this.clips = clips;
    }

    public List<IonEpisode> getClips() {
        return clips;
    }
}
