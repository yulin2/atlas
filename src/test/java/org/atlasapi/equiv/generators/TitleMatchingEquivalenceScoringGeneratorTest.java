package org.atlasapi.equiv.generators;

import static com.google.common.collect.ImmutableSet.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.query.content.search.DummySearcher;
import org.atlasapi.search.model.SearchQuery;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class TitleMatchingEquivalenceScoringGeneratorTest {

    @Test
    public void testDoesntSearchForPublisherOfSubjectContent() {
        
        final Publisher subjectPublisher = Publisher.BBC;
        Brand subject = new Brand("uri","curie",subjectPublisher);
        final String title = "test";
        subject.setTitle(title);

        SearchResolver searchResolver = new SearchResolver() {
            @Override
            public List<Identified> search(SearchQuery query, ApplicationConfiguration appConfig) {
                
                assertFalse(query.getIncludedPublishers().contains(subjectPublisher));
                assertFalse(appConfig.getEnabledSources().contains(subjectPublisher));
                
                assertTrue(query.getTerm().equals(title));
                
                Brand result = new Brand("result","curie",Publisher.PA);
                result.setTitle(title);
                return ImmutableList.<Identified>of(result);
            }
        };
        
        TitleMatchingEquivalenceScoringGenerator generator = new TitleMatchingEquivalenceScoringGenerator(searchResolver);
        ScoredEquivalents<Container> generated = generator.generate(subject, new DefaultDescription());
        
        assertTrue(generated.equivalents().keySet().size() == 1);
        
    }

    @Test
    public void testScoringBrandTitles() {
        
        TitleMatchingEquivalenceScoringGenerator scorer = new TitleMatchingEquivalenceScoringGenerator(new DummySearcher());
        DefaultDescription desc = new DefaultDescription();
        
        score(1, scorer.score(brandWithTitle("The Great Escape"), of(brandWithTitle("The Great Escape")), desc));
        scoreLt(0, scorer.score(brandWithTitle("The Great Escape"), of(brandWithTitle("Italian Job")), desc));
        score(1, scorer.score(brandWithTitle("Rosencrantz & Guildenstern Are Dead"), of(brandWithTitle("Rosencrantz and Guildenstern Are Dead")), desc));
        score(1, scorer.score(brandWithTitle("Bill & Ben"), of(brandWithTitle("Bill and Ben")), desc));
        
        score(1, scorer.score(brandWithTitle("The Great Escape"), of(brandWithTitle("Great Escape")), desc));
        score(1, scorer.score(brandWithTitle("the Great Escape"), of(brandWithTitle("Great Escape")), desc));
        scoreLt(0, scorer.score(brandWithTitle("Theatreland"), of(brandWithTitle("The atreland")), desc));
        scoreLt(0, scorer.score(brandWithTitle("theatreland"), of(brandWithTitle("the atreland")), desc));
    }
    
    private void scoreLt(double expected, ScoredEquivalents<Container> scores) {
        Score value = Iterables.getOnlyElement(scores.equivalents().entrySet()).getValue();
        assertThat(value.asDouble(), is(lessThan(expected)));
    }

    private void score(double expected, ScoredEquivalents<Container> scores) {
        Score value = Iterables.getOnlyElement(scores.equivalents().entrySet()).getValue();
        assertTrue(String.format("expected %s got %s", expected, value), value.equals(expected > 0 ? Score.valueOf(expected) : Score.NULL_SCORE));
    }

    private Container brandWithTitle(String title) {
        Brand brand = new Brand("uri","curie",Publisher.BBC);
        brand.setTitle(title);
        return brand;
    }

}
