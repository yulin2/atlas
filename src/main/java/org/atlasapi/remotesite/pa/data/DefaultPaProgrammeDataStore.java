package org.atlasapi.remotesite.pa.data;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.atlasapi.s3.S3Client;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;

public class DefaultPaProgrammeDataStore implements PaProgrammeDataStore {

    private static final FilenameFilter filenameFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith("_tvdata.xml");
        }
    };

    private final File localFolder;
    private final S3Client s3client;

    public DefaultPaProgrammeDataStore(String localFilesPath, S3Client s3client) {
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
    public boolean requiresUpdating(FTPFile ftpFile) {
        String name = ftpFile.getName();
        Maybe<File> existingFile = findExistingFile(name);
        return existingFile.isNothing() || moreRecent(ftpFile, existingFile.requireValue()) || differentSizes(ftpFile, existingFile.requireValue());
    }

    @Override
    public void save(String fileName, InputStream dataStream) throws Exception {
        FileOutputStream fos = null;
        try {
            File localFile = new File(localFolder, fileName);
            fos = new FileOutputStream(localFile);
            IOUtils.copy(dataStream, fos);
            s3client.put(fileName, localFile);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    private boolean differentSizes(FTPFile ftpFile, File existingFile) {
        return ftpFile.getSize() != existingFile.length();
    }

    private boolean moreRecent(FTPFile ftpFile, File existingFile) {
        return ftpFile.getTimestamp().getTime().after(new Date(existingFile.lastModified()));
    }

    @Override
    public List<File> localFiles(Predicate<File> filter) {
        Predicate<File> fileFilter = filter == null ? Predicates.<File>alwaysTrue() : filter;
        return ImmutableList.copyOf(Iterables.filter(ImmutableList.copyOf(localFolder.listFiles(filenameFilter)), fileFilter));
    }

    private Maybe<File> findExistingFile(String fileName) {
        for (File file : localFolder.listFiles()) {
            if (fileName.equals(file.getName())) {
                return Maybe.just(file);
            }
        }

        return Maybe.nothing();
    }
}
