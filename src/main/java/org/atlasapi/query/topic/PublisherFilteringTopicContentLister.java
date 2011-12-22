package org.atlasapi.query.topic;

import java.util.Iterator;
import java.util.Set;

import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.topic.TopicContentLister;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

public class PublisherFilteringTopicContentLister implements TopicContentLister {
    
    private final TopicContentLister delegate;

    public PublisherFilteringTopicContentLister(TopicContentLister delegate) {
        this.delegate = delegate;
    }

    @Override
    public Iterator<Content> contentForTopic(String topicUri, ContentQuery contentQuery) {
        final Set<Publisher> includedPublishers = contentQuery.getConfiguration().getEnabledSources();
        return contentQuery.getSelection().applyTo(Iterators.filter(delegate.contentForTopic(topicUri, contentQuery), new Predicate<Described>() {
            @Override
            public boolean apply(Described input) {
                return includedPublishers.contains(input.getPublisher());
            }
        })).iterator();
    }
    
    

}
