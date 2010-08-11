package org.atlasapi.remotesite.channel4;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Content;

import com.google.common.base.Preconditions;

public class C4AtomApi {

	private static final String PROGRAMMES_BASE = "http://www.channel4.com/programmes/";
	
	public static final Pattern CANONICAL_BRAND_URI_PATTERN = Pattern.compile(Pattern.quote(PROGRAMMES_BASE) + "([a-z0-9\\-]+)");
	private static final Pattern CANONICAL_EPISODE_URI_PATTERN = Pattern.compile(Pattern.quote(PROGRAMMES_BASE) + "[a-z0-9\\-]+/episode-guide/series-\\d+/episode-\\d+");

	private static final Pattern FEED_ID_PATTERN = Pattern.compile("tag:www.channel4.com,\\d{4}:/programmes/([a-z0-9\\-]+)/.*");

	private static final String API_BASE_URL = "http://api.channel4.com/programmes/";

	private static final Pattern IMAGE_PATTERN = Pattern.compile("(http.+?)\\d+x\\d+(\\.[a-zA-Z]+)");
	
	private static final String IMAGE_SIZE = "625x352";
	private static final String THUMBNAIL_SIZE = "200x113";
	
	
	public static void addImages(Content content, String anImage) {
		if (anImage != null) {
			Matcher matcher = IMAGE_PATTERN.matcher(anImage);
			if (matcher.matches()) {
				content.setThumbnail(matcher.group(1) + THUMBNAIL_SIZE + matcher.group(2));
				content.setImage((matcher.group(1) + IMAGE_SIZE + matcher.group(2)));
			}
		}
	}

	public static boolean isACanonicalBrandUri(String brandUri) {
		return CANONICAL_BRAND_URI_PATTERN.matcher(brandUri).matches();
	}

	public static String requestForBrand(String brandCanonicalUri, String extension) {
		return createBrandRequest(extractWebSafeNameFromBrandUri(brandCanonicalUri), extension);
	}

	private static String extractWebSafeNameFromBrandUri(String brandCanonicalUri) {
		Matcher matcher = CANONICAL_BRAND_URI_PATTERN.matcher(brandCanonicalUri);
		Preconditions.checkArgument(matcher.matches(), "Not a valid brand URI: " + brandCanonicalUri);
		return matcher.group(1);
	}

	public static String createBrandRequest(String webSafeName, String extension) {
		return API_BASE_URL + webSafeName + extension;

	}

	public static boolean isACanonicalEpisodeUri(String href) {
		return CANONICAL_EPISODE_URI_PATTERN.matcher(href).matches();
	}

	public static String webSafeNameFromAnyFeedId(String id) {
		Matcher matcher = FEED_ID_PATTERN.matcher(id);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		return null;
	}

	public static String episodeUri(String webSafeBrandName, int seriesNumber, int episodeNumber) {
		return seriesUriFor(webSafeBrandName, seriesNumber) + "/episode-" + episodeNumber;
	}

	public static String seriesUriFor(String webSafeBrandName, int seriesNumber) {
		return PROGRAMMES_BASE + webSafeBrandName + "/episode-guide/series-" + seriesNumber;
	}
}
