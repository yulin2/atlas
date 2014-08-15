package org.atlasapi.remotesite.btvod;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.remotesite.btvod.BtVodData.BtVodDataRow;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.metabroadcast.common.base.Maybe;


public class BtVodContentGroupUpdater implements BtVodContentListener {

    private static final String FOX_PROVIDER_ID = "XXA";
    private static final String SONY_PROVIDER_ID = "XXB";
    private static final String EONE_PROVIDER_ID = "XXC";
    
    private final ContentGroupResolver contentGroupResolver;
    private final ContentGroupWriter contentGroupWriter;
    private final ImmutableMap<String, Predicate<VodDataAndContent>> contentGroupsAndCriteria;
    private final String uriPrefix;
    private final Publisher publisher;
    private Multimap<String, ChildRef> contents;

    public BtVodContentGroupUpdater(ContentGroupResolver contentGroupResolver, 
            ContentGroupWriter contentGroupWriter,
            Map<String, Predicate<VodDataAndContent>> contentGroupsAndCriteria,
            String uriPrefix, Publisher publisher) {
        this.contentGroupResolver = checkNotNull(contentGroupResolver);
        this.contentGroupWriter = checkNotNull(contentGroupWriter);
        this.contentGroupsAndCriteria = ImmutableMap.copyOf(contentGroupsAndCriteria);
        this.publisher = checkNotNull(publisher);
        this.uriPrefix = checkNotNull(uriPrefix);
    }
    
    @Override
    public void onContent(Content content, BtVodDataRow vodData) {
        VodDataAndContent vodDataAndContent = new VodDataAndContent(vodData, content);
        for (Entry<String, Predicate<VodDataAndContent>> entry : 
                contentGroupsAndCriteria.entrySet()) {
            if (entry.getValue().apply(vodDataAndContent)) {
                contents.put(entry.getKey(), content.childRef());
            }
        }
    }

    public void start() {
        contents = HashMultimap.create();
    }
    
    public void finish() {
        for (String key : contentGroupsAndCriteria.keySet()) {
            ContentGroup contentGroup = getOrCreateContentGroup(uriPrefix + key);
            Collection<ChildRef> newChildRefs = contents.get(key);
            
            contentGroup.setContents(newChildRefs);
            contentGroupWriter.createOrUpdate(contentGroup);
        }
    }
    
    private ContentGroup getOrCreateContentGroup(String canonicalUri) {
        Maybe<Identified> maybeContentGroup = contentGroupResolver
                .findByCanonicalUris(ImmutableSet.of(canonicalUri))
                .getFirstValue();
        
        if (maybeContentGroup.hasValue()) {
            return (ContentGroup) maybeContentGroup.requireValue();
        }
        
        return new ContentGroup(canonicalUri, publisher);
    }
    
    public static Predicate<VodDataAndContent> categoryPredicate(final String category) {
        
        return new Predicate<VodDataAndContent>() {

            @Override
            public boolean apply(VodDataAndContent input) {
                return category.equals(
                                    input.getBtVodDataRow()
                                         .getColumnValue(BtVodFileColumn.CATEGORY));
            }
            
        };
    }
    
    public static Predicate<VodDataAndContent> contentProviderPredicate(final String providerId) {
        
        return new Predicate<VodDataAndContent>() {

            @Override
            public boolean apply(VodDataAndContent input) {
                return providerId.equals(
                                    input.getBtVodDataRow()
                                         .getColumnValue(BtVodFileColumn.CONTENT_PROVIDER_ID));
            }
            
        };
    }
    
    @SuppressWarnings("unchecked")
    public static Predicate<VodDataAndContent> buyToOwnPredicate() {
        
        return Predicates.<VodDataAndContent>or(contentProviderPredicate(FOX_PROVIDER_ID), 
                             contentProviderPredicate(SONY_PROVIDER_ID), 
                             contentProviderPredicate(EONE_PROVIDER_ID));
        
    };
    
    public static Predicate<VodDataAndContent> tvBoxSetsPredicate() {
        
        return new Predicate<VodDataAndContent>() {

            @Override
            public boolean apply(VodDataAndContent input) {
                return Strings.isNullOrEmpty(input.getBtVodDataRow().getColumnValue(BtVodFileColumn.CATEGORY))
                        && input.getContent() instanceof Series;
            }
        };
    }
    
    public static Predicate<VodDataAndContent> boxOfficePredicate() {
        
        return Predicates.and(Predicates.not(buyToOwnPredicate()),
                new Predicate<VodDataAndContent>() {

                    @Override
                    public boolean apply(VodDataAndContent input) {
                        return input.getContent() instanceof Film;
                    }
        });
                
    }
}
