package org.atlasapi.output.simple;

import java.util.List;
import java.util.Set;

import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.KeyPhrase;
import org.atlasapi.media.entity.simple.PublisherDetails;
import org.atlasapi.media.entity.simple.RelatedLink;
import org.atlasapi.media.entity.simple.TopicQueryResult;
import org.atlasapi.output.Annotation;
import org.atlasapi.persistence.topic.TopicQueryResolver;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public abstract class ContentModelSimplifier<F extends Content, T extends Description> extends DescribedModelSimplifier<F,T> {

    private final TopicQueryResolver topicResolver;

    public ContentModelSimplifier(TopicQueryResolver topicResolver) {
        this.topicResolver = topicResolver;
    }
    
    protected void copyBasicContentAttributes(F content, T simpleDescription, Set<Annotation> annotations) {
        copyBasicDescribedAttributes(content, simpleDescription, annotations);

        if (annotations.contains(Annotation.CLIPS)) {
            simpleDescription.setClips(clipToSimple(content.getClips(),annotations));
        }
        
        if (annotations.contains(Annotation.TOPICS)) {
            simpleDescription.setTopics(resolveTopics(content.getTopics(), annotations));
        }
        if (annotations.contains(Annotation.KEY_PHRASES)) {
            simpleDescription.setKeyPhrases(simplifyPhrases(content));
        }
        if (annotations.contains(Annotation.RELATED_LINKS)) {
            simpleDescription.setRelatedLinks(simplifyRelatedLinks(content));
        }
    }

    public Iterable<RelatedLink> simplifyRelatedLinks(F content) {
        return Iterables.transform(content.getRelatedLinks(), new Function<org.atlasapi.media.entity.RelatedLink, RelatedLink>(){

            @Override
            public RelatedLink apply(org.atlasapi.media.entity.RelatedLink rl) {
                RelatedLink simpleLink = new RelatedLink();
                
                simpleLink.setUrl(rl.getUrl());
                simpleLink.setType(rl.getType().toString().toLowerCase());
                simpleLink.setSourceId(rl.getSourceId());
                simpleLink.setShortName(rl.getShortName());
                simpleLink.setTitle(rl.getTitle());
                simpleLink.setDescription(rl.getDescription());
                simpleLink.setImage(rl.getImage());
                simpleLink.setThumbnail(rl.getThumbnail());
                
                return simpleLink;
            }});
    }
    
    private Iterable<org.atlasapi.media.entity.simple.Topic> resolveTopics(List<String> topics, Set<Annotation> annotations) {
        if(topics.isEmpty()) { //don't even ask (the resolver)
            return ImmutableList.of();
        }
        return writeOutTopics(topicResolver.topicsForUris(topics), annotations).getContents();
    }

    public Iterable<KeyPhrase> simplifyPhrases(F content) {
        return Iterables.transform(content.getKeyPhrases(), new Function<org.atlasapi.media.entity.KeyPhrase, KeyPhrase>() {
            @Override
            public KeyPhrase apply(org.atlasapi.media.entity.KeyPhrase input) {
                return new KeyPhrase(input.getPhrase(), toPublisherDetails(input.getPublisher()), input.getWeighting());
            }
        });
    }
    
    private List<org.atlasapi.media.entity.simple.Item> clipToSimple(List<Clip> clips, final Set<Annotation> annotations) {
        return Lists.transform(clips, new Function<Clip, org.atlasapi.media.entity.simple.Item>() {
            @Override
            public org.atlasapi.media.entity.simple.Item apply(Clip clip) {
                return simplify(clip,annotations);
            }
        });
    }
    
    private TopicQueryResult writeOutTopics(Iterable<Topic> fullTopics, Set<Annotation> annotations) {
        TopicQueryResult result = new TopicQueryResult();
        for (Topic fullTopic : fullTopics) {
            org.atlasapi.media.entity.simple.Topic topic = new org.atlasapi.media.entity.simple.Topic();
            copyIdentifiedAttributesTo(fullTopic, topic, annotations);
            topic.setTitle(fullTopic.getTitle());
            topic.setDescription(fullTopic.getDescription());
            topic.setImage(fullTopic.getImage());
            topic.setImage(fullTopic.getThumbnail());
            topic.setPublishers(ImmutableSet.copyOf(Iterables.transform(fullTopic.getPublishers(), new Function<Publisher, PublisherDetails>() {
                @Override
                public PublisherDetails apply(Publisher input) {
                    return toPublisherDetails(input);
                }
            })));
            topic.setType(fullTopic.getType().toString());
            topic.setValue(fullTopic.getValue());
            topic.setNamespace(fullTopic.getNamespace());
            result.add(topic);
        }
        return result;
    }

    protected abstract org.atlasapi.media.entity.simple.Item simplify(Item item, Set<Annotation> annotations);

}
