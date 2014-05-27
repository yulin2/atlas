package org.atlasapi.remotesite.pa.features;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.pa.PaHelper;
import org.atlasapi.remotesite.pa.features.PaFeaturesContentGroupProcessor.FeatureSetContentGroups;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class PaFeaturesProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(PaFeaturesProcessor.class);
    private static final Ordering<Broadcast> BY_BROADCAST_DATE = Ordering.natural().onResultOf(Broadcast.TO_TRANSMISSION_TIME);
    
    private final ContentResolver contentResolver;

    private Interval upcomingPickInterval;
    
    public PaFeaturesProcessor(ContentResolver contentResolver) {
        this.contentResolver = checkNotNull(contentResolver);
    }

    public void prepareUpdate(Interval upcomingPickInterval) {
        this.upcomingPickInterval = upcomingPickInterval;
    }
    
    public void process(String programmeId, FeatureSetContentGroups contentGroups) {
        Set<String> candidateUris = ImmutableSet.of(PaHelper.getFilmUri(programmeId), 
                PaHelper.getEpisodeUri(programmeId), PaHelper.getAlias(programmeId));
        log.trace("Looking up URIs {}", candidateUris);
        
        Map<String, Identified> resolvedContent = contentResolver.findByUris(candidateUris).asResolvedMap();
        List<Identified> resolved = Lists.newArrayList(resolvedContent.values());
        
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
            contentGroups.getTodayContentGroup().addContent(item.childRef()); 
        }
        contentGroups.getAllFeaturedContentContentGroup().addContent(item.childRef());
    }
}
