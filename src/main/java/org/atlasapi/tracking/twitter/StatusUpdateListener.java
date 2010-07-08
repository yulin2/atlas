package org.atlasapi.tracking.twitter;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import twitter4j.Status;
import twitter4j.StatusListener;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class StatusUpdateListener implements StatusListener {
	
	static final Log LOG = LogFactory.getLog(KeywordTracker.class);
	
	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(150);
	private String filteredHashTag;

	private StatusProcessor statusProcessor;

	public StatusUpdateListener(String filteredHashTag, StatusProcessor statusProcessors) {
		this.filteredHashTag = filteredHashTag;
		this.statusProcessor = statusProcessors;
	}

	public void onStatus(Status status) {
	
		if (status.getText() == null) {
			LOG.warn("Twitter status update has null text");
			return;
		}

		LOG.debug("tracking status: " + status.getText());

		if (filteredHashTag != null && status.getText().contains(filteredHashTag)) {
			LOG.info("Ignoring status update because it contains " + filteredHashTag);
			return;
		}
		
		executorService.schedule(new StatusProcessingJob(status), 10, TimeUnit.SECONDS);
	}

	
	class StatusProcessingJob implements Runnable {
		
		private final Status status;

		public StatusProcessingJob(Status status) {
			this.status = status;
		}

		public void run() {
			try {
				statusProcessor.process(status);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.warn(e);
			}
		}
	}
	
	public void onException(Exception ex) {
		LOG.warn(ex);
	}
	
	@VisibleForTesting
	public void setExecutorService(ScheduledExecutorService executorService) {
		this.executorService = executorService;
	}

	public void setFilteredHashTag(String hashTag) {
		filteredHashTag = hashTag;
	}

	public void shutdown() {
		executorService.shutdown();
	}
}