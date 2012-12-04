package org.atlasapi.remotesite.btvod;

import org.atlasapi.media.entity.Certificate;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.remotesite.btvod.model.BtVodItemData;
import org.atlasapi.remotesite.btvod.model.BtVodLocationData;

import com.google.inject.internal.ImmutableSet;
import com.metabroadcast.common.intl.Countries;

public class BtVodEpisodeCreator implements BtVodContentCreator<Episode> {

    @Override
    public Episode extract(BtVodItemData data) {
        Episode episode = new Episode();
        
        episode.setCanonicalUri(data.getUri());
        episode.setTitle(data.getTitle());
        episode.setDescription(data.getDescription());
        episode.setYear(data.getYear());
        episode.setLanguages(ImmutableSet.of(data.getLanguage()));
        episode.setCertificates(ImmutableSet.of(new Certificate(data.getCertificate(), Countries.GB)));
        episode.setGenres(data.getGenres());
        for (BtVodLocationData location : data.getLocations()) {
            episode.addVersion(BtVodExtractionHelper.generateVersion(location));
        }
        episode.addAlias(data.getSelfLink());
        episode.addAlias(data.getExternalId());
        episode.setSeriesRef(new ParentRef(data.getContainer().get()));
        episode.setParentRef(new ParentRef(data.getContainer().get()));
        episode.setSeriesNumber(data.getSeriesNumber().get());
        episode.setEpisodeNumber(data.getEpisodeNumber().get());
        episode.setSpecialization(Specialization.TV);
        episode.setPublisher(Publisher.BT);
        
        return episode;
    }
}
