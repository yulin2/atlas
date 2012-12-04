package org.atlasapi.remotesite.btvod;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nu.xom.Element;

import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.ContentMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.metabroadcast.common.base.Maybe;

public class DefaultBtVodXmlElementHandler implements BtVodXmlElementHandler {
    
    private final Map<String, Container> seen = Maps.newHashMap();
    private final SetMultimap<String, Content> cached = HashMultimap.create();
    private final Logger log = LoggerFactory.getLogger(DefaultBtVodXmlElementHandler.class);
            
    private final ContentExtractor<Element, Set<? extends Content>> extractor;
    private final ContentResolver resolver;
    private final ContentWriter writer;

    public DefaultBtVodXmlElementHandler(ContentExtractor<Element, Set<? extends Content>> extractor, ContentResolver resolver, ContentWriter writer) {
        this.extractor = extractor;
        this.resolver = resolver;
        this.writer = writer;
    }

    @Override
    public void prepare() {
    }

    @Override
    public void handle(Element element) {
        for (Content content : extractor.extract(element)) {
            Maybe<Identified> existing = resolve(content.getCanonicalUri());
            if (existing.isNothing()) {
                write(content);
            } else {
                Identified identified = existing.requireValue();
                if (content instanceof Item) {
                    write(ContentMerger.merge(ContentMerger.asItem(identified), (Item) content));
                } else if (content instanceof Container) {
                    write(ContentMerger.merge(ContentMerger.asContainer(identified), (Container) content));
                }
            }
        }
    }

    @Override
    public void finish() {
        if (cached.values().size() > 0) {
            log.warn("{} extracted but unwritten", cached.values().size());
            for (Entry<String, Collection<Content>> mapping : cached.asMap().entrySet()) {
                log.warn(mapping.toString());
            }

        }
        seen.clear();
        cached.clear();
    }

    private Maybe<Identified> resolve(String uri) {
        ImmutableSet<String> uris = ImmutableSet.of(uri);
        return resolver.findByCanonicalUris(uris).get(uri);
    }

    private void write(Content content) {
        if (content instanceof Container) {
            if (content instanceof Series) {
                writeSeriesAndCachedSubContents((Series) content);
            } else {
                writer.createOrUpdate((Container) content);
            }
        } else if (content instanceof Item) {
            if (content instanceof Episode) {
                cacheOrWriteEpisode((Episode) content);
            } else {
                cacheOrWriteItem(content);
            }
        }
    }

    private void writeSeriesAndCachedSubContents(Series series) {
        writer.createOrUpdate(series);
        String seriesUri = series.getCanonicalUri();
        seen.put(seriesUri, series);
        for (Content subContent : cached.removeAll(seriesUri)) {
            write(subContent);
        }
    }

    private void cacheOrWriteItem(Content content) {
        Item item = (Item) content;
        ParentRef parent = item.getContainer();
        if (parent != null && !seen.containsKey(parent.getUri())) {
            cached.put(parent.getUri(), item);
        } else {
            writer.createOrUpdate((Item) content);
        }
    }

    private void cacheOrWriteEpisode(Episode episode) {
        String seriesUri = episode.getContainer().getUri();
        
        if (!seen.containsKey(seriesUri)) {
            cached.put(seriesUri, episode);
            return;
        }
        
        writer.createOrUpdate(episode);
    }
}
