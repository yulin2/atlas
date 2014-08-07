package org.atlasapi.remotesite.pa.features;

import static com.google.api.client.util.Preconditions.checkState;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ResolvedContent;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;

public class PaFeaturesContentGroupProcessor {
    
    private static final String PREPARE_ERROR_MSG = "Must call prepareUpdate() before fetching a FeatureSets ContentGroups";

    private static final String ALL_CONTENT_GROUP_SUFFIX = "/all";
    
    private final ContentGroupWriter contentGroupWriter;
    private final ContentGroupResolver contentGroupResolver;

    private Map<String, FeatureSetContentGroups> contentGroups;
    private final PaFeaturesConfiguration config;
    
    public PaFeaturesContentGroupProcessor(ContentGroupResolver contentGroupResolver, 
            ContentGroupWriter contentGroupWriter, PaFeaturesConfiguration config) {
        this.contentGroupWriter = checkNotNull(contentGroupWriter);
        this.contentGroupResolver = checkNotNull(contentGroupResolver);
        this.config = checkNotNull(config);
    }

    public void prepareUpdate() {
        this.contentGroups = getOrCreateContentGroups();
    }
    
    private Map<String, FeatureSetContentGroups> getOrCreateContentGroups() {
        return ImmutableMap.copyOf(Maps.transformEntries(
                config.getFeatureSetMap(), 
                new EntryTransformer<String, ContentGroupDetails, FeatureSetContentGroups>() {
                    @Override
                    public FeatureSetContentGroups transformEntry(String key, ContentGroupDetails value) {
                        return new FeatureSetContentGroups(
                                getOrCreateContentGroup(value.uriBase(), value.publisher()),
                                getOrCreateContentGroup(value.uriBase() + ALL_CONTENT_GROUP_SUFFIX, value.publisher())
                        );
                    }
                }
        ));
    }

    private ContentGroup getOrCreateContentGroup(String uri, Publisher publisher) {
        ResolvedContent resolvedContent = contentGroupResolver.findByCanonicalUris(ImmutableList.of(uri));
        if (resolvedContent.get(uri).hasValue()) {
            ContentGroup contentGroup = (ContentGroup) resolvedContent.get(uri).requireValue();
            contentGroup.setContents(ImmutableList.<ChildRef>of());
            return contentGroup;
        } else {
            return new ContentGroup(uri, publisher);
        }
    }
    
    public Optional<FeatureSetContentGroups> getContentGroups(String featureSetId) {
        checkState(contentGroups != null, PREPARE_ERROR_MSG);
        return Optional.fromNullable(contentGroups.get(featureSetId));
    }
    
    public void finishUpdate() {
        checkState(contentGroups != null, PREPARE_ERROR_MSG);
        for (FeatureSetContentGroups featuredCgs : contentGroups.values()) {
            contentGroupWriter.createOrUpdate(featuredCgs.getTodayContentGroup());
            contentGroupWriter.createOrUpdate(featuredCgs.getAllFeaturedContentContentGroup());
        }
        contentGroups = null;
    }
    
    public static class FeatureSetContentGroups {
        
        private final ContentGroup todayContentGroup;
        private final ContentGroup allFeaturedContentEverContentGroup;
        
        public FeatureSetContentGroups(ContentGroup todayContentGroup, ContentGroup allFeaturedContentEverContentGroup) {
            this.todayContentGroup = checkNotNull(todayContentGroup);
            this.allFeaturedContentEverContentGroup = checkNotNull(allFeaturedContentEverContentGroup);
        }
        
        public ContentGroup getTodayContentGroup() {
            return todayContentGroup;
        }
        
        public ContentGroup getAllFeaturedContentContentGroup() {
            return allFeaturedContentEverContentGroup;
        }
    }
}
