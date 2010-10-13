package org.atlasapi.remotesite.itv;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.ContentExtractor;

import com.google.common.collect.ImmutableSet;

public class ItvMercuryBrandExtractor implements ContentExtractor<Map<String, Object>, Brand> {
    
    private final ItvMercuryEpisodeExtractor episodeExtractor = new ItvMercuryEpisodeExtractor();
    
    @SuppressWarnings("unchecked")
    @Override
    public Brand extract(Map<String, Object> values) {
        Brand brand = null;
        
        if (values.containsKey("Result")) {
            Map<String, Object> result = (Map<String, Object>) values.get("Result");
            String id = (String) result.get("Key");
            String uri = ItvMercuryBrandAdapter.BASE_URL+id;
            String curie = "itv:"+id;
            brand = new Brand(uri, curie, Publisher.ITV);
            
            if (result.containsKey("Details")) {
                List<Map<String, Object>> programmes = (List<Map<String, Object>>) result.get("Details");
                
                for (Map<String, Object> programme: programmes) {
                    Map<String, Object> progInfo = (Map<String, Object>) programme.get("Programme");
                    
                    if (progInfo.containsKey("Programme")) {
                        Map<String, Object> info = (Map<String, Object>) progInfo.get("Programme");
                        brand.setTitle((String) info.get("Title"));
                    }
                    
                    brand.setDescription((String) progInfo.get("ShortSynopsis"));
                    brand.setGenres(genres((String) progInfo.get("Genres")));
                    brand.addAlias((String) progInfo.get("AdditionalContentUri"));
                    brand.setImage((String) progInfo.get("ImageUri"));
                    
                    List<Map<String, Object>> episodes = (List<Map<String, Object>>) result.get("Episodes");
                    for (Map<String, Object> episodeInfo: episodes) {
                        Episode episode = episodeExtractor.extract(episodeInfo);
                        if (episode != null) {
                            brand.addItem(episode);
                        }
                    }
                }
            }
        }
        
        return brand;
    }
    
    static protected Set<String> genres(String genres) {
        if (genres != null && genres.contains(".")) {
            genres = genres.split(".")[0];
        }
        return ImmutableSet.of(genres);
    }
}
