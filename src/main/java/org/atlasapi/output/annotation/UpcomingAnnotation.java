package org.atlasapi.output.annotation;

import static com.metabroadcast.common.base.MorePredicates.transformingPredicate;

import java.io.IOException;

import org.atlasapi.media.content.Container;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.output.writers.ChildRefWriter;
import org.atlasapi.persistence.output.UpcomingChildrenResolver;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class UpcomingAnnotation extends OutputAnnotation<Content> {

    private final UpcomingChildrenResolver upcomingChildrenResolver;

    public UpcomingAnnotation(UpcomingChildrenResolver childrenResolver) {
        super(Content.class);
        this.upcomingChildrenResolver = childrenResolver;
    }

    @Override
    public void write(Content content, FieldWriter writer, OutputContext ctxt) throws IOException {
        if (content instanceof Container) {
            Container container = (Container) content;
            writer.writeList(new ChildRefWriter("upcoming"), Iterables.filter(container.getChildRefs(), upcomingFilter(container)), ctxt);
        }
    }

    private Predicate<ChildRef> upcomingFilter(Container container) {
        return asChildRefFilter(upcomingChildrenResolver.availableChildrenFor(container));
    }
    
    private Predicate<ChildRef> asChildRefFilter(Iterable<String> uris) {
        return transformingPredicate(ChildRef.TO_URI, Predicates.in(ImmutableSet.copyOf(uris)));
    }
    
}
