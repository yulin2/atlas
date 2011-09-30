package org.atlasapi.remotesite.worldservice;

import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;

import java.io.IOException;
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
import com.google.common.collect.Iterables;
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
    public Maybe<WsDataSet> latestData() {
        return dataSetForPrefixPattern("", "\\d{8}");
    }
    
    @Override
    public Maybe<WsDataSet> dataForDay(DateTime day) {
        String folder = dateFormat.print(day);
        return dataSetForPrefixPattern(folder, folder);
    }

    private Maybe<WsDataSet> dataSetForPrefixPattern(String prefix, String pattern) {
        try {
            RestS3Service service = new RestS3Service(creds);
            return dataForPattern(service, listSortedObjects(service, prefix), pattern);
        } catch (Exception e) {
            log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Error getting WS data"));
            return Maybe.nothing();
        }
    }
    
    public Maybe<WsDataSet> dataForPattern(final S3Service service, ImmutableList<S3Object> objects, String pattern) {
        Map<String, WsDataFile> patterns = patternFileMap(pattern);
        
        Map<WsDataFile, S3Object> matchedFileObjects = matchObjectsToFiles(objects, patterns);
        
        if(!complete(matchedFileObjects) || !sameDay(matchedFileObjects.values())) {
            return Maybe.nothing();
        }

        return Maybe.<WsDataSet>just(new S3WsDataSet(service, getName(Iterables.getLast(matchedFileObjects.values())), matchedFileObjects));
    }

    private String getName(S3Object last) {
        return last.getName().substring(0, 8);
    }

    public boolean complete(Map<WsDataFile, S3Object> mostRecent) {
        return mostRecent.size() == WsDataFile.values().length;
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
    
    private class S3WsDataSource extends WsDataSource {

        private S3Object object;

        public S3WsDataSource(WsDataFile file, S3Object s3Object) throws Exception {
            super(file, new GZIPInputStream(s3Object.getDataInputStream()));
            this.object = s3Object;
        }

        @Override
        public void close() throws IOException {
            object.closeDataInputStream();
            super.close();
        }
    }
    
    private class S3WsDataSet implements WsDataSet {

        private final Map<WsDataFile, S3Object> fullObjects;
        private final S3Service service;
        private final String setName;

        public S3WsDataSet(S3Service service, String setName, Map<WsDataFile, S3Object> fullObjects) {
            this.service = service;
            this.setName = setName;
            this.fullObjects = fullObjects;
        }
        
        public WsDataSource inputStreamFor(WsDataFile file) {
            try {
                return new S3WsDataSource(file, getFullObject(fullObjects.get(file)));
            } catch (Exception e) {
                log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Couldn't get full object for %s", file.filename()));
                return null;
            }
        }

        private S3Object getFullObject(S3Object s3Object) throws S3ServiceException {
            return service.getObject(s3Object.getBucketName(), s3Object.getKey());
        }

        @Override
        public WsDataSource getAudioItem() {
            return inputStreamFor(WsDataFile.AUDIO_ITEM);
        }

        @Override
        public WsDataSource getAudioItemProgLink() {
            return inputStreamFor(WsDataFile.AUDIO_ITEM_PROG_LINK);
        }

        @Override
        public WsDataSource getGenre() {
            return inputStreamFor(WsDataFile.GENRE);
        }

        @Override
        public WsDataSource getProgramme() {
            return inputStreamFor(WsDataFile.PROGRAMME);
        }

        @Override
        public WsDataSource getSeries() {
            return inputStreamFor(WsDataFile.SERIES);
        }

        @Override
        public String getName() {
            return setName;
        }
        
    }
}
