package org.atlasapi.equiv.scorers;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents.ScoredEquivalentsBuilder;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Item;

import com.google.common.base.Objects;

public class CrewMemberScorer implements ContentEquivalenceScorer<Item> {

    @Override
    public ScoredEquivalents<Item> score(Item content, Iterable<Item> candidates, ResultDescription desc) {
        ScoredEquivalentsBuilder<Item> scored = DefaultScoredEquivalents.fromSource("crew");

        List<CrewMember> contentCrew = content.getPeople();
        for (Item candidate : candidates) {
            List<CrewMember> candidateCrew = candidate.getPeople();
            Score score;
            if (nullOrEmpty(contentCrew) || nullOrEmpty(candidateCrew)) {
                score = Score.NULL_SCORE;
            } else {
                score = score(contentCrew, candidateCrew);
            }
            scored.addEquivalent(candidate, score);
        }
        return scored.build();
    }

    private Score score(List<CrewMember> contentCrew, List<CrewMember> candidateCrew) {
        //consistently choose needles and haystack so the scoring will be symmetric.
        int contentSize = contentCrew.size();
        int candidateSize = candidateCrew.size();
        List<CrewMember> needles = contentSize < candidateSize ? contentCrew : candidateCrew;
        List<CrewMember> haystack = contentSize < candidateSize ? candidateCrew : contentCrew;

        /*
         * This is obviously contentSize * candidateSize. Since there usually
         * aren't many contributors this shouldn't be a problem. If it is then
         * we can sort by name and go from there, assuming matching is binary.
         * 
         * This could be the sum of the best score for each needle in the
         * haystack allowing in-exact matches between people. Again n^2
         */
        double found = 0;
        for (CrewMember needle : needles) {
            if (needle.name() != null && needleInHaystack(needle, haystack)) {
                found++;
            }
        }
        
        return Score.valueOf(found/needles.size());
    }

    private boolean needleInHaystack(CrewMember needle, List<CrewMember> haystack) {
        for (CrewMember hay : haystack) {
            if (Objects.equal(needle.name(), hay.name())
             && Objects.equal(needle.role(), hay.role())) {
                return true;
            }
        }
        return false;
    }

    private boolean nullOrEmpty(List<CrewMember> people) {
        return people == null || people.isEmpty();
    }

}
