package org.atlasapi.remotesite.opta.events;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.opta.events.model.OptaMatch;
import org.atlasapi.remotesite.opta.events.model.OptaSportType;
import org.atlasapi.remotesite.opta.events.model.OptaTeam;
import org.jets3t.service.S3Service;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3Object;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;


public final class S3OptaEventsFetcher<T extends OptaTeam, M extends OptaMatch> implements OptaEventsFetcher<T, M> {
    
    private static final String NO_DELIMITER = null;
    
    private final S3Service s3Service;
    private final Map<OptaSportType, String> fileNames;
    private final OptaDataTransformer<T, M> transformer;
    private final String bucketName;
    
    public S3OptaEventsFetcher(S3Service s3Service, Map<OptaSportType, String> fileNames, 
            OptaDataTransformer<T, M> transformer, String bucketName) {
        this.s3Service = checkNotNull(s3Service);
        this.fileNames = ImmutableMap.copyOf(fileNames);
        this.transformer = checkNotNull(transformer);
        this.bucketName = checkNotNull(bucketName);
    }

    @Override
    public Optional<? extends OptaEventsData<T, M>> fetch(OptaSportType sport) {
        try {
            String fileName = fileNames.get(sport);
            if (fileName == null) {
                return Optional.absent();
            }
            S3Object[] objects = s3Service.listObjects(bucketName, "", NO_DELIMITER);
            S3Object file = getFileforName(objects, fileName);
            if (file == null) {
                throw new FetchException(String.format("No data file %s in %s", fileName, bucketName));
            }
            InputStream in = inputStreamFor(s3Service, file);
            return Optional.fromNullable(transformer.transform(in));
        } catch (ServiceException | JsonSyntaxException | JsonIOException | IOException e) {
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

    private InputStream inputStreamFor(final S3Service service, S3Object object) throws IOException {
        final String key = object.getKey();
        try {
            S3Object fullObject = service.getObject(bucketName, key);
            return fullObject.getDataInputStream();
        } catch (ServiceException e) {
            throw new IOException(e.getMessage(), e);
        }
    }
    
    @Override
    public Set<OptaSportType> sports() {
        return fileNames.keySet();
    }
}
