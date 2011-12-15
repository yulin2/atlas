package org.atlasapi.remotesite.bbc.ion.model;

public class IonSegmentEvent {

    private String mediumSynopsis;
    private String shortSynopsis;
    private Integer position;
    private Boolean isChapter;
    private String pid;
    private String longSynopsis;
    private String title;
    private Integer offset;
    
    private IonSegment segment;

    public String getMediumSynopsis() {
        return this.mediumSynopsis;
    }

    public String getShortSynopsis() {
        return this.shortSynopsis;
    }

    public Integer getPosition() {
        return this.position;
    }

    public Boolean getIsChapter() {
        return this.isChapter;
    }

    public String getPid() {
        return this.pid;
    }

    public String getLongSynopsis() {
        return this.longSynopsis;
    }

    public String getTitle() {
        return this.title;
    }

    public Integer getOffset() {
        return this.offset;
    }

    public IonSegment getSegment() {
        return segment;
    }

}
