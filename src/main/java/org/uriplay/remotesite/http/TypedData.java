package org.uriplay.remotesite.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.uriplay.media.reference.entity.MimeType;

public class TypedData {

	private final MimeType contentType;
	private final byte[] data;
	
	public TypedData(String contentType, byte[] data) {
		this.contentType = MimeType.fromString(contentType);
		this.data = data;
	}
	
	public TypedData(MimeType contentType, byte[] data) {
		this.contentType = contentType;
		this.data = data;
	}
	
	public MimeType contentType() {
		return contentType;
	}
	
	public byte[] data() {
		return data;
	}

	public String contentTypeAsString() {
		if (contentType != null) {
			return contentType.toString();
		}
		return null;
	}

	public InputStream asStream() {
		return new ByteArrayInputStream(data);
	}
	
	@Override
	public String toString() {
		return data.length + " bytes of data of type " + contentType();
	}
}