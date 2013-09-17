package org.atlasapi;

import org.atlasapi.application.ApplicationModule;
import org.atlasapi.application.OldApplicationModule;
import org.atlasapi.equiv.EquivModule;
import org.atlasapi.equiv.EquivTaskModule;
import org.atlasapi.feeds.AtlasFeedsModule;
import org.atlasapi.feeds.interlinking.delta.InterlinkingDeltaModule;
import org.atlasapi.feeds.radioplayer.RadioPlayerModule;
import org.atlasapi.feeds.xmltv.XmlTvModule;
import org.atlasapi.feeds.youview.YouViewUploadModule;
import org.atlasapi.logging.AtlasLoggingModule;
import org.atlasapi.messaging.AtlasMessagingModule;
import org.atlasapi.messaging.WorkersModule;
import org.atlasapi.persistence.AtlasPersistenceModule;
import org.atlasapi.query.QueryModule;
import org.atlasapi.query.QueryWebModule;
import org.atlasapi.remotesite.RemoteSiteModule;
import org.atlasapi.remotesite.RemoteSiteModuleConfigurer;
import org.atlasapi.remotesite.health.RemoteSiteHealthModule;
import org.atlasapi.system.BootstrapModule;
import org.atlasapi.system.DebugModule;
import org.atlasapi.system.SystemModule;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import com.metabroadcast.common.properties.Configurer;

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
            AtlasModule.class,
            AtlasMessagingModule.class,
            AtlasPersistenceModule.class,
            AtlasLoggingModule.class,
            AtlasWebModule.class,
            QueryModule.class,
            AtlasFetchModule.class,
//            RemoteSiteModule.class,
            SystemModule.class,
//            RadioPlayerModule.class,
//            XmlTvModule.class, 
            RemoteSiteHealthModule.class/*,
            EquivModule.class*/
        );
        
        if(runProcessingOnly()) {
            builder.add(
//                InterlinkingDeltaModule.class,
//                EquivTaskModule.class,
//                AtlasMessagingModule.class,
                WorkersModule.class,
                BootstrapModule.class,
                DebugModule.class
            );
            if (Configurer.get("youview.upload.enabled").toBoolean()) {
                builder.add(YouViewUploadModule.class);
            }
            builder.addAll(new RemoteSiteModuleConfigurer().enabledModules());
        } else {
            builder.add(
//                AtlasFeedsModule.class,
                QueryWebModule.class,
                OldApplicationModule.class,
                ApplicationModule.class
            );
        }
    }

    private boolean runProcessingOnly() {
        return Boolean.parseBoolean(System.getProperty("processing.config"));
    }
}
