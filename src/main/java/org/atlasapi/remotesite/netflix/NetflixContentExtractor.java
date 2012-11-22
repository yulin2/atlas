package org.atlasapi.remotesite.netflix;

import java.util.List;
import java.util.Map;
import java.util.Set;

import nu.xom.Element;

import org.apache.commons.lang.StringEscapeUtils;
import org.atlasapi.media.entity.Certificate;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Version;
import org.atlasapi.media.entity.CrewMember.Role;
import org.joda.time.Duration;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Sets;
import com.google.inject.internal.ImmutableMap;
import com.google.inject.internal.Lists;
import com.metabroadcast.common.intl.Countries;

public abstract class NetflixContentExtractor<T extends Content> {

    private static final String SYSTEM_ATTRIBUTE = "system";
    private static final String ID_ATTRIBUTE = "id";
    private static final String TYPE_ATTRIBUTE = "type";
    private static final String GENRES_KEY = "genres";
    private static final String PEOPLE_KEY = "people";
    private static final String NAME_KEY = "name";
    private static final String LONG_SYNOPSIS_KEY = "long_synopsis";
    private static final String SHORT_SYNOPSIS_KEY = "short_synopsis";
    private static final String RELEASE_YEAR_KEY = "release_year";
    private static final String DURATION_KEY = "duration";
    public static final String TITLE_KEY = "title";
    private static final String URL_KEY = "url";
    private static final String SHOW_KEY = "show";
    private static final String SEASON_NUMBER_KEY = "season_number";
    private static final String EPISODE_NUMBER_KEY = "episode_number";
    private static final String BBFC = "BBFC";
    private static final String PARENTAL_ADVISORIES_KEY = "parental_advisories";
    private static final String GENRES_URL_PREFIX = "http://gb.netflix.com/genres/";
    private static final String PEOPLE_URL_PREFIX = "http://gb.netflix.com/people/";

    private static final Map<String, Role> TYPE_ROLE_MAPPING = ImmutableMap.<String, Role>builder()
            .put("actor", Role.ACTOR)
            .put("creator", Role.WRITER)
            .put("director", Role.DIRECTOR)
            .build();
    
    abstract Set<T> extract(Element source, int id);

    int getSeriesNumber(Element filmElement) throws ElementNotFoundException {
        Element showElement = filmElement.getFirstChildElement(SHOW_KEY);
        if (showElement != null) {
            Element seasonNumberElement = showElement.getFirstChildElement(SEASON_NUMBER_KEY);
            if (seasonNumberElement != null) {
                return Integer.parseInt(seasonNumberElement.getValue());
            }
            throw new ElementNotFoundException(showElement, SEASON_NUMBER_KEY );
        }
        throw new ElementNotFoundException(filmElement, SHOW_KEY);
    }

    int getEpisodeNumber(Element filmElement) throws ElementNotFoundException {
        Element showElement = filmElement.getFirstChildElement(SHOW_KEY);
        if (showElement != null) {
            Element episodeNumberElement = showElement.getFirstChildElement(EPISODE_NUMBER_KEY );
            if (episodeNumberElement != null) {
                return Integer.parseInt(episodeNumberElement.getValue());
            }
            throw new ElementNotFoundException(showElement, EPISODE_NUMBER_KEY);
        }
        throw new ElementNotFoundException(filmElement, SHOW_KEY);
    }

    Set<Certificate> getCertificates(Element filmElement) throws ElementNotFoundException, AttributeNotFoundException {
        Element parentalAdvisories = filmElement.getFirstChildElement(PARENTAL_ADVISORIES_KEY);
        if (parentalAdvisories != null) {
            Set<Certificate> certificates =  Sets.newHashSet();
            for (int i = 0; i < parentalAdvisories.getChildElements().size(); i++) {
                Element parentalAdvisory = parentalAdvisories.getChildElements().get(i);
                String advisorySystem = advisorySystem(parentalAdvisory); 
                if (advisorySystem != null && advisorySystem.equals(BBFC)) {
                    certificates.add(new Certificate(parentalAdvisory.getValue(), Countries.GB));
                }
            }
            return certificates;
        }
        throw new ElementNotFoundException(filmElement, PARENTAL_ADVISORIES_KEY);
    }

    private String advisorySystem(Element parentalAdvisory) throws AttributeNotFoundException {
        for (int i = 0; i < parentalAdvisory.getAttributeCount(); i++) {
            if (parentalAdvisory.getAttribute(i).getLocalName().equals(SYSTEM_ATTRIBUTE)) {
                return parentalAdvisory.getAttribute(i).getValue();
            }
        }
        throw new AttributeNotFoundException(parentalAdvisory, SYSTEM_ATTRIBUTE);
    }

    String getTitle(Element filmElement) throws ElementNotFoundException {
        Element titleElement = filmElement.getFirstChildElement(TITLE_KEY);
        if (titleElement != null) {
            return titleElement.getValue();
        }
        throw new ElementNotFoundException(filmElement, TITLE_KEY);
    }

    int getYear(Element filmElement) throws ElementNotFoundException {
        Element yearElement = filmElement.getFirstChildElement(RELEASE_YEAR_KEY);
        if (yearElement != null) {
            return Integer.parseInt(yearElement.getValue());
        }
        throw new ElementNotFoundException(filmElement, RELEASE_YEAR_KEY);
    }

    String getDescription(Element filmElement) throws ElementNotFoundException {
        Element synopsisElement = filmElement.getFirstChildElement(LONG_SYNOPSIS_KEY);
        if (synopsisElement != null) {
            return synopsisElement.getValue();
        }
        //fall back to short_synopsis
        Element shortSynopsisElement = filmElement.getFirstChildElement(SHORT_SYNOPSIS_KEY);
        if (shortSynopsisElement != null) {
            return shortSynopsisElement.getValue();
        }
        throw new ElementNotFoundException(filmElement, SHORT_SYNOPSIS_KEY);
    }

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
                CrewMember person = new CrewMember();
                person.setCanonicalUri(PEOPLE_URL_PREFIX + getId(personElement));
                person.withRole(TYPE_ROLE_MAPPING.get(getType(personElement)));

                Element nameElement = personElement.getFirstChildElement(NAME_KEY);
                if (nameElement == null) {
                    throw new ElementNotFoundException(personElement, NAME_KEY);
                }
                person.withName(personElement.getValue().trim());
                people.add(person);
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

    Version getVersion(Element filmElement) throws ElementNotFoundException {
        Element durationElement = filmElement.getFirstChildElement(DURATION_KEY);
        if (durationElement != null) {
            Version version = new Version();
            version.setDuration(Duration.standardSeconds(Integer.parseInt(durationElement.getValue())));
            return version;
        }
        throw new ElementNotFoundException(filmElement, DURATION_KEY);
    }

    private int getId(Element source) throws IdNotFoundException {
        for (int i = 0; i < source.getAttributeCount(); i++) {
            if (source.getAttribute(i).getLocalName().equals(ID_ATTRIBUTE)) {
                return Integer.parseInt(source.getAttribute(i).getValue());
            }
        }
        throw new IdNotFoundException(source);
    }

    String getAlias(Element filmElement) throws ElementNotFoundException {
        Element urlElement = filmElement.getFirstChildElement(URL_KEY);
        if (urlElement != null) {
            return urlElement.getValue();
        }
        throw new ElementNotFoundException(filmElement, URL_KEY);
    }
}
