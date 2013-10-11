package org.atlasapi.remotesite.channel4.pmlsd.epg;

import static org.atlasapi.remotesite.channel4.pmlsd.C4AtomApi.PROGRAMMES_BASE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.remotesite.channel4.pmlsd.epg.model.C4EpgEntry;

import com.google.common.base.Optional;

public class C4EpgEntryUriExtractor {
    
    private final Pattern uriPattern = Pattern.compile(
        "https?://(.+).channel4.com/([^/]+)/([^./]+)(.atom|/4od.atom|/episode-guide/series-(\\d+)(.atom|/episode-(\\d+).atom))"
    );
    
    private final int seriesNumberGroup = 5;
    private final int brandNameGroup = 3;
    
    private final String seriesUriInfix = "/episode-guide/series-";

    public Optional<String> uriForBrand(C4EpgEntry entry){
        if (!entry.hasRelatedLink()) {
            return Optional.absent();
        }
        String linkUri = entry.getRelatedLink();
        Matcher matcher = uriPattern.matcher(linkUri);
        if (!matcher.matches()) {
            return Optional.absent();
        } else {
            return Optional.of(PROGRAMMES_BASE + matcher.group(3));
        }
    }
    
    public Optional<String> uriForSeries(C4EpgEntry entry){
        if (!entry.hasRelatedLink()) {
            return Optional.absent();
        }
        String linkUri = entry.getRelatedLink();
        Matcher matcher = uriPattern.matcher(linkUri);
        if (matcher.matches() && matcher.group(seriesNumberGroup) != null) {
            String seriesNumber = matcher.group(seriesNumberGroup);
            return Optional.of(PROGRAMMES_BASE + matcher.group(brandNameGroup) + seriesUriInfix + seriesNumber);
        }
        return Optional.absent();
    }
    
    public String uriForItemId(C4EpgEntry entry){
        return PROGRAMMES_BASE + entry.programmeId();
    }

}
