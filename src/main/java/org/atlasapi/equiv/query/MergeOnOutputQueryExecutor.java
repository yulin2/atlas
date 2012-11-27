package org.atlasapi.equiv.query;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.equiv.OutputContentMerger;
import org.atlasapi.equiv.SeriesOrder;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Container;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Certificate;
import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.KeyPhrase;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.ReleaseDate;
import org.atlasapi.media.entity.Subtitles;
import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

public class MergeOnOutputQueryExecutor implements KnownTypeQueryExecutor {

    private static final Ordering<Episode> SERIES_ORDER = Ordering.from(new SeriesOrder());
    private final KnownTypeQueryExecutor delegate;
    
    private final OutputContentMerger merger = new OutputContentMerger();

    public MergeOnOutputQueryExecutor(KnownTypeQueryExecutor delegate) {
        this.delegate = delegate;
    }

    @Override
    public Map<Id, List<Identified>> executeUriQuery(Iterable<String> uris, final ContentQuery query) {
        return mergeResults(query, delegate.executeUriQuery(uris, query));
    }

    @Override
    public Map<Id, List<Identified>> executeIdQuery(Iterable<Id> ids, final ContentQuery query) {
        return mergeResults(query, delegate.executeIdQuery(ids, query));
    }

    @Override
    public Map<String, List<Identified>> executeAliasQuery(Optional<String> namespace, Iterable<String> values,
            ContentQuery query) {
        return delegate.executeAliasQuery(namespace, values, query);
    }

    private Map<Id, List<Identified>> mergeResults(final ContentQuery query, Map<Id, List<Identified>> unmergedResult) {
        final ApplicationConfiguration config = query.getConfiguration();
        if (!config.precedenceEnabled()) {
            return unmergedResult;
        }
        return Maps.transformValues(unmergedResult, new Function<List<Identified>, List<Identified>>() {

            @Override
            public List<Identified> apply(List<Identified> input) {

                List<Content> content = Lists.newArrayList();
                List<Identified> ids = Lists.newArrayList();

                for (Identified ided : input) {
                    if (ided instanceof Content) {
                        content.add((Content) ided);
                    } else {
                        ids.add(ided);
                    }
                }

                return ImmutableList.copyOf(Iterables.concat(merger.merge(config, content), ids));
            }
        });
    }
    
}
