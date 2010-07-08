/* Copyright 2009 British Broadcasting Corporation
 
Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.atlasapi.remotesite;

/**
 * Unchecked exception symbolising a problem fetching data for a query.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class FetchException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FetchException(String msg) {
		super(msg);
	}

	public FetchException(String msg, Exception e) {
		super(msg, e);
	}

}
