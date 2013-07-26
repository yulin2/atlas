package org.atlasapi.remotesite.itv.whatson;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Series;


public class ItvWhatsOnEntryProcessor {
    private final ItvWhatsOnEntryTranslator translator;
  
    public ItvWhatsOnEntryProcessor() {
        this.translator = new ItvWhatsOnEntryTranslator();
    }
    
    public void process(ItvWhatsOnEntry entry) {
        Brand brand = translator.toBrand(entry);
        Series series = translator.toSeries(entry);
        Episode episode = translator.toEpisode(entry);
    }

}
