package org.atlasapi.remotesite.netflix;

import java.util.Set;

import nu.xom.Element;

import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Film;
import org.atlasapi.remotesite.ContentExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.inject.internal.ImmutableSet;

public class NetflixXmlElementContentExtractor implements ContentExtractor<Element, Set<? extends Content>> {
    private static final String TYPE_ATTRIBUTE = "type";
    private static final String ID_ATTRIBUTE = "id";
    
    private final NetflixContentExtractor<Film> filmExtractor;
    private final NetflixContentExtractor<Container> brandExtractor;
    private final NetflixContentExtractor<? extends Content> episodeExtractor;
    
    private final Logger log = LoggerFactory.getLogger(NetflixXmlElementContentExtractor.class);
    
    public NetflixXmlElementContentExtractor(NetflixContentExtractor<Film> filmExtractor, NetflixContentExtractor<Container> brandExtractor, NetflixContentExtractor<? extends Content> episodeExtractor) {
        this.filmExtractor = filmExtractor;
        this.brandExtractor = brandExtractor;
        this.episodeExtractor = episodeExtractor;
    }
    
    @Override
    public Set<? extends Content> extract(Element source) {
        try {
            String type = getType(source);
            int id = getId(source);

            if (type.equals("movie")) {
                return filmExtractor.extract(source, id);
            } else if (type.equals("show")) {
                return brandExtractor.extract(source, id);
            } else if (type.equals("episode")) {
                return episodeExtractor.extract(source, id);
            }
            log.warn("content type of element recognised but not parsed: " + source);
            return ImmutableSet.of();
        } catch (Exception e) {
            Throwables.propagate(e);
            // never reaches here
            return null;
        }
    }

    private int getId(Element source) throws IdNotFoundException {
        for (int i = 0; i < source.getAttributeCount(); i++) {
            if (source.getAttribute(i).getLocalName().equals(ID_ATTRIBUTE)) {
                return Integer.parseInt(source.getAttribute(i).getValue());
            }
        }
        throw new IdNotFoundException(source);
    }

    private String getType(Element source) throws TypeNotFoundException {
        for (int i = 0; i < source.getAttributeCount(); i++) {
            if (source.getAttribute(i).getLocalName().equals(TYPE_ATTRIBUTE)) {
                return source.getAttribute(i).getValue();
            }
        }
        throw new TypeNotFoundException(source);
    }
}
