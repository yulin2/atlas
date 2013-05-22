package org.atlasapi.remotesite.lovefilm;

import static org.atlasapi.remotesite.lovefilm.LoveFilmCsvColumn.ITEM_NAME;
import static org.atlasapi.remotesite.lovefilm.LoveFilmCsvColumn.SERIES_ID;
import static org.atlasapi.remotesite.lovefilm.LoveFilmCsvColumn.SHOW_ID;
import static org.atlasapi.remotesite.lovefilm.LoveFilmCsvColumn.SKU;
import static org.atlasapi.remotesite.lovefilm.LoveFilmDataRowUtils.LOVEFILM_URI_PATTERN;
import static org.atlasapi.remotesite.lovefilm.LoveFilmDataRowUtils.SEASON_RESOURCE_TYPE;
import static org.atlasapi.remotesite.lovefilm.LoveFilmDataRowUtils.SHOW_RESOURCE_TYPE;

import java.util.Map;

import org.atlasapi.remotesite.lovefilm.LoveFilmData.LoveFilmDataRow;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
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
            String brandUri = generateUri(SHOW_ID.valueFrom(row), SHOW_RESOURCE_TYPE);
            uriTitleMapping.put(uri, getTitle(row));
            brandSeriesMapping.put(brandUri, uri);
        }
        if (LoveFilmDataRowUtils.isEpisode(row)) {
            String sku = SKU.valueFrom(row);
            String uri = generateUri(sku, LoveFilmDataRowUtils.EPISODE_RESOURCE_TYPE);
            String seriesUri = generateUri(SERIES_ID.valueFrom(row), SEASON_RESOURCE_TYPE);
            uriTitleMapping.put(uri, getTitle(row));
            seriesEpisodeMapping.put(seriesUri, uri);
        }
    }

    private String getTitle(LoveFilmDataRow row) {
        return ITEM_NAME.valueFrom(row);
    }

    @Override
    public void finish() {
        for (String brand : brandSeriesMapping.keySet()) {
            if (brandSeriesMapping.get(brand).size() == 1) {
                String series = Iterables.getOnlyElement(brandSeriesMapping.get(brand));
                if (seriesEpisodeMapping.get(series).size() == 1) {
                    String episode = Iterables.getOnlyElement(seriesEpisodeMapping.get(series));
                    if (uriTitleMapping.get(brand).equals(uriTitleMapping.get(series))) {
                        if (uriTitleMapping.get(brand).equals(uriTitleMapping.get(episode))) {
                            brandTypeMapping.put(brand, BrandType.STAND_ALONE_EPISODE);
                        } else {
                            brandTypeMapping.put(brand, BrandType.TOP_LEVEL_SERIES);
                        }
                    } else {
                        brandTypeMapping.put(brand, BrandType.BRAND_SERIES_EPISODE);
                    }
                } else {
                    if (uriTitleMapping.get(brand).equals(uriTitleMapping.get(series))) {
                        brandTypeMapping.put(brand, BrandType.TOP_LEVEL_SERIES);
                    } else {
                        brandTypeMapping.put(brand, BrandType.BRAND_SERIES_EPISODE);
                    }
                }
            } else {
                brandTypeMapping.put(brand, BrandType.BRAND_SERIES_EPISODE);
            }
        }
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
