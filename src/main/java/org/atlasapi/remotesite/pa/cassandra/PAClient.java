package org.atlasapi.remotesite.pa.cassandra;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.scheduling.RepetitionRule;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import java.util.Properties;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.content.cassandra.CassandraContentStore;
import org.atlasapi.remotesite.pa.PaChannelProcessor;
import org.atlasapi.remotesite.pa.PaCompleteUpdater;
import org.atlasapi.remotesite.pa.PaProgrammeProcessor;
import org.atlasapi.remotesite.pa.data.DefaultPaProgrammeDataStore;
import org.atlasapi.s3.DefaultS3Client;

/**
 */
public class PAClient {

    public static void main(String[] args) throws Exception {
        String operation = args[0];
        if (operation.equals("INGEST")) {
            Properties props = new Properties();
            props.load(PAClient.class.getClassLoader().getResourceAsStream("cassandra.ingestion.properties"));

            String localPath = Optional.fromNullable(props.getProperty("local.path")).get();
            String cassandraHost = Optional.fromNullable(props.getProperty("cassandra.host")).get();
            String s3Bucket = Optional.fromNullable(props.getProperty("s3.bucket")).get();
            String s3Access = Optional.fromNullable(props.getProperty("s3.access")).get();
            String s3Key = Optional.fromNullable(props.getProperty("s3.key")).get();

            CassandraContentStore store = new CassandraContentStore(9160, ImmutableList.of(cassandraHost));

            DefaultPaProgrammeDataStore s3Store = new DefaultPaProgrammeDataStore(localPath, new DefaultS3Client(s3Access, s3Key, s3Bucket));
            PaProgrammeProcessor processor = new PaProgrammeProcessor(store, store, new DummyChannelResolver(), new DummyPeopleWriter(), new DummyAdapterLog());
            PaCompleteUpdater paUpdater = new PaCompleteUpdater(new PaChannelProcessor(processor, null, new DummyScheduleWriter(), new DummyAdapterLog()), s3Store, new DummyChannelResolver(), new DummyAdapterLog());

            SimpleScheduler scheduler = new SimpleScheduler();
            scheduler.schedule(paUpdater, RepetitionRules.NEVER);

            paUpdater.run();
        } else {
            String uri = operation;

            Properties props = new Properties();
            props.load(PAClient.class.getClassLoader().getResourceAsStream("cassandra.ingestion.properties"));

            String cassandraHost = Optional.fromNullable(props.getProperty("cassandra.host")).get();

            CassandraContentStore store = new CassandraContentStore(9160, ImmutableList.of(cassandraHost));

            ResolvedContent content = store.findByCanonicalUris(ImmutableList.of(uri));
            if (!content.isEmpty()) {
                System.out.println(content.getFirstValue().requireValue());
            }
        }
    }
}
