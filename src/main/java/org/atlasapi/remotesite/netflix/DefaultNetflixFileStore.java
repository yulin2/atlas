package org.atlasapi.remotesite.netflix;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import nu.xom.Builder;
import nu.xom.Document;

import org.apache.commons.io.IOUtils;
import org.atlasapi.s3.S3Client;

public class DefaultNetflixFileStore implements NetflixDataStore {
    
    private final S3Client s3client;
    private final File localFolder;
    private final String netflixFileName;

    public DefaultNetflixFileStore(String netflixFileName, String localFilesPath, S3Client s3client) {
        this.netflixFileName = netflixFileName;
        checkNotNull(localFilesPath);
        this.s3client = s3client;
        this.localFolder = new File(localFilesPath);
        if (!localFolder.exists()) {
            localFolder.mkdir();
        }
        if (!localFolder.isDirectory()) {
            throw new IllegalArgumentException("Files path is not a directory: " + localFilesPath);
        }
    }
    
    @Override
    public void save(InputStream dataStream) throws Exception {
        FileOutputStream fos = null;
        try {
            File localFile = new File(localFolder, netflixFileName);
            fos = new FileOutputStream(localFile);
            IOUtils.copy(dataStream, fos);
            s3client.put(netflixFileName, localFile);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    @Override
    public Document getData() {
        for (File file : localFolder.listFiles()) {
            if (netflixFileName.equals(file.getName())) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                    return new Builder().build(fis);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        throw new RuntimeException("Cannot find Netflix ingest xml file: " + netflixFileName + " in folder " + localFolder.getAbsolutePath());
    }
}
