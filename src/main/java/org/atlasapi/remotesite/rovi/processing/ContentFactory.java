package org.atlasapi.remotesite.rovi.processing;

import static com.google.common.base.Preconditions.checkArgument;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Item;
import org.atlasapi.remotesite.rovi.model.RoviShowType;

import com.google.api.client.repackaged.com.google.common.base.Throwables;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;


public class ContentFactory {
    
    private static final BiMap<RoviShowType, Class<? extends Content>> showTypeToClass = HashBiMap.create();
    
    static {
        showTypeToClass.put(RoviShowType.MOVIE, Film.class);
        showTypeToClass.put(RoviShowType.SERIES_EPISODE, Episode.class);
        showTypeToClass.put(RoviShowType.SERIES_MASTER, Brand.class);
        showTypeToClass.put(RoviShowType.OTHER, Item.class);
    }
    
    public static Content createContent(RoviShowType showType) {
        checkShowType(showType);
        
        try {
            return showTypeToClass.get(showType).newInstance();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        } 
    }

    public static boolean hasCorrectType(Content content, RoviShowType showType) {
        checkShowType(showType);
        
        Class<? extends Content> clazz = showTypeToClass.get(showType);
        return content.getClass().isAssignableFrom(clazz);
    }

    private static void checkShowType(RoviShowType showType) {
        checkArgument(showTypeToClass.containsKey(showType), "Unexpected show type: " + showType);
    }
}
