package org.atlasapi.remotesite.rte;

import java.util.Comparator;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.RelatedLink;
import org.atlasapi.media.entity.RelatedLink.LinkType;
import org.atlasapi.remotesite.ContentExtractor;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;


public class RteBrandExtractor implements ContentExtractor<Entry, Brand>{
    
    private static final Function<Link, RelatedLink> TO_RELATED_LINK = new Function<Link, RelatedLink>() {

        @Override
        public RelatedLink apply(Link alternateLink) {
            return new RelatedLink.Builder(LinkType.VOD, alternateLink.getHref()).build();
        }
    };
    
    private static final Comparator<RelatedLink> BY_LINK_URL = Ordering.natural()
            .onResultOf(new Function<RelatedLink, String>() {

                @Override
                public String apply(RelatedLink link) {
                    return link.getUrl();
                }
            });
    
    @Override
    @SuppressWarnings("unchecked")
    public Brand extract(Entry source) {
        Brand brand = new Brand();
        
        brand.setCanonicalUri(source.getId());
        brand.setTitle(source.getTitle());
        
        brand.setRelatedLinks(FluentIterable.from(source.getAlternateLinks())
                .transform(TO_RELATED_LINK)
                .toSortedList(BY_LINK_URL));
        
        brand.setPublisher(Publisher.RTE);
        
        return brand;
    }

}
