package org.atlasapi.remotesite.bbc.nitro.extract;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.atlas.glycerin.model.Availability;
import com.metabroadcast.atlas.glycerin.model.Broadcast;

/**
 * A source which contains all the data required for extracting an
 * {@link org.atlasapi.media.entity.Item Item}, including {@link Availability}s
 * and {@link Broadcast}s.
 * 
 * @param <T> - the type of {@link com.metabroadcast.atlas.glycerin.model.Programme Programme}
 */
public class NitroItemSource<T> {

    /**
     * Create a source for the given programme and availabilities. 
     * @param programme - the programme.
     * @param availabilities - the availabilities.
     * @return a {@code NitroItemSource} for the programme and availabilities.
     */
    public static <T> NitroItemSource<T> valueOf(T programme, Iterable<Availability> availabilities) {
        return new NitroItemSource<T>(programme, ImmutableList.copyOf(availabilities), ImmutableList.<Broadcast>of());
    }

    /**
     * Create a source for the given programme, availabilities and broadcasts. 
     * @param programme - the programme.
     * @param availabilities - the availabilities.
     * @param broadcasts - the broadcasts.
     * @return a {@code NitroItemSource} for the programme, availabilities and broadcasts.
     */
    public static <T> NitroItemSource<T> valueOf(T programme, List<Availability> availabilities, List<Broadcast> broadcasts) {
        return new NitroItemSource<T>(programme, ImmutableList.copyOf(availabilities), ImmutableList.copyOf(broadcasts));
    }

    private final T programme;
    private final ImmutableList<Availability> availabilities;
    private final ImmutableList<Broadcast> broadcasts;

    private NitroItemSource(T programme, ImmutableList<Availability> availabilities,
            ImmutableList<Broadcast> broadcasts) {
        this.programme = checkNotNull(programme);
        this.availabilities = availabilities;
        this.broadcasts = broadcasts;
    }
    
    /**
     * Get the programme related to this source. 
     * @return - the programme
     */
    public T getProgramme() {
        return programme;
    }
    
    /**
     * Get the availabilities related to this source. 
     * @return - the availabilities
     */
    public ImmutableList<Availability> getAvailabilities() {
        return availabilities;
    }

    /**
     * Get the broadcasts related to this source.
     * @return - the broadcasts
     */
    public ImmutableList<Broadcast> getBroadcasts() {
        return broadcasts;
    }

}
