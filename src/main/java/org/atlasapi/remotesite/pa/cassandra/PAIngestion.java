package org.atlasapi.remotesite.pa.cassandra;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;
import java.io.IOException;
import java.util.Properties;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.cassandra.CassandraContentStore;
import org.atlasapi.remotesite.pa.PaChannelProcessor;
import org.atlasapi.remotesite.pa.PaCompleteUpdater;
import org.atlasapi.remotesite.pa.PaProgrammeProcessor;
import org.atlasapi.remotesite.pa.data.DefaultPaProgrammeDataStore;
import org.atlasapi.s3.DefaultS3Client;
import org.atlasapi.s3.S3Client;

/**
 */
public class PAIngestion {
   
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.load(PAIngestion.class.getClassLoader().getResourceAsStream("cassandra.ingestion.properties"));
        String localPath = Optional.fromNullable(props.getProperty("local.path")).get();
        String cassandraHost = Optional.fromNullable(props.getProperty("cassandra.host")).get();
        String s3Bucket = Optional.fromNullable(props.getProperty("s3.bucket")).get();
        String s3Access = Optional.fromNullable(props.getProperty("s3.access")).get();
        String s3Key = Optional.fromNullable(props.getProperty("s3.key")).get();
        
        CassandraContentStore store = new CassandraContentStore(9160, ImmutableList.of(cassandraHost));
        
        DefaultPaProgrammeDataStore s3Store = new DefaultPaProgrammeDataStore(localPath, new DefaultS3Client(s3Access, s3Key, s3Bucket));
        PaProgrammeProcessor processor = new PaProgrammeProcessor(store, store, new DummyChannelResolver(), new DummyPeopleWriter(), new DummyAdapterLog());
        PaCompleteUpdater paUpdater = new PaCompleteUpdater(new PaChannelProcessor(processor, null, new DummyScheduleWriter(), new DummyAdapterLog()), s3Store, new DummyChannelResolver(), new DummyAdapterLog());
    
        paUpdater.runTask();
    }
}
