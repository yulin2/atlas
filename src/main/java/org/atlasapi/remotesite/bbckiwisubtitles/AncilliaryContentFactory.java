package org.atlasapi.remotesite.bbckiwisubtitles;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.LookupRef;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.elasticsearch.common.collect.ImmutableSet;

public class AncilliaryContentFactory {

    public Content fromPrimaryContent(Content primaryContent, Publisher ancilliaryDataPublisher) {
        Content ancilliaryContent = null;
        if(primaryContent instanceof Brand) {
            ancilliaryContent = createAncilliaryBrand((Brand)primaryContent, ancilliaryDataPublisher);
        } else if(primaryContent instanceof Series) {
            ancilliaryContent =  createAncilliarySeries((Series)primaryContent, ancilliaryDataPublisher);
        } else if(primaryContent instanceof Item) {
            ancilliaryContent =  createAncilliaryItem((Item)primaryContent, ancilliaryDataPublisher);
        } else {
            throw new UnsupportedOperationException("Cannot deal with content of type " + primaryContent.getClass().getCanonicalName());
        }
        ancilliaryContent.setEquivalentTo(ImmutableSet.of(LookupRef.from(primaryContent)));
        return ancilliaryContent;
    }

    private Content createAncilliaryItem(Item primaryItem, Publisher ancilliaryDataPublisher) {
        return new Item(ancilliaryIdFor(primaryItem, ancilliaryDataPublisher), "", ancilliaryDataPublisher);
    }

    private Content createAncilliarySeries(Series primarySeries, Publisher ancilliaryDataPublisher) {
        return new Series(ancilliaryIdFor(primarySeries, ancilliaryDataPublisher), "", ancilliaryDataPublisher);
    }

    private Content createAncilliaryBrand(Brand primaryBrand, Publisher ancilliaryDataPublisher) {
        return new Brand(ancilliaryIdFor(primaryBrand, ancilliaryDataPublisher), "", ancilliaryDataPublisher);
    }
    
    private String ancilliaryIdFor(Identified identified, Publisher publisher) {
        return String.format("http://%s/%s", publisher.key(), identified.getCanonicalUri().replaceFirst("(http(s?)://)", ""));
    }
}
