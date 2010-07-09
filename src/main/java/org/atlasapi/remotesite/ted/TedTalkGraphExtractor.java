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

package org.atlasapi.remotesite.ted;

import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.query.content.PerPublisherCurieExpander;
import org.atlasapi.remotesite.html.HtmlDescriptionOfItem;
import org.atlasapi.remotesite.html.HtmlDescriptionSource;

import com.metabroadcast.common.media.MimeType;

public class TedTalkGraphExtractor  {


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
		String uri = src.getUri();
		Item item = new Item(uri, PerPublisherCurieExpander.CurieAlgorithm.TED.compact(uri), Publisher.TED);

		HtmlDescriptionOfItem htmlItem = src.getItem();
		item.setTitle(htmlItem.getTitle());
		item.setDescription(htmlItem.getDescription());
		item.setThumbnail(htmlItem.getThumbnail());
		item.setIsLongForm(true);
		return item;
	}

	
	
	private Encoding encoding() {
		Encoding encoding = new Encoding();
		encoding.setDataContainerFormat(MimeType.VIDEO_MP4);
		return encoding;
	}
	
	private Location embedLocation(HtmlDescriptionOfItem item) {
		Location location = new Location();
		
		location.setUri(item.getVideoSource());
		location.setTransportType(TransportType.EMBED);
		
		location.setEmbedCode(embedCode(item.getFlashFile(), item.getThumbnail()));

		return location;
	}
	
	private Location htmlLinkLocation(String uri) {
		Location location = new Location();
		location.setUri(uri);
		location.setTransportType(TransportType.LINK);
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
