package org.atlasapi.remotesite.hulu;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HuluFeed {

	private static final Pattern ITEM_URL_CANONICALIZER = Pattern.compile("(http://www.hulu.com/watch/\\d+).*");

	static String canonicaliseEpisodeUri(String uri) {
		Matcher matcher = ITEM_URL_CANONICALIZER.matcher(uri);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		return null;
	}
}
