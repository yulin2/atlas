package org.uriplay.remotesite.itv;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
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

import javax.xml.bind.annotation.XmlType;

import com.google.common.collect.Lists;


/**
 * Representation of an Itv programme (brand) to be bound to xml
 * using JAXB.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@XmlType(name="ITVCatchUpProgramme")
public class ItvProgramme {

	@XmlElement(name="ProgrammeTitle")
	private String title;
	
	@XmlElement(name="ProgrammeMediaUrl")
	private String thumbnail;

	@XmlElement(name="ProgrammeId")
	private int programmeId;

	@XmlElement(name="Url")
	private String url;

	private final List<ItvEpisode> episodes = Lists.newArrayList();
	
	public ItvProgramme() {}
	
	public ItvProgramme(String uri) {
		url = uri;
	}

	public String title() {
		return title;
	}

	public String thumbnail() {
		return thumbnail;
	}

	public int programmeId() {
		return programmeId;
	}

	public List<ItvEpisode> episodes() {
		return episodes;
	}

	public void addEpisode(ItvEpisode itvEpisode) {
		episodes.add(itvEpisode);
	}

	public String url() {
		return url;
	}

	public ItvProgramme withThumbnail(String thumbnailUrl) {
		this.thumbnail = thumbnailUrl;
		return this;
	}
	
}
