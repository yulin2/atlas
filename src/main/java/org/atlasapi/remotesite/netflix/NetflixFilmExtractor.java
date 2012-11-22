package org.atlasapi.remotesite.netflix;

import java.util.Set;

import nu.xom.Element;

import org.atlasapi.media.entity.Certificate;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Version;
import org.joda.time.Duration;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.inject.internal.ImmutableSet;
import com.metabroadcast.common.intl.Countries;


public class NetflixFilmExtractor extends NetflixContentExtractor<Film>  {
    private static final String SYSTEM_ATTRIBUTE = "system";
    private static final String BBFC = "BBFC";
    private static final String MOVIES_URL_PREFIX = "http://gb.netflix.com/movies/";
    private static final String TITLE_KEY = "title";
    private static final String PARENTAL_ADVISORIES_KEY = "parental_advisories";
    private static final String RELEASE_YEAR_KEY = "release_year";
    private static final String LONG_SYNOPSIS_KEY = "long_synopsis";
    private static final String SHORT_SYNOPSIS_KEY = "short_synopsis";
    private static final String DURATION_KEY = "duration";
    private static final String URL_KEY = "url";
    
    @Override
    public Set<Film> extract(Element source, int id) {
        try {
            Film film = new Film();

            film.setCanonicalUri(MOVIES_URL_PREFIX + id);

            film.setTitle(getTitle(source));
            film.setYear(getYear(source));
            film.setDescription(getDescription(source));
            film.addVersion(getVersion(source));
            film.setGenres(getGenres(source));
            film.setPeople(getPeople(source));
            film.setCertificates(getCertificates(source));
            film.addAlias(getAlias(source));

            return ImmutableSet.<Film>builder().add(film).build();
        } catch (Exception e) {
            Throwables.propagate(e);
            // never get here
            return null;
        }
    }

    private String getAlias(Element filmElement) throws ElementNotFoundException {
        Element urlElement = filmElement.getFirstChildElement(URL_KEY);
        if (urlElement != null) {
            return urlElement.getValue();
        }
        throw new ElementNotFoundException(filmElement, URL_KEY);
    }


    private Version getVersion(Element filmElement) throws ElementNotFoundException {
        Element durationElement = filmElement.getFirstChildElement(DURATION_KEY);
        if (durationElement != null) {
            Version version = new Version();
            version.setDuration(Duration.standardSeconds(Integer.parseInt(durationElement.getValue())));
            return version;
        }
        throw new ElementNotFoundException(filmElement, DURATION_KEY);
    }

    private String getDescription(Element filmElement) throws ElementNotFoundException {
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

    private int getYear(Element filmElement) throws ElementNotFoundException {
        Element yearElement = filmElement.getFirstChildElement(RELEASE_YEAR_KEY);
        if (yearElement != null) {
            return Integer.parseInt(yearElement.getValue());
        }
        throw new ElementNotFoundException(filmElement, RELEASE_YEAR_KEY);
    }

    private String getTitle(Element filmElement) throws ElementNotFoundException {
        Element titleElement = filmElement.getFirstChildElement(TITLE_KEY);
        if (titleElement != null) {
            return titleElement.getValue();
        }
        throw new ElementNotFoundException(filmElement, TITLE_KEY);
    }

    private Set<Certificate> getCertificates(Element filmElement) throws ElementNotFoundException, AttributeNotFoundException {
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
}
