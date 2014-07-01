package org.atlasapi.remotesite.bt.channels.mpxclient;

import com.google.common.base.Optional;
import com.metabroadcast.common.query.Selection;

public interface BtMpxClient {

    PaginatedEntries getChannels(Optional<Selection> selection) throws BtMpxClientException;
    PaginatedEntries getCategories(Optional<Selection> selection) throws BtMpxClientException;

}