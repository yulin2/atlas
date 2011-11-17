package org.atlasapi.remotesite.bbc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;

import java.io.InputStreamReader;
import java.util.List;

import org.atlasapi.media.entity.KeyPhrase;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.html.HtmlNavigator;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;

public class BbcHashTagAdapterTest extends MockObjectTestCase {

    @SuppressWarnings("unchecked")
    private final RemoteSiteClient<HtmlNavigator> buzzPageClient = mock(RemoteSiteClient.class);

    private final BbcHashTagAdapter hashTagAdapter = new BbcHashTagAdapter(buzzPageClient);
    
    public void testCanFetchHashTagsFromBbcProgrammeBuzzPage() throws Exception {
        
        final String uri = "http://www.bbc.co.uk/programmes/b006mkw3/buzz";
        final HtmlNavigator buzzNavigator = new HtmlNavigator(new InputStreamReader(Resources.getResource("bbc-hignfy-buzz.html").openStream(), Charsets.UTF_8));
        
        checking(new Expectations(){{
            one(buzzPageClient).get(uri); will(returnValue(buzzNavigator));
        }});
        
        List<KeyPhrase> fetchedTags = hashTagAdapter.fetch(uri);
        
        ImmutableSet<String> expectedTags = ImmutableSet.of("#hignfy", "#bbcHIGNFY");

        assertThat(fetchedTags.size(), is(2));
        assertThat(fetchedTags.get(0).getPhrase(), isIn(expectedTags));
        assertThat(fetchedTags.get(1).getPhrase(), isIn(expectedTags));
    }

    public void testCanFetchSlashProgrammesUri() {
        assertTrue(hashTagAdapter.canFetch("http://www.bbc.co.uk/programmes/b006mkw3"));
    }
    
    public void testCanFetchSlashProgrammesBuzzUri() {
        assertTrue(hashTagAdapter.canFetch("http://www.bbc.co.uk/programmes/b006mkw3/buzz"));
    }
    
    public void testCantFetchNonSlashProgrammesUri() {
        assertFalse(hashTagAdapter.canFetch("http://www.example.com/uri"));
    }
    
    public void testCantFetchNonSlash() {
        assertFalse(hashTagAdapter.canFetch("http://www.example.com/uri/buzz"));
    }
}
