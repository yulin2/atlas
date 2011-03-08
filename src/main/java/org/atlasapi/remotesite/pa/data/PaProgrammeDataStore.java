package org.atlasapi.remotesite.pa.data;

import java.io.InputStream;

import org.apache.commons.net.ftp.FTPFile;

public interface PaProgrammeDataStore {

    boolean requiresUpdating(FTPFile file);
    
    void save(String fileName, InputStream dataStream) throws Exception;
    
}
