package org.atlasapi.remotesite.bbc;

import org.atlasapi.remotesite.SiteSpecificAdapter;

public abstract class AbstractBbcAdapter<T> implements SiteSpecificAdapter<T> {

    @Override
    public boolean canFetch(String uri) {
        return BbcFeeds.isACanonicalSlashProgrammesUri(uri);
    }

}