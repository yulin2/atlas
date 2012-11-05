package org.atlasapi.equiv.generators;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.DefaultScoredCandidates;
import org.atlasapi.equiv.results.scores.DefaultScoredCandidates.Builder;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.entity.Content;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public final class ContentTitleScorer<T extends Content> {

    public ScoredCandidates<T> scoreCandidates(T content, Iterable<? extends T> candidates, ResultDescription desc) {
        Builder<T> equivalents = DefaultScoredCandidates.fromSource("Title");
        desc.appendText("Scoring %s candidates", Iterables.size(candidates));
        
        for (T found : ImmutableSet.copyOf(candidates)) {
            Score score = score(content, found);
            desc.appendText("%s (%s) scored %s", found.getTitle(), found.getCanonicalUri(), score);
            equivalents.addEquivalent(found, score);
        }

        return equivalents.build();
    }
    
    /**
     * Calculates a score representing the similarity of the candidate's title compared to the subject's title.
     * @param subject - subject content
     * @param candidate - candidate content
     * @return score representing how closely candidate's title matches subject's title.
     */
    private Score score(T subject, T candidate) {
        return Score.valueOf(score(sanitize(subject.getTitle()), sanitize(candidate.getTitle())));
    }
    
    private String sanitize(String title) {
        return removeCommonPrefixes(title.replaceAll(" & ", " and ").replaceAll("[^\\d\\w\\s]", "").toLowerCase());
    }
    
    private String removeCommonPrefixes(String title) {
        return (title.startsWith("the ") ? title.substring(4) : title).replace(" ", "");
    }
    
    private double score(String subjectTitle, String candidateTitle) {
        return subjectTitle.length() < candidateTitle.length() ? scoreTitles(subjectTitle, candidateTitle) : scoreTitles(candidateTitle, subjectTitle);
    }

    private double scoreTitles(String shorter, String longer) {
        double commonPrefix = commonPrefixLength(shorter, longer);
        double difference = longer.length() - commonPrefix;
        return 1.0 / (Math.exp(Math.pow(difference, 2)) + 8*difference);
    }

    private double commonPrefixLength(String t1, String t2) {
        int i = 0;
        for (; i < Math.min(t1.length(), t2.length()) && t1.charAt(i) == t2.charAt(i); i++) {
        }
        return i;
    }
}
