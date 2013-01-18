package org.atlasapi.output.simple;

import static com.metabroadcast.common.base.MorePredicates.transformingPredicate;

import java.util.Set;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Container;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.EntityType;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.SeriesRef;
import org.atlasapi.media.entity.simple.ContentIdentifier;
import org.atlasapi.media.entity.simple.Playlist;
import org.atlasapi.media.entity.simple.ContentIdentifier.SeriesIdentifier;
import org.atlasapi.media.product.ProductResolver;
import org.atlasapi.media.util.Identifiables;
import org.atlasapi.persistence.media.product.ProductResolver;
import org.atlasapi.output.Annotation;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.media.product.ProductResolver;
import org.atlasapi.persistence.output.AvailableChildrenResolver;
import org.atlasapi.persistence.output.RecentlyBroadcastChildrenResolver;
import org.atlasapi.persistence.output.UpcomingChildrenResolver;
import org.atlasapi.persistence.topic.TopicQueryResolver;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ContainerModelSimplifier extends ContentModelSimplifier<Container, Playlist> {

    private final ModelSimplifier<Item, org.atlasapi.media.entity.simple.Item> itemSimplifier;
    private final AvailableChildrenResolver availableChildrenResolver;
    private final UpcomingChildrenResolver upcomingChildrenResolver;
    private final RecentlyBroadcastChildrenResolver recentlyBroadcastResolver;
    private final Function<ChildRef, ContentIdentifier> toContentIdentifier = new Function<ChildRef, ContentIdentifier>() {

        @Override
        public ContentIdentifier apply(ChildRef input) {
            return ContentIdentifier.identifierFor(input, idCodec);
        }
    };
    
    private final Function<SeriesRef, SeriesIdentifier> toSeriesIdentifier = new Function<SeriesRef, SeriesIdentifier>() {

        @Override
        public SeriesIdentifier apply(SeriesRef input) {
            return ContentIdentifier.seriesIdentifierFor(input, idCodec);
        }
    };

    public ContainerModelSimplifier(ModelSimplifier<Item, org.atlasapi.media.entity.simple.Item> itemSimplifier, String localHostName, ContentGroupResolver contentGroupResolver, TopicQueryResolver topicResolver, AvailableChildrenResolver availableChildren, UpcomingChildrenResolver upcomingChildren, ProductResolver productResolver, RecentlyBroadcastChildrenResolver recentChildren) {
        super(localHostName, contentGroupResolver, topicResolver, productResolver);
        this.itemSimplifier = itemSimplifier;
        this.availableChildrenResolver = availableChildren;
        this.upcomingChildrenResolver = upcomingChildren;
        this.recentlyBroadcastResolver = recentChildren;
    }

    @Override
    public Playlist simplify(Container fullPlayList, Set<Annotation> annotations, ApplicationConfiguration config) {

        Playlist simplePlaylist = new Playlist();

        copyBasicContentAttributes(fullPlayList, simplePlaylist, annotations, config);
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
        
        if (annotations.contains(Annotation.SERIES)) {
            if (fullPlayList instanceof Brand) {
                Brand brand = (Brand) fullPlayList;
                simplePlaylist.setSeriesList(Lists.transform(brand.getSeriesRefs(), toSeriesIdentifier));
            }
        }

        if (annotations.contains(Annotation.AVAILABLE_LOCATIONS)) {
            simplePlaylist.setAvailableContent(filterAndTransformChildRefs(fullPlayList, availableFilter(fullPlayList)));
        }

        if (annotations.contains(Annotation.UPCOMING)) {
            simplePlaylist.setUpcomingContent(filterAndTransformChildRefs(fullPlayList, upcomingFilter(fullPlayList)));;
        }
        
        if (annotations.contains(Annotation.RECENTLY_BROADCAST)) {
            simplePlaylist.setRecentContent(filterAndTransformChildRefs(fullPlayList, recentlyBroadcastFilter(fullPlayList)));
        }

        return simplePlaylist;
    }

    private Iterable<ContentIdentifier> filterAndTransformChildRefs(Container fullPlayList, Predicate<ChildRef> filter) {
        return Iterables.transform(Iterables.filter(fullPlayList.getChildRefs(), filter), toContentIdentifier);
    }

    private Predicate<ChildRef> availableFilter(Container fullPlayList) {
        return asChildRefFilter(availableChildrenResolver.availableChildrenFor(fullPlayList));
    }

    private Predicate<ChildRef> upcomingFilter(Container fullPlayList) {
        return asChildRefFilter(upcomingChildrenResolver.availableChildrenFor(fullPlayList));
    }

    private Predicate<ChildRef> recentlyBroadcastFilter(Container fullPlayList) {
        return asChildRefFilter(recentlyBroadcastResolver.recentlyBroadcastChildrenFor(fullPlayList, 3));
    }
    
    private Predicate<ChildRef> asChildRefFilter(Iterable<Id> childRefId) {
        return transformingPredicate(Identifiables.toId(), Predicates.in(ImmutableSet.copyOf(childRefId)));
    }

    @Override
    protected org.atlasapi.media.entity.simple.Item simplify(org.atlasapi.media.entity.Item item, Set<Annotation> annotations, ApplicationConfiguration config) {
        return itemSimplifier.simplify(item, annotations, config);
    }
}
