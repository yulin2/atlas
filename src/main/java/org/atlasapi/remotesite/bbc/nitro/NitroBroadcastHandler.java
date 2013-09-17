package org.atlasapi.remotesite.bbc.nitro;

import com.metabroadcast.atlas.glycerin.model.Broadcast;

/**
 * <p>
 * A {@code NitroBroadcastHandler} processes a {@link Broadcast}.
 * </p>
 * 
 * @param <T>
 *            - the type of the result of processing the {@code Broadcast}.
 */
public interface NitroBroadcastHandler<T> {

    /**
     * Process {@link Broadcast}.
     * @param broadcast - the {@code Broadcast} to be processed.
     * @return - the result of processing the {@code broadcast}
     */
    T handle(Broadcast broadcast) throws NitroException;

}
