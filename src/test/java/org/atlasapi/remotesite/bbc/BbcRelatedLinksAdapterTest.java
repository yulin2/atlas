package org.atlasapi.remotesite.bbc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.atlasapi.media.entity.RelatedLink;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.bbc.SlashProgrammesContainer.SlashProgrammesProgramme;
import org.atlasapi.remotesite.bbc.SlashProgrammesContainer.SlashProgrammesRelatedLink;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class BbcRelatedLinksAdapterTest extends MockObjectTestCase {

    @SuppressWarnings("unchecked")
    private final RemoteSiteClient<SlashProgrammesContainer> client = mock(RemoteSiteClient.class);
    
    private final BbcRelatedLinksAdapter adapter = new BbcRelatedLinksAdapter(client);
    
    public void testCanFetchRelatedLinksForBbcProgramme() throws Exception {
        
        final String uri = "http://www.bbc.co.uk/programmes/b006mkw3";
        
        final SlashProgrammesRelatedLink link = slashProgrammesLink("misc","Tag Title","http://ceci.nest.pas/une/link");
        
        checking(new Expectations(){{
            one(client).get(uri + ".json"); will(returnValue(containerWithLink(link)));
        }});
        
        List<RelatedLink> links = adapter.fetch(uri);
        
        assertThat(Iterables.getOnlyElement(links).getTitle(), is(link.getTitle()));
        assertThat(Iterables.getOnlyElement(links).getUrl(), is(link.getUrl()));
    }

    private SlashProgrammesContainer containerWithLink(SlashProgrammesRelatedLink link) {
        SlashProgrammesContainer container = new SlashProgrammesContainer();
        SlashProgrammesProgramme programme = new SlashProgrammesProgramme();
        programme.setLinks(ImmutableList.of(link));
        container.setProgramme(programme);
        return container;
    }

    private SlashProgrammesRelatedLink slashProgrammesLink(String type, String title, String url) {
        SlashProgrammesRelatedLink link = new SlashProgrammesRelatedLink();
        link.setType(type);
        link.setTitle(title);
        link.setUrl(url);
        return link;
    }
}
