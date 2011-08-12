package org.atlasapi.remotesite.worldservice;

import static com.google.common.base.Predicates.notNull;
import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;

import java.io.InputStream;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import org.atlasapi.persistence.logging.AdapterLog;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.metabroadcast.common.base.Maybe;

public class S3WsDataStore implements WsDataStore {
    
    private final AWSCredentials creds;
    private final String bucketname;
    private final AdapterLog log;
    
    private static final Ordering<S3Object> dateSort = Ordering.from(new Comparator<S3Object>() {
        @Override
        public int compare(S3Object o1, S3Object o2) {
            return o1.getName().compareTo(o2.getName());
        }
    });
    public static final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyyMMdd");
    

    public S3WsDataStore(AWSCredentials credentials, String bucketname, AdapterLog log) {
        this.creds = credentials;
        this.bucketname = bucketname;
        this.log = log;
    }

    @Override
    public Maybe<WsData> latestData() {
        try {
            RestS3Service service = new RestS3Service(creds);
            return dataForPattern(service, listSortedObjects(service, ""), "\\d{8}");
        } catch (Exception e) {
            log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Couldn't get latest WS data"));
            return Maybe.nothing();
        }
    }

    @Override
    public Maybe<WsData> dataForDay(DateTime day) {
        try {
            String folder = dateFormat.print(day);
            RestS3Service service = new RestS3Service(creds);
            return dataForPattern(service, listSortedObjects(service, folder), folder);
        } catch (Exception e) {
            log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Couldn't get WS data for %s", day));
            return Maybe.nothing();
        }
    }
    
    public Maybe<WsData> dataForPattern(final S3Service service, ImmutableList<S3Object> objects, String pattern) {
        Map<String, WsDataFile> patterns = patternFileMap(pattern);
        
        Map<WsDataFile, S3Object> mostRecent = matchObjectsToFiles(objects, patterns);
        
        if(complete(mostRecent) || !sameDay(mostRecent.values())) {
            return Maybe.nothing();
        }
        
        Map<WsDataFile, S3Object> fullObjects = getFullObjects(service, mostRecent);
        
        if(complete(mostRecent)) {
            return Maybe.nothing();
        }

        return Maybe.<WsData>just(new S3WsData(fullObjects));
    }

    public boolean complete(Map<WsDataFile, S3Object> mostRecent) {
        return mostRecent.size() != WsDataFile.values().length;
    }
    
    public Map<WsDataFile, S3Object> getFullObjects(final S3Service service, Map<WsDataFile, S3Object> mostRecent) {
        return ImmutableMap.copyOf(Maps.filterValues(Maps.transformValues(mostRecent, new Function<S3Object, S3Object>() {
            @Override
            public S3Object apply(S3Object input) {
                try {
                    return service.getObject(input.getBucketName(), input.getKey());
                } catch (Exception e) {
                    log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Couldn't get full object for %s", input.getKey()));
                    return null;
                }
            }
        }), notNull()));
    }

    public Map<WsDataFile, S3Object> matchObjectsToFiles(ImmutableList<S3Object> objects, Map<String, WsDataFile> patterns) {
        Map<WsDataFile, S3Object> mostRecent = Maps.newHashMap();
        int reached = 0;
        for (Entry<String, WsDataFile> filePattern : patterns.entrySet()) {
            for (S3Object object : objects.subList(reached, objects.size())) {
                reached++;
                if(object.getName().matches(filePattern.getKey()) && !mostRecent.containsKey(filePattern.getKey())) {
                    mostRecent.put(filePattern.getValue(), object);
                    break;
                }
            }
        }
        return mostRecent;
    }

    public Map<String, WsDataFile> patternFileMap(final String pattern) {
        Map<String, WsDataFile> patterns = Maps.uniqueIndex(ImmutableList.copyOf(WsDataFile.values()).reverse(), new Function<WsDataFile, String>() {
            @Override
            public String apply(WsDataFile input) {
                return String.format("%s/%s.gz", pattern, input.filename());
            }
        });
        return patterns;
    }

    public ImmutableList<S3Object> listSortedObjects(final S3Service service, String prefix) throws S3ServiceException {
        return dateSort.reverse().immutableSortedCopy(ImmutableList.copyOf(service.listObjects(bucketname, prefix, null)));
    }

    private boolean sameDay(Iterable<S3Object> objs) {
        String prev = null;
        for (S3Object obj : objs) {
            String day = obj.getName().substring(0, 8);
            if(prev != null && !prev.equals(day)) {
                return false;
            } else {
                prev = day;
            }
        }
        return true;
    }
    
    public class S3WsData implements WsData {


        private final Map<WsDataFile, S3Object> fullObjects;

        public S3WsData(Map<WsDataFile, S3Object> fullObjects) {
            this.fullObjects = fullObjects;
        }
        
        public InputStream inputStreamFor(WsDataFile file) {
            try {
                return new GZIPInputStream(fullObjects.get(file).getDataInputStream());
            } catch (Exception e) {
                log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Couldn't get full object for %s", file.filename()));
                return null;
            }
        }

        @Override
        public InputStream getAudioItem() {
            return inputStreamFor(WsDataFile.AUDIO_ITEM);
        }

        @Override
        public InputStream getAudioItemProgLink() {
            return inputStreamFor(WsDataFile.AUDIO_ITEM_PROG_LINK);
        }

        @Override
        public InputStream getGenre() {
            return inputStreamFor(WsDataFile.GENRE);
        }

        @Override
        public InputStream getProgramme() {
            return inputStreamFor(WsDataFile.PROGRAMME);
        }

        @Override
        public InputStream getSeries() {
            return inputStreamFor(WsDataFile.SERIES);
        }
        
    }
}
