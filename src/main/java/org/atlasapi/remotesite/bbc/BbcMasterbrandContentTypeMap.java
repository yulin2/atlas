package org.atlasapi.remotesite.bbc;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.MediaType;

import com.google.common.collect.ImmutableMap;
import com.metabroadcast.common.base.Maybe;

public class BbcMasterbrandContentTypeMap {

	private static Map<String, MediaType> serviceContentTypeMap = ImmutableMap.<String,MediaType>builder().
			put("radio1", 	MediaType.AUDIO).
			put("radio2", 	MediaType.AUDIO).
			put("radio3", 	MediaType.AUDIO).
			put("radio4", 	MediaType.AUDIO). 
			put("bbcone", 	MediaType.VIDEO).
			put("bbctwo", 	MediaType.VIDEO).
			put("bbcthree", MediaType.VIDEO).
			put("bbcfour", 	MediaType.VIDEO).
			put("bbchd", 	MediaType.VIDEO).build();
	
	private static Pattern masterbrandPattern = Pattern.compile("/([^#]+)#service");
	
	public static Maybe<MediaType> lookup(String masterbrand) {
		Matcher brandMatch = masterbrandPattern.matcher(masterbrand);
		if (brandMatch.matches()) {
			return Maybe.fromPossibleNullValue(serviceContentTypeMap.get(brandMatch.group(1)));
		} 
		return Maybe.nothing();
	}
	
	
	public static Maybe<MediaType> lookupService(String service) {
		return Maybe.fromPossibleNullValue(serviceContentTypeMap.get(service));
	}
}
