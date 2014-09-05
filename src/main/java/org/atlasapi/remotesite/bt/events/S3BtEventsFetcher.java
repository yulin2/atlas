package org.atlasapi.remotesite.bt.events;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.bt.events.feedModel.BtEvent;
import org.atlasapi.remotesite.bt.events.feedModel.BtTeam;
import org.atlasapi.remotesite.bt.events.model.BtSportType;
import org.atlasapi.remotesite.events.EventsData;
import org.atlasapi.remotesite.events.EventsDataTransformer;
import org.atlasapi.remotesite.events.S3FileFetcher;

import com.google.common.base.Optional;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;


public class S3BtEventsFetcher implements BtEventsFetcher {

    private final S3FileFetcher fileFetcher;
    private final Map<BtSportType, String> fileNames;
    private final String bucketName;
    private final EventsDataTransformer<BtTeam, BtEvent> transformer;
    
    public S3BtEventsFetcher(S3FileFetcher fileFetcher, Map<BtSportType, String> fileNames, 
            String bucketName, EventsDataTransformer<BtTeam, BtEvent> transformer) {
        this.fileFetcher = checkNotNull(fileFetcher);
        this.fileNames = checkNotNull(fileNames);
        this.bucketName = checkNotNull(bucketName);
        this.transformer = checkNotNull(transformer);
    }
    
    @Override
    public Optional<? extends EventsData<BtTeam, BtEvent>> fetch(BtSportType sport) throws FetchException {
        try {
            String fileName = fileNames.get(sport);
            if (fileName == null) {
                throw new IllegalArgumentException("No configuration for sport " + sport.name());
            }
            InputStream in = fileFetcher.fetch(bucketName, "", fileName);
            return Optional.fromNullable(transformer.transform(in));
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new FetchException(e.getMessage(), e);
        } 
    }
    
    @Override
    public Set<BtSportType> sports() {
        return fileNames.keySet();
    }
}
