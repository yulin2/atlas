package org.atlasapi.remotesite.channel4.epg;

import static com.google.common.base.Predicates.notNull;
import static org.atlasapi.media.entity.Channel.CHANNEL_FOUR;
import static org.atlasapi.media.entity.Channel.E_FOUR;
import static org.atlasapi.media.entity.Channel.FILM_4;
import static org.atlasapi.media.entity.Channel.FOUR_MUSIC;
import static org.atlasapi.media.entity.Channel.MORE_FOUR;
import static org.atlasapi.persistence.logging.AdapterLogEntry.Severity.ERROR;
import static org.atlasapi.persistence.logging.AdapterLogEntry.Severity.WARN;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import nu.xom.Document;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import org.atlasapi.media.entity.Channel;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.common.time.DayRangeGenerator;

public class C4EpgUpdater implements Runnable {

    private final static Map<String, Channel> CHANNEL_MAP = ImmutableMap.of(
            "C4", CHANNEL_FOUR,
            "M4", MORE_FOUR,
            "F4", FILM_4,
            "E4", E_FOUR,
            "4M", FOUR_MUSIC
    );
    
    private final static String epgUriPattern = "http://api.channel4.com/pmlsd/tv-listings/daily/%s/%s.atom";

    private final RemoteSiteClient<Document> c4AtomFetcher;
    private final AdapterLog log;
    private final DayRangeGenerator rangeGenerator;
    private final C4EpgEntryProcessor entryProcessor;
    private final C4EpgBrandlessEntryProcessor brandlessEntryProcessor;
    private final BroadcastTrimmer trimmer;

    public C4EpgUpdater(RemoteSiteClient<Document> fetcher, ContentWriter writer, ContentResolver store, BroadcastTrimmer trimmer, AdapterLog log) {
        this(fetcher, new C4EpgEntryProcessor(writer, store, log), new C4EpgBrandlessEntryProcessor(writer, store, log), trimmer, log, new DayRangeGenerator().withLookAhead(7).withLookBack(7));
    }

    public C4EpgUpdater(RemoteSiteClient<Document> fetcher, C4EpgEntryProcessor entryProcessor, C4EpgBrandlessEntryProcessor brandlessEntryProcessor, BroadcastTrimmer trimmer, AdapterLog log, DayRangeGenerator generator) {
        this.c4AtomFetcher = fetcher;
        this.trimmer = trimmer;
        this.log = log;
        this.rangeGenerator = generator;
        this.entryProcessor = entryProcessor;
        this.brandlessEntryProcessor = brandlessEntryProcessor;
    }

    @Override
    public void run() {
        DateTime start = new DateTime(DateTimeZones.UTC);
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("C4 EPG Update initiated"));

        for (Map.Entry<String, Channel> channelEntry : CHANNEL_MAP.entrySet()) {
            for (LocalDate scheduleDay : rangeGenerator.generate(new LocalDate(DateTimeZones.UTC))) {
                updateChannelDay(channelEntry, scheduleDay);
            }
        }

        String runTime = new Period(start, new DateTime(DateTimeZones.UTC)).toString(PeriodFormat.getDefault());
        log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("C4 EPG Update finished in " + runTime));
    }

    public void updateChannelDay(Map.Entry<String, Channel> channelEntry, LocalDate scheduleDay) {
        String uri = uriFor(channelEntry.getKey(), scheduleDay);
        log.record(new AdapterLogEntry(Severity.DEBUG).withDescription("Updating from " + uri).withSource(getClass()));
        Document scheduleDocument = getSchedule(uri);
        if(scheduleDocument != null) {
            try {
                List<C4EpgEntry> entries = getEntries(scheduleDocument);
                trim(scheduleDay, channelEntry.getValue(), entries);
                process(entries, channelEntry.getValue());
            } catch (Exception e) {
                log.record(new AdapterLogEntry(ERROR).withCause(e).withSource(getClass()).withDescription("Exception updating from " + uri));
            }
        }
    }

    private void trim(LocalDate scheduleDay, Channel channel, Iterable<C4EpgEntry> entries) {
        DateTime scheduleStart = scheduleDay.toDateTime(new LocalTime(6,0,0), DateTimeZones.LONDON);
        Interval scheduleInterval = new Interval(scheduleStart, scheduleStart.plusDays(1));
        trimmer.trimBroadcasts(scheduleInterval, channel, broacastIdsFrom(entries));
    }

    private Collection<String> broacastIdsFrom(Iterable<C4EpgEntry> entries) {
        return ImmutableSet.copyOf(Iterables.filter(Iterables.transform(entries, new Function<C4EpgEntry, String>() {
            @Override
            public String apply(C4EpgEntry entry) {
                String slotId = entry.slotId();
                return slotId == null ? null : "c4:"+slotId;
            }
        }), notNull()));
    }

    private Document getSchedule(String uri) {
        try {
            return c4AtomFetcher.get(uri);
        } catch (Exception e) {
            log.record(new AdapterLogEntry(WARN).withCause(e).withSource(getClass()).withDescription("Exception fetching feed at " + uri));
            return null;
        }
    }

    private String uriFor(String channelKey, LocalDate scheduleDay) {
        return String.format(epgUriPattern, scheduleDay.toString("yyyy/MM/dd"), channelKey);
    }

    private void process(Iterable<C4EpgEntry> entries, Channel channel) {
        for (C4EpgEntry entry : entries) {
            if (entry.relatedEntry() != null) {
                entryProcessor.process(entry, channel);
            } else {
                brandlessEntryProcessor.process(entry, channel);
            }
        }
    }

    private List<C4EpgEntry> getEntries(Document scheduleDocument) {
        Nodes entryNodes = scheduleDocument.query("//atom:feed/atom:entry", new XPathContext("atom", "http://www.w3.org/2005/Atom"));
        
        List<C4EpgEntry> entries = Lists.newArrayListWithCapacity(entryNodes.size());
        
        for (int i = 0; i < entryNodes.size(); i++) {
            C4EpgEntry entry = C4EpgEntry.from((C4EpgEntryElement) entryNodes.get(i));
            entries.add(entry);
        }
        
        return entries;
    }
}
