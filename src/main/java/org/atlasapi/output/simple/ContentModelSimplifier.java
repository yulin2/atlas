package org.atlasapi.output.simple;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.KeyPhrase;
import org.atlasapi.media.product.Product;
import org.atlasapi.media.entity.simple.RelatedLink;
import org.atlasapi.media.product.ProductResolver;
import org.atlasapi.output.Annotation;
import org.atlasapi.persistence.topic.TopicQueryResolver;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.ContentGroupRef;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.persistence.content.ContentGroupResolver;

public abstract class ContentModelSimplifier<F extends Content, T extends Description> extends DescribedModelSimplifier<F, T> {

    private final ContentGroupResolver contentGroupResolver;
    private final ModelSimplifier<ContentGroup, org.atlasapi.media.entity.simple.ContentGroup> contentGroupSimplifier;
    private final TopicQueryResolver topicResolver;
    private final ModelSimplifier<Topic, org.atlasapi.media.entity.simple.Topic> topicSimplifier;
    private final ProductResolver productResolver;
    private final ModelSimplifier<Product, org.atlasapi.media.entity.simple.Product> productSimplifier;

    public ContentModelSimplifier(String localHostName, ContentGroupResolver contentGroupResolver, TopicQueryResolver topicResolver, ProductResolver productResolver) {
        this.contentGroupResolver = contentGroupResolver;
        this.topicResolver = topicResolver;
        this.productResolver = productResolver;
        this.contentGroupSimplifier = new ContentGroupModelSimplifier();
        this.topicSimplifier = new TopicModelSimplifier(localHostName);
        this.productSimplifier = new ProductModelSimplifier(localHostName);
    }

    protected void copyBasicContentAttributes(F content, T simpleDescription, Set<Annotation> annotations, ApplicationConfiguration config) {
        copyBasicDescribedAttributes(content, simpleDescription, annotations);

        simpleDescription.setId(null);

        if (annotations.contains(Annotation.CLIPS)) {
            simpleDescription.setClips(clipToSimple(content.getClips(), annotations, config));
        }
        if (annotations.contains(Annotation.TOPICS)) {
            simpleDescription.setTopics(topicRefToSimple(content.getTopicRefs(), annotations, config));
        }
        if (annotations.contains(Annotation.CONTENT_GROUP)) {
            simpleDescription.setContentGroups(contentGroupRefToSimple(content.getContentGroupRefs(), annotations, config));
        }
        if (annotations.contains(Annotation.KEY_PHRASES)) {
            simpleDescription.setKeyPhrases(simplifyPhrases(content));
        }
        if (annotations.contains(Annotation.RELATED_LINKS)) {
            simpleDescription.setRelatedLinks(simplifyRelatedLinks(content));
        }
        if (annotations.contains(Annotation.PRODUCTS)) {
            simpleDescription.setProducts(resolveAndSimplifyProductsFor(content, annotations, config));
        }
    }

    private Iterable<org.atlasapi.media.entity.simple.Product> resolveAndSimplifyProductsFor(Content content, final Set<Annotation> annotations, final ApplicationConfiguration config) {
        return Iterables.transform(filter(productResolver.productsForContent(content.getCanonicalUri()), config), new Function<Product, org.atlasapi.media.entity.simple.Product>() {

            @Override
            public org.atlasapi.media.entity.simple.Product apply(Product input) {
                return productSimplifier.simplify(input, annotations, config);
            }
        });
    }

    private Iterable<Product> filter(Iterable<Product> productsForContent, final ApplicationConfiguration config) {
        return Iterables.filter(productsForContent, new Predicate<Product>() {

            @Override
            public boolean apply(Product input) {
                return config.isEnabled(input.getPublisher());
            }
        });
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

    private Iterable<Topic> res(Iterable<Long> topics, Set<Annotation> annotations) {
        if (Iterables.isEmpty(topics)) { // don't even ask (the resolver)
            return ImmutableList.of();
        }
        return topicResolver.topicsForIds(topics);
    }

    private Iterable<ContentGroup> resolveContentGroups(Iterable<Long> contentGroups, Set<Annotation> annotations) {
        if (Iterables.isEmpty(contentGroups)) { // don't even ask (the resolver)
            return ImmutableList.of();
        }
        return Iterables.transform(contentGroupResolver.findByIds(contentGroups).asResolvedMap().values(), new Function<Identified, ContentGroup>() {

            @Override
            public ContentGroup apply(Identified input) {
                return (ContentGroup) input;
            }
        });
    }

    public Iterable<KeyPhrase> simplifyPhrases(F content) {
        return Iterables.transform(content.getKeyPhrases(), new Function<org.atlasapi.media.entity.KeyPhrase, KeyPhrase>() {

            @Override
            public KeyPhrase apply(org.atlasapi.media.entity.KeyPhrase input) {
                return new KeyPhrase(input.getPhrase(), toPublisherDetails(input.getPublisher()), input.getWeighting());
            }
        });
    }

    private List<org.atlasapi.media.entity.simple.Item> clipToSimple(List<Clip> clips, final Set<Annotation> annotations, final ApplicationConfiguration config) {
        return Lists.transform(clips, new Function<Clip, org.atlasapi.media.entity.simple.Item>() {

            @Override
            public org.atlasapi.media.entity.simple.Item apply(Clip clip) {
                return simplify(clip, annotations, config);
            }
        });
    }

    private List<org.atlasapi.media.entity.simple.TopicRef> topicRefToSimple(List<TopicRef> contentTopics, final Set<Annotation> annotations, final ApplicationConfiguration config) {

        final Map<Long, Topic> topics = Maps.uniqueIndex(res(Iterables.transform(contentTopics, TOPICREF_TO_TOPIC_ID), annotations), TOPIC_TO_TO_TOPIC_ID);

        return Lists.transform(contentTopics, new Function<TopicRef, org.atlasapi.media.entity.simple.TopicRef>() {

            @Override
            public org.atlasapi.media.entity.simple.TopicRef apply(TopicRef topicRef) {
                org.atlasapi.media.entity.simple.TopicRef tr = new org.atlasapi.media.entity.simple.TopicRef();
                tr.setSupervised(topicRef.isSupervised());
                tr.setWeighting(topicRef.getWeighting());
                tr.setTopic(topicSimplifier.simplify(topics.get(topicRef.getTopic()), annotations, config));
                return tr;
            }
        });
    }

    private Iterable<org.atlasapi.media.entity.simple.ContentGroup> contentGroupRefToSimple(List<ContentGroupRef> refs, final Set<Annotation> annotations, final ApplicationConfiguration config) {

        Iterable<ContentGroup> groups = resolveContentGroups(Iterables.transform(refs, CONTENT_GROUP_REF_TO_CONTENT_GROUP_ID), annotations);

        return Iterables.transform(groups, new Function<ContentGroup, org.atlasapi.media.entity.simple.ContentGroup>() {

            @Override
            public org.atlasapi.media.entity.simple.ContentGroup apply(ContentGroup group) {
                return contentGroupSimplifier.simplify(group, annotations, config);
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
    private final Function<ContentGroupRef, Long> CONTENT_GROUP_REF_TO_CONTENT_GROUP_ID = new Function<ContentGroupRef, Long>() {

        @Override
        public Long apply(ContentGroupRef input) {
            return input.getId();
        }
    };
    private static Function<ContentGroup, Long> CONTENT_GROUP_TO_CONTENT_GROUP_ID = new Function<ContentGroup, Long>() {

        @Override
        public Long apply(ContentGroup input) {
            return input.getId();
        }
    };

    protected abstract org.atlasapi.media.entity.simple.Item simplify(Item item, Set<Annotation> annotations, ApplicationConfiguration config);
}
