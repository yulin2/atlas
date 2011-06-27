package org.atlasapi.equiv.generators;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.equiv.results.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.Score;
import org.atlasapi.equiv.results.DefaultScoredEquivalents.ScoredEquivalentsBuilder;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.media.entity.Item;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class TitleMatchingItemEquivalenceScorer implements ContentEquivalenceGenerator<Item> {
    
    public enum TitleType {
        
        DATE("\\d{1,2}/\\d{1,2}/(\\d{2}|\\d{4})"),
        SEQUENCE("((?:E|e)pisode)(?:.*)(\\d+)"),
        DEFAULT(".*");
     
        private Pattern pattern;

        TitleType(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }
        
        public static TitleType titleTypeOf(Item item) {
            return titleTypeOf(item.getTitle());
        }
        
        public static TitleType titleTypeOf(String title) {
            for (TitleType type : ImmutableList.copyOf(TitleType.values())) {
                if(type.matches(title)) {
                    return type;
                }
            }
            return DEFAULT;
        }


        private boolean matches(String title) {
            return pattern.matcher(title).matches();
        }
        
    }
    
    
    @Override
    public ScoredEquivalents<Item> generateEquivalences(Item subject, Set<Item> suggestions) {
        ScoredEquivalentsBuilder<Item> equivalents = DefaultScoredEquivalents.fromSource("Title");
        
        for (Item suggestion : Iterables.filter(suggestions, Item.class)) {
            equivalents.addEquivalent(suggestion, score(subject, suggestion));
        }
        
        return equivalents.build();
    }


    private Score score(Item subject, Item suggestion) {
        
        String subjTitle = removeSeq(subject.getTitle());
        String suggTitle = removeSeq(suggestion.getTitle());
        
        TitleType subjectType = TitleType.titleTypeOf(subject.getTitle());
        TitleType suggestionType = TitleType.titleTypeOf(suggestion.getTitle());
        
        subjTitle = subjTitle.replaceAll("[^A-Za-z0-9]+", "-");
        suggTitle = suggTitle.replaceAll("[^A-Za-z0-9]+", "-");
        
        if(subjectType == suggestionType && Objects.equal(subjTitle, suggTitle)) {
            return Score.valueOf(1.0);
        }
        
        return Score.NULL_SCORE;
    }

    private final Pattern seqTitle = Pattern.compile("(\\d+)(\\s?[.:-]{1}\\s?)(.*)");
    
    private String removeSeq(String title) {
        Matcher matcher = seqTitle.matcher(title);
        if(matcher.matches()) {
            return matcher.group(3);
        }
        return title;
    }
}
