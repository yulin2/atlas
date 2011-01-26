package org.atlasapi.remotesite.itunes;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.itunes.ItunesAdapterHelper.ItunesRegion;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.collect.Maps;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.http.SimpleHttpClient;

public class ItunesSeriesFinder {
    
    Pattern seasonNumberPattern = Pattern.compile(".*Season ([0-9]+)$");
    Pattern seriesNumberPattern = Pattern.compile(".*Series ([0-9]+)$");
    
    private final SimpleHttpClient client;
    private final AdapterLog log;
    private final ItunesAdapterHelper helper;

    public ItunesSeriesFinder(SimpleHttpClient client, AdapterLog log, ItunesAdapterHelper helper) {
        this.client = client;
        this.log = log;
        this.helper = helper;
    }
    
    public Map<Long, Maybe<Series>> findSeries(String brandId, ItunesRegion region) {
        Map<Long, Maybe<Series>> seriesIdToSeries = Maps.newHashMap();
        String seriesSearchUri = ItunesAdapterHelper.LOOKUP_URL_BASE + region.getSearchArgument() + "&id=" + brandId + "&entity=tvSeason";
        
        try {
            String contents = client.getContentsOf(seriesSearchUri);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readValue(contents, JsonNode.class);
            JsonNode resultsNode = rootNode.path("results");
            
            for (JsonNode seriesNode : resultsNode) {
                if (isSeriesNode(seriesNode)) {
                    try {
                        String seriesTitle = seriesNode.path("collectionName").getTextValue();
                        Integer seriesNumber = getSeriesNumber(seriesTitle);
                        long seriesId = seriesNode.path("collectionId").getNumberValue().longValue();
                        Series series = null;
                        
                        if (seriesNumber != null) {
                            String seriesUrl = seriesNode.path("collectionViewUrl").getTextValue();
                            
                            String seriesThumbnail = seriesNode.path("artworkUrl100").getTextValue();
                            
                            /*JsonNode priceNode = seriesNode.path("collectionPrice");
                            if (priceNode != null && priceNode.getNumberValue().doubleValue() > 0) {
                                Number seriesPrice = priceNode.getNumberValue();
                                String currency = seriesNode.path("currency").getTextValue();
                                Price price = new Price(Currency.getInstance(currency), seriesPrice.doubleValue());
                            }*/
                            
                            series = new Series(seriesUrl, helper.getCurie(seriesId), Publisher.ITUNES);
                            series.setTitle(seriesTitle);
                            series.withSeriesNumber(seriesNumber);
                            series.setThumbnail(seriesThumbnail);
                            series.setImage(seriesThumbnail);
                        }
                        seriesIdToSeries.put(seriesId, Maybe.fromPossibleNullValue(series));
                    }
                    catch (Exception e) {
                        log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(ItunesBrandAdapter.class)
                                         .withDescription("Error when processing series from " + seriesNode.toString()));
                    }
                }
            }
        }
        catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(ItunesBrandAdapter.class).withUri(seriesSearchUri));
        }
        
        return seriesIdToSeries;
    }
    
    private Integer getSeriesNumber(String seriesTitle) {
        Matcher seriesNumberMatcher = seriesNumberPattern.matcher(seriesTitle);
        if (seriesNumberMatcher.matches()) {
            return Integer.valueOf(seriesNumberMatcher.group(1));
        }
        
        Matcher seasonNumberMatcher = seasonNumberPattern.matcher(seriesTitle);
        if (seasonNumberMatcher.matches()) {
            return Integer.valueOf(seasonNumberMatcher.group(1));
        }
        
        return null;
    }
    
    private boolean isSeriesNode(JsonNode node) {
        String wrapperType = node.path("wrapperType").getTextValue();
        if ("collection".equals(wrapperType)) {
            String collectionType = node.path("collectionType").getTextValue();
            if ("TV Season".equals(collectionType)) {
                return true;
            }
        }
        return false;
    }
}
