package org.atlasapi.remotesite.lovefilm;

import org.atlasapi.remotesite.lovefilm.LoveFilmData.LoveFilmDataRow;

public interface LoveFilmDataRowHandler {

    void prepare();

    void handle(LoveFilmDataRow row);

    void finish();

}
