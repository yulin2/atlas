package org.atlasapi.remotesite.bbc.ion.model;

import java.net.URL;

import org.joda.time.DateTime;

import com.google.common.base.Strings;

public class IonBroadcast {

    private String aspectRatio;
    private DateTime date;
    private DateTime startTimeIso;
    private Boolean hasGuidance;
    private String completeTitle;
    private DateTime updated;
    private String id;
    private IonEpisode episode;
    private String myurl;
    private Boolean isRepeat;
    private Long duration;
    private Boolean isAudiodescribed;
    private String versionId;
    private String service;
    private DateTime end;
    private Boolean onNow;
    private Boolean isBlanked;
    private String serviceTitle;
    private String type;
    private String title;
    private DateTime start;
    private String mediumSynopsis;
    private String brandId;
    private String mediaType;
    private URL passionsiteLink;
    private DateTime endTimeIso;
    private String parentTitle;
    private String shortSynopsis;
    private String passsionsiteTitle;
    private String seriesId;
    private Boolean isSigned;
    private String episodeId;

    public String getAspectRatio() {
        return aspectRatio;
    }

    public DateTime getDate() {
        return date;
    }

    public DateTime getStartTimeIso() {
        return startTimeIso;
    }

    public Boolean isHasGuidance() {
        return hasGuidance;
    }

    public String getCompleteTitle() {
        return completeTitle;
    }

    public DateTime getUpdated() {
        return updated;
    }

    public String getId() {
        return id;
    }

    public IonEpisode getEpisode() {
        return episode;
    }

    public String getMyurl() {
        return myurl;
    }

    public Boolean getIsRepeat() {
        return isRepeat;
    }

    public Long getDuration() {
        return duration;
    }

    public Boolean getIsAudiodescribed() {
        return isAudiodescribed;
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

    public Boolean getOnNow() {
        return onNow;
    }

    public Boolean getIsBlanked() {
        return isBlanked;
    }

    public String getServiceTitle() {
        return serviceTitle;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public DateTime getStart() {
        return start;
    }

    public String getMediumSynopsis() {
        return mediumSynopsis;
    }

    public String getBrandId() {
        return brandId;
    }

    public String getMediaType() {
        return mediaType;
    }

    public URL getPassionsiteLink() {
        return passionsiteLink;
    }

    public DateTime getEndTimeIso() {
        return endTimeIso;
    }

    public String getParentTitle() {
        return parentTitle;
    }

    public String getShortSynopsis() {
        return shortSynopsis;
    }

    public String getPasssionsiteTitle() {
        return passsionsiteTitle;
    }

    public String getSeriesId() {
        return seriesId;
    }

    public Boolean getIsSigned() {
        return isSigned;
    }

    public String getEpisodeId() {
        return episodeId;
    }
    
    public boolean hasSeries() {
        return !Strings.isNullOrEmpty(getSeriesId());
    }

    public boolean hasBrand() {
        return !Strings.isNullOrEmpty(getBrandId());
    }

    public void setAspectRatio(String aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public void setStartTimeIso(DateTime startTimeIso) {
        this.startTimeIso = startTimeIso;
    }

    public void setHasGuidance(Boolean hasGuidance) {
        this.hasGuidance = hasGuidance;
    }

    public void setCompleteTitle(String completeTitle) {
        this.completeTitle = completeTitle;
    }

    public void setUpdated(DateTime updated) {
        this.updated = updated;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEpisode(IonEpisode episode) {
        this.episode = episode;
    }

    public void setMyurl(String myurl) {
        this.myurl = myurl;
    }

    public void setIsRepeat(Boolean isRepeat) {
        this.isRepeat = isRepeat;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public void setIsAudiodescribed(Boolean isAudiodescribed) {
        this.isAudiodescribed = isAudiodescribed;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public void setService(String service) {
        this.service = service;
    }

    public void setEnd(DateTime end) {
        this.end = end;
    }

    public void setOnNow(Boolean onNow) {
        this.onNow = onNow;
    }

    public void setIsBlanked(Boolean isBlanked) {
        this.isBlanked = isBlanked;
    }

    public void setServiceTitle(String serviceTitle) {
        this.serviceTitle = serviceTitle;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStart(DateTime start) {
        this.start = start;
    }

    public void setMediumSynopsis(String mediumSynopsis) {
        this.mediumSynopsis = mediumSynopsis;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public void setPassionsiteLink(URL passionsiteLink) {
        this.passionsiteLink = passionsiteLink;
    }

    public void setEndTimeIso(DateTime endTimeIso) {
        this.endTimeIso = endTimeIso;
    }

    public void setParentTitle(String parentTitle) {
        this.parentTitle = parentTitle;
    }

    public void setShortSynopsis(String shortSynopsis) {
        this.shortSynopsis = shortSynopsis;
    }

    public void setPasssionsiteTitle(String passsionsiteTitle) {
        this.passsionsiteTitle = passsionsiteTitle;
    }

    public void setSeriesId(String seriesId) {
        this.seriesId = seriesId;
    }

    public void setIsSigned(Boolean isSigned) {
        this.isSigned = isSigned;
    }

    public void setEpisodeId(String episodeId) {
        this.episodeId = episodeId;
    }
}
