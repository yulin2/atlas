package org.atlasapi.remotesite.bbc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;

import java.io.InputStreamReader;
import java.util.List;

import junit.framework.TestCase;

import org.atlasapi.media.entity.KeyPhrase;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.html.HtmlNavigator;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;

@RunWith(JMock.class)
public class BbcHashTagAdapterTest extends TestCase {

    private final Mockery context = new Mockery();
    @SuppressWarnings("unchecked")
    private final RemoteSiteClient<HtmlNavigator> buzzPageClient = context.mock(RemoteSiteClient.class);

    private final BbcHashTagAdapter hashTagAdapter = new BbcHashTagAdapter(buzzPageClient);

    @Test
    public void testCanFetchHashTagsFromBbcProgrammeBuzzPage() throws Exception {
        
        final String uri = "http://www.bbc.co.uk/programmes/b006mkw3/buzz";
        final HtmlNavigator buzzNavigator = new HtmlNavigator(new InputStreamReader(Resources.getResource("bbc-hignfy-buzz.html").openStream(), Charsets.UTF_8));
        
        context.checking(new Expectations(){{
            one(buzzPageClient).get(uri); will(returnValue(buzzNavigator));
        }});
        
        List<KeyPhrase> fetchedTags = hashTagAdapter.fetch(uri);
        
        ImmutableSet<String> expectedTags = ImmutableSet.of("#hignfy", "#bbcHIGNFY");

        assertThat(fetchedTags.size(), is(2));
        assertThat(fetchedTags.get(0).getPhrase(), isIn(expectedTags));
        assertThat(fetchedTags.get(1).getPhrase(), isIn(expectedTags));
    }

    @Test
    public void testCanFetchSlashProgrammesUri() {
        assertTrue(hashTagAdapter.canFetch("http://www.bbc.co.uk/programmes/b006mkw3"));
    }

    @Test
    public void testCanFetchSlashProgrammesBuzzUri() {
        assertTrue(hashTagAdapter.canFetch("http://www.bbc.co.uk/programmes/b006mkw3/buzz"));
    }

    @Test
    public void testCantFetchNonSlashProgrammesUri() {
        assertFalse(hashTagAdapter.canFetch("http://www.example.com/uri"));
    }

    @Test
    public void testCantFetchNonSlash() {
        assertFalse(hashTagAdapter.canFetch("http://www.example.com/uri/buzz"));
    }
}
