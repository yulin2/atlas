package org.atlasapi.remotesite.lovefilm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.metabroadcast.common.base.Maybe;


public class DefaultLoveFilmFileStore implements LoveFilmFileStore {
    
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
        // TODO Auto-generated method stub
        localFolder.listFiles(filter);
        
        Charset charset = Charset.forName("windows-1252");
        return new LoveFilmData(Files.newReaderSupplier(data, charset));
    }

    private File fetchLatestFile() {
        List<File> allFiles = Lists.newArrayList(localFolder.listFiles());
        Collections.sort(allFiles, COMPARE_BY_TIME);
        return allFiles.get(0);
    }
}
