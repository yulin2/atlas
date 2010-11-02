package org.atlasapi.remotesite.pa;

import java.io.File;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;

import org.atlasapi.genres.GenreMap;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.pa.bindings.Billing;
import org.atlasapi.remotesite.pa.bindings.Category;
import org.atlasapi.remotesite.pa.bindings.ChannelData;
import org.atlasapi.remotesite.pa.bindings.ProgData;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.xml.sax.XMLReader;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.metabroadcast.common.time.DateTimeZones;

public class PaUpdater implements Runnable {
    
    private final PaChannelMap channelMap = new PaChannelMap();
    private final GenreMap genreMap = new PaGenreMap();
    private final ContentWriter contentWriter;
    private final Map<String, Brand> brandMap = Maps.newHashMap();
    private final AdapterLog log;
    private final String filename;
    
    public PaUpdater(ContentWriter contentWriter, AdapterLog log, String filename) {
        this.contentWriter = contentWriter;
        this.log = log;
        this.filename = filename;
    }
    
    @Override
    public void run() {
        try {
            brandMap.clear();
            JAXBContext context = JAXBContext.newInstance("org.atlasapi.remotesite.pa.bindings");
            Unmarshaller unmarshaller = context.createUnmarshaller();
            
            unmarshaller.setListener(new Unmarshaller.Listener() {
                public void beforeUnmarshal(Object target, Object parent) {
                }
    
                public void afterUnmarshal(Object target, Object parent) {
                    if(target instanceof ProgData) {
                        processProgData((ProgData) target, (ChannelData) parent);
                    }
                }
            });
    
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XMLReader reader = factory.newSAXParser().getXMLReader();
            reader.setContentHandler(unmarshaller.getUnmarshallerHandler());
            reader.parse(new File(filename).toURI().toString());
            
            for (Brand brand : brandMap.values()) {
                contentWriter.createOrUpdatePlaylist(brand, true);
            }
        }
        catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(PaUpdater.class).withUri(filename));
        }
    }
    
    private void processProgData(ProgData progData, ChannelData channelData) {
        try {
            Brand brand = getBrand(progData);
            Series series = getSeries(progData);
            Episode episode = getEpisode(progData, channelData);
            
            if (series != null) {
                series.addItem(episode);
            }
            if (brand != null) {
                brand.addItem(episode);
            }
            else {
                contentWriter.createOrUpdateItem(episode);
            }
        }
        catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(PaUpdater.class));
        }
    }
    
    private Brand getBrand(ProgData progData) {
        String brandId = progData.getSeriesId();
        if (brandId == null || brandId.trim().isEmpty()) {
            return null;
        }
        
        if (brandMap.containsKey(brandId)) {
            return brandMap.get(brandId);
        }
        
        Brand brand = new Brand("http://pressassociation.com/brands/" + brandId, "pa:b-" + brandId, Publisher.PA);
        brand.setTitle(progData.getTitle());
        
        brand.setGenres(genreMap.map(getCategoryUris(progData.getCategory())));
        
        /* Pictures currently have no path
        List<Picture> pictures = progData.getPicture();
        for (Picture picture : pictures) {
            picture.getvalue();
        }*/
        
        brandMap.put(brandId, brand);
        
        return brand;
    }
    
    private Series getSeries(ProgData progData) {
        if (progData.getSeriesNumber() != null) {
            Series series = new Series("http://pressassociation.com/series/" + progData.getSeriesId() + "-" + progData.getSeriesNumber(), "pa:s-" + progData.getSeriesId() + "-" + progData.getSeriesNumber());
            
            series.setPublisher(Publisher.PA);
            
            series.setGenres(genreMap.map(getCategoryUris(progData.getCategory())));
            
            return series;
        }
        return null;
    }
    
    private Episode getEpisode(ProgData progData, ChannelData channelData) {
        Episode episode  = new Episode("http://pressassociation.com/episodes/" + progData.getProgId(), "pa:e-" + progData.getProgId(), Publisher.PA);
        if (progData.getEpisodeTitle() != null) {
            episode.setTitle(progData.getEpisodeTitle());
        }
        else {
            episode.setTitle(progData.getTitle());
        }
        
        try {
            if (progData.getEpisodeNumber() != null) {
                episode.setEpisodeNumber(Integer.valueOf(progData.getEpisodeNumber()));
            }
            if (progData.getSeriesNumber() != null) {
                episode.setSeriesNumber(Integer.valueOf(progData.getSeriesNumber()));
            }
        }
        catch (NumberFormatException e) {
            // sometimes we don't get valid numbers
        }
        
        if (progData.getBillings() != null) {
            for (Billing billing : progData.getBillings().getBilling()) {
                if (billing.getType().equals("synopsis")) {
                    episode.setDescription(billing.getvalue());
                }
            }
        }
        
        episode.setGenres(genreMap.map(getCategoryUris(progData.getCategory())));
        //episode.setImage(image);
        //episode.setThumbnail(thumbnail);
        
        Version version = new Version();
        version.setProvider(Publisher.PA);
        
        Duration duration = Duration.standardMinutes(Long.valueOf(progData.getDuration()));
        version.setDuration(duration);
        
        String channelUri = channelMap.getChannelUri(Integer.valueOf(channelData.getChannelId()));
        if (channelUri != null) {
            String dateString = progData.getDate() + "-" + progData.getTime();
            DateTime transmissionTime = DateTimeFormat.forPattern("dd/MM/yyyy-HH:mm").withZone(DateTimeZones.LONDON).parseDateTime(dateString);
            Broadcast broadcast = new Broadcast(channelUri, transmissionTime, duration);
            version.addBroadcast(broadcast);
        }
        
        episode.addVersion(version);
        
        return episode;
    }
    
    private Set<String> getCategoryUris(Iterable<Category> categories) {
        Set<String> uris = Sets.newHashSet();
        
        for (Category category : categories) {
            uris.add("http://pressassociation.com/genres/" + category.getCategoryCode());
        }
        
        return uris;
    }

}
