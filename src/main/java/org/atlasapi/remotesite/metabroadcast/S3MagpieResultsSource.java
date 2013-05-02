//package org.atlasapi.remotesite.metabroadcast;
//
//import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;
//
//import java.io.InputStreamReader;
//import java.util.Comparator;
//import java.util.Date;
//import java.util.Iterator;
//import java.util.List;
//
//import javax.annotation.Nullable;
//
//import org.jets3t.service.S3Service;
//import org.jets3t.service.S3ServiceException;
//import org.jets3t.service.ServiceException;
//import org.jets3t.service.model.S3Object;
//
//import com.google.common.base.Predicate;
//import com.google.common.base.Throwables;
//import com.google.common.collect.AbstractIterator;
//import com.google.common.collect.ImmutableList;
//import com.google.common.collect.Iterables;
//import com.google.common.collect.Ordering;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.metabroadcast.common.time.Timestamp;
//
//public class S3MagpieResultsSource implements RemoteMagpieResultsSource {
//    
//    private final S3Service s3Service;
//    private final String s3Bucket;
//    private final String s3folder;
//    private final Gson gson;
//    
//    private static final Ordering<S3Object> LAST_MODIFIED_ORDERING = Ordering.from(new Comparator<S3Object>() {
//        @Override
//        public int compare(S3Object o1, S3Object o2) {
//            return o1.getLastModifiedDate().compareTo(o2.getLastModifiedDate());
//        }
//    });
//    
//    public S3MagpieResultsSource(S3Service s3Service, String s3Bucket, String s3folder) {
//        this.s3Service = s3Service;
//        this.s3Bucket = s3Bucket;
//        this.s3folder = s3folder;
//        this.gson = new GsonBuilder()
//            .setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES)
//            .create();
//    }
//    
//    @Override
//    public Iterable<RemoteMagpieResults> resultsChangeSince(final Timestamp since) {
//        return new Iterable<RemoteMagpieResults>(){
//            @Override
//            public Iterator<RemoteMagpieResults> iterator() {
//                try {
//                    return new S3MagpieResultsIterator(since);
//                } catch (S3ServiceException e) {
//                    throw Throwables.propagate(e);
//                }
//            }
//        };
//    }
//    
//    private final class S3MagpieResultsIterator extends AbstractIterator<RemoteMagpieResults> {
//            
//        private Iterator<S3Object> objectsToProcess;
//
//        public S3MagpieResultsIterator(Timestamp since) throws S3ServiceException {
//            this.objectsToProcess = getObjectList(since).iterator();
//        }
//
//        @Override
//        protected RemoteMagpieResults computeNext() {
//            if (objectsToProcess.hasNext()) {
//                try {
//                    S3Object nextObject = objectsToProcess.next();
//                    Date lastModifiedDate = nextObject.getLastModifiedDate();
//                    MagpieResults results = deserialize(retrieve(nextObject));
//                    return RemoteMagpieResults.retrieved(results, Timestamp.of(lastModifiedDate));
//                } catch (Exception e) {
//                    return RemoteMagpieResults.missing(e);
//                }
//            }
//            return endOfData();
//        }
//
//        private MagpieResults deserialize(S3Object object) throws ServiceException {
//            InputStreamReader stream = objectDataStream(object);
//            return gson.fromJson(stream, MagpieResults.class);
//        }
//
//        private InputStreamReader objectDataStream(S3Object object) throws ServiceException {
//            return new InputStreamReader(object.getDataInputStream());
//        }
//
//        private S3Object retrieve(S3Object object) throws S3ServiceException {
//            return s3Service.getObject(s3Bucket, object.getKey());
//        }
//
//        private List<S3Object> getObjectList(final Timestamp since) throws S3ServiceException {
//            return LAST_MODIFIED_ORDERING.immutableSortedCopy(Iterables.filter(
//                listObjects(),
//                objectsModifiedAfter(since)));
//        }
//
//        private ImmutableList<S3Object> listObjects() throws S3ServiceException {
//            return ImmutableList.copyOf(s3Service.listObjects(s3Bucket, s3folder + "/", ""));
//        }
//
//        private Predicate<S3Object> objectsModifiedAfter(final Timestamp since) {
//            return new Predicate<S3Object>() {
//                @Override
//                public boolean apply(@Nullable S3Object input) {
//                    return since.millis() < input.getLastModifiedDate().getTime();
//                }
//            };
//        }
//    };
//}