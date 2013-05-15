package org.atlasapi.remotesite.btvod;

import org.atlasapi.media.entity.Certificate;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.remotesite.btvod.model.BtVodItemData;
import org.atlasapi.remotesite.btvod.model.BtVodLocationData;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.intl.Countries;

public class BtVodEpisodeCreator implements BtVodContentCreator<Episode> {

    @Override
    public Episode extract(BtVodItemData data) {
        Episode episode = new Episode();
        
        episode.setCanonicalUri(data.getUri());
        episode.setTitle(data.getTitle());
        episode.setDescription(data.getDescription());
        if (data.getYear() != null) {
            episode.setYear(data.getYear());
        }
        if (data.getLanguage() != null) {
            episode.setLanguages(ImmutableSet.of(data.getLanguage()));
        }
        if (data.getCertificate() != null) {
            episode.setCertificates(ImmutableSet.of(new Certificate(data.getCertificate(), Countries.GB)));
        }
        episode.setGenres(data.getGenres());
        for (BtVodLocationData location : data.getLocations()) {
            episode.addVersion(BtVodExtractionHelper.generateVersion(location));
        }
        if (data.getSelfLink() != null) {
            // TODO new aliases
            episode.addAliasUrl(data.getSelfLink());
        }
        episode.addAliasUrl(data.getExternalId());
        
        episode.setSeriesRef(new ParentRef(data.getContainer().get()));
        episode.setParentRef(new ParentRef(data.getContainer().get()));
        if (data.getSeriesNumber().isPresent()) {
            episode.setSeriesNumber(data.getSeriesNumber().get());
        }
        if (data.getEpisodeNumber().isPresent()) {
            episode.setEpisodeNumber(data.getEpisodeNumber().get());
        }
        episode.setSpecialization(Specialization.TV);
        episode.setPublisher(Publisher.BT);
        
        return episode;
    }
}
