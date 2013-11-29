package org.atlasapi.remotesite.wikipedia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.atlasapi.media.entity.Alias;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.CrewMember.Role;
import org.atlasapi.media.entity.Film;
import org.atlasapi.remotesite.wikipedia.film.FilmExtractor;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;

public class FilmExtractionTests {
    FilmExtractor extractor;
    String articleText;
    Article article;
    
    private Article fakeArticle(final String articleText) {
        return new Article() {
            @Override
            public DateTime getLastModified() {
                return new DateTime();
            }

            @Override
            public String getMediaWikiSource() {
                return articleText;
            }

            @Override
            public String getTitle() {
                return "Fake title";
            }
        };
    }
    
    private static class CrewMemberTestFields {
        public String name;
        public Role role;
        public String articleTitle;
        public CrewMemberTestFields(String name, Role role, String articleTitle) {
            this.name = name;
            this.role = role;
            this.articleTitle = articleTitle;
        }
    }
    
    private boolean matches(CrewMemberTestFields assertion, CrewMember person) {
        if (!( assertion.name.equals(person.name()) && assertion.role == person.role() )) {
            return false;
        }
        assertEquals(assertion.articleTitle == null ? null : Article.urlFromTitle(assertion.articleTitle), person.getCanonicalUri());
        return true;
    }
    
    private void assertAllPresentAndCorrect(List<CrewMember> actuallyExtracted, List<CrewMemberTestFields> shouldBe) {
        try {
            assertions:
            for (CrewMemberTestFields assertion : shouldBe) {
                for (CrewMember person : actuallyExtracted) {
                    if (matches(assertion, person)) { continue assertions; }
                }
                fail("Extraction of " + assertion.name + " seems to have failed.");
            }
            people:
            for (CrewMember person : actuallyExtracted) {
                for (CrewMemberTestFields assertion : shouldBe) {
                   if (matches(assertion, person)) { continue people; }
                }
                fail("Extracted unexpected " + person + " (" + person.name() + ")!");
            }
        } catch (AssertionError e) {
            System.err.println("Actually extracted these:");
            for (CrewMember person : actuallyExtracted) {
                System.err.println("  " + person.name() + ", " + person.role());
            }
            throw e;
        }
    }
    
    @Before
    public void setUp() throws IOException {
        extractor = new FilmExtractor();
    }
    
    @Test
    public void testHackers() throws IOException {
        Film flim = extractor.extract(fakeArticle(
                IOUtils.toString(Resources.getResource(getClass(), "film/Hackers.mediawiki").openStream(), Charsets.UTF_8.name())
        ));
        assertEquals("Hackers", flim.getTitle());
        
        assertTrue(flim.getAliases().contains(new Alias("imdb:url", "http://imdb.com/title/tt0113243")));
        assertTrue(flim.getAliases().contains(new Alias("imdb:title", "0113243")));
        
        assertAllPresentAndCorrect(flim.getPeople(), ImmutableList.of(
                new CrewMemberTestFields("Iain Softley", Role.DIRECTOR, "Iain Softley"),
                new CrewMemberTestFields("Michael Peyser", Role.PRODUCER, null),
                new CrewMemberTestFields("Rafael Moreu", Role.WRITER, "Rafael Moreu"),
                new CrewMemberTestFields("Simon Boswell", Role.COMPOSER, "Simon Boswell"),
                new CrewMemberTestFields("Chris Blunden", Role.EDITOR, null),
                new CrewMemberTestFields("Martin Walsh", Role.EDITOR, "Martin Walsh (film editor)"),
                new CrewMemberTestFields("Jonny Lee Miller", Role.ACTOR, "Jonny Lee Miller"),
                new CrewMemberTestFields("Angelina Jolie", Role.ACTOR, "Angelina Jolie"),
                new CrewMemberTestFields("Jesse Bradford", Role.ACTOR, "Jesse Bradford"),
                new CrewMemberTestFields("Matthew Lillard", Role.ACTOR, "Matthew Lillard"),
                new CrewMemberTestFields("Fisher Stevens", Role.ACTOR, "Fisher Stevens"),
                new CrewMemberTestFields("Lorraine Bracco", Role.ACTOR, "Lorraine Bracco"),
                new CrewMemberTestFields("Renoly Santiago", Role.ACTOR, "Renoly Santiago"),
                new CrewMemberTestFields("Laurence Mason", Role.ACTOR, "Laurence Mason")
        ));
    }
}
