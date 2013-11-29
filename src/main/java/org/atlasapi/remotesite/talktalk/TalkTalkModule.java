package org.atlasapi.remotesite.talktalk;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.remotesite.HttpClients;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xml.sax.SAXException;

import com.google.common.base.Optional;
import com.google.common.io.Resources;
import com.google.common.net.HostSpecifier;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.scheduling.SimpleScheduler;

/**
 * Module for ingesting data from TalkTalk's TVDataInterface API.
 * 
 * The {@link TalkTalkChannelProcessingTask} uses a {@link TalkTalkClient} to
 * regularly pull in Content.
 * 
 * There is also the {@link TalkTalkContentUpdateController} for manually
 * triggered specific updates.
 */
@Configuration
public class TalkTalkModule {

    private static final String SCHEMA_FILENAME = "TVDataInterfaceModel.xsd";

    @Autowired private ContentResolver contentResolver;
    @Autowired private ContentWriter contentWriter;
    @Autowired private ContentGroupResolver contentGroupResolver;
    @Autowired private ContentGroupWriter contentGroupWriter;
    @Autowired private SimpleScheduler scheduler;

    private @Value("${talktalk.host}") String ttHost;
    private @Value("${talktalk.validate}") String validate;

    @PostConstruct
    public void schedule() {
        scheduler.schedule(talkTalkUpdater().withName("TalkTalk Content Updater"), 
                RepetitionRules.daily(new LocalTime(10, 00)));
        scheduler.schedule(talkTalkVodPicksUpdater().withName("TalkTalk VOD Picks Processor"), 
                RepetitionRules.daily(new LocalTime(11, 00)));
    }
    
    @Bean
    public ScheduledTask talkTalkVodPicksUpdater() {
        return new TalkTalkVodContentListUpdateTask(talkTalkClient(),
                contentGroupResolver, contentGroupWriter, contentResolver,
                GroupType.IMAGE, "COMPAPP2");
    }

    @Bean
    public ScheduledTask talkTalkUpdater() {
        return new TalkTalkChannelProcessingTask(talkTalkClient(), talkTalkChannelProcessor(), 
                contentGroupResolver, contentGroupWriter);
    }

    @Bean
    public VodEntityProcessingTalkTalkChannelProcessor talkTalkChannelProcessor() {
        return new VodEntityProcessingTalkTalkChannelProcessor(talkTalkClient(), talkTalkContentEntityProcessor());
    }

    @Bean
    public ContentUpdatingTalkTalkVodEntityProcessor talkTalkContentEntityProcessor() {
        return new ContentUpdatingTalkTalkVodEntityProcessor(talkTalkClient(), contentResolver, contentWriter);
    }

    @Bean
    public TalkTalkClient talkTalkClient() {
        return new XmlTalkTalkClient(HttpClients.webserviceClient(), 
            HostSpecifier.fromValid(ttHost),
            new JaxbTalkTalkTvDataInterfaceResponseParser(getJaxbContext(), getSchema()));
    }

    @Bean
    public TalkTalkContentUpdateController talkTalkChannelUpdateController() {
        return new TalkTalkContentUpdateController(talkTalkChannelProcessor(), talkTalkContentEntityProcessor());
    }

    private JAXBContext getJaxbContext() {
        try {
            return JAXBContext.newInstance("org.atlasapi.remotesite.talktalk.vod.bindings");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<Schema> getSchema() {
        if (!Boolean.parseBoolean(validate)) {
            return Optional.absent();
        }
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            return Optional.of(schemaFactory.newSchema(Resources.getResource(SCHEMA_FILENAME)));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

}
