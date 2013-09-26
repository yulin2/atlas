package org.atlasapi.remotesite.bbc.nitro;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.metabroadcast.atlas.glycerin.queries.ProgrammesMixin.TITLES;

import java.util.List;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Series;
import org.atlasapi.remotesite.bbc.nitro.extract.NitroBrandExtractor;
import org.atlasapi.remotesite.bbc.nitro.extract.NitroEpisodeExtractor;
import org.atlasapi.remotesite.bbc.nitro.extract.NitroItemSource;
import org.atlasapi.remotesite.bbc.nitro.extract.NitroSeriesExtractor;
import org.atlasapi.remotesite.bbc.nitro.v1.NitroClient;
import org.atlasapi.remotesite.bbc.nitro.v1.NitroFormat;
import org.atlasapi.remotesite.bbc.nitro.v1.NitroGenreGroup;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.atlas.glycerin.Glycerin;
import com.metabroadcast.atlas.glycerin.GlycerinException;
import com.metabroadcast.atlas.glycerin.model.Availability;
import com.metabroadcast.atlas.glycerin.model.Broadcast;
import com.metabroadcast.atlas.glycerin.model.Episode;
import com.metabroadcast.atlas.glycerin.model.PidReference;
import com.metabroadcast.atlas.glycerin.model.Programme;
import com.metabroadcast.atlas.glycerin.queries.AvailabilityQuery;
import com.metabroadcast.atlas.glycerin.queries.BroadcastsQuery;
import com.metabroadcast.atlas.glycerin.queries.ProgrammesQuery;
import com.metabroadcast.common.time.Clock;

/** 
 * A {@link NitroContentAdapter} based on a {@link Glycerin}. 
 *
 */
public class GlycerinNitroContentAdapter implements NitroContentAdapter {

    private final Glycerin glycerin;
    private final GlycerinNitroClipsAdapter clipsAdapter;
    private final NitroClient nitroClient;

    private final NitroBrandExtractor brandExtractor;
    private final NitroSeriesExtractor seriesExtractor;
    private final NitroEpisodeExtractor itemExtractor;
    
    public GlycerinNitroContentAdapter(Glycerin glycerin, NitroClient nitroClient, Clock clock) {
        this.glycerin = checkNotNull(glycerin);
        this.nitroClient = checkNotNull(nitroClient);
        this.clipsAdapter = new GlycerinNitroClipsAdapter(glycerin, clock);
        this.brandExtractor = new NitroBrandExtractor(clock);
        this.seriesExtractor = new NitroSeriesExtractor(clock);
        this.itemExtractor = new NitroEpisodeExtractor(clock);
    }
    
    @Override
    public Brand fetchBrand(PidReference ref) throws NitroException {
        try {
            checkRefType(ref, "brand");
            Programme programme = fetchProgramme(ref.getPid());
            checkState(programme.isBrand(), "fetched programme {} not brand", ref.getPid());
            Brand brand = brandExtractor.extract(programme.getAsBrand());
            brand.setClips(clipsAdapter.clipsFor(ref));
            return brand;
        } catch (GlycerinException e) {
            throw new NitroException(ref.getPid(), e);
        }
    }

    private Programme fetchProgramme(String pid) throws GlycerinException {
        ProgrammesQuery query = ProgrammesQuery.builder()
                .withPid(pid)
                .withMixins(TITLES)
                .build();
        ImmutableList<Programme> results = glycerin.execute(query).getResults();
        if (results.isEmpty()) {
            throw new GlycerinException("Failed to fetch: " + query);
        }
        if (results.size() > 1) {
            throw new GlycerinException("More than 1 result: " + query);
        }
        return results.get(0);
    }

    private void checkRefType(PidReference ref, String type) {
        checkArgument(type.equals(ref.getResultType()), "%s not %s", ref.getPid(), type);
    }

    @Override
    public Series fetchSeries(PidReference ref) throws NitroException {
        try {
            checkRefType(ref, "series");
            Programme programme = fetchProgramme(ref.getPid());
            checkState(programme.isSeries(), "fetched programme {} not series", ref.getPid());
            Series series = seriesExtractor.extract(programme.getAsSeries());
            series.setClips(clipsAdapter.clipsFor(ref));
            return series;
        } catch (GlycerinException e) {
            throw new NitroException(ref.getPid(), e);
        }
    }

    @Override
    public Item fetchEpisode(PidReference ref) throws NitroException {
        try {
            checkRefType(ref, "episode");
            Programme programme = fetchProgramme(ref.getPid());
            checkState(programme.isEpisode(), "fetched programme {} not episode", ref.getPid());
            Episode episode = programme.getAsEpisode();
            NitroItemSource<Episode> source = NitroItemSource.valueOf(
                    episode,
                    availabilities(episode),
                    broadcasts(episode),
                    genres(episode),
                    formats(episode));
            Item item = itemExtractor.extract(source);
            item.setClips(clipsAdapter.clipsFor(ref));
            return item;
        } catch (GlycerinException e) {
            throw new NitroException(ref.getPid(), e);
        }
    }

    private List<NitroFormat> formats(Episode episode) throws NitroException {
        return nitroClient.formats(episode.getPid());
    }

    private List<NitroGenreGroup> genres(Episode episode) throws NitroException {
        return nitroClient.genres(episode.getPid());
    }

    private List<Broadcast> broadcasts(Episode episode) throws GlycerinException {
        BroadcastsQuery query = BroadcastsQuery.builder()
                .withDescendantsOf(episode.getPid())
                .withPageSize(300)
                .build();
        return glycerin.execute(query).getResults();
    }

    private List<Availability> availabilities(Episode episode) throws GlycerinException {
        AvailabilityQuery query = AvailabilityQuery.builder()
                .withDescendantsOf(episode.getPid())
                .withPageSize(300)
                .build();
        return glycerin.execute(query).getResults();
    }
    
}
