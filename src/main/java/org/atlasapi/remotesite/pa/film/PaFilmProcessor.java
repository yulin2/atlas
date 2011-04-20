package org.atlasapi.remotesite.pa.film;

import java.util.List;

import nu.xom.Element;
import nu.xom.Elements;

import org.atlasapi.media.entity.Actor;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Restriction;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.pa.PaHelper;
import org.joda.time.Duration;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.text.MoreStrings;

public class PaFilmProcessor {
    
    private final ContentResolver contentResolver;
    private final ContentWriter contentWriter;
    private final AdapterLog log;
    private final ItemsPeopleWriter personWriter;

    public PaFilmProcessor(ContentResolver contentResolver, ContentWriter contentWriter, ItemsPeopleWriter peopleWriter, AdapterLog log) {
        this.contentResolver = contentResolver;
        this.contentWriter = contentWriter;
        this.log = log;
        this.personWriter = peopleWriter;
    }
    
    public void process(Element filmElement) {
        String id = filmElement.getFirstChildElement("film_reference_no").getValue();
        
        Item film;
        Identified existingFilm = contentResolver.findByCanonicalUri(PaHelper.getFilmUri(id));
        if (existingFilm != null) {
            film = (Item) existingFilm;
        }
        else {
            film = new Item(PaHelper.getFilmUri(id), PaHelper.getFilmCurie(id), Publisher.PA);
            
            film.setSpecialization(Specialization.FILM);
            film.setTitle(filmElement.getFirstChildElement("title").getValue());
            
            Version version = new Version();
            version.setProvider(Publisher.PA);
            Element certificateElement = filmElement.getFirstChildElement("certificate");
            if (!Strings.isNullOrEmpty(certificateElement.getValue()) && MoreStrings.containsOnlyAsciiDigits(certificateElement.getValue())) {
                version.setRestriction(Restriction.from(Integer.parseInt(certificateElement.getValue())));
            }
            
            Element durationElement = filmElement.getFirstChildElement("running_time");
            if (durationElement != null && !Strings.isNullOrEmpty(durationElement.getValue())) {
                version.setDuration(Duration.standardMinutes(Long.parseLong(durationElement.getValue())));
            }
            
            film.addVersion(version);
        }
        
        film.setPeople(ImmutableList.copyOf(Iterables.concat(getActors(filmElement.getFirstChildElement("cast")), getDirectors(filmElement.getFirstChildElement("direction")))));
            
        contentWriter.createOrUpdate(film);
            
        personWriter.createOrUpdatePeople(film);
    }
    
    private List<Actor> getActors(Element castElement) {
        Elements actorElements = castElement.getChildElements("actor");
        
        List<Actor> actors = Lists.newArrayList();
        
        for (int i = 0; i < actorElements.size(); i++) {
            Element actorElement = actorElements.get(i);
            
            String role = actorElement.getFirstChildElement("role").getValue();
            
            actors.add(Actor.actor(name(actorElement), role, Publisher.PA));
        }
        
        return actors;
    }
    
    private List<CrewMember> getDirectors(Element directionElement) {
        Elements directorElements = directionElement.getChildElements("director");
        
        List<CrewMember> actors = Lists.newArrayList();
        
        for (int i = 0; i < directorElements.size(); i++) {
            Element directorElement = directorElements.get(i);
            
            String role = directorElement.getFirstChildElement("role").getValue();
            
            String name = name(directorElement);
            
            if (name != null) {
                actors.add(CrewMember.crewMember(name, role, Publisher.PA));
            }
        }
        
        return actors;
    }
    
    private String name(Element personElement) {
        
        Element forename = personElement.getFirstChildElement("forename");
        Element surname = personElement.getFirstChildElement("surname");
        
        if (forename == null && surname == null) {
            log.record(new AdapterLogEntry(Severity.WARN).withDescription("Person found with no name: " + personElement.toXML()).withSource(getClass()));
            return null;
        }
        
        if (forename != null && surname != null) {
           return forename + " " + surname;
        }
        else {
            if (forename != null) {
                return forename.getValue();
            }
            else {
                return surname.getValue();
            }
        }
    }
}
