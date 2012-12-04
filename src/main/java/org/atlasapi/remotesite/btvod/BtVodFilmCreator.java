package org.atlasapi.remotesite.btvod;

import org.atlasapi.media.entity.Certificate;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.remotesite.btvod.model.BtVodItemData;
import org.atlasapi.remotesite.btvod.model.BtVodLocationData;

import com.google.inject.internal.ImmutableSet;
import com.metabroadcast.common.intl.Countries;

public class BtVodFilmCreator implements BtVodContentCreator<Film> {

    @Override
    public Film extract(BtVodItemData data) {
        Film film = new Film();
        
        film.setCanonicalUri(data.getUri());
        film.setTitle(data.getTitle());
        film.setDescription(data.getDescription());
        film.setYear(data.getYear());
        film.setLanguages(ImmutableSet.of(data.getLanguage()));
        film.setCertificates(ImmutableSet.of(new Certificate(data.getCertificate(), Countries.GB)));
        film.setGenres(data.getGenres());
        for (BtVodLocationData location : data.getLocations()) {
            film.addVersion(BtVodExtractionHelper.generateVersion(location));
        }
        film.addAlias(data.getSelfLink());
        film.addAlias(data.getExternalId());
        film.setSpecialization(Specialization.FILM);
        film.setPublisher(Publisher.BT);
        
        return film;
    }

}
