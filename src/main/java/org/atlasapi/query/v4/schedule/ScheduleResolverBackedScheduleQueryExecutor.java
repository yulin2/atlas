package org.atlasapi.query.v4.schedule;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.List;

import org.atlasapi.equiv.MergingEquivalentsResolver;
import org.atlasapi.equiv.ResolvedEquivalents;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.schedule.ScheduleResolver;
import org.atlasapi.media.entity.ChannelSchedule;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.media.util.Identifiables;
import org.atlasapi.media.util.ItemAndBroadcast;
import org.atlasapi.output.NotFoundException;
import org.atlasapi.query.common.QueryContext;
import org.atlasapi.query.common.QueryExecutionException;
import org.atlasapi.query.common.QueryResult;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.metabroadcast.common.base.Maybe;


public class ScheduleResolverBackedScheduleQueryExecutor implements ScheduleQueryExecutor {

    private static final long QUERY_TIMEOUT = 60000;

    private ChannelResolver channelResolver;
    private ScheduleResolver scheduleResolver;
    private MergingEquivalentsResolver<Content> mergingContentResolver;

    public ScheduleResolverBackedScheduleQueryExecutor(ChannelResolver channelResolver,
            ScheduleResolver scheduleResolver, MergingEquivalentsResolver<Content> mergingContentResolver) {
        this.channelResolver = checkNotNull(channelResolver);
        this.scheduleResolver = checkNotNull(scheduleResolver);
        this.mergingContentResolver = checkNotNull(mergingContentResolver);
    }

    @Override
    public QueryResult<ChannelSchedule> execute(ScheduleQuery query)
            throws QueryExecutionException {
        
        Channel channel = resolveChannel(query);
        ListenableFuture<Schedule> resolve = scheduleResolver.resolve(ImmutableList.of(channel), query.getInterval(), query.getSource());
        
        return QueryResult.singleResult(channelSchedule(resolve, query), query.getContext());
    }

    private ChannelSchedule channelSchedule(ListenableFuture<Schedule> schedule, ScheduleQuery query) throws ScheduleQueryExecutionException {
        
        if (query.getContext().getApplicationSources().isPrecedenceEnabled()) {
            schedule = Futures.transform(schedule, toEquivalentEntries(query));
        }
        
        return Iterables.getOnlyElement(Futures.get(schedule,
                QUERY_TIMEOUT, MILLISECONDS, ScheduleQueryExecutionException.class).channelSchedules()); 
    }

    private AsyncFunction<Schedule, Schedule> toEquivalentEntries(final ScheduleQuery query) {
        return new AsyncFunction<Schedule, Schedule>() {
            @Override
            public ListenableFuture<Schedule> apply(Schedule input) {
                ChannelSchedule channelSchedule = Iterables.getOnlyElement(input.channelSchedules());
                return Futures.transform(resolveEquivalents(channelSchedule, query.getContext()), toSchedule(channelSchedule));
            }
        };
    }

    private Function<List<ItemAndBroadcast>, Schedule> toSchedule(final ChannelSchedule channelSchedule) {
        return new Function<List<ItemAndBroadcast>, Schedule>() {
            @Override
            public Schedule apply(List<ItemAndBroadcast> input) {
                List<ChannelSchedule> schedules = ImmutableList.of(channelSchedule.copyWithEntries(input));
                return new Schedule(schedules, channelSchedule.getInterval());
            }
        };
    }
    
    private ListenableFuture<List<ItemAndBroadcast>> resolveEquivalents(ChannelSchedule schedule,
            QueryContext context) {
        return replaceItems(schedule.getEntries(), mergingContentResolver.resolveIds(idsFrom(schedule), context.getApplicationSources(), context.getAnnotations().all()));
    }

    private ListenableFuture<List<ItemAndBroadcast>> replaceItems(final List<ItemAndBroadcast> entries,
            ListenableFuture<ResolvedEquivalents<Content>> resolveIds) {
        return Futures.transform(resolveIds,
            new Function<ResolvedEquivalents<Content>, List<ItemAndBroadcast>>() {
                @Override
                public List<ItemAndBroadcast> apply(ResolvedEquivalents<Content> input) {
                    return replaceItems(entries, input);
                }
            }
        );
    }

    private List<ItemAndBroadcast> replaceItems(List<ItemAndBroadcast> entries,
            final ResolvedEquivalents<Content> equivs) {
        return Lists.transform(entries, new Function<ItemAndBroadcast, ItemAndBroadcast>() {
            @Override
            public ItemAndBroadcast apply(ItemAndBroadcast input) {
                Item item = (Item) Iterables.getOnlyElement(equivs.get(input.getItem().getId()));
                return new ItemAndBroadcast(item, input.getBroadcast());
            }
        });
    }
    
    private Iterable<Id> idsFrom(ChannelSchedule channelSchedule) {
        return Lists.transform(channelSchedule.getEntries(), 
                Functions.compose(Identifiables.toId(), ItemAndBroadcast.toItem()));
    }

    private Channel resolveChannel(ScheduleQuery query) throws NotFoundException {
        Maybe<Channel> possibleChannel = channelResolver.fromId(query.getChannelId());
        if (!possibleChannel.hasValue()) {
            throw new NotFoundException(query.getChannelId());
        }
        return possibleChannel.requireValue();
    }

}
