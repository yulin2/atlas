package org.atlasapi.tracking.twitter;

import twitter4j.Status;

public interface StatusProcessor {

	void process(Status status);

}
