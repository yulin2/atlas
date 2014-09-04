package org.atlasapi.remotesite.events;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.atlasapi.remotesite.FetchException;
import org.jets3t.service.S3Service;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3Object;

import com.google.common.base.Supplier;


public class S3FileFetcher {

    private static final String NO_DELIMITER = null;
    
    private final Supplier<S3Service> s3ServiceSupplier;
    
    public S3FileFetcher(Supplier<S3Service> s3ServiceSupplier) {
        this.s3ServiceSupplier = checkNotNull(s3ServiceSupplier);
    }

    public InputStream fetch(String bucketName, String folderName, String fileName) throws FetchException {
        try {
        checkArgument(bucketName != null);
        checkArgument(folderName != null);
        checkArgument(fileName != null);
        
        S3Service s3Service = s3ServiceSupplier.get();
        S3Object[] objects = s3Service.listObjects(bucketName, fileName, NO_DELIMITER);
        S3Object file = getFileforName(objects, fileName);
        if (file == null) {
            throw new FetchException(String.format("No data file %s in bucket %s and folder %s", fileName, bucketName, folderName));
        }
        return inputStreamFor(s3Service, bucketName, file);
        } catch (ServiceException | IOException e) {
            throw new FetchException(e.getMessage(), e);
        }
    }
    
    private S3Object getFileforName(S3Object[] objects, String fileName) {
        for (S3Object obj : objects) {
            if (fileName.equals(obj.getName())) {
                return obj;
            }
        }
        return null;
    }
    
    private InputStream inputStreamFor(final S3Service service, String bucketName, S3Object object) throws IOException {
        final String key = object.getKey();
        try {
            S3Object fullObject = service.getObject(bucketName, key);
            return fullObject.getDataInputStream();
        } catch (ServiceException e) {
            throw new IOException(e.getMessage(), e);
        }
    }
}
