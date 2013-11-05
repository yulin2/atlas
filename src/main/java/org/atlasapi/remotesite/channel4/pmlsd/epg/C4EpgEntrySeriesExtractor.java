package org.atlasapi.remotesite.channel4.pmlsd.epg;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Series;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.channel4.pmlsd.C4AtomApi;
import org.atlasapi.remotesite.channel4.pmlsd.C4PmlsdModule;
import org.atlasapi.remotesite.channel4.pmlsd.epg.model.C4EpgEntry;

import com.google.common.base.Optional;

public class C4EpgEntrySeriesExtractor implements ContentExtractor<C4EpgEntry, Optional<Series>> {

    private final C4EpgEntryUriExtractor uriExtractor = new C4EpgEntryUriExtractor();
    
    @Override
    public Optional<Series> extract(C4EpgEntry source) {
        Optional<String> possibleSeriesUri = uriExtractor.uriForSeries(source);
        if (possibleSeriesUri.isPresent()) {
            
            String seriesUri = possibleSeriesUri.get();
            Series series = C4PmlsdModule.contentFactory().createSeries();
            series.setCanonicalUri(seriesUri);
            series.addAliasUrl(seriesUri.replace(C4AtomApi.PROGRAMMES_BASE, C4AtomApi.WEB_BASE));
            
            if (source.seriesNumber() != null) {
                series.withSeriesNumber(source.seriesNumber());
            } else {
                series.withSeriesNumber(extractUriFrom(seriesUri));
            }
            return Optional.of(series);
        }
        return Optional.absent();
    }

    private Integer extractUriFrom(String seriesUri) {
        Pattern pattern = Pattern.compile("(\\d+)$");
        Matcher matcher = pattern.matcher(seriesUri);
        if (matcher.matches()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

}
