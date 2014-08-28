package org.atlasapi.remotesite.opta.events.soccer;

import java.util.List;

import org.atlasapi.remotesite.opta.events.OptaEventsData;
import org.atlasapi.remotesite.opta.events.soccer.model.SoccerMatchData;
import org.atlasapi.remotesite.opta.events.soccer.model.SoccerTeam;

import com.google.common.collect.ImmutableList;


public class OptaSoccerEventsData implements OptaEventsData<SoccerTeam, SoccerMatchData> {

    private final List<SoccerMatchData> matches;
    private final List<SoccerTeam> teams;
    
    public OptaSoccerEventsData(Iterable<SoccerMatchData> matches, Iterable<SoccerTeam> teams) {
        this.matches = ImmutableList.copyOf(matches);
        this.teams = ImmutableList.copyOf(teams);
    }

    @Override
    public Iterable<SoccerTeam> teams() {
        return teams;
    }

    @Override
    public Iterable<SoccerMatchData> matches() {
        return matches;
    }
}
