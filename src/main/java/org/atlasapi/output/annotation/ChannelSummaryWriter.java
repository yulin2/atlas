package org.atlasapi.output.annotation;

import java.io.IOException;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.query.v4.schedule.FieldWriter;
import org.atlasapi.query.v4.schedule.OutputContext;


public class ChannelSummaryWriter extends OutputAnnotation<Channel> {

    public ChannelSummaryWriter() {
        super(Channel.class);
    }

    @Override
    public void write(Channel entity, FieldWriter writer, OutputContext ctxt) throws IOException {
        writer.writeField("title", entity.title());
        writer.writeField("image", null);
    }

}
