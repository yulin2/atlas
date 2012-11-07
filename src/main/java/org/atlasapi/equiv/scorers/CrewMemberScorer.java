package org.atlasapi.equiv.scorers;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents.ScoredEquivalentsBuilder;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.CrewMember.Role;
import org.atlasapi.media.entity.Item;

import com.google.common.base.Objects;

public class CrewMemberScorer implements ContentEquivalenceScorer<Item> {

    @Override
    public ScoredEquivalents<Item> score(Item content, Iterable<Item> candidates, ResultDescription desc) {
        ScoredEquivalentsBuilder<Item> scored = DefaultScoredEquivalents.fromSource("crew");

        List<CrewMember> contentCrew = content.getPeople();
        
        if (nullOrEmpty(contentCrew)) {
            desc.appendText("Subject has no crew");
            for (Item candidate : candidates) {
                scored.addEquivalent(candidate, Score.NULL_SCORE);
            }
            return scored.build();
        }
        
        for (Item candidate : candidates) {
            List<CrewMember> candidateCrew = candidate.getPeople();
            Score score;
            if (nullOrEmpty(candidateCrew)) {
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
        int found = 0;
        int missed= 0;
        for (CrewMember needle : needles) {
            if (needle.name() != null) {
                CrewMember match = needleInHaystack(needle, haystack);
                if (match != null) {
                    desc.appendText(describeMatch(needle, match));
                    found++;
                } else {
                    desc.appendText("no match: %s (%s)", needle.getCanonicalUri(), nameAndRole(needle));
                    missed++;
                }
            } else {
                desc.appendText("no name: %s", needle.getCanonicalUri());
            }
        }
        
        desc.appendText("score: (%s - %s)/%s", found, missed, needles.size());
        return Score.valueOf((found-missed)/(double)needles.size());
    }

    private CrewMember needleInHaystack(CrewMember needle, List<CrewMember> haystack) {
        for (CrewMember hay : haystack) {
            if (Objects.equal(normalize(needle.name()), normalize(hay.name()))
             && Objects.equal(needle.role(), hay.role())) {
                return hay;
            }
        }
        return null;
    }
    
    private Object normalize(String name) {
        return name.toLowerCase().replace("[^\\d\\w]","");
    }

    protected String describeMatch(CrewMember needle, CrewMember match) {
        return String.format("%s (%s) matched %s (%s)", 
            needle.getCanonicalUri(), nameAndRole(needle),
            match.getCanonicalUri(), nameAndRole(match)
                );
    }
    
    private String nameAndRole(CrewMember member) {
        Role role = member.role();
        String roleKey = role == null ? "" : "," + role.key();
        return String.format("%s%s", member.name(), roleKey);
    }

    private boolean nullOrEmpty(List<CrewMember> people) {
        return people == null || people.isEmpty();
    }

    @Override
    public String toString() {
        return "Crew Member";
    }
    
}
