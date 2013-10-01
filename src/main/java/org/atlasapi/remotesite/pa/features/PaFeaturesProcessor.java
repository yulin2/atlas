package org.atlasapi.remotesite.pa.features;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.output.Annotation;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.EquivalentContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.remotesite.pa.PaHelper;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class PaFeaturesProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(PaFeaturesProcessor.class);
    
    private static final String TODAY_CONTENT_GROUP_URI = "http://pressassocation.com/features/tvpicks";
    private static final String ALL_CONTENT_GROUP_URI = "http://pressassocation.com/features/tvpicks/all";
    private static final Ordering<Broadcast> BY_BROADCAST_DATE = Ordering.natural().onResultOf(Broadcast.TO_TRANSMISSION_TIME);
    
    private final KnownTypeQueryExecutor queryExecutor;
    private final ContentGroupWriter contentGroupWriter;
    private final ContentGroupResolver contentGroupResolver;

    private Interval upcomingPickInterval;
    private ContentGroup todayContentGroup;
    private ContentGroup allFeaturedContentEverContentGroup;
    
    public PaFeaturesProcessor(KnownTypeQueryExecutor queryExecutor, ContentGroupResolver contentGroupResolver, ContentGroupWriter contentGroupWriter) {
        this.queryExecutor = queryExecutor;
        this.contentGroupWriter = contentGroupWriter;
        this.contentGroupResolver = contentGroupResolver;
    }

    public void prepareUpdate(Interval upcomingPickInterval) {
        this.upcomingPickInterval = upcomingPickInterval;
        this.todayContentGroup = getOrCreateContentGroup(TODAY_CONTENT_GROUP_URI);
        this.allFeaturedContentEverContentGroup = getOrCreateContentGroup(ALL_CONTENT_GROUP_URI);
    }
    
    private ContentGroup getOrCreateContentGroup(String uri) {
        ResolvedContent resolvedContent = contentGroupResolver.findByCanonicalUris(ImmutableList.of(uri));
        if (resolvedContent.get(uri).hasValue()) {
            ContentGroup contentGroup = (ContentGroup) resolvedContent.get(uri).requireValue();
            contentGroup.setContents(ImmutableList.<ChildRef>of());
            return contentGroup;
        } else {
            return new ContentGroup(uri, Publisher.PA_FEATURES);
        }
    }
    
    public void process(String programmeId) {
        Set<String> candidateUris = ImmutableSet.of(PaHelper.getFilmUri(programmeId), 
                PaHelper.getEpisodeUri(programmeId), PaHelper.getAlias(programmeId));
        log.trace("Looking up URIs {}", candidateUris);
        List<Identified> resolved = Lists.newArrayList(Iterables.concat(queryExecutor.executeUriQuery(candidateUris, ContentQuery.MATCHES_EVERYTHING).values()));
        log.trace("Resolved {}", Iterables.transform(resolved, Identified.TO_URI));
        
        Collections.sort(resolved, new PaIdentifiedComparator());
        Item item = (Item) Iterables.getFirst(resolved, null);
        
        if (item == null) {
            log.error("Could not resolve item " + programmeId);
            return;
        }
        
        log.trace("Resolved and chose item {}", item.getCanonicalUri());
        
        Broadcast broadcast = BY_BROADCAST_DATE.min(Iterables.concat(Iterables.transform(item.getVersions(), Version.TO_BROADCASTS)));
        if (upcomingPickInterval.contains(broadcast.getTransmissionTime())) {
            todayContentGroup.addContent(item.childRef()); 
        }
        allFeaturedContentEverContentGroup.addContent(item.childRef());
    }
    
    public void finishUpdate() {
        contentGroupWriter.createOrUpdate(todayContentGroup);
        contentGroupWriter.createOrUpdate(allFeaturedContentEverContentGroup);
    }
    
}
