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


package org.uriplay.remotesite.html;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * @author John Ayres (john@metabroadcast.com)
 */
public class HtmlDescriptionOfItem {
	
	private String title;
	private String description;
	
	private List<String> keywords = Lists.newArrayList();
	
	private String author;
	
	private String thumbnail;
	private String videoSource;
	private String flashFile;
	private List<String> locationUris;
	private String embedObject;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void parseKeywords(String csvKeywords) {
		if (csvKeywords != null) {
			String[] parts = csvKeywords.split(",");
			for (String keyword : parts) {
				keywords.add(keyword);
			}
		}
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
	
	public String getThumbnail() {
		return thumbnail;
	}
	
	public void setVideoSource(String videoSource) {
		this.videoSource = videoSource;
	}

	public String getVideoSource() {
		return videoSource;
	}

	public String getFlashFile() {
		return flashFile;
	}
	
	public void setFlashFile(String flashFile) {
		this.flashFile = flashFile;
	}

	public List<String> getLocationUris() {
		return locationUris;
	}

	public void setLocationUris(List<String> locationUris) {
		this.locationUris = locationUris;
	}

	public String getEmbedObject() {
		return this.embedObject;
	}

	public void setEmbedObject(String html) {
		embedObject = html;
	}
	
}
