package org.atlasapi.remotesite.lovefilm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;
import com.google.common.primitives.Longs;


public class DefaultLoveFilmFileStore implements LoveFilmFileStore {

    private static final Charset CHARSET = Charset.forName("windows-1252");
    private static final Ordering<File> BY_TIME_ORDERING = new Ordering<File>() { 
        @Override
        public int compare(File left, File right) {
            return Longs.compare(left.lastModified(), right.lastModified());
        }
    };
    
    private final File localFolder;

    public DefaultLoveFilmFileStore(String localFilesPath) {
        this.localFolder = new File(localFilesPath);
        if (!localFolder.exists()) {
            localFolder.mkdir();
        }
        if (!localFolder.isDirectory()) {
            throw new IllegalArgumentException("Files path is not a directory: " + localFilesPath);
        }
    }
    
    @Override
    public void save(String fileName, InputStream dataStream) throws IOException {
        FileOutputStream fos = null;
        try {
            File localFile = new File(localFolder, fileName);
            fos = new FileOutputStream(localFile);
            IOUtils.copy(dataStream, fos);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    @Override
    public LoveFilmData fetchLatestData() {
        return new LoveFilmData(Files.newReaderSupplier(fetchLatestFile(), CHARSET));
    }

    private File fetchLatestFile() {
        List<File> allFiles = Lists.newArrayList(localFolder.listFiles());
        return BY_TIME_ORDERING.max(allFiles);
    }
}
