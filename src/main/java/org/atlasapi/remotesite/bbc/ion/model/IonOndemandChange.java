package org.atlasapi.remotesite.bbc.ion.model;

import java.util.Set;

import org.joda.time.DateTime;

public class IonOndemandChange {
    private String versionCrid;
    private Long fileSize;
    private String oid;
    private Set<IonMediaSet> mediaSets;
    private DateTime discoverableEnd;
    private String linearServiceId;
    private String masterbrandId;
    private DateTime updated;
    private String id;
    private DateTime scheduledStart;
    private String episodeCompleteTitle;
    private Boolean isFree;
    private String radioFilename;
    private DateTime actualStart;
    private String lastChange;
    private String publicationEventId;
    private Long duration;
    private String versionId;
    private String service;
    private DateTime end;
    private String episodeId;
    private String revocationStatus;
    private String bdsService;
    private String type;
    private Boolean hasCompetitionWarning;
    private Boolean hidden;
    private String toplevelContainerId;
    
    public String getVersionCrid() {
        return versionCrid;
    }
    public Long getFileSize() {
        return fileSize;
    }
    public String getOid() {
        return oid;
    }
    public Set<IonMediaSet> getMediaSets() {
        return mediaSets;
    }
    public DateTime getDiscoverableEnd() {
        return discoverableEnd;
    }
    public String getLinearServiceId() {
        return linearServiceId;
    }
    public String getMasterbrandId() {
        return masterbrandId;
    }
    public DateTime getUpdated() {
        return updated;
    }
    public String getId() {
        return id;
    }
    public DateTime getScheduledStart() {
        return scheduledStart;
    }
    public String getEpisodeCompleteTitle() {
        return episodeCompleteTitle;
    }
    public Boolean getIsFree() {
        return isFree;
    }
    public String getRadioFilename() {
        return radioFilename;
    }
    public DateTime getActualStart() {
        return actualStart;
    }
    public String getLastChange() {
        return lastChange;
    }
    public String getPublicationEventId() {
        return publicationEventId;
    }
    public Long getDuration() {
        return duration;
    }
    public String getVersionId() {
        return versionId;
    }
    public String getService() {
        return service;
    }
    public DateTime getEnd() {
        return end;
    }
    public String getEpisodeId() {
        return episodeId;
    }
    public String getRevocationStatus() {
        return revocationStatus;
    }
    public String getBdsService() {
        return bdsService;
    }
    public String getType() {
        return type;
    }
    public Boolean getHasCompetitionWarning() {
        return hasCompetitionWarning;
    }
    public Boolean getHidden() {
        return hidden;
    }
    public String getToplevelContainerId() {
        return toplevelContainerId;
    }
}
