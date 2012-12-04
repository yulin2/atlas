package org.atlasapi.output.annotation;

import static com.metabroadcast.common.base.MorePredicates.transformingPredicate;

import java.io.IOException;

import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
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

    private Predicate<ChildRef> recentlyBroadcastFilter(Container container) {
        return asChildRefFilter(recentlyBroadcastResolver.recentlyBroadcastChildrenFor(container, 3));
    }
    
    private Predicate<ChildRef> asChildRefFilter(Iterable<String> childRefUris) {
        return transformingPredicate(ChildRef.TO_URI, Predicates.in(ImmutableSet.copyOf(childRefUris)));
    }
    
}
