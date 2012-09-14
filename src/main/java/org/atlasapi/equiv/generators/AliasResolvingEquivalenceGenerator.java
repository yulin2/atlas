package org.atlasapi.equiv.generators;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents.ScoredEquivalentsBuilder;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;

import com.google.common.collect.Iterables;

public class AliasResolvingEquivalenceGenerator<T extends Content> implements EquivalenceGenerator<T> {

    public static final <T extends Content> EquivalenceGenerator<T> aliasResolvingGenerator(ContentResolver resolver, Class<T> cls) {
        return new AliasResolvingEquivalenceGenerator<T>(resolver,cls);
    }
    
    private final ContentResolver resolver;
    private final Class<T> cls;

    public AliasResolvingEquivalenceGenerator(ContentResolver resolver, Class<T> cls) {
        this.resolver = resolver;
        this.cls = cls;
    }

    @Override
    public ScoredCandidates<T> generate(T content, ResultDescription desc) {
        ScoredEquivalentsBuilder<T> equivalents = DefaultScoredEquivalents.fromSource("Alias");
        desc.startStage("Resolving aliases:");
        for (String alias : content.getAliases()) {
            desc.appendText(alias);
        }
        desc.finishStage();
        
       ResolvedContent resolved = resolver.findByCanonicalUris(content.getAliases());
       
       for (T identified : Iterables.filter(resolved.getAllResolvedResults(), cls)) {
               equivalents.addEquivalent(identified, Score.ONE);
               desc.appendText("Resolved %s", identified.getCanonicalUri());
       }
       desc.appendText("Missed %s", resolved.getUnresolved().size());
        
        return equivalents.build();
    }
    
    @Override
    public String toString() {
        return "Alias Resolving Generator";
    }
}
