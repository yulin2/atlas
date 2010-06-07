package org.uriplay.remotesite.hulu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.map.ObjectMapper;
import org.jaxen.JaxenException;
import org.jdom.Element;
import org.uriplay.media.TransportType;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Countries;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Episode;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Policy;
import org.uriplay.media.entity.Version;
import org.uriplay.query.content.PerPublisherCurieExpander;
import org.uriplay.remotesite.ContentExtractor;
import org.uriplay.remotesite.FetchException;
import org.uriplay.remotesite.html.HtmlNavigator;

import com.google.soy.common.collect.Sets;

public class HuluItemContentExtractor implements ContentExtractor<HtmlNavigator, Episode> {
    private static final String SOCIAL_FEED = "SocialFeed.facebook_template_data.watch = ";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Pattern INFO_PATTERN = Pattern.compile("^Season\\s*(\\d+)\\s*:\\s*Ep\\.\\s*(\\d+).*\\((\\d+):(\\d+):?(\\d*)\\).*$");

    @SuppressWarnings("unchecked")
    @Override
    public Episode extract(HtmlNavigator source) {
        try {
            Episode item = new Episode();
            Version version = new Version();
            Encoding encoding = new Encoding();
            Location location = new Location();
        
            location.setPolicy(usPolicy());
            encoding.addAvailableAt(location);
            version.addManifestedAs(encoding);
            item.addVersion(version);

            List<Element> elements = source.allElementsMatching("//li[@class='tags-content-cell']/a");
            Set<String> tags = Sets.newHashSet();
            for (Element element : elements) {
                tags.add("http://www.hulu.com" + element.getAttributeValue("href"));
            }
            item.setTags(tags);

            Element infoElement = source.firstElementOrNull("//span[@class='video-info']");
            if (infoElement != null && infoElement.getValue() != null) {
                Matcher matcher = INFO_PATTERN.matcher(infoElement.getValue());
                if (matcher.matches() && matcher.groupCount() > 3) {
                    item.setSeriesNumber(Integer.valueOf(matcher.group(1)));
                    item.setEpisodeNumber(Integer.valueOf(matcher.group(2)));

                    Integer first = Integer.valueOf(matcher.group(3));
                    Integer second = Integer.valueOf(matcher.group(4));
                    Integer third = null;
                    if (matcher.group(5) != null && matcher.group(5).length() > 0) {
                        third = Integer.valueOf(matcher.group(5));
                    }

                    Integer duration = 0;
                    if (third != null) {
                        duration = first * 60 * 60 + second * 60 + third;
                    } else {
                        duration = first * 60 + second;
                    }
                    version.setDuration(duration);
                }
            }
            
            Element shareElement = source.firstElementOrNull("//div[@id='share-copy-code-div']/script");
            if (shareElement != null && shareElement.getValue() != null) {
                String embedCode = shareElement.getValue();
                String search = "var failsafeEmbedCode = '";
                int index = embedCode.indexOf(search) + search.length();
                
                embedCode = embedCode.substring(index, embedCode.length()).trim();
                location.setEmbedCode(embedCode.substring(0, embedCode.length()-2));
            }

            elements = source.allElementsMatching("//body/script");

            for (Element element : elements) {
                String value = element.getValue();

                if (value.startsWith(SOCIAL_FEED)) {
                    Map<String, Object> attributes;
                    try {
                        attributes = mapper.readValue(value.replace(SOCIAL_FEED, ""), HashMap.class);
                    } catch (Exception e) {
                        throw new FetchException("Unable to map JSON values", e);
                    }

                    if (attributes.containsKey("video") && attributes.get("video") instanceof Map) {
                        Map<String, Object> videoAttributes = (Map<String, Object>) attributes.get("video");
                        if (videoAttributes.containsKey("video_title")) {
                            item.setTitle((String) videoAttributes.get("video_title"));
                        }
                        location.setUri((String) videoAttributes.get("video_src"));
                        location.setAvailable(true);
                        location.setTransportIsLive(true);
                        location.setTransportType(TransportType.EMBED);
                        item.setThumbnail((String) videoAttributes.get("preview_img"));
                        item.setImage((String) videoAttributes.get("preview_img"));
                    }

                    String videoLink = (String) attributes.get("video_link");
                    Matcher matcher = HuluRssAdapter.URI_PATTERN.matcher(videoLink);
                    if (matcher.matches()) {
                        String uri = matcher.group(1);
                        item.setCanonicalUri(uri);
                        item.setCurie("hulu:" + uri.replace(HuluItemAdapter.BASE_URI, ""));
                    }
                    
                    item.addAlias(videoLink);
                    item.setDescription((String) attributes.get("video_description"));
                    item.setPublisher("hulu.com");
                    item.setIsLongForm(true);
                    
                    item.setBrand(brandFrom(attributes));

                    break;
                }
            }

            return item;
        } catch (JaxenException e) {
            throw new FetchException("Unable to navigate HTML document", e);
        }
    }

	private Policy usPolicy() {
		Policy policy = new Policy();
		policy.addAvailableCountry(Countries.US);
		return policy;
	}

	private Brand brandFrom(Map<String, Object> attributes) {
		String brandUri = (String) attributes.get("show_link");
		Brand brand = new Brand(brandUri, PerPublisherCurieExpander.CurieAlgorithm.HULU.compact(brandUri));
		brand.setDescription((String) attributes.get("show_description"));
		brand.setTitle((String) attributes.get("show_title"));
		return brand;
	}
}
