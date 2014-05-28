package org.atlasapi.remotesite.rte;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.RelatedLink;
import org.atlasapi.media.entity.RelatedLink.LinkType;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;


public class RteBrandExtractorTest {
    
    private static final String ALTERNATE_LINK_HREF = "http://www.rte.ie/player/#v=3288112";
    
    private final RteBrandExtractor extractor = new RteBrandExtractor();
    
    @Test
    public void testBrandExtraction() {
        Entry source = new Entry();
        source.setId("http://feeds.rasset.ie/rteavgen/player/videos/show/?id=3288112");
        source.setTitle("A Stranger's Notebook on Dublin");
        
        Link alternateLink = new Link();
        alternateLink.setHref(ALTERNATE_LINK_HREF);
        source.setAlternateLinks(Lists.newArrayList(alternateLink));
        
        Brand brand = extractor.extract(source);
        
        assertThat(brand.getCanonicalUri(), equalTo("http://rte.ie/shows/3288112"));
        assertThat(brand.getTitle(), equalTo(source.getTitle()));
        assertThat(brand.getPublisher(), equalTo(Publisher.RTE));
        assertThat(brand.getMediaType(), equalTo(MediaType.VIDEO));
        
        RelatedLink relatedLink = new RelatedLink.Builder(LinkType.VOD, ALTERNATE_LINK_HREF).build();
        assertThat(brand.getRelatedLinks().size(), equalTo(1));
        assertThat(brand.getRelatedLinks(), hasItem(relatedLink));
    }
    
}
