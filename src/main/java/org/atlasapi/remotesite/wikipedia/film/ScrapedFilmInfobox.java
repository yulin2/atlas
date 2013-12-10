package org.atlasapi.remotesite.wikipedia.film;

import java.util.Map;
import java.util.TreeMap;

import org.atlasapi.remotesite.wikipedia.SwebleHelper.ListItemResult;
import org.atlasapi.remotesite.wikipedia.film.FilmInfoboxScraper.ReleaseDateResult;

import com.google.common.collect.ImmutableList;

public class ScrapedFilmInfobox {
    public ImmutableList<ListItemResult> name;
    public ImmutableList<ListItemResult> directors;
    public ImmutableList<ListItemResult> producers;
    public ImmutableList<ListItemResult> writers;
    public ImmutableList<ListItemResult> screenplayWriters;
    public ImmutableList<ListItemResult> storyWriters;
    public ImmutableList<ListItemResult> narrators;
    public ImmutableList<ListItemResult> starring;
    public ImmutableList<ListItemResult> composers;
    public ImmutableList<ListItemResult> cinematographers;
    public ImmutableList<ListItemResult> editors;
    public ImmutableList<ListItemResult> productionStudios;
    public ImmutableList<ListItemResult> distributors;
    public ImmutableList<ReleaseDateResult> releaseDates;
    public Integer runtimeInMins;
    public ImmutableList<ListItemResult> countries;
    public ImmutableList<ListItemResult> language;
    public Map<String,String> externalAliases = new TreeMap<>();
}