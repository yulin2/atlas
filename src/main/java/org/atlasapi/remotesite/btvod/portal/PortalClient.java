package org.atlasapi.remotesite.btvod.portal;

import java.util.Set;


public interface PortalClient {

    Set<String> getProductIdsForGroup(String groupId);
}
