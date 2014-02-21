package org.atlasapi.remotesite.rovi.processing;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Item;
import org.atlasapi.remotesite.rovi.model.RoviShowType;


public class ContentCreator {
    
    public static Content createContent(RoviShowType showType) {
        switch (showType) {
            case MOVIE:
                return new Film();
            case SERIES_EPISODE:
                return new Episode();
            case SERIES_MASTER:
                return new Brand();
            case OTHER:    
                return new Item();
            default:    
                throw new RuntimeException("Unexpected show type: " + showType);
        }
    }

}
