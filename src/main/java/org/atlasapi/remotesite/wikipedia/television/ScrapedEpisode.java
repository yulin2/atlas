package org.atlasapi.remotesite.wikipedia.television;

import org.atlasapi.remotesite.wikipedia.SwebleHelper.ListItemResult;
import org.joda.time.LocalDate;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class ScrapedEpisode {
    public int numberInShow;
    public int numberInSeason;
    public SeasonSectionScraper.Result season;
    public ImmutableList<ListItemResult> title;
    public ImmutableList<ListItemResult> director;
    public ImmutableList<ListItemResult> writer;
    public Optional<LocalDate> originalAirDate = Optional.absent();
    public String prodCode;
    public String summary;

    @Override
    public String toString() {
        return "Result{" + "numberInShow=" + numberInShow + ", numberInSeason=" + numberInSeason + ", season=" + (season==null ? null : season.name) + ", title=" + title + ", director=" + director + ", writer=" + writer + ", originalAirDate=" + originalAirDate + ", prodCode=" + prodCode + ", summary=" + summary + '}';
    }
}