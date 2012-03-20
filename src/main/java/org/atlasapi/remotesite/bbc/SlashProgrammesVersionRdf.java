/* Copyright 2009 British Broadcasting Corporation
   Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.atlasapi.remotesite.bbc;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.atlasapi.media.vocabulary.PO;
import org.atlasapi.media.vocabulary.RDF;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesTopic;
import org.joda.time.DateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.time.DateTimeZones;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@XmlRootElement(name="RDF", namespace=RDF.NS)
class SlashProgrammesVersionRdf {
	
	@XmlElement(namespace=PO.NS, name="Broadcast")
	List<BbcBroadcast> broadcasts;
	
	List<BbcBroadcast> broadcastSlots() {
		return broadcasts;
	}
	
	@XmlElement(namespace=PO.NS, name="Version")
	BbcVersion version;
	
	String pid() {
	    if (version != null) {
	        return version.pid();
	    }
	    return null;
	}
	
	static class BbcVersion {
	    @XmlElement(namespace=PO.NS, name="pid")
	    String pid;

        @XmlElement(namespace = PO.NS, name = "duration")
        String duration;

	    String pid() {
	        return pid;
	    }
	    
	    String duration() {
	        return duration;
	    }
	}
	
	static class BbcBroadcast {
		
		@XmlElement(namespace="http://purl.org/NET/c4dm/event.owl#", name="time")
		Event event;

		@XmlElement(namespace=PO.NS, name="schedule_date")
		String scheduleDate;
		
		@XmlElement(namespace=PO.NS, name="broadcast_on")
		BroadcastOn broadcastOn;
		
		@XmlElement(namespace=RDF.NS, name="type")
        BroadcastType broadcastType;
		
		public BbcBroadcast atTime(String time) {
			Interval interval =  new Interval();
			interval.startTime = time;
			return atTime(interval);
		}
		
		public BbcBroadcast atTime(Interval invterval) {
			this.event = new Event();
			this.event.interval = invterval;
			return this;
		}

		public BbcBroadcast onChannel(String channel) {
			broadcastOn = new BroadcastOn();
			broadcastOn.service = new Service();
			broadcastOn.service.resourceUri = channel;
			return this;
		}
		
		public DateTime broadcastDateTime() {
			return new DateTime(event.interval.startTime, DateTimeZones.UTC);
		}
		
		public DateTime broadcastEndDateTime() {
			return new DateTime(event.interval.endTime, DateTimeZones.UTC);
		}

		public String scheduleDate() {
			return scheduleDate;
		}

		public String broadcastOn() {
			if (broadcastOn != null && broadcastOn.service != null) {
				return broadcastOn.service.resourceUri;
			}
			return null;
		}

		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}
		
		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}

        public BroadcastType broadcastType() {
            return broadcastType;
        }

        public void setBroadcastType(BroadcastType broadcastType) {
            this.broadcastType = broadcastType;
        }

	}
	
	static class BroadcastType {

        @XmlAttribute(name = "resource", namespace = RDF.NS)
        private String resourceUri;

        public String resourceUri() {
            return resourceUri;
        }

        public BroadcastType withResourceUri(String uri) {
            resourceUri = uri;
            return this;
        }
        
        public Boolean isRepeatType() {
            if ("http://purl.org/ontology/po/RepeatBroadcast".equals(resourceUri)) {
                return true;
            }
            if ("http://purl.org/ontology/po/FirstBroadcast".equals(resourceUri)) {
                return false;
            }
            return null;
        }
    }
	
	static class Event {
		
		@XmlElement(namespace="http://purl.org/NET/c4dm/timeline.owl#", name="Interval")
		Interval interval;

		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}
		
		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}
	}
	
	static class BroadcastOn {
		
	    @XmlElement(namespace=PO.NS, name="Service")
		Service service;
		
		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}
		
		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}
		
	}
	
	static class Service {
	    @XmlAttribute(namespace=RDF.NS, name="about")
        String resourceUri;
	    
	    @Override
        public boolean equals(Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj);
        }
        
        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
	}
	
	static class Interval {
		
		@XmlElement(name="start", namespace="http://purl.org/NET/c4dm/timeline.owl#")
		String startTime;
		
		@XmlElement(name="end", namespace="http://purl.org/NET/c4dm/timeline.owl#")
		String endTime;
		
		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}
		
		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}
	}

	public SlashProgrammesVersionRdf withLastTransmitted(DateTime lastTx, String channel) {
		this.broadcasts = Lists.newArrayList(new BbcBroadcast().atTime(lastTx.toString()).onChannel(channel));
		return this;
	}

	public DateTime lastTransmitted() {
		
		if (broadcasts == null || broadcasts.isEmpty()) { return null; }
		
		Collections.sort(broadcasts, new ByTransmissionDateComparator());
		
		return Iterables.getLast(broadcasts).broadcastDateTime();
	}
	
	static class ByTransmissionDateComparator implements Comparator<BbcBroadcast> {

		public int compare(BbcBroadcast o1, BbcBroadcast o2) {
			return new DateTime(o1.event.interval.startTime).compareTo(new DateTime(o2.event.interval.startTime));
		}
	}
}
