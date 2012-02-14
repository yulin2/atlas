package org.atlasapi.output.simple;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.KeyPhrase;
import org.atlasapi.media.entity.simple.RelatedLink;
import org.atlasapi.output.Annotation;
import org.atlasapi.persistence.topic.TopicQueryResolver;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class ContentModelSimplifier<F extends Content, T extends Description> extends DescribedModelSimplifier<F, T> {

    private final TopicQueryResolver topicResolver;

    private final TopicModelSimplifier topicSimplifier = new TopicModelSimplifier();

    public ContentModelSimplifier(TopicQueryResolver topicResolver) {
        this.topicResolver = topicResolver;
    }

    protected void copyBasicContentAttributes(F content, T simpleDescription, Set<Annotation> annotations) {
        copyBasicDescribedAttributes(content, simpleDescription, annotations);

        if (annotations.contains(Annotation.CLIPS)) {
            simpleDescription.setClips(clipToSimple(content.getClips(), annotations));
        }
        if (annotations.contains(Annotation.TOPICS)) {
            simpleDescription.setTopics(topicRefToSimple(content.getTopicRefs(), annotations));
        }
        if (annotations.contains(Annotation.KEY_PHRASES)) {
            simpleDescription.setKeyPhrases(simplifyPhrases(content));
        }
        if (annotations.contains(Annotation.RELATED_LINKS)) {
            simpleDescription.setRelatedLinks(simplifyRelatedLinks(content));
        }
    }

    public Iterable<RelatedLink> simplifyRelatedLinks(F content) {
        return Iterables.transform(content.getRelatedLinks(), new Function<org.atlasapi.media.entity.RelatedLink, RelatedLink>() {

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
            }
        });
    }

    private Iterable<Topic> resolveTopics(Iterable<Long> topics, Set<Annotation> annotations) {
        if (Iterables.isEmpty(topics)) { // don't even ask (the resolver)
            return ImmutableList.of();
        }
        return topicResolver.topicsForIds(topics);
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
                return simplify(clip, annotations);
            }
        });
    }

    private List<org.atlasapi.media.entity.simple.TopicRef> topicRefToSimple(List<TopicRef> contentTopics, final Set<Annotation> annotations) {

        final Map<Long, Topic> topics = Maps.uniqueIndex(resolveTopics(Iterables.transform(contentTopics, TOPICREF_TO_TOPIC_ID), annotations), TOPIC_TO_TO_TOPIC_ID);

        return Lists.transform(contentTopics, new Function<TopicRef, org.atlasapi.media.entity.simple.TopicRef>() {
            @Override
            public org.atlasapi.media.entity.simple.TopicRef apply(TopicRef topicRef) {
                org.atlasapi.media.entity.simple.TopicRef tr = new org.atlasapi.media.entity.simple.TopicRef();
                tr.setSupervised(topicRef.isSupervised());
                tr.setWeighting(topicRef.getWeighting());
                tr.setTopic(topicSimplifier.simplify(topics.get(topicRef.getTopic()), annotations));
                return tr;
            }
        });
    }

    private final Function<TopicRef, Long> TOPICREF_TO_TOPIC_ID = new Function<TopicRef, Long>() {
        @Override
        public Long apply(TopicRef input) {
            return input.getTopic();
        }
    };

    private static Function<Topic, Long> TOPIC_TO_TO_TOPIC_ID = new Function<Topic, Long>() {
        @Override
        public Long apply(Topic input) {
            return input.getId();
        }
    };

    protected abstract org.atlasapi.media.entity.simple.Item simplify(Item item, Set<Annotation> annotations);

}
