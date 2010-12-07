package org.atlasapi.remotesite.pa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.s3.S3Client;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;

public class PaLocalFileManager {
    
    private static FTPFileFilter ftpFilenameFilter = new FTPFileFilter() {
        @Override
        public boolean accept(FTPFile file) {
            return file.isFile() && !file.getName().endsWith(".md5");
        }
    };
    
    private static final FilenameFilter filenameFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith("_tvdata.xml");
        }
    };
    
    private final String ftpHost;
    private final String ftpUsername;
    private final String ftpPassword;
    private final String ftpFilesPath;
    private final S3Client s3client;
    private final AdapterLog log;
    private final File localFolder;

    public PaLocalFileManager(String ftpHost, String ftpUsername, String ftpPassword, String ftpFilesPath, String localFilesPath, S3Client s3client, AdapterLog log) {
        this.ftpHost = ftpHost;
        this.ftpUsername = ftpUsername;
        this.ftpPassword = ftpPassword;
        this.ftpFilesPath = ftpFilesPath;
        this.s3client = s3client;
        this.log = log;
        
        this.localFolder = new File(localFilesPath);
        if (!localFolder.exists()) {
            localFolder.mkdir();
        }
        if (!localFolder.isDirectory()) {
            throw new IllegalArgumentException("Files path is not a directory: " + localFilesPath);
        }
    }

    public void updateFilesFromFtpSite() throws IOException {
        if (Strings.isNullOrEmpty(ftpHost) || Strings.isNullOrEmpty(ftpUsername) || Strings.isNullOrEmpty(ftpPassword) || Strings.isNullOrEmpty(ftpFilesPath)) {
            log.record(new AdapterLogEntry(Severity.WARN).withSource(PaLocalFileManager.class).withDescription("FTP details incomplete / missing, skipping FTP update"));
            return;
        }

        FTPClient client = new FTPClient();
        
        try {
            client.connect(ftpHost);
            client.enterLocalPassiveMode();
            if (!client.login(ftpUsername, ftpPassword)) {
                throw new Exception("Unable to connect to " + ftpHost + " with username: " + ftpUsername + " and password...");
            }
            if (!client.changeWorkingDirectory(ftpFilesPath)) {
                throw new Exception("Unable to change working directory to " + ftpFilesPath);
            }
            FTPFile[] listFiles = client.listFiles(ftpFilesPath, ftpFilenameFilter);

            for (final FTPFile file : listFiles) {
                FileOutputStream fos = null;
                try {
                    String name = file.getName();

                    final Maybe<File> existingFile = findExistingFile(name);
                    File fileToWrite;
                    if (existingFile.hasValue()) {
                        Date date = new Date(existingFile.requireValue().lastModified());

                        if (file.getSize() == existingFile.requireValue().length() && file.getTimestamp().getTime().before(date)) {
                            continue;
                        }
                        
                        fileToWrite = existingFile.requireValue();
                    } else {
                        fileToWrite = new File(localFolder, name);
                    }
                    
                    try {
                        if (s3client.getAndSaveIfUpdated(name, fileToWrite, existingFile)) {
                            continue;
                        }
                    } catch (IOException e){
                        log.record(new AdapterLogEntry(Severity.WARN).withSource(PaLocalFileManager.class).withCause(e).withDescription("Error getting file " + file.getName()));
                    }
                    
                    fos = new FileOutputStream(fileToWrite);
                    client.retrieveFile(name, fos);
                    fos.close();
                    
                    s3client.put(name, fileToWrite);
                    
                } catch (Exception e) {
                    if (fos != null) {
                        fos.close();
                    }
                    log.record(new AdapterLogEntry(Severity.ERROR).withSource(PaLocalFileManager.class).withCause(e).withDescription("Error copying file " + file.getName()));
                    if (e instanceof FTPConnectionClosedException) {
                        throw new IOException(e);
                    }
                }
            }
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(PaLocalFileManager.class).withDescription("Error when trying to copy files from FTP"));
        } finally {
            client.disconnect();
        }
    }
    
    public List<File> localFiles() {
        return ImmutableList.copyOf(localFolder.listFiles(filenameFilter));
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
