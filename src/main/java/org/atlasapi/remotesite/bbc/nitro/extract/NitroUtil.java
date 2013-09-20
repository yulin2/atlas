package org.atlasapi.remotesite.bbc.nitro.extract;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.atlas.glycerin.model.Availability;
import com.metabroadcast.atlas.glycerin.model.AvailabilityOf;
import com.metabroadcast.atlas.glycerin.model.Broadcast;
import com.metabroadcast.atlas.glycerin.model.PidReference;
import com.metabroadcast.common.time.DateTimeZones;

/**
 * Utility methods for extracting Nitro data. 
 *
 */
public final class NitroUtil {

    private NitroUtil() {}
    
    private static final Set<String> availabilityParentTypes = ImmutableSet.of("episode", "clip"); 
    private static final Set<String> versionTypes = ImmutableSet.of("version"); 
    
    /**
     * Returns the PID of the episode or clip associated with an availability.
     * 
     * @param availability
     *            - the availability from which to extract the PID.
     * @return - the PID of the availability's programme or null if there is
     *         none.
     */
    public static String programmePid(Availability availability) {
        checkNotNull(availability, "null availability");
        return pidInTypes(availability, availabilityParentTypes);
    }

    /**
     * Returns the {@link PidReference} of the version associated with an availability.
     * 
     * @param availability
     *            - the availability from which to extract the PID.
     * @return - the {@link PidReference} of the availability's version or null if there is
     *         none.
     */
    public static String versionPid(Availability availability) {
        checkNotNull(availability, "null availability");
        return pidInTypes(availability, versionTypes);
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
    
    /**
     * Returns the {@link PidReference} of the episode or clip associated with an broadcast.
     * 
     * @param broadcast
     *            - the broadcast from which to extract the PID.
     * @return - the {@link PidReference} of the broadcast's programme or null if there is
     *         none.
     */
    public static PidReference programmePid(Broadcast broadcast) {
        checkNotNull(broadcast, "null broadcast");
        return refInTypes(broadcast, availabilityParentTypes);
    }
    
    /**
     * Returns the {@link PidReference} of the version associated with a broadcast.
     * 
     * @param broadcast
     *            - the broadcast from which to extract the PID.
     * @return - the {@link PidReference} of the broadcast's version or null if there is
     *         none.
     */
    public static PidReference versionPid(Broadcast broadcast) {
        checkNotNull(broadcast, "null broadcast");
        return refInTypes(broadcast, versionTypes);
    }
    
    private static PidReference refInTypes(Broadcast broadcast, Set<String> types) {
        PidReference parentPid = null;
        for (PidReference pidRef : broadcast.getBroadcastOf()) {
            if (types.contains(pidRef.getResultType())) {
                parentPid = pidRef;
            }
        }
        return parentPid;
    }
    
    /**
     * Converts an {@link XMLGregorianCalendar} to a {@link DateTime}.
     * 
     * @param cal - the {@link XMLGregorianCalendar} to convert.
     * @return - {@link DateTime} representing the {@link XMLGregorianCalendar}.
     */
    public static DateTime toDateTime(XMLGregorianCalendar cal) {
        checkNotNull(cal, "null calendar");
        return new DateTime(cal.toGregorianCalendar(), ISOChronology.getInstance())
            .toDateTime(DateTimeZones.UTC);
    }
    
}
