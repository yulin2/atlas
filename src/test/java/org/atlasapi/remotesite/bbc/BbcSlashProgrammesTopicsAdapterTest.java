package org.atlasapi.remotesite.bbc;

import static org.atlasapi.remotesite.bbc.BbcSlashProgrammesRdfClient.slashProgrammesClient;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.http.FixedResponseHttpClient;
import com.metabroadcast.common.http.SimpleHttpClient;

@Deprecated
@RunWith(JMock.class)
public class BbcSlashProgrammesTopicsAdapterTest extends TestCase {

    private final Mockery context = new Mockery();
    private static String URI = "http://www.bbc.co.uk/programmes/b0144pvg.rdf";
    @SuppressWarnings("unchecked")
    private final SiteSpecificAdapter<TopicRef> topicFetcher = context.mock(SiteSpecificAdapter.class);
    private static final SimpleHttpClient httpClient = new FixedResponseHttpClient(URI , xmlDocument());

    private static String xmlDocument()  {
        try {
            return IOUtils.toString(new ClassPathResource("brighton-to-eastbourne-rdf.xml").getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    BbcSlashProgrammesTopicsAdapter adapter = new BbcSlashProgrammesTopicsAdapter(slashProgrammesClient(httpClient, SlashProgrammesRdf.class), topicFetcher);
    
    @Test
    public void testFetch() {
        
        ImmutableList<String> topics = ImmutableList.of("public_transport", "sandi_toksvig", "brighton", "eastbourne");
        
        for (String topicSuffix : topics) {
            final String uri = slashProgrammsUri(topicSuffix);
            context.checking(new Expectations() {{
                one(topicFetcher).canFetch(uri); will(returnValue(true));
                one(topicFetcher).fetch(uri); will(returnValue(null));
            }});
        }
        
        adapter.fetch("http://www.bbc.co.uk/programmes/b0144pvg");
        
    }

    private String slashProgrammsUri(String suffix) {
        return "http://www.bbc.co.uk/programmes/topics/" + suffix;
    }
}
