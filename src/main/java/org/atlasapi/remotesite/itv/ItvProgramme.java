package org.atlasapi.remotesite.itv;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
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
