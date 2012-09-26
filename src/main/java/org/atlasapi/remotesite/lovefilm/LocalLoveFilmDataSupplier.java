package org.atlasapi.remotesite.lovefilm;

import java.io.File;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class LocalLoveFilmDataSupplier implements LoveFilmDataSupplier {

    private File data;

    public LocalLoveFilmDataSupplier(File data) {
        this.data = data;
    }
    
    @Override
    public LoveFilmData getLatestData() {
        return new LoveFilmData(Files.newReaderSupplier(data, Charsets.UTF_8));
    }

}
