package org.atlasapi;

import com.metabroadcast.common.ids.UUIDGenerator;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.mongodb.Mongo;
import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.persistence.media.channel.MongoChannelStore;
import org.atlasapi.persistence.media.channel.cassandra.CassandraChannelStore;
import org.joda.time.Duration;
import static org.atlasapi.persistence.cassandra.CassandraSchema.*;

public class CassandraChannelInitialPopulate {

    public static void main(String[] args) throws Exception {
        Mongo mongo = new Mongo("127.0.0.1");
        String dbName = "atlas";
        DatabasedMongo dbMongo = new DatabasedMongo(mongo, dbName);
        AstyanaxContext<Keyspace> context = new AstyanaxContext.Builder().forCluster(CLUSTER).forKeyspace(getKeyspace("stage")).
                withAstyanaxConfiguration(new AstyanaxConfigurationImpl().setDiscoveryType(NodeDiscoveryType.NONE)).
                withConnectionPoolConfiguration(new ConnectionPoolConfigurationImpl(CLUSTER).setPort(9160).
                setMaxBlockedThreadsPerHost(Runtime.getRuntime().availableProcessors() * 10).
                setMaxConnsPerHost(Runtime.getRuntime().availableProcessors() * 10).
                setConnectTimeout(10000).
                setSeeds("cassandra1.owl.atlas.mbst.tv")).
                withConnectionPoolMonitor(new CountingConnectionPoolMonitor()).
                buildKeyspace(ThriftFamilyFactory.getInstance());
        context.start();
        //
        MongoChannelStore source = new MongoChannelStore(dbMongo);
        //
        CassandraChannelStore destination = new CassandraChannelStore(context, 60000, new UUIDGenerator(), Duration.standardHours(1));
        System.out.println(String.format("Writing channels to %s", destination.getClass().getName()));
        System.out.println(String.format("Wrote %s channels", writeChannels(source, destination)));
    }

    public static int writeChannels(MongoChannelStore source, CassandraChannelStore destination) {
        int written = 0;
        for (Channel channel : source.all()) {
            destination.write(channel);
            written++;
        }
        return written;
    }
}
