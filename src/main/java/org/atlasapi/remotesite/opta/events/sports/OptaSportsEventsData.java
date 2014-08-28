package org.atlasapi.remotesite.opta.events.sports;

import java.util.List;

import org.atlasapi.remotesite.opta.events.OptaEventsData;
import org.atlasapi.remotesite.opta.events.sports.model.OptaFixture;
import org.atlasapi.remotesite.opta.events.sports.model.OptaSportsTeam;

import com.google.common.collect.ImmutableList;


public class OptaSportsEventsData implements OptaEventsData<OptaSportsTeam, OptaFixture> {

    private final List<OptaFixture> fixtures;
    private final List<OptaSportsTeam> teams;
    
    public OptaSportsEventsData(Iterable<OptaFixture> fixtures, Iterable<OptaSportsTeam> teams) {
        this.fixtures = ImmutableList.copyOf(fixtures);
        this.teams = ImmutableList.copyOf(teams);
    }
    
    public Iterable<OptaFixture> matches() {
        return fixtures;
    }
    
    public Iterable<OptaSportsTeam> teams() {
        return teams;
    }
}
