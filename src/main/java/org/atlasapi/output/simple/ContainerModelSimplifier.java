package org.atlasapi.output.simple;

import java.util.Set;

import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.EntityType;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.simple.ContentIdentifier;
import org.atlasapi.media.entity.simple.Item;
import org.atlasapi.media.entity.simple.Playlist;
import org.atlasapi.output.Annotation;
import org.atlasapi.persistence.output.AvailableChildrenResolver;
import org.atlasapi.persistence.output.UpcomingChildrenResolver;
import org.atlasapi.persistence.topic.TopicQueryResolver;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ContainerModelSimplifier extends ContentModelSimplifier<Container, Playlist> {

    private final ItemModelSimplifier itemSimplifier;
    private final AvailableChildrenResolver availableChildrenResolver;
    private final UpcomingChildrenResolver upcomingChildrenResolver;
    private final Function<ChildRef, ContentIdentifier> toContentIdentifier = new Function<ChildRef, ContentIdentifier>() {
        @Override
        public ContentIdentifier apply(ChildRef input) {
            return ContentIdentifier.identifierFor(input);
        }
    };

    public ContainerModelSimplifier(ItemModelSimplifier itemSimplifier, TopicQueryResolver topicResolver, AvailableChildrenResolver availableChildren, UpcomingChildrenResolver upcomingChildren) {
        super(topicResolver);
        this.availableChildrenResolver = availableChildren;
        this.upcomingChildrenResolver = upcomingChildren;
        this.itemSimplifier = itemSimplifier;
    }
    
    @Override
    public Playlist simplify(Container fullPlayList, Set<Annotation> annotations) {

        Playlist simplePlaylist = new Playlist();

        copyBasicContentAttributes(fullPlayList, simplePlaylist, annotations);
        simplePlaylist.setType(EntityType.from(fullPlayList).toString());

        if (annotations.contains(Annotation.EXTENDED_DESCRIPTION)) {
            if (fullPlayList instanceof Series) {
                Series series = (Series) fullPlayList;
                simplePlaylist.setSeriesNumber(series.getSeriesNumber());
                simplePlaylist.setTotalEpisodes(series.getTotalEpisodes());
            }
        }

        if (annotations.contains(Annotation.SUB_ITEMS)) {
            simplePlaylist.setContent(Lists.transform(fullPlayList.getChildRefs(), toContentIdentifier));
        }
        
        if(annotations.contains(Annotation.AVAILABLE_LOCATIONS)) {
            simplePlaylist.setAvailableContent(Iterables.transform(Iterables.filter(fullPlayList.getChildRefs(), availableFilter(fullPlayList)), toContentIdentifier));
        }

        if(annotations.contains(Annotation.UPCOMING)) {
            simplePlaylist.setUpcomingContent(Iterables.transform(Iterables.filter(fullPlayList.getChildRefs(), upcomingFilter(fullPlayList)), toContentIdentifier));;
        }

        return simplePlaylist;
    }

    private Predicate<ChildRef> availableFilter(Container fullPlayList) {
        final ImmutableSet<String> availableChildren = ImmutableSet.copyOf(availableChildrenResolver.availableChildrenFor(fullPlayList));
        return new Predicate<ChildRef>() {
            @Override
            public boolean apply(ChildRef input) {
                return availableChildren.contains(input.getUri());
            }
        };
    }

    private Predicate<ChildRef> upcomingFilter(Container fullPlayList) {
        final ImmutableSet<String> availableChildren = ImmutableSet.copyOf(upcomingChildrenResolver.availableChildrenFor(fullPlayList));
        return new Predicate<ChildRef>() {
            @Override
            public boolean apply(ChildRef input) {
                return availableChildren.contains(input.getUri());
            }
        };
    }

    @Override
    protected Item simplify(org.atlasapi.media.entity.Item item, Set<Annotation> annotations) {
        return itemSimplifier.simplify(item, annotations);
    }

}
