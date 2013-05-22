package org.atlasapi.remotesite.lovefilm;

import static org.atlasapi.remotesite.lovefilm.LoveFilmCsvColumn.ITEM_NAME;
import static org.atlasapi.remotesite.lovefilm.LoveFilmCsvColumn.SKU;
import static org.atlasapi.remotesite.lovefilm.LoveFilmDataRowUtils.LOVEFILM_URI_PATTERN;

import java.util.Map;

import org.atlasapi.remotesite.lovefilm.LoveFilmData.LoveFilmDataRow;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;


public class DefaultLoveFilmBrandProcessor implements LoveFilmBrandProcessor {
    
    private final Map<String, String> uriTitleMapping = Maps.newHashMap();
    private final Multimap<String, String> brandSeriesMapping = ArrayListMultimap.create();
    private final Multimap<String, String> seriesEpisodeMapping = ArrayListMultimap.create();
    private final Map<String, BrandType> brandTypeMapping = Maps.newHashMap();

    @Override
    public void prepare() {
    }

    @Override
    public void handle(LoveFilmDataRow row) {
        if (LoveFilmDataRowUtils.isBrand(row)) {
            String sku = SKU.valueFrom(row);
            String uri = generateUri(sku, LoveFilmDataRowUtils.SHOW_RESOURCE_TYPE);
            uriTitleMapping.put(uri, getTitle(row));
        }
        if (LoveFilmDataRowUtils.isSeries(row)) {
            String sku = SKU.valueFrom(row);
            String uri = generateUri(sku, LoveFilmDataRowUtils.SEASON_RESOURCE_TYPE);
            uriTitleMapping.put(uri, getTitle(row));
            // add entry for brand -> series
        }
        if (LoveFilmDataRowUtils.isEpisode(row)) {
            String sku = SKU.valueFrom(row);
            String uri = generateUri(sku, LoveFilmDataRowUtils.EPISODE_RESOURCE_TYPE);
            uriTitleMapping.put(uri, getTitle(row));
            // add entry for series -> episode
        }
    }

    private String getTitle(LoveFilmDataRow row) {
        return ITEM_NAME.valueFrom(row);
    }


    @Override
    public void finish() {
        
    }

    @Override
    public BrandType getBrandType(String uri) {
        return brandTypeMapping.get(uri);
    }

    private String generateUri(String id, String resource) {
        String uri = String.format(LOVEFILM_URI_PATTERN, resource, id);
        return uri;
    }
}
