/* Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.uriplay.remotesite.ted;

import org.uriplay.media.TransportType;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Version;
import org.uriplay.media.reference.entity.MimeType;
import org.uriplay.query.content.PerPublisherCurieExpander;
import org.uriplay.remotesite.html.HtmlDescriptionOfItem;
import org.uriplay.remotesite.html.HtmlDescriptionSource;

public class TedTalkGraphExtractor  {

	private static final String TED_PUBLISHER = "ted.com";

	public Item extractFrom(HtmlDescriptionSource src) {
		Encoding encoding = encoding();
		encoding.addAvailableAt(embedLocation(src.getItem()));
		encoding.addAvailableAt(htmlLinkLocation(src.getUri()));
		
		Version version = new Version();
		version.addManifestedAs(encoding);
		
		Item item = item(src);
		item.addVersion(version);
		
		return item;
	}
	
	private Item item(HtmlDescriptionSource src) {
		Item item = new Item();
		item.setCanonicalUri(src.getUri());

		HtmlDescriptionOfItem htmlItem = src.getItem();
		item.setTitle(htmlItem.getTitle());
		item.setDescription(htmlItem.getDescription());
		item.setPublisher(TED_PUBLISHER);
		item.setThumbnail(htmlItem.getThumbnail());
		item.setIsLongForm(true);
		addCurieTo(item);
		return item;
	}

	private void addCurieTo(Item item) {
		String curie = PerPublisherCurieExpander.CurieAlgorithm.TED.compact(item.getCanonicalUri());
		if (curie != null) {
			item.setCurie(curie);
		}
	}
	
	private Encoding encoding() {
		Encoding encoding = new Encoding();
		encoding.setDataContainerFormat(MimeType.VIDEO_MP4);
		return encoding;
	}
	
	private Location embedLocation(HtmlDescriptionOfItem item) {
		Location location = new Location();
		
		location.setUri(item.getVideoSource());
		location.setTransportType(TransportType.EMBEDOBJECT);
		
		location.setTransportSubType("html");
		location.setEmbedCode(embedCode(item.getFlashFile(), item.getThumbnail()));

		return location;
	}
	
	private Location htmlLinkLocation(String uri) {
		Location location = new Location();
		location.setUri(uri);
		location.setTransportType(TransportType.HTMLEMBED);
		return location;
	}

	private String embedCode(String flashFile, String thumbnail) {
		
		return "<object width=\"334\" height=\"326\">" +
				  "<param name=\"movie\" value=\"http://video.ted.com/assets/player/swf/EmbedPlayer.swf\"></param>" +
				  "<param name=\"allowFullScreen\" value=\"true\" />" +
				  "<param name=\"wmode\" value=\"transparent\"></param>" +
				  "<param name=\"bgColor\" value=\"#ffffff\"></param>" +
				  "<param name=\"flashvars\" value=\"vu=" + flashFile + "&su=" + thumbnail + "&vw=320&vh=240&ap=0&ti=38\" />" +
				  "<embed src=\"http://video.ted.com/assets/player/swf/EmbedPlayer.swf\" pluginspace=\"http://www.macromedia.com/go/getflashplayer\" type=\"application/x-shockwave-flash\" wmode=\"transparent\" bgColor=\"#ffffff\" width=\"334\" height=\"326\" allowFullScreen=\"true\" flashvars=\"vu=" + flashFile + "&su=" + thumbnail + "&vw=320&vh=240&ap=0&ti=38\">" +
				  "</embed>" +
				"</object>";
	}
}
