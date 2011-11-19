package org.atlasapi.remotesite.itunes.epf;

import org.atlasapi.remotesite.itunes.epf.ArtistTable.ArtistTableRow;

public interface ArtistTableRowProcessor {

    boolean process(ArtistTableRow row);
    
}
