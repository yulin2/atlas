package org.atlasapi;

import org.atlasapi.logging.AtlasLoggingModule;
import org.atlasapi.persistence.MongoContentPersistenceModule;
import org.atlasapi.remotesite.RemoteSiteModule;
import org.atlasapi.remotesite.RemoteSiteModuleConfigurer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import org.atlasapi.messaging.MessagingModule;
import org.atlasapi.persistence.CassandraPersistenceModule;

public class AtlasProcessingWebApplicationContext extends AnnotationConfigWebApplicationContext {

    private static final Function<Class<?>, String> TO_FQN = new Function<Class<?>, String>() {
        @Override
        public String apply(Class<?> clazz) {
            return clazz.getCanonicalName();
        }
    };

    @Override
    public final void setConfigLocation(String location) {
        Builder<Class<?>> builder = ImmutableList.builder();
        configure(builder);
        super.setConfigLocations(Lists.transform(builder.build(), TO_FQN).toArray(new String[0]));
    }

    private void configure(Builder<Class<?>> builder) {
        builder.add(AtlasModule.class);        
        builder.add(AtlasLoggingModule.class, MongoContentPersistenceModule.class, CassandraPersistenceModule.class, AtlasFetchModule.class, RemoteSiteModule.class, MessagingModule.class);
        builder.addAll(new RemoteSiteModuleConfigurer().enabledModules());
    }
}