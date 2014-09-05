package org.atlasapi.remotesite.events;

import java.io.InputStream;


public interface EventsDataTransformer<T, M> {

    <Data extends EventsData<T, M>> Data transform(InputStream input);
}
