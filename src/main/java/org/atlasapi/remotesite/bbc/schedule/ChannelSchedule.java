package org.atlasapi.remotesite.bbc.schedule;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

/**
 * XML binding for BBC schedule XML (JAXB)
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@XmlRootElement(name="schedule")
public class ChannelSchedule {

	@XmlElement(name="day")
	DailySchedule dailySchedule;

	public List<Programme> programmes() {
		List<Broadcast> broadcasts = dailySchedule.broadcasts;
		List<Programme> programmes = Lists.newArrayList();
		for (Broadcast broadcast : broadcasts) {
			programmes.add(broadcast.programme);
		}
		return programmes;
	}
	
	static class DailySchedule {
		
		@XmlElementWrapper(name="broadcasts")
		@XmlElement(name="broadcast")
		List<Broadcast> broadcasts;
	}
	
	static class Broadcast {
		
		public Broadcast() {
		}
		
		public Broadcast(Programme programme) {
			this.programme = programme;
		}

		@XmlElement
		Programme programme;
	}
	
	static class Programme {

		@XmlElement
		String pid;

		@XmlAttribute
		String type;
		
		public Programme() {
		}
		
		public Programme(String type, String pid) {
			this.type = type;
			this.pid = pid;
		}

		public String pid() {
			return pid;
		}
		
		boolean isEpisode() {
			return "episode".equals(type);
		}
		
		boolean isBrand() {
			return "brand".equals(type);
		}
		
	}

	void withProgrammes(List<Programme> programmes) {
		if (dailySchedule == null) {
			dailySchedule = new DailySchedule();
		}
		if (dailySchedule.broadcasts == null) {
			dailySchedule.broadcasts = Lists.newArrayList();
		}
		
		for (Programme programme : programmes) {
			dailySchedule.broadcasts.add(new Broadcast(programme));
		}
	}

}
