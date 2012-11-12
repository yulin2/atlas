package org.atlasapi.equiv.generators;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.media.entity.Content;

import com.google.common.base.Function;

public class ContentTitleScorer {
    
    private Function<String, String> titleTransform;

    public ContentTitleScorer(Function<String, String> titleTransform) {
        this.titleTransform = titleTransform;
    }

    /**
     * Calculates a score representing the similarity of the candidate's title compared to the subject's title.
     * @param subject - subject content
     * @param candidate - candidate content
     * @return score representing how closely candidate's title matches subject's title.
     */
    public Score score(Content subject, Content candidate, ResultDescription desc) {
        String subjectTitle = sanitize(subject.getTitle());
        String contentTitle = sanitize(candidate.getTitle());
        double score = score(subjectTitle, contentTitle);
        desc.appendText("%s vs. %s (%s): %s", subjectTitle, contentTitle, candidate.getCanonicalUri(), score);
        return Score.valueOf(score);
    }
    
    private String sanitize(String title) {
        return removeCommonPrefixes(titleTransform.apply(title)
            .replaceAll(" & ", " and ")
            .replaceAll("[^\\d\\w\\s]", "").toLowerCase());
    }
    
    private String removeCommonPrefixes(String title) {
        return (title.startsWith("the ") ? title.substring(4) : title).replace(" ", "");
    }
    
    private double score(String subjectTitle, String candidateTitle) {
        if (subjectTitle.length() < candidateTitle.length()) {
            return scoreTitles(subjectTitle, candidateTitle);
        } else {
            return scoreTitles(candidateTitle, subjectTitle);
        }
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
