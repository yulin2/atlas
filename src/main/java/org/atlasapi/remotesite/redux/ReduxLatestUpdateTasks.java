package org.atlasapi.remotesite.redux;

import java.util.concurrent.ExecutorService;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.redux.model.BaseReduxProgramme;
import org.atlasapi.remotesite.redux.model.PaginatedBaseProgrammes;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;

public final class ReduxLatestUpdateTasks {

    private ReduxLatestUpdateTasks(){};
    
    public static final class TillFoundReduxLatestUpdateTask extends ReduxLatestUpdateTask {

        private final ContentResolver resolver;

        public TillFoundReduxLatestUpdateTask(ContentResolver resolver, ReduxClient client, ReduxProgrammeHandler handler, AdapterLog log) {
            super(client, handler, log);
            this.resolver = resolver;
        }

        public TillFoundReduxLatestUpdateTask(ContentResolver resolver, ReduxClient client, ReduxProgrammeHandler handler, AdapterLog log, ExecutorService executor) {
            super(client, handler, log, executor);
            this.resolver = resolver;
        }

        @Override
        protected boolean finished(PaginatedBaseProgrammes programmes) {
            BaseReduxProgramme first = programmes.getResults().get(0);
            String firstUri = FullProgrammeItemExtractor.CANONICAL_URI_BASE + first.getCanonical();
            Maybe<Identified> possibleContent = resolver.findByCanonicalUris(ImmutableList.of(firstUri)).get(firstUri);
            return possibleContent.hasValue();
        }

    }
    
    public static final class FullReduxLatestUpdateTask extends ReduxLatestUpdateTask {

        public FullReduxLatestUpdateTask(ReduxClient client, ReduxProgrammeHandler handler, AdapterLog log) {
            super(client, handler, log);
        }

        public FullReduxLatestUpdateTask(ReduxClient client, ReduxProgrammeHandler handler, AdapterLog log, ExecutorService executor) {
            super(client, handler, log, executor);
        }

        @Override
        protected boolean finished(PaginatedBaseProgrammes programmes) {
            return programmes.getResults().size() <= limit();
        }

    }

    public static final class FirstPageReduxLatestUpdateTask extends ReduxLatestUpdateTask {

        public FirstPageReduxLatestUpdateTask(ReduxClient client, ReduxProgrammeHandler handler, AdapterLog log, ExecutorService executor) {
            super(client, handler, log, executor);
        }

        public FirstPageReduxLatestUpdateTask(ReduxClient client, ReduxProgrammeHandler handler, AdapterLog log) {
            super(client, handler, log);
        }

        @Override
        protected boolean finished(PaginatedBaseProgrammes programmes) {
            return false;
        }

    }
    
    public static final class MaximumReduxLatestUpdateTask extends ReduxLatestUpdateTask {

        public MaximumReduxLatestUpdateTask(int max, ReduxClient client, ReduxProgrammeHandler handler, AdapterLog log, ExecutorService executor) {
            super(client, handler, log, executor);
            this.max = max;
        }

        public MaximumReduxLatestUpdateTask(int max, ReduxClient client, ReduxProgrammeHandler handler, AdapterLog log) {
            super(client, handler, log);
            this.max = max;
        }
        
        private int seen = 0;
        private int max = 1000;
        
        @Override
        protected boolean finished(PaginatedBaseProgrammes programmes) {
            seen += programmes.getResults().size();
            return seen >= max;
        }
        
    }
}
