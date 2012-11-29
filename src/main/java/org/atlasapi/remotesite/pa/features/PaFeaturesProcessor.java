package org.atlasapi.remotesite.pa.features;

import java.util.Map;
import java.util.NoSuchElementException;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.remotesite.pa.PaHelper;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

public class PaFeaturesProcessor {
    private static final String CONTENT_GROUP_URI = "http://pressassocation.com/features/tvpicks";
    private static final Ordering<Broadcast> BY_BROADCAST_DATE = new Ordering<Broadcast>() {
        @Override
        public int compare(Broadcast left, Broadcast right) {
            return left.getTransmissionTime().compareTo(right.getTransmissionTime());
        }
    };
    
    private final ContentResolver contentResolver;
    private final ContentGroupWriter contentGroupWriter;
    private final Interval featureDate;
    private ContentGroup contentGroup;
    private final Logger log = LoggerFactory.getLogger(PaFeaturesProcessor.class);
    
    public PaFeaturesProcessor(ContentResolver contentResolver, ContentGroupResolver contentGroupResolver, ContentGroupWriter contentGroupWriter, Interval featureDate) {
        this.contentResolver = contentResolver;
        this.contentGroupWriter = contentGroupWriter;
        this.featureDate = featureDate;
        
        ResolvedContent resolvedContent = contentGroupResolver.findByCanonicalUris(ImmutableList.of(CONTENT_GROUP_URI));
        if (resolvedContent.get(CONTENT_GROUP_URI).hasValue()) {
            contentGroup = (ContentGroup) resolvedContent.get(CONTENT_GROUP_URI).requireValue();
            contentGroup.setContents(ImmutableList.<ChildRef>of());
        } else {
            contentGroup = new ContentGroup(CONTENT_GROUP_URI, Publisher.PA_FEATURES);
        }
    }
    
    public void process(String programmeId) {
        Map<String, Identified> resolvedContent = contentResolver.findByCanonicalUris(ImmutableSet.of(PaHelper.getFilmUri(programmeId), PaHelper.getEpisodeUri(programmeId))).asResolvedMap();
        try {
            Item item = (Item) Iterables.getOnlyElement(resolvedContent.values());
            Broadcast broadcast = BY_BROADCAST_DATE.min(Iterables.concat(Iterables.transform(item.getVersions(), Version.TO_BROADCASTS)));
            if (featureDate.contains(broadcast.getTransmissionTime())) {
                contentGroup.addContent(item.childRef()); 
            }
        } catch (NoSuchElementException e) {
            log .error("No content found for programme Id: " + programmeId, e);
        }
    }
    
    public void writeContentGroup() {
        contentGroupWriter.createOrUpdate(contentGroup);
    }
    
}
