package org.atlasapi.remotesite.btfeatured;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import nu.xom.Element;
import nu.xom.Elements;

import org.apache.commons.lang.StringUtils;
import org.atlasapi.media.entity.Certificate;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Policy.Platform;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Version;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.intl.Countries;


public class BTFeaturedElementHandler implements SimpleElementHandler {

    private static final String SERIES_NUMBER_ATTR = "series_number";
    private static final String EPISODE_NUMBER_ATTR = "episode_number";
    private static final String FEATURE_SRC_ATTR = "src";
    private static final String PRODUCT_URI_PREFIX = "http://featured.bt.com/products/";
    private static final String BT_FEATURED_IMAGE_URI = "http://portal.vision.bt.com/btvo/";
    private static final String BT_FEATURED_GENRE_URI = "http://featured.bt.com/genres/";
    public  static final String CURIE_PREFIX = "btfeatured:";

    private static final String RATING_ATTR = "rating";
    private static final String IMAGE_ATTR = "packshot";
    private static final String DESCRIPTION_ATTR = "description";
    private static final String RELEASE_ID_ATTR = "releaseid";
    private static final String SYNOPSIS_ATTR = "synopsis";


    private static final String ITEM_SUBGENRE = "subGenre";
    private static final String COLLECTION_SUBGENRE = "subgenre";

    private static final String SERVICE_ELEMENT = "service";
    private static final String FEATURE_ELEMENT = "feature";

    private static final String SERVICE_PLATFORM_NAME = "name";

    private static final String PLATFORM_YOUVIEW = "YOU";
    private static final String PLATFORM_BTVISION = "CAR";

 
    private static final Logger log = LoggerFactory.getLogger(BTFeaturedElementHandler.class);

    @Override
    public Optional<Content> handle(Element element, @Nonnull Optional<Container> parent) {
        if (!(element instanceof BTFeaturedProductElement)) {
            return Optional.absent();
        }
        BTFeaturedProductElement product = (BTFeaturedProductElement)element;
        
        log.debug("Parsing product "+product);
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
           container.setDescription(collectionElement.getAttributeValue(DESCRIPTION_ATTR));
           setGenres(container, collectionElement, COLLECTION_SUBGENRE);
           
           return Optional.<Content>of(container);
        }
        else if (product.isSeries()) {
            log.info("Got series "+product.getTitle()+" id is "+product.getProductId()+" parent is "+parent);
            Series series = new Series(getUriFrom(product.getProductId()), getCurieFrom(product.getProductId()), Publisher.BT_FEATURED_CONTENT);
            
            Element seriesElement = product.getSeries();
            
            setCommonFields(series, product, seriesElement);
            series.setDescription(seriesElement.getAttributeValue(DESCRIPTION_ATTR));
            setGenres(series, seriesElement, COLLECTION_SUBGENRE);
            
            return Optional.<Content>of(series);
        }
        
        Element assetElement = product.getAsset();
      
        log.info("Got product "+product.getTitle()+" id is "+product.getProductId()+" parent is "+parent);
        Item item;
        if (parent.isPresent() && parent.get() instanceof Series) {
            Episode episode = new Episode(getUriFrom(product.getProductId()), getCurieFrom(product.getProductId()), Publisher.BT_FEATURED_CONTENT);
            setSeriesAndEpsiode(assetElement, episode);

            item = episode;
        }
        else {
            item = new Item(getUriFrom(product.getProductId()), getCurieFrom(product.getProductId()), Publisher.BT_FEATURED_CONTENT);
        }
        
        setCommonFields(item, product, assetElement);
        item.setDescription(assetElement.getAttributeValue(SYNOPSIS_ATTR));
        
        createVersionAndLocationsFor(item, product, assetElement);
        setGenres(item, assetElement, ITEM_SUBGENRE);
        
        if (parent.isPresent()) {
            item.setParentRef(ParentRef.parentRefFrom(parent.get()));
        }
        return Optional.<Content>of(item);
    }


    protected void setSeriesAndEpsiode(Element assetElement, Episode episode) {
        String epsiodeNumber = assetElement.getAttributeValue(EPISODE_NUMBER_ATTR);
        String seriesNumber = assetElement.getAttributeValue(SERIES_NUMBER_ATTR);
        
        if (StringUtils.isNotEmpty(epsiodeNumber)) {
            episode.setEpisodeNumber(Integer.parseInt(epsiodeNumber));
        }
 
        if (StringUtils.isNotEmpty(seriesNumber)) {
            episode.setEpisodeNumber(Integer.parseInt(seriesNumber));
        }
    }


    protected void createVersionAndLocationsFor(Item item, BTFeaturedProductElement product,
            Element assetElement) {
        Elements serviceElements = assetElement.getChildElements(SERVICE_ELEMENT);

        Version version = new Version();
        Builder<Encoding> encodingBuilder = ImmutableSet.builder();
        
        for (int i=0; i<serviceElements.size(); i++) {
            Element serviceElement = serviceElements.get(i);
            Encoding encoding = new Encoding();
            
            Policy policy = new Policy();
            String platform = serviceElement.getAttributeValue(SERVICE_PLATFORM_NAME);
            if (PLATFORM_BTVISION.equalsIgnoreCase(platform)) {
                policy.setPlatform(Platform.BTVISION_CARDINAL);
            }
            else if(PLATFORM_YOUVIEW.equalsIgnoreCase(platform)){
                policy.setPlatform(Platform.YOUVIEW);
            }
            else {
                log.error("Unrecognised platform for item "+product+" '"+platform+"'");
            }
            
            policy.setAvailableCountries(ImmutableSet.of(Countries.GB));
            policy.setAvailabilityStart(product.getAvailabilityStart());
            policy.setAvailabilityEnd(product.getAvailabilityEnd());
            
            Location location = new Location();
            location.setPolicy(policy);
            
            Element featureElement = serviceElement.getFirstChildElement(FEATURE_ELEMENT);
            if (featureElement != null) {
                location.setUri(featureElement.getAttributeValue(RELEASE_ID_ATTR));
                encoding.setCanonicalUri(featureElement.getAttributeValue(FEATURE_SRC_ATTR));
            }

            encoding.setAvailableAt(ImmutableSet.of(location));
            
            encodingBuilder.add(encoding);
        }

        setDuration(assetElement, version, product);
        
        version.setManifestedAs(encodingBuilder.build());
        item.setVersions(ImmutableSet.of(version));
    }


    private void setDuration(Element assetElement, Version version, BTFeaturedProductElement product) {
        String durationString = assetElement.getAttributeValue("duration");
        if (StringUtils.isEmpty(durationString)) {
            log.warn("Missing duration for product "+product);
            return;
        }
        
        String[] parts = durationString.split(":");
        if (parts.length != 2) {
            log.error("Invalid duration format, expecting hh:mm, got '"+durationString+"' for product "+product);
            return;
        }

        try {
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            
            int durationSeconds = ((hours * 60) + minutes)*60;
            Duration duration = Duration.standardSeconds(durationSeconds);
            version.setDuration(duration);
            version.setPublishedDuration(durationSeconds);
        }
        catch (NumberFormatException nfe) {
            log.error("Invalid duration value, expecting hh:mm got '"+durationString+"' for product "+product);
        }
    }


    protected void setGenres(Content content, Element described, String attributeName) {
        List<String> genreList = Arrays.asList(described.getAttributeValue(attributeName).split(","));

        content.setGenres(
                Iterables.transform(genreList,
                        new Function<String, String>() {
                            @Override
                            @Nullable
                            public String apply(@Nullable String genre) {
                                return BT_FEATURED_GENRE_URI+genre;
                            }
                        }
                ));
    }


    private void setCommonFields(Content content, BTFeaturedProductElement product, Element described) {
        content.setTitle(product.getTitle());
        content.setImage(BT_FEATURED_IMAGE_URI+described.getAttributeValue(IMAGE_ATTR));
        
        String rating = described.getAttributeValue(RATING_ATTR);
        content.setCertificates(ImmutableList.of(new Certificate(rating, Countries.GB)));
    }
    
    private String getCurieFrom(String productId) {
        return CURIE_PREFIX+productId;
    }

    private String getUriFrom(String productId) {
        return PRODUCT_URI_PREFIX + productId;
    }
}
