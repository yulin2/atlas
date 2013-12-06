package org.atlasapi.remotesite.wikipedia.television;

import org.joda.time.LocalDate;

import com.google.common.base.Optional;

public class ScrapedEpisode {
    public int numberInShow;
    public int numberInSeason;
    public SeasonSectionScraper.Result season;
    public String title;
    public String director;
    public String writer;
    public Optional<LocalDate> originalAirDate = Optional.absent();
    public String prodCode;
    public String summary;

    @Override
    public String toString() {
        return "Result{" + "numberInShow=" + numberInShow + ", numberInSeason=" + numberInSeason + ", season=" + (season==null ? null : season.name) + ", title=" + title + ", director=" + director + ", writer=" + writer + ", originalAirDate=" + originalAirDate + ", prodCode=" + prodCode + ", summary=" + summary + '}';
    }
}