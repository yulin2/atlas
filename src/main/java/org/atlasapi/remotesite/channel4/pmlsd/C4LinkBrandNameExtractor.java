package org.atlasapi.remotesite.channel4.pmlsd;

import static org.atlasapi.remotesite.channel4.pmlsd.C4AtomApi.PROGRAMMES_BASE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Optional;

class C4LinkBrandNameExtractor {

    private final Pattern channel4Link = Pattern.compile(
        "https?://.+\\.channel4\\.com/[^/]+/([^./]+).*"
    );
      
    public Optional<String> brandNameFrom(String url) {
       Matcher matcher = channel4Link.matcher(url);
       if (matcher.matches()) {
           return Optional.of(matcher.group(1));
       }
       return Optional.absent();
    }
    
    public Optional<String> atlasBrandUriFrom(Publisher publisher, String url) {
        Optional<String> brandName = brandNameFrom(url);
        if (brandName.isPresent()) {
            return Optional.of(String.format("http://%s/pmlsd/%s", publisherHost(publisher), brandName.get()));
        }
        return Optional.absent();
    }
    
    public Optional<String> c4CanonicalUriFrom(String url) {
         Optional<String> brandName = brandNameFrom(url);
         if (brandName.isPresent()) {
            return Optional.of(PROGRAMMES_BASE + brandName.get());
         }
         return Optional.absent();
     }
    
     private String publisherHost(Publisher publisher) {
         String host = C4PmlsdModule.PUBLISHER_TO_CANONICAL_URI_HOST_MAP.get(publisher);
         if (host == null) {
             throw new IllegalArgumentException("Could not map publisher " + publisher.key() + " to a canonical URI host");
         }
         return host;
     }
}
