package org.atlasapi.remotesite.netflix;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.CrewMember.Role;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Sets;
import com.google.inject.internal.ImmutableMap;
import com.google.inject.internal.Lists;

import nu.xom.Element;

public abstract class NetflixContentExtractor<T extends Content> {
    
    private static final String ID_ATTRIBUTE = "id";
    private static final String TYPE_ATTRIBUTE = "type";
    private static final String GENRES_KEY = "genres";
    private static final String PEOPLE_KEY = "people";
    private static final String NAME_KEY = "name";
    private static final String GENRES_URL_PREFIX = "http://gb.netflix.com/genres/";
    private static final String PEOPLE_URL_PREFIX = "http://gb.netflix.com/people/";

    private static final Map<String, Role> TYPE_ROLE_MAPPING = ImmutableMap.<String, Role>builder()
            .put("actor", Role.ACTOR)
            .put("creator", Role.WRITER)
            .put("director", Role.DIRECTOR)
            .build();
    
    abstract Set<T> extract(Element source, int id);

    Iterable<String> getGenres(Element contentElement) throws ElementNotFoundException {
        Element genresElement = contentElement.getFirstChildElement(GENRES_KEY);
        if (genresElement != null) {
            Set<String> genres = Sets.newHashSet();
            for (int i = 0; i < genresElement.getChildElements().size(); i++) {
                String netflixGenre = genresElement.getChildElements().get(i).getValue();
                netflixGenre = StringEscapeUtils.unescapeHtml(netflixGenre);
                netflixGenre = CharMatcher.WHITESPACE.removeFrom(netflixGenre);
                genres.add(GENRES_URL_PREFIX + netflixGenre.toLowerCase());
            }
            return genres;
        }
        throw new ElementNotFoundException(contentElement, GENRES_KEY);
    }
    
    List<CrewMember> getPeople(Element contentElement) throws ElementNotFoundException, IdNotFoundException {
        Element peopleElement = contentElement.getFirstChildElement(PEOPLE_KEY);
        if (peopleElement != null) {
            List<CrewMember> people = Lists.newArrayList();
            for (int i = 0; i < peopleElement.getChildElements().size(); i++) {
                Element personElement = peopleElement.getChildElements().get(i);
                if (personElement != null) {
                    CrewMember person = new CrewMember();
                    person.setCanonicalUri(PEOPLE_URL_PREFIX + getId(personElement));
                    person.withRole(TYPE_ROLE_MAPPING.get(getType(personElement)));
                    
                    Element nameElement = personElement.getFirstChildElement(NAME_KEY);
                    if (nameElement != null) {
                        person.withName(personElement.getValue());
                    }
                    
                    people.add(person);
                }
            }
            return people;
        }
        throw new ElementNotFoundException(contentElement, PEOPLE_KEY);
    }

    private String getType(Element source) throws IdNotFoundException {
        for (int i = 0; i < source.getAttributeCount(); i++) {
            if (source.getAttribute(i).getLocalName().equals(TYPE_ATTRIBUTE)) {
                return source.getAttribute(i).getValue();
            }
        }
        throw new IdNotFoundException(source);
    }

    private int getId(Element source) throws IdNotFoundException {
        for (int i = 0; i < source.getAttributeCount(); i++) {
            if (source.getAttribute(i).getLocalName().equals(ID_ATTRIBUTE)) {
                return Integer.parseInt(source.getAttribute(i).getValue());
            }
        }
        throw new IdNotFoundException(source);
    }
}
