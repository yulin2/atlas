package org.atlasapi.query.v4.schedule;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.query.common.QueryContext;
import org.joda.time.Interval;

public final class ScheduleQuery {
    
    private final Publisher source;
    private final Channel channel;
    private final Interval interval;
    private final QueryContext context;

    public ScheduleQuery(Publisher source, Channel channel, Interval interval, QueryContext context) {
        this.source = checkNotNull(source);
        this.channel = checkNotNull(channel);
        this.interval = checkNotNull(interval);
        this.context = checkNotNull(context);
    }

    public Publisher getSource() {
        return source;
    }

    public Channel getChannel() {
        return channel;
    }
    
    public Interval getInterval() {
        return interval;
    }
    
    public QueryContext getContext() {
        return this.context;
    }

}
