package org.atlasapi.remotesite.bbc;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.content.MediaType;

import com.google.common.collect.ImmutableMap;
import com.metabroadcast.common.base.Maybe;

public class BbcMasterbrandMediaTypeMap {

	private static Map<String, MediaType> serviceContentTypeMap = ImmutableMap.<String,MediaType>builder().
        	put("bbcone", MediaType.VIDEO).
        	put("bbctwo", MediaType.VIDEO).
        	put("bbcthree", MediaType.VIDEO).
        	put("bbcfour", MediaType.VIDEO).
        	put("bbchd", MediaType.VIDEO).
        	put("cbbc", MediaType.VIDEO).
        	put("cbeebies", MediaType.VIDEO).
        	put("bbcnews", MediaType.VIDEO).
        	put("parliament", MediaType.VIDEO).
        	put("bbcalba", MediaType.VIDEO).
        	put("radio1", MediaType.AUDIO).
        	put("1xtra", MediaType.AUDIO).
        	put("radio2", MediaType.AUDIO).
        	put("radio3", MediaType.AUDIO).
        	put("radio4", MediaType.AUDIO).
        	put("5live", MediaType.AUDIO).
        	put("5livesportsextra", MediaType.AUDIO).
        	put("6music", MediaType.AUDIO).
        	put("radio7", MediaType.AUDIO).
        	put("asiannetwork", MediaType.AUDIO).
        	put("worldservice", MediaType.AUDIO).
        	put("radioscotland", MediaType.AUDIO).
        	put("radionangaidheal", MediaType.AUDIO).
        	put("radioulster", MediaType.AUDIO).
        	put("radiofoyle", MediaType.AUDIO).
        	put("radiowales", MediaType.AUDIO).
        	put("radiocymru", MediaType.AUDIO).
        	put("berkshire", MediaType.AUDIO).
        	put("bristol", MediaType.AUDIO).
        	put("cambridgeshire", MediaType.AUDIO).
        	put("cornwall", MediaType.AUDIO).
        	put("coventry", MediaType.AUDIO).
        	put("cumbria", MediaType.AUDIO).
        	put("derby", MediaType.AUDIO).
        	put("devon", MediaType.AUDIO).
        	put("essex", MediaType.AUDIO).
        	put("gloucestershire", MediaType.AUDIO).
        	put("guernsey", MediaType.AUDIO).
        	put("herefordandworcester", MediaType.AUDIO).
        	put("humberside", MediaType.AUDIO).
        	put("jersey", MediaType.AUDIO).
        	put("kent", MediaType.AUDIO).
        	put("lancashire", MediaType.AUDIO).
        	put("leeds", MediaType.AUDIO).
        	put("leicester", MediaType.AUDIO).
        	put("lincolnshire", MediaType.AUDIO).
        	put("london", MediaType.AUDIO).
        	put("manchester", MediaType.AUDIO).
        	put("merseyside", MediaType.AUDIO).
        	put("newcastle", MediaType.AUDIO).
        	put("norfolk", MediaType.AUDIO).
        	put("northampton", MediaType.AUDIO).
        	put("nottingham", MediaType.AUDIO).
        	put("oxford", MediaType.AUDIO).
        	put("sheffield", MediaType.AUDIO).
        	put("shropshire", MediaType.AUDIO).
        	put("solent", MediaType.AUDIO).
        	put("somerset", MediaType.AUDIO).
        	put("stoke", MediaType.AUDIO).
        	put("suffolk", MediaType.AUDIO).
        	put("surrey", MediaType.AUDIO).
        	put("sussex", MediaType.AUDIO).
        	put("tees", MediaType.AUDIO).
        	put("threecounties", MediaType.AUDIO).
        	put("wiltshire", MediaType.AUDIO).
        	put("wm", MediaType.AUDIO).
        	put("york", MediaType.AUDIO).build();
	
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
