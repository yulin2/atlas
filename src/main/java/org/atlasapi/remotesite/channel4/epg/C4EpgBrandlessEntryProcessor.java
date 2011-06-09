package org.atlasapi.remotesite.channel4.epg;

import static org.atlasapi.media.entity.Publisher.C4;

import java.util.Set;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Channel;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.joda.time.DateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;

public class C4EpgBrandlessEntryProcessor {
    
    private static final String REAL_PROGRAMME_BASE = "http://www.channel4.com/programmes/";
    private static final String REAL_TAG_BASE = "tag:www.channel4.com,2009:/programmes/";
  
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
            String brandUri = REAL_PROGRAMME_BASE + brandName;
            Maybe<Identified> maybeBrand = contentStore.findByCanonicalUris(ImmutableList.of(brandUri)).get(brandUri);
            
            Brand brand = null;
            if(!maybeBrand.hasValue()) {
                brand = brandFromEntry(entry, brandName, channel);
            } else {
                brand = (Brand) maybeBrand.requireValue();
                updateBrand(entry, brand, brandName, channel);
            }
            
            contentWriter.createOrUpdate(brand);
            
        }catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withCause(e).withDescription("Exception processing brandless entry " + entry.id()));
        }
    }

    /**
     * Give synthesized brands 'real' uris so that when/if they appear in the /programmes feed they are
     * matched up 
     */
    private Brand brandFromEntry(C4EpgEntry entry, String synthBrandName, Channel channel) {
        Brand brand = new Brand(REAL_PROGRAMME_BASE + synthBrandName, "c4:"+synthBrandName, C4);
        brand.addAlias(REAL_TAG_BASE + synthBrandName);
        brand.setTitle(entry.title());
        brand.setLastUpdated(entry.updated());
        
        brand.addContents(episodeFrom(entry, synthBrandName, channel));
        
        return brand;
    }

    private Episode episodeFrom(C4EpgEntry entry, String synthBrandName, Channel channel) {
        String slotId = entry.slotId();
        Episode episode = new Episode(SYNTH_PROGRAMME_BASE + synthBrandName + "/" + slotId, "c4:"+synthBrandName +"-"+slotId, C4);
        episode.addAlias(SYNTH_TAG_BASE+synthBrandName+"/"+slotId);
        episode.setTitle(entry.title());
        episode.setDescription(entry.summary());
        episode.setLastUpdated(entry.updated());
        
        C4EpgEntryProcessor.updateVersion(episode, entry, channel);
        
        return episode;
    }

    private void updateBrand(C4EpgEntry entry, Brand brand, String synthbrandName, Channel channel) {
        boolean found = false;
        //look for an episode with a broadcast with this entry's id, replace if found.
        for (Episode episode : brand.getContents()) {
            for (Version version : episode.getVersions()) {
                Set<Broadcast> broadcasts = Sets.newHashSet();
                for (Broadcast broadcast : version.getBroadcasts()) {
                    if(broadcast.getId() != null && broadcast.getId().equals("c4:"+entry.slotId())) {
                        broadcasts.add(createBroadcast(entry, channel));
                        found = true;
                    } else {
                        broadcasts.add(broadcast);
                    }
                }
                if(found) {
                    version.setBroadcasts(broadcasts);
                    return;
                }
            }
        }
        
        //Try to locate an item with the same description.
        for (Episode episode : brand.getContents()) {
            if(episode.getDescription().equals(entry.summary())) {
                //Known from above that this is a new broadcast so just add.
                C4EpgEntryProcessor.updateVersion(episode, entry, channel);
                return;
            }
        }
        //otherwise create a new one.
        brand.addContents(episodeFrom(entry, synthbrandName, channel));
    }

    private Broadcast createBroadcast(C4EpgEntry entry, Channel channel) {
        Broadcast entryBroadcast = new Broadcast(channel.uri(), entry.txDate(), entry.duration()).withId("c4:"+entry.slotId());
        entryBroadcast.addAlias(entry.id());
        entryBroadcast.setIsActivelyPublished(true);
        entryBroadcast.setLastUpdated(entry.updated() != null ? entry.updated() : new DateTime());
        return entryBroadcast;
    }

    private String brandName(String title) {
        return title.replaceAll("[^ a-zA-Z0-9]", "").replaceAll("\\s+", "-").toLowerCase();
    }
}
