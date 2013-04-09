package org.atlasapi.remotesite.redux;

import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.atlasapi.feeds.utils.UpdateProgress;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.redux.model.BaseReduxProgramme;
import org.atlasapi.remotesite.redux.model.PaginatedBaseProgrammes;

import com.google.common.base.Function;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.scheduling.ScheduledTask;

public final class ReduxLatestUpdateTasks {
    
    private static final int SELECTION_LIMIT = 25;
    private static final Executor sharedExecutor = new ThreadPoolExecutor(0, 20, 1, TimeUnit.MINUTES, new SynchronousQueue<Runnable>(), new ThreadFactoryBuilder().setNameFormat("Redux Latest Thread %d").build(), new ThreadPoolExecutor.CallerRunsPolicy());
    
    public static final ScheduledTask firstBatchOnlyReduxLatestTask(ReduxClient client, ContentWriter writer, SiteSpecificAdapter<Item> adapter, AdapterLog log) {
        return taskFor(new BaseReduxLatestTaskProducer(client, writer, adapter, log) {
            
            @Override
            protected boolean finished(PaginatedBaseProgrammes lastBatch) {
                return true;
            }
        });
    }

    public static final ScheduledTask untilFoundReduxLatestTask(ReduxClient client, ContentWriter writer, SiteSpecificAdapter<Item> adapter, AdapterLog log, final ContentResolver resolver) {
        return taskFor(new BaseReduxLatestTaskProducer(client, writer, adapter, log) {
            
            @Override
            protected boolean finished(PaginatedBaseProgrammes lastBatch) {
                BaseReduxProgramme first = lastBatch.getResults().get(0);
                String firstUri = FullProgrammeItemExtractor.REDUX_URI_BASE + first.getCanonical();
                Maybe<Identified> possibleContent = resolver.findByCanonicalUris(ImmutableList.of(firstUri)).getFirstValue();
                return possibleContent.hasValue();
            }
        });
    }
    
    public static final ScheduledTask completeReduxLatestTask(ReduxClient client, ContentWriter writer, SiteSpecificAdapter<Item> adapter, AdapterLog log) {
        return taskFor(new BaseReduxLatestTaskProducer(client, writer, adapter, log) {
            
            @Override
            protected boolean finished(PaginatedBaseProgrammes lastBatch) {
                return lastBatch.getResults().size() <= SELECTION_LIMIT;
            }
        });
    }
    
    public static final ScheduledTask maximumReduxLatestTask(final int maximum, ReduxClient client, ContentWriter writer, SiteSpecificAdapter<Item> adapter, AdapterLog log) {
        return taskFor(new BaseReduxLatestTaskProducer(client, writer, adapter, log) {
            
            private int seen = 0;
            private int max = maximum;
            
            @Override
            protected boolean finished(PaginatedBaseProgrammes lastBatch) {
                seen += lastBatch.getResults().size();
                return seen >= max;
            }
            
        });
    }
    
    private static ResultProcessingScheduledTask<UpdateProgress,UpdateProgress> taskFor(Iterable<Callable<UpdateProgress>> taskProducer) {
        ResultProcessor<? super UpdateProgress,UpdateProgress> taskProcessor = new ResultProcessor<UpdateProgress, UpdateProgress>() {
            @Override
            public UpdateProgress process(UpdateProgress input) {
                return input;
            }
        };
        return new ResultProcessingScheduledTask<UpdateProgress,UpdateProgress>(taskProducer, taskProcessor , sharedExecutor);
    }

    private static abstract class BaseReduxLatestTaskProducer implements Iterable<Callable<UpdateProgress>> {
        
        private final ReduxClient client;
        private final ReduxDiskrefUpdateTask.Builder taskBuilder;
        private final AdapterLog log;

        public BaseReduxLatestTaskProducer(ReduxClient client, ContentWriter writer, SiteSpecificAdapter<Item> adapter, AdapterLog log) {
            this.client = client;
            this.log = log;
            this.taskBuilder = ReduxDiskrefUpdateTask.diskRefUpdateTaskBuilder(writer, adapter, log);
        }
        
        @Override
        public Iterator<Callable<UpdateProgress>> iterator() {
            return new AbstractIterator<Callable<UpdateProgress>>() {

                private PaginatedBaseProgrammes lastBatch;
                private Iterator<Callable<UpdateProgress>> currentBatch = Iterators.emptyIterator();
                private Selection currentSelection = Selection.ALL;
                
                @Override
                protected Callable<UpdateProgress> computeNext() {
                    while(!currentBatch.hasNext()) {
                        if(lastBatch != null && finished(lastBatch)) {
                            return endOfData();
                        } else {
                            lastBatch = batchForSelection(currentSelection);
                            if (lastBatch != null) {
                                currentBatch = transformedIterator(lastBatch);
                                currentSelection = new Selection(lastBatch.getLast() + SELECTION_LIMIT);
                            } else {
                                return endOfData();
                            }
                        }
                    }
                    return currentBatch.next();
                }
            };
        }
        
        private PaginatedBaseProgrammes batchForSelection(Selection selection) {
            try {
                return client.latest(selection);
            } catch (Exception e) {
                log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Couldn't get Latest for %s", selection));
                return null;
            }
        }
        
        private Iterator<Callable<UpdateProgress>> transformedIterator(PaginatedBaseProgrammes latestBatch) {
            return Iterators.transform(latestBatch.getResults().iterator(), new Function<BaseReduxProgramme, Callable<UpdateProgress>>() {
                @Override
                public Callable<UpdateProgress> apply(BaseReduxProgramme programme) {
                    return taskBuilder.updateFor(programme.getDiskref());
                }
            });
        }
        
        protected abstract boolean finished(PaginatedBaseProgrammes lastBatch);
        
    }
    
    private ReduxLatestUpdateTasks(){};
}
