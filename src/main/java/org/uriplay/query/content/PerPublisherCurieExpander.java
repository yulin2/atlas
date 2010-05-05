package org.uriplay.query.content;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.uriplay.remotesite.bbc.BbcUriCanonicaliser;
import org.uriplay.remotesite.youtube.YoutubeUriCanonicaliser;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class PerPublisherCurieExpander implements CurieExpander {

	public enum CurieAlgorithm {
		BBC {
			@Override
			public String expand(String curie) {
				return "http://www.bbc.co.uk/programmes/" + curie.substring(4);
			}

			@Override
			public String compact(String url) {
				return "bbc:b00" + BbcUriCanonicaliser.bbcProgrammeIdFrom(url);
			}
		},
		C4 {
			
			final String separator = "_";
			@Override
			public String expand(String curie) {
				String withoutPrefix = curie.substring(curie.indexOf(":") + 1);
				if (withoutPrefix.contains(separator)) {
					String[] components = withoutPrefix.split(separator);
					return String.format("http://www.channel4.com/programmes/%s/4od#%s", components[0], components[1]);
				} else {
					return String.format("http://www.channel4.com/programmes/%s/4od", withoutPrefix);
				}
			}

			final Pattern c4BrandIdPattern = Pattern.compile("http://www.channel4.com/programmes/([^\\./&=]+).*");
			final Pattern c4ItemIdPattern = Pattern.compile("http://www.channel4.com/programmes/[^\\./&=]+/4od#([^\\./&=]+).*");

			@Override
			public String compact(String url) {
				String itemId = matchAgainst(url, c4ItemIdPattern);
				if (itemId == null) {
					return "c4:" + matchAgainst(url, c4BrandIdPattern);
				}
				return "c4:" + matchAgainst(url, c4BrandIdPattern) + separator + itemId;
			}
		},
		ITV {
			@Override
			public String expand(String curie) {
				String withoutPrefix = curie.substring(curie.indexOf(":") + 1);
				if (withoutPrefix.contains("-")) {
					String[] components = withoutPrefix.split("-");
					if ("1".equals(components[0])) {
						return "http://www.itv.com/ITVPlayer/Programmes/default.html?ViewType=1&Filter=" + components[1];
					} 
					
					if ("5".equals(components[0])) {
						return "http://www.itv.com/ITVPlayer/Video/default.html?ViewType=5&Filter=" + components[1];
					}
				}
				
				return null;
			}

			final Pattern itvBrandIdPattern = Pattern.compile("http://www.itv.com/ITVPlayer/Programmes/default.html\\?ViewType=1&Filter=(\\d+)");
			final Pattern itvItemIdPattern = Pattern.compile("http://www.itv.com/ITVPlayer/Video/default.html\\?ViewType=5&Filter=(\\d+)");

			@Override
			public String compact(String url) {
				String itemId = matchAgainst(url, itvItemIdPattern);
				if (itemId != null) {
					return "itv:5-" + itemId;
				}
				String brandId = matchAgainst(url, itvBrandIdPattern);
				if (brandId != null) {
					return "itv:1-" + brandId;
				}

				return null;
			}
		},
		YT {
			@Override
			public String expand(String curie) {
				return "http://www.youtube.com/watch?v=" + curie.substring(3);
			}

			@Override
			public String compact(String url) {
				return "yt:" + YoutubeUriCanonicaliser.videoIdFrom(url);
			}	
		},
		BLIP {
			@Override
			public String expand(String curie) {
				return "http://blip.tv/file/" + curie.substring(5);
			}

			@Override
			public String compact(String url) {
				return "blip:" + removeTrailingSlash(url.replace("http://blip.tv/file/", ""));
			}
		},
		VIM {
			@Override
			public String expand(String curie) {
				return "http://vimeo.com/" + curie.substring(4);
			}

			final Pattern vimeoPattern = Pattern.compile("https?://.*vimeo.com/([^\\./&=]+).*");

			@Override
			public String compact(String url) {
				return "vim:" + matchAgainst(url, vimeoPattern);
			}	
		},
		DM {
			@Override
			public String expand(String curie) {
				return "http://www.dailymotion.com/video/" + curie.substring(3);
			}

			final Pattern dailyMotionPattern = Pattern.compile("https?://.*dailymotion.com/video/([^\\./&=]+).*");

			@Override
			public String compact(String url) {
				return "dm:" + matchAgainst(url, dailyMotionPattern);
			}	
		},
		HULU {
			@Override
			public String expand(String curie) {
				return "http://www.hulu.com/watch/" + curie.substring(5);
			}

			final Pattern huluPattern = Pattern.compile("https?://.*hulu.com/watch/([^\\./&=]+).*");

			@Override
			public String compact(String url) {
				return "hulu:" + matchAgainst(url, huluPattern);
			}	
		},
		TED {
			@Override
			public String expand(String curie) {
				return "http://www.ted.com/talks/" + curie.substring(4) + ".html";
			}

			final Pattern tedPattern = Pattern.compile("https?://.*ted.com/talks/([^\\./&=]+).html");

			@Override
			public String compact(String url) {
				return "ted:" + matchAgainst(url, tedPattern);
			}	
		};
		
		public abstract String expand(String curie);
		public abstract String compact(String url);
		
		private static String removeTrailingSlash(String url) {
			if (url.endsWith("/")) {
				return url.substring(0, url.length() - 1);
			}
			return url;
		}
		
		private static String matchAgainst(String uri, Pattern pattern) {
			Matcher matcher = pattern.matcher(uri);
			if (matcher.matches()) {
				return matcher.group(1);
			}
			return null;
		}

	}
	
	@Override
	public String expand(String curie) {
		String prefix = prefixOf(curie);
		if (prefix == null) {
			return null;
		}
		CurieAlgorithm algorithm;
		try {
			algorithm = CurieAlgorithm.valueOf(prefix.toUpperCase());
		} catch (IllegalArgumentException e) {
			// no matching algorithm
			return null;
		}
		return algorithm.expand(curie);
	}

	private String prefixOf(String curie) {
		int index = curie.indexOf(":");
		if (index < 1) {
			return null;
		}
		return curie.substring(0, index);
	}

}
