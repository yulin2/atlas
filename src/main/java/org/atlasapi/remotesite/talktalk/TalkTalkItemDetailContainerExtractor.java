package org.atlasapi.remotesite.talktalk;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemTypeType;

import com.google.common.base.Optional;

/**
 * Creates {@link Container}s from {@link ItemDetailType}s.
 */
public class TalkTalkItemDetailContainerExtractor {

    private final String BRAND_URI_PATTERN = "http://talktalk.net/brands/%s";
    private final String SERIES_URI_PATTERN = "http://talktalk.net/series/%s";
    
    private static final TalkTalkDescriptionExtractor descriptionExtractor = new TalkTalkDescriptionExtractor();
    private static final TalkTalkImagesExtractor imagesExtractor = new TalkTalkImagesExtractor();
    private static final TalkTalkGenresExtractor genresExtractor = new TalkTalkGenresExtractor();
    
    public Brand extractBrand(ItemDetailType detail) {
        checkArgument(ItemTypeType.BRAND.equals(detail.getItemType()), 
                "Can't extract Brand from non-BRAND Item type");
        Brand brand = new Brand();
        brand.setCanonicalUri(String.format(BRAND_URI_PATTERN, detail.getId()));
        return setCommonContainerFields(detail, brand);
    }

    private <C extends Container> C setCommonContainerFields(ItemDetailType detail, C container) {
        container.setPublisher(Publisher.TALK_TALK);
        container.setTitle(detail.getTitle());
        
        container = descriptionExtractor.extractDescriptions(container, detail.getSynopsisList());
        container.setImages(imagesExtractor.extract(detail));
        container.setGenres(genresExtractor.extract(detail));
        return container;
    }

    public Series extractSeries(ItemDetailType detail, Optional<Brand> brand) {
        checkArgument(ItemTypeType.SERIES.equals(detail.getItemType()), 
                "Can't extract Series from non-SERIES Item type");
        Series series = new Series();
        series.setCanonicalUri(String.format(SERIES_URI_PATTERN, detail.getId()));
        if (brand.isPresent()) {
            series.setParent(brand.get());
        }
        return setCommonContainerFields(detail, series)
                .withSeriesNumber(extractSeriesNumber(detail));
    }

    private Integer extractSeriesNumber(ItemDetailType entity) {
        if (entity.getTitle() == null) {
            return null;
        }
        //Should match e.g: No Ordinary Family S1
        Pattern seriesNumberPattern = Pattern.compile(".*S(\\d+)$");
        Matcher matcher = seriesNumberPattern.matcher(entity.getTitle());
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }
    
    
}
