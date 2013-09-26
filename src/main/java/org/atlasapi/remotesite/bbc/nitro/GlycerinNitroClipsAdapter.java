package org.atlasapi.remotesite.bbc.nitro;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.atlasapi.remotesite.bbc.nitro.extract.NitroClipExtractor;
import org.atlasapi.remotesite.bbc.nitro.extract.NitroItemSource;
import org.atlasapi.remotesite.bbc.nitro.extract.NitroUtil;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.metabroadcast.atlas.glycerin.Glycerin;
import com.metabroadcast.atlas.glycerin.GlycerinException;
import com.metabroadcast.atlas.glycerin.GlycerinResponse;
import com.metabroadcast.atlas.glycerin.model.Availability;
import com.metabroadcast.atlas.glycerin.model.Clip;
import com.metabroadcast.atlas.glycerin.model.PidReference;
import com.metabroadcast.atlas.glycerin.model.Programme;
import com.metabroadcast.atlas.glycerin.queries.AvailabilityQuery;
import com.metabroadcast.atlas.glycerin.queries.EntityTypeOption;
import com.metabroadcast.atlas.glycerin.queries.ProgrammesQuery;
import com.metabroadcast.common.time.Clock;

/**
 * Adapter to fetch and extract {@link org.atlasapi.media.entity.Clip Clip}s for
 * a {@link PidReference} from Nitro using {@link Glycerin}.
 */
public class GlycerinNitroClipsAdapter {

    private static final int BATCH_SIZE = 10;
    
    private static final Predicate<Programme> isClip
        = new Predicate<Programme>() {
            @Override
            public boolean apply(Programme input) {
                return input.isClip();
            }
        };
    private static final Function<Programme, com.metabroadcast.atlas.glycerin.model.Clip> toClip
        = new Function<Programme, com.metabroadcast.atlas.glycerin.model.Clip>() {
            @Override
            public com.metabroadcast.atlas.glycerin.model.Clip apply(Programme input) {
                return input.getAsClip();
            }
        };

    private final Glycerin glycerin;
    private final NitroClipExtractor clipExtractor;

    public GlycerinNitroClipsAdapter(Glycerin glycerin, Clock clock) {
        this.glycerin = glycerin;
        this.clipExtractor = new NitroClipExtractor(clock);
    }
    
    public List<org.atlasapi.media.entity.Clip> clipsFor(PidReference ref) throws NitroException {
        try {
            Iterable<com.metabroadcast.atlas.glycerin.model.Clip> nitroClips
                = Iterables.transform(Iterables.filter(getNitroClips(ref), isClip), toClip);
            Iterable<List<Clip>> clipParts = Iterables.partition(nitroClips, BATCH_SIZE);
            ImmutableList.Builder<org.atlasapi.media.entity.Clip> clips = ImmutableList.builder();
            for (List<Clip> clipPart : clipParts) {
                clips.addAll(extractClips(clipPart));
            }
            return clips.build();
        } catch (GlycerinException e) {
            throw new NitroException(ref.getPid(), e);
        }
        
    }

    private Iterable<org.atlasapi.media.entity.Clip> extractClips(List<Clip> clipPart) throws GlycerinException {
        final ListMultimap<String, Availability> availabilities = getNitroAvailabilities(clipPart);
        return Lists.transform(clipPart, new Function<Clip, org.atlasapi.media.entity.Clip>() {
            @Override
            public org.atlasapi.media.entity.Clip apply(Clip input) {
                return clipExtractor.extract(NitroItemSource.valueOf(input, availabilities.get(input.getPid())));
            }
        });
    }

    private ListMultimap<String, Availability> getNitroAvailabilities(List<Clip> clipPart) throws GlycerinException {
        AvailabilityQuery query = AvailabilityQuery.builder()
                .withDescendantsOf(toPid(clipPart))
                .withPageSize(300)
                .build();
        GlycerinResponse<Availability> availabilities = glycerin.execute(query);
        return Multimaps.index(availabilities.getResults(), new Function<Availability, String>() {
            @Override
            public String apply(Availability input) {
                return checkNotNull(NitroUtil.programmePid(input));
            }
        });
    }
    
    private Iterable<String> toPid(List<Clip> clipPart) {
        return Lists.transform(clipPart, new Function<Clip, String>() {
            @Override
            public String apply(Clip input) {
                return input.getPid();
            }
        });
    }

    private ImmutableList<Programme> getNitroClips(PidReference ref) throws GlycerinException {
        return glycerin.execute(ProgrammesQuery.builder()
                .withEntityType(EntityTypeOption.CLIP)
                .withChildrenOf(ref.getPid())
                .withPageSize(300)
                .build()).getResults();
    }
    
}
