package org.atlasapi.remotesite.bbc;

import static org.atlasapi.remotesite.bbc.BbcSlashProgrammesRdfClient.slashProgrammesClient;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.remotesite.FixedResponseHttpClient;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.springframework.core.io.ClassPathResource;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.http.SimpleHttpClient;

public class BbcSlashProgrammesTopicsAdapterTest extends MockObjectTestCase {

    private static String URI = "http://www.bbc.co.uk/programmes/b0144pvg.rdf";
    @SuppressWarnings("unchecked")
    private final SiteSpecificAdapter<Topic> topicFetcher = mock(SiteSpecificAdapter.class);
    private static final SimpleHttpClient httpClient = new FixedResponseHttpClient(URI , xmlDocument());

    private static String xmlDocument()  {
        try {
            return IOUtils.toString(new ClassPathResource("brighton-to-eastbourne-rdf.xml").getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    BbcSlashProgrammesTopicsAdapter adapter = new BbcSlashProgrammesTopicsAdapter(slashProgrammesClient(httpClient, SlashProgrammesRdf.class), topicFetcher);
    
    public void testFetch() {
        
        ImmutableList<String> topics = ImmutableList.of("public_transport", "sandi_toksvig", "brighton", "eastbourne");
        
        for (String topicSuffix : topics) {
            final String uri = slashProgrammsUri(topicSuffix);
            checking(new Expectations() {{
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
