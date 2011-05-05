package org.atlasapi;

import org.atlasapi.application.ApplicationModule;
import org.atlasapi.equiv.EquivModule;
import org.atlasapi.feeds.AtlasFeedsModule;
import org.atlasapi.logging.AtlasLoggingModule;
import org.atlasapi.logging.HealthModule;
import org.atlasapi.persistence.MongoContentPersistenceModule;
import org.atlasapi.query.QueryModule;
import org.atlasapi.remotesite.RemoteSiteModule;
import org.atlasapi.remotesite.RemoteSiteModuleConfigurer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList.Builder;

public class ConfigurableAnnotationWebApplicationContext extends AnnotationConfigWebApplicationContext {

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
        
        if(runProcessingOnly()) {
            builder.add(AtlasLoggingModule.class, AtlasWebModule.class, MongoContentPersistenceModule.class, AtlasFetchModule.class, RemoteSiteModule.class, HealthModule.class);
            builder.addAll(new RemoteSiteModuleConfigurer().enabledModules());
        } else {
            builder.add(AtlasLoggingModule.class, AtlasWebModule.class, EquivModule.class, QueryModule.class, 
                    MongoContentPersistenceModule.class, AtlasFetchModule.class, RemoteSiteModule.class,
                    AtlasFeedsModule.class, HealthModule.class, ApplicationModule.class);
        }
        
        
    }

	private boolean runProcessingOnly() {
		return Boolean.parseBoolean(System.getProperty("processing.config"));
	}
}
