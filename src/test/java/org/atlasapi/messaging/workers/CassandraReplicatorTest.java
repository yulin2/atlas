package org.atlasapi.messaging.workers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.time.DateTimeZones;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.messaging.EntityUpdatedMessage;
import org.atlasapi.messaging.Message;
import org.atlasapi.serialization.json.JsonFactory;
import org.joda.time.DateTime;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 */
public class CassandraReplicatorTest {

    private final static ObjectMapper MAPPER = JsonFactory.makeJsonMapper();
    private final DateTime now = new DateTime(DateTimeZones.UTC);
    
    @Test
    public void testProcessContainer() throws IOException {
        final String uri = "http://metabroadcast.com/1";
        final Container container = mock(Container.class);

        ContentResolver resolver = mock(ContentResolver.class);
        when(resolver.findByCanonicalUris(Arrays.asList(uri))).thenReturn(new ResolvedContent(new HashMap<String, Maybe<Identified>>() {

            {
                put(uri, Maybe.just((Identified) container));
            }
        }));

        ContentWriter writer = mock(ContentWriter.class);

        CassandraReplicator cassandraReplicator = new CassandraReplicator(resolver, writer);
        cassandraReplicator.onMessage(marshal(new EntityUpdatedMessage("0", now, uri, "", Publisher.BBC.key())));

        verify(writer).createOrUpdate(same(container));
    }

    @Test
    public void testProcessItem() throws IOException {
        final String uri = "http://metabroadcast.com/1";
        final Item item = mock(Item.class);

        ContentResolver resolver = mock(ContentResolver.class);
        when(resolver.findByCanonicalUris(Arrays.asList(uri))).thenReturn(new ResolvedContent(new HashMap<String, Maybe<Identified>>() {

            {
                put(uri, Maybe.just((Identified) item));
            }
        }));

        ContentWriter writer = mock(ContentWriter.class);

        CassandraReplicator cassandraReplicator = new CassandraReplicator(resolver, writer);
        cassandraReplicator.onMessage(marshal(new EntityUpdatedMessage("0", now, uri, "", Publisher.BBC.key())));

        verify(writer).createOrUpdate(same(item));
    }

    private String marshal(Message event) throws IOException {
        return MAPPER.writeValueAsString(event);
    }
}
