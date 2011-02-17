/* Copyright 2010 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.atlasapi.remotesite;

import java.util.List;

import javax.annotation.PostConstruct;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.system.Fetcher;
import org.atlasapi.remotesite.archiveorg.ArchiveOrgModule;
import org.atlasapi.remotesite.bbc.BbcModule;
import org.atlasapi.remotesite.bbc.BbcPodcastAdapter;
import org.atlasapi.remotesite.bbc.BbcProgrammeAdapter;
import org.atlasapi.remotesite.bliptv.BlipTvAdapter;
import org.atlasapi.remotesite.channel4.C4Module;
import org.atlasapi.remotesite.dailymotion.DailyMotionItemAdapter;
import org.atlasapi.remotesite.hbo.HboModule;
import org.atlasapi.remotesite.hulu.HuluModule;
import org.atlasapi.remotesite.ictomorrow.ICTomorrowModule;
import org.atlasapi.remotesite.imdb.ImdbAdapter;
import org.atlasapi.remotesite.itunes.ItunesModule;
import org.atlasapi.remotesite.itv.ItvModule;
import org.atlasapi.remotesite.msnvideo.MsnVideoModule;
import org.atlasapi.remotesite.oembed.OembedXmlAdapter;
import org.atlasapi.remotesite.pa.PaModule;
import org.atlasapi.remotesite.seesaw.SeesawModule;
import org.atlasapi.remotesite.synd.OpmlAdapter;
import org.atlasapi.remotesite.ted.TedTalkAdapter;
import org.atlasapi.remotesite.vimeo.VimeoAdapter;
import org.atlasapi.remotesite.youtube.YouTubeAdapter;
import org.atlasapi.remotesite.youtube.YouTubeFeedAdapter;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.google.common.collect.Lists;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.webapp.scheduling.ManualTaskTrigger;

@Configuration
@Import({ C4Module.class, ICTomorrowModule.class, BbcModule.class, ItvModule.class, ArchiveOrgModule.class, HuluModule.class, HboModule.class, ItunesModule.class, MsnVideoModule.class,
        PaModule.class, SeesawModule.class })
public class RemoteSiteModule {

    private @Autowired AdapterLog log;
    private @Autowired C4Module c4Module;
    private @Autowired ArchiveOrgModule archiveOrgModule;
    private @Autowired HboModule hboModule;

    private @Autowired BbcModule bbcModule;
    private @Autowired ItvModule itvModule;
    private @Autowired ItunesModule itunesModule;
    private @Autowired MsnVideoModule msnVideoModule;
    private @Autowired HuluModule huluModule;

    private @Autowired DatabasedMongo mongo;

    public @Bean SimpleScheduler scheduler() {
        return new SimpleScheduler();
    }

    @PostConstruct
    public void scheduleAvailabilityUpdater() {
        scheduler().schedule(itemAvailabilityUpdater(), RepetitionRules.every(new Duration(30 * 60 * 1000L)));
    }

    public @Bean Runnable itemAvailabilityUpdater() {
        return new ItemAvailabilityUpdater(mongo, log);
    }

    public @Bean ManualTaskTrigger manualTaskTrigger() {
        return new ManualTaskTrigger(scheduler());
    }

    public @Bean Fetcher<Content> remoteFetcher() {

        PerSiteAdapterDispatcher dispatcher = new PerSiteAdapterDispatcher(log);

        List<SiteSpecificAdapter<? extends Content>> adapters = Lists.newArrayList();

        adapters.add(new YouTubeAdapter());
        adapters.add(new YouTubeFeedAdapter());
        // Commented out for now, as it generates too much gdata traffic
        // adapters.add(new YouTubeUserAdapter());
        adapters.add(new TedTalkAdapter());

        adapters.addAll(c4Module.adapters());
        adapters.addAll(bbcModule.adapters());
        adapters.addAll(itvModule.adapters());
        adapters.addAll(archiveOrgModule.adapters());
        adapters.addAll(hboModule.adapters());
        adapters.addAll(itunesModule.adapters());
        adapters.addAll(msnVideoModule.adapters());
        adapters.addAll(huluModule.adapters());

        adapters.add(new DailyMotionItemAdapter());
        adapters.add(new BlipTvAdapter());
        adapters.add(new BbcProgrammeAdapter(log));

        adapters.add(new BbcPodcastAdapter());

        adapters.add(new VimeoAdapter());

        OembedXmlAdapter flickrAdapter = new OembedXmlAdapter();
        flickrAdapter.setAcceptedUriPattern("http://www.flickr.com/photos/[^/]+/[\\d]+");
        flickrAdapter.setOembedEndpoint("http://www.flickr.com/services/oembed/");
        flickrAdapter.setPublisher(Publisher.FLICKR);

        adapters.add(flickrAdapter);
        adapters.add(new OpmlAdapter(dispatcher));

        // avoid overloading with equiv requests
        // adapters.add(new WikipediaSparqlAdapter());

        adapters.add(new ImdbAdapter(dispatcher));

        dispatcher.setAdapters(adapters);
        return dispatcher;
    }

    public @Bean ContentWriters contentWriters() {
        return new ContentWriters();
    }
}
