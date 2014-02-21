package org.atlasapi.remotesite.rovi;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;


public enum RoviShowType {

    /*
     * Movie
     */
    MOVIE("MO"),
    /*
     * Series Episode
     */
    SERIES_EPISODE("SE"), 
    /*
     * Series Master
     */
    SERIES_MASTER("SM"), 
    /*
     * Other Program
     */
    OTHER("OT");
    
    private final String roviType;
    
    private RoviShowType(String roviType) {
        this.roviType = roviType;
    }
    
    public static RoviShowType fromRoviType(final String roviType) {
        checkNotNull(roviType);
        
        return Iterables.find(asList(RoviShowType.values()), new Predicate<RoviShowType>() {
            @Override
            public boolean apply(RoviShowType actionType) {
                return actionType.roviType.equalsIgnoreCase(roviType);
            }
        });
    }

    public String getRoviType() {
        return roviType;
    }
}
