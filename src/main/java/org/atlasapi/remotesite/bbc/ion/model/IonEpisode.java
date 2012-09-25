package org.atlasapi.remotesite.bbc.ion.model;

import java.net.URL;
import java.util.List;

import org.joda.time.DateTime;

import com.google.common.base.Strings;

public class IonEpisode {

    private String mySeriesUrl;
    private Boolean isEmbargoed;
    private URL myMediaselectorJsonUrl;
    private Boolean playVersionHasCompetitionWarning;
    private Boolean isEmbeddable;
    private String originalTitle;
    private String stackedContainerId;
    private String completeTitle;
    private Boolean isSimulcast;
    private String subseriesTitle;
    private Boolean isDownloadableSd;
    private Boolean isRepeat;
    private DateTime actualStart;
    private String masterbrandTitle;
    private String title;
    private Boolean isDownloadableHd;
    private Boolean isAvailableMediasetPcHd;
    private String partner;
    private Boolean isDelayed;
    private Long availableSiblingCount;
    private String myShortUrl;
    private Boolean isFilm;
    private String contextualFamilyTree;
    private Boolean isAvailableMediasetPcSd;
    private URL passionsiteLink;
    private String mediaType;
    private String passionsiteTitle;
    private List<IonCategory> categories;
    private String seriesId;
    private String toplevelContainerId;
    private DateTime availableUntil;
    private URL myImageBaseUrl;
    private Boolean isAvailableMediasetStbSd;
    private String masterbrand;
    private String playVersionId;
    //private List<IonTagScheme> tagSchemes;
    private Boolean isStacked;
    private String parentId;
    private String myPlaylistUrl;
    private String toplevelContainerTitle;
    private String synopsis;
    private String hasGuidance;
    private String ondemandStart;
    private String myAlternateUrl;
    private String id;
    private DateTime updated;
    private Boolean isFree;
    private Boolean isClip;
    private String brandTitle;
    private Long duration;
    private String subseriesId;
    private String hierarchicalTitle;
    private String type;
    private String brandId;
    private String myUrl;
    private DateTime originalBroadcastDatetime;
    private Long position;
    private String seriesTitle;
    private Boolean isAvailableMediasetStbHd;
    private String availability;
    private Boolean isDownloadable;
    private String shortSynopsis;
    private Boolean isHdOnly;
    private URL myMediaselectorXmlUrl;
    private List<IonContributor> contributors;
    private List<IonGenre> genres;

    public List<IonContributor> getContributors() {
        return contributors;
    }

    public void setContributors(List<IonContributor> contributors) {
        this.contributors = contributors;
    }

    public String getMySeriesUrl() {
        return mySeriesUrl;
    }

    public Boolean getIsEmbargoed() {
        return isEmbargoed;
    }

    public URL getMyMediaselectorJsonUrl() {
        return myMediaselectorJsonUrl;
    }

    public Boolean getPlayVersionHasCompetitionWarning() {
        return playVersionHasCompetitionWarning;
    }

    public Boolean getIsEmbeddable() {
        return isEmbeddable;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getStackedContainerId() {
        return stackedContainerId;
    }

    public String getCompleteTitle() {
        return completeTitle;
    }

    public Boolean getIsSimulcast() {
        return isSimulcast;
    }

    public String getSubseriesTitle() {
        return subseriesTitle;
    }

    public Boolean getIsDownloadableSd() {
        return isDownloadableSd;
    }

    public Boolean getIsRepeat() {
        return isRepeat;
    }

    public DateTime getActualStart() {
        return actualStart;
    }

    public String getMasterbrandTitle() {
        return masterbrandTitle;
    }

    public String getTitle() {
        return title;
    }

    public Boolean getIsDownloadableHd() {
        return isDownloadableHd;
    }

    public Boolean getIsAvailableMediasetPcHd() {
        return isAvailableMediasetPcHd;
    }

    public String getPartner() {
        return partner;
    }

    public Boolean getIsDelayed() {
        return isDelayed;
    }

    public Long getAvailableSiblingCount() {
        return availableSiblingCount;
    }

    public String getMyShortUrl() {
        return myShortUrl;
    }

    public Boolean getIsFilm() {
        return isFilm;
    }

    public String getContextualFamilyTree() {
        return contextualFamilyTree;
    }

    public Boolean getIsAvailableMediasetPcSd() {
        return isAvailableMediasetPcSd;
    }

    public URL getPassionsiteLink() {
        return passionsiteLink;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getPassionsiteTitle() {
        return passionsiteTitle;
    }

    public List<IonCategory> getCategories() {
        return categories;
    }

    public String getSeriesId() {
        return seriesId;
    }

    public String getToplevelContainerId() {
        return toplevelContainerId;
    }

    public DateTime getAvailableUntil() {
        return availableUntil;
    }

    public URL getMyImageBaseUrl() {
        return myImageBaseUrl;
    }

    public Boolean getIsAvailableMediasetStbSd() {
        return isAvailableMediasetStbSd;
    }

    public String getMasterbrand() {
        return masterbrand;
    }

    public String getPlayVersionId() {
        return playVersionId;
    }

//    public List<IonTagScheme> getTagSchemes() {
//        return tagSchemes;
//    }

    public Boolean getIsStacked() {
        return isStacked;
    }

    public String getParent_id() {
        return parentId;
    }

    public String getMyPlaylistUrl() {
        return myPlaylistUrl;
    }

    public String getToplevelContainerTitle() {
        return toplevelContainerTitle;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public String getHasGuidance() {
        return hasGuidance;
    }

    public String getOndemandStart() {
        return ondemandStart;
    }

    public String getMyAlternateUrl() {
        return myAlternateUrl;
    }

    public String getId() {
        return id;
    }

    public DateTime getUpdated() {
        return updated;
    }

    public Boolean getIsFree() {
        return isFree;
    }

    public Boolean getIsClip() {
        return isClip;
    }

    public String getBrandTitle() {
        return brandTitle;
    }

    public Long getDuration() {
        return duration;
    }

    public String getSubseriesId() {
        return subseriesId;
    }

    public String getHierarchicalTitle() {
        return hierarchicalTitle;
    }

    public String getType() {
        return type;
    }

    public String getBrandId() {
        return brandId;
    }

    public String getMyUrl() {
        return myUrl;
    }

    public DateTime getOriginalBroadcastDatetime() {
        return originalBroadcastDatetime;
    }

    public Long getPosition() {
        return position;
    }

    public String getSeriesTitle() {
        return seriesTitle;
    }

    public Boolean getIsAvailableMediasetStbHd() {
        return isAvailableMediasetStbHd;
    }

    public String getAvailability() {
        return availability;
    }

    public Boolean getIsDownloadable() {
        return isDownloadable;
    }

    public String getShortSynopsis() {
        return shortSynopsis;
    }

    public Boolean getIsHdOnly() {
        return isHdOnly;
    }

    public URL getMyMediaselectorXmlUrl() {
        return myMediaselectorXmlUrl;
    }

    public String getParentId() {
        return parentId;
    }
    
    public static class IonTagScheme {
        //not used
    }
    
    public List<IonGenre> getGenres() {
        return genres;
    }

    public static class IonCategory {
        private String shortName;
        private String path;
        private Long episodeCount;
        private String parentId;
        private Long level;
        private String text;
        private DateTime updated;
        private String type;
        private String id;
        private String title;

        public String getShortName() {
            return shortName;
        }

        public String getPath() {
            return path;
        }

        public Long getEpisodeCount() {
            return episodeCount;
        }

        public String getParentId() {
            return parentId;
        }

        public Long getLevel() {
            return level;
        }

        public String getText() {
            return text;
        }

        public DateTime getUpdated() {
            return updated;
        }

        public String getType() {
            return type;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }
    }
    
    public boolean hasSeries() {
        return !Strings.isNullOrEmpty(getSeriesId());
    }

    public boolean hasBrand() {
        return !Strings.isNullOrEmpty(getBrandId());
    }
}