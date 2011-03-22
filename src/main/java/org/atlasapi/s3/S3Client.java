package org.atlasapi.s3;

import java.io.File;
import java.io.IOException;

import com.metabroadcast.common.base.Maybe;

public interface S3Client {

    void put(String name, File file) throws IOException;

    boolean getAndSaveIfUpdated(String name, File fileToWrite, Maybe<File> existingFile) throws IOException;

}