/* Copyright 2009 British Broadcasting Corporation
   Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.uriplay.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uriplay.remotesite.FetchException;

/**
 * Simple {@link UriplayLogger} that logs using Log4J.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class Log4JLogger implements UriplayLogger {

	Log logger = LogFactory.getLog(Log4JLogger.class); 
	
	public void fetchFailed(String uri, FetchException fetchException) {
		logger.warn("Failed to fetch: " + uri, fetchException);
	}

	public void unknownDataContainerFormat(String uri, String type) {
		logger.warn("Processed feed with unrecognised data container format: " + type + ", " + uri);
	}

}
