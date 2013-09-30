package org.atlasapi.remotesite.pa.features;

import java.util.Comparator;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Identified;

/**
 * When resolving content for use in the PA Featured Content content group, we want to look for
 * items with film IDs, or raw IDs first (which will for now be films). This is to work around
 * a duplicate item problem on ingest from PA.
 * 
 * @author tom
 *
 */
public class PaIdentifiedComparator implements Comparator<Identified> {

    private static final Pattern ID_PATTERN = Pattern.compile("^http:\\/\\/pressassociation.com\\/\\d+$");
    private static final Pattern FILM_PATTERN = Pattern.compile("^http:\\/\\/pressassociation.com\\/films\\/\\d+$");
    
    @Override
    public int compare(Identified o1, Identified o2) {
        if (o1 == o2) {
            return 0;
        }
        String o1Uri = o1.getCanonicalUri();
        String o2Uri = o2.getCanonicalUri();
        
        if (FILM_PATTERN.matcher(o1Uri).matches()) {
            if (FILM_PATTERN.matcher(o2Uri).matches()) {
                return o1Uri.compareTo(o2Uri);
            } else {
                return -1;
            }
        }
        
        if (FILM_PATTERN.matcher(o2Uri).matches()) {
            return 1;
        }
        
        if (ID_PATTERN.matcher(o1Uri).matches()) {
            if (ID_PATTERN.matcher(o2Uri).matches()) {
                return o1Uri.compareTo(o2Uri);
            } else {
                return -1;
            }
        }
        
        if (ID_PATTERN.matcher(o2Uri).matches()) {
            return 1;
        }
       
        return o1Uri.compareTo(o2Uri);
    }
}
