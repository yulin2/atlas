package org.atlasapi.remotesite.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CommonsLoggingAdapterLog implements AdapterLog {
	
	@Override
	public void record(AdapterLogEntry entry) {
		Log log = LogFactory.getLog(entry.sourceOrDefault(getClass()));
		if (entry.cause() != null) {
			log.error(entry.cause());
		} else {
			log.warn(entry.message());
		}
	}
}
