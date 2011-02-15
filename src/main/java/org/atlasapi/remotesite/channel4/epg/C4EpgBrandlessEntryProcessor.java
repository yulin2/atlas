package org.atlasapi.remotesite.channel4.epg;

import static org.atlasapi.media.entity.Publisher.C4;

import java.util.regex.Matcher;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Channel;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.channel4.C4AtomApi;

import com.google.common.collect.Iterables;

public class C4EpgBrandlessEntryProcessor {
    
    private static final String REAL_PROGRAMME_BASE = "http://www.channel4.com/programmes/";
    private static final String SYNTH_PROGRAMME_BASE = "http://www.channel4.com/programmes/synthesized/";
    private static final String SYNTH_TAG_BASE = "tag:www.channel4.com,2009:/programmes/synthesized/";

    private final ContentWriter contentWriter;
    private final ContentResolver contentStore;
    private final AdapterLog log;

    public C4EpgBrandlessEntryProcessor(ContentWriter contentWriter, ContentResolver contentStore, AdapterLog log) {
        this.contentWriter = contentWriter;
        this.contentStore = contentStore;
        this.log = log;
    }

    public void process(C4EpgEntry entry, Channel channel) {
        try{
            
            String realBrandName = C4EpgEntryProcessor.webSafeBrandName(entry);
            
            String brandName = realBrandName != null ? realBrandName : brandName(entry.title());
            
            //try to get container for the item.
            Brand brand = (Brand) contentStore.findByCanonicalUri((realBrandName != null ? REAL_PROGRAMME_BASE : SYNTH_PROGRAMME_BASE) + brandName);
            
            if(brand == null) {
                brand = brandFromEntry(entry, brandName, channel);
            } else {
                updateBrand(entry, brand, brandName, channel);
            }
            
            contentWriter.createOrUpdate(brand, true);
            
        }catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withCause(e).withDescription("Exception processing brandless entry " + entry.id()));
        }
    }

    private Brand brandFromEntry(C4EpgEntry entry, String synthBrandName, Channel channel) {
        Brand brand = new Brand(SYNTH_PROGRAMME_BASE + synthBrandName, "c4:"+synthBrandName, C4);
        brand.addAlias(SYNTH_TAG_BASE+synthBrandName);
        brand.setTitle(entry.title());
        brand.setLastUpdated(entry.updated());
        
        brand.addContents(episodeFrom(entry, synthBrandName, channel));
        
        return brand;
    }

    private Episode episodeFrom(C4EpgEntry entry, String synthBrandName, Channel channel) {
        String slotId = slotIdFrom(entry);
        Episode episode = new Episode(SYNTH_PROGRAMME_BASE + synthBrandName + "/" + slotId, "c4:"+synthBrandName +"-"+slotId, C4);
        episode.addAlias(SYNTH_TAG_BASE+synthBrandName+"/"+slotId);
        episode.setTitle(entry.title());
        episode.setDescription(entry.summary());
        episode.setLastUpdated(entry.updated());
        
        addBroadcastTo(episode, entry, channel, slotId);
        
        return episode;
    }

    private void addBroadcastTo(Episode episode, C4EpgEntry entry, Channel channel, String slotId) {
        
        Version version = Iterables.getFirst(episode.getVersions(), new Version());
        version.setDuration(entry.duration());
        
        
        Broadcast broadcast = new Broadcast(channel.uri(), entry.txDate(), entry.duration()).withId("c4:"+slotId);
        broadcast.addAlias(entry.id());
        
        version.addBroadcast(broadcast);
        
        if(!episode.getVersions().contains(version)) {
            episode.addVersion(version);
        }
        
    }

    private void updateBrand(C4EpgEntry entry, Brand brand, String synthbrandName, Channel channel) {
        //Try to locate an item with the same description.
        for (Episode episode : brand.getContents()) {
            if(episode.getDescription().equals(entry.summary())) {
                addBroadcastTo(episode, entry, channel, slotIdFrom(entry));
                return;
            }
        }
        //otherwise create a new one.
        brand.addContents(episodeFrom(entry, synthbrandName, channel));

        brand.setLastUpdated(entry.updated());
    }

    private String slotIdFrom(C4EpgEntry entry) {
        Matcher slotMatcher = C4AtomApi.SLOT_PATTERN.matcher(entry.id());
        if(!slotMatcher.matches()) {
            throw new RuntimeException("No slot ID found for " + entry.id());
        }
        
        String slotId = slotMatcher.group(1);
        return slotId;
    }

    private String brandName(String title) {
        return title.replaceAll("[^ a-zA-Z0-9]", "").replaceAll("\\s+", "-").toLowerCase();
    }
/*
 *  <entry>
        <id>tag:int.channel4.com,2009:slot/299</id>
        <title type="text">Lassie</title>
        <updated>2011-02-03T15:43:00.849Z</updated>
        <summary type="html">(2005) Joe's (Jonathan Mason) beloved dog Lassie
            is sold by his destitute parents to the Duke of Rudling (Peter
            O'Toole), but the collie escapes and embarks on an adventurous
            journey home.</summary>
        <dc:date.TXDate>2011-01-07T13:05:00.000Z</dc:date.TXDate>
        <dc:relation.TXChannel>C4</dc:relation.TXChannel>
        <dc:relation.Subtitles>true</dc:relation.Subtitles>
        <dc:relation.AudioDescription>false</dc:relation.AudioDescription>
        <dc:relation.Duration>110:00</dc:relation.Duration>
        <dc:relation.WideScreen>false</dc:relation.WideScreen>
        <dc:relation.Signing>false</dc:relation.Signing>
        <dc:relation.Repeat>true</dc:relation.Repeat>
    </entry>
 */
}
