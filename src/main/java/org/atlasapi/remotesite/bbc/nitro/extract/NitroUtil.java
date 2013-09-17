package org.atlasapi.remotesite.bbc.nitro.extract;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.atlas.glycerin.model.Availability;
import com.metabroadcast.atlas.glycerin.model.AvailabilityOf;

/**
 * Utility methods for extracting Nitro data. 
 *
 */
public final class NitroUtil {

    private NitroUtil() {}
    
    private static final Set<String> availabilityParentTypes = ImmutableSet.of("episode", "clip"); 
    private static final Set<String> availabilityVersionTypes = ImmutableSet.of("version"); 
    
    /**
     * Returns the PID of the episode or clip associated with an availability
     * 
     * @param availability
     *            - the availability from which to extract the PID.
     * @return - the PID of the availability's programme or null if there is
     *         none.
     */
    public static String programmePid(Availability availability) {
        return pidInTypes(availability, availabilityParentTypes);
    }

    /**
     * Returns the PID of the version associated with an availability
     * 
     * @param availability
     *            - the availability from which to extract the PID.
     * @return - the PID of the availability's version or null if there is
     *         none.
     */
    public static String versionPid(Availability availability) {
        return pidInTypes(availability, availabilityVersionTypes);
    }

    private static String pidInTypes(Availability availability, Set<String> types) {
        String parentPid = null;
        for (AvailabilityOf availabilityOf : availability.getAvailabilityOf()) {
            if (types.contains(availabilityOf.getResultType())) {
                parentPid = availabilityOf.getPid();
            }
        }
        return parentPid;
    }
    
}
