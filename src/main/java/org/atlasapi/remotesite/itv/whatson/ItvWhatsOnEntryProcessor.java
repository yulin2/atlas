package org.atlasapi.remotesite.itv.whatson;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.remotesite.ContentMerger;

import com.google.common.collect.ImmutableList;


public class ItvWhatsOnEntryProcessor {
    private final ItvWhatsOnEntryTranslator translator;
    private final ContentResolver contentResolver;
    private final ContentWriter contentWriter;
  
    public ItvWhatsOnEntryProcessor(ContentResolver contentResolver, ContentWriter contentWriter) {
        this.translator = new ItvWhatsOnEntryTranslator();
        this.contentResolver = contentResolver;
        this.contentWriter = contentWriter;
    }
    
    private void createOrUpdate(Content content) {
        ResolvedContent resolved = contentResolver.findByCanonicalUris(ImmutableList.of(content.getCanonicalUri()));
        if (!resolved.resolved(content.getCanonicalUri())) {
            if (content instanceof Item) {
                contentWriter.createOrUpdate((Item) content);
            } else {
                contentWriter.createOrUpdate((Container) content); 
            }
        } else {
            Content resolvedContent = (Content)resolved.get(content.getCanonicalUri()).requireValue();
            if (content instanceof Item) {
                ContentMerger.merge((Item)resolvedContent, (Item) content);
                contentWriter.createOrUpdate((Item)resolvedContent);
            }
            else {
                ContentMerger.merge((Container)resolvedContent, (Container) content);
                contentWriter.createOrUpdate((Container)resolvedContent);
            }
        }
    }
    
    public void process(ItvWhatsOnEntry entry) {
        Brand brand = translator.toBrand(entry);
        createOrUpdate(brand);
        Series series = translator.toSeries(entry);
        createOrUpdate(series);
        Episode episode = translator.toEpisode(entry);
        createOrUpdate(episode);
    }

}
