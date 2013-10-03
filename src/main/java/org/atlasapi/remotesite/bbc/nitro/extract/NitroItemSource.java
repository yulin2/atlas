package org.atlasapi.remotesite.bbc.nitro.extract;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.atlasapi.remotesite.bbc.nitro.v1.NitroFormat;
import org.atlasapi.remotesite.bbc.nitro.v1.NitroGenreGroup;

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
        return new NitroItemSource<T>(programme, 
            availabilities, 
            ImmutableList.<Broadcast>of(),
            ImmutableList.<NitroGenreGroup>of(),
            ImmutableList.<NitroFormat>of()
        );
    }

    /**
     * Create a source for the given programme, availabilities and broadcasts. 
     * @param programme - the programme.
     * @param availabilities - the availabilities.
     * @param broadcasts - the broadcasts.
     * @return a {@code NitroItemSource} for the programme, availabilities and broadcasts.
     */
    public static <T> NitroItemSource<T> valueOf(T programme, List<Availability> availabilities, List<Broadcast> broadcasts, List<NitroGenreGroup> genres, List<NitroFormat> formats) {
        return new NitroItemSource<T>(programme, 
            availabilities, 
            broadcasts,
            genres,
            formats
        );
    }

    private final T programme;
    private final ImmutableList<Availability> availabilities;
    private final ImmutableList<Broadcast> broadcasts;
    private final ImmutableList<NitroGenreGroup> genres;
    private final ImmutableList<NitroFormat> formats;

    private NitroItemSource(T programme, Iterable<Availability> availabilities,
            Iterable<Broadcast> broadcasts,
            Iterable<NitroGenreGroup> genres, 
            Iterable<NitroFormat> formats) {
        this.programme = checkNotNull(programme);
        this.availabilities = ImmutableList.copyOf(availabilities);
        this.broadcasts = ImmutableList.copyOf(broadcasts);
        this.genres = ImmutableList.copyOf(genres);
        this.formats = ImmutableList.copyOf(formats);
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

    
    /**
     * Get the genre groups related to this source.
     * @return - the genre groups
     */
    public ImmutableList<NitroGenreGroup> getGenres() {
        return genres;
    }

    /**
     * Get the formats related to this source.
     * @return - the formats
     */
    public ImmutableList<NitroFormat> getFormats() {
        return formats;
    }

}
