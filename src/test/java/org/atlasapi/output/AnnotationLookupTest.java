package org.atlasapi.output;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.atlasapi.query.common.ActiveAnnotations;
import org.atlasapi.query.common.InvalidAnnotationException;
import org.atlasapi.query.common.Resource;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class AnnotationLookupTest {

    Set<Annotation> annotations = ImmutableSet.of(Annotation.ID, 
            Annotation.DESCRIPTION); 
    
    @Test
    public void testContentContextlessLookup() throws Exception {
        AnnotationLookup lookup = AnnotationLookup.builder()
            .withImplicitListContext(Resource.CONTENT, annotations)
            .build();
        
        ActiveAnnotations active = lookup.lookup(ImmutableList.of("description"));
        
        assertThat(active.forPath(ImmutableList.of(Resource.CONTENT)),
                is(ImmutableSet.of(Annotation.DESCRIPTION)));
        
    }

    @Test
    public void testContentContextLookup() throws Exception {
        AnnotationLookup lookup = AnnotationLookup.builder()
            .withImplicitListContext(Resource.CONTENT, annotations)
            .build();
        
        ActiveAnnotations active = lookup.lookup(ImmutableList.of("content.description"));
        
        assertThat(active.forPath(ImmutableList.of(Resource.CONTENT)),
                is(ImmutableSet.of(Annotation.DESCRIPTION)));
        
    }

    @Test
    public void testTopicLookupInContentContext() throws Exception {
        AnnotationLookup lookup = AnnotationLookup.builder()
            .withImplicitListContext(Resource.CONTENT, annotations)
            .withExplicitSingleContext(Resource.TOPIC, annotations)
            .build();
    
        ActiveAnnotations active
            = lookup.lookup(ImmutableList.of("description","topic.description"));
        
        assertThat(active.forPath(ImmutableList.of(Resource.CONTENT)),
                is(ImmutableSet.of(Annotation.DESCRIPTION)));
        assertThat(active.forPath(ImmutableList.of(Resource.TOPIC)),
                is(ImmutableSet.of(Annotation.DESCRIPTION)));
    }

    @Test
    public void testContentTopicLookupInContentContextless() throws Exception {
        AnnotationLookup lookup = AnnotationLookup.builder()
            .withImplicitListContext(Resource.CONTENT, annotations)
            .withExplicitSingleContext(Resource.TOPIC, annotations)
            .attachLookup(Resource.CONTENT, Annotation.TOPICS, AnnotationLookup.builder()
                .withImplicitListContext(Resource.TOPIC, annotations)
                .build())
            .build();
        
        ActiveAnnotations active = lookup.lookup(ImmutableList.of(
                "topics.description","topic.description"));
        

        assertThat(active.forPath(ImmutableList.of(Resource.CONTENT)),
                is(ImmutableSet.of(Annotation.TOPICS)));
        assertThat(active.forPath(ImmutableList.of(Resource.CONTENT,Resource.TOPIC)),
                is(ImmutableSet.of(Annotation.DESCRIPTION)));
        assertThat(active.forPath(ImmutableList.of(Resource.TOPIC)),
                is(ImmutableSet.of(Annotation.DESCRIPTION)));
    }

    @Test
    public void testContentTopicLookupInContentContext() throws InvalidAnnotationException {
        AnnotationLookup lookup = AnnotationLookup.builder()
                .withImplicitListContext(Resource.CONTENT, annotations)
                .withExplicitSingleContext(Resource.TOPIC, annotations)
                .attachLookup(Resource.CONTENT, Annotation.TOPICS, AnnotationLookup.builder()
                    .withImplicitListContext(Resource.TOPIC, annotations)
                    .build())
                .build();
        
        ActiveAnnotations active = lookup.lookup(ImmutableList.of(
                "topics.description","topic.description"));

        assertThat(active.forPath(ImmutableList.of(Resource.CONTENT)),
                is(ImmutableSet.of(Annotation.TOPICS)));
        assertThat(active.forPath(ImmutableList.of(Resource.CONTENT,Resource.TOPIC)),
                is(ImmutableSet.of(Annotation.DESCRIPTION)));
        assertThat(active.forPath(ImmutableList.of(Resource.TOPIC)),
                is(ImmutableSet.of(Annotation.DESCRIPTION)));
    }

    @Test
    public void testContentTopicLookupInContentContextHasDefault() throws InvalidAnnotationException {
        AnnotationLookup lookup = AnnotationLookup.builder()
                .withImplicitListContext(Resource.CONTENT, annotations)
                .withExplicitSingleContext(Resource.TOPIC, annotations)
                .attachLookup(Resource.CONTENT, Annotation.TOPICS, AnnotationLookup.builder()
                    .withImplicitSingleContext(Resource.TOPIC, annotations)
                    .build())
                .build();
        
        ActiveAnnotations active = lookup.lookup(ImmutableList.of(
                "topics","topic.description"));
        
        assertThat(active.forPath(ImmutableList.of(Resource.CONTENT)),
                is(ImmutableSet.of(Annotation.TOPICS)));
        assertThat(active.forPath(ImmutableList.of(Resource.CONTENT, Resource.TOPIC)),
                is(ImmutableSet.of(Annotation.ID)));
        assertThat(active.forPath(ImmutableList.of(Resource.TOPIC)),
                is(ImmutableSet.of(Annotation.DESCRIPTION)));
    }
}
