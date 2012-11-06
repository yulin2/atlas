package org.atlasapi.equiv.scorers;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.DefaultScoredCandidates;
import org.atlasapi.equiv.results.scores.DefaultScoredCandidates.Builder;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Item;

import com.google.common.base.Objects;

public class CrewMemberScorer implements EquivalenceScorer<Item> {

    @Override
    public ScoredCandidates<Item> score(Item content, Iterable<Item> candidates, ResultDescription desc) {
        Builder<Item> scored = DefaultScoredCandidates.fromSource("crew");

        List<CrewMember> contentCrew = content.getPeople();
        
        for (Item candidate : candidates) {
            List<CrewMember> candidateCrew = candidate.getPeople();
            Score score;
            if (nullOrEmpty(contentCrew)) {
                desc.appendText("Subject has no crew");
                score = Score.NULL_SCORE;
            } else if (nullOrEmpty(candidateCrew)) {
                desc.appendText("%s has no crew", candidate.getCanonicalUri());
                score = Score.NULL_SCORE;
            } else {
                desc.startStage(candidate.getCanonicalUri()+":");
                score = score(contentCrew, candidateCrew, desc);
                desc.finishStage();
            }
            scored.addEquivalent(candidate, score);
        }
        return scored.build();
    }

    private Score score(List<CrewMember> contentCrew, List<CrewMember> candidateCrew, ResultDescription desc) {
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
        double missed= 0;
        for (CrewMember needle : needles) {
            if (needle.name() != null) {
                CrewMember match = needleInHaystack(needle, haystack);
                if (match != null) {
                    desc.appendText("%s (%s, %s) matched %s (%s, %s)", 
                        needle.getCanonicalUri(), needle.name(),needle.role(),
                        match.getCanonicalUri(), match.name(), match.role()
                    );
                    found++;
                } else {
                    missed++;
                    desc.appendText("no match: %s (%s, %s)", needle.getCanonicalUri(), needle.name(),needle.role());
                }
            } else {
                desc.appendText("no name: %s", needle.getCanonicalUri());
            }
        }
        
        desc.appendText("score: (%s - %s)/%s", found, missed, needles.size());
        return Score.valueOf((found-missed)/needles.size());
    }

    private CrewMember needleInHaystack(CrewMember needle, List<CrewMember> haystack) {
        for (CrewMember hay : haystack) {
            if (Objects.equal(needle.name(), hay.name())
             && Objects.equal(needle.role(), hay.role())) {
                return hay;
            }
        }
        return null;
    }

    private boolean nullOrEmpty(List<CrewMember> people) {
        return people == null || people.isEmpty();
    }

    @Override
    public String toString() {
        return "Crew Member";
    }
    
}
