package org.atlasapi.output.annotation;

import static org.atlasapi.media.entity.Broadcast.ACTIVELY_PUBLISHED;
import static org.atlasapi.media.entity.Version.TO_BROADCASTS;

import java.io.IOException;
import java.util.Set;

import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Version;
import org.atlasapi.output.FieldWriter;
import org.atlasapi.output.OutputContext;
import org.atlasapi.output.writers.BroadcastWriter;

import com.google.common.collect.Iterables;

public class BroadcastsAnnotation extends OutputAnnotation<Content> {
    
    private final BroadcastWriter broadcastWriter;
    
    public BroadcastsAnnotation() {
        super();
        broadcastWriter = new BroadcastWriter("broadcasts");
    }

    @Override
    public void write(Content entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        if (entity instanceof Item) {
            writeBroadcasts(writer, (Item) entity, ctxt);
        }
    }

    private void writeBroadcasts(FieldWriter writer, Item item, OutputContext ctxt) throws IOException {
        Set<Version> versions = item.getVersions();
        Iterable<Broadcast> broadcasts = Iterables.concat(Iterables.transform(versions, TO_BROADCASTS));
        writer.writeList(broadcastWriter, Iterables.filter(broadcasts, ACTIVELY_PUBLISHED), ctxt);
    }

}
