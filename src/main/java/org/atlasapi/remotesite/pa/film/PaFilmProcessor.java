package org.atlasapi.remotesite.pa.film;

import nu.xom.Element;

import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Restriction;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.remotesite.pa.PaHelper;
import org.joda.time.Duration;

import com.google.common.base.Strings;
import com.metabroadcast.common.text.MoreStrings;

public class PaFilmProcessor {
    
    private final ContentResolver contentResolver;
    private final ContentWriter contentWriter;

    public PaFilmProcessor(ContentResolver contentResolver, ContentWriter contentWriter) {
        this.contentResolver = contentResolver;
        this.contentWriter = contentWriter;
    }
    
    public void process(Element filmElement) {
        String id = filmElement.getFirstChildElement("film_reference_no").getValue();
        
        Film film;
        Identified existingFilm = contentResolver.findByCanonicalUri(PaHelper.getFilmUri(id));
        if (existingFilm != null) {
            if (existingFilm instanceof Film) {
                film = (Film) existingFilm;
            }
            else {
                film = new Film();
                Item.copyTo((Item) existingFilm, film);
            }
        }
        else {
            film = new Film(PaHelper.getFilmUri(id), PaHelper.getFilmCurie(id), Publisher.PA);
            
            film.setSpecialization(Specialization.FILM);
            film.setTitle(filmElement.getFirstChildElement("title").getValue());
            String year = filmElement.getFirstChildElement("year").getValue();
            if (!Strings.isNullOrEmpty(year) && MoreStrings.containsOnlyAsciiDigits(year)) {
                film.setYear(Integer.parseInt(year));
            }
            
            Version version = new Version();
            version.setProvider(Publisher.PA);
            Element certificateElement = filmElement.getFirstChildElement("certificate");
            if (certificateElement != null && !Strings.isNullOrEmpty(certificateElement.getValue()) && MoreStrings.containsOnlyAsciiDigits(certificateElement.getValue())) {
                version.setRestriction(Restriction.from(Integer.parseInt(certificateElement.getValue())));
            }
            
            Element durationElement = filmElement.getFirstChildElement("running_time");
            if (durationElement != null && !Strings.isNullOrEmpty(durationElement.getValue()) && MoreStrings.containsOnlyAsciiDigits(durationElement.getValue())) {
                version.setDuration(Duration.standardMinutes(Long.parseLong(durationElement.getValue())));
            }
            
            film.addVersion(version);
        }
        
        contentWriter.createOrUpdate(film);
    }
}
