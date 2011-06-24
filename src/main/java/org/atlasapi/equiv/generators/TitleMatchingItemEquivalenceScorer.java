package org.atlasapi.equiv.generators;

import java.util.Set;
import java.util.regex.Pattern;

import org.atlasapi.equiv.results.DefaultScoredEquivalents;
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
            for (TitleType type : ImmutableList.copyOf(TitleType.values())) {
                if(type.matches(item.getTitle())) {
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


    private double score(Item subject, Item suggestion) {
        TitleType subjectType = TitleType.titleTypeOf(subject);
        TitleType suggestionType = TitleType.titleTypeOf(suggestion);
        
        String subjTitle = subject.getTitle().replaceAll("[^A-Za-z0-9]", "");
        String suggTitle = suggestion.getTitle().replaceAll("[^A-Za-z0-9]", "");
        
        if(subjectType == suggestionType && Objects.equal(subjTitle, suggTitle)) {
            return 1;
        }
        
        return 0;
    }
}
