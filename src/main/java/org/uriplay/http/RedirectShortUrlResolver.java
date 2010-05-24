package org.uriplay.http;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.joda.time.Duration;
import org.uriplay.remotesite.http.CommonsHttpClient;

public class RedirectShortUrlResolver implements ShortUrlResolver {

	private final CommonsHttpClient client = new CommonsHttpClient().withSocketTimeout(Duration.standardSeconds(10)).withConnectionTimeout(Duration.standardSeconds(5));
	
	@Override
	public String resolve(String shortUri) {
		
		HeadMethod response = client.head(shortUri);
		
		if (response != null && response.getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY) {
			Header locationHeader = response.getResponseHeader("Location");
			if (locationHeader == null) {
				return null;
			}
			return locationHeader.getValue();
		} else {
			return null;
		}
	}
}
