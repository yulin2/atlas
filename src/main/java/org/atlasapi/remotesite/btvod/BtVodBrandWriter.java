package org.atlasapi.remotesite.btvod;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.remotesite.ContentMerger;
import org.atlasapi.remotesite.ContentMerger.MergeStrategy;
import org.atlasapi.remotesite.btvod.BtVodData.BtVodDataRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.scheduling.UpdateProgress;

/**
 * Create a {@link Brand} from a {@link BtVodDataRow}. This may either be
 * from brand information contained explicitly in a row, or synthesized 
 * from episode data
 * 
 * @author tom
 *
 */
public class BtVodBrandWriter implements BtVodDataProcessor<UpdateProgress> {

    private static final Pattern BRAND_TITLE_FROM_EPISODE_PATTERN = Pattern.compile("^(.*)\\ S[0-9]+\\-E.*");
    private static final Logger log = LoggerFactory.getLogger(BtVodBrandWriter.class);
    private static final boolean CONTINUE = true;
    
    private final Map<String, ParentRef> processedBrands = Maps.newHashMap();
    private final ContentWriter writer;
    private final ContentResolver resolver;
    private final Publisher publisher;
    private final String uriPrefix;
    private final ContentMerger contentMerger;
    private final BtVodContentListener listener;
    private final Set<String> processedRows;
    private final BtVodDescribedFieldsExtractor describedFieldsExtractor;
    private UpdateProgress progress = UpdateProgress.START;
    
    public BtVodBrandWriter(ContentWriter writer, ContentResolver resolver,
            Publisher publisher, String uriPrefix, BtVodContentListener listener, 
            BtVodDescribedFieldsExtractor describedFieldsExtractor, Set<String> processedRows) {
        this.describedFieldsExtractor = checkNotNull(describedFieldsExtractor);
        this.listener = checkNotNull(listener);
        this.writer = checkNotNull(writer);
        this.resolver = checkNotNull(resolver);
        this.publisher = checkNotNull(publisher);
        this.uriPrefix = checkNotNull(uriPrefix);
        this.contentMerger = new ContentMerger(MergeStrategy.REPLACE);
        this.processedRows = checkNotNull(processedRows);
    }
    
    @Override
    public boolean process(BtVodDataRow row) {
        UpdateProgress thisProgress = UpdateProgress.FAILURE;
        try {
            String brandId = row.getColumnValue(BtVodFileColumn.BRANDIA_ID);
            if ( ( Strings.isNullOrEmpty(brandId) && !shouldSynthesizeBrand(row))
                    || processedBrands.containsKey(brandId)
                    || processedRows.contains(getKey(row))) {
                thisProgress = UpdateProgress.SUCCESS;
                return CONTINUE;
            }
            Brand brand = brandFrom(row);
            write(brand);
            listener.onContent(brand, row);
            processedBrands.put(brandId, ParentRef.parentRefFrom(brand));
            thisProgress = UpdateProgress.SUCCESS;
        } catch (Exception e) {
            log.error("Failed to process row " + row.toString(), e);
        }
        finally {
            progress = progress.reduce(thisProgress);
        }
        return CONTINUE;
    }

    private String getKey(BtVodDataRow row) {
        if (shouldSynthesizeBrand(row)) {
            return brandTitleFromEpisodeTitle(row.getColumnValue(BtVodFileColumn.PRODUCT_TITLE));
        } else {
            return row.getColumnValue(BtVodFileColumn.PRODUCT_ID);
        }
    }
    
    private String brandTitleFromEpisodeTitle(String title) {
        Matcher matcher = BRAND_TITLE_FROM_EPISODE_PATTERN.matcher(title);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    private boolean shouldSynthesizeBrand(BtVodDataRow row) {
        String seriesNumber = row.getColumnValue(BtVodFileColumn.SERIES_NUMBER);
        return Strings.isNullOrEmpty(row.getColumnValue(BtVodFileColumn.BRANDIA_ID))
                && seriesNumber != null 
                && Ints.tryParse(seriesNumber) != null;
    }

    private void write(Brand extracted) {
        Maybe<Identified> existing = resolver
                .findByCanonicalUris(ImmutableSet.of(extracted.getCanonicalUri()))
                .getFirstValue();
        
        if (existing.hasValue()) {
            Container merged = contentMerger.merge((Brand) existing.requireValue(), 
                                                   extracted);
            writer.createOrUpdate(merged);
        } else {
            writer.createOrUpdate(extracted);
        }
        
    }

    private Brand brandFrom(BtVodDataRow row) {
        Brand brand = new Brand(uriFor(row), null, publisher);
        if (shouldSynthesizeBrand(row)) {
            brand.setTitle(brandTitleFromEpisodeTitle(row.getColumnValue(BtVodFileColumn.PRODUCT_TITLE)));
        } else {
            brand.setTitle(row.getColumnValue(BtVodFileColumn.BRAND_TITLE));
        }
        describedFieldsExtractor.setDescribedFieldsFrom(row, brand);
        return brand;
    }
    
    private String uriFor(BtVodDataRow row) {
        if (shouldSynthesizeBrand(row)) {
          return uriPrefix + "synthesized/brands/" + getKey(row);
        } else {
            return uriPrefix + "brands/" + row.getColumnValue(BtVodFileColumn.BRANDIA_ID);
        }
    }

    @Override
    public UpdateProgress getResult() {
        return progress;
    }
    
    public ParentRef getBrandRefFor(String brandIaId) {
        return processedBrands.get(brandIaId);
    }
    
}