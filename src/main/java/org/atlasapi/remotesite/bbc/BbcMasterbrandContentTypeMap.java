package org.atlasapi.remotesite.bbc;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.ContentType;

import com.google.common.collect.ImmutableMap;
import com.metabroadcast.common.base.Maybe;

public class BbcMasterbrandContentTypeMap {

	private static Map<String, ContentType> serviceContentTypeMap = ImmutableMap.<String,ContentType>builder().
			put("radio1", 	ContentType.AUDIO).
			put("radio2", 	ContentType.AUDIO).
			put("radio3", 	ContentType.AUDIO).
			put("radio4", 	ContentType.AUDIO). 
			put("bbcone", 	ContentType.VIDEO).
			put("bbctwo", 	ContentType.VIDEO).
			put("bbcthree", ContentType.VIDEO).
			put("bbcfour", 	ContentType.VIDEO).
			put("bbchd", 	ContentType.VIDEO).build();
	
	private static Pattern masterbrandPattern = Pattern.compile("/([^#]+)#service");
	
	public static Maybe<ContentType> lookup(String masterbrand) {
		Matcher brandMatch = masterbrandPattern.matcher(masterbrand);
		if (brandMatch.matches()) {
			return Maybe.fromPossibleNullValue(serviceContentTypeMap.get(brandMatch.group(1)));
		} 
		return Maybe.nothing();
	}
	
}
