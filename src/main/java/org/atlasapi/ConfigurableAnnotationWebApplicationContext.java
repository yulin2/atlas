package org.atlasapi;

import org.atlasapi.application.ApplicationModule;
import org.atlasapi.equiv.EquivModule;
import org.atlasapi.feeds.AtlasFeedsModule;
import org.atlasapi.feeds.interlinking.delta.InterlinkingDeltaModule;
import org.atlasapi.feeds.radioplayer.RadioPlayerModule;
import org.atlasapi.feeds.xmltv.XmlTvModule;
import org.atlasapi.logging.AtlasLoggingModule;
import org.atlasapi.logging.HealthModule;
import org.atlasapi.messaging.WorkersModule;
import org.atlasapi.messaging.MessagingModule;
import org.atlasapi.persistence.AtlasPersistenceModule;
import org.atlasapi.query.QueryModule;
import org.atlasapi.query.QueryWebModule;
import org.atlasapi.remotesite.RemoteSiteModule;
import org.atlasapi.remotesite.RemoteSiteModuleConfigurer;
import org.atlasapi.remotesite.health.RemoteSiteHealthModule;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;

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
        builder.add(
                AtlasPersistenceModule.class,
                AtlasLoggingModule.class,
                AtlasWebModule.class,
                AtlasFetchModule.class,
                QueryModule.class,
                RemoteSiteModule.class,
                HealthModule.class,
                RadioPlayerModule.class,
                XmlTvModule.class,
                RemoteSiteHealthModule.class);

        if (runProcessingOnly()) {
            builder.add(
                    MessagingModule.class,
                    WorkersModule.class,
                    EquivModule.class,
                    ManualScheduleRebuildModule.class,
                    InterlinkingDeltaModule.class);
            builder.addAll(new RemoteSiteModuleConfigurer().enabledModules());
        } else {
            builder.add(
                    AtlasFeedsModule.class,
                    QueryWebModule.class,
                    ApplicationModule.class);
        }
    }

    private boolean runProcessingOnly() {
        return Boolean.parseBoolean(System.getProperty("processing.config"));
    }
}
