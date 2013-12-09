package org.atlasapi.remotesite.bbc.ion.model;

public class IonSegment {

    private String mediumSynopsis;
    private String trackNumber;
    private String releaseTitle;
    private String recordingDate;
    private String trackSide;
    private String segmentType;
    private String recordLabel;
    private String pid;
    private String longSynopsis;
    private String publisher;
    private String musicCode;
    private String sourceMedia;
    private String shortSynopsis;
    private Integer duration;
    private String catalogue_number;
    private String title;
    //Disabled because this is unused 
    //and now an array in the supplied JSON
    //private String contributions;

    public String getMediumSynopsis() {
        return this.mediumSynopsis;
    }

    public String getTrackNumber() {
        return this.trackNumber;
    }

    public String getReleaseTitle() {
        return this.releaseTitle;
    }

    public String getRecordingDate() {
        return this.recordingDate;
    }

    public String getTrackSide() {
        return this.trackSide;
    }

    public String getSegmentType() {
        return this.segmentType;
    }

    public String getRecordLabel() {
        return this.recordLabel;
    }

    public String getPid() {
        return this.pid;
    }

    public String getLongSynopsis() {
        return this.longSynopsis;
    }

    public String getPublisher() {
        return this.publisher;
    }

    public String getMusicCode() {
        return this.musicCode;
    }

    public String getSourceMedia() {
        return this.sourceMedia;
    }

    public String getShortSynopsis() {
        return this.shortSynopsis;
    }

    public Integer getDuration() {
        return this.duration;
    }

    public String getCatalogue_number() {
        return this.catalogue_number;
    }

    public String getTitle() {
        return this.title;
    }

//    public String getContributions() {
//        return this.contributions;
//    }

}