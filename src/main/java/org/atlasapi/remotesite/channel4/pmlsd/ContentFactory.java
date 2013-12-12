package org.atlasapi.remotesite.channel4.pmlsd;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Series;

import com.google.common.base.Optional;

public interface ContentFactory<B, S, I> {

    public abstract Optional<Brand> createBrand(B remote);

    public abstract Optional<Clip> createClip(I remote);

    public abstract Optional<Episode> createEpisode(I remote);

    public abstract Optional<Item> createItem(I remote);

    public abstract Optional<Series> createSeries(S remote);

}