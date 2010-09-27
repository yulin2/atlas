package org.atlasapi.tracking.twitter;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.util.NamedThreadFactory;

import twitter4j.Status;

import com.metabroadcast.common.social.twitter.stream.TweetProcessor;

public class QueueingTweetProcessor implements TweetProcessor {

	private static final int MAX_THREADS = 20;

	private final Log log = LogFactory.getLog(getClass());
	
	private final TweetProcessor delegate;
	private final ThreadPoolExecutor executor;
	
	public QueueingTweetProcessor(TweetProcessor delegate) {
		this(delegate, (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_THREADS, new NamedThreadFactory("tweet-processor")));
	}

	public QueueingTweetProcessor(TweetProcessor delegate, ThreadPoolExecutor executor) {
		this.delegate = delegate;
		this.executor = executor;
	}
	
	@Override
	public void process(final Status status) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					delegate.process(status);
				} catch (Exception e) {
					log.warn(e);
				}
			}
		});
	}

	public int queueSize() {
		return executor.getQueue().size();
	}
	
	public int activeThreads() {
		return executor.getActiveCount();
	}
}
