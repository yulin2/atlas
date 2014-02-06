package org.atlasapi.remotesite.rovi.schedule;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.remotesite.rovi.KeyedLine;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;


public class ScheduleLine implements KeyedLine<String> {
    
    private final String sourceId;
    private final LocalDate startDate;
    private final LocalTime startTime;
    private final Boolean isTimeApproximate;
    private final int duration;
    private final String programmeId;
    private final String seriesId;
    private final String tvRating;
    private final String tvRatingReason;
    private final ShowingType showingType;
    private final CaptionType captionType;
    private final AudioLevel audioLevel;
    private final String threeDLevel;
    private final Boolean sap;
    private final String colorType;
    private final String airingType;
    private final Boolean subtitled;
    private final Boolean joinedInProgress;
    private final Boolean subjectToBlackout;
    private final String aspectRatio;
    private final Boolean descriptiveVideoService;
    private final Integer partNumber;
    private final Integer totalNumberOfParts;
    private final String hdtvLevel;
    private final Boolean syndicated;
    private final String delta;
    private final String scheduleId;
    
    public ScheduleLine(String sourceId, LocalDate startDate, LocalTime startTime,
            Boolean isTimeApproximate, int duration, String programmeId, String seriesId,
            String tvRating, String tvRatingReason, ShowingType showingType,
            CaptionType captionType, AudioLevel audioLevel, String threeDLevel, Boolean sap,
            String colorType, String airingType, Boolean subtitled, Boolean joinedInProgress,
            Boolean subjectToBlackout, String aspectRatio, Boolean descriptiveVideoService,
            Integer partNumber, Integer totalNumberOfParts, String hdtvLevel, Boolean syndicated,
            String delta, String scheduleId) {
        
        this.sourceId = checkNotNull(sourceId);
        this.startDate = checkNotNull(startDate);
        this.startTime = checkNotNull(startTime);
        this.isTimeApproximate = isTimeApproximate;
        this.duration = checkNotNull(duration);
        this.programmeId = checkNotNull(programmeId);
        this.seriesId = seriesId;
        this.tvRating = tvRating;
        this.tvRatingReason = tvRatingReason;
        this.showingType = showingType;
        this.captionType = captionType;
        this.audioLevel = audioLevel;
        this.threeDLevel = threeDLevel;
        this.sap = sap;
        this.colorType = colorType;
        this.airingType = airingType;
        this.subtitled = subtitled;
        this.joinedInProgress = joinedInProgress;
        this.subjectToBlackout = subjectToBlackout;
        this.aspectRatio = aspectRatio;
        this.descriptiveVideoService = descriptiveVideoService;
        this.partNumber = partNumber;
        this.totalNumberOfParts = totalNumberOfParts;
        this.hdtvLevel = hdtvLevel;
        this.syndicated = syndicated;
        this.delta = delta;
        this.scheduleId = checkNotNull(scheduleId);
    }

    @Override
    public String getKey() {
        return programmeId;
    }
    
    public String getSourceId() {
        return sourceId;
    }

    
    public LocalDate getStartDate() {
        return startDate;
    }

    
    public LocalTime getStartTime() {
        return startTime;
    }

    
    public Boolean getIsTimeApproximate() {
        return isTimeApproximate;
    }

    
    public int getDuration() {
        return duration;
    }

    
    public String getProgrammeId() {
        return programmeId;
    }

    
    public String getSeriesId() {
        return seriesId;
    }

    
    public String getTvRating() {
        return tvRating;
    }

    
    public String getTvRatingReason() {
        return tvRatingReason;
    }

    
    public ShowingType getShowingType() {
        return showingType;
    }

    
    public CaptionType getCaptionType() {
        return captionType;
    }

    
    public AudioLevel getAudioLevel() {
        return audioLevel;
    }

    
    public String getThreeDLevel() {
        return threeDLevel;
    }

    
    public Boolean getSap() {
        return sap;
    }

    
    public String getColorType() {
        return colorType;
    }

    
    public String getAiringType() {
        return airingType;
    }

    
    public Boolean getSubtitled() {
        return subtitled;
    }

    
    public Boolean getJoinedInProgress() {
        return joinedInProgress;
    }

    
    public Boolean getSubjectToBlackout() {
        return subjectToBlackout;
    }

    
    public String getAspectRatio() {
        return aspectRatio;
    }

    
    public Boolean getDescriptiveVideoService() {
        return descriptiveVideoService;
    }

    
    public Integer getPartNumber() {
        return partNumber;
    }

    
    public Integer getTotalNumberOfParts() {
        return totalNumberOfParts;
    }

    
    public String getHdtvLevel() {
        return hdtvLevel;
    }

    
    public Boolean getSyndicated() {
        return syndicated;
    }

    
    public String getDelta() {
        return delta;
    }

    
    public String getScheduleId() {
        return scheduleId;
    }
    
}
