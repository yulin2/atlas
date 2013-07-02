package org.atlasapi.remotesite.btfeatured;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import nu.xom.Element;

import org.atlasapi.media.entity.Certificate;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.intl.Countries;


public class BTFeaturedElementHandler implements SimpleElementHandler {

    public static final String CURIE_PREFIX = "btfeatured:";

    private static final Logger log = LoggerFactory.getLogger(BTFeaturedElementHandler.class);
    
    private ContentGroup contentGroup;

    @Override
    public Optional<Content> handle(Element element, @Nonnull Optional<Container> parent) {
        if (!(element instanceof BTFeaturedProductElement)) {
            return Optional.absent();
        }
        BTFeaturedProductElement product = (BTFeaturedProductElement)element;
        
        
        if (product.isCollection()) { 
            /**
             * title          title
             * description    collection/description
             * image          'http://portal.vision.bt.com/btvo/' + collection/packshot
             * certificates   rating                                                Standard BBFC values, set country to 'GB'
             * genres         collection/subgenre                                   See below
             * total_epsiodes collection/count
             */
           Container container = new Container(getUriFrom(product.getProductId()), getCurieFrom(product.getProductId()), Publisher.BT_FEATURED_CONTENT);
           
           Element collectionElement = product.getCollection();
           
           setCommonFields(container, product, collectionElement);
           container.setDescription(collectionElement.getAttributeValue("description"));
           container.setGenres(Arrays.asList(collectionElement.getAttributeValue("subgenre").split(",")));
           setGenres(container, collectionElement, "subgenre");
           
           return Optional.<Content>of(container);
        }
        else if (product.isSeries()) {
            log.info("Got series "+product.getTitle()+" id is "+product.getProductId()+" parent is "+parent);
            Series series = new Series(getUriFrom(product.getProductId()), getCurieFrom(product.getProductId()), Publisher.BT_FEATURED_CONTENT);
            
            Element seriesElement = product.getSeries();
            
            setCommonFields(series, product, seriesElement);
            series.setDescription(seriesElement.getAttributeValue("description"));
            setGenres(series, seriesElement, "subgenre");
            
            return Optional.<Content>of(series);
        }
        
        log.info("Got product "+product.getTitle()+" id is "+product.getProductId()+" parent is "+parent);
        Item item = new Item(getUriFrom(product.getProductId()), getCurieFrom(product.getProductId()), Publisher.BT_FEATURED_CONTENT);
        
        Element assetElement = product.getAsset();
        setCommonFields(item, product, assetElement);
        item.setDescription(assetElement.getAttributeValue("synopsis"));
        
        setGenres(item, assetElement, "subGenre");
        
        if (parent.isPresent()) {
            item.setParentRef(ParentRef.parentRefFrom(parent.get()));
        }
        contentGroup.addContent(item.childRef());
        return Optional.<Content>of(item);
    }


    protected void setGenres(Content content, Element described, String attributeName) {
        List<String> genreList = Arrays.asList(described.getAttributeValue(attributeName).split(","));

        content.setGenres(
                Iterables.transform(genreList,
                        new Function<String, String>() {
                            @Override
                            @Nullable
                            public String apply(@Nullable String genre) {
                                return "http://featured.bt.com/genres/"+genre;
                            }
                        }
                ));
    }


    private void setCommonFields(Content content, BTFeaturedProductElement product, Element described) {
        content.setTitle(product.getTitle());
        content.setImage("http://portal.vision.bt.com/btvo/"+described.getAttributeValue("packshot"));
        
        String rating = described.getAttributeValue("rating");
        content.setCertificates(ImmutableList.of(new Certificate(rating, Countries.GB)));
    }
    
    private String getCurieFrom(String productId) {
        return CURIE_PREFIX+productId;
    }

    private String getUriFrom(String productId) {
        return "http://featured.bt.com/products/" + productId;
    }

    @Override
    public void withContentGroup(ContentGroup contentGroup) {
        this.contentGroup = contentGroup;
    }
}
