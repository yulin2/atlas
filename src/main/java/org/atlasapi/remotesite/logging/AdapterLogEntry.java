package org.atlasapi.remotesite.logging;

import org.joda.time.DateTime;

import com.metabroadcast.common.time.DateTimeZones;

public class AdapterLogEntry {

	private final DateTime reportedAt = new DateTime(DateTimeZones.UTC);
	private String uri;
	private Exception e;
	private String message;
	private Class<?> clazz;
	
	public AdapterLogEntry withUri(String uri) {
		this.uri = uri;
		return this;
	}
	
	public AdapterLogEntry withCause(Exception e) {
		this.e = e;
		return this;
	}
	
	public AdapterLogEntry withMessage(String message) {
		this.message = message;
		return this;
	}

	public Exception cause() {
		return e;
	}
	
	public String message() {
		return message;
	}
	
	public String uri() {
		return uri;
	}
	
	public DateTime reportedAt() {
		return reportedAt;
	}

	public AdapterLogEntry withSource(Class<?> clazz) {
		this.clazz = clazz;
		return this;
	}
	
	public Class<?> sourceOrDefault(Class<?> defaultClass) {
		if (clazz != null) {
			return clazz;
		}
		return defaultClass;
	}
}
