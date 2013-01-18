package org.atlasapi.equiv.generators;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import junit.framework.TestCase;

import org.atlasapi.equiv.ContentRef;
import org.atlasapi.equiv.EquivalenceSummary;
import org.atlasapi.equiv.EquivalenceSummaryStore;
import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Container;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

@RunWith(MockitoJUnitRunner.class)
public class ContainerChildEquivalenceGeneratorTest extends TestCase {

    private static final ImmutableSet<Id> NO_CANDIDATES = ImmutableSet.of();
    
    private final ContentResolver resolver = mock(ContentResolver.class);
    private final EquivalenceSummaryStore equivSummaryStore = mock(EquivalenceSummaryStore.class);

    private final ContainerChildEquivalenceGenerator generator = new ContainerChildEquivalenceGenerator(resolver, equivSummaryStore);
    
    @Test
    public void testExtractsContainerFromStrongItemEquivalents() {
        
        Container subject = new Brand("subject","s",Publisher.BBC);
        subject.setId(1L);
        Container equiv1 = new Brand("equivalent1","e1",Publisher.PA);
        equiv1.setId(2L);
        Container equiv2 = new Brand("equivalent2","e2",Publisher.ITV);
        equiv2.setId(3L);
        
        when(equivSummaryStore.summariesForChildren(subject.getId())).thenReturn(
            ImmutableSet.of(
                new EquivalenceSummary(3L,1L,NO_CANDIDATES,ImmutableMap.of(
                    Publisher.BBC, new ContentRef(6L, Publisher.BBC,null),
                    Publisher.PA, new ContentRef(5L, Publisher.PA, equiv1.getId()))),
                new EquivalenceSummary(4L,1L,NO_CANDIDATES,ImmutableMap.of(
                    Publisher.BBC, new ContentRef(7L, Publisher.BBC,equiv2.getId()),
                    Publisher.PA, new ContentRef(5L, Publisher.PA, equiv1.getId()))
                )
            )
        );
        
        ResolvedContent content = ResolvedContent.builder()
                .put(equiv1.getId(), equiv1)
                .put(equiv2.getId(), equiv2)
                .build();
        
        when(resolver.findByCanonicalUris(argThat(hasItems("equivalent1","equivalent2")))).thenReturn(
            content
        );
        
        ScoredCandidates<Container> scores = generator.generate(subject, new DefaultDescription());
        
        assertThat(scores.candidates(), hasEntry(equiv1, Score.ONE));
        assertThat(scores.candidates(), hasEntry(equiv2, Score.valueOf(0.5)));
    }

}
