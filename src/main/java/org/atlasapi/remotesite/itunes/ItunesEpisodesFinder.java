//package org.atlasapi.remotesite.itunes;
//
//import java.util.Currency;
//import java.util.List;
//import java.util.Map;
//
//import org.atlasapi.media.TransportSubType;
//import org.atlasapi.media.TransportType;
//import org.atlasapi.media.entity.Encoding;
//import org.atlasapi.media.entity.Episode;
//import org.atlasapi.media.entity.Location;
//import org.atlasapi.media.entity.Policy;
//import org.atlasapi.media.entity.Policy.RevenueContract;
//import org.atlasapi.media.entity.Publisher;
//import org.atlasapi.media.entity.Series;
//import org.atlasapi.media.entity.Version;
//import org.atlasapi.persistence.logging.AdapterLog;
//import org.atlasapi.persistence.logging.AdapterLogEntry;
//import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
//import org.atlasapi.remotesite.itunes.ItunesAdapterHelper.ItunesRegion;
//import org.codehaus.jackson.JsonNode;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.joda.time.Duration;
//
//import com.google.common.collect.ImmutableList;
//import com.google.common.collect.ImmutableSet;
//import com.google.common.collect.Lists;
//import com.metabroadcast.common.base.Maybe;
//import com.metabroadcast.common.currency.Price;
//import com.metabroadcast.common.http.SimpleHttpClient;
//import com.metabroadcast.common.intl.Country;
//
//public class ItunesEpisodesFinder {
//    
//    private final SimpleHttpClient client;
//    private final AdapterLog log;
//    private final ItunesAdapterHelper helper;
//
//    public ItunesEpisodesFinder(SimpleHttpClient client, AdapterLog log, ItunesAdapterHelper helper) {
//        this.client = client;
//        this.log = log;
//        this.helper = helper;
//    }
//    
//    public List<Episode> findEpisodes(String brandId, ItunesRegion region, Map<Long, Maybe<Series>> seriesIdToSeries) {
//        List<Episode> episodes = Lists.newArrayList();
//        
//        for(Long seriesId : seriesIdToSeries.keySet()) {
//            String episodesSearchUri = ItunesAdapterHelper.LOOKUP_URL_BASE + region.getSearchArgument() + "&id=" + seriesId + "&entity=tvEpisode";
//            try {
//                String contents = client.getContentsOf(episodesSearchUri);
//                ObjectMapper mapper = new ObjectMapper();
//                JsonNode rootNode = mapper.readValue(contents, JsonNode.class);
//                JsonNode resultsNode = rootNode.path("results");
//                
//                for (JsonNode episodeNode : resultsNode) {
//                    if (isEpisodeNode(episodeNode)) {
//                        Episode episode = extractEpisode(episodeNode, region.getCountry());
//                        
//                        if (episode != null) {
//                            episodes.add(episode);
//                            
//                            Maybe<Series> maybeSeries = seriesIdToSeries.get(seriesId);
//                            if (maybeSeries.hasValue()) {
//                                Series series = maybeSeries.requireValue();
//                                if (series.getSeriesNumber() != null) {
//                                    episode.setSeriesNumber(series.getSeriesNumber());
//                                }
//                                series.addContents(ImmutableList.of(episode));
//                            }
//                        }
//                    }
//                }
//            }
//            catch (Exception e) {
//                log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(ItunesBrandAdapter.class).withUri(episodesSearchUri));
//            }
//        }
//        
//        return episodes;
//    }
//    
//    private Episode extractEpisode(JsonNode episodeNode, Country availableCountry) {
//        try {
//            String episodeUri = episodeNode.path("trackViewUrl").getTextValue();
//            long episodeId = episodeNode.path("trackId").getNumberValue().longValue();
//            
//            Episode episode = new Episode(episodeUri, helper.getCurie(episodeId), Publisher.ITUNES);
//            episode.setTitle(episodeNode.path("trackName").getTextValue());
//            
//            String imageUrl = episodeNode.path("artworkUrl30").getTextValue();
//            episode.setImage(imageUrl);
//            
//            String thumbnailUrl = episodeNode.path("artworkUrl100").getTextValue();
//            episode.setThumbnail(thumbnailUrl);
//            
//            Integer episodeNumber = episodeNode.path("trackNumber").getNumberValue().intValue();
//            episode.setEpisodeNumber(episodeNumber);
//            
//            long seriesId = episodeNode.path("collectionId").getNumberValue().longValue();
//            long trackTimeMillis = episodeNode.path("trackTimeMillis").getNumberValue().longValue();
//            
//            JsonNode priceNode = episodeNode.path("trackPrice");
//            
//            Version version = new Version();
//            version.setDuration(new Duration(trackTimeMillis));
//            version.setProvider(Publisher.ITUNES);
//            Encoding encoding = new Encoding();
//            
//            Policy policy = new Policy();
//            policy.setAvailableCountries(ImmutableSet.of(availableCountry));
//            if (priceNode != null && priceNode.getNumberValue().doubleValue() > 0) {
//                double amount = priceNode.getNumberValue().doubleValue();
//                String currencyCode = episodeNode.path("currency").getTextValue();
//                policy.setPrice(new Price(Currency.getInstance(currencyCode), amount));
//                policy.setRevenueContract(RevenueContract.PAY_TO_BUY);
//            }
//            else {
//                policy.setRevenueContract(RevenueContract.FREE_TO_VIEW);
//            }
//            
//            Location linkLocation = new Location();
//            linkLocation.setUri(episodeUri);
//            linkLocation.setTransportType(TransportType.LINK);
//            linkLocation.setTransportSubType(TransportSubType.HTTP);
//            linkLocation.setPolicy(policy);
//            
//            Location appLocation = new Location();
//            appLocation.setUri(getItunesLink(seriesId, episodeId));
//            appLocation.setTransportType(TransportType.APPLICATION);
//            appLocation.setTransportSubType(TransportSubType.ITUNES);
//            appLocation.setPolicy(policy);
//            
//            encoding.addAvailableAt(appLocation);
//            encoding.addAvailableAt(linkLocation);
//            version.addManifestedAs(encoding);
//            episode.setVersions(ImmutableSet.of(version));
//            
//            return episode;
//        }
//        catch (Exception e) {
//            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(ItunesBrandAdapter.class));
//        }
//        
//        return null;
//    }
//    
//    private boolean isEpisodeNode(JsonNode node) {
//        String wrapperType = node.get("wrapperType").getTextValue();
//        if ("track".equals(wrapperType)) {
//            String artistType = node.path("kind").getTextValue();
//            if ("tv-episode".equals(artistType)) {
//                return true;
//            }
//        }
//        return false;
//    }
//    
//    private String getItunesLink(long seriesId, long episodeId) {
//        return "itms://phobos.apple.com/WebObjects/MZStore.woa/wa/viewTVSeason?i=" + episodeId + "&id=" + seriesId;
//    }
//}
