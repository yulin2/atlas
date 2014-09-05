package org.atlasapi.remotesite.events;

import java.util.Map;
import java.util.Set;

import org.joda.time.Duration;

import com.google.common.base.Optional;


public interface EventsFieldMapper<S> {

    /**
     * Where an end time has not been provided for an {@link org.atlasapi.media.entity.Event},
     * a duration can be mapped per sport and used to estimate the end time. 
     * @return the mapped duration
     */
    Duration fetchDuration(S sport);
    
    /**
     * Looks up a location url, generally of the form http://dbpedia.org/resources/[some location].
     * If no mapping exists, returns Optional.absent()
     * @param location
     */
    Optional<String> fetchLocationUrl(String location);

    /**
     * For each sport, there may be location values attached to events that we don't want to ingest,
     * such as test events or elements that are not actually sporting events (elements for highlights
     * programmes, for example) 
     */
    Set<String> fetchIgnoredLocations(S sport);

    /**
     * For each sport, there may be team names that we don't want to ingest, such as test elements 
     * (team names such as 'TBC', for example) 
     */
    Set<String> fetchIgnoredTeams();
    
    /**
     * For a given sport, looks up a set of associated DBpedia Topic value Strings
     * and returns them.
     * <p>
     * For example, Rugby might be associated with the urls for Rugby League and 
     * Rugby Football.
     */
    Map<String, String> fetchEventGroupUrls(S sport);
}
