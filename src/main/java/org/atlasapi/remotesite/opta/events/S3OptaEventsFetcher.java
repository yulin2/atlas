package org.atlasapi.remotesite.opta.events;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.events.S3FileFetcher;
import org.atlasapi.remotesite.opta.events.model.OptaMatch;
import org.atlasapi.remotesite.opta.events.model.OptaSportType;
import org.atlasapi.remotesite.opta.events.model.OptaTeam;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;


public final class S3OptaEventsFetcher<T extends OptaTeam, M extends OptaMatch> implements OptaEventsFetcher<T, M> {
    
    private final S3FileFetcher fileFetcher;
    private final Map<OptaSportType, String> fileNames;
    private final OptaDataTransformer<T, M> transformer;
    private final String bucketName;
    
    public S3OptaEventsFetcher(S3FileFetcher fileFetcher, Map<OptaSportType, String> fileNames, 
            OptaDataTransformer<T, M> transformer, String bucketName) {
        this.fileFetcher = checkNotNull(fileFetcher);
        this.fileNames = ImmutableMap.copyOf(fileNames);
        this.transformer = checkNotNull(transformer);
        this.bucketName = checkNotNull(bucketName);
    }

    @Override
    public Optional<? extends OptaEventsData<T, M>> fetch(OptaSportType sport) {
        try {
            String fileName = fileNames.get(sport);
            if (fileName == null) {
                throw new IllegalArgumentException("No configuration for sport " + sport.name());
            }
            
            InputStream in = fileFetcher.fetch(bucketName, "", fileName);
            return Optional.fromNullable(transformer.<OptaEventsData<T, M>>transform(in));
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new FetchException(e.getMessage(), e);
        }
    }
    
    @Override
    public Set<OptaSportType> sports() {
        return fileNames.keySet();
    }
}
