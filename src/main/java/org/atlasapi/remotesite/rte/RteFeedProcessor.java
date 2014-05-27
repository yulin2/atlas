package org.atlasapi.remotesite.rte;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.remotesite.ContentMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.scheduling.StatusReporter;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;


public class RteFeedProcessor {
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private final ContentWriter contentWriter;
    private final ContentResolver contentResolver;
    private final ContentMerger contentMerger;
    private final RteBrandExtractor brandExtractor;
    

    public RteFeedProcessor(ContentWriter contentWriter, ContentResolver contentResolver, ContentMerger contentMerger, RteBrandExtractor brandExtractor) {
        this.contentWriter = checkNotNull(contentWriter);
        this.contentResolver = checkNotNull(contentResolver);
        this.contentMerger = checkNotNull(contentMerger);
        this.brandExtractor = checkNotNull(brandExtractor);
    }

    public void process(Feed feed, StatusReporter statusReporter) {
        int processed = 0;
        int failed = 0;
        
        @SuppressWarnings("unchecked")
        List<Entry> entries = feed.getEntries();
        
        for (Entry entry: entries) {
            try {
                Brand extractedBrand = brandExtractor.extract(entry);
                contentWriter.createOrUpdate(resolveAndMerge(extractedBrand));
                processed++;
            } catch (Exception e) {
                log.error("Error while processing feed entry with id: " + entry.getId(), e);
                failed++;
            }
            statusReporter.reportStatus(statusMessage(processed, failed));
        }
    }

    private Container resolveAndMerge(Brand extractedBrand) {
        Maybe<Identified> resolvedBrand = contentResolver.findByCanonicalUris(canonicalUrisFor(extractedBrand))
                .getFirstValue();

        if (!resolvedBrand.hasValue()) {
            return extractedBrand;
        }

        return contentMerger.merge((Brand) resolvedBrand.requireValue(), extractedBrand);
    }

    private ImmutableList<String> canonicalUrisFor(Brand extractedBrand) {
        return ImmutableList.of(extractedBrand.getCanonicalUri());
    }
    
    private String statusMessage(int processed, int failed) {
        return String.format("Number of entries processed: %d - Number of entries failed: %d",
                processed,
                failed);
    }

}
