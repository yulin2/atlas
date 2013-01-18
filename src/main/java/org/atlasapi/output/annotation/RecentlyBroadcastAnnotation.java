package org.atlasapi.output.annotation;

import static com.metabroadcast.common.base.MorePredicates.transformingPredicate;

import java.io.IOException;

import org.atlasapi.media.common.Id;
import org.atlasapi.media.common.Identifiable;
import org.atlasapi.media.content.Container;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.util.Identifiables;
import org.atlasapi.output.writers.ChildRefWriter;
import org.atlasapi.persistence.output.RecentlyBroadcastChildrenResolver;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class RecentlyBroadcastAnnotation extends OutputAnnotation<Content> {

    private final RecentlyBroadcastChildrenResolver recentlyBroadcastResolver;

    public RecentlyBroadcastAnnotation(RecentlyBroadcastChildrenResolver recentlyBroadcastResolver) {
        super(Content.class);
        this.recentlyBroadcastResolver = recentlyBroadcastResolver;
    }

    @Override
    public void write(Content content, FieldWriter writer, OutputContext ctxt) throws IOException {
        if (content instanceof Container) {
            Container container = (Container) content;
            writer.writeList(new ChildRefWriter("recent_content"), Iterables.filter(container.getChildRefs(), recentlyBroadcastFilter(container)), ctxt);
        }
    }

    private Predicate<Identifiable> recentlyBroadcastFilter(Container container) {
        Iterable<Id> ids = recentlyBroadcastResolver.recentlyBroadcastChildrenFor(container, 3);
        return asChildRefFilter(ids);
    }
    
    private Predicate<Identifiable> asChildRefFilter(Iterable<Id> childRefIds) {
        return transformingPredicate(Identifiables.toId(), Predicates.in(ImmutableSet.copyOf(childRefIds)));
    }
    
}
