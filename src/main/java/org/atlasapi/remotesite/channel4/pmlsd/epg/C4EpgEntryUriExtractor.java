package org.atlasapi.remotesite.channel4.pmlsd.epg;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.channel4.pmlsd.C4PmlsdModule;
import org.atlasapi.remotesite.channel4.pmlsd.C4UriExtractor;
import org.atlasapi.remotesite.channel4.pmlsd.epg.model.C4EpgEntry;

import com.google.common.base.Optional;

public class C4EpgEntryUriExtractor implements C4UriExtractor<C4EpgEntry, C4EpgEntry, C4EpgEntry>{
    
    private final Pattern uriPattern = Pattern.compile(
        "https?://(.+).channel4.com/([^/]+)/([^./]+)(.atom|/4od.atom|/episode-guide/series-(\\d+)(.atom|/episode-(\\d+).atom))"
    );
    private final String ATLAS_URI_FORMAT = "http://%s/pmlsd/%s";
    
    private final int seriesNumberGroup = 5;
    private final int brandNameGroup = 3;
    
    private final String seriesUriInfix = "/episode-guide/series-";

    @Override
    public Optional<String> uriForBrand(Publisher publisher, C4EpgEntry entry){
        if (!entry.hasRelatedLink()) {
            return Optional.absent();
        }
        String linkUri = entry.getRelatedLink();
        Matcher matcher = uriPattern.matcher(linkUri);
        if (!matcher.matches()) {
            return Optional.absent();
        } else {
            return Optional.of(String.format(ATLAS_URI_FORMAT, publisherHost(publisher), matcher.group(3)));
        }
    }
    
    @Override
    public Optional<String> uriForSeries(Publisher publisher, C4EpgEntry entry){
        if (!entry.hasRelatedLink()) {
            return Optional.absent();
        }
        String linkUri = entry.getRelatedLink();
        Matcher matcher = uriPattern.matcher(linkUri);
        if (matcher.matches() && matcher.group(seriesNumberGroup) != null) {
            String seriesNumber = matcher.group(seriesNumberGroup);
            return Optional.of(String.format(ATLAS_URI_FORMAT, publisherHost(publisher), matcher.group(brandNameGroup) + seriesUriInfix + seriesNumber));
        }
        return Optional.absent();
    }
    
    @Override
    public Optional<String> uriForItem(Publisher publisher, C4EpgEntry entry){
        return Optional.of(String.format(ATLAS_URI_FORMAT, publisherHost(publisher), entry.programmeId()));
    }

    @Override
    public Optional<String> uriForClip(Publisher publisher, C4EpgEntry remote) {
        throw new UnsupportedOperationException("Clips not supported as not ingested from EPG");
    }
    
    private String publisherHost(Publisher publisher) {
        String host = C4PmlsdModule.PUBLISHER_TO_CANONICAL_URI_HOST_MAP.get(publisher);
        if (host == null) {
            throw new IllegalArgumentException("Could not map publisher " + publisher.key() + " to a canonical URI host");
        }
        return host;
    }

}
