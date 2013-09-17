package org.atlasapi.remotesite.bbc.nitro;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Series;

import com.metabroadcast.atlas.glycerin.model.PidReference;

/**
 * Adapter for fetching data from Nitro by {@link PidReference}.  
 */
//Given PidReference is from glycerin should there be a separate PID type?
public interface NitroContentAdapter {

    /**
     * Fetch and transform data for the given ref into a {@link Brand}.
     * 
     * @param ref
     *            - the PID reference, must have result type "brand".
     * @return - a {@link Brand} representing the fetched data.
     * @throws NitroException
     *             - if there was an error fetching data from Nitro.
     * @throws IllegalArgumentException
     *             - if the {@link PidReference} is not for a brand.
     */
    Brand fetchBrand(PidReference ref) throws NitroException;

    /**
     * Fetch and transform data for the given ref into a {@link Series}.
     * 
     * @param ref
     *            - the PID reference, must have result type "series".
     * @return - a {@link Series} representing the fetched data.
     * @throws NitroException
     *             - if there was an error fetching data from Nitro.
     * @throws IllegalArgumentException
     *             - if the {@link PidReference} is not for a series.
     */
    Series fetchSeries(PidReference ref) throws NitroException;

    /**
     * Fetch and transform data for the given ref into a {@link Item}.
     * 
     * @param ref
     *            - the PID reference, must have result type "episode".
     * @return - a {@link Item} representing the fetched data.
     * @throws NitroException
     *             - if there was an error fetching data from Nitro.
     * @throws IllegalArgumentException
     *             - if the {@link PidReference} is not for an episode.
     */
    Item fetchEpisode(PidReference ref) throws NitroException;

}
