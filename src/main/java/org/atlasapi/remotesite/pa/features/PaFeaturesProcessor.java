package org.atlasapi.remotesite.pa.features;

import java.util.Map;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.remotesite.pa.PaHelper;
import org.joda.time.Interval;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.metabroadcast.common.base.Maybe;

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
    
    public PaFeaturesProcessor(ContentResolver contentResolver, ContentGroupResolver contentGroupResolver, ContentGroupWriter contentGroupWriter, Interval featureDate) {
        this.contentResolver = contentResolver;
        this.contentGroupWriter = contentGroupWriter;
        this.featureDate = featureDate;
        
        Maybe<Identified> resolvedContent = contentGroupResolver.findByCanonicalUris(ImmutableList.of(CONTENT_GROUP_URI)).getFirstValue();
        if (resolvedContent.hasValue()) {
            contentGroup = (ContentGroup) resolvedContent.requireValue();
            contentGroup.setContents(ImmutableList.<ChildRef>of());
        } else {
            contentGroup = new ContentGroup(CONTENT_GROUP_URI, Publisher.PA_FEATURES);
        }
    }
    
    public void process(String programmeId) {
        Map<String, Optional<Content>> resolvedContent = contentResolver.resolveAliases(ImmutableSet.of(PaHelper.getFilmUri(programmeId), PaHelper.getEpisodeUri(programmeId)), Publisher.PA);
        Item item = (Item) Iterables.getOnlyElement(resolvedContent.values()).get();
        Broadcast broadcast = BY_BROADCAST_DATE.min(Iterables.concat(Iterables.transform(item.getVersions(), Version.TO_BROADCASTS)));
        if (featureDate.contains(broadcast.getTransmissionTime())) {
            contentGroup.addContent(item.childRef()); 
        }
    }
    
    public void writeContentGroup() {
        contentGroupWriter.createOrUpdate(contentGroup);
    }
    
}
